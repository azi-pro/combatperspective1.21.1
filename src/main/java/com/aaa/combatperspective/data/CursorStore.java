package com.aaa.combatperspective.data;

public class CursorStore {
    private static boolean enable = false;
    private static double x;
    private static double y;

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
}