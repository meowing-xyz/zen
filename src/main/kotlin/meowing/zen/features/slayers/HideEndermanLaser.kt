package meowing.zen.features.slayers
import meowing.zen.Zen
import meowing.zen.api.EntityDetection
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.events.WorldEvent
import meowing.zen.features.Feature
import meowing.zen.features.slayers.carrying.CarryCounter
import net.minecraft.entity.monster.EntityEnderman
import java.util.concurrent.ConcurrentHashMap

@Zen.Module
object HideEndermanLaser : Feature("hideendermanlaser", true) {
    private val hideForOption by ConfigDelegate<Int>("hideendermanlaserboss")
    private val endermanCache = ConcurrentHashMap<Int, EntityEnderman>()
    private val spawnerCache = ConcurrentHashMap<Int, String>()

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
        val slayerEntities = EntityDetection.getSlayerEntities()

        endermanCache.keys.retainAll(slayerEntities.keys.map { it.entityId }.toSet())

        slayerEntities.keys.filterIsInstance<EntityEnderman>().forEach { enderman ->
            endermanCache[enderman.entityId] = enderman
        }

        return endermanCache.values.minByOrNull { guardianEntity.getDistanceToEntity(it) }
    }

    private fun shouldHideLaser(slayerEntityId: Int): Boolean {
        if (hideForOption == 0) return true

        val spawnerNametag = getCachedSpawnerNametag(slayerEntityId)
        if (!spawnerNametag.contains("Spawned by")) return false

        val playerName = player?.name ?: return true

        return when (hideForOption) {
            1 -> CarryCounter.carryees.any { spawnerNametag.endsWith("by: ${it.name}") }
            2 -> spawnerNametag.endsWith("by: $playerName")
            3 -> spawnerNametag.endsWith("by: $playerName") || CarryCounter.carryees.any { spawnerNametag.endsWith("by: ${it.name}") }
            4 -> !spawnerNametag.endsWith("by: $playerName") && !CarryCounter.carryees.any { spawnerNametag.endsWith("by: ${it.name}") }
            else -> false
        }
    }

    private fun getCachedSpawnerNametag(slayerEntityId: Int): String {
        return spawnerCache.getOrPut(slayerEntityId) {
            world?.getEntityByID(slayerEntityId + 3)?.customNameTag ?: ""
        }
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