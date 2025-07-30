package meowing.zen.feats.dungeons

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import meowing.zen.events.RenderEvent
import meowing.zen.events.WorldEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.Render3D
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.util.Vec3

@Zen.Module
object ScarfSpawnTimer : Feature("scarfspawntimers", area = "catacombs", subarea = listOf("F2", "M2")) {
    private var time = 0.0
    private var activeTimers = emptyList<TimerData>()
    private var timerId: Long? = null

    private data class TimerData(val name: String, val offset: Double, val pos: Vec3)

    private val minions = listOf(
        TimerData("§cWarrior", 0.2, Vec3(14.5, 72.5, -3.5)),
        TimerData("§dPriest", 0.3, Vec3(-28.5, 72.5, -3.5)),
        TimerData("§bMage", 0.4, Vec3(14.5, 72.5, -22.5)),
        TimerData("§aArcher", 0.5, Vec3(-28.5, 72.5, -22.5))
    )

    private val boss = listOf(TimerData("§6Scarf", 0.4, Vec3(-7.5, 72.0, -10.5)))

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Dungeons", "Scarf Spawn Timers", ConfigElement(
                "scarfspawntimers",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }

    override fun initialize() {
        createCustomEvent<RenderEvent.World>("render") { event ->
            activeTimers.forEach { timer ->
                val displayTime = time + timer.offset
                if (displayTime > 0)
                    Render3D.drawString("${timer.name} §e${"%.1f".format(displayTime)}s", timer.pos, event.partialTicks)
            }
        }

        register<ChatEvent.Receive> { event ->
            when (event.event.message.unformattedText.removeFormatting()) {
                "[BOSS] Scarf: If you can beat my Undeads, I'll personally grant you the privilege to replace them." -> {
                    time = 7.75
                    activeTimers = minions
                    startTimer()
                }
                "[BOSS] Scarf: Those toys are not strong enough I see." -> {
                    time = 10.0
                    activeTimers = boss
                    startTimer()
                }
            }
        }

        register<WorldEvent.Change> { cleanup() }
    }

    private fun startTimer() {
        registerEvent("render")
        timerId = createTimer(((time + 5) * 20).toInt(),
            onTick = {
                time -= 0.05
            },
            onComplete = {
                cleanup()
            }
        )
    }

    private fun cleanup() {
        activeTimers = emptyList()
        unregisterEvent("render")
    }
}