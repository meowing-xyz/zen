package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import meowing.zen.events.PacketEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.ItemUtils.getSBStrength
import meowing.zen.utils.ItemUtils.isHolding
import meowing.zen.utils.TitleUtils.showTitle
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.network.play.server.S29PacketSoundEffect

@Zen.Module
object RagnarockAlert : Feature("ragalert") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Ragnarok alert", ConfigElement(
                "ragalert",
                "Alert on ragnarok cast",
                "Shows a title with how much strength you gained when you use your ragnarok axe.",
                ElementType.Switch(false)
            ))
            .addElement("General", "Ragnarok alert", ConfigElement(
                "ragparty",
                "Send party message",
                "Sends a party message with how much strength you gained from your ragnarok axe.",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        register<PacketEvent.Received> { event ->
            if (event.packet is S29PacketSoundEffect) {
                val packet = event.packet
                if (packet.soundName != "mob.wolf.howl" || packet.pitch != 1.4920635f || !isHolding("RAGNAROCK_AXE")) return@register
                val strengthGain = ((mc.thePlayer?.heldItem?.getSBStrength ?: return@register) * 1.5).toInt()
                showTitle("§cRag §fCasted!", "§c❁ Strength:§b $strengthGain", 2000)
                if (config.ragparty) ChatUtils.command("pc Strength from Ragnarok: $strengthGain")
            }
        }

        register<ChatEvent.Receive> { event ->
            if (event.event.message.unformattedText.removeFormatting() == "Ragnarock was cancelled due to taking damage!") {
                showTitle("§cRag §4Cancelled!", null, 2000)
            }
        }
    }
}