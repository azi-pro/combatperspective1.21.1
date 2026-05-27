package com.aaa.combatperspective.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = ModelPart.Cube.class, priority = 200)
public class ModelPartCubeMixin {

  /**
   * 1.21.1 可用：修改玩家模型透明度（第三人称半透明）
   */
  @ModifyVariable(
          method = "compile(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",
          at = @At("HEAD"),
          index = 5
  )
  private int combatperspective$modifyModelOpacity(int color) {
    Minecraft mc = Minecraft.getInstance();

    // 仅第三人称下生效
    if (!mc.options.getCameraType().isFirstPerson() && mc.player != null) {

      // 1.21.1 原版位运算解析透明度（无 ARGB 类）
      int originalAlpha = (color >> 24) & 0xFF;
      float alpha = originalAlpha / 255.0F;

      // 设置半透明（0.4 = 40% 不透明度）
      float newAlpha = Math.min(alpha, 0.4F);
      int newAlphaInt = (int) (newAlpha * 255.0F);

      // 重新组合颜色
      return (newAlphaInt << 24) | (color & 0xFFFFFF);
    }

    return color;
  }
}