package xyz.meowing.zen.features.general

import xyz.meowing.zen.Zen
import xyz.meowing.zen.Zen.Companion.mc
import xyz.meowing.zen.events.EventBus
import xyz.meowing.zen.events.GuiEvent
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.features.visuals.CustomSize
import xyz.meowing.zen.features.visuals.CustomSize.customX
import xyz.meowing.zen.features.visuals.CustomSize.customY
import xyz.meowing.zen.features.visuals.CustomSize.customZ
import xyz.meowing.zen.utils.NetworkUtils
import xyz.meowing.zen.utils.OutlineUtils
import xyz.meowing.zen.utils.Utils
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.model.ModelBase
import net.minecraft.client.model.ModelRenderer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.entity.layers.LayerRenderer
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation
import java.awt.Color

@Zen.Module
object ContributorColor {
    private var contributorData: Map<String, ContributorInfo>? = null
    private var inGui = false

    data class ContributorInfo(
        val displayName: String,
        val highlightColor: List<Int>
    )

    init {
        NetworkUtils.fetchJson<Map<String, Map<String, Any>>>(
            "https://raw.githubusercontent.com/kiwidotzip/zen-data/refs/heads/main/assets/ContributorColor.json",
            onSuccess = { data ->
                contributorData = data.mapValues { (_, info) ->
                    val colorList = (info["highlightColor"] as? List<Int>)
                    ContributorInfo(
                        displayName = info["displayName"] as? String ?: "",
                        highlightColor = if (colorList?.size == 4) colorList else listOf(0, 255, 255, 127)
                    )
                }
            },
            onError = {
                contributorData = mapOf(
                    "aurielyn" to ContributorInfo("§daurielyn§r", listOf(255, 0, 255, 127)),
                    "cheattriggers" to ContributorInfo("§cKiwi§r", listOf(255, 0, 0, 127)),
                    "Aur0raDye" to ContributorInfo("§5Mango 6 7§r", listOf(170, 0, 170, 127)),
                    "Skyblock_Lobby" to ContributorInfo("§9Skyblock_Lobby§r", listOf(85, 85, 255, 127))
                )
            }
        )

        EventBus.register<RenderEvent.EntityModel> ({ event ->
            contributorData?.get(event.entity.name.removeFormatting())?.let { info ->
                val (r, g, b, a) = info.highlightColor
                OutlineUtils.outlineEntity(event, Color(r, g, b, a), shouldCancelHurt = false)
            }
        })

        EventBus.register<GuiEvent.Open> ({ inGui = true })

        EventBus.register<GuiEvent.Close> ({ inGui = false })
    }

    @JvmStatic
    fun replace(text: String?): String? {
        if (text == null || contributorData == null) return text

        return contributorData!!.entries.fold(text) { acc, (key, info) ->
            acc.replace(key, info.displayName)
        }
    }

    class CosmeticRendering : LayerRenderer<EntityLivingBase> {
        override fun doRenderLayer(
            entityLivingBaseIn: EntityLivingBase, _1: Float, _2: Float, _3: Float, _4: Float, _5: Float, _6: Float, _7: Float
        ) {
            val player = entityLivingBaseIn as EntityPlayer
            val contributorInfo = contributorData?.get(player.name.removeFormatting())

            if (contributorInfo != null) {
                GlStateManager.pushMatrix()
                GlStateManager.disableLighting()

                val (r, g, b, _) = contributorInfo.highlightColor
                AngelHalo.drawHalo(player, r / 255f, g / 255f, b / 255f)

                GlStateManager.enableLighting()
                GlStateManager.popMatrix()
            }
        }

        override fun shouldCombineTextures(): Boolean {
            return false
        }
    }

    /*
     * Modified code from NoammAddons
     * https://github.com/Noamm9/NoammAddons/blob/master/src/main/kotlin/noammaddons/features/impl/misc/Cosmetics.kt
     */
    object AngelHalo : ModelBase() {
        private val haloTexture = ResourceLocation("zen", "HaloTexture.png")
        private val halo: ModelRenderer

