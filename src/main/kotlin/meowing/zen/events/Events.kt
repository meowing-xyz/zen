package meowing.zen.events

import meowing.zen.api.EntityDetection
import meowing.zen.api.ItemAbility
import meowing.zen.api.PartyTracker.PartyMember
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.model.ModelBase
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.Container
import net.minecraft.item.ItemStack
import net.minecraft.network.Packet
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S0FPacketSpawnMob
import net.minecraft.network.play.server.S1CPacketEntityMetadata
import net.minecraft.network.play.server.S38PacketPlayerListItem
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraft.world.World
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.MouseEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.event.entity.living.EnderTeleportEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent

abstract class Event

abstract class CancellableEvent : Event() {
    private var cancelled = false
    fun cancel() {
        cancelled = true
    }
    fun isCancelled() = cancelled
}

class HurtCamEvent(val partialTicks: Float) : CancellableEvent()
class SidebarUpdateEvent(val lines: List<String>) : Event()
class TablistEvent(val packet: S38PacketPlayerListItem) : Event()

class ItemTooltipEvent(val lines: MutableList<String>,val itemStack: ItemStack) : CancellableEvent()

abstract class MouseEvent {
    class Click(val event: MouseEvent) : Event()
    class Release(val event: MouseEvent) : Event()
    class Scroll(val event: MouseEvent) : Event()
    class Move(val event: MouseEvent) : Event()
}

abstract class KeyEvent {
    class Press(val keyCode: Int) : Event()
    class Release(val keyCode: Int) : Event()
}

abstract class EntityEvent {
    class Join(val entity: Entity) : CancellableEvent()
    class Leave(val entity: Entity) : Event()
    class Attack(val entityPlayer: EntityPlayer, val target: Entity) : Event()
    class Metadata(val packet: S1CPacketEntityMetadata, val entity: Entity, val name: String) : CancellableEvent()
    class Spawn(val packet: S0FPacketSpawnMob, val entity: Entity, val name: String) : CancellableEvent()
    class Interact(val action: PlayerInteractEvent.Action, val pos: BlockPos?) : Event()
    class ArrowHit(val shooterName: String, val hitEntity: Entity) : Event()
    class ItemToss(val stack: ItemStack) : CancellableEvent()
}

abstract class TickEvent {
    class Client : Event()
    class Server : Event()
}

abstract class RenderEvent {
    class World(val partialTicks: Float) : Event()
    class EntityModel(val entity: EntityLivingBase, val model: ModelBase, val limbSwing: Float, val limbSwingAmount: Float, val ageInTicks: Float, val headYaw: Float, val headPitch: Float, val scaleFactor: Float) : Event()
    class Text(val partialTicks: Float, val resolution: ScaledResolution) : CancellableEvent()
    class HUD(val elementType: RenderGameOverlayEvent.ElementType, val partialTicks: Float, val resolution: ScaledResolution) : CancellableEvent()
    class BlockHighlight(val blockPos: BlockPos, val partialTicks: Float) : CancellableEvent()
    class EndermanTP(val event: EnderTeleportEvent) : CancellableEvent()
    class GuardianLaser(val entity: net.minecraft.entity.Entity, val target: net.minecraft.entity.Entity) : CancellableEvent()

    abstract class Entity {
        class Pre(val entity: EntityLivingBase, val x: Double, val y: Double, val z: Double) : CancellableEvent()
        class Post(val entity: EntityLivingBase, val x: Double, val y: Double, val z: Double) : CancellableEvent()
    }

    abstract class Player {
        class Pre(val player: EntityPlayer, val x: Double, val y: Double, val z: Double, val partialTicks: Float) : CancellableEvent()
        class Post(val player: EntityPlayer, val x: Double, val y: Double, val z: Double, val partialTicks: Float) : CancellableEvent()
    }
}

abstract class PartyEvent {
    class Changed(val type: PartyChangeType, val playerName: String? = null, val members: Map<String, PartyMember>) : Event()
}

enum class PartyChangeType {
    MEMBER_JOINED, MEMBER_LEFT, PLAYER_JOINED, PLAYER_LEFT, LEADER_CHANGED, DISBANDED, LIST, PARTY_FINDER
}

abstract class GuiEvent {
    class Open(val screen: GuiScreen) : Event()
    class Close(val gui: GuiContainer, val container: Container) : CancellableEvent()
    class Click(val gui: GuiScreen) : CancellableEvent()
    class Key(val gui: GuiScreen) : CancellableEvent()
    class BackgroundDraw(val gui: GuiScreen) : CancellableEvent()
    abstract class Slot {
        class Click(val slot: net.minecraft.inventory.Slot?, val gui: GuiContainer, val container: Container, val slotId: Int, val clickedButton: Int, val clickType: Int) : CancellableEvent()
        class RenderPre(val slot: net.minecraft.inventory.Slot, val gui: GuiContainer) : CancellableEvent()
        class RenderPost(val slot: net.minecraft.inventory.Slot, val gui: GuiContainer) : CancellableEvent()
    }
}

abstract class ChatEvent {
    class Receive(val event: ClientChatReceivedEvent) : CancellableEvent()
    class Send(val message: String) : CancellableEvent()
    class Packet(val packet: S02PacketChat) : CancellableEvent()
}

abstract class PacketEvent {
    class Received(val packet: Packet<*>) : CancellableEvent()
    class Sent(val packet: Packet<*>) : CancellableEvent()
    class ReceivedPost(val packet: Packet<*>) : Event()
    class SentPost(val packet: Packet<*>) : Event()
}

abstract class WorldEvent {
    class Load(val world: World) : Event()
    class Unload(val world: World) : Event()
    class Change(val world: World) : Event()
}

abstract class GameEvent {
    class Load() : Event()
    class Unload() : Event()
    class Disconnect() : Event()
    class ActionBar(val event: ClientChatReceivedEvent) : CancellableEvent()
}

abstract class SkyblockEvent {
    abstract class Slayer {
        class Spawn(val entity: Entity, val entityID: Int, val packet: S1CPacketEntityMetadata) : Event()
        class Death(val entity: Entity, val entityID: Int) : Event()
        class Cleanup() : Event()
        class Fail() : Event()
        class QuestStart() : Event()
    }

    class ItemAbilityUsed(val ability: ItemAbility.ItemAbility) : Event()
    class EntitySpawn(val skyblockMob: EntityDetection.SkyblockMob) : Event()
    class DamageSplash(val damage: Int, val originalName: String, val entityPos: Vec3, val packet: S0FPacketSpawnMob) : CancellableEvent()
}

abstract class AreaEvent {
    class Main(val area: String?) : Event()
    class Sub(val subarea: String?) : Event()
    class Skyblock(val newVal: Boolean) : Event()
}