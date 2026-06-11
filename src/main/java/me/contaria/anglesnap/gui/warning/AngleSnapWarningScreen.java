package me.contaria.anglesnap.gui.warning;

import net.minecraft.client.gui.screens.multiplayer.WarningScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class AngleSnapWarningScreen extends WarningScreen {
    private final Consumer<Boolean> onConfirm;
    private final Runnable onCancel;

    private AngleSnapWarningScreen(Component header, Component message, Component checkbox, Component narration, Consumer<Boolean> onConfirm, Runnable onCancel) {
        super(header, message, checkbox, narration);
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
    }

    @Override
    protected Layout addFooterButtons() {
        LinearLayout layout = LinearLayout.horizontal().spacing(8);
        layout.addChild(Button.builder(CommonComponents.GUI_PROCEED, button -> this.onConfirm.accept(this.stopShowing != null && this.stopShowing.selected())).build());
        layout.addChild(Button.builder(CommonComponents.GUI_BACK, button -> this.onCancel.run()).build());
        return layout;
    }

    public static AngleSnapWarningScreen create(Consumer<Boolean> onConfirm, Runnable onCancel) {
        Component header = Component.translatable("anglesnap.gui.warning.header");
        Component message = Component.translatable("anglesnap.gui.warning.message");
        Component checkbox = Component.translatable("anglesnap.gui.warning.checkbox");
        return new AngleSnapWarningScreen(
                header,
                message,
                checkbox,
                header.copy().append("\n").append(message),
                onConfirm,
                onCancel
        );
    }
}
