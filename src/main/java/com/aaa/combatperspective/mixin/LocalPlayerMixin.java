package com.aaa.combatperspective.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void turnThenMove(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer self = (LocalPlayer)(Object)this;
        boolean isthirdPersonback = !mc.options.getCameraType().isFirstPerson()
                && !mc.options.getCameraType().isMirrored();
        boolean noScreen = mc.screen == null;

        if(isthirdPersonback && noScreen) {
            // 禁用左右侧向移动
            self.input.left = false;
            self.input.right = false;
            // 保持前后行进判定，转向完毕即可向前移动
        }
    }
}