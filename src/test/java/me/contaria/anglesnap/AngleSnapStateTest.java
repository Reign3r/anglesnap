package me.contaria.anglesnap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AngleSnapStateTest {
    @Test
    void resetClearsTransientWorldState() {
        AngleSnapState state = new AngleSnapState();
        CameraPosEntry position = new CameraPosEntry(1.0, 2.0, 3.0);

        state.toggleOverlay();
        state.setCurrentCameraPos(position);

        state.resetRuntimeState();

        assertFalse(state.shouldRenderOverlay());
        assertNull(state.getCurrentCameraPos());
    }

    @Test
    void deletingActiveCameraPositionClearsIt() {
        AngleSnapState state = new AngleSnapState();
        CameraPosEntry active = new CameraPosEntry(1.0, 2.0, 3.0);
        CameraPosEntry other = new CameraPosEntry(4.0, 5.0, 6.0);

        state.setCurrentCameraPos(active);
        state.clearCurrentCameraPos(other);
        assertSame(active, state.getCurrentCameraPos());

        state.clearCurrentCameraPos(active);
        assertNull(state.getCurrentCameraPos());
    }

    @Test
    void overlayToggleCanEnableAndDisableOverlay() {
        AngleSnapState state = new AngleSnapState();

        state.toggleOverlay();
        assertTrue(state.shouldRenderOverlay());

        state.toggleOverlay();
        assertFalse(state.shouldRenderOverlay());
    }
}
