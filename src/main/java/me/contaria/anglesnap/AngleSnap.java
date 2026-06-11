package me.contaria.anglesnap;

import com.mojang.logging.LogUtils;
import me.contaria.anglesnap.config.AngleSnapConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Camera;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.DeltaTracker;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import me.contaria.anglesnap.gui.camerasnap.CameraSnapScreen;
import me.contaria.anglesnap.gui.screen.AngleSnapScreen;
import net.minecraft.util.CommonColors;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import java.util.Objects;

public class AngleSnap implements ClientModInitializer {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final AngleSnapConfig CONFIG = new AngleSnapConfig();

    private static final KeyMapping.Category ANGLESNAP_CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("anglesnap", "key"));

    public static KeyMapping openMenu;
    public static KeyMapping openOverlay;
    public static KeyMapping cameraPositions;

    private static final AngleSnapState STATE = new AngleSnapState();

    @Override
    public void onInitializeClient() {
        openMenu = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "anglesnap.key.openmenu",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_F6,
                ANGLESNAP_CATEGORY
        ));

        openOverlay = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "anglesnap.key.openoverlay",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_F7,
                ANGLESNAP_CATEGORY
        ));

        cameraPositions = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "anglesnap.key.camerapositions",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_F8,
                ANGLESNAP_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenu.consumeClick()) {
                if (AngleSnap.CONFIG.hasAngles() && client.screen == null) {
                    client.setScreen(AngleSnapScreen.create(null));
                }
            }
            while (openOverlay.consumeClick()) {
                STATE.toggleOverlay();
            }
            while (cameraPositions.consumeClick()) {
                if (AngleSnap.getCurrentCameraPos() == null) {
                    if (AngleSnap.CONFIG.hasCameraPositions() && client.screen == null) {
                        client.setScreen(CameraSnapScreen.create(null));
                    }
                } else {
                    AngleSnap.setCurrentCameraPos(null);
                }
            }
        });

        HudElementRegistry.addFirst(Identifier.fromNamespaceAndPath("anglesnap", "overlay"), AngleSnap::renderHud);

        ClientPlayConnectionEvents.JOIN.register((networkHandler, packetSender, client) -> {
            AngleSnap.resetRuntimeState();
            if (client.hasSingleplayerServer()) {
                AngleSnap.CONFIG.loadAnglesAndCameraPositions(
                        Objects.requireNonNull(client.getSingleplayerServer()).getWorldPath(LevelResource.ROOT).getParent().getFileName().toString(),
                        AngleSnapConfig.AngleFolder.SINGLEPLAYER
                );
            } else if (Objects.requireNonNull(client.getCurrentServer()).isRealm()) {
                AngleSnap.CONFIG.loadAnglesAndCameraPositions(client.getCurrentServer().name, AngleSnapConfig.AngleFolder.REALMS);
            } else {
                AngleSnap.CONFIG.loadAnglesAndCameraPositions(client.getCurrentServer().ip, AngleSnapConfig.AngleFolder.MULTIPLAYER);
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((networkHandler, client) -> {
            AngleSnap.resetRuntimeState();
            AngleSnap.CONFIG.unloadAnglesAndCameraPositions();
        });

        LevelRenderEvents.END_MAIN.register(context -> AngleSnap.renderOverlay(
                Minecraft.getInstance().gameRenderer.getMainCamera(),
                context.poseStack().last().pose()
        ));
    }

    public static boolean shouldRenderOverlay() {
        return STATE.shouldRenderOverlay();
    }

    public static boolean shouldSnapToAngle(LocalPlayer player) {
        return player != null && shouldRenderOverlay() && AngleSnap.CONFIG.snapToAngle.getValue();
    }

    public static void resetRuntimeState() {
        STATE.resetRuntimeState();
    }

    @Nullable
    public static CameraPosEntry getCurrentCameraPos() {
        return STATE.getCurrentCameraPos();
    }

    public static void setCurrentCameraPos(@Nullable CameraPosEntry currentCameraPos) {
        STATE.setCurrentCameraPos(currentCameraPos);
    }

    public static void clearCurrentCameraPos(CameraPosEntry cameraPos) {
        STATE.clearCurrentCameraPos(cameraPos);
    }

    @Nullable
    public static CameraPosEntry getActiveCameraPosition() {
        Minecraft client = Minecraft.getInstance();
        return client.level != null && client.player != null ? getCurrentCameraPos() : null;
    }

    private static void renderHud(GuiGraphicsExtractor context, DeltaTracker tickCounter) {
        if (shouldRenderOverlay()) {
            if (AngleSnap.CONFIG.angleHud.getValue()) {
                renderAngleHud(context);
            }
        }
    }

    public static void renderOverlay(Camera camera, Matrix4f positionMatrix) {
        if (!shouldRenderOverlay()) {
            return;
        }

        float markerScale = AngleSnap.CONFIG.markerScale.getValue();
        float textScale = AngleSnap.CONFIG.textScale.getValue();
        if (markerScale <= 0.0f && textScale <= 0.0f) {
            return;
        }

        for (AngleEntry angle : AngleSnap.CONFIG.getAngles()) {
            renderMarker(camera, positionMatrix, angle, markerScale, textScale);
        }
        Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
    }

    private static void renderMarker(Camera camera, Matrix4f positionMatrix, AngleEntry angle, float markerScale, float textScale) {
        markerScale = markerScale / 10.0f;
        textScale = textScale / 50.0f;

        Vector3f pos = Vec3.directionFromRotation(
                Mth.wrapDegrees(angle.pitch),
                Mth.wrapDegrees(angle.yaw + 180.0f)
        ).multiply(-1.0, 1.0, -1.0).toVector3f();

        Quaternionf rotation = camera.rotation();

        drawIcon(camera, positionMatrix, pos, rotation, angle, markerScale);
        if (!angle.name.isEmpty()) {
            drawName(camera, positionMatrix, pos, rotation, angle, textScale);
        }
    }

    private static void drawIcon(Camera camera, Matrix4f positionMatrix, Vector3f pos, Quaternionf rotation, AngleEntry angle, float scale) {
        if (scale == 0.0f) {
            return;
        }

        PoseStack matrices = new PoseStack();
        matrices.mulPose(positionMatrix);
        matrices.pushPose();
        matrices.translate(pos.x(), pos.y(), pos.z());
        matrices.mulPose(rotation);
        matrices.scale(scale, -scale, scale);

        Matrix4f matrix4f = matrices.last().pose();
        Minecraft client = Minecraft.getInstance();

        RenderType layer = RenderTypes.entityTranslucent(angle.getIcon());
        VertexConsumer consumer = client.renderBuffers().bufferSource().getBuffer(layer);

        int a = ARGB.alpha(angle.color);
        int r = ARGB.red(angle.color);
        int g = ARGB.green(angle.color);
        int b = ARGB.blue(angle.color);
        int packedColor = ARGB.color(a, r, g, b);

        int light = 0x00F000F0;
        int overlay = OverlayTexture.NO_OVERLAY;

        float nx = 0.0f;
        float ny = 0.0f;
        float nz = 1.0f;

        org.joml.Vector4f v0 = new org.joml.Vector4f(-1.0f, -1.0f, 0.0f, 1.0f).mul(matrix4f);
        org.joml.Vector4f v1 = new org.joml.Vector4f(-1.0f,  1.0f, 0.0f, 1.0f).mul(matrix4f);
        org.joml.Vector4f v2 = new org.joml.Vector4f(1.0f,  1.0f, 0.0f, 1.0f).mul(matrix4f);
        org.joml.Vector4f v3 = new org.joml.Vector4f(1.0f, -1.0f, 0.0f, 1.0f).mul(matrix4f);

        consumer.addVertex(v0.x, v0.y, v0.z, packedColor, 0.0f, 0.0f, overlay, light, nx, ny, nz);
        consumer.addVertex(v1.x, v1.y, v1.z, packedColor, 0.0f, 1.0f, overlay, light, nx, ny, nz);
        consumer.addVertex(v2.x, v2.y, v2.z, packedColor, 1.0f, 1.0f, overlay, light, nx, ny, nz);
        consumer.addVertex(v3.x, v3.y, v3.z, packedColor, 1.0f, 0.0f, overlay, light, nx, ny, nz);

        matrices.popPose();
    }

    private static void drawName(Camera camera, Matrix4f positionMatrix, Vector3f pos, Quaternionf rotation, AngleEntry angle, float scale) {
        if (scale == 0.0f || angle.name.isEmpty()) {
            return;
        }

        PoseStack matrices = new PoseStack();
        matrices.mulPose(positionMatrix);
        matrices.pushPose();
        matrices.translate(pos.x(), pos.y(), pos.z());
        matrices.mulPose(rotation);
        matrices.scale(scale, -scale, scale);

        Matrix4f matrix4f = matrices.last().pose();
        Minecraft client = Minecraft.getInstance();
        Font textRenderer = client.font;

        float x = -textRenderer.width(angle.name) / 2.0f;
        int backgroundColor = (int) (client.options.getBackgroundOpacity(0.25f) * 255.0f) << 24;

        textRenderer.drawInBatch(
                angle.name, x, -15.0f, CommonColors.WHITE, false, matrix4f,
                client.renderBuffers().bufferSource(),
                Font.DisplayMode.SEE_THROUGH, backgroundColor, 15
        );

        matrices.popPose();
    }

    private static void renderAngleHud(GuiGraphicsExtractor context) {
        Minecraft client = Minecraft.getInstance();
        if (client.getDebugOverlay().showDebugScreen() || client.player == null) {
            return;
        }

        Font textRenderer = client.font;
        String text = String.format("%.3f / %.3f", Mth.wrapDegrees(client.player.getYRot()), Mth.wrapDegrees(client.player.getXRot()));
        context.fill(5, 5, 5 + 2 + textRenderer.width(text) + 2, 5 + 2 + textRenderer.lineHeight + 2, -1873784752);
        context.text(textRenderer, text, 5 + 2 + 1, 5 + 2 + 1, -2039584, false);
    }

    public static boolean isInMultiplayer() {
        return Minecraft.getInstance().level != null && !Minecraft.getInstance().isSingleplayer();
    }
}