        init {
            textureWidth = 32
            textureHeight = 32
            halo = ModelRenderer(this, "ring")

            halo.addBox(-2f, -10f, -6f, 1, 1, 1, 0f)
            halo.addBox(1f, -10f, 5f, 1, 1, 1, 0f)
            halo.addBox(0f, -10f, 5f, 1, 1, 1, 0f)
            halo.addBox(-1f, -10f, 5f, 1, 1, 1, 0f)
            halo.addBox(-2f, -10f, 5f, 1, 1, 1, 0f)
            halo.addBox(-2f, -10f, 4f, 1, 1, 1, 0f)
            halo.addBox(-3f, -10f, 4f, 1, 1, 1, 0f)
            halo.addBox(-4f, -10f, 4f, 1, 1, 1, 0f)
            halo.addBox(-4f, -10f, 3f, 1, 1, 1, 0f)
            halo.addBox(-5f, -10f, 3f, 1, 1, 1, 0f)
            halo.addBox(-5f, -10f, 2f, 1, 1, 1, 0f)
            halo.addBox(-5f, -10f, 1f, 1, 1, 1, 0f)
            halo.addBox(-6f, -10f, 1f, 1, 1, 1, 0f)
            halo.addBox(-6f, -10f, -2f, 1, 1, 1, 0f)
            halo.addBox(-6f, -10f, -1f, 1, 1, 1, 0f)
            halo.addBox(1f, -10f, 4f, 1, 1, 1, 0f)
            halo.addBox(2f, -10f, 4f, 1, 1, 1, 0f)
            halo.addBox(3f, -10f, 4f, 1, 1, 1, 0f)
            halo.addBox(3f, -10f, 3f, 1, 1, 1, 0f)
            halo.addBox(4f, -10f, 3f, 1, 1, 1, 0f)
            halo.addBox(4f, -10f, 2f, 1, 1, 1, 0f)
            halo.addBox(4f, -10f, 1f, 1, 1, 1, 0f)
            halo.addBox(5f, -10f, 1f, 1, 1, 1, 0f)
            halo.addBox(5f, -10f, -0f, 1, 1, 1, 0f)
            halo.addBox(5f, -10f, -1f, 1, 1, 1, 0f)
            halo.addBox(5f, -10f, -2f, 1, 1, 1, 0f)
            halo.addBox(-6f, -10f, 0f, 1, 1, 1, 0f)
            halo.addBox(-5f, -10f, -2f, 1, 1, 1, 0f)
            halo.addBox(-5f, -10f, -3f, 1, 1, 1, 0f)
            halo.addBox(-5f, -10f, -4f, 1, 1, 1, 0f)
            halo.addBox(-4f, -10f, -4f, 1, 1, 1, 0f)
            halo.addBox(-4f, -10f, -5f, 1, 1, 1, 0f)
            halo.addBox(-3f, -10f, -5f, 1, 1, 1, 0f)
            halo.addBox(-2f, -10f, -5f, 1, 1, 1, 0f)
            halo.addBox(4f, -10f, -2f, 1, 1, 1, 0f)
            halo.addBox(4f, -10f, -3f, 1, 1, 1, 0f)
            halo.addBox(4f, -10f, -4f, 1, 1, 1, 0f)
            halo.addBox(3f, -10f, -4f, 1, 1, 1, 0f)
            halo.addBox(3f, -10f, -5f, 1, 1, 1, 0f)
            halo.addBox(2f, -10f, -5f, 1, 1, 1, 0f)
            halo.addBox(1f, -10f, -5f, 1, 1, 1, 0f)
            halo.addBox(1f, -10f, -6f, 1, 1, 1, 0f)
            halo.addBox(0f, -10f, -6f, 1, 1, 1, 0f)
            halo.addBox(-1f, -10f, -6f, 1, 1, 1, 0f)

            halo.rotationPointX = 0f
            halo.rotationPointY = 0f
            halo.render(0.0625f)
        }

        fun drawHalo(player: EntityPlayer, r: Float, g: Float, b: Float) {
            val rotation = if (inGui) player.renderYawOffset else interpolate(player.prevRenderYawOffset, player.renderYawOffset)
            val yPos = if (CustomSize.isEnabled() && customY == 1.0) 0.3f else 0.45f

            GlStateManager.pushMatrix()
            GlStateManager.translate(0f, -yPos, 0f)

            if (CustomSize.isEnabled()) GlStateManager.scale(customX, customY, customZ)

            val rotationAngle = (System.currentTimeMillis() % 3600) / 10f
            GlStateManager.rotate(rotationAngle - rotation, 0f, 1f, 0f)

            if (player.isSneaking) GlStateManager.translate(0f, yPos, 0f)

            GlStateManager.color(r, g, b, 1f)
            mc.textureManager.bindTexture(haloTexture)
            GlStateManager.enableCull()

            halo.render(0.0625f)

            GlStateManager.disableCull()
            GlStateManager.popMatrix()
        }
    }

    private fun interpolate(yaw1: Float, yaw2: Float): Float {
        var f = (yaw1 + (yaw2 - yaw1) * Utils.partialTicks) % 360
        if (f < 0) f += 360f
        return f
    }
}