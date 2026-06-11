package me.contaria.anglesnap.config;

import com.google.gson.JsonElement;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.locale.Language;

public abstract class Option<T> {
    private final String id;

    protected Option(String id) {
        this.id = id;
    }

    public final String getId() {
        return this.id;
    }

    public final Component getName() {
        return Component.translatable("anglesnap.gui.config.option." + this.getId());
    }

    public final Component getMessage() {
        Language language = Language.getInstance();
        String valueSpecified = "anglesnap.gui.config.option." + this.getId() + ".value." + this.getValue();
        if (language.has(valueSpecified)) {
            return Component.translatable(valueSpecified);
        }
        String value = "anglesnap.gui.config.option." + this.getId() + ".value";
        if (language.has(value)) {
            return Component.translatable(value, this.getDefaultMessage());
        }
        return this.getDefaultMessage();
    }

    public abstract T getValue();

    public abstract void setValue(T value);

    public abstract boolean hasWidget();

    public abstract AbstractWidget createWidget(int x, int y, int width, int height);

    public abstract Component getDefaultMessage();

    protected abstract void fromJson(JsonElement jsonGuiEventListener);

    protected abstract JsonElement toJson();
}
