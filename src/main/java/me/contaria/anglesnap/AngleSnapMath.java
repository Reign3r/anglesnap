package me.contaria.anglesnap;

public final class AngleSnapMath {
    private AngleSnapMath() {
    }

    public static float wrapDegrees(float degrees) {
        float wrapped = degrees % 360.0f;
        if (wrapped >= 180.0f) {
            wrapped -= 360.0f;
        }
        if (wrapped < -180.0f) {
            wrapped += 360.0f;
        }
        return wrapped;
    }

    public static float clampPitch(float pitch) {
        return Math.max(-90.0f, Math.min(90.0f, pitch));
    }

    public static float yawDistance(float yaw, float targetYaw) {
        return Math.abs(wrapDegrees(yaw - targetYaw));
    }

    public static float pitchDistance(float pitch, float targetPitch) {
        return Math.abs(clampPitch(pitch) - clampPitch(targetPitch));
    }

    public static float angleDistance(float yaw, float pitch, float targetYaw, float targetPitch) {
        float yawDistance = yawDistance(yaw, targetYaw);
        float pitchDistance = pitchDistance(pitch, targetPitch);
        return (float) Math.sqrt(yawDistance * yawDistance + pitchDistance * pitchDistance);
    }
}
