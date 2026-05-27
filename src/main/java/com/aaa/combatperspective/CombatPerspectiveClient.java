package com.aaa.combatperspective;

import net.minecraft.client.Minecraft;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = CombatPerspective.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = CombatPerspective.MOD_ID, value = Dist.CLIENT)
public class CombatPerspectiveClient {

    public CombatPerspectiveClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        CombatPerspective.LOGGER.info("客户端加载成功");
    }

    // 缓存最终FOV角度（单位：度），volatile保证跨线程可见
    private static volatile float currentFov = 70.0F;
    // 客户端初始化时调用一次即可完成注册
    public static void init() {
        NeoForge.EVENT_BUS.register(CombatPerspectiveClient.class);
    }
    // 对外读取入口，直接调用即可拿到当前最新FOV
    public static float getCurrentFov() {
        return currentFov;
    }
    // 纯监听存值，无额外逻辑
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onFovCompute(ComputeFovModifierEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options == null || mc.options.fov() == null) return;
        // 完全对应当前原版源码的FOV计算逻辑，数值和渲染用值完全一致
        currentFov = mc.options.fov().get().intValue() * event.getNewFovModifier();
    }
}