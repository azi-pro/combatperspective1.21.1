package com.aaa.combatperspective.item;

import com.aaa.combatperspective.CombatPerspective;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;


// ============================================================
enum CPWeaponTier implements Tier
{
    LONG_SWORD(243, 6.0F, 3.0F, 15);

    private final int uses;
    private final float speed;
    private final float attackDamage;
    private final int enchantmentValue;

    CPWeaponTier(int uses, float speed, float attackDamage, int enchantmentValue) {
        this.uses = uses;
        this.speed = speed;
        this.attackDamage = attackDamage;
        this.enchantmentValue = enchantmentValue;
    }

    @Override public int getUses()
            {return uses;}

    @Override public float getSpeed()
            {return speed;}

    @Override public float getAttackDamageBonus()
            {return attackDamage;}

    @Override public TagKey<Block> getIncorrectBlocksForDrops()
            {return BlockTags.INCORRECT_FOR_IRON_TOOL;}

    @Override public int getEnchantmentValue()
            {return enchantmentValue;}

    @Override public Ingredient getRepairIngredient()
            {return Ingredient.of(Items.IRON_INGOT);}
}
// ============================================================




public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(CombatPerspective.MOD_ID);


    public static final DeferredItem<Item> Iron_LongSword = ITEMS.register("weapon/iron_longsword",
        () -> new SwordItem(CPWeaponTier.LONG_SWORD, new Item.Properties()
                .attributes(SwordItem.createAttributes(CPWeaponTier.LONG_SWORD, 1, -2.1F))
                .stacksTo(1)
        )
    );




    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
