package xyz.meowing.zen.features.slayers

import com.google.gson.JsonObject
import xyz.meowing.zen.Zen
import xyz.meowing.zen.Zen.Companion.prefix
import xyz.meowing.zen.api.SlayerTracker.bossType
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.*
import xyz.meowing.zen.utils.StringUtils.decodeRoman
import xyz.meowing.zen.utils.TimeUtils.millis
import net.minecraft.command.ICommandSender
import kotlin.time.Duration

@Zen.Module
object SlayerTimer : Feature("slayertimer", true) {
    val slayerRecord = DataUtils("slayerRecords", JsonObject())

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

    fun sendTimerMessage(action: String, timeToKill: Duration, ticks: Int) {
        val seconds = timeToKill.millis / 1000.0
        val serverTime = ticks / 20.0
        val content = "$prefix §f$action in §b${"%.2f".format(seconds)}s §7| §b${"%.2f".format(serverTime)}s"
        val hoverText = "§c${timeToKill.millis}ms §f| §c${"%.0f".format(ticks.toFloat())} ticks"

        ChatUtils.addMessage(content, hoverText)
        if (action == "You killed your boss") {
            val lastRecord = getSelectedSlayerRecord()

            if (timeToKill.millis < lastRecord && bossType.isNotEmpty()) {
                if (lastRecord == Long.MAX_VALUE) {
                    ChatUtils.addMessage("$prefix §d§lNew personal best! §r§7This is your first recorded time!", hoverText)
                } else {
                    ChatUtils.addMessage("$prefix §d§lNew personal best! §r§7${"%.2f".format(lastRecord / 1000.0)}s §r➜ §a${"%.2f".format(seconds)}s", hoverText)
                }

                slayerRecord.setData(slayerRecord.getData().apply {
                    addProperty("timeToKill${bossType.replace(" ", "_")}MS", timeToKill.millis)
                })
                slayerRecord.save()
            }
        }
    }

    fun getSelectedSlayerRecord(): Long {
        val data = slayerRecord.getData()
        return data.get("timeToKill${bossType.replace(" ", "_")}MS")?.asLong ?: Long.MAX_VALUE
    }

    fun sendBossSpawnMessage(timeSinceQuestStart: Duration) {
        val content = "$prefix §fBoss spawned after §b${"%.2f".format(timeSinceQuestStart.millis / 1000.0)}s"
        val hoverText = "§c${timeSinceQuestStart.millis}ms"
        ChatUtils.addMessage(content, hoverText)
    }
}

@Zen.Command
object SlayerPBCommand : CommandUtils("zenslayers", aliases = listOf("zenpb")) {
    override fun processCommand(sender: ICommandSender?, args: Array<out String?>?) {
        val data = SlayerTimer.slayerRecord.getData()
        if (data.entrySet().isEmpty()) {
            ChatUtils.addMessage("$prefix §fYou have no recorded slayer boss kills.")
            return
        }

        // Parse records into structured objects
        val records = data.entrySet().mapNotNull { (key, value) ->
            val raw = key.removePrefix("timeToKill").removeSuffix("MS")
            val parts = raw.split("_")

            if (parts.size < 2) return@mapNotNull null

            val slayerName = parts.dropLast(1).joinToString(" ")
            val tierRoman = parts.last()
            val tier = tierRoman.decodeRoman()
            val seconds = value.asLong / 1000.0

            Triple(slayerName, "$slayerName $tierRoman", seconds to tier)
        }

        // Group by slayer name and sort tiers
        val grouped = records.groupBy { it.first }
        ChatUtils.addMessage("$prefix §d§lYour Slayer Personal Bests:")

        for ((slayer, entries) in grouped) {
            ChatUtils.addMessage("")
            ChatUtils.addMessage("§8» §b§l$slayer Slayer")
            for ((_, displayName, timeTier) in entries.sortedBy { it.third.second }) {
                val (seconds, _) = timeTier
                ChatUtils.addMessage(
                    "   §7▪ §3$displayName §7➜ §b${"%.2f".format(seconds)}s"
                )
            }
        }
    }
}