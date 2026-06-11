package me.contaria.anglesnap.gui.config;

import me.contaria.anglesnap.AngleSnap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class AngleSnapConfigScreen extends Screen {
    private final Screen parent;

    public AngleSnapConfigScreen(Screen parent) {
        super(Component.translatable("anglesnap.gui.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int titleWidth = this.font.width(this.title);
        this.addRenderableWidget(new StringWidget((this.width - titleWidth) / 2, 10, titleWidth, 15, this.title, this.font));
        this.addRenderableWidget(new AngleSnapConfigListWidget(this.minecraft, this.width, this.height - 70, 35));
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).bounds(this.width / 2 - 100, this.height - 27, 200, 20).build());
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(this.parent);
    }

    @Override
    public void removed() {
        AngleSnap.CONFIG.save();
    }
}
