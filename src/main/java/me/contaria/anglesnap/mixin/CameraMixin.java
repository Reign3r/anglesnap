package me.contaria.anglesnap.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.contaria.anglesnap.AngleSnap;
import me.contaria.anglesnap.CameraPosEntry;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @ModifyExpressionValue(
            method = "alignWithEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/CameraType;isFirstPerson()Z"
            )
    )
    private boolean useDetachedCameraForCameraPosition(boolean firstPerson) {
        return firstPerson && AngleSnap.getActiveCameraPosition() == null;
    }

    @ModifyExpressionValue(
            method = "alignWithEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/CameraType;isMirrored()Z"
            )
    )
    private boolean disableMirroredCameraForCameraPosition(boolean mirrored) {
        return mirrored && AngleSnap.getActiveCameraPosition() == null;
    }

    @ModifyVariable(
            method = "setPosition(Lnet/minecraft/world/phys/Vec3;)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Vec3 modifyCameraPosition(Vec3 pos) {
        CameraPosEntry entry = AngleSnap.getActiveCameraPosition();
        if (entry != null) {
            return new Vec3(entry.x, entry.y, entry.z);
        }
        return pos;
    }
}
