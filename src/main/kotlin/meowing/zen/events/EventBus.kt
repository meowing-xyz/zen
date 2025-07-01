package meowing.zen.events

import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.Packet
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S1CPacketEntityMetadata
import net.minecraft.network.play.server.S32PacketConfirmTransaction
import net.minecraft.network.play.server.S38PacketPlayerListItem
import net.minecraft.network.play.server.S3CPacketUpdateScore
import net.minecraft.network.play.server.S3DPacketDisplayScoreboard
import net.minecraft.network.play.server.S3EPacketTeams
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.DrawBlockHighlightEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.client.event.RenderPlayerEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.living.EnderTeleportEvent
import java.util.concurrent.ConcurrentHashMap

object EventBus {
    val listeners = ConcurrentHashMap<Class<*>, MutableSet<Any>>()

    init {
        MinecraftForge.EVENT_BUS.register(this)
        initPacketDispatcher()
    }

    private fun initPacketDispatcher() {
        register<PacketEvent.Received> ({ event ->
            packetReceived(event)
        })
        register<PacketEvent.Sent> ({ event ->
            packetSent(event)
        })
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onEntityJoin(event: EntityJoinWorldEvent) = post(EntityJoinEvent(event.entity))

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onEntityDeath(event: LivingDeathEvent) = post(EntityLeaveEvent(event.entity))

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onAttackEntity(event: net.minecraftforge.event.entity.player.AttackEntityEvent) =
        post(AttackEntityEvent(event.entityPlayer, event.target))

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase == TickEvent.Phase.START) post(TickEvent())
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onServerTick(event: TickEvent.ServerTickEvent) {
        if (event.phase == TickEvent.Phase.START) post(TickEvent())
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onRenderWorld(event: RenderWorldLastEvent) = post(RenderWorldEvent(event.partialTicks))

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onGuiOpen(event: net.minecraftforge.client.event.GuiOpenEvent) {
        when {
            event.gui != null -> post(GuiOpenEvent(event.gui))
            else -> post(GuiCloseEvent())
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onGuiBackgroundDraw(event: GuiScreenEvent.BackgroundDrawnEvent) = post(GuiBackgroundDrawEvent())

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onGuiMouseClick(event: GuiScreenEvent.MouseInputEvent.Pre) {
        if (post(GuiClickEvent(event.gui))) event.isCanceled = true
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onGuiKeyPress(event: GuiScreenEvent.KeyboardInputEvent.Pre) {
        if (post(GuiKeyEvent(event.gui))) event.isCanceled = true
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onRenderGameOverlay(event: RenderGameOverlayEvent.Pre) {
        if (post(RenderEvent(event.type, event.partialTicks, event.resolution))) event.isCanceled = true
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onChatReceived(event: ClientChatReceivedEvent) {
        if (post(ChatReceiveEvent(event))) event.isCanceled = true
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onRenderLiving(event: RenderLivingEvent.Pre<EntityLivingBase>) {
        if (post(RenderLivingEntityEvent(event.entity, event.x, event.y, event.z))) event.isCanceled = true
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onRenderLivingPost(event: RenderLivingEvent.Post<EntityLivingBase>) {
        post(RenderLivingEntityPostEvent(event.entity, event.x, event.y, event.z))
    }

    @SubscribeEvent
    fun onEndermanTP(event: EnderTeleportEvent) {
        if (post(EndermanTPEvent(event))) event.isCanceled = true
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onRenderPlayer(event: RenderPlayerEvent.Pre) {
        post(RenderPlayerEvent(event.entityPlayer, event.partialRenderTick))
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onRenderPlayerPost(event: RenderPlayerEvent.Post) {
        post(RenderPlayerPostEvent(event.entityPlayer, event.partialRenderTick))
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onDrawBlockHighlight(event: DrawBlockHighlightEvent) {
        val blockpos = event.target.blockPos
        if (blockpos == null) return
        if (post(BlockHighlightEvent(blockpos, event.partialTicks))) event.isCanceled = true
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onWorldLoad(event: WorldEvent.Load) {
        post(WorldLoadEvent(event.world))
        post(WorldChangeEvent(event.world))
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onWorldUnload(event: WorldEvent.Unload) {
        post(WorldUnloadEvent(event.world))
        post(WorldChangeEvent(event.world))
    }

    fun onPacketReceived(packet: Packet<*>) {
        post(PacketEvent.Received(packet))
    }

    fun onPacketSent(packet: Packet<*>) {
        post(PacketEvent.Sent(packet))
    }

    private fun packetReceived(event: PacketEvent.Received) {
        when (val packet = event.packet) {
            is S32PacketConfirmTransaction -> {
                if (packet.func_148888_e() || packet.actionNumber > 0) return
                post(ServerTickEvent())
            }
            is S1CPacketEntityMetadata -> {
                post(EntityMetadataEvent(packet))
            }
            is S02PacketChat -> {
                post(ChatPacketEvent(packet))
            }
            is S3EPacketTeams, is S3CPacketUpdateScore, is S3DPacketDisplayScoreboard -> {
                post(ScoreboardEvent(packet))
            }
            is S38PacketPlayerListItem -> {
                if (packet.action == S38PacketPlayerListItem.Action.UPDATE_DISPLAY_NAME || packet.action == S38PacketPlayerListItem.Action.ADD_PLAYER)
                    post(TablistEvent(packet))
            }
        }
    }

    private fun packetSent(event: PacketEvent.Sent) {
        post(PacketEvent.Sent(event.packet))
    }

    inline fun <reified T : Event> register(noinline callback: (T) -> Unit, add: Boolean = true): EventCall {
        val eventClass = T::class.java
        val handlers = listeners.getOrPut(eventClass) { ConcurrentHashMap.newKeySet() }
        if (add) handlers.add(callback)
        return EventCallImpl(callback, handlers)
    }

    fun <T : Event> post(event: T): Boolean {
        val eventClass = event::class.java
        val handlers = listeners[eventClass] ?: return false
        if (handlers.isEmpty()) return false

        for (handler in handlers) {
            try {
                @Suppress("UNCHECKED_CAST")
                (handler as (T) -> Unit)(event)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return if (event is CancellableEvent) event.isCancelled() else false
    }

    class EventCallImpl(
        private val callback: Any,
        private val handlers: MutableSet<Any>
    ) : EventCall {
        override fun unregister(): Boolean = handlers.remove(callback)
        override fun register(): Boolean = handlers.add(callback)
    }

    interface EventCall {
        fun unregister(): Boolean
        fun register(): Boolean
    }
}