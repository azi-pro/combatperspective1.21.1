package com.aaa.combatperspective.mixin;

import com.aaa.combatperspective.data.ProjectilePhysics;
import com.aaa.combatperspective.data.ProjectileStore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 投射物轨迹计算 Mixin
 * 在玩家拉弓时计算并存储抛物线轨迹
 */
@Mixin(LocalPlayer.class)
public abstract class ProjectileTrajectoryMixin {
    
    /**
     * 在 tick 时检测拉弓状态并计算轨迹
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void calculateProjectileTrajectory(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        
        // 检查是否启用
        if (!ProjectileStore.isEnabled()) {
            ProjectileStore.clear();
            return;
        }
        
        // 检查战斗视角
        if (!isCombatPerspective(mc)) {
            ProjectileStore.clear();
            return;
        }
        
        LocalPlayer player = (LocalPlayer) (Object) this;
        
        // 检测是否在使用弓/弩等投射物
        if (!player.isUsingItem()) {
            ProjectileStore.clear();
            return;
        }
        
        var useItem = player.getUseItem();
        var item = useItem.getItem();
        String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).toString();
        
        // 只对弓、弩、三叉戟等投射物显示轨迹
        boolean isProjectileItem = itemId.contains("bow") 
            || itemId.contains("crossbow") 
            || itemId.contains("trident");
        
        if (!isProjectileItem) {
            ProjectileStore.clear();
            return;
        }
        
        // 检测投射物类型
        ProjectileStore.ProjectileType type = ProjectileStore.detectProjectileType(player);
        ProjectileStore.setProjectileType(type);
        
        // 计算弓的蓄力
        float bowPower = ProjectilePhysics.calculateBowPower(player);
        
        // 只有蓄力到一定程度才显示轨迹
        if (bowPower < 0.1f) {
            ProjectileStore.clear();
            return;
        }
        
        // 计算抛物线轨迹
        ProjectilePhysics.calculateTrajectory(player, bowPower);
    }
    
    /**
     * 检查是否处于战斗视角状态
     */
    private boolean isCombatPerspective(Minecraft mc) {
        if (mc.player == null) return false;
        if (mc.options.getCameraType().isFirstPerson()) return false;
        if (mc.options.getCameraType().isMirrored()) return false;
        if (mc.screen != null) return false;
        return true;
    }
}
