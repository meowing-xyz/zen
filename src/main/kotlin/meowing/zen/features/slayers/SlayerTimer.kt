package meowing.zen.features.slayers

import com.google.gson.JsonObject
import meowing.zen.Zen
import meowing.zen.Zen.Companion.prefix
import meowing.zen.api.SlayerTracker.bossType
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.features.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.DataUtils
import meowing.zen.utils.SimpleTimeMark
import meowing.zen.utils.TimeUtils.millis

@Zen.Module
object SlayerTimer : Feature("slayertimer", true) {
    private val slayerRecord = DataUtils("slayerRecords", JsonObject())

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Slayers", "Slayer timer", ConfigElement(
                "slayertimer",
                "Slayer timer",
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("Slayers", "Slayer timer", "", ConfigElement(
                "",
                null,
                ElementType.TextParagraph("Logs your time to kill slayer bosses to chat.")
            ))
    }

    fun sendTimerMessage(action: String, timeTaken: Long, ticks: Int) {
        val seconds = timeTaken / 1000.0
        val serverTime = ticks / 20.0
        val content = "$prefix §f$action in §b${"%.2f".format(seconds)}s §7| §b${"%.2f".format(serverTime)}s"
        val hoverText = "§c${timeTaken}ms §f| §c${"%.0f".format(ticks.toFloat())} ticks"

        ChatUtils.addMessage(content, hoverText)
        ChatUtils.addMessage("bosstype: $bossType")
        if (action == "You killed your boss") {
            val lastRecord = getSelectedSlayerRecord()

            if (timeTaken < lastRecord && bossType.isNotEmpty()) {
                if (lastRecord == Long.MAX_VALUE) {
                    ChatUtils.addMessage("$prefix §d§lNew personal best! §r§7This is your first recorded kill time!", hoverText)
                } else {
                    ChatUtils.addMessage("$prefix §d§lNew personal best! §r§7${"%.2f".format(lastRecord / 1000.0)}s §r➜ §a${"%.2f".format(seconds)}s", hoverText)
                }

                slayerRecord.setData(slayerRecord.getData().apply {
                    addProperty("timeToKill${bossType.replace(" ", "_")}MS", timeTaken)
                })
                slayerRecord.save()
            }
        }
    }

    fun getSelectedSlayerRecord(): Long {
        val data = slayerRecord.getData()
        return data.get("timeToKill${bossType.replace(" ", "_")}MS")?.asLong ?: Long.MAX_VALUE
    }

    fun sendBossSpawnMessage(spawnTime: SimpleTimeMark) {
        val timeSinceQuestStart = spawnTime.since.millis
        val content = "$prefix §fBoss spawned after §b${"%.2f".format(timeSinceQuestStart / 1000.0)}s"
        val hoverText = "§c${timeSinceQuestStart}ms"
        ChatUtils.addMessage(content, hoverText)
    }
}