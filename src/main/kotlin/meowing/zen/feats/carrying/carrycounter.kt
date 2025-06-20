package meowing.zen.feats.carrying

import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.events.EntityMetadataUpdateEvent
import meowing.zen.events.RenderEntityModelEvent
import meowing.zen.mixins.AccessorMinecraft
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.OutlineUtils
import meowing.zen.utils.PersistentData
import meowing.zen.utils.RenderUtils
import meowing.zen.utils.TickScheduler
import meowing.zen.utils.Utils
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.event.ClickEvent
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern
import kotlin.math.abs
import kotlin.math.round

object carrycounter {
    private val tradeInit = Pattern.compile("^Trade completed with (?:\\[.*?] )?(\\w+)!$")
    private val tradeComp = Pattern.compile("^ \\+ (\\d+\\.?\\d*)M coins$")
    private val playerDead = Pattern.compile("^ ☠ (\\w+) was killed by (.+)\\.$")
    private val bossNames = setOf("Voidgloom Seraph", "Revenant Horror", "Tarantula Broodfather", "Sven Packmaster", "Inferno Demonlord")

    private var lasttradeuser: String? = null
    private var entityEventsReg = false
    private var renderBossEntityReg = false
    private var renderPlayerEntityReg = false
    private var tradeEventsReg = false

    private val carryeesByName = ConcurrentHashMap<String, Carryee>()
    private val carryeesByBossId = ConcurrentHashMap<Int, Carryee>()
    private val completedCarriesMap = ConcurrentHashMap<String, CompletedCarry>()
    private val bossPerHourCache = ConcurrentHashMap<String, Pair<String, Long>>()

    val carryees get() = carryeesByName.values.toList()
    val persistentData = PersistentData("carrylogs", CarryLogs())

    @JvmStatic
    fun initialize() {
        Zen.registerListener("carrycounter", this)
        loadCompletedCarries()
        TickScheduler.loop(400, {
            val world = mc.theWorld ?: return@loop
            val deadCarryees = carryeesByBossId.entries.mapNotNull { (bossId, carryee) ->
                val entity = world.getEntityByID(bossId)
                if (entity == null || entity.isDead) carryee else null
            }
            deadCarryees.forEach {
                it.reset()
            }
        })
    }

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        if (event.type.toInt() == 2) return
        tradeInit.matcher(event.message.unformattedText.removeFormatting()).let { matcher ->
            if (!matcher.matches()) return
            lasttradeuser = matcher.group(1)
            if (!tradeEventsReg) {
                MinecraftForge.EVENT_BUS.register(ChatEvents)
                tradeEventsReg = true
                TickScheduler.schedule(25, {
                    if (tradeEventsReg) {
                        MinecraftForge.EVENT_BUS.unregister(ChatEvents)
                        tradeEventsReg = false
                    }
                })
            }
        }
    }

    private fun loadCompletedCarries() {
        persistentData.getData().completedCarries.forEach { carry ->
            completedCarriesMap[carry.playerName] = carry
        }
    }

    fun checkRegistration() {
        try {
            if (carryeesByName.isNotEmpty()) {
                if (!entityEventsReg) {
                    MinecraftForge.EVENT_BUS.register(EntityEvents)
                    entityEventsReg = true
                    ChatUtils.addMessage("registered")
                }
                if (!renderBossEntityReg && Zen.config.carrybosshighlight) {
                    MinecraftForge.EVENT_BUS.register(RenderBossEntity)
                    renderBossEntityReg = true
                }
                if (!renderPlayerEntityReg && Zen.config.carryclienthighlight) {
                    MinecraftForge.EVENT_BUS.register(RenderPlayerEntity)
                    renderPlayerEntityReg = true
                }
            } else {
                if (entityEventsReg) {
                    MinecraftForge.EVENT_BUS.unregister(EntityEvents)
                    entityEventsReg = false
                }
                if (renderBossEntityReg) {
                    MinecraftForge.EVENT_BUS.unregister(RenderBossEntity)
                    renderBossEntityReg = false
                }
                if (renderPlayerEntityReg) {
                    MinecraftForge.EVENT_BUS.unregister(RenderPlayerEntity)
                    renderPlayerEntityReg = false
                }
            }
        } finally {
            CarryInventoryHud.checkRegistration()
        }
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
        @SubscribeEvent
        fun onEntityMetadataUpdate(event: EntityMetadataUpdateEvent) {
            event.packet.func_149376_c()?.find { it.dataValueId == 2 && it.`object` is String }?.let { obj ->
                val name = (obj.`object` as String).removeFormatting()
                if (name.contains("Spawned by")) {
                    val spawnerName = name.substringAfter("by: ")
                    carryeesByName[spawnerName]?.onSpawn(event.packet.entityId - 3)
                }
            }
        }

        @SubscribeEvent
        fun onEntityDeath(event: LivingDeathEvent) {
            carryeesByBossId[event.entity.entityId]?.let {
                val ms = System.currentTimeMillis() - (it.startTime ?: 0L)
                val ticks = TickScheduler.getCurrentServerTick() - (it.startTicks ?: 0L)
                ChatUtils.addMessage(
                    "§c[Zen] §fYou killed §b${it.name}§f's boss in §b${"%.1f".format(ms / 1000.0)}s §7| §b${"%.1f".format(ticks / 20.0)}s",
                    "§c${ticks} ticks"
                )
                it.onDeath()
            }
        }
    }

    object RenderBossEntity {
        @SubscribeEvent
        fun onBossRender(event: RenderEntityModelEvent) {
            carryeesByBossId[event.entity.entityId]?.let {
                OutlineUtils.outlineEntity(
                    event = event,
                    color = Zen.config.carrybosscolor,
                    lineWidth = Zen.config.carrybosswidth,
                    depth = true,
                    shouldCancelHurt = true
                )
            }
        }
    }

    object RenderPlayerEntity {
        @SubscribeEvent
        fun onEntityRender(event: RenderLivingEvent.Post<EntityPlayerMP>) {
            if (event.entity !is EntityPlayerMP) return
            val partialTicks = (mc as AccessorMinecraft).timer.renderPartialTicks
            val cleanName = event.entity.name.removeFormatting()
            carryeesByName[cleanName]?.let {
                RenderUtils.drawOutlineBox(
                    entity = event.entity,
                    color = Zen.config.carryclientcolor,
                    partialTicks = partialTicks,
                    lineWidth = Zen.config.carryclientwidth
                )
            }
        }
    }

    object ChatEvents {
        @SubscribeEvent
        fun onChat(event: ClientChatReceivedEvent) {
            if (event.type.toInt() == 2) return
            val text = event.message.unformattedText.removeFormatting()

            tradeComp.matcher(text).let { matcher ->
                if (matcher.matches()) {
                    val coins = matcher.group(1).toDoubleOrNull() ?: return
                    val carry = Zen.config.carryvalue.split(',')
                        .mapNotNull { it.trim().toDoubleOrNull() }
                        .find { abs(coins / it - round(coins / it)) < 1e-6 } ?: return
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
                    return
                }
            }

            playerDead.matcher(text).let { matcher ->
                if (matcher.matches() && matcher.group(2) in bossNames) carryeesByName[matcher.group(1)]?.reset()
            }

            lasttradeuser = null
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
            bossID?.let {
                carryeesByBossId.remove(it)
            }
            checkRegistration()
        }
    }
}