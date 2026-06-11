package me.contaria.anglesnap;

import org.jetbrains.annotations.Nullable;

public final class AngleSnapState {
    private boolean overlayEnabled;

    @Nullable
    private CameraPosEntry currentCameraPos;

    public void toggleOverlay() {
        this.overlayEnabled = !this.overlayEnabled;
    }

    public boolean shouldRenderOverlay() {
        return this.overlayEnabled;
    }

    public void resetRuntimeState() {
        this.overlayEnabled = false;
        this.currentCameraPos = null;
    }

    @Nullable
    public CameraPosEntry getCurrentCameraPos() {
        return this.currentCameraPos;
    }

    public void setCurrentCameraPos(@Nullable CameraPosEntry currentCameraPos) {
        this.currentCameraPos = currentCameraPos;
    }

    public void clearCurrentCameraPos(CameraPosEntry cameraPos) {
        if (this.currentCameraPos == cameraPos) {
            this.currentCameraPos = null;
        }
    }
}
