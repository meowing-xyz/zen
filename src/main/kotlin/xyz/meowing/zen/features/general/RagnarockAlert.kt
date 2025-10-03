package xyz.meowing.zen.features.general

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.ChatEvent
import xyz.meowing.zen.events.PacketEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.ChatUtils
import xyz.meowing.zen.utils.ItemUtils.getSBStrength
import xyz.meowing.zen.utils.ItemUtils.isHolding
import xyz.meowing.zen.utils.TitleUtils.showTitle
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.network.play.server.S29PacketSoundEffect

@Zen.Module
object RagnarockAlert : Feature("ragalert", true) {
    private val ragparty by ConfigDelegate<Boolean>("ragparty")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Ragnarok alert", ConfigElement(
                "ragalert",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("General", "Ragnarok alert", "Options", ConfigElement(
                "ragparty",
                "Send party message",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        register<PacketEvent.Received> { event ->
            if (event.packet is S29PacketSoundEffect) {
                val packet = event.packet
                if (packet.soundName != "mob.wolf.howl" || packet.pitch != 1.4920635f || !isHolding("RAGNAROCK_AXE")) return@register
                val strengthGain = ((player?.heldItem?.getSBStrength ?: return@register) * 1.5).toInt()
                showTitle("§cRag §fCasted!", "§c❁ Strength:§b $strengthGain", 2000)
                if (ragparty) ChatUtils.command("pc Strength from Ragnarok: $strengthGain")
            }
        }

        register<ChatEvent.Receive> { event ->
            if (event.event.message.unformattedText.removeFormatting() == "Ragnarock was cancelled due to taking damage!") {
                showTitle("§cRag §4Cancelled!", null, 2000)
            }
        }
    }
}