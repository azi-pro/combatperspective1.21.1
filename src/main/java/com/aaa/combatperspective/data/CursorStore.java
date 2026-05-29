package com.aaa.combatperspective.data;

public class CursorStore {
    private static boolean enable = false;
    private static double x;
    private static double y;
    private static float cameraYaw;              // 摄像机水平朝向
    private static Object cameraTarget;         // 摄像机指向的实体（null=未初始化）

    // 包访问权限，同包/子包可调用，无public
    static boolean isEnable() {
        return enable;
    }

    static void setEnable(boolean flag) {
        enable = flag;
    }

    static double getX() {
        return x;
    }

    static double getY() {
        return y;
    }

    static void setPos(double px, double py) {
        x = px;
        y = py;
    }

    /** 摄像机水平朝向（yaw），由 CameraMixin 每帧写入 */
    public static float getCameraYaw() {
        return cameraYaw;
    }

    public static void setCameraYaw(float yaw) {
        cameraYaw = yaw;
    }

    /** 摄像机当前指向的实体，仅对本地玩家有效 */
    public static Object getCameraTarget() {
        return cameraTarget;
    }

    public static void setCameraTarget(Object target) {
        cameraTarget = target;
    }

}