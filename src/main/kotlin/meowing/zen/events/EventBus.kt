package meowing.zen.events

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
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.concurrent.ConcurrentHashMap

object EventBus {
    val listeners = ConcurrentHashMap<Class<*>, MutableSet<Any>>()

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
            else -> post(GuiEvent.Close())
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onGuiBackgroundDraw(event: GuiScreenEvent.BackgroundDrawnEvent) = post(GuiEvent.BackgroundDraw())

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
        if (post(RenderEvent.HUD(event.type, event.partialTicks, event.resolution))) event.isCanceled = true
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
        if (post(RenderEvent.LivingEntity.Pre(event.entity, event.x, event.y, event.z))) event.isCanceled = true
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onRenderLivingPost(event: RenderLivingEvent.Post<EntityLivingBase>) {
        post(RenderEvent.LivingEntity.Post(event.entity, event.x, event.y, event.z))
    }

    @SubscribeEvent
    fun onEndermanTP(event: EnderTeleportEvent) {
        if (post(RenderEvent.EndermanTP(event))) event.isCanceled = true
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onRenderPlayer(event: RenderPlayerEvent.Pre) {
        if (post(RenderEvent.Player.Pre(event.entityPlayer, event.partialRenderTick))) event.isCanceled = true
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onRenderPlayerPost(event: RenderPlayerEvent.Post) {
        post(RenderEvent.Player.Post(event.entityPlayer, event.partialRenderTick))
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onDrawBlockHighlight(event: DrawBlockHighlightEvent) {
        val blockpos = event.target.blockPos
        if (blockpos == null) return
        if (post(RenderEvent.BlockHighlight(blockpos, event.partialTicks))) event.isCanceled = true
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onDrawFog(event: EntityViewRenderEvent.FogDensity) {
        if (post(RenderEvent.Fog(event))) event.isCanceled = true
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onWorldLoad(event: WorldEvent.Load) {
        post(meowing.zen.events.WorldEvent.Load(event.world))
        post(meowing.zen.events.WorldEvent.Change(event.world))
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onWorldUnload(event: WorldEvent.Unload) {
        post(meowing.zen.events.WorldEvent.Unload(event.world))
        post(meowing.zen.events.WorldEvent.Change(event.world))
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

    fun onPacketReceived(packet: Packet<*>) {
        post(PacketEvent.Received(packet))
        when (packet) {
            is S32PacketConfirmTransaction -> {
                if (packet.func_148888_e() || packet.actionNumber > 0) return
                post(meowing.zen.events.TickEvent.Server())
            }
            is S1CPacketEntityMetadata -> {
                post(EntityEvent.Metadata(packet))
            }
            is S0FPacketSpawnMob -> {
                post(EntityEvent.Spawn(packet))
            }
            is S02PacketChat -> {
                post(ChatEvent.Packet(packet))
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

    fun onPacketSent(packet: Packet<*>): Boolean {
        when (packet) {
            is C01PacketChatMessage -> {
                return post(ChatEvent.Send(packet.message))
            }
        }
        return post(PacketEvent.Sent(packet))
    }

    /*
     * Modified from Devonian code
     * Under GPL 3.0 License
     */
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