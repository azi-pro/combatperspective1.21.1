package com.aaa.combatperspective.data;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * 投射物轨迹数据存储
 * 用于在 Mixin 之间共享抛物线计算结果
 */
public class ProjectileStore {
    
    // ==================== 配置参数 ====================
    
    /** 是否启用抛物线预测 */
    private static boolean enabled = true;
    
    /** 预测点数量 */
    private static int predictionPoints = 60;
    
    /** 预测时间步长（tick） */
    private static float timeStep = 1.0f;
    
    /** 最大预测距离（格） */
    private static float maxDistance = 300.0f;
    
    /** 轨迹点列表 */
    private static final List<TrajectoryPoint> trajectoryPoints = new ArrayList<>();
    
    /** 落点位置 */
    private static Vec3 landingPos = null;
    
    /** 落点方块位置 */
    private static BlockPos landingBlockPos = null;
    
    /** 轨迹是否有效（正在拉弓） */
    private static boolean isCalculating = false;
    
    /** 投射物类型 */
    private static ProjectileType projectileType = ProjectileType.ARROW;
    
    // ==================== 投射物类型枚举 ====================
    
    public enum ProjectileType {
        ARROW(3.0, 0.05f, 0.99f),           // 弓箭
        TRIDENT(2.5, 0.05f, 0.99f),          // 三叉戟
        SNOWBALL(1.5, 0.05f, 0.98f),        // 雪球
        EGG(1.5, 0.05f, 0.98f),             // 鸡蛋
        ENDER_PEARL(1.5, 0.05f, 0.98f),    // 末影珍珠
        POTION(0.5, 0.05f, 0.95f),         // 药水（溅射）
        FISHING_BOBBER(0.0, 0.03f, 0.92f); // 钓鱼浮标
        
        public final double baseVelocity;   // 基础速度
        public final float gravity;          // 重力加速度
        public final float drag;             // 空气阻力
        
        ProjectileType(double velocity, float gravity, float drag) {
            this.baseVelocity = velocity;
            this.gravity = gravity;
            this.drag = drag;
        }
    }
    
    // ==================== 轨迹点数据结构 ====================
    
    public static class TrajectoryPoint {
        public final Vec3 position;
        public final float time;
        public final boolean isLanding;
        
        public TrajectoryPoint(Vec3 position, float time, boolean isLanding) {
            this.position = position;
            this.time = time;
            this.isLanding = isLanding;
        }
    }
    
    // ==================== Getter/Setter ====================
    
    public static boolean isEnabled() { return enabled; }
    public static void setEnabled(boolean v) { enabled = v; }
    
    public static int getPredictionPoints() { return predictionPoints; }
    public static void setPredictionPoints(int v) { predictionPoints = Math.max(10, Math.min(200, v)); }
    
    public static float getMaxDistance() { return maxDistance; }
    public static void setMaxDistance(float v) { maxDistance = v; }
    
    public static List<TrajectoryPoint> getTrajectoryPoints() { return trajectoryPoints; }
    
    public static Vec3 getLandingPos() { return landingPos; }
    public static BlockPos getLandingBlockPos() { return landingBlockPos; }
    
    public static boolean isCalculating() { return isCalculating; }
    public static void setCalculating(boolean v) { isCalculating = v; }
    
    public static ProjectileType getProjectileType() { return projectileType; }
    public static void setProjectileType(ProjectileType v) { projectileType = v; }
    
    // ==================== 数据更新 ====================
    
    /**
     * 更新轨迹数据
     */
    public static void update(Vec3 landing, BlockPos blockPos, List<TrajectoryPoint> points) {
        landingPos = landing;
        landingBlockPos = blockPos;
        trajectoryPoints.clear();
        trajectoryPoints.addAll(points);
        isCalculating = true;
    }
    
    /**
     * 清除轨迹数据
     */
    public static void clear() {
        landingPos = null;
        landingBlockPos = null;
        trajectoryPoints.clear();
        isCalculating = false;
    }
    
    /**
     * 根据手中物品判断投射物类型
     */
    public static ProjectileType detectProjectileType(LocalPlayer player) {
        if (player == null) return ProjectileType.ARROW;
        
        var heldItem = player.getMainHandItem();
        var item = heldItem.getItem();
        
        String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).toString();
        
        if (itemId.contains("bow")) {
            return ProjectileType.ARROW;
        } else if (itemId.contains("trident")) {
            return ProjectileType.TRIDENT;
        } else if (itemId.contains("snowball")) {
            return ProjectileType.SNOWBALL;
        } else if (itemId.contains("egg")) {
            return ProjectileType.EGG;
        } else if (itemId.contains("ender_pearl")) {
            return ProjectileType.ENDER_PEARL;
        } else if (itemId.contains("potion") || itemId.contains("splash_potion")) {
            return ProjectileType.POTION;
        } else if (itemId.contains("fishing_rod") || itemId.contains("fishing_bobber")) {
            return ProjectileType.FISHING_BOBBER;
        }
        
        return ProjectileType.ARROW;
    }
}
