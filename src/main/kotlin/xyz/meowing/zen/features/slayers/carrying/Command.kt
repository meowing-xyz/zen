package xyz.meowing.zen.features.slayers.carrying

import xyz.meowing.knit.api.command.Commodore
import xyz.meowing.zen.Zen
import xyz.meowing.zen.Zen.Companion.prefix
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.utils.ChatUtils
import net.minecraft.client.Minecraft
import net.minecraft.event.ClickEvent
import net.minecraft.util.ChatComponentText
import java.text.SimpleDateFormat
import java.util.Date

@Zen.Command
object CarryCommand : Commodore("carry", "zencarry") {
    private val carrycounter by ConfigDelegate<Boolean>("carrycounter")
    private var currentLogPage = 1

    init {
        literal("add") {
            runs { player: String, count: Int ->
                if (!checkEnabled()) return@runs
                addCarryee(player, count)
            }
        }

        literal("remove", "rem") {
            runs { player: String ->
                if (!checkEnabled()) return@runs
                removeCarryee(player)
            }
        }

        literal("settotal") {
            runs { player: String, total: Int ->
                if (!checkEnabled()) return@runs
                setTotal(player, total)
            }
        }

        literal("setcount") {
            runs { player: String, count: Int ->
                if (!checkEnabled()) return@runs
                setCount(player, count)
            }
        }

        literal("list", "ls") {
            runs {
                if (!checkEnabled()) return@runs
                listCarryees()
            }
        }

        literal("clear") {
            runs {
                if (!checkEnabled()) return@runs
                clearCarryees()
            }
        }

        literal("log", "logs") {
            runs { page: Int? ->
                if (!checkEnabled()) return@runs
                showLogs(page ?: currentLogPage)
            }
        }

        literal("devtest") {
            runs {
                if (!checkEnabled()) return@runs
                CarryCounter.carryees.forEach { it.onDeath() }
            }
        }

        runs {
            if (!checkEnabled()) return@runs
            showHelp()
        }
    }

    private fun checkEnabled(): Boolean {
        if (!carrycounter) {
            ChatUtils.addMessage(
                "$prefix §fPlease enable carry counter first!",
                "§cClick to open settings GUI",
                ClickEvent.Action.RUN_COMMAND,
                "/zen"
            )
            return false
        }
        return true
    }

    private fun addCarryee(playerName: String, count: Int) {
        if (playerName.isBlank()) return ChatUtils.addMessage("$prefix §fPlayer name cannot be empty!")
        if (count <= 0) return ChatUtils.addMessage("$prefix §fInvalid count! Must be positive.")

        val carryee = CarryCounter.addCarryee(playerName, count)
        if (carryee != null) {
            if (carryee.total == count) ChatUtils.addMessage("$prefix §fAdded §b$playerName§f for §b$count§f carries.")
            else ChatUtils.addMessage("$prefix §fUpdated §b$playerName§f to §b${carryee.total}§f total (§b${carryee.count}§f/§b${carryee.total}§f)")
        }
    }

    private fun removeCarryee(playerName: String) {
        if (playerName.isBlank()) return ChatUtils.addMessage("$prefix §fPlayer name cannot be empty!")
        val removed = CarryCounter.removeCarryee(playerName)
        ChatUtils.addMessage("$prefix §f${if (removed) "Removed" else "Player not found:"} §b$playerName")
    }

    private fun setTotal(playerName: String, total: Int) {
        if (playerName.isBlank()) return ChatUtils.addMessage("$prefix §fPlayer name cannot be empty!")
        if (total <= 0) return ChatUtils.addMessage("$prefix §fTotal must be positive.")

        val carryee = CarryCounter.findCarryee(playerName)
        if (carryee != null) {
            carryee.total = total
            ChatUtils.addMessage("$prefix §fSet §b$playerName§f total to §b$total§f (§b${carryee.count}§f/§b$total§f)")
        } else ChatUtils.addMessage("$prefix §fPlayer §b$playerName§f not found!")
    }

