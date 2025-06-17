package meowing.zen.feats.carrying

import meowing.zen.Zen
import meowing.zen.events.EntityMetadataUpdateEvent
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.PersistentData
import meowing.zen.utils.TickScheduler
import meowing.zen.utils.Utils
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.Minecraft
import net.minecraft.event.ClickEvent
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.regex.Pattern

object carrycounter {
    private val mc = Minecraft.getMinecraft()
    private val tradeInit = Pattern.compile("^Trade completed with (?:\\[.*?] )?(\\w+)!$")
    private val tradeComp = Pattern.compile("^ \\+ (\\d+\\.?\\d*)M coins$")
    private var lasttradeuser: String? = null
    val carryees = mutableListOf<Carryee>()
    val persistentData = PersistentData("carrylogs", CarryLogs())

    @JvmStatic
    fun initialize() = Zen.registerListener("carrycounter", this)

    fun checkRegistration() {
        if (carryees.isNotEmpty()) MinecraftForge.EVENT_BUS.register(EntityEvents)
        else MinecraftForge.EVENT_BUS.unregister(EntityEvents)
    }

    object EntityEvents {
        @SubscribeEvent
        fun onEntityMetadataUpdate(event: EntityMetadataUpdateEvent) {
            event.packet.func_149376_c()?.find { it.dataValueId == 2 && it.`object` is String }?.let { obj ->
                val name = (obj.`object` as String).removeFormatting()
                if (name.contains("Spawned by")) {
                    carryees.find { it.name == name.substringAfter("by: ") }?.onSpawn(event.packet.entityId - 3)
                }
            }
        }

        @SubscribeEvent
        fun onEntityDeath(event: LivingDeathEvent) {
            carryees.find { it.bossID == event.entity.entityId }?.let {
                val time = System.currentTimeMillis() - (it.startTime ?: 0L)
                val ticks = TickScheduler.getCurrentServerTick() - (it.startTicks ?: 0L)
                ChatUtils.addMessage(
                    "§c[Zen] §fYou killed §b${it.name}§f's boss in §b${time}ms §7| §b${ticks / 20}s",
                    "&c${ticks} ticks"
                )
                it.onDeath()
            }
        }
    }

    object TradeEvents {
        @SubscribeEvent
        fun onChat(event: ClientChatReceivedEvent) {
            if (event.type.toInt() != 2 && tradeComp.matcher(event.message.unformattedText.removeFormatting()).matches()) {
                val count = (tradeComp.matcher(event.message.unformattedText.removeFormatting()).group(1).toDouble() / 1.3).toInt()
                ChatUtils.addMessage(
                    "§c[Zen] §fAdd §b$lasttradeuser §ffor §b$count §fcarries? ",
                    "§aAdd",
                    ClickEvent.Action.RUN_COMMAND,
                    "/zencarry add $lasttradeuser $count",
                    "§a[●]"
                )
            }
            lasttradeuser = null
        }
    }

    data class CarryLogs(val completedCarries: MutableList<CompletedCarry> = mutableListOf())

    data class CompletedCarry(
        val playerName: String,
        val totalCarries: Int,
        val completionTime: Long,
        val averageBossTime: Double,
        val totalSessionTime: Long,
        val lastKnownCount: Int = 0,
        var timestamp: Long
    )

    data class Carryee(
        val name: String,
        var total: Int,
        var count: Int = 0,
        var lastBossTime: Long? = null,
        var firstBossTime: Long? = null,
        var startTime: Long? = null,
        var startTicks: Long? = null,
        var isFighting: Boolean = false,
        var bossID: Int? = null,
        var sessionStartTime: Long = System.currentTimeMillis(),
        var totalCarryTime: Long = 0,
        val bossTimes: MutableList<Long> = mutableListOf()
    ) {
        fun onSpawn(id: Int) {
            if (startTime == null && !isFighting) {
                startTime = System.currentTimeMillis()
                startTicks = TickScheduler.getCurrentServerTick()
                isFighting = true
                bossID = id
                Utils.playSound("mob.cat.meow", 5f, 2f)
                Utils.showTitle("§bBoss spawned", "§bby §c$name", 20)
            }
        }

        fun onDeath() {
            if (firstBossTime == null) firstBossTime = System.currentTimeMillis()
            lastBossTime = System.currentTimeMillis()
            startTime?.let { bossTimes.add(System.currentTimeMillis() - it) }
            isFighting = false
            startTime = null
            startTicks = null
            bossID = null
            if (++count >= total) complete()
        }

        fun getTimeSinceLastBoss(): String = lastBossTime?.let {
            "§c${String.format("%.1fs", (System.currentTimeMillis() - it) / 1000.0)}"
        } ?: "§7N/A"

        fun getBossPerHour(): String {
            if (count <= 2) return "§7N/A"
            val totalTime = totalCarryTime + (firstBossTime?.let { System.currentTimeMillis() - it } ?: 0)
            return if (totalTime > 0) "§e${(count / (totalTime / 3.6e6)).toInt()}/hr" else "§7N/A"
        }

        fun complete() {
            val avgBossTime = if (bossTimes.isNotEmpty()) bossTimes.average() else 0.0
            val sessionTime = System.currentTimeMillis() - sessionStartTime
            val existingIndex = persistentData.getData().completedCarries.indexOfFirst { it.playerName.equals(name, ignoreCase = true) }

            if (existingIndex != -1) {
                val existing = persistentData.getData().completedCarries[existingIndex]
                persistentData.getData().completedCarries[existingIndex] = CompletedCarry(
                    name,
                    existing.totalCarries + count,
                    sessionTime,
                    avgBossTime,
                    existing.totalSessionTime + sessionTime,
                    existing.totalCarries + count,
                    System.currentTimeMillis()
                )
            } else {
                persistentData.getData().completedCarries.add(CompletedCarry(
                    name,
                    count,
                    sessionTime,
                    avgBossTime,
                    sessionTime,
                    count,
                    System.currentTimeMillis()
                    )
                )
            }

            persistentData.save()
            ChatUtils.addMessage("§c[Zen] §fCarries completed for §b$name §fin §b${sessionTime / 1000}s")
            Utils.playSound("mob.cat.meow", 5f, 2f)
            Utils.showTitle("§fCarries Completed: §b$name", "§b$count§f/§b$total", 150)
            carryees.removeIf { it.name.equals(name, ignoreCase = true) }
            checkRegistration()
        }
    }

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        if (event.type.toInt() != 2) {
            val text = tradeInit.matcher(event.message.unformattedText.removeFormatting())
            if (text.matches()) {
                lasttradeuser = text.group(1)
                MinecraftForge.EVENT_BUS.register(TradeEvents)
                TickScheduler.schedule(25) { MinecraftForge.EVENT_BUS.unregister(TradeEvents) }
            }
        }
    }
}