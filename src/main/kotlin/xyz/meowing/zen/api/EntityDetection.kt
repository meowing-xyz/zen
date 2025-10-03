package xyz.meowing.zen.api

import xyz.meowing.zen.Zen
import xyz.meowing.zen.Zen.Companion.mc
import xyz.meowing.zen.events.*
import xyz.meowing.zen.events.EventBus.post
import xyz.meowing.zen.utils.TickUtils
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.projectile.EntityArrow

@Zen.Module
object EntityDetection {
    private val hashMap = HashMap<Entity, SkyblockMob>()
    private val slayerEntities = HashMap<Entity, SkyblockMob>()
    private val normalMobRegex = "\\[Lv\\d+k?] (?:[Ж༕ൠ☮⊙Ž✰♨⚂❆☽✿☠⸕⚓♆♣⚙\uFE0E♃⛨✈⸙]+ )?(.+?) [\\d.,]+[MkB]?/[\\d.,]+[MkB]?❤".toRegex()
    private val slayerMobRegex = "(?<=☠\\s)[A-Za-z]+\\s[A-Za-z]+(?:\\s[IVX]+)?".toRegex()
    private val dungeonMobRegex = "(?:[Ж༕ൠ☮⊙Ž✰♨⚂❆☽✿☠⸕⚓♆♣⚙︎♃⛨✈⸙]+ )?✯?\\s*(?:Flaming|Super|Healing|Boomer|Golden|Speedy|Fortified|Stormy|Healthy)?\\s*([\\w\\s]+?)\\s*([\\d.,]+[mkM?]*|[?]+)❤".toRegex()
    private val patterns = listOf(normalMobRegex, slayerMobRegex, dungeonMobRegex)
    private var inSlayerFight = false
    private var SlayerEntity: Entity? = null
    var bossID: Int? = null
        private set

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
                    post(SkyblockEvent.EntitySpawn(skyblockMob))
                }
            }
        }

        TickUtils.loop(100) {
            bossID?.let { id ->
                val world = mc.theWorld ?: return@loop
                val boss = world.getEntityByID(id)
                if (boss == null || boss.isDead) {
                    post(SkyblockEvent.Slayer.Cleanup())
                    bossID = null
                }
            }
        }

        EventBus.register<EntityEvent.Metadata> { event ->
            if (inSlayerFight) return@register
            val world = mc.theWorld ?: return@register
            val player = mc.thePlayer ?: return@register
            val name = event.name

            if (name.contains("Spawned by") && name.endsWith("by: ${player.name}")) {
                val hasBlackhole = world.loadedEntityList.any {
                    event.entity.getDistanceToEntity(it) <= 3f && it.name?.removeFormatting()?.contains("black hole", true) == true
                }

                if (!hasBlackhole) {
                    bossID = event.packet.entityId - 3
                    SlayerEntity = world.getEntityByID(event.packet.entityId - 3)
                    inSlayerFight = true
                    post(SkyblockEvent.Slayer.Spawn(event.entity, event.entity.entityId, event.packet))
                }
            }
        }

        EventBus.register<EntityEvent.Leave> { event ->
            if (event.entity.entityId == bossID && inSlayerFight) {
                bossID = null
                SlayerEntity = null
                inSlayerFight = false
                post(SkyblockEvent.Slayer.Death(event.entity, event.entity.entityId))
            }
        }

        EventBus.register<ChatEvent.Receive> { event ->
            when (event.event.message.unformattedText.removeFormatting()) {
                "  SLAYER QUEST FAILED!" -> {
                    bossID = null
                    SlayerEntity = null
                    inSlayerFight = false
                    post(SkyblockEvent.Slayer.Fail())
                }
                "  SLAYER QUEST STARTED!" -> {
                    bossID = null
                    SlayerEntity = null
                    inSlayerFight = false
                    post(SkyblockEvent.Slayer.QuestStart())
                }
            }
        }

        EventBus.register<WorldEvent.Change> {
            hashMap.clear()
        }

        EventBus.register<EntityEvent.Leave> { event ->
            hashMap.remove(event.entity)
            slayerEntities.remove(event.entity)
            hashMap.entries.removeAll { it.value.skyblockMob == event.entity }
        }
    }

    private fun updateMobData(sbMob: SkyblockMob) {
        val rawMobName = sbMob.nameEntity.displayName.unformattedText.removeFormatting().replace(",", "")

        patterns.forEachIndexed { index, pattern ->
            pattern.find(rawMobName)?.let { match ->
                sbMob.id = when (index) {
                    0 -> match.groupValues[1]
                    1 -> {
                        match.value.also {
                            sbMob.id = it
                            slayerEntities[sbMob.skyblockMob] = sbMob
                        }
                    }
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
    fun getSlayerEntities(): Map<Entity, SkyblockMob> = slayerEntities
    fun getNameTag(entity: Entity): SkyblockMob? = hashMap.values.firstOrNull { it.nameEntity == entity }
    fun getSlayerEntity(): Entity? = SlayerEntity
}