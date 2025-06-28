package meowing.zen.events

import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.model.ModelBase
import net.minecraft.network.Packet
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S1CPacketEntityMetadata
import net.minecraft.network.play.server.S38PacketPlayerListItem
import net.minecraft.util.BlockPos
import net.minecraft.world.World
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.entity.living.EnderTeleportEvent

open class Event

open class CancellableEvent : Event() {
    private var cancelled = false
    fun cancel() {
        cancelled = true
    }
    fun isCancelled() = cancelled
}

class EntityJoinEvent(val entity: Entity) : CancellableEvent()
class EntityLeaveEvent(val entity: Entity) : Event()
class AttackEntityEvent(val entityPlayer: EntityPlayer, val target: Entity) : Event()
class HurtCamEvent(val partialTicks: Float) : CancellableEvent()
class TickEvent : Event()
class ServerTickEvent : Event()

class RenderWorldEvent(val partialTicks: Float) : Event()
class RenderLivingEntityEvent(val entity: EntityLivingBase, val x: Double, val y: Double, val z: Double) : CancellableEvent()
class RenderLivingEntityPostEvent(val entity: EntityLivingBase, val x: Double, val y: Double, val z: Double) : Event()
class RenderPlayerEvent(val player: EntityPlayer, val partialTicks: Float) : CancellableEvent()
class RenderPlayerPostEvent(val player: EntityPlayer, val partialTicks: Float) : CancellableEvent()
class RenderEntityModelEvent(
    val entity: EntityLivingBase,
    val model: ModelBase,
    val limbSwing: Float,
    val limbSwingAmount: Float,
    val ageInTicks: Float,
    val headYaw: Float,
    val headPitch: Float,
    val scaleFactor: Float
) : Event()
class RenderFallingBlockEvent(val entity: Entity, val x: Double, y: Double, z: Double, entityYaw: Float, partialTicks: Float) : CancellableEvent()
class BlockHighlightEvent(val blockPos: BlockPos, val partialTicks: Float) : CancellableEvent()
class EndermanTPEvent(event: EnderTeleportEvent) : CancellableEvent()

class GuiOpenEvent(val screen: GuiScreen) : Event()
class GuiCloseEvent : Event()
class GuiClickEvent(val screen: GuiScreen) : CancellableEvent()
class GuiKeyEvent(val screen: GuiScreen) : CancellableEvent()
class GuiBackgroundDrawEvent : CancellableEvent()

class ChatReceiveEvent(val event: ClientChatReceivedEvent) : Event()
class ChatMessageEvent(val message: String) : Event()
class ChatPacketEvent(val packet: S02PacketChat) : Event()

abstract class PacketEvent : Event() {
    class Received(val packet: Packet<*>) : CancellableEvent()
    class Sent(val packet: Packet<*>) : CancellableEvent()
}

class EntityMetadataEvent(val packet: S1CPacketEntityMetadata) : Event()
class ScoreboardEvent(val packet: Packet<*>) : Event()
class TablistEvent(val packet: S38PacketPlayerListItem) : Event()

class WorldLoadEvent(val world: World) : Event()
class WorldUnloadEvent(val world: World) : Event()
class WorldChangeEvent(val world: World) : Event()

class AreaEvent(val area: String?) : Event()
class SubAreaEvent(val subarea: String?) : Event()