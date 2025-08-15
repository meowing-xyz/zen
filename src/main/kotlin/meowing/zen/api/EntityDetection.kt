package meowing.zen.api

import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.events.EntityEvent
import meowing.zen.events.EventBus
import meowing.zen.events.SkyblockEvent
import meowing.zen.events.WorldEvent
import meowing.zen.utils.TickUtils
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.projectile.EntityArrow

@Zen.Module
object EntityDetection {
    private val hashMap = HashMap<Entity, SkyblockMob>()
    private val normalMobRegex = "\\[Lv\\d+k?] (?:[༕ൠ☮⊙Ž✰♨⚂❆☽✿☠⸕⚓♆♣⚙\uFE0E♃⛨✈⸙] )?(.+?) [\\d.,]+[MkB]?/[\\d.,]+[MkB]?❤".toRegex()
    private val slayerMobRegex = "(?<=☠\\s)[A-Za-z]+\\s[A-Za-z]+(?:\\s[IVX]+)?".toRegex()
    private val dungeonMobRegex = "(?:[༕ൠ☮⊙Ž✰♨⚂❆☽✿☠⸕⚓♆♣⚙︎♃⛨✈⸙] )?✯?\\s*(?:Flaming|Super|Healing|Boomer|Golden|Speedy|Fortified|Stormy|Healthy)?\\s*([\\w\\s]+?)\\s*([\\d.,]+[mkM?]*|[?]+)❤".toRegex()
    private val patterns = listOf(normalMobRegex, slayerMobRegex, dungeonMobRegex)

    class SkyblockMob(val nameEntity: Entity, val skyblockMob: Entity) {
        var id: String? = null
    }

    init {
        TickUtils.loop(5) {
            val world = mc.theWorld ?: return@loop
            val player = mc.thePlayer ?: return@loop

            world.loadedEntityList.forEach { entity ->
                if (player.getDistanceToEntity(entity) > 30 || entity !is EntityArmorStand || !entity.hasCustomName() || hashMap.containsKey(entity)) return@forEach
                val nameTag = entity.customNameTag
                val mobId = if (nameTag.contains("Withermancer")) entity.entityId - 3 else entity.entityId - 1
                val mob = world.getEntityByID(mobId) ?: return@forEach

                if (!mob.isEntityAlive || mob is EntityArrow) return@forEach

                val skyblockMob = SkyblockMob(entity, mob)
                hashMap[entity] = skyblockMob
                updateMobData(skyblockMob)

                if (skyblockMob.id != null) {
                    EventBus.post(SkyblockEvent.EntitySpawn(skyblockMob))
                }
            }
        }

        EventBus.register<WorldEvent.Load> ({
            hashMap.clear()
        })

        EventBus.register<EntityEvent.Leave> ({ event ->
            hashMap.remove(event.entity)
            hashMap.entries.removeAll { it.value.skyblockMob == event.entity }
        })
    }

    private fun updateMobData(sbMob: SkyblockMob) {
        val rawMobName = sbMob.nameEntity.displayName.unformattedText.removeFormatting().replace(",", "")

        patterns.forEachIndexed { index, pattern ->
            pattern.find(rawMobName)?.let { match ->
                sbMob.id = when (index) {
                    0 -> match.groupValues[1]
                    1 -> "${match.value} Slayer"
                    2 -> {
                        val mobName = match.groupValues[1]
                        if (rawMobName.startsWith("ൠ")) "$mobName Pest" else mobName
                    }
                    else -> return
                }

                sbMob.id?.let { id ->
                    if (id.startsWith("a") && id.length > 2 && Character.isUpperCase(id[1])) {
                        sbMob.id = id.substring(1, id.length - 2)
                    }
                }
                return
            }
        }
    }

    inline val Entity.sbMobID: String? get() = getSkyblockMob(this)?.id

    fun getSkyblockMob(entity: Entity): SkyblockMob? = hashMap.values.firstOrNull { it.skyblockMob == entity }
    fun getNameTag(entity: Entity): SkyblockMob? = hashMap.values.firstOrNull { it.nameEntity == entity }
}