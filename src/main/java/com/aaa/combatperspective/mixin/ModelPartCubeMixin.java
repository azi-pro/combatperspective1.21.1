package com.aaa.combatperspective.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = ModelPart.Cube.class, priority = 200)
public class ModelPartCubeMixin {

  /**
   * 1.21.1 可用：修改玩家模型透明度
   * 目前禁用，等待更好的实现方案
   */
  @ModifyVariable(
          method = "compile(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",
          at = @At("HEAD"),
          index = 5
  )
  private int combatperspective$modifyModelOpacity(int color) {
    // 暂时禁用实体半透明，保留原样
    return color;
  }
}