package xyz.meowing.zen.mixins;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * @author Aton
 */
@Mixin(value = Entity.class, priority = 3490)
public abstract class MixinEntity {
    @Shadow(remap = false)
    public abstract boolean equals(Object paramObject);
}