package me.contaria.anglesnap;

public final class MouseSnapState {
    private double lastCursorMoveTime;
    private double lastSnapAngleTime = Double.NEGATIVE_INFINITY;
    private Object lastPlayer;

    public void updatePlayer(Object player, double now) {
        if (player != this.lastPlayer) {
            this.lastPlayer = player;
            this.lastCursorMoveTime = now;
            this.lastSnapAngleTime = Double.NEGATIVE_INFINITY;
        }
    }

    public void recordRawMovement(boolean moved, double now) {
        if (moved) {
            this.lastCursorMoveTime = now;
        }
    }

    public boolean isSnapLocked(boolean snapActive, float snapLock, double now) {
        return snapActive && this.lastSnapAngleTime + snapLock > now;
    }

    public boolean canSnap(boolean snapActive, float snapDelay, double now) {
        return snapActive && this.lastCursorMoveTime + snapDelay < now;
    }

    public void recordSnap(double now) {
        this.lastSnapAngleTime = now;
    }
}
