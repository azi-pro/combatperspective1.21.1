package com.aaa.combatperspective.data;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.projectile.ProjectileUtil;

/**
 * 投射物物理计算
 * 使用 Minecraft 1.21 真实的弓箭物理参数
 */
public class ProjectilePhysics {
    
    // =========================================================================
    // Minecraft 1.21 弓箭物理参数 (参考 Arrow 实体)
    // =========================================================================
    
    /** 满蓄力弓箭初速度 (格/tick) */
    private static final double ARROW_MAX_POWER = 3.0;
    
    /** 重力加速度 - Minecraft Entity 的默认 gravity 是 0.08 */
    private static final double GRAVITY = 0.05;
    
    /** 空气阻力 - Minecraft 弓箭几乎没有阻力，但为了预测准确性加一点 */
    private static final double ARROW_DRAG = 1.0;  // 无阻力，按原版
    
    /** 最大飞行距离限制 */
    private static final double MAX_ARROW_DISTANCE = 100.0;
    
    /**
     * 计算投射物轨迹
     * @param player 玩家
     * @param bowPower 弓的蓄力 (0.0 - 1.0)
     * @return 轨迹点列表
     */
    public static java.util.List<ProjectileStore.TrajectoryPoint> calculateTrajectory(
            LocalPlayer player, float bowPower) {
        
        java.util.List<ProjectileStore.TrajectoryPoint> points = new java.util.ArrayList<>();
        
        if (player == null) return points;
        
        // 获取投射物类型
        ProjectileStore.ProjectileType type = ProjectileStore.getProjectileType();
        
        // 获取物理参数
        double maxPower;
        double gravity;
        double drag;
        
        switch (type) {
            case ARROW -> {
                // 弓箭：使用 Minecraft 真实物理
                maxPower = ARROW_MAX_POWER * bowPower;
                gravity = GRAVITY;
                drag = ARROW_DRAG;
            }
            case TRIDENT -> {
                // 三叉戟
                maxPower = 2.5 * bowPower;
                gravity = 0.006;
                drag = 1.0;
            }
            case SNOWBALL, EGG, ENDER_PEARL -> {
                // 雪球/鸡蛋/末影珍珠
                maxPower = 1.5 * bowPower;
                gravity = 0.03;
                drag = 0.98;
            }
            case POTION -> {
                // 药水
                maxPower = 0.5 * bowPower;
                gravity = 0.04;
                drag = 0.95;
            }
            default -> {
                maxPower = 1.0 * bowPower;
                gravity = 0.05;
                drag = 0.99;
            }
        }
        
        // 获取玩家眼睛位置和朝向
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getViewVector(1.0F);
        
        // 初始速度 = 方向 * 功率
        double vx = lookVec.x * maxPower;
        double vy = lookVec.y * maxPower;
        double vz = lookVec.z * maxPower;
        
        // 弓箭发射位置：从眼睛向前偏移一点（约0.5格）
        // 这样轨迹起点更接近实际弓箭位置
        double launchOffset = 0.3;
        double x = eyePos.x + lookVec.x * launchOffset;
        double y = eyePos.y + lookVec.y * launchOffset;
        double z = eyePos.z + lookVec.z * launchOffset;
        
        // 上一帧位置
        Vec3 lastPos = eyePos;
        
        Level level = player.level();
        int maxPoints = ProjectileStore.getPredictionPoints();
        float maxDist = (float) ProjectileStore.getMaxDistance();
        
        // 逐 tick 模拟
        for (int i = 0; i < maxPoints; i++) {
            // 添加当前轨迹点
            Vec3 pos = new Vec3(x, y, z);
            points.add(new ProjectileStore.TrajectoryPoint(pos, i, false));
            
            // 上一帧位置
            lastPos = pos;
            
            // ===============================================================
            // Minecraft 物理更新
            // ===============================================================
            
            // 应用重力（每 tick）
            vy -= gravity;
            
            // 应用拖曳
            if (drag < 1.0) {
                vx *= drag;
                vz *= drag;
            }
            
            // 更新位置
            x += vx;
            y += vy;
            z += vz;
            
            // 新位置
            Vec3 newPos = new Vec3(x, y, z);
            
            // 碰撞检测
            HitResult hit = checkCollision(level, lastPos, newPos, player);
            
            if (hit != null) {
                // 找到落点
                Vec3 hitPos = hit.getLocation();
                points.add(new ProjectileStore.TrajectoryPoint(hitPos, i + 1, true));
                
                net.minecraft.core.BlockPos blockPos;
                if (hit instanceof BlockHitResult blockHit) {
                    blockPos = blockHit.getBlockPos();
                } else {
                    blockPos = new net.minecraft.core.BlockPos((int)hitPos.x, (int)hitPos.y, (int)hitPos.z);
                }
                
                ProjectileStore.update(hitPos, blockPos, points);
                return points;
            }
            
            // 检查距离限制（使用更真实的限制）
            double currentDist = Math.sqrt(
                (x - eyePos.x) * (x - eyePos.x) +
                (y - eyePos.y) * (y - eyePos.y) +
                (z - eyePos.z) * (z - eyePos.z)
            );
            
            // 弓箭有最大飞行距离限制
            if (type == ProjectileStore.ProjectileType.ARROW && currentDist > MAX_ARROW_DISTANCE) {
                ProjectileStore.update(null, null, points);
                return points;
            }
            
            // 通用最大距离
            if (eyePos.distanceTo(newPos) > maxDist) {
                ProjectileStore.update(null, null, points);
                return points;
            }
            

            
            // 低于世界底部
            if (y < -64) {
                ProjectileStore.update(new Vec3(x, -64, z), 
                    new net.minecraft.core.BlockPos((int)x, -64, (int)z), points);
                return points;
            }
        }
        
        // 超时
        ProjectileStore.update(null, null, points);
        return points;
    }
    
    /**
     * 检测碰撞 - 使用射线检测路径上的碰撞
     * 用上一帧位置和当前帧位置做射线检测
     */
    private static HitResult checkCollision(Level level, Vec3 startPos, Vec3 endPos, Entity exclude) {
        
        // 方块碰撞检测 - 射线从 startPos 到 endPos
        ClipContext ctx = new ClipContext(
            startPos, endPos,
            ClipContext.Block.OUTLINE,
            ClipContext.Fluid.NONE,
            exclude
        );
        
        BlockHitResult blockHit = level.clip(ctx);
        
        // 实体碰撞检测
        AABB sweepBox = new AABB(startPos, endPos).inflate(0.2);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
            level, exclude,
            startPos, endPos,
            sweepBox,
            e -> !e.isSpectator() && e.isPickable() && e != exclude
        );
        
        // 比较距离
        double blockDist = blockHit.getType() == HitResult.Type.BLOCK
            ? startPos.distanceToSqr(blockHit.getLocation()) : Double.MAX_VALUE;
        double entityDist = entityHit != null
            ? startPos.distanceToSqr(entityHit.getLocation()) : Double.MAX_VALUE;
        
        if (entityDist < blockDist && entityHit != null) {
            return entityHit;
        } else if (blockHit.getType() == HitResult.Type.BLOCK) {
            return blockHit;
        }
        
        return null;
    }
    
    /**
     * 根据弓的蓄力计算速度
     * Minecraft 1.21 弓箭蓄力公式（非线性）
     */
    public static float calculateBowPower(LocalPlayer player) {
        if (player == null) return 0.0f;
        
        // 检测是否在使用弓/弩
        if (!player.isUsingItem()) {
            return 0.0f;
        }
        
        var useItem = player.getUseItem();
        var item = useItem.getItem();
        String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).toString();
        
        // 只对弓、弩显示轨迹
        if (!itemId.contains("bow") && !itemId.contains("crossbow")) {
            return 0.0f;
        }
        
        // ===============================================================
        // 计算蓄力比例
        // getUseItemDuration() = 总共可使用时间 (如弓最大 20 ticks)
        // getUseItemRemainingTicks() = 剩余时间
        // 已使用时间 = 总时间 - 剩余时间
        // ===============================================================
        // ===============================================================
        // Minecraft 蓄力机制：
        // - getUseItemRemainingTicks() 从物品最大使用时间递减
        // - 弓的最大使用时间是 72000 ticks，但蓄力限制在约 20 ticks
        // - remaining 越小，蓄力越满
        // ===============================================================
        final int MAX_CHARGE_TICKS = 20;
        
        // 剩余时间 (从 72000 递减)
        int remaining = player.getUseItemRemainingTicks();
        
        // 蓄力时间 = (最大蓄力时间) - remaining
        // 由于弓的最大使用时间是 72000，而满蓄力是 20 ticks
        // 所以已蓄力时间 = Math.min(20, 72000 - remaining)
        int chargeTicks = 72000 - remaining;
        
        // 限制最大蓄力时间为 20 ticks
        chargeTicks = Math.min(chargeTicks, MAX_CHARGE_TICKS);
        
        // 确保不为负数
        if (chargeTicks < 0) chargeTicks = 0;
        
        // 蓄力比例 (0.0 - 1.0)
        float chargeRatio = (float) chargeTicks / MAX_CHARGE_TICKS;
        
        // ===============================================================
        // Minecraft 非线性蓄力公式
        // 来源: net.minecraft.world.item.BowItem
        // power = (chargeRatio^2 + chargeRatio*2) / 3
        // 这使得初期蓄力更快，后期较慢
        // ===============================================================
        float power = (chargeRatio * chargeRatio + chargeRatio * 2.0f) / 3.0f;
        
        return Math.max(0.0f, Math.min(1.0f, power));
    }
}
