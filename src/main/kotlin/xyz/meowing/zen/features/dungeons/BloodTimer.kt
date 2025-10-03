package xyz.meowing.zen.features.dungeons

import xyz.meowing.zen.Zen
import xyz.meowing.zen.Zen.Companion.prefix
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.ChatEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.ChatUtils
import xyz.meowing.zen.utils.TimeUtils
import xyz.meowing.zen.utils.TitleUtils.showTitle
import xyz.meowing.zen.utils.Utils.removeFormatting
import java.util.regex.Pattern

@Zen.Module
object BloodTimer : Feature("bloodtimer", area = "catacombs") {
    private val bloodstart = Pattern.compile("\\[BOSS] The Watcher: .+")
    private val dialogue = Pattern.compile("\\[BOSS] The Watcher: Let's see how you can handle this\\.")
    private val bloodcamp = Pattern.compile("\\[BOSS] The Watcher: You have proven yourself\\. You may pass\\.")
    private var bloodopen = false
    private var starttime = TimeUtils.zero

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Dungeons", "Blood helper", ConfigElement(
                "bloodtimer",
                "Blood camp helper",
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            val text = event.event.message.unformattedText.removeFormatting()
            when {
                !bloodopen && bloodstart.matcher(text).matches() -> {
                    bloodopen = true
                    starttime = TimeUtils.now
                }
                dialogue.matcher(text).matches() -> {
                    val diftime = starttime.since.inWholeSeconds.toDouble()
                    showTitle("§c§l!", "§cWatcher reached dialogue!", 3000)
                    ChatUtils.addMessage("$prefix §fWatcher took §c${"%.2f".format(diftime)}s §fto reach dialogue!")
                }
                bloodcamp.matcher(text).matches() -> {
                    val camptime = starttime.since.inWholeSeconds.toDouble()
                    ChatUtils.addMessage("$prefix §fBlood camp took §c${"%.2f".format(camptime)}s")
                    bloodopen = false
                }
            }
        }
    }

    override fun onRegister() {
        bloodopen = false
        starttime = TimeUtils.zero
        super.onRegister()
    }

    override fun onUnregister() {
        bloodopen = false
        starttime = TimeUtils.zero
        super.onUnregister()
    }
}