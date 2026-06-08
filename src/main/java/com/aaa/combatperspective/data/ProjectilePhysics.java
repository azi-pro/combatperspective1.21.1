package com.aaa.combatperspective.data;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.projectile.ProjectileUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 投射物物理计算
 * 计算弓箭等投射物的抛物线轨迹和落点
 */
public class ProjectilePhysics {
    
    /** Minecraft 物理常量 */
    private static final double GRAVITY = 0.05;      // Minecraft 重力加速度
    private static final double ARROW_GRAVITY = 0.05;
    private static final double ARROW_DRAG = 0.99;
    
    /**
     * 计算投射物轨迹
     * @param player 玩家
     * @param bowPower 弓的蓄力 (0.0 - 1.0)
     * @return 轨迹点列表
     */
    public static List<ProjectileStore.TrajectoryPoint> calculateTrajectory(
            LocalPlayer player, float bowPower) {
        
        List<ProjectileStore.TrajectoryPoint> points = new ArrayList<>();
        
        if (player == null) return points;
        
        // 获取投射物类型和物理参数
        ProjectileStore.ProjectileType type = ProjectileStore.getProjectileType();
        double velocity = type.baseVelocity * bowPower;
        float gravity = type.gravity;
        float drag = type.drag;
        
        // 获取玩家眼睛位置和朝向
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        
        // 初始速度
        double vx = lookVec.x * velocity;
        double vy = lookVec.y * velocity;
        double vz = lookVec.z * velocity;
        
        // 当前位置
        double x = eyePos.x;
        double y = eyePos.y;
        double z = eyePos.z;
        
        // 时间
        float time = 0f;
        
        // 上一帧位置（用于碰撞检测）
        Vec3 lastPos = new Vec3(x, y, z);
        
        Level level = player.level();
        int maxPoints = ProjectileStore.getPredictionPoints();
        float maxDist = (float) ProjectileStore.getMaxDistance();
        
        // 步进模拟
        for (int i = 0; i < maxPoints; i++) {
            // 添加轨迹点
            Vec3 pos = new Vec3(x, y, z);
            points.add(new ProjectileStore.TrajectoryPoint(pos, time, false));
            
            // 保存当前位置
            lastPos = pos;
            
            // 物理更新
            vy -= gravity;          // 重力
            vx *= drag;             // 空气阻力
            vz *= drag;
            
            x += vx;
            y += vy;
            z += vz;
            
            time += 1f / 20f; // 1 tick
            
            // 新位置
            Vec3 newPos = new Vec3(x, y, z);
            
            // 碰撞检测
            HitResult hit = checkCollision(level, lastPos, newPos, player);
            
            if (hit != null) {
                // 找到落点
                Vec3 hitPos = hit.getLocation();
                
                // 添加最后一个点
                points.add(new ProjectileStore.TrajectoryPoint(hitPos, time, true));
                
                // 计算落点方块位置
                var blockPos = hit instanceof BlockHitResult blockHit 
                    ? blockHit.getBlockPos() 
                    : new net.minecraft.core.BlockPos((int)hitPos.x, (int)hitPos.y, (int)hitPos.z);
                
                // 更新存储
                ProjectileStore.update(hitPos, blockPos, points);
                return points;
            }
            
            // 检查距离限制
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
        
        // 超时，返回当前轨迹
        ProjectileStore.update(null, null, points);
        return points;
    }
    
    /**
     * 检测两点之间的碰撞
     */
    private static HitResult checkCollision(Level level, Vec3 start, Vec3 end, Entity exclude) {
        
        // 方块碰撞检测
        ClipContext ctx = new ClipContext(
            start, end,
            ClipContext.Block.OUTLINE,
            ClipContext.Fluid.NONE,
            exclude
        );
        
        BlockHitResult blockHit = level.clip(ctx);
        
        // 实体碰撞检测
        AABB sweepBox = new AABB(start, end).inflate(0.3);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
            level, exclude,
            start, end,
            sweepBox,
            e -> !e.isSpectator() && e.isPickable() && e != exclude
        );
        
        // 比较哪个更近
        double blockDist = blockHit.getType() == HitResult.Type.BLOCK
            ? start.distanceToSqr(blockHit.getLocation()) : Double.MAX_VALUE;
        double entityDist = entityHit != null
            ? start.distanceToSqr(entityHit.getLocation()) : Double.MAX_VALUE;
        
        if (entityDist < blockDist && entityHit != null) {
            return entityHit;
        } else if (blockHit.getType() == HitResult.Type.BLOCK) {
            return blockHit;
        }
        
        return null;
    }
    
    /**
     * 根据弓的蓄力计算速度倍率
     * Minecraft 弓箭蓄力公式
     */
    public static float calculateBowPower(LocalPlayer player) {
        if (player == null) return 0.0f;
        
        // 检测是否在拉弓
        var useItem = player.getUseItem();
        var item = useItem.getItem();
        String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).toString();
        
        // 如果不是弓/弩，返回0
        if (!itemId.contains("bow") && !itemId.contains("crossbow")) {
            // 检查是否在使用物品（拉弓状态）
            if (!player.isUsingItem()) {
                return 0.0f;
            }
        }
        
        // 获取蓄力时间
        int useTime = player.getUseItemRemainingTicks();
        
        // Minecraft 弓的最大蓄力时间是 1.0 秒 = 20 ticks
        // 但实际上弓可以蓄力更久，这里用 20 作为最大值
        float maxUseTime = 20.0f;
        
        if (useTime <= 0) {
            return 0.0f;
        }
        
        // 计算蓄力比例 (0.0 - 1.0)
        float power = Math.min(1.0f, useTime / maxUseTime);
        
        return power;
    }
    
    /**
     * 简单抛物线计算（不进行碰撞检测）
     * 用于快速预览
     */
    public static Vec3[] calculateSimpleArc(Vec3 start, Vec3 velocity, int steps, float gravity) {
        Vec3[] points = new Vec3[steps];
        
        double vx = velocity.x;
        double vy = velocity.y;
        double vz = velocity.z;
        double x = start.x;
        double y = start.y;
        double z = start.z;
        
        for (int i = 0; i < steps; i++) {
            points[i] = new Vec3(x, y, z);
            
            vy -= gravity;
            x += vx;
            y += vy;
            z += vz;
        }
        
        return points;
    }
}
