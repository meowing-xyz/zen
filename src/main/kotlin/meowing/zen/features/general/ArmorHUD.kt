package meowing.zen.features.general

import meowing.zen.Zen
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.features.ClientTick
import meowing.zen.features.Feature
import meowing.zen.hud.HUDManager
import meowing.zen.utils.Render2D
import net.minecraft.init.Items
import net.minecraft.item.ItemStack

@Zen.Module
object ArmorHUD : Feature("armorhud") {
    private const val name = "Armor HUD"
    private var armor = emptyList<ItemStack>()
    private val armorhudvert by ConfigDelegate<Boolean>("armorhudvert")
    private val exampleArmor = listOf(
        ItemStack(Items.diamond_helmet),
        ItemStack(Items.diamond_chestplate),
        ItemStack(Items.diamond_leggings),
        ItemStack(Items.diamond_boots)
    )

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Armor HUD", ConfigElement(
                "armorhud",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("General", "Armor HUD", "Options", ConfigElement(
                "armorhudvert",
                "Vertical Armor HUD",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        HUDManager.registerCustom(name, if (armorhudvert) 16 else 70, if (armorhudvert) 70 else 16, this::HUDEditorRender)

        setupLoops {
            loop<ClientTick>(20) {
                val player = player ?: return@loop
                armor = player.inventory.armorInventory?.reversed() ?: emptyList()
            }
        }

        register<RenderEvent.Text> { event ->
            if (HUDManager.isEnabled(name)) render()
        }
    }

    private fun render() {
        val x = HUDManager.getX(name)
        val y = HUDManager.getY(name)
        val scale = HUDManager.getScale(name)
        drawHUD(x, y, scale, false)
    }

    private fun HUDEditorRender(x: Float, y: Float, width: Int, height: Int, scale: Float, partialTicks: Float, previewMode: Boolean) {
        drawHUD(x, y, 1f, true)
    }

    private fun drawHUD(x: Float, y: Float, scale: Float, preview: Boolean) {
        val iconSize = 16f * scale
        val spacing = 2f * scale
        val armorToRender = if (preview) exampleArmor else armor

        var currentX = x
        var currentY = y

        armorToRender.forEach { item ->
            @Suppress("SENSELESS_COMPARISON")
            if (item != null) Render2D.renderItem(item, currentX, currentY, if (preview) 1f else scale)
            if (armorhudvert) currentY += iconSize + spacing
            else currentX += iconSize + spacing
        }
    }
}