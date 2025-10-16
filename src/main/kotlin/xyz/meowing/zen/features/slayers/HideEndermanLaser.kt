package xyz.meowing.zen.features.slayers

import xyz.meowing.zen.Zen
import xyz.meowing.zen.api.EntityDetection
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.*
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.features.slayers.carrying.CarryCounter
import xyz.meowing.zen.utils.TickUtils
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.entity.monster.EntityEnderman
import java.util.concurrent.ConcurrentHashMap

@Zen.Module
object HideEndermanLaser : Feature("hideendermanlaser", true) {
    private val hideForOption by ConfigDelegate<Int>("hideendermanlaserboss")
    private val endermanCache = ConcurrentHashMap<Int, EntityEnderman>()
    private val nametagData = mutableMapOf<Int, String>()
    private var lastCacheUpdate = 0
    private var cacheInitialized = false

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Slayers", "Hide Enderman Laser", ConfigElement(
                "hideendermanlaser",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("Slayers", "Hide Enderman Laser", "Options", ConfigElement(
                "hideendermanlaserboss",
                "Hide For",
                ElementType.Dropdown(
                    listOf("All bosses", "Carries", "Mine", "Mine and carries", "Not mine/carries"),
                    0
                )
            ))
    }

    override fun initialize() {
        register<RenderEvent.GuardianLaser> { event ->
            val guardianEntity = event.entity
            val closestSlayer = getCachedClosestEnderman(guardianEntity)

            if (closestSlayer != null && shouldHideLaser(closestSlayer.entityId)) {
                event.cancel()
            }
        }

        register<WorldEvent.Change> {
            clearCache()
            cacheInitialized = false
        }

        register<EntityEvent.Metadata> { event ->
            nametagData[event.entity.entityId] = event.name.removeFormatting()
        }

        register<SkyblockEvent.Slayer.Spawn> { event ->
            if (event.entity is EntityEnderman) {
                updateCache()
                cacheInitialized = true
            }
        }
    }

    private fun getCachedClosestEnderman(guardianEntity: net.minecraft.entity.Entity): EntityEnderman? {
        val currentTick = TickUtils.getCurrentServerTick().toInt()
        if (!cacheInitialized || currentTick - lastCacheUpdate >= 5) {
            updateCache()
            lastCacheUpdate = currentTick
            cacheInitialized = true
        }

        return endermanCache.values.minByOrNull { guardianEntity.getDistanceSqToEntity(it) }
    }

    private fun updateCache() {
        val slayerEntities = EntityDetection.getSlayerEntities()

        endermanCache.clear()
        slayerEntities.keys.filterIsInstance<EntityEnderman>().forEach { enderman ->
            endermanCache[enderman.entityId] = enderman
        }
    }

    private fun shouldHideLaser(slayerEntityId: Int): Boolean {
        if (hideForOption == 0) return true

        val spawnerNametag = nametagData[slayerEntityId + 3] ?: ""
        if (!spawnerNametag.contains("Spawned by")) return false

        val playerName = player?.name ?: return false
        val cleanSpawnerName = spawnerNametag.removeFormatting()
        val cleanPlayerName = playerName.removeFormatting()

        val isMyBoss = cleanSpawnerName.endsWith("by: $cleanPlayerName")
        val isCarryBoss = CarryCounter.carryees.any {
            cleanSpawnerName.endsWith("by: ${it.name.removeFormatting()}")
        }

        return when (hideForOption) {
            1 -> isCarryBoss
            2 -> isMyBoss
            3 -> isMyBoss || isCarryBoss
            4 -> !isMyBoss && !isCarryBoss
            else -> false
        }
    }

    fun clearCache() {
        endermanCache.clear()
        nametagData.clear()
    }

    override fun onUnregister() {
        super.onUnregister()
        clearCache()
    }
}