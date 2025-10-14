package xyz.meowing.zen.features.slayers

import xyz.meowing.zen.Zen
import xyz.meowing.zen.api.EntityDetection
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.events.WorldEvent
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
    private val spawnerCache = ConcurrentHashMap<Int, String>()
    private var lastCacheUpdate = 0

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

            if (closestSlayer != null && shouldHideLaser(closestSlayer.entityId)) event.cancel()
        }

        register<WorldEvent.Change> {
            clearCache()
        }
    }

    private fun getCachedClosestEnderman(guardianEntity: net.minecraft.entity.Entity): EntityEnderman? {
        val currentTick = TickUtils.getCurrentServerTick().toInt()

        if (currentTick - lastCacheUpdate >= 100) {
           clearCache()
           lastCacheUpdate = currentTick
        }

        if (currentTick - lastCacheUpdate >= 10) {
            lastCacheUpdate = currentTick
            val slayerEntities = EntityDetection.getSlayerEntities()
            endermanCache.keys.retainAll(slayerEntities.keys.map { it.entityId }.toSet())
            slayerEntities.keys.filterIsInstance<EntityEnderman>().forEach { enderman ->
                endermanCache[enderman.entityId] = enderman
            }
        }
        return endermanCache.values.minByOrNull { guardianEntity.getDistanceSqToEntity(it) }
    }

    private fun shouldHideLaser(slayerEntityId: Int): Boolean {
        if (hideForOption == 0) return true

        val spawnerNametag = getCachedSpawnerNametag(slayerEntityId)
        if (!spawnerNametag.contains("Spawned by")) return false

        val playerName = player?.name ?: return true

        val cleanSpawnerName = spawnerNametag.removeFormatting()
        val cleanPlayerName = playerName.removeFormatting()

        return when (hideForOption) {
            1 -> CarryCounter.carryees.any { cleanSpawnerName.endsWith("by: ${it.name.removeFormatting()}") }
            2 -> cleanSpawnerName.endsWith("by: $cleanPlayerName")
            3 -> cleanSpawnerName.endsWith("by: $cleanPlayerName") || CarryCounter.carryees.any { cleanSpawnerName.endsWith("by: ${it.name.removeFormatting()}") }
            4 -> !cleanSpawnerName.endsWith("by: $cleanPlayerName") && !CarryCounter.carryees.any { cleanSpawnerName.endsWith("by: ${it.name.removeFormatting()}") }
            else -> false
        }
    }

    private fun getCachedSpawnerNametag(slayerEntityId: Int): String {
        val cached = spawnerCache[slayerEntityId]
        if (cached != null && cached.isNotEmpty()) return cached

        val entity = world?.getEntityByID(slayerEntityId + 3) ?: return ""
        val nameTag = entity.customNameTag ?: return ""

        if (nameTag.contains("Spawned by") && !nameTag.removeFormatting().contains("Armorstand")) {
            spawnerCache[slayerEntityId] = nameTag
            return nameTag
        }

        return ""
    }

    fun clearCache() {
        endermanCache.clear()
        spawnerCache.clear()
    }

    override fun onUnregister() {
        super.onUnregister()
        clearCache()
    }
}