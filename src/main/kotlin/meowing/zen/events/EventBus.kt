package meowing.zen.events

import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.Packet
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.client.event.RenderPlayerEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.common.MinecraftForge

object EventBus {
    val events = hashMapOf<String, MutableList<Any>>()

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    // Entity action events
    @SubscribeEvent
    fun onEntityJoin(event: EntityJoinWorldEvent) {
        post(EntityJoinEvent(event.entity))
    }

    @SubscribeEvent
    fun onEntityDeath(event: LivingDeathEvent) {
        post(EntityLeaveEvent(event.entityLiving))
    }

    @SubscribeEvent
    fun onAttackEntity(event: net.minecraftforge.event.entity.player.AttackEntityEvent) {
        val attackEvent = AttackEntityEvent(event.entityPlayer, event.target)
        post(attackEvent)
    }

    // Tick events
    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase == TickEvent.Phase.START) post(TickEvent())
    }

    @SubscribeEvent
    fun onServerTick(event: TickEvent.ServerTickEvent) {
        post(TickEvent())
    }

    // Render world
    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        post(RenderWorldEvent(event.partialTicks))
    }

    // Gui events
    @SubscribeEvent
    fun onGuiOpen(event: net.minecraftforge.client.event.GuiOpenEvent) {
        if (event.gui != null) post(GuiOpenEvent(event.gui))
    }

    @SubscribeEvent
    fun onGuiClose(event: net.minecraftforge.client.event.GuiOpenEvent) {
        if (event.gui == null) post(GuiCloseEvent())
    }

    @SubscribeEvent
    fun onGuiBackgroundDraw(event: GuiScreenEvent.BackgroundDrawnEvent) {
        post(GuiBackgroundDrawEvent())
    }

    @SubscribeEvent
    fun onGuiMouseClick(event: GuiScreenEvent.MouseInputEvent.Pre) {
        val mouseEvent = GuiClickEvent(event.gui)
        post(mouseEvent)
        if (mouseEvent.isCancelled()) event.isCanceled = true
    }

    @SubscribeEvent
    fun onGuiKeyPress(event: GuiScreenEvent.KeyboardInputEvent.Pre) {
        val keyEvent = GuiKeyEvent(event.gui)
        post(keyEvent)
        if (keyEvent.isCancelled()) event.isCanceled = true
    }

    // Chat events
    @SubscribeEvent
    fun onChatReceived(event: ClientChatReceivedEvent) {
        post(ChatReceiveEvent(event))
        post(ChatMessageEvent(event.message.unformattedText))
    }

    // Render entities
    @SubscribeEvent
    fun onRenderLiving(event: RenderLivingEvent.Pre<EntityLivingBase>) {
        val renderEvent = RenderLivingEntityEvent(event.entity, event.x, event.y, event.z)
        post(renderEvent)
        if (renderEvent.isCancelled()) event.isCanceled = true
    }

    @SubscribeEvent
    fun onRenderLivingPost(event: RenderLivingEvent.Post<EntityLivingBase>) {
        val renderEvent = RenderLivingEntityPostEvent(event.entity, event.x, event.y, event.z)
        post(renderEvent)
    }

    @SubscribeEvent
    fun onRenderPlayer(event: RenderPlayerEvent.Pre) {
        val renderEvent = RenderPlayerEvent(event.entityPlayer, event.partialRenderTick)
        post(renderEvent)
        if (renderEvent.isCancelled()) event.isCanceled = true
    }

    @SubscribeEvent
    fun onRenderPlayerPost(event: RenderPlayerEvent.Post) {
        val renderEvent = RenderPlayerPostEvent(event.entityPlayer, event.partialRenderTick)
        post(renderEvent)
        if (renderEvent.isCancelled()) event.isCanceled = true
    }

    // World events
    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        post(WorldLoadEvent(event.world))
    }
    @SubscribeEvent
    fun onWorldUnload(event: WorldEvent.Unload) {
        post(WorldUnloadEvent(event.world))
    }

    // Packet shit
    fun onPacketReceived(packet: Packet<*>) {
        post(PacketEvent.Received(packet))
    }

    fun onPacketSent(packet: Packet<*>) {
        post(PacketEvent.Sent(packet))
    }

    // Internal stuff
    inline fun <reified T : Event> register(noinline cb: (T) -> Unit, add: Boolean = true): EventCall {
        if (add) events.getOrPut(T::class.java.name) { mutableListOf() }.add(cb)
        return object : EventCall {
            override fun unregister() = events[T::class.java.name]?.remove(cb) ?: false
            override fun register() = events.getOrPut(T::class.java.name) { mutableListOf() }.add(cb)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Event> post(event: T) {
        val listeners = events[event::class.java.name] ?: return
        for (cb in listeners.toList()) (cb as (T) -> Unit).invoke(event)
    }

    interface EventCall {
        fun unregister(): Boolean
        fun register(): Boolean
    }
}