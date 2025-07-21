package meowing.zen.feats.carrying

import meowing.zen.Zen
import meowing.zen.Zen.Companion.prefix
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.*
import meowing.zen.feats.Feature
import meowing.zen.utils.*
import meowing.zen.utils.TitleUtils.showTitle
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.event.ClickEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent
import java.awt.Color
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern
import kotlin.math.abs
import kotlin.math.round

@Zen.Module
object CarryCounter : Feature("carrycounter") {
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
    val dataUtils = DataUtils("carrylogs", CarryLogs())

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Slayers", "Carrying", ConfigElement(
                "carrycounter",
                "Carry counter",
                "Counts the carries automatically",
                ElementType.Switch(false)
            ))
            .addElement("Slayers", "Carrying", ConfigElement(
                "carrycountsend",
                "Send count",
                "Sends the count in party chat",
                ElementType.Switch(true)
            ))
            .addElement("Slayers", "Carrying", ConfigElement(
                "carryvalue",
                "Carry value",
                "The values for the auto-add from trade in carry counter",
                ElementType.TextInput("1.3", "1.3")
            ))
            .addElement("Slayers", "Carrying", ConfigElement(
                "carrybosshighlight",
                "Carry boss highlight",
                "Highlights your client's slayer boss",
                ElementType.Switch(false)
            ))
            .addElement("Slayers", "Carrying", ConfigElement(
                "carrybosscolor"
                , "Carry boss highlight color",
                null,
                ElementType.ColorPicker(Color(0, 255, 255, 127))
            ))
            .addElement("Slayers", "Carrying", ConfigElement(
                "carrybosswidth",
                "Carry boss highlight width",
                "Width for the carry boss outline",
                ElementType.Slider(1.0, 10.0, 2.0, false)
            ))
            .addElement("Slayers", "Carrying", ConfigElement(
                "carryclienthighlight",
                "Carry client highlight",
                "Highlights your client",
                ElementType.Switch(false)
            ))
            .addElement("Slayers", "Carrying", ConfigElement(
                "carryclientcolor",
                "Carry client highlight color",
                null,
                ElementType.ColorPicker(Color(0, 255, 255, 127))
            ))
            .addElement("Slayers", "Carrying", ConfigElement(
                "carryclientwidth",
                "Carry client highlight width",
                "Width for the carry client outline",
                ElementType.Slider(1.0, 10.0, 2.0, false)
            ))
    }

    override fun initialize() {
        TickUtils.loop(400) {
            val world = mc.theWorld ?: return@loop
            val deadCarryees = carryeesByBossId.entries.mapNotNull { (bossId, carryee) ->
                val entity = world.getEntityByID(bossId)
                if (entity == null || entity.isDead) carryee else null
            }
            deadCarryees.forEach { it.reset() }
        }
        register<ChatEvent.Receive> { event ->
            handleChatMessage(event.event.message.unformattedText.removeFormatting())
        }
        CarryHUD.initialize()
        register<RenderEvent.HUD> { event ->
            if (event.elementType == RenderGameOverlayEvent.ElementType.TEXT) CarryHUD.render()
        }
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
            events.add(EventBus.register<EntityEvent.Metadata> ({ event ->
                event.packet.func_149376_c()?.find { it.dataValueId == 2 && it.`object` is String }?.let { obj ->
                    val name = (obj.`object` as String).removeFormatting()
                    if (name.contains("Spawned by")) {
                        val targetEntity = mc.theWorld.getEntityByID(event.packet.entityId)
                        val hasBlackhole = targetEntity?.let { entity ->
                            mc.theWorld.loadedEntityList.any { Entity ->
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
                    val ms = System.currentTimeMillis() - (it.startTime ?: 0L)
                    val ticks = TickUtils.getCurrentServerTick() - (it.startTicks ?: 0L)
                    ChatUtils.addMessage(
                        "$prefix §fYou killed §b${it.name}§f's boss in §b${"%.1f".format(ms / 1000.0)}s §7| §b${"%.1f".format(ticks / 20.0)}s",
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
            if (registered || !Zen.config.carrybosshighlight) return
            events.add(EventBus.register<RenderEvent.EntityModel> ({ event ->
                carryeesByBossId[event.entity.entityId]?.let {
                    OutlineUtils.outlineEntity(
                        event = event,
                        color = Zen.config.carrybosscolor,
                        lineWidth = Zen.config.carrybosswidth.toFloat(),
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
            if (registered || !Zen.config.carryclienthighlight) return
            events.add(EventBus.register<RenderEvent.EntityModel> ({ event ->
                if (event.entity !is EntityPlayer) return@register
                val cleanName = event.entity.name.removeFormatting()
                carryeesByName[cleanName]?.let {
                    OutlineUtils.outlineEntity(
                    event = event,
                    color = Zen.config.carryclientcolor,
                    lineWidth = Zen.config.carryclientwidth.toFloat(),
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
                        val carry = Zen.config.carryvalue.split(',')
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
                startTicks = TickUtils.getCurrentServerTick()
                isFighting = true
                bossID = id
                carryeesByBossId[id] = this
                Utils.playSound("mob.cat.meow", 5f, 2f)
                showTitle("§bBoss spawned", "§bby §c$name", 1000)
            }
        }

        fun onDeath() {
            if (firstBossTime == null) firstBossTime = System.currentTimeMillis()
            lastBossTime = System.currentTimeMillis()
            startTime?.let { bossTimes.add(System.currentTimeMillis() - it) }
            cleanup()
            if (++count >= total) complete()
            if (Zen.config.carrycountsend) ChatUtils.command("/pc $name: $count/$total")
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

            ensureDataLoaded()

            val updatedCarry = completedCarriesMap[name]?.let { existing ->
                CompletedCarry(
                    name,
                    existing.totalCarries + count,
                    existing.totalCarries + count,
                    System.currentTimeMillis()
                )
            } ?: CompletedCarry(name, count, count, System.currentTimeMillis())

            completedCarriesMap[name] = updatedCarry

            val carriesList = dataUtils.getData().completedCarries
            val existingIndex = carriesList.indexOfFirst { it.playerName == name }
            if (existingIndex != -1) carriesList[existingIndex] = updatedCarry
            else carriesList.add(updatedCarry)

            dataUtils.save()
            ChatUtils.addMessage("$prefix §fCarries completed for §b$name §fin §b${sessionTime / 1000}s")
            Utils.playSound("mob.cat.meow", 5f, 2f)
            showTitle("§fCarries Completed: §b$name", "§b$count§f/§b$total", 3000)

            carryeesByName.remove(name)
            bossID?.let { carryeesByBossId.remove(it) }
            checkRegistration()
        }
    }
}