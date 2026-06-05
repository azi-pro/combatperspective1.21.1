// =============================================================================
// ModItems.java - 物品注册类
// =============================================================================
// 包声明
package com.aaa.combatperspective.item;

// 导入模组主类，用于获取 MOD_ID
import com.aaa.combatperspective.CombatPerspective;

// 导入基础物品类
import net.minecraft.world.item.*;

// 导入剑物品类

// 导入事件总线接口
import net.neoforged.bus.api.IEventBus;

// 导入延迟物品持有者
import net.neoforged.neoforge.registries.DeferredItem;

// 导入延迟注册器
import net.neoforged.neoforge.registries.DeferredRegister;


// =============================================================================
// 物品注册类
// 使用延迟注册模式，在游戏初始化时注册所有物品
// =============================================================================
public class ModItems {
    // =========================================================================
    // 创建延迟物品注册器
    // DeferredRegister.createItems() 创建物品类型的延迟注册器
    // 参数为模组 ID，用于所有注册物品的命名空间
    // =========================================================================
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(CombatPerspective.MOD_ID);

    // =========================================================================
    // 注册战斗长剑
    // 使用 CPSword 以支持高附魔值
    // -2.1F = 攻击速度（原版铁剑是 -2.1）
    // =========================================================================
    public static final DeferredItem<Item> Iron_LongSword = ITEMS.register("weapon/iron_longsword",
        () -> new CPSword(CPTier.LONG_SWORD, new Item.Properties()
                .attributes(SwordItem.createAttributes(CPTier.LONG_SWORD, 1, -2.1F))
                .stacksTo(1)
        )
    );

    // =========================================================================
    // 注册方法：将注册器绑定到事件总线
    // param eventBus 模组事件总线
    // =========================================================================
    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}