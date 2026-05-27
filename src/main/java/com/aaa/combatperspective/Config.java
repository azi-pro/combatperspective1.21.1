package com.aaa.combatperspective;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // 你原来的配置
    public static final ModConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
            .comment("Whether to log the dirt block on common setup")
            .define("logDirtBlock", true);

    public static final ModConfigSpec.IntValue MAGIC_NUMBER = BUILDER
            .comment("A magic number")
            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
            .comment("What you want the introduction message to be for the magic number")
            .define("magicNumberIntroduction", "The magic number is... ");

    public static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
            .comment("A list of items to log on common setup.")
            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), () -> "", Config::validateItemName);

    // ==========================================
    // 你的战斗视角配置（放进你自带的配置里）
    // ==========================================
    public static final ModConfigSpec.BooleanValue ENABLE_REACH_CAMERA = BUILDER
            .comment("启用特殊视角：摄像机朝向最远触及点")
            .define("enableReachCamera", false);

    public static final ModConfigSpec.DoubleValue REACH_DISTANCE = BUILDER
            .comment("最远触及距离")
            .defineInRange("reachDistance", 6.0D, 1.0D, 20.0D);

    public static final ModConfigSpec.DoubleValue CAMERA_DISTANCE = BUILDER
            .comment("相机距离玩家")
            .defineInRange("cameraDistance", 2.0D, 0.0D, 10.0D);

    public static final ModConfigSpec.DoubleValue CAMERA_HEIGHT = BUILDER
            .comment("相机高度")
            .defineInRange("cameraHeight", 0.7D, -5.0D, 5.0D);

    public static final ModConfigSpec.DoubleValue CAMERA_SIDE = BUILDER
            .comment("相机左右偏移")
            .defineInRange("cameraSide", 0.0D, -5.0D, 5.0D);

    static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }

    @EventBusSubscriber(modid = CombatPerspective.MOD_ID)
    public static class ConfigEvents {
        @SubscribeEvent
        static void onLoad(final ModConfigEvent.Loading configEvent) {}
    }
}