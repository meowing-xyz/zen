package meowing.zen.feats.carrying

import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.events.*
import meowing.zen.feats.Feature
import meowing.zen.mixins.AccessorMinecraft
import meowing.zen.utils.*
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.event.ClickEvent
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern
import kotlin.math.abs
import kotlin.math.round

object carrycounter : Feature("carrycounter") {
    private val tradeInit = Pattern.compile("^Trade completed with (?:\\[.*?] )?(\\w+)!$")
    private val tradeComp = Pattern.compile("^ \\+ (\\d+\\.?\\d*)M coins$")
    private val playerDead = Pattern.compile("^ ☠ (\\w+) was killed by (.+)\\.$")
    private val bossNames = setOf("Voidgloom Seraph", "Revenant Horror", "Tarantula Broodfather", "Sven Packmaster", "Inferno Demonlord")

    private var lasttradeuser: String? = null
    private val carryeesByName = ConcurrentHashMap<String, Carryee>()
    private val carryeesByBossId = ConcurrentHashMap<Int, Carryee>()
    private val completedCarriesMap = ConcurrentHashMap<String, CompletedCarry>()
    private val bossPerHourCache = ConcurrentHashMap<String, Pair<String, Long>>()

    val carryees get() = carryeesByName.values.toList()
    val persistentData = PersistentData("carrylogs", CarryLogs())

    override fun initialize() {
        TickScheduler.loop(400) {
            val world = mc.theWorld ?: return@loop
            val deadCarryees = carryeesByBossId.entries.mapNotNull { (bossId, carryee) ->
                val entity = world.getEntityByID(bossId)
                if (entity == null || entity.isDead) carryee else null
            }
            deadCarryees.forEach { it.reset() }
        }
        register<ChatMessageEvent> { handleChatMessage(it.message) }
    }

    private fun handleChatMessage(message: String) {
        val text = message.removeFormatting()

        tradeInit.matcher(text).let { matcher ->
            if (matcher.matches()) {
                lasttradeuser = matcher.group(1)
                ChatEvents.register()
                return
            }
        }
    }

    private fun checkRegistration() {
        if (carryeesByName.isNotEmpty()) {
            EntityEvents.register()
            RenderBossEntity.register()
            RenderPlayerEntity.register()
        } else {
            EntityEvents.unregister()
            RenderBossEntity.unregister()
            RenderPlayerEntity.unregister()
        }
        CarryInventoryHud.checkRegistration()
    }

    fun addCarryee(name: String, total: Int): Carryee? {
        if (name.isBlank() || total <= 0) return null
        val existing = carryeesByName[name]
        if (existing != null) {
            existing.total += total
            return existing
        }

        val carryee = Carryee(name, total)
        carryeesByName[name] = carryee
        checkRegistration()
        return carryee
    }

    fun removeCarryee(name: String): Boolean {
        if (name.isBlank()) return false
        val carryee = carryeesByName.remove(name) ?: return false
        carryee.bossID?.let { carryeesByBossId.remove(it) }
        checkRegistration()
        return true
    }

    fun findCarryee(name: String): Carryee? = if (name.isBlank()) null else carryeesByName[name]

    fun clearCarryees() {
        carryeesByName.clear()
        carryeesByBossId.clear()
        checkRegistration()
    }

    object EntityEvents {
        private var registered = false
        private val events = mutableListOf<EventBus.EventCall>()

        fun register() {
            if (registered) return
            events.add(EventBus.register<EntityMetadataEvent> { event ->
                event.packet.func_149376_c()?.find { it.dataValueId == 2 && it.`object` is String }?.let { obj ->
                    val name = (obj.`object` as String).removeFormatting()
                    if (name.contains("Spawned by")) {
                        val spawnerName = name.substringAfter("by: ")
                        carryeesByName[spawnerName]?.onSpawn(event.packet.entityId - 3)
                    }
                }
            })

            events.add(EventBus.register<EntityLeaveEvent> { event ->
                carryeesByBossId[event.entity.entityId]?.let {
                    val ms = System.currentTimeMillis() - (it.startTime ?: 0L)
                    val ticks = TickScheduler.getCurrentServerTick() - (it.startTicks ?: 0L)
                    ChatUtils.addMessage(
                        "§c[Zen] §fYou killed §b${it.name}§f's boss in §b${"%.1f".format(ms / 1000.0)}s §7| §b${"%.1f".format(ticks / 20.0)}s",
                        "§c${ticks} ticks"
                    )
                    it.onDeath()
                }
            })
            registered = true
        }

        fun unregister() {
            if (!registered) return
            events.forEach { it.unregister() }
            events.clear()
            registered = false
        }
    }

    object RenderBossEntity {
        private var registered = false
        private val events = mutableListOf<EventBus.EventCall>()

        fun register() {
            if (registered || !Zen.config.carrybosshighlight) return
            events.add(EventBus.register<RenderEntityModelEvent> { event ->
                carryeesByBossId[event.entity.entityId]?.let {
                    OutlineUtils.outlineEntity(
                        event = event,
                        color = Zen.config.carrybosscolor,
                        lineWidth = Zen.config.carrybosswidth,
                        depth = true,
                        shouldCancelHurt = true
                    )
                }
            })
            registered = true
        }

        fun unregister() {
            if (!registered) return
            events.forEach { it.unregister() }
            events.clear()
            registered = false
        }
    }

    object RenderPlayerEntity {
        private var registered = false
        private val events = mutableListOf<EventBus.EventCall>()

        fun register() {
            if (registered || !Zen.config.carryclienthighlight) return
            events.add(EventBus.register<RenderPlayerPostEvent> { event ->
                val partialTicks = (mc as AccessorMinecraft).timer.renderPartialTicks
                val cleanName = event.player.name.removeFormatting()
                carryeesByName[cleanName]?.let {
                    RenderUtils.drawOutlineBox(
                        entity = event.player,
                        color = Zen.config.carryclientcolor,
                        partialTicks = partialTicks,
                        lineWidth = Zen.config.carryclientwidth
                    )
                }
            })
            registered = true
        }

        fun unregister() {
            if (!registered) return
            events.forEach { it.unregister() }
            events.clear()
            registered = false
        }
    }

    object ChatEvents {
        private var registered = false
        private val events = mutableListOf<EventBus.EventCall>()

        fun register() {
            if (registered) return
            events.add(EventBus.register<ChatMessageEvent> { event ->
                val text = event.message.removeFormatting()

                tradeComp.matcher(text).let { matcher ->
                    if (matcher.matches()) {
                        val coins = matcher.group(1).toDoubleOrNull() ?: return@let
                        val carry = Zen.config.carryvalue.split(',')
                            .mapNotNull { it.trim().toDoubleOrNull() }
                            .find { abs(coins / it - round(coins / it)) < 1e-6 } ?: return@let
                        val count = round(coins / carry).toInt()
                        lasttradeuser?.let { user ->
                            ChatUtils.addMessage(
                                "§c[Zen] §fAdd §b$user §ffor §b$count §fcarries? ",
                                "§aAdd",
                                ClickEvent.Action.RUN_COMMAND,
                                "/zencarry add $user $count",
                                "§a[●]"
                            )
                        }
                        return@register
                    }
                }

                playerDead.matcher(text).let { matcher ->
                    if (matcher.matches() && matcher.group(2) in bossNames) {
                        carryeesByName[matcher.group(1)]?.reset()
                    }
                }

                lasttradeuser = null
                TickScheduler.schedule(25) { unregister() }
            })
            registered = true
        }

        fun unregister() {
            if (!registered) return
            events.forEach { it.unregister() }
            events.clear()
            registered = false
        }
    }

    data class CarryLogs(val completedCarries: MutableList<CompletedCarry> = mutableListOf())

    data class CompletedCarry(
        val playerName: String,
        val totalCarries: Int,
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
                carryeesByBossId[id] = this
                Utils.playSound("mob.cat.meow", 5f, 2f)
                Utils.showTitle("§bBoss spawned", "§bby §c$name", 20)
            }
        }

        fun onDeath() {
            if (firstBossTime == null) firstBossTime = System.currentTimeMillis()
            lastBossTime = System.currentTimeMillis()
            startTime?.let { bossTimes.add(System.currentTimeMillis() - it) }
            cleanup()
            if (++count >= total) complete()
        }

        fun reset() {
            if (firstBossTime == null) firstBossTime = System.currentTimeMillis()
            lastBossTime = System.currentTimeMillis()
            startTime?.let { bossTimes.add(System.currentTimeMillis() - it) }
            cleanup()
        }

        private fun cleanup() {
            isFighting = false
            bossID?.let { carryeesByBossId.remove(it) }
            startTime = null
            startTicks = null
            bossID = null
        }

        fun getTimeSinceLastBoss(): String = lastBossTime?.let {
            String.format("%.1fs", (System.currentTimeMillis() - it) / 1000.0)
        } ?: "§7N/A"

        fun getBossPerHour(): String {
            if (count <= 2) return "N/A"
            val cacheKey = "$name-$count"
            val cached = bossPerHourCache[cacheKey]
            val now = System.currentTimeMillis()

            if (cached != null && now - cached.second < 5000) return cached.first

            val totalTime = totalCarryTime + (firstBossTime?.let { now - it } ?: 0)
            val result = if (totalTime > 0) "${(count / (totalTime / 3.6e6)).toInt()}/hr" else "§7N/A"
            bossPerHourCache[cacheKey] = result to now
            return result
        }

        fun complete() {
            val sessionTime = System.currentTimeMillis() - sessionStartTime

            val updatedCarry = completedCarriesMap[name]?.let { existing ->
                CompletedCarry(
                    name,
                    existing.totalCarries + count,
                    existing.totalCarries + count,
                    System.currentTimeMillis()
                )
            } ?: CompletedCarry(name, count, count, System.currentTimeMillis())

            completedCarriesMap[name] = updatedCarry

            val carriesList = persistentData.getData().completedCarries
            val existingIndex = carriesList.indexOfFirst { it.playerName == name }
            if (existingIndex != -1) carriesList[existingIndex] = updatedCarry
            else carriesList.add(updatedCarry)

            persistentData.save()
            ChatUtils.addMessage("§c[Zen] §fCarries completed for §b$name §fin §b${sessionTime / 1000}s")
            Utils.playSound("mob.cat.meow", 5f, 2f)
            Utils.showTitle("§fCarries Completed: §b$name", "§b$count§f/§b$total", 150)

            carryeesByName.remove(name)
            bossID?.let { carryeesByBossId.remove(it) }
            checkRegistration()
        }
    }
}