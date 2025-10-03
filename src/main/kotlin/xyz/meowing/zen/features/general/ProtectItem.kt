package xyz.meowing.zen.features.general

import xyz.meowing.zen.Zen
import xyz.meowing.zen.Zen.Companion.mc
import xyz.meowing.zen.Zen.Companion.prefix
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.EntityEvent
import xyz.meowing.zen.events.GuiEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.ChatUtils
import xyz.meowing.zen.utils.CommandUtils
import xyz.meowing.zen.utils.DataUtils
import xyz.meowing.zen.utils.FontUtils
import xyz.meowing.zen.utils.ItemUtils.lore
import xyz.meowing.zen.utils.ItemUtils.uuid
import xyz.meowing.zen.utils.LocationUtils
import xyz.meowing.zen.utils.Render2D
import xyz.meowing.zen.utils.TickUtils
import xyz.meowing.zen.utils.Utils.chestName
import net.minecraft.command.ICommandSender
import net.minecraft.init.Blocks
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.Item
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.item.ItemStack
import org.lwjgl.input.Keyboard
import java.awt.Color

/**
 * Module contains code from Skytils
 *
 * @license GPL-3.0
 */
@Zen.Module
object ProtectItem : Feature("protectitem", true) {
    val protectedItems = DataUtils("protected_items", mutableSetOf<String>())
    val protectedTypes = DataUtils("protected_types", mutableSetOf<String>())

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Item Protection", ConfigElement(
                "protectitem",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("General", "Item Protection", "", ConfigElement(
                "",
                null,
                ElementType.TextParagraph("Tries to prevent you from dropping items that you have protected using §c/protectitem\n§7Aliases: /pitem, /zenpi")
            ))
            .addElement("General", "Item Protection", "GUI", ConfigElement(
                "protectItem.GuiButton",
                "Protect Item GUI",
                ElementType.Button("Open GUI") {
                    mc.displayGuiScreen(ItemProtectGUI())
                }
            ))
    }

    override fun initialize() {
        register<EntityEvent.ItemToss> { event ->
            if (LocationUtils.checkArea("catacombs")) return@register
            if (isProtected(event.stack)) {
                sendProtectionMessage("dropping", event.stack.displayName)
                event.cancel()
            }
        }

        register<GuiEvent.Close> { event ->
            val item = player?.inventory?.itemStack ?: return@register
            if (isProtected(item)) {
                for (slot in event.container.inventorySlots) {
                    if (slot.inventory !== player?.inventory || slot.hasStack || !slot.isItemValid(item)) continue
                    mc.playerController.windowClick(event.container.windowId, slot.slotNumber, 0, 0, player)
                    sendProtectionMessage("dropping", item.displayName)
                    event.cancel()
                    return@register
                }
            }
        }

        register<GuiEvent.Slot.Click> { event ->
            if (event.container is ContainerChest) handleChestClick(event)
            handleDropClick(event)
        }
    }

    private fun handleChestClick(event: GuiEvent.Slot.Click) {
        val container = event.container as ContainerChest
        val inv = container.lowerChestInventory
        val chestName = event.gui?.chestName ?: return
        val slot = event.slot ?: return
        val item = slot.stack ?: return

        if (!slot.hasStack || !isProtected(item)) return

        when {
            chestName.startsWith("Salvage") -> {
                val inSalvageGui = item.displayName.contains("Salvage") || item.displayName.contains("Essence")
                if (inSalvageGui || slot.inventory === player?.inventory) {
                    sendProtectionMessage("salvaging", item.displayName)
                    event.cancel()
                }
            }

            chestName != "Large Chest" && inv.sizeInventory == 54 && !chestName.contains("Auction") -> {
                val sellItem = inv.getStackInSlot(49)
                val isSellGui = sellItem?.item === Item.getItemFromBlock(Blocks.hopper) && (sellItem.displayName.contains("Sell Item") || sellItem.lore.any { it.contains("buyback") })

                if (isSellGui && event.slotId != 49 && slot.inventory === mc.thePlayer.inventory) {
                    sendProtectionMessage("selling", item.displayName)
                    event.cancel()
                }
            }

            chestName.startsWith("Create ") && chestName.endsWith(" Auction") -> {
                if (inv.getStackInSlot(13) != null) {
                    sendProtectionMessage("auctioning", item.displayName)
                    event.cancel()
                }
            }
        }
    }

    private fun handleDropClick(event: GuiEvent.Slot.Click) {
        val item = when {
            event.slotId == -999 && player?.inventory?.itemStack != null && event.clickType != 5 -> player?.inventory?.itemStack
            event.clickType == 4 && event.slotId != -999 && event.slot?.hasStack == true -> event.slot.stack
            else -> return
        } ?: return

        if (isProtected(item)) {
            sendProtectionMessage("dropping", item.displayName)
            event.cancel()
        }
    }

    private fun isProtected(item: ItemStack): Boolean {
        val itemUuid = item.uuid
        if (itemUuid.isNotEmpty() && itemUuid in protectedItems()) return true

        val itemId = item.item.unlocalizedName
        return itemId in protectedTypes()
    }

    private fun sendProtectionMessage(action: String, itemName: String) {
        ChatUtils.addMessage("$prefix §fStopped you from $action $itemName§r!")
    }
}

@Zen.Command
object ProtectItemCommand : CommandUtils("protectitem", aliases = listOf("zenprotect", "pitem", "zenpi")) {
    override fun processCommand(sender: ICommandSender, args: Array<out String?>?) {
        val stringArgs = args?.filterNotNull()?.toTypedArray() ?: return

        if (stringArgs.size == 1 && stringArgs[0] == "gui") {
            TickUtils.schedule(2) {
                mc.displayGuiScreen(ItemProtectGUI())
            }
            return
        }

        val heldItem = mc.thePlayer?.heldItem
        if (heldItem == null) {
            ChatUtils.addMessage("$prefix §cYou must be holding an item!")
            return
        }

        val itemUuid = heldItem.uuid
        val itemId = heldItem.item.unlocalizedName

        if (itemUuid.isEmpty()) {
            ProtectItem.protectedTypes.update {
                if (itemId in this) {
                    remove(itemId)
                    ChatUtils.addMessage("$prefix §fRemoved all ${heldItem.displayName} §ffrom protected items!")
                } else {
                    add(itemId)
                    ChatUtils.addMessage("$prefix §fAdded all ${heldItem.displayName} §fto protected items! §7(No UUID - protecting by type)")
                }
            }
        } else {
            ProtectItem.protectedItems.update {
                if (itemUuid in this) {
                    remove(itemUuid)
                    ChatUtils.addMessage("$prefix §fRemoved ${heldItem.displayName} §ffrom protected items!")
                } else {
                    add(itemUuid)
                    ChatUtils.addMessage("$prefix §fAdded ${heldItem.displayName} §fto protected items!")
                }
            }
        }
    }
}

class ItemProtectGUI : GuiScreen() {
    private val slots = mutableListOf<InventorySlot>()
    private var hoveredSlot = -1
    private val slotSize = 18
    private val fontObj = FontUtils.getFontRenderer()

    private val guiWidth = 194
    private val guiHeight = 160

    private val titleColor = Color(180, 220, 255).rgb
    private val instructionColor = Color(200, 200, 200).rgb
    private val backgroundBlur = Color(0, 0, 0, 120).rgb
    private val guiBackground = Color(40, 40, 50, 200).rgb
    private val guiBorder = Color(120, 140, 160, 255).rgb
    private val protectedSlotColor = Color(40, 120, 40, 180).rgb
    private val hoveredSlotColor = Color(80, 80, 100, 180).rgb
    private val normalSlotColor = Color(60, 60, 70, 180).rgb
    private val protectedBorder = Color(80, 200, 80, 255).rgb
    private val typeProtectedColor = Color(120, 100, 40, 180).rgb
    private val typeProtectedBorder = Color(200, 160, 80, 255).rgb

    data class InventorySlot(
        val stack: ItemStack?,
        val uuid: String,
        val itemId: String,
        var isProtected: Boolean,
        var isTypeProtected: Boolean,
        val x: Int,
        val y: Int,
        val slotIndex: Int
    )

    override fun initGui() {
        super.initGui()
        loadInventory()
    }

    private fun loadInventory() {
        slots.clear()
        val player = mc.thePlayer ?: return
        val protectedSet = ProtectItem.protectedItems()
        val protectedTypeSet = ProtectItem.protectedTypes()
        val sr = ScaledResolution(mc)
        val guiX = (sr.scaledWidth - guiWidth) / 2
        val guiY = (sr.scaledHeight - guiHeight) / 2

        val armorSlots = listOf(39, 38, 37, 36)
        armorSlots.forEachIndexed { index, slotIndex ->
            val stack = player.inventory.getStackInSlot(slotIndex)
            val uuid = stack?.uuid ?: ""
            val itemId = stack?.item?.unlocalizedName ?: ""
            val x = guiX + 8 + (index * 20)
            val y = guiY + 30
            slots.add(InventorySlot(
                stack, uuid, itemId,
                uuid.isNotEmpty() && uuid in protectedSet,
                itemId.isNotEmpty() && itemId in protectedTypeSet,
                x, y, slotIndex
            ))
        }

        for (row in 0..2) {
            for (col in 0..8) {
                val slotIndex = 9 + row * 9 + col
                val stack = player.inventory.getStackInSlot(slotIndex)
                val uuid = stack?.uuid ?: ""
                val itemId = stack?.item?.unlocalizedName ?: ""
                val x = guiX + 8 + (col * 20)
                val y = guiY + 60 + (row * 20)
                slots.add(InventorySlot(
                    stack, uuid, itemId,
                    uuid.isNotEmpty() && uuid in protectedSet,
                    itemId.isNotEmpty() && itemId in protectedTypeSet,
                    x, y, slotIndex
                ))
            }
        }

        for (col in 0..8) {
            val stack = player.inventory.getStackInSlot(col)
            val uuid = stack?.uuid ?: ""
            val itemId = stack?.item?.unlocalizedName ?: ""
            val x = guiX + 8 + (col * 20)
            val y = guiY + 130
            slots.add(InventorySlot(
                stack, uuid, itemId,
                uuid.isNotEmpty() && uuid in protectedSet,
                itemId.isNotEmpty() && itemId in protectedTypeSet,
                x, y, col
            ))
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val sr = ScaledResolution(mc)
        drawGradientRect(0, 0, sr.scaledWidth, sr.scaledHeight, backgroundBlur, backgroundBlur)

        val guiX = (sr.scaledWidth - guiWidth) / 2
        val guiY = (sr.scaledHeight - guiHeight) / 2

        drawRect(guiX, guiY, guiX + guiWidth, guiY + guiHeight, guiBackground)
        drawHollowRect(guiX, guiY, guiX + guiWidth, guiY + guiHeight, guiBorder)

        val title = "Item Protection Manager"
        val titleWidth = fontRendererObj.getStringWidth(title)
        val titleX = guiX + (guiWidth - titleWidth) / 2
        Render2D.renderString(title, titleX.toFloat(), (guiY + 8).toFloat(), 1f, titleColor, Render2D.TextStyle.DROP_SHADOW)

        hoveredSlot = -1
        slots.forEachIndexed { index, slot ->
            renderSlot(slot, mouseX, mouseY, index)
        }

        if (hoveredSlot >= 0) {
            renderTooltip(slots[hoveredSlot], mouseX, mouseY)
        }

        val instructions = "L to toggle protection • ESC to close"
        val textWidth = fontRendererObj.getStringWidth(instructions)
        Render2D.renderString(instructions, (guiX + (guiWidth - textWidth) / 2).toFloat(), (guiY + guiHeight + 5).toFloat(), 1f, instructionColor, Render2D.TextStyle.DROP_SHADOW)

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    private fun renderSlot(slot: InventorySlot, mouseX: Int, mouseY: Int, index: Int) {
        val isHovered = mouseX >= slot.x && mouseX <= slot.x + slotSize && mouseY >= slot.y && mouseY <= slot.y + slotSize

        if (isHovered) hoveredSlot = index

        val slotColor = when {
            slot.isProtected -> protectedSlotColor
            slot.isTypeProtected -> typeProtectedColor
            isHovered -> hoveredSlotColor
            else -> normalSlotColor
        }

        drawRect(slot.x, slot.y, slot.x + slotSize, slot.y + slotSize, slotColor)

        val borderColor = when {
            slot.isProtected -> protectedBorder
            slot.isTypeProtected -> typeProtectedBorder
            else -> guiBorder
        }
        drawHollowRect(slot.x, slot.y, slot.x + slotSize, slot.y + slotSize, borderColor)

        slot.stack?.let { stack ->
            Render2D.renderItem(stack, slot.x + 1f, slot.y + 1f, 1f)
        }
    }

    private fun renderTooltip(slot: InventorySlot, mouseX: Int, mouseY: Int) {
        val lines = mutableListOf<String>()

        when {
            slot.stack == null -> lines.add("§7Empty Slot")
            else -> {
                lines.add(slot.stack.displayName)
                when {
                    slot.isProtected -> lines.add("§aProtected (UUID) - Press L to unprotect")
                    slot.isTypeProtected -> lines.add("§6Protected (All of type) - Press L to unprotect")
                    slot.uuid.isNotEmpty() -> lines.add("§7Not protected - Press L to protect")
                    else -> lines.add("§7No UUID - Press L to protect all of this type")
                }
            }
        }

        GlStateManager.pushMatrix()
        renderHoveringString(lines, mouseX.toFloat(), mouseY.toFloat())
        GlStateManager.popMatrix()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton == 0) {
            slots.forEachIndexed { _, slot ->
                if (mouseX >= slot.x && mouseX <= slot.x + slotSize && mouseY >= slot.y && mouseY <= slot.y + slotSize) {
                    if (slot.stack != null) toggleProtection(slot)
                    return
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        when (keyCode) {
            Keyboard.KEY_ESCAPE -> {
                mc.displayGuiScreen(null)
                return
            }
            Keyboard.KEY_L -> {
                if (hoveredSlot >= 0) {
                    val slot = slots[hoveredSlot]
                    if (slot.stack != null) {
                        toggleProtection(slot)
                    }
                    return
                }
            }
        }
        super.keyTyped(typedChar, keyCode)
    }

    private fun toggleProtection(slot: InventorySlot) {
        if (slot.uuid.isNotEmpty()) {
            ProtectItem.protectedItems.update {
                if (slot.uuid in this) {
                    remove(slot.uuid)
                    slot.isProtected = false
                } else {
                    add(slot.uuid)
                    slot.isProtected = true
                }
            }
        } else {
            ProtectItem.protectedTypes.update {
                if (slot.itemId in this) {
                    remove(slot.itemId)
                    slots.forEach { s ->
                        if (s.itemId == slot.itemId) {
                            s.isTypeProtected = false
                        }
                    }
                } else {
                    add(slot.itemId)
                    slots.forEach { s ->
                        if (s.itemId == slot.itemId) {
                            s.isTypeProtected = true
                        }
                    }
                }
            }
        }
    }

    private fun renderHoveringString(lines: MutableList<String>, mouseX: Float, mouseY: Float) {
        val sr = ScaledResolution(mc)
        val maxWidth = lines.maxOfOrNull { fontObj.getStringWidth(it) } ?: 0
        val tooltipWidth = maxWidth + 8
        val tooltipHeight = lines.size * fontObj.FONT_HEIGHT + 6
        val tooltipX = (mouseX.toInt() - tooltipWidth / 2).coerceIn(2, sr.scaledWidth - tooltipWidth - 2)
        val tooltipY = (mouseY.toInt() - tooltipHeight - 8).coerceAtLeast(2)

        GlStateManager.pushMatrix()
        GlStateManager.translate(0f, 0f, 300f)
        GlStateManager.disableDepth()

        drawRect(tooltipX, tooltipY, tooltipX + tooltipWidth, tooltipY + tooltipHeight, 0xC8000000.toInt())
        drawRect(tooltipX - 1, tooltipY - 1, tooltipX + tooltipWidth + 1, tooltipY, 0xFF646464.toInt())
        drawRect(tooltipX - 1, tooltipY + tooltipHeight, tooltipX + tooltipWidth + 1, tooltipY + tooltipHeight + 1, 0xFF646464.toInt())
        drawRect(tooltipX - 1, tooltipY, tooltipX, tooltipY + tooltipHeight, 0xFF646464.toInt())
        drawRect(tooltipX + tooltipWidth, tooltipY, tooltipX + tooltipWidth + 1, tooltipY + tooltipHeight, 0xFF646464.toInt())

        lines.forEachIndexed { index, line ->
            Render2D.renderString(line, tooltipX + 4f, tooltipY + 4f + (index * 10f), 1f)
        }

        GlStateManager.enableDepth()
        GlStateManager.popMatrix()
    }

    private fun drawHollowRect(x1: Int, y1: Int, x2: Int, y2: Int, color: Int) {
        drawRect(x1, y1, x2, y1 + 1, color)
        drawRect(x1, y2 - 1, x2, y2, color)
        drawRect(x1, y1, x1 + 1, y2, color)
        drawRect(x2 - 1, y1, x2, y2, color)
    }

    override fun doesGuiPauseGame() = false
}