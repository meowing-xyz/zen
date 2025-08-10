package meowing.zen.features.carrying

import meowing.zen.Zen
import meowing.zen.Zen.Companion.prefix
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import meowing.zen.events.EntityEvent
import meowing.zen.events.EventBus
import meowing.zen.events.RenderEvent
import meowing.zen.features.Feature
import meowing.zen.utils.*
import meowing.zen.utils.TimeUtils.millis
import meowing.zen.utils.TitleUtils.showTitle
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.event.ClickEvent
import java.awt.Color
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern
import kotlin.math.abs
import kotlin.math.round
import kotlin.time.Duration.Companion.seconds

@Zen.Module
object CarryCounter : Feature("carrycounter") {
    private val tradeInit = Pattern.compile("^Trade completed with (?:\\[.*?] )?(\\w+)!$")
    private val tradeComp = Pattern.compile("^ \\+ (\\d+\\.?\\d*)M coins$")
    private val playerDead = Pattern.compile("^ ☠ (\\w+) was killed by (.+)\\.$")
    private val bossNames = setOf("Voidgloom Seraph", "Revenant Horror", "Tarantula Broodfather", "Sven Packmaster", "Inferno Demonlord")
    private val carryeesByBossId = ConcurrentHashMap<Int, Carryee>()
    private val completedCarriesMap = ConcurrentHashMap<String, CompletedCarry>()
    private val bossPerHourCache = ConcurrentHashMap<String, Pair<String, SimpleTimeMark>>()
    private var lasttradeuser: String? = null
    inline val carryees get() = carryeesByName.values.toList()
    val dataUtils = DataUtils("carrylogs", CarryLogs())
    val carryeesByName = ConcurrentHashMap<String, Carryee>()

