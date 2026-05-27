package com.aaa.combatperspective.item;

import com.aaa.combatperspective.CombatPerspective;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CombatPerspective.MOD_ID);
    public static final Supplier<CreativeModeTab> CP_TAB =
            CREATIVE_MODE_TABS.register("combatperspective_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.Iron_LongSword.get()))
                    .title(Component.translatable("itemGroup.combatperspective_tab"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.Iron_LongSword);
                    }).build());

    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TABS.register(eventBus);
    }
}