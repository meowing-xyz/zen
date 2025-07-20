package meowing.zen.feats.dungeons

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import meowing.zen.events.EventBus
import meowing.zen.events.RenderEvent
import meowing.zen.events.TickEvent
import meowing.zen.events.WorldEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.Render3D
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.util.Vec3

@Zen.Module
object ScarfSpawnTimer : Feature("scarfspawntimers", area = "catacombs", subarea = listOf("F2", "M2")) {
    private var time = 0.0
    private var activeTimers = emptyList<TimerData>()

    private data class TimerData(val name: String, val offset: Double, val pos: Vec3)

    private val minions = listOf(
        TimerData("§cWarrior", 0.2, Vec3(14.5, 72.5, -3.5)),
        TimerData("§dPriest", 0.3, Vec3(-28.5, 72.5, -3.5)),
        TimerData("§bMage", 0.4, Vec3(14.5, 72.5, -22.5)),
        TimerData("§aArcher", 0.5, Vec3(-28.5, 72.5, -22.5))
    )

    private val boss = listOf(TimerData("§6Scarf", 0.4, Vec3(-7.5, 72.0, -10.5)))

    private val tickCall = EventBus.register<TickEvent.Server>({
        time -= 0.05
        if (time <= -5) cleanup()
    }, false)

    private val renderCall = EventBus.register<RenderEvent.World>({ event ->
        activeTimers.forEach { timer ->
            val displayTime = time + timer.offset
            if (displayTime > 0)
                Render3D.drawString("${timer.name} §e${"%.1f".format(displayTime)}s", timer.pos, event.partialTicks)
        }
    }, false)

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Dungeons", "Scarf", ConfigElement(
                "scarfspawntimers",
                "Spawn timers",
                "Spawn timers for Scarf and his minions.",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            when (event.event.message.unformattedText.removeFormatting()) {
                "[BOSS] Scarf: If you can beat my Undeads, I'll personally grant you the privilege to replace them." -> {
                    time = 7.75
                    activeTimers = minions
                    tickCall.register()
                    renderCall.register()
                }
                "[BOSS] Scarf: Those toys are not strong enough I see." -> {
                    time = 10.0
                    activeTimers = boss
                    tickCall.register()
                    renderCall.register()
                }
            }
        }

        register<WorldEvent.Change> { cleanup() }
    }

    private fun cleanup() {
        activeTimers = emptyList()
        tickCall.unregister()
        renderCall.unregister()
    }
}