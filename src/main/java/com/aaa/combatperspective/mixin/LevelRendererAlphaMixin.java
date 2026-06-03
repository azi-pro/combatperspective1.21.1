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
 * 第三人称后视角下，玩家头顶的方块渲染为半透明（可从相机看到角色）。
 */
@Mixin(value = LevelRenderer.class)
public class LevelRendererAlphaMixin {

    private static int currentOriginY = Integer.MIN_VALUE;
    private static RenderType currentRenderType;

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
        return origin;
    }

    @Redirect(
            method = "renderSectionLayer",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexBuffer;draw()V")
    )
    private void drawWithAlpha(VertexBuffer buffer) {
        Minecraft mc = Minecraft.getInstance();

        // 正常绘制
        buffer.draw();

        // 检查是否需要半透明处理
        if (!shouldApplyAlpha(mc, currentOriginY)) {
            return;
        }

        // 跳过透明渲染层
        if (currentRenderType == RenderType.translucent() || currentRenderType == RenderType.tripwire()) {
            return;
        }

        // 应用半透明
        applyAlphaBlend(buffer);
    }

    private boolean shouldApplyAlpha(Minecraft mc, int originY) {
        if (mc.player == null) return false;
        if (!isThirdPersonBack(mc)) return false;

        Player player = mc.player;
        // 头顶方块的 Y 坐标（玩家 Y + 1）
        int headBlockY = (int) Math.floor(player.getY()) + 1;

        // 检查区块段是否包含头顶方块
        // 区块段范围: [originY, originY + 16)
        return originY <= headBlockY && headBlockY < originY + 16;
    }

    private boolean isThirdPersonBack(Minecraft mc) {
        // 只要是第三人称后视角就启用，不检查 screen
        return !mc.options.getCameraType().isFirstPerson()
                && !mc.options.getCameraType().isMirrored();
    }

    private void applyAlphaBlend(VertexBuffer buffer) {
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // 0.15F = 15% 不透明度（85% 透明）
        org.lwjgl.opengl.GL14.glBlendColor(1.0F, 1.0F, 1.0F, 0.15F);
        RenderSystem.blendFunc(
                org.lwjgl.opengl.GL14.GL_CONSTANT_ALPHA,
                org.lwjgl.opengl.GL14.GL_ONE_MINUS_CONSTANT_ALPHA
        );

        buffer.draw();

        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }
}
