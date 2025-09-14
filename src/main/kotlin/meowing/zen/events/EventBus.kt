package meowing.zen.events

import meowing.zen.Zen.Companion.configUI
import meowing.zen.utils.LocationUtils
import meowing.zen.utils.ScoreboardUtils
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.Packet
import net.minecraft.network.play.client.C01PacketChatMessage
import net.minecraft.network.play.server.*
import net.minecraftforge.client.event.*
import net.minecraftforge.client.event.MouseEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.living.EnderTeleportEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import java.util.concurrent.ConcurrentHashMap

object EventBus {
    val listeners = ConcurrentHashMap<Class<*>, MutableSet<PrioritizedCallback<*>>>()
    data class PrioritizedCallback<T>(val priority: Int, val callback: (T) -> Unit)

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onEntityJoin(event: EntityJoinWorldEvent) = post(EntityEvent.Join(event.entity))

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onEntityDeath(event: LivingDeathEvent) = post(EntityEvent.Leave(event.entity))

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onAttackEntity(event: net.minecraftforge.event.entity.player.AttackEntityEvent) =
        post(EntityEvent.Attack(event.entityPlayer, event.target))

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase == TickEvent.Phase.START) post(meowing.zen.events.TickEvent.Client())
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onRenderWorld(event: RenderWorldLastEvent) = post(RenderEvent.World(event.partialTicks))

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onGuiOpen(event: GuiOpenEvent) {
        when {
            event.gui != null -> post(GuiEvent.Open(event.gui))
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onGuiBackgroundDraw(event: GuiScreenEvent.BackgroundDrawnEvent) = post(GuiEvent.BackgroundDraw(event.gui))

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onGuiMouseClick(event: GuiScreenEvent.MouseInputEvent.Pre) {
        if (post(GuiEvent.Click(event.gui))) event.isCanceled = true
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onGuiKeyPress(event: GuiScreenEvent.KeyboardInputEvent.Pre) {
        if (post(GuiEvent.Key(event.gui))) event.isCanceled = true
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onRenderGameOverlay(event: RenderGameOverlayEvent.Pre) {
        event.isCanceled = when {
            post(RenderEvent.HUD(event.type, event.partialTicks, event.resolution)) -> true
            event.type == RenderGameOverlayEvent.ElementType.TEXT && post(RenderEvent.Text(event.partialTicks, event.resolution)) -> true
            else -> false
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onChatReceived(event: ClientChatReceivedEvent) {
        val customEvent = when (event.type.toInt()) {
            2 -> GameEvent.ActionBar(event)
            else -> ChatEvent.Receive(event)
        }
        if (post(customEvent)) event.isCanceled = true
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onRenderLiving(event: RenderLivingEvent.Pre<EntityLivingBase>) {
        if (post(RenderEvent.Entity.Pre(event.entity, event.x, event.y, event.z))) event.isCanceled = true
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onRenderLivingPost(event: RenderLivingEvent.Post<EntityLivingBase>) {
        post(RenderEvent.Entity.Post(event.entity, event.x, event.y, event.z))
    }

    @SubscribeEvent
    fun onEndermanTP(event: EnderTeleportEvent) {
        if (post(RenderEvent.EndermanTP(event))) event.isCanceled = true
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onRenderPlayer(event: RenderPlayerEvent.Pre) {
        if (post(RenderEvent.Player.Pre(event.entityPlayer, event.x, event.y, event.z, event.partialRenderTick))) event.isCanceled = true
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onRenderPlayerPost(event: RenderPlayerEvent.Post) {
        post(RenderEvent.Player.Post(event.entityPlayer, event.x, event.y, event.z, event.partialRenderTick))
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onDrawBlockHighlight(event: DrawBlockHighlightEvent) {
        val blockpos = event.target.blockPos ?: return
        if (post(RenderEvent.BlockHighlight(blockpos, event.partialTicks))) event.isCanceled = true
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onWorldLoad(event: WorldEvent.Load) {
        if(meowing.zen.events.WorldEvent.Change.shouldPost()) {
            post(meowing.zen.events.WorldEvent.Change(event.world))
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        post(EntityEvent.Interact(event.action, event.pos))
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onMouse(event: MouseEvent) {
        when {
            event.button != -1 && event.buttonstate -> post(meowing.zen.events.MouseEvent.Click(event))
            event.button != -1 && !event.buttonstate -> post(meowing.zen.events.MouseEvent.Release(event))
            event.dwheel != 0 -> post(meowing.zen.events.MouseEvent.Scroll(event))
            event.dx != 0 || event.dy != 0 -> post(meowing.zen.events.MouseEvent.Move(event))
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onToolTip(event: ItemTooltipEvent) {
        val tooltipEvent = ItemTooltipEvent(event.toolTip, event.itemStack)

        if (post(tooltipEvent)) {
            event.isCanceled = true
        } else if (tooltipEvent.lines != event.toolTip) {
            event.toolTip.clear()
            event.toolTip.addAll(tooltipEvent.lines)
        }
    }

    @SubscribeEvent
    fun onDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        post(GameEvent.Disconnect())
    }

    fun onPacketReceived(packet: Packet<*>): Boolean {
        if (post(PacketEvent.Received(packet))) return true

        return when (packet) {
            is S32PacketConfirmTransaction -> {
                if (packet.func_148888_e() || packet.actionNumber > 0) false
                else post(meowing.zen.events.TickEvent.Server())
            }
            is S02PacketChat -> {
                post(ChatEvent.Packet(packet))
            }
            is S3CPacketUpdateScore, is S3DPacketDisplayScoreboard, is S3EPacketTeams -> {
                val lines = ScoreboardUtils.getSidebarLines(true)
                post(SidebarUpdateEvent(lines))
            }
            is S38PacketPlayerListItem -> {
                if (packet.action == S38PacketPlayerListItem.Action.UPDATE_DISPLAY_NAME || packet.action == S38PacketPlayerListItem.Action.ADD_PLAYER) post(TablistEvent(packet))
                else false
            }
            else -> false
        }
    }

    fun onPacketSent(packet: Packet<*>): Boolean {
        return when (packet) {
            is C01PacketChatMessage -> {
                post(ChatEvent.Send(packet.message))
            }
            else -> post(PacketEvent.Sent(packet))
        }
    }

    /*
     * Modified from Devonian code
     * Under GPL 3.0 License
     */
    inline fun <reified T : Event> register(priority: Int = 0, noinline callback: (T) -> Unit, add: Boolean = true): EventCall {
        val eventClass = T::class.java
        val handlers = listeners.getOrPut(eventClass) { ConcurrentHashMap.newKeySet() }
        val prioritizedCallback = PrioritizedCallback(priority, callback)
        if (add) handlers.add(prioritizedCallback)
        return EventCallImpl(prioritizedCallback, handlers)
    }

    inline fun <reified T : Event> register(noinline callback: (T) -> Unit, add: Boolean = true): EventCall {
        return register(0, callback, add)
    }

    inline fun <reified T : Event> register(noinline callback: (T) -> Unit): EventCall {
        return register(0, callback, true)
    }

    @JvmStatic
    fun <T : Event> registerJava(eventClass: Class<T>, priority: Int, add: Boolean = false, callback: (T) -> Unit): EventCall {
        val handlers = listeners.getOrPut(eventClass) { ConcurrentHashMap.newKeySet() }
        val prioritizedCallback = PrioritizedCallback(priority, callback)
        if (add) handlers.add(prioritizedCallback)
        return EventCallImpl(prioritizedCallback, handlers)
    }

    fun <T : Event> post(event: T): Boolean {
        val eventClass = event::class.java
        val handlers = listeners[eventClass] ?: return false
        if (handlers.isEmpty()) return false

        val sortedHandlers = handlers.sortedBy { it.priority }

        for (handler in sortedHandlers) {
            try {
                @Suppress("UNCHECKED_CAST")
                (handler.callback as (T) -> Unit)(event)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return if (event is CancellableEvent) event.isCancelled() else false
    }

    class EventCallImpl(
        private val callback: PrioritizedCallback<*>,
        private val handlers: MutableSet<PrioritizedCallback<*>>
    ) : EventCall {
        override fun unregister(): Boolean = handlers.remove(callback)
        override fun register(): Boolean = handlers.add(callback)
    }

    interface EventCall {
        fun unregister(): Boolean
        fun register(): Boolean
    }
}

inline fun <reified T : Event> configRegister(
    configKeys: Any,
    priority: Int = 0,
    skyblockOnly: Boolean = false,
    area: Any? = null,
    subarea: Any? = null,
    noinline enabledCheck: (Map<String, Any?>) -> Boolean,
    noinline callback: (T) -> Unit
): EventBus.EventCall {
    val eventCall = EventBus.register<T>(priority, callback, false)
    val keys = when (configKeys) {
        is String -> listOf(configKeys)
        is List<*> -> configKeys.filterIsInstance<String>()
        else -> throw IllegalArgumentException("configKeys must be String or List<String>")
    }

    val areas = when (area) {
        is String -> listOf(area.lowercase())
        is List<*> -> area.filterIsInstance<String>().map { it.lowercase() }
        else -> emptyList()
    }
    val subareas = when (subarea) {
        is String -> listOf(subarea.lowercase())
        is List<*> -> subarea.filterIsInstance<String>().map { it.lowercase() }
        else -> emptyList()
    }

    val checkAndUpdate = {
        val configValues = keys.associateWith { configUI.getConfigValue(it) }
        val configEnabled = enabledCheck(configValues)
        val skyblockEnabled = !skyblockOnly || LocationUtils.inSkyblock
        val areaEnabled = areas.isEmpty() || areas.any { LocationUtils.checkArea(it) }
        val subareaEnabled = subareas.isEmpty() || subareas.any { LocationUtils.checkSubarea(it) }

        if (configEnabled && skyblockEnabled && areaEnabled && subareaEnabled) {
            eventCall.register()
        } else {
            eventCall.unregister()
        }
    }

    keys.forEach { configKey ->
        configUI.registerListener(configKey) { checkAndUpdate() }
    }

    if (areas.isNotEmpty()) {
        EventBus.register<AreaEvent.Main> { checkAndUpdate() }
    }

    if (subareas.isNotEmpty()) {
        EventBus.register<AreaEvent.Sub> { checkAndUpdate() }
    }

    if (skyblockOnly) {
        EventBus.register<AreaEvent.Skyblock> { checkAndUpdate() }
    }

    checkAndUpdate()
    return eventCall
}

@Suppress("UNUSED")
inline fun <reified T : Event> configRegister(
    configKeys: Any,
    priority: Int = 0,
    skyblockOnly: Boolean = false,
    area: Any? = null,
    subarea: Any? = null,
    noinline callback: (T) -> Unit
): EventBus.EventCall {
    return configRegister(configKeys, priority, skyblockOnly, area, subarea, { configValues ->
        configValues.values.all { it as? Boolean == true }
    }, callback)
}

@Suppress("UNUSED")
inline fun <reified T : Event> configRegister(
    configKeys: Any,
    enabledIndices: Set<Int>,
    priority: Int = 0,
    skyblockOnly: Boolean = false,
    area: Any? = null,
    subarea: Any? = null,
    noinline callback: (T) -> Unit
): EventBus.EventCall {
    return configRegister(configKeys, priority, skyblockOnly, area, subarea, { configValues ->
        configValues.values.all { value ->
            when (value) {
                is Int -> value in enabledIndices
                is Boolean -> value
                else -> false
            }
        }
    }, callback)
}

@Suppress("UNUSED")
inline fun <reified T : Event> configRegister(
    configKeys: Any,
    requiredIndex: Int,
    priority: Int = 0,
    skyblockOnly: Boolean = false,
    area: Any? = null,
    subarea: Any? = null,
    noinline callback: (T) -> Unit
): EventBus.EventCall {
    return configRegister(configKeys, priority, skyblockOnly, area, subarea, { configValues ->
        configValues.values.all { value ->
            when (value) {
                is Set<*> -> value.contains(requiredIndex)
                is Boolean -> value
                else -> false
            }
        }
    }, callback)
}