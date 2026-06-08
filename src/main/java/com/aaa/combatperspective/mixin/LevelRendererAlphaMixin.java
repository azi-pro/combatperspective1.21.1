package com.aaa.combatperspective.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 第三人称后视角下，玩家头顶的方块渲染为半透明。
 */
@Mixin(value = LevelRenderer.class)
public class LevelRendererAlphaMixin {

    private static int currentOriginY = Integer.MIN_VALUE;
    private static RenderType currentRenderType;
    private static boolean shouldApplyAlpha = false;

    @Redirect(
            method = "renderSectionLayer",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;setupRenderState()V")
    )
    private void captureRenderType(RenderType type) {
        currentRenderType = type;
        type.setupRenderState();
    }

    @Redirect(
            method = "renderSectionLayer",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher$RenderSection;getOrigin()Lnet/minecraft/core/BlockPos;")
    )
    private BlockPos captureOrigin(SectionRenderDispatcher.RenderSection section) {
        BlockPos origin = section.getOrigin();
        currentOriginY = origin.getY();
        
        // 检查是否需要半透明
        Minecraft mc = Minecraft.getInstance();
        shouldApplyAlpha = checkShouldApplyAlpha(mc, currentOriginY);
        
        return origin;
    }

    @Redirect(
            method = "renderSectionLayer",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexBuffer;draw()V")
    )
    private void drawWithAlpha(VertexBuffer buffer) {
        // 正常绘制
        buffer.draw();

        // 如果需要应用半透明，且不是透明渲染层
        if (shouldApplyAlpha && currentRenderType != RenderType.translucent()) {
            applyAlphaBlend(buffer);
        }
    }

    private boolean checkShouldApplyAlpha(Minecraft mc, int originY) {
        if (mc.player == null) return false;
        if (!isThirdPersonBack(mc)) return false;

        Player player = mc.player;
        int headBlockY = (int) Math.floor(player.getY()) + 1;
        
        // 检查区块段是否包含头顶方块
        return originY <= headBlockY && headBlockY < originY + 16;
    }

    private boolean isThirdPersonBack(Minecraft mc) {
        return !mc.options.getCameraType().isFirstPerson()
                && !mc.options.getCameraType().isMirrored();
    }

    private void applyAlphaBlend(VertexBuffer buffer) {
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        
        // 20% 不透明度
        org.lwjgl.opengl.GL14.glBlendColor(1.0F, 1.0F, 1.0F, 0.2F);
        RenderSystem.blendFunc(
                org.lwjgl.opengl.GL14.GL_CONSTANT_ALPHA,
                org.lwjgl.opengl.GL14.GL_ONE_MINUS_CONSTANT_ALPHA
        );

        buffer.draw();

        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }
}
