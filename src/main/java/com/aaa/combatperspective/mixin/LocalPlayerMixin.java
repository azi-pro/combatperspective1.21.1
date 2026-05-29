package com.aaa.combatperspective.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    /** 禁用侧移：进入第三人称后视角时屏蔽 input.left/right */
    @Inject(method = "tick", at = @At("HEAD"))
    private void turnThenMove(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer self = (LocalPlayer) (Object) this;
        boolean thirdPerson = !mc.options.getCameraType().isFirstPerson()
                && !mc.options.getCameraType().isMirrored();
        if (thirdPerson && mc.screen == null) {
            self.input.left = false;
            self.input.right = false;
        }
    }

    /**
     * 覆盖原版疾跑判定：去掉 forwardImpulse > 0.8 的条件，
     * 改为「按键方向与玩家视线夹角 < 45°」
     */
    @Inject(method = "aiStep", at = @At("TAIL"))
    private void overrideSprint(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();

        // 仅战斗视角（第三人称后视角）生效
        if (mc.options.getCameraType().isFirstPerson()
                || mc.options.getCameraType().isMirrored()) {
            return;
        }

        LocalPlayer self = (LocalPlayer) (Object) this;

        // ===== 读按键 =====
        boolean w = mc.options.keyUp.isDown();
        boolean a = mc.options.keyLeft.isDown();
        boolean s = mc.options.keyDown.isDown();
        boolean d = mc.options.keyRight.isDown();
        boolean anyKey = w || a || s || d;

        // 没按任何方向键 → 停跑
        if (!anyKey) {
            self.setSprinting(false);
            return;
        }

        // 原版停跑条件（碰撞/水中浮起）保持不变
        boolean blockStop = self.horizontalCollision && !self.minorHorizontalCollision;
        boolean waterStop = self.isInWater() && !self.isUnderWater();
        if (blockStop || waterStop) {
            self.setSprinting(false);
            return;
        }

        // ===== 计算移动方向（世界坐标，对齐 EntityMixin 的 yaw=180°） =====
        // W→北(-Z)  S→南(+Z)  A→西(-X)  D→东(+X)
        double moveX = (a ? -1 : 0) + (d ? 1 : 0);
        double moveZ = (w ? -1 : 0) + (s ? 1 : 0);

        // 世界方向 → 角度（atan2(x, z)，因为 Minecraft X=东 → -X=西）
        float moveYaw = (float) Math.toDegrees(Math.atan2(-moveX, moveZ));
        if (moveYaw < 0) moveYaw += 360;

        // 玩家模型朝向
        float playerYaw = self.getYRot() % 360;
        if (playerYaw < 0) playerYaw += 360;

        // 夹角
        float diff = Math.abs(moveYaw - playerYaw) % 360;
        float angle = diff > 180 ? 360 - diff : diff;

        // ===== 条件判定（去掉原版的 forwardImpulse 检查） =====
        // hasEnoughFoodToStartSprinting() 是 private，内联其逻辑
        boolean hasEnoughFood = self.isPassenger()
                || (float) self.getFoodData().getFoodLevel() > 6.0F
                || self.getAbilities().mayfly;

        boolean shouldSprint = angle < 45 && hasEnoughFood && !self.isUsingItem();

        self.setSprinting(shouldSprint);
    }
}