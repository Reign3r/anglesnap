package me.contaria.anglesnap.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.contaria.anglesnap.AngleEntry;
import me.contaria.anglesnap.AngleSnap;
import me.contaria.anglesnap.MouseSnapState;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MouseHandler.class)
public abstract class MouseMixin {
    @Shadow
    private double lastHandleMovementTime;

    @Unique
    private final MouseSnapState angleSnap$state = new MouseSnapState();

    @WrapOperation(
            method = "turnPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"
            )
    )
    private void snapToAngle(LocalPlayer player, double cursorDeltaX, double cursorDeltaY, Operation<Void> original) {
        double now = this.lastHandleMovementTime;
        boolean snapActive = AngleSnap.shouldSnapToAngle(player);

        this.angleSnap$state.updatePlayer(player, now);
        this.angleSnap$state.recordRawMovement(cursorDeltaX != 0.0 || cursorDeltaY != 0.0, now);

        if (this.angleSnap$state.isSnapLocked(snapActive, AngleSnap.CONFIG.snapLock.getValue(), now)) {
            original.call(player, 0.0, 0.0);
            return;
        }

        original.call(player, cursorDeltaX, cursorDeltaY);
        this.snapToAngle(player, snapActive, now);
    }

    @Unique
    private void snapToAngle(LocalPlayer player, boolean snapActive, double now) {
        // don't snap to angle if mouse was in motion within snapDelay
        if (!this.angleSnap$state.canSnap(snapActive, AngleSnap.CONFIG.snapDelay.getValue(), now)) {
            return;
        }

        AngleEntry closestAngle = null;
        float closestDistance = AngleSnap.CONFIG.snapDistance.getValue();
        for (AngleEntry angle : AngleSnap.CONFIG.getAngles()) {
            float distance = angle.getDistance(player.getYRot(), player.getXRot());
            if (distance < closestDistance) {
                closestAngle = angle;
                closestDistance = distance;
            }
        }
        if (closestAngle != null && closestDistance > 0.0f) {
            this.angleSnap$state.recordSnap(now);
            closestAngle.snap();
        }
    }
}
