package com.aaa.combatperspective.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.checkerframework.common.aliasing.qual.Unique;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin {
    @Unique
    private float camerayaw;  // 世界水平角度固定(北朝(朝北

    @Inject(method = "setup",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;move(FFF)V"),
            cancellable = true)
    private void onlyThirdPersonFixedCamera
            (BlockGetter level,
            Entity entity,
            boolean detached,
            boolean mirror,
            float partialTick,
            CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.options.getCameraType().isFirstPerson()) {return;}

        // 只对玩家生效
        if (!(entity instanceof Player player)) {return;}

        Camera camera = (Camera) (Object) this;

        Vec3 eye = player.getEyePosition(partialTick);
        double x = eye.x;
        double y = eye.y + 6;
        double z = eye.z + 4;

        float pitch = 60;
        this.camerayaw = 180;

        ((CameraInvoker) camera).invokeSetPosition(new Vec3(x, y, z));
        ((CameraInvoker) camera).invokeSetRotation(camerayaw, pitch);

        ci.cancel();
    }

}