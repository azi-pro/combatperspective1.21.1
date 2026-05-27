package com.aaa.combatperspective.mixin;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Camera.class)
public interface CameraAccessor {
    @Accessor("camerayaw")       // 对于 @Unique 字段，名字就是 field 名
    float getCameraYaw();
    @Accessor("camerayaw")
    void setCameraYaw(float yaw);
}
