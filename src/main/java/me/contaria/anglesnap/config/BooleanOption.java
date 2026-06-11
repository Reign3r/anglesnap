package me.contaria.anglesnap.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class BooleanOption extends Option<Boolean> {
    private boolean value;

    protected BooleanOption(String id, boolean defaultValue) {
        super(id);
        this.setValue(defaultValue);
    }

    @Override
    public Boolean getValue() {
        return this.value;
    }

    @Override
    public void setValue(Boolean value) {
        this.value = value;
    }

    @Override
    public boolean hasWidget() {
        return true;
    }

    @Override
    public AbstractWidget createWidget(int x, int y, int width, int height) {
        return Button.builder(this.getMessage(), button -> {
            this.setValue(!this.getValue());
            button.setMessage(BooleanOption.this.getMessage());
        }).bounds(x, y, width, height).build();
    }

    @Override
    public Component getDefaultMessage() {
        return CommonComponents.optionStatus(this.getValue());
    }

    @Override
    protected void fromJson(JsonElement jsonGuiEventListener) {
        this.setValue(jsonGuiEventListener.getAsBoolean());
    }

    @Override
    protected JsonElement toJson() {
        return new JsonPrimitive(this.getValue());
    }
}
