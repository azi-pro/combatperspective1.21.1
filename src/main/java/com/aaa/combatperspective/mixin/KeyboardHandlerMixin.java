package com.aaa.combatperspective.mixin;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.aaa.combatperspective.CombatPerspective.LOGGER;
import static java.lang.Math.abs;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    private static boolean pressW, pressA, pressS, pressD;

    @Inject(method = "keyPress(JIIII)V", at = @At("HEAD"), cancellable = true)
    private void captureKeys(long window, int key, int scancode, int action, int mods, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // 正确写法：mc.options
        boolean isthirdPersonback = !mc.options.getCameraType().isFirstPerson()
                && !mc.options.getCameraType().isMirrored();

        boolean noScreen = mc.screen == null;

        boolean isWASD = key == GLFW.GLFW_KEY_W
                || key == GLFW.GLFW_KEY_A
                || key == GLFW.GLFW_KEY_S
                || key == GLFW.GLFW_KEY_D;

        // 第三人称背面 + 无界面 + 按下WASD → 屏蔽原版
        if (isthirdPersonback && noScreen && isWASD) {
            boolean down = action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT;

            switch (key) {
                case GLFW.GLFW_KEY_W -> pressW = down;
                case GLFW.GLFW_KEY_A -> pressA = down;
                case GLFW.GLFW_KEY_S -> pressS = down;
                case GLFW.GLFW_KEY_D -> pressD = down;
            }

            // 屏蔽原版 WASD 移动
            ci.cancel();
        }
    }
    @Inject(method = "tick", at = @At("TAIL"))
    private void handleWorldDirMove(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        boolean isthirdPersonback = !mc.options.getCameraType().isFirstPerson()
                && !mc.options.getCameraType().isMirrored();
        if (!isthirdPersonback || mc.screen != null) return;

        double speed = player.getAttribute(Attributes.MOVEMENT_SPEED).getValue() * 2;

        double movementspeed = speed;

        // 通过 Minecraft 实例获取真实 Camera，再强转为 CameraAccessor 读取 camerayaw
        net.minecraft.client.Camera camera = mc.gameRenderer.getMainCamera();
        float camerayaw = ((CameraAccessor) camera).getCameraYaw();

        // 判断是否疾跑（视线方向与按键方向夹角 < 45度）
        if (pressW || pressS || pressA || pressD) {
            double yaw = player.getYRot(); // 获取玩家的Y轴旋转角度
            if (pressW){
                camerayaw += 0;
            }
            if (pressA){
                camerayaw += 90;
            }
            if (pressS){
                camerayaw += 180;
            }
            if (pressD){
                camerayaw += 270;
            }
            LOGGER.info("camerayaw = {}", camerayaw);
            // 计算玩家当前按键方向和视线方向之间的夹角
            double diff = Math.abs(yaw - camerayaw) % 360;
            double angle = diff > 180 ? 360 - diff : diff;

            // 如果按键方向与视线方向夹角小于45度（即 π/4 弧度），让玩家疾跑
            if (angle < Math.toRadians(45)) {
                movementspeed = speed * 2.0f; // 疾跑速度是普通速度的两倍（可以根据需要调整）
            }
        }

        // 计算最终方向矢量（与按键方向一致）
        double movementX = 0.0;
        double movementZ = 0.0;

        if (pressW) movementZ = 1.0;
        if (pressS) movementZ = -1.0;
        if (pressA) movementX = -1.0;
        if (pressD) movementX = 1.0;

        // 标准化方向值（如 W/S/A/D 同时被按下）
        // 这里我们假设你不处理多个方向同时按下的情况，或你希望按键叠加

        Vec3 motion = new Vec3(
                movementX * movementspeed,
                player.getDeltaMovement().y,
                movementZ * movementspeed
        );

        player.setDeltaMovement(motion);
    }
}
