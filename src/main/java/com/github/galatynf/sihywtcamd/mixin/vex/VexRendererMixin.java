package com.github.galatynf.sihywtcamd.mixin.vex;

import com.github.galatynf.sihywtcamd.config.ModConfig;
import net.minecraft.client.render.entity.VexEntityRenderer;
import net.minecraft.entity.mob.VexEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VexEntityRenderer.class)
public abstract class VexRendererMixin {
    private static final Identifier VEX_TEXTURE = new Identifier("sihywtcamd", "textures/entity/vex.png");
    private static final Identifier VEX_CHARGING_TEXTURE = new Identifier("sihywtcamd", "textures/entity/vex_charging.png");

    @Inject(method = "getTexture*", at = @At("HEAD"), cancellable = true)
    private void getTexture(VexEntity vexEntity, CallbackInfoReturnable<Identifier> cir) {
        if (ModConfig.get().cosmetic.translucentVex) {
            cir.setReturnValue(vexEntity.isCharging() ? VEX_CHARGING_TEXTURE : VEX_TEXTURE);
        }
    }
}
