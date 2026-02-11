package me.contaria.anglesnap;

import com.mojang.logging.LogUtils;
import me.contaria.anglesnap.config.AngleSnapConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
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

    private static final KeyBinding.Category ANGLESNAP_CATEGORY =
            KeyBinding.Category.create(Identifier.of("anglesnap", "key"));

    public static KeyBinding openMenu;
    public static KeyBinding openOverlay;
    public static KeyBinding cameraPositions;

    private static boolean overlayEnabled;

    @Nullable
    public static CameraPosEntry currentCameraPos;

    @Override
    public void onInitializeClient() {
        openMenu = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "anglesnap.key.openmenu",
                GLFW.GLFW_KEY_F6,
                ANGLESNAP_CATEGORY
        ));

        openOverlay = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "anglesnap.key.openoverlay",
                GLFW.GLFW_KEY_F7,
                ANGLESNAP_CATEGORY
        ));

        cameraPositions = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "anglesnap.key.camerapositions",
                GLFW.GLFW_KEY_F8,
                ANGLESNAP_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openOverlay.wasPressed()) {
                overlayEnabled = !overlayEnabled;
            }
        });

        HudElementRegistry.addFirst(Identifier.of("anglesnap", "overlay"), AngleSnap::renderHud);

        ClientPlayConnectionEvents.JOIN.register((networkHandler, packetSender, client) -> {
            if (client.isIntegratedServerRunning()) {
                AngleSnap.CONFIG.loadAnglesAndCameraPositions(
                        Objects.requireNonNull(client.getServer()).getSavePath(WorldSavePath.ROOT).getParent().getFileName().toString(),
                        AngleSnapConfig.AngleFolder.SINGLEPLAYER
                );
            } else if (Objects.requireNonNull(networkHandler.getServerInfo()).isRealm()) {
                AngleSnap.CONFIG.loadAnglesAndCameraPositions(networkHandler.getServerInfo().name, AngleSnapConfig.AngleFolder.REALMS);
            } else {
                AngleSnap.CONFIG.loadAnglesAndCameraPositions(networkHandler.getServerInfo().address, AngleSnapConfig.AngleFolder.MULTIPLAYER);
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((networkHandler, client) -> AngleSnap.CONFIG.unloadAnglesAndCameraPositions());
    }

    public static boolean shouldRenderOverlay() {
        return overlayEnabled;
    }

    private static void renderHud(DrawContext context, RenderTickCounter tickCounter) {
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
        MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers().draw();
    }

    private static void renderMarker(Camera camera, Matrix4f positionMatrix, AngleEntry angle, float markerScale, float textScale) {
        markerScale = markerScale / 10.0f;
        textScale = textScale / 50.0f;

        Vector3f pos = Vec3d.fromPolar(
                MathHelper.wrapDegrees(angle.pitch),
                MathHelper.wrapDegrees(angle.yaw + 180.0f)
        ).multiply(-1.0, 1.0, -1.0).toVector3f();

        Quaternionf rotation = camera.getRotation();

        drawIcon(camera, positionMatrix, pos, rotation, angle, markerScale);
        if (!angle.name.isEmpty()) {
            drawName(camera, positionMatrix, pos, rotation, angle, textScale);
        }
    }

    private static void drawIcon(Camera camera, Matrix4f positionMatrix, Vector3f pos, Quaternionf rotation, AngleEntry angle, float scale) {
        if (scale == 0.0f) {
            return;
        }

        MatrixStack matrices = new MatrixStack();
        matrices.multiplyPositionMatrix(positionMatrix);
        matrices.push();
        matrices.translate(pos.x(), pos.y(), pos.z());
        matrices.multiply(rotation);
        matrices.scale(scale, -scale, scale);

        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        MinecraftClient client = MinecraftClient.getInstance();

        RenderLayer layer = RenderLayer.getEntityTranslucent(angle.getIcon());
        VertexConsumer consumer = client.getBufferBuilders().getEntityVertexConsumers().getBuffer(layer);

        int a = ColorHelper.getAlpha(angle.color);
        int r = ColorHelper.getRed(angle.color);
        int g = ColorHelper.getGreen(angle.color);
        int b = ColorHelper.getBlue(angle.color);
        int packedColor = ColorHelper.getArgb(a, r, g, b);

        int light = 0x00F000F0;
        int overlay = OverlayTexture.DEFAULT_UV;

        float nx = 0.0f;
        float ny = 0.0f;
        float nz = 1.0f;

        org.joml.Vector4f v0 = new org.joml.Vector4f(-1.0f, -1.0f, 0.0f, 1.0f).mul(matrix4f);
        org.joml.Vector4f v1 = new org.joml.Vector4f(-1.0f,  1.0f, 0.0f, 1.0f).mul(matrix4f);
        org.joml.Vector4f v2 = new org.joml.Vector4f(1.0f,  1.0f, 0.0f, 1.0f).mul(matrix4f);
        org.joml.Vector4f v3 = new org.joml.Vector4f(1.0f, -1.0f, 0.0f, 1.0f).mul(matrix4f);

        consumer.vertex(v0.x, v0.y, v0.z, packedColor, 0.0f, 0.0f, overlay, light, nx, ny, nz);
        consumer.vertex(v1.x, v1.y, v1.z, packedColor, 0.0f, 1.0f, overlay, light, nx, ny, nz);
        consumer.vertex(v2.x, v2.y, v2.z, packedColor, 1.0f, 1.0f, overlay, light, nx, ny, nz);
        consumer.vertex(v3.x, v3.y, v3.z, packedColor, 1.0f, 0.0f, overlay, light, nx, ny, nz);

        matrices.pop();
    }

    private static void drawName(Camera camera, Matrix4f positionMatrix, Vector3f pos, Quaternionf rotation, AngleEntry angle, float scale) {
        if (scale == 0.0f || angle.name.isEmpty()) {
            return;
        }

        MatrixStack matrices = new MatrixStack();
        matrices.multiplyPositionMatrix(positionMatrix);
        matrices.push();
        matrices.translate(pos.x(), pos.y(), pos.z());
        matrices.multiply(rotation);
        matrices.scale(scale, -scale, scale);

        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        float x = -textRenderer.getWidth(angle.name) / 2.0f;
        int backgroundColor = (int) (client.options.getTextBackgroundOpacity(0.25f) * 255.0f) << 24;

        textRenderer.draw(
                angle.name, x, -15.0f, Colors.WHITE, false, matrix4f,
                client.getBufferBuilders().getEntityVertexConsumers(),
                TextRenderer.TextLayerType.SEE_THROUGH, backgroundColor, 15
        );

        matrices.pop();
    }

    private static void renderAngleHud(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getDebugHud().shouldShowDebugHud() || client.player == null) {
            return;
        }

        TextRenderer textRenderer = client.textRenderer;
        String text = String.format("%.3f / %.3f", MathHelper.wrapDegrees(client.player.getYaw()), MathHelper.wrapDegrees(client.player.getPitch()));
        context.fill(5, 5, 5 + 2 + textRenderer.getWidth(text) + 2, 5 + 2 + textRenderer.fontHeight + 2, -1873784752);
        context.drawText(textRenderer, text, 5 + 2 + 1, 5 + 2 + 1, -2039584, false);
    }

    public static boolean isInMultiplayer() {
        return MinecraftClient.getInstance().world != null && !MinecraftClient.getInstance().isInSingleplayer();
    }
}