    private fun setCount(playerName: String, count: Int) {
        if (playerName.isBlank()) return ChatUtils.addMessage("$prefix §fPlayer name cannot be empty!")
        if (count < 0) return ChatUtils.addMessage("$prefix §fCount cannot be negative.")

        val carryee = CarryCounter.findCarryee(playerName)
        if (carryee != null) {
            carryee.count = count
            ChatUtils.addMessage("$prefix §fSet §b$playerName§f count to §b$count§f (§b$count§f/§b${carryee.total}§f)")
            if (count >= carryee.total) carryee.complete()
        } else ChatUtils.addMessage("$prefix §fPlayer §b$playerName§f not found!")
    }

    private fun clearCarryees() {
        val count = CarryCounter.carryees.size
        CarryCounter.clearCarryees()
        ChatUtils.addMessage("$prefix §fCleared §b$count§f carries.")
    }

    private fun listCarryees() {
        if (CarryCounter.carryees.isEmpty()) return ChatUtils.addMessage("$prefix §fNo active carries.")

        ChatUtils.addMessage("$prefix §fActive Carries:")
        CarryCounter.carryees.forEach { carryee ->
            val progress = "§b${carryee.count}§f/§b${carryee.total}"
            val lastBoss = if (carryee.count > 0) "§7(${carryee.getTimeSinceLastBoss()} ago)" else ""
            ChatUtils.addMessage("§7> §b${carryee.name}§f - $progress $lastBoss")
        }
    }

    private fun showLogs(page: Int = currentLogPage) {
        val logs = CarryCounter.dataUtils.getData().completedCarries.sortedByDescending { it.timestamp }
        if (logs.isEmpty()) return ChatUtils.addMessage("$prefix §fNo carry logs found.")

        val totalCarries = logs.sumOf { it.totalCarries }
        val totalPages = (logs.size + 9) / 10
        currentLogPage = page.coerceIn(1, totalPages)

        val startIndex = (currentLogPage - 1) * 10
        val endIndex = (startIndex + 10).coerceAtMost(logs.size)

        ChatUtils.addMessage("§7⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤")

        val prevPage = if (currentLogPage > 1) "§b[<]" else "§7[<]"
        val nextPage = if (currentLogPage < totalPages) "§b[>]" else "§7[>]"

        val player = Minecraft.getMinecraft().thePlayer ?: return
        player.addChatMessage(
            ChatComponentText("$prefix §fCarry Logs - §fPage §b$currentLogPage§f/§b$totalPages ")
                .appendSibling(
                    ChatComponentText(prevPage).apply {
                        chatStyle = ChatUtils.createChatStyle(
                            prevPage,
                            ClickEvent.Action.RUN_COMMAND,
                            if (currentLogPage > 1) "/carry log ${currentLogPage - 1}" else ""
                        )
                    }
                )
                .appendSibling(ChatComponentText(" §7| "))
                .appendSibling(
                    ChatComponentText(nextPage).apply {
                        chatStyle = ChatUtils.createChatStyle(
                            nextPage,
                            ClickEvent.Action.RUN_COMMAND,
                            if (currentLogPage < totalPages) "/carry log ${currentLogPage + 1}" else ""
                        )
                    }
                )
        )

        logs.subList(startIndex, endIndex).forEach { log ->
            val date = SimpleDateFormat("d/M/yyyy").format(Date(log.timestamp))
            val time = SimpleDateFormat("HH:mm").format(Date(log.timestamp))
            ChatUtils.addMessage("§7> §b${log.playerName} §7- §c$date §fat §c$time §7- §b${log.totalCarries} §fcarries")
        }

        ChatUtils.addMessage("§c§l| §fTotal carries: §b$totalCarries")
        ChatUtils.addMessage("§7⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤")
    }

    private fun showHelp() {
        ChatUtils.addMessage("$prefix §fCarry Commands:")
        listOf(
            "add §c<player> <count>§7 - §fAdd carries",
            "settotal §c<player> <total>§7 - §fSet total carries",
            "setcount §c<player> <count>§7 - §fSet current count",
            "remove §c<player>§7 - §fRemove player",
            "log §c[page]§7 - §fShow carry history",
            "list§7 - §fShow active carries",
            "clear§7 - §fClear all carries"
        ).forEach { ChatUtils.addMessage("§7> §7/§bcarry $it") }
    }
}