package meowing.zen.features.general

import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.Zen.Companion.prefix
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.EntityEvent
import meowing.zen.events.GuiEvent
import meowing.zen.features.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.CommandUtils
import meowing.zen.utils.DataUtils
import meowing.zen.utils.ItemUtils.lore
import meowing.zen.utils.ItemUtils.uuid
import meowing.zen.utils.Utils.chestName
import net.minecraft.command.ICommandSender
import net.minecraft.init.Blocks
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.Item

/**
 * Module contains code from Skytils
 *
 * @license GPL-3.0
 */
@Zen.Module
object ProtectItem : Feature("protectitem", true) {
    val protectedItems = DataUtils("protected_items", mutableSetOf<String>())

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Item Protection", ConfigElement(
                "protectitem",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("General", "Item Protection", "", ConfigElement(
                "",
                null,
                ElementType.TextParagraph("Tries to prevent you from dropping items that you have protected using §c/protectitem\n§7Aliases: /pitem, /zenpi")
            ))
    }

    override fun initialize() {
        register<EntityEvent.ItemToss> { event ->
            val itemUuid = event.stack.uuid
            if (itemUuid.isNotEmpty() && itemUuid in protectedItems()) {
                sendProtectionMessage("dropping", event.stack.displayName)
                event.cancel()
            }
        }

        register<GuiEvent.Close> { event ->
            val item = player?.inventory?.itemStack ?: return@register
            val itemUuid = item.uuid
            if (itemUuid.isNotEmpty() && itemUuid in protectedItems()) {
                for (slot in event.container.inventorySlots) {
                    if (slot.inventory !== player?.inventory || slot.hasStack || !slot.isItemValid(item)) continue
                    mc.playerController.windowClick(event.container.windowId, slot.slotNumber, 0, 0, player)
                    sendProtectionMessage("dropping", item.displayName)
                    event.cancel()
                    return@register
                }
            }
        }

        register<GuiEvent.Slot.Click> { event ->
            if (event.container is ContainerChest) handleChestClick(event)
            handleDropClick(event)
        }
    }

    private fun handleChestClick(event: GuiEvent.Slot.Click) {
        val container = event.container as ContainerChest
        val inv = container.lowerChestInventory
        val chestName = event.gui.chestName
        val slot = event.slot ?: return
        val item = slot.stack ?: return
        val itemUuid = item.uuid

        if (!slot.hasStack || itemUuid.isEmpty() || itemUuid !in protectedItems()) return

        when {
            chestName.startsWith("Salvage") -> {
                val inSalvageGui = item.displayName.contains("Salvage") || item.displayName.contains("Essence")
                if (inSalvageGui || slot.inventory === player?.inventory) {
                    sendProtectionMessage("salvaging", item.displayName)
                    event.cancel()
                }
            }

            chestName != "Large Chest" && inv.sizeInventory == 54 && !chestName.contains("Auction") -> {
                val sellItem = inv.getStackInSlot(49)
                val isSellGui = sellItem?.item === Item.getItemFromBlock(Blocks.hopper) && (sellItem.displayName.contains("Sell Item") || sellItem.lore.any { it.contains("buyback") })

                if (isSellGui && event.slotId != 49 && slot.inventory === mc.thePlayer.inventory) {
                    sendProtectionMessage("selling", item.displayName)
                    event.cancel()
                }
            }

            chestName.startsWith("Create ") && chestName.endsWith(" Auction") -> {
                if (inv.getStackInSlot(13) != null) {
                    sendProtectionMessage("auctioning", item.displayName)
                    event.cancel()
                }
            }
        }
    }

    private fun handleDropClick(event: GuiEvent.Slot.Click) {
        val item = when {
            event.slotId == -999 && player?.inventory?.itemStack != null && event.clickType != 5 -> player?.inventory?.itemStack
            event.clickType == 4 && event.slotId != -999 && event.slot?.hasStack == true -> event.slot.stack
            else -> return
        } ?: return

        val itemUUID = item.uuid
        if (itemUUID.isNotEmpty() && itemUUID in protectedItems()) {
            sendProtectionMessage("dropping", item.displayName)
            event.cancel()
        }
    }

    private fun sendProtectionMessage(action: String, itemName: String) {
        ChatUtils.addMessage("$prefix §fStopped you from $action $itemName§r!")
    }
}

@Zen.Command
object ProtectItemCommand : CommandUtils("protectitem", aliases = listOf("zenprotect", "pitem", "zenpi")) {
    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        val heldItem = mc.thePlayer?.heldItem
        if (heldItem == null) {
            ChatUtils.addMessage("$prefix §cYou must be holding an item!")
            return
        }

        val itemUUID = heldItem.uuid
        if (itemUUID.isEmpty()) {
            ChatUtils.addMessage("$prefix §cThis item doesn't have a UUID!")
            return
        }

        ProtectItem.protectedItems.update {
            if (itemUUID in this) {
                remove(itemUUID)
                ChatUtils.addMessage("$prefix §fRemoved ${heldItem.displayName} §ffrom protected items!")
            } else {
                add(itemUUID)
                ChatUtils.addMessage("$prefix §fAdded ${heldItem.displayName} §fto protected items!")
            }
        }
    }
}