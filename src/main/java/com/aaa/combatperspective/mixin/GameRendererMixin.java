package com.aaa.combatperspective.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    /** 第三人称下锁定基础 FOV，禁用疾跑/药水等一切 FOV 变化 */
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void lockFovInThirdPerson(Camera camera, float partialTick, boolean useFovSetting,
                                       CallbackInfoReturnable<Double> cir) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options == null) return;

        boolean thirdPerson = !mc.options.getCameraType().isFirstPerson()
                && !mc.options.getCameraType().isMirrored();
        if (thirdPerson) {
            cir.setReturnValue((double) mc.options.fov().get().intValue());
        }
    }
}
