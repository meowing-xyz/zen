package xyz.meowing.zen.features.slayers

import xyz.meowing.zen.Zen
import xyz.meowing.zen.Zen.Companion.prefix
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.SkyblockEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.ChatUtils
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.util.Vec3
import java.util.regex.Pattern

@Zen.Module
object VengDamage : Feature("vengdmg", true) {
    private var nametagID = -1
    private val veng = Pattern.compile("^\\d+(,\\d+)*ﬗ$")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Slayers", "Vengeance damage tracker", ConfigElement(
                "vengdmg",
                "Vengeance damager tracker",
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }

    override fun initialize() {
        register<SkyblockEvent.Slayer.Spawn> { event ->
            nametagID = event.entityID
        }

        register<SkyblockEvent.DamageSplash> { event ->
            if (nametagID == -1) return@register

            val entityName = event.originalName.removeFormatting()
            val vengMatch = veng.matcher(entityName)
            if (!vengMatch.matches()) return@register
            val name = vengMatch.group(0).replace("ﬗ", "")

            val nametagEntity = world?.getEntityByID(nametagID) ?: return@register
            if (event.entityPos.distanceTo(Vec3(nametagEntity.posX, nametagEntity.posY, nametagEntity.posZ)) > 5) return@register

            val numStr = name.replace(",", "")
            val num = numStr.toLongOrNull() ?: return@register

            if (num > 500000) ChatUtils.addMessage("$prefix §fVeng DMG: §c${name}")
        }
    }
}