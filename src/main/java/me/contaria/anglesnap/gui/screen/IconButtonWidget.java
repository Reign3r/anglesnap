package me.contaria.anglesnap.gui.screen;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class IconButtonWidget extends Button {
    private Identifier texture;

    public IconButtonWidget(Component message, Button.OnPress onPress, Identifier texture) {
        this(0, 0, 16, 16, message, onPress, Button.DEFAULT_NARRATION, texture);
    }

    public IconButtonWidget(int x, int y, Component message, Button.OnPress onPress, Identifier texture) {
        this(x, y, 16, 16, message, onPress, Button.DEFAULT_NARRATION, texture);
    }

    protected IconButtonWidget(int x, int y, int width, int height, Component message, Button.OnPress onPress, Button.CreateNarration narrationSupplier, Identifier texture) {
        super(x, y, width, height, message, onPress, narrationSupplier);
        this.texture = texture;
        this.setTooltip(Tooltip.create(message));
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.blit(RenderPipelines.GUI_TEXTURED, this.texture, this.getX(), this.getY(), 0, 0, this.getWidth(), this.getHeight(), 16, 16);
    }

    public void setTexture(Identifier texture) {
        this.texture = texture;
    }
}
