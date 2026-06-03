package com.aaa.combatperspective.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {

    private static final ResourceLocation CROSSHAIR = ResourceLocation.withDefaultNamespace("hud/crosshair");

    @Inject(method = "render", at = @At("TAIL"))
    private void renderCrosshairInThirdPerson(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options == null || mc.player == null) return;

        boolean thirdPerson = !mc.options.getCameraType().isFirstPerson()
                && !mc.options.getCameraType().isMirrored();

        System.out.println("GuiMixin render TAIL: thirdPerson=" + thirdPerson + " screen=" + (mc.screen != null) + " hideGui=" + mc.options.hideGui);
        if (thirdPerson && mc.screen == null && !mc.options.hideGui) {
            System.out.println("DRAWING CROSSHAIR");
            double[] mx = new double[1], my = new double[1];
            org.lwjgl.glfw.GLFW.glfwGetCursorPos(mc.getWindow().getWindow(), mx, my);
            System.out.println("  cursor=(" + mx[0] + "," + my[0] + ") win=" + mc.getWindow().getWidth() + "x" + mc.getWindow().getHeight());
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                    com.mojang.blaze3d.platform.GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
                    com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
                    com.mojang.blaze3d.platform.GlStateManager.SourceFactor.ONE,
                    com.mojang.blaze3d.platform.GlStateManager.DestFactor.ZERO
            );
            int size = 15;
            double guiScale = mc.getWindow().getGuiScale();
            int cx = (int) (mx[0] / guiScale) - size / 2;
            int cy = (int) (my[0] / guiScale) - size / 2;
            System.out.println("  blitSprite at (" + cx + "," + cy + ")");
            graphics.blitSprite(CROSSHAIR, cx, cy, size, size);
        }
    }
}
