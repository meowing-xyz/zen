/*
    BSD 3-Clause License

    Copyright (c) 2024, odtheking

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:

    1. Redistributions of source code must retain the above copyright notice, this
       list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright notice,
       this list of conditions and the following disclaimer in the documentation
       and/or other materials provided with the distribution.

    3. Neither the name of the copyright holder nor the names of its
       contributors may be used to endorse or promote products derived from
       this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
    DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
    FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
    DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
    SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
    CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
    OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
    OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package meowing.zen.utils

import net.minecraft.client.Minecraft
import meowing.zen.events.RenderEvent
import meowing.zen.mixins.AccessorRenderLivingEntity
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.entity.RendererLivingEntity
import net.minecraft.client.renderer.entity.layers.LayerArmorBase
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor
import net.minecraft.client.shader.Framebuffer
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.EXTFramebufferObject
import org.lwjgl.opengl.EXTPackedDepthStencil
import org.lwjgl.opengl.GL11.*
import java.awt.Color

/**
 * Taken from Odin under BSD 3-Clause License
 * https://github.com/odtheking/Odin/blob/main/LICENSE
 */
object OutlineUtils {
    private val mc = Minecraft.getMinecraft()
    private var shown = false

    fun outlineEntity(
        event: RenderEvent.EntityModel,
        color: Color = Color(255, 255, 255, 255),
        lineWidth: Float = 2f,
        shouldCancelHurt: Boolean = true
    ) {
        if (shouldCancelHurt) event.entity.hurtTime = 0
        if (!shown && !OpenGlHelper.isFramebufferEnabled()) {
            ChatUtils.addMessage("§c[Zen] §fPlease disable §bFast Render§f in §bOptifine §7- It can cause unexpected issues with features.")
            ChatUtils.addMessage("§c[Zen] §fPath: §bOptions §f-> §bVideo Settings §f-> §bPerformance")
            shown = true
        }

        val fancyGraphics = mc.gameSettings.fancyGraphics
        val gamma = mc.gameSettings.gammaSetting
        mc.gameSettings.fancyGraphics = false
        mc.gameSettings.gammaSetting = 100000f

        val canBeSeen = mc.thePlayer.canEntityBeSeen(event.entity)
        val useDepth = !canBeSeen

        glPushMatrix()
        glPushAttrib(GL_ALL_ATTRIB_BITS)
        checkSetupFBO()

        if (!useDepth) {
            glDisable(GL_DEPTH_TEST)
            glDepthMask(false)
        }

        glDisable(GL_ALPHA_TEST)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_LIGHTING)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        glEnable(GL_STENCIL_TEST)
        glClear(GL_STENCIL_BUFFER_BIT)
        glClearStencil(0xF)
        glStencilFunc(GL_ALWAYS, 1, 0xFF)
        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE)
        glColorMask(false, false, false, false)

        render(event)

        glStencilFunc(GL_NOTEQUAL, 1, 0xFF)
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP)
        glColorMask(true, true, true, true)

        if (!useDepth) {
            glEnable(GL_POLYGON_OFFSET_LINE)
            glPolygonOffset(1.0f, -2000000f)
        }

        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0f, 240.0f)
        glLineWidth(lineWidth)
        glEnable(GL_LINE_SMOOTH)
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
        glColor4d(color.red / 255.0, color.green / 255.0, color.blue / 255.0, color.alpha / 255.0)

        render(event)

        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)
        glDisable(GL_STENCIL_TEST)
        glLineWidth(1f)
        glPopAttrib()
        glPopMatrix()

        mc.gameSettings.fancyGraphics = fancyGraphics
        mc.gameSettings.gammaSetting = gamma
    }

    private fun render(event: RenderEvent.EntityModel) {
        event.model.render(
            event.entity,
            event.limbSwing,
            event.limbSwingAmount,
            event.ageInTicks,
            event.headYaw,
            event.headPitch,
            event.scaleFactor
        )
        renderLayers(event)
    }

    private fun renderLayers(event: RenderEvent.EntityModel) {
        val entity = event.entity
        val renderer = mc.renderManager.getEntityRenderObject<EntityLivingBase>(entity)

        if (renderer is RendererLivingEntity<*>) {
            val layerRenderers = (renderer as AccessorRenderLivingEntity).layerRenderers

            glDisable(GL_TEXTURE_2D)

            for (layer in layerRenderers)
                if (layer is LayerBipedArmor || layer is LayerArmorBase<*>)
                    layer.doRenderLayer(
                        entity,
                        event.limbSwing,
                        event.limbSwingAmount,
                        Utils.getPartialTicks(),
                        event.ageInTicks,
                        event.headYaw,
                        event.headPitch,
                        event.scaleFactor
                    )
        }
    }

    private fun checkSetupFBO() {
        val fbo = mc.framebuffer ?: return
        if (fbo.depthBuffer <= -1) return
        setupFBO(fbo)
        fbo.depthBuffer = -1
    }

    private fun setupFBO(fbo: Framebuffer) {
        EXTFramebufferObject.glDeleteRenderbuffersEXT(fbo.depthBuffer)
        val stencilDepthBufferId = EXTFramebufferObject.glGenRenderbuffersEXT()
        EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencilDepthBufferId)
        EXTFramebufferObject.glRenderbufferStorageEXT(
            EXTFramebufferObject.GL_RENDERBUFFER_EXT,
            EXTPackedDepthStencil.GL_DEPTH_STENCIL_EXT,
            mc.displayWidth,
            mc.displayHeight
        )
        EXTFramebufferObject.glFramebufferRenderbufferEXT(
            EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
            EXTFramebufferObject.GL_STENCIL_ATTACHMENT_EXT,
            EXTFramebufferObject.GL_RENDERBUFFER_EXT,
            stencilDepthBufferId
        )
        EXTFramebufferObject.glFramebufferRenderbufferEXT(
            EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
            EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT,
            EXTFramebufferObject.GL_RENDERBUFFER_EXT,
            stencilDepthBufferId
        )
    }
}