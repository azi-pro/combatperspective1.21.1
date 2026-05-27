package com.aaa.combatperspective;

import com.aaa.combatperspective.item.ModCreativeModeTabs;
import com.aaa.combatperspective.item.ModItems;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.aaa.combatperspective.item.ModCreativeModeTabs.CP_TAB;

// 这里的值必须与 META-INF/neoforge.mods.toml 文件中的一项匹配
@Mod(CombatPerspective.MOD_ID)
public class CombatPerspective {
    // 在统一的公共位置定义 mod id，方便所有内容引用
    public static final String MOD_ID = "combatperspective";
    // 直接引用一个 slf4j 日志器
    public static final Logger LOGGER = LogUtils.getLogger();

    // 模组类的构造方法，是模组加载时运行的第一段代码
    // FML 会自动识别某些参数类型（如 IEventBus 或 ModContainer）并自动传入
    public CombatPerspective(IEventBus modEventBus, ModContainer modContainer) {
        // 注册 commonSetup 方法，用于模组加载时执行
        modEventBus.addListener(this::commonSetup);

        // 将创造模式标签页延迟注册器注册到模组事件总线，让标签页得以注册
        ModItems.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);

        // 注册自身以接收服务器及其他我们关心的事件
        // 注意：仅当你希望 *本类* (CombatPerspective) 直接响应事件时才需要这行
        // 如果本类中没有像 onServerStarting() 这样带 @SubscribeEvent 注解的函数，不要加这行
        NeoForge.EVENT_BUS.register(this);

        // 将物品注册到创造模式标签页
        modEventBus.addListener(this::addCreative);

        // 注册模组的配置文件，让 FML 可以创建并加载我们的配置文件
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // 一些通用的初始化代码
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {// 战斗用品
            event.accept(ModItems.Iron_LongSword);
        }
        if (event.getTabKey() == CP_TAB) {// 自定义模组标签页
            event.accept(ModItems.Iron_LongSword);
        }
    }

    // 你可以使用 SubscribeEvent，让事件总线自动调用方法
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // 服务器启动时执行一些操作
        LOGGER.info("HELLO from server starting");
    }
}
