package me.contaria.anglesnap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MouseSnapStateTest {
    @Test
    void snapLockDoesNotApplyWhenSnappingIsInactive() {
        MouseSnapState state = new MouseSnapState();

        state.recordSnap(1.0);

        assertFalse(state.isSnapLocked(false, 0.25f, 1.1));
        assertTrue(state.isSnapLocked(true, 0.25f, 1.1));
    }

    @Test
    void rawMovementDuringSnapLockDelaysTheNextSnap() {
        MouseSnapState state = new MouseSnapState();
        Object player = new Object();

        state.updatePlayer(player, 1.0);
        state.recordSnap(1.0);
        state.recordRawMovement(true, 1.1);

        assertFalse(state.canSnap(true, 0.4f, 1.3));
        assertTrue(state.canSnap(true, 0.4f, 1.6));
    }

    @Test
    void playerChangeClearsInheritedSnapLock() {
        MouseSnapState state = new MouseSnapState();

        state.updatePlayer(new Object(), 10.0);
        state.recordSnap(10.0);
        state.updatePlayer(new Object(), 10.1);

        assertFalse(state.isSnapLocked(true, 0.25f, 10.2));
        assertFalse(state.canSnap(true, 0.25f, 10.2));
    }
}
