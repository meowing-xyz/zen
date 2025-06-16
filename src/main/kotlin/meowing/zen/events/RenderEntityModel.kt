package meowing.zen.events

import net.minecraft.client.model.ModelBase
import net.minecraft.entity.EntityLivingBase
import net.minecraftforge.fml.common.eventhandler.Event

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