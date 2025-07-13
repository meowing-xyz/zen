package meowing.zen.utils

import meowing.zen.Zen.Companion.mc
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import kotlin.collections.orEmpty

// Taken from Odin 1.8.9
// https://github.com/odtheking/Odin/blob/main/src/main/kotlin/me/odinmain/utils/skyblock/ItemUtils.kt
object ItemUtils {
    val strengthRegex = Regex("Strength: \\+(\\d+)")
    val abilityRegex = Regex("Ability:.*RIGHT CLICK")

    inline val ItemStack?.extraAttributes: NBTTagCompound? get() = this?.getSubCompound("ExtraAttributes", false)

    inline val ItemStack?.skyblockID: String get() = this?.extraAttributes?.getString("id") ?: ""

    inline val ItemStack?.lore: List<String>
        get() = this?.tagCompound?.getCompoundTag("display")?.getTagList("Lore", 8)?.let {
            List(it.tagCount()) { i -> it.getStringTagAt(i) }
        }.orEmpty()

    inline val ItemStack?.uuid: String get() = this?.extraAttributes?.getString("uuid") ?: ""

    inline val ItemStack?.hasAbility: Boolean get() = this?.lore?.any { abilityRegex.containsMatchIn(it) } == true

    inline val ItemStack?.getSBStrength: Int
        get() = this?.lore?.asSequence()
            ?.map { it.removeFormatting() }
            ?.firstOrNull { it.startsWith("Strength:") }
            ?.let { strengthRegex.find(it)?.groups?.get(1)?.value?.toIntOrNull() } ?: 0

    inline val ItemStack?.isShortbow: Boolean get() = this?.lore?.any { "Shortbow: Instantly shoots!" in it } == true

    fun isHolding(vararg id: String): Boolean = mc.thePlayer?.heldItem?.skyblockID in id

    fun ItemStack.displayName(): String {
        val displayTag = this.tagCompound?.getCompoundTag("display")
        return if (displayTag?.hasKey("Name", 8) == true) displayTag.getString("Name")
            else this.item.getItemStackDisplayName(this)
    }
}