package me.contaria.anglesnap.gui.camerasnap;

import me.contaria.anglesnap.AngleSnap;
import me.contaria.anglesnap.gui.config.AngleSnapConfigScreen;
import me.contaria.anglesnap.gui.screen.IconButtonWidget;
import me.contaria.anglesnap.gui.warning.AngleSnapWarningScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class CameraSnapScreen extends Screen {
    private static final Component CONFIGURE_TEXT = Component.translatable("anglesnap.gui.screen.configure");
    private static final Identifier CONFIGURE_TEXTURE = Identifier.fromNamespaceAndPath("anglesnap", "textures/gui/configure.png");

    private final Screen parent;

    protected CameraSnapScreen(Screen parent) {
        super(Component.translatable("anglesnap.gui.screen.title.camerasnap"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int titleWidth = this.font.width(this.title);
        this.addRenderableWidget(new StringWidget((this.width - titleWidth) / 2, 10, titleWidth, 15, this.title, this.font));
        this.addRenderableWidget(new IconButtonWidget(this.width - 26, 10, CONFIGURE_TEXT, button -> Minecraft.getInstance().setScreen(new AngleSnapConfigScreen(this)), CONFIGURE_TEXTURE));
        this.addRenderableWidget(new CameraSnapListWidget(this.minecraft, this.width, this.height - 70, 35));
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).bounds(this.width / 2 - 100, this.height - 27, 200, 20).build());
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(this.parent);
    }

    @Override
    public void removed() {
        AngleSnap.CONFIG.saveCameraPositions();
    }

    public static Screen create(Screen parent) {
        if (AngleSnap.CONFIG.hasCameraPositions()) {
            if (AngleSnap.isInMultiplayer() && !AngleSnap.CONFIG.disableMultiplayerWarning.getValue()) {
                return AngleSnapWarningScreen.create(
                        disableMultiplayerWarning -> {
                            if (disableMultiplayerWarning) {
                                AngleSnap.CONFIG.disableMultiplayerWarning.setValue(true);
                                AngleSnap.CONFIG.save();
                            }
                            Minecraft.getInstance().setScreen(new CameraSnapScreen(parent));
                        },
                        () -> Minecraft.getInstance().setScreen(parent)
                );
            }
            return new CameraSnapScreen(parent);
        }
        return new AngleSnapConfigScreen(parent);
    }
}
