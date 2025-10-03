package xyz.meowing.zen.config.ui.constraint

import gg.essential.elementa.UIComponent
import gg.essential.elementa.constraints.SizeConstraint
import gg.essential.elementa.constraints.ConstraintType
import gg.essential.elementa.constraints.resolution.ConstraintVisitor

class ChildHeightConstraint(private val padding: Float = 0f) : SizeConstraint {
    override var cachedValue = 0f
    override var recalculate = true
    override var constrainTo: UIComponent? = null

    override fun getHeightImpl(component: UIComponent): Float {
        val children = component.children
        if (children.isEmpty()) return padding

        val containerTop = component.getTop()
        var maxBottom = containerTop

        for (child in children) {
            val childBottom = child.getTop() + child.getHeight()
            if (childBottom > maxBottom) maxBottom = childBottom
        }

        return maxBottom - containerTop + padding
    }

    override fun getWidthImpl(component: UIComponent): Float {
        return getHeightImpl(component)
    }

    override fun visitImpl(visitor: ConstraintVisitor, type: ConstraintType) {}

    override fun getRadiusImpl(component: UIComponent): Float {
        return 0f
    }
}