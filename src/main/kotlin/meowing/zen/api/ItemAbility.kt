package meowing.zen.api

import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.events.*
import meowing.zen.utils.DungeonUtils
import meowing.zen.utils.ItemUtils.lore
import meowing.zen.utils.ItemUtils.skyblockID
import meowing.zen.utils.LocationUtils
import meowing.zen.utils.TickUtils
import meowing.zen.utils.DungeonUtils.isMage
import meowing.zen.utils.SimpleTimeMark
import meowing.zen.utils.TimeUtils
import meowing.zen.utils.TimeUtils.millis
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.item.ItemStack
import net.minecraftforge.event.entity.player.PlayerInteractEvent

@Zen.Module
object ItemAbility {
    private val cooldowns = hashMapOf<String, CooldownItem>()
    private val activeCooldowns = hashMapOf<String, Double>()
    private var justUsedAbility: ItemAbility? = null
    private var cooldownReduction = -1

    data class CooldownItem(
        var sneakRightClick: ItemAbility? = null,
        var sneakLeftClick: ItemAbility? = null,
        var rightClick: ItemAbility? = null,
        var leftClick: ItemAbility? = null
    )

    data class ItemAbility(
        val itemId: String,
        var cooldownSeconds: Double = 0.0,
        var currentCount: Double = 0.0,
        var manaCost: Int = 0,
        var usedAt: SimpleTimeMark = TimeUtils.now,
        var abilityName: String = "Unknown",
        var type: String? = null
    )

    private fun sendItemAbilityEvent(ability: ItemAbility) {
        if (ability.manaCost > PlayerStats.mana) return

        EventBus.post(SkyblockEvent.ItemAbilityUsed(ability))
        justUsedAbility = ability
        activeCooldowns[ability.abilityName] = ability.cooldownSeconds
    }

    init {
        TickUtils.loop(10) {
            if (mc.thePlayer == null || mc.theWorld == null) return@loop

            activeCooldowns.replaceAll { _, cooldown -> updateCooldown(cooldown) }
            activeCooldowns.clear()

            for (i in 0..7) {
                if (mc.thePlayer.inventory.mainInventory[i] == null) continue

                val stack: ItemStack = mc.thePlayer.inventory.mainInventory[i]
                setStackCooldown(stack)
                val skyblockId: String? = stack.skyblockID

                if (skyblockId != null && cooldowns[skyblockId] != null) {
                    val cdSeconds = cooldowns[skyblockId]?.rightClick?.cooldownSeconds ?: 0.0
                    val abilityName = cooldowns[skyblockId]?.rightClick?.abilityName ?: "Unknown"
                    activeCooldowns[abilityName] = cdSeconds / 2.0
                }
            }
        }

        EventBus.register<WorldEvent.Load> ({
            activeCooldowns.clear()
            cooldowns.clear()
            cooldownReduction = -1
        })

        EventBus.register<MouseEvent.Click> ({ event ->
            if (mc.theWorld == null) return@register
            val heldItem = mc.thePlayer.heldItem ?: return@register
            val skyblockId = heldItem.skyblockID
            val cdItem = cooldowns[skyblockId] ?: return@register
            val sneaking = mc.thePlayer.isSneaking

            if (event.event.button == 0) {
                if (sneaking) {
                    cdItem.sneakLeftClick?.let { sendItemAbilityEvent(it) }
                } else if (cdItem.leftClick != null) {
                    sendItemAbilityEvent(cdItem.leftClick!!)
                }
            }
        })

        EventBus.register<EntityEvent.Interact> ({ event ->
            if (mc.theWorld == null || event.action != PlayerInteractEvent.Action.RIGHT_CLICK_AIR) return@register
            val heldItem = mc.thePlayer.heldItem ?: return@register
            val skyblockId = heldItem.skyblockID
            val cdItem = cooldowns[skyblockId] ?: return@register

            if (mc.thePlayer?.isSneaking == true) cdItem.sneakRightClick?.let {
                sendItemAbilityEvent(it)
            } else if (cdItem.rightClick != null) {
                sendItemAbilityEvent(cdItem.rightClick!!)
            }
        })

        EventBus.register<ChatEvent.Receive> ({ event ->
            if (event.event.type.toInt() == 2) return@register
            val clean = event.event.message.unformattedText.removeFormatting()

            if (clean.startsWith("Used") && LocationUtils.checkArea("catacombs"))
                justUsedAbility = ItemAbility("Dungeon_Ability")

            justUsedAbility?.let { ability ->
                val skyblockId = mc.thePlayer.heldItem?.skyblockID ?: return@register
                if (ability.itemId == skyblockId && clean.startsWith("This ability is on cooldown for") && ability.usedAt.since.millis <= 300) {
                    val currentCooldown = clean.replace("[^0-9]".toRegex(), "").toInt()
                    ability.currentCount = ability.cooldownSeconds - currentCooldown
                    activeCooldowns[ability.abilityName] = currentCooldown.toDouble()
                }
            }
        })
    }

    private fun setItemAbility(line: String, cdItem: CooldownItem, skyblockId: String) {
        val abilityName = line.split(": ")[1].split(" {2}")[0]
        val ability = ItemAbility(skyblockId, abilityName = abilityName)

        when {
            line.endsWith("RIGHT CLICK") -> cdItem.rightClick = ability
            line.endsWith("LEFT CLICK") -> cdItem.leftClick = ability
            line.endsWith("SNEAK RIGHT CLICK") -> cdItem.sneakRightClick = ability
            line.endsWith("SNEAK LEFT CLICK") -> cdItem.sneakLeftClick = ability
        }
    }

    private fun setCooldownSeconds(clean: String, cdItem: CooldownItem) {
        val cooldownSeconds = clean.replace("[^0-9]".toRegex(), "").toInt().toDouble()
        listOfNotNull(cdItem.rightClick, cdItem.leftClick, cdItem.sneakRightClick, cdItem.sneakLeftClick).forEach { it.cooldownSeconds = cooldownSeconds }
    }

    private fun setManaCost(clean: String, cdItem: CooldownItem) {
        val manaCost = clean.replace("[^0-9]".toRegex(), "").toInt()
        listOfNotNull(cdItem.rightClick, cdItem.leftClick, cdItem.sneakRightClick, cdItem.sneakLeftClick).forEach { it.manaCost = manaCost }
    }

    private fun setStackCooldown(item: ItemStack) {
        if (mc.theWorld == null) return
        val skyblockId = item.skyblockID
        if (cooldowns.containsKey(skyblockId)) return
        val cdItem = CooldownItem()

        item.lore.forEach { line ->
            val clean = line.removeFormatting()
            when {
                clean.contains("Ability: ") -> setItemAbility(clean, cdItem, skyblockId)
                clean.contains("Cooldown: ") -> setCooldownSeconds(clean, cdItem)
                clean.contains("Mana Cost: ") -> setManaCost(clean, cdItem)
            }
        }

        if (listOf(cdItem.rightClick, cdItem.leftClick, cdItem.sneakRightClick, cdItem.sneakLeftClick).any { it != null }) {
            cooldowns[skyblockId] = cdItem
        }
    }

    private fun updateCooldown(cooldownCount: Double): Double {
        var secondsToAdd = 0.05

        if (LocationUtils.checkArea("catacombs") && cooldownReduction == -1 && isMage()) {
            cooldownReduction = (DungeonUtils.getCurrentLevel() / 2) + 25
            if (!DungeonUtils.isDuplicate("mage")) cooldownReduction += 25
        }

        if (cooldownReduction != -1) secondsToAdd *= (100.0 + cooldownReduction) / cooldownReduction

        return cooldownCount - secondsToAdd
    }
}