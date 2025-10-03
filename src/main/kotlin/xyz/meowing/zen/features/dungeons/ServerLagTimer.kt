package xyz.meowing.zen.features.dungeons

import xyz.meowing.zen.Zen
import xyz.meowing.zen.Zen.Companion.prefix
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.ChatEvent
import xyz.meowing.zen.events.TickEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.ChatUtils
import xyz.meowing.zen.utils.TickUtils
import xyz.meowing.zen.utils.Utils.removeFormatting
import java.util.regex.Pattern

@Zen.Module
object ServerLagTimer : Feature("serverlagtimer", area = "catacombs") {
    private val regex = Pattern.compile("^\\s*☠ Defeated .+ in 0?[\\dhms ]+?\\s*(?:\\(NEW RECORD!\\))?$")
    private var sent = false
    private var ticking = false
    private var clienttick: Long = 0
    private var servertick: Long = 0

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Dungeons", "Server lag timer", ConfigElement(
                "serverlagtimer",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            val text = event.event.message.unformattedText.removeFormatting()
            when {
                text == "[NPC] Mort: Good luck." -> {
                    ticking = true
                    sent = false
                }
                regex.matcher(text).matches() && !sent -> {
                    val lagtick = clienttick - servertick
                    val lagtime = lagtick / 20.0
                    ticking = false
                    sent = true
                    TickUtils.schedule(2, {
                        ChatUtils.addMessage("$prefix §fServer lagged for §c${"%.1f".format(lagtime)}s §7| §c${lagtick} ticks§f.")
                    })
                }
            }
        }
        register<TickEvent.Server> { event ->
            if (ticking) servertick++
        }
        register<TickEvent.Client> { event ->
            if (ticking) clienttick++
        }
    }

    override fun onRegister() {
        sent = false
        clienttick = 0
        servertick = 0
        ticking = false
        super.onRegister()
    }

    override fun onUnregister() {
        sent = false
        clienttick = 0
        servertick = 0
        ticking = false
        super.onUnregister()
    }
}