    private val carrycountsend by ConfigDelegate<Boolean>("carrycountsend")
    private val carrysendmsg by ConfigDelegate<Boolean>("carrysendmsg")
    private val carryvalue by ConfigDelegate<String>("carryvalue")
    private val carrybosshighlight by ConfigDelegate<Boolean>("carrybosshighlight")
    private val carrybosscolor by ConfigDelegate<Color>("carrybosscolor")
    private val carrybosswidth by ConfigDelegate<Double>("carrybosswidth")
    private val carryclienthighlight by ConfigDelegate<Boolean>("carryclienthighlight")
    private val carryclientcolor by ConfigDelegate<Color>("carryclientcolor")
    private val carryclientwidth by ConfigDelegate<Double>("carryclientwidth")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Slayers", "Carrying", ConfigElement(
                "carrycounter",
                "Carry counter",
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("Slayers", "Carrying", "QOL", ConfigElement(
                "carrycountsend",
                "Send count",
                ElementType.Switch(true)
            ))
            .addElement("Slayers", "Carrying", "QOL", ConfigElement(
                "carrysendmsg",
                "Send boss spawn message",
                ElementType.Switch(true)
            ))
            .addElement("Slayers", "Carrying", "QOL", ConfigElement(
                "carryvalue",
                "Carry value",
                ElementType.TextInput("1.3", "1.3")
            ))
            .addElement("Slayers", "Carrying", "Carry Boss", ConfigElement(
                "carrybosshighlight",
                "Carry boss highlight",
                ElementType.Switch(false)
            ))
            .addElement("Slayers", "Carrying", "Carry Boss", ConfigElement(
                "carrybosscolor"
                , "Carry boss highlight color",
                ElementType.ColorPicker(Color(0, 255, 255, 127))
            ))
            .addElement("Slayers", "Carrying", "Carry Boss", ConfigElement(
                "carrybosswidth",
                "Carry boss highlight width",
                ElementType.Slider(1.0, 10.0, 2.0, false)
            ))
            .addElement("Slayers", "Carrying", "Carry Client", ConfigElement(
                "carryclienthighlight",
                "Carry client highlight",
                ElementType.Switch(false)
            ))
            .addElement("Slayers", "Carrying", "Carry Client", ConfigElement(
                "carryclientcolor",
                "Carry client highlight color",
                ElementType.ColorPicker(Color(0, 255, 255, 127))
            ))
            .addElement("Slayers", "Carrying", "Carry Client", ConfigElement(
                "carryclientwidth",
                "Carry client highlight width",
                ElementType.Slider(1.0, 10.0, 2.0, false)
            ))
    }

    override fun initialize() {
        TickUtils.loop(400) {
            val world = world ?: return@loop
            val deadCarryees = carryeesByBossId.entries.mapNotNull { (bossId, carryee) ->
                val entity = world.getEntityByID(bossId)
                if (entity == null || entity.isDead) carryee else null
            }
            deadCarryees.forEach { it.reset() }
        }

        register<ChatEvent.Receive> { event ->
            val text = event.event.message.unformattedText.removeFormatting()

            tradeInit.matcher(text).let { matcher ->
                if (matcher.matches()) {
                    lasttradeuser = matcher.group(1)
                    ChatEvents.register()
                    LoopUtils.setTimeout(2000, { ChatEvents.unregister() })
                }
            }
        }

        CarryHUD.initialize()

        register<RenderEvent.HUD> { CarryHUD.render() }
    }

    private fun loadCompletedCarries() {
        try {
            val carriesList = dataUtils.getData().completedCarries
            completedCarriesMap.clear()
            carriesList.forEach { carry ->
                completedCarriesMap[carry.playerName] = carry
            }
            println("[Zen] Data loaded.")
        } catch (e: Exception) {
            println("[Zen] Data error: $e")
        }
    }

    private fun ensureDataLoaded() {
        if (completedCarriesMap.isEmpty()) loadCompletedCarries()
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
            events.add(EventBus.register<EntityEvent.Metadata> ({ event ->
                event.packet.func_149376_c()?.find { it.dataValueId == 2 && it.`object` is String }?.let { obj ->
                    val name = (obj.`object` as String).removeFormatting()
                    if (name.contains("Spawned by")) {
                        val targetEntity = world?.getEntityByID(event.packet.entityId)
                        val hasBlackhole = targetEntity?.let { entity ->
                            world?.loadedEntityList?.any { Entity ->
                                entity.getDistanceToEntity(Entity) <= 3f && Entity.name?.removeFormatting()?.lowercase()?.contains("black hole") == true
                            }
                        } ?: false

                        if (hasBlackhole) return@register
                        val spawnerName = name.substringAfter("by: ")
                        carryeesByName[spawnerName]?.onSpawn(event.packet.entityId - 3)
                    }
                }
            }))

            events.add(EventBus.register<EntityEvent.Leave> ({ event ->
                carryeesByBossId[event.entity.entityId]?.let {
                    val seconds = (it.startTime.since.millis / 1000.0)
                    val ticks = TickUtils.getCurrentServerTick() - (it.startTicks ?: 0L)
                    ChatUtils.addMessage(
                        "$prefix §fYou killed §b${it.name}§f's boss in §b${"%.1f".format(seconds)}s §7| §b${"%.1f".format(ticks / 20.0)}s",
                        "§c${ticks} ticks"
                    )
                    it.onDeath()
                }
            }))
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
            if (registered || !carrybosshighlight) return
            events.add(EventBus.register<RenderEvent.EntityModel> ({ event ->
                carryeesByBossId[event.entity.entityId]?.let {
                    OutlineUtils.outlineEntity(
                        event = event,
                        color = carrybosscolor,
                        lineWidth = carrybosswidth.toFloat(),
                        shouldCancelHurt = true
                    )
                }
            }))
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
            if (registered || !carryclienthighlight) return
            events.add(EventBus.register<RenderEvent.EntityModel> ({ event ->
                if (event.entity !is EntityPlayer) return@register
                val cleanName = event.entity.name.removeFormatting()
                carryeesByName[cleanName]?.let {
                    OutlineUtils.outlineEntity(
                    event = event,
                    color = carryclientcolor,
                    lineWidth = carryclientwidth.toFloat(),
                    shouldCancelHurt = true
                    )
                }
            }))
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
            events.add(EventBus.register<ChatEvent.Receive> ({ event ->
                val text = event.event.message.unformattedText.removeFormatting()

                tradeComp.matcher(text).let { matcher ->
                    if (matcher.matches()) {
                        val coins = matcher.group(1).toDoubleOrNull() ?: return@let
                        val carry = carryvalue.split(',')
                            .mapNotNull { it.trim().toDoubleOrNull() }
                            .find { abs(coins / it - round(coins / it)) < 1e-6 } ?: return@let
                        val count = round(coins / carry).toInt()

                        lasttradeuser?.let { user ->
                            ChatUtils.addMessage(
                                "$prefix §fAdd §b$user §ffor §b$count §fcarries? ",
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
                TickUtils.schedule(25) { unregister() }
            }))
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
        var lastBossTime: SimpleTimeMark = TimeUtils.zero,
        var firstBossTime: SimpleTimeMark = TimeUtils.zero,
        var startTime: SimpleTimeMark = TimeUtils.zero,
        var startTicks: Long? = null,
        var isFighting: Boolean = false,
        var bossID: Int? = null,
        var sessionStartTime: SimpleTimeMark = TimeUtils.now,
        var totalCarryTime: Long = 0,
        val bossTimes: MutableList<Long> = mutableListOf()
    ) {
        fun onSpawn(id: Int) {
            if (startTime.isZero && !isFighting) {
                startTime = TimeUtils.now
                startTicks = TickUtils.getCurrentServerTick()
                isFighting = true
                bossID = id
                carryeesByBossId[id] = this
                Utils.playSound("mob.cat.meow", 5f, 2f)
                showTitle("§bBoss spawned", "§bby §c$name", 1000)
                if (carrysendmsg) ChatUtils.addMessage("$prefix §fBoss spawned by §c$name")
            }
        }

        fun onDeath() {
            if (firstBossTime.isZero) firstBossTime = TimeUtils.now
            lastBossTime = TimeUtils.now
            bossTimes.add(startTime.since.millis)
            cleanup()
            if (++count >= total) complete()
            if (carrycountsend) ChatUtils.command("/pc $name: $count/$total")
        }

        fun reset() {
            if (firstBossTime.isZero) firstBossTime = TimeUtils.now
            lastBossTime = TimeUtils.now
            bossTimes.add(startTime.since.millis)
            cleanup()
        }

        private fun cleanup() {
            isFighting = false
            bossID?.let { carryeesByBossId.remove(it) }
            startTime = TimeUtils.zero
            startTicks = null
            bossID = null
        }

        fun getTimeSinceLastBoss(): String =
            if (lastBossTime.isZero) "§7N/A"
            else String.format("%.1fs", lastBossTime.since.millis / 1000.0)

        fun getBossPerHour(): String {
            if (count <= 2) return "N/A"
            val cacheKey = "$name-$count"
            val cached = bossPerHourCache[cacheKey]
            val now = TimeUtils.now

            if (cached != null && now - cached.second < 5.seconds) return cached.first

            val totalTime = totalCarryTime + firstBossTime.since.inWholeMilliseconds
            val result = if (totalTime > 0) "${(count / (totalTime / 3.6e6)).toInt()}/hr" else "§7N/A"
            bossPerHourCache[cacheKey] = result to now
            return result
        }

        fun complete() {
            val sessionTime = sessionStartTime.since.millis / 1000

            ensureDataLoaded()

            val updatedCarry = completedCarriesMap[name]?.let { existing ->
                CompletedCarry(
                    name,
                    existing.totalCarries + count,
                    existing.totalCarries + count,
                    TimeUtils.now.toMillis
                )
            } ?: CompletedCarry(name, count, count, TimeUtils.now.toMillis)

            completedCarriesMap[name] = updatedCarry

            val carriesList = dataUtils.getData().completedCarries
            val existingIndex = carriesList.indexOfFirst { it.playerName == name }
            if (existingIndex != -1) carriesList[existingIndex] = updatedCarry
            else carriesList.add(updatedCarry)

            dataUtils.save()
            ChatUtils.addMessage("$prefix §fCarries completed for §b$name §fin §b${sessionTime}s")
            Utils.playSound("mob.cat.meow", 5f, 2f)
            showTitle("§fCarries Completed: §b$name", "§b$count§f/§b$total", 3000)

            carryeesByName.remove(name)
            bossID?.let { carryeesByBossId.remove(it) }
            checkRegistration()
        }
    }
}