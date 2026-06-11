package me.contaria.anglesnap.gui.camerasnap;

import me.contaria.anglesnap.AngleSnap;
import me.contaria.anglesnap.CameraPosEntry;
import me.contaria.anglesnap.gui.screen.IconButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

import java.util.ArrayList;
import java.util.List;

public class CameraSnapListWidget extends ContainerObjectSelectionList<CameraSnapListWidget.AbstractEntry> {
    private static final Component NAME_TEXT = Component.translatable("anglesnap.gui.screen.name");
    private static final Component X_TEXT = Component.translatable("anglesnap.gui.screen.x");
    private static final Component Y_TEXT = Component.translatable("anglesnap.gui.screen.y");
    private static final Component Z_TEXT = Component.translatable("anglesnap.gui.screen.z");
    private static final Component ADD_TEXT = Component.translatable("anglesnap.gui.screen.add");
    private static final Component DELETE_TEXT = Component.translatable("anglesnap.gui.screen.delete");
    private static final Component EDIT_TEXT = Component.translatable("anglesnap.gui.screen.edit");
    private static final Component SAVE_TEXT = Component.translatable("anglesnap.gui.screen.save");

    private static final Identifier ADD_TEXTURE = Identifier.fromNamespaceAndPath("anglesnap", "textures/gui/add.png");
    private static final Identifier DELETE_TEXTURE = Identifier.fromNamespaceAndPath("anglesnap", "textures/gui/delete.png");
    private static final Identifier EDIT_TEXTURE = Identifier.fromNamespaceAndPath("anglesnap", "textures/gui/edit.png");
    private static final Identifier SAVE_TEXTURE = Identifier.fromNamespaceAndPath("anglesnap", "textures/gui/save.png");

    private static final int HOVERED_COLOR = ARGB.color(100, 200, 200, 200);

    public CameraSnapListWidget(Minecraft minecraftClient, int width, int height, int y) {
        super(minecraftClient, width, height, y, 20);

        for (CameraPosEntry pos : AngleSnap.CONFIG.getCameraPositions()) {
            this.addEntry(new Entry(pos));
        }
        this.addEntry(new AddAngleEntry());
    }

    @Override
    public int getRowLeft() {
        return 6;
    }

    @Override
    public int getRowWidth() {
        return this.width - 12;
    }

    @Override
    protected int scrollBarX() {
        return this.width - 6;
    }

    protected void renderHeader(GuiGraphicsExtractor context, int x, int y) {
        Font textRenderer = this.minecraft.font;
        context.text(textRenderer, NAME_TEXT, x + 5, y + (20 - textRenderer.lineHeight) / 2, CommonColors.WHITE, true);
        context.text(textRenderer, X_TEXT, x + 5 + 5 * this.getRowWidth() / 17, y + (20 - textRenderer.lineHeight) / 2, CommonColors.WHITE, true);
        context.text(textRenderer, Y_TEXT, x + 5 + 7 * this.getRowWidth() / 17, y + (20 - textRenderer.lineHeight) / 2, CommonColors.WHITE, true);
        context.text(textRenderer, Z_TEXT, x + 5 + 9 * this.getRowWidth() / 17, y + (20 - textRenderer.lineHeight) / 2, CommonColors.WHITE, true);
    }

    public abstract static class AbstractEntry extends ContainerObjectSelectionList.Entry<AbstractEntry> {
        protected final Minecraft client;
        protected final List<AbstractWidget> children;

        protected AbstractEntry() {
            this.client = Minecraft.getInstance();
            this.children = new ArrayList<>();
        }

        protected <T extends AbstractWidget> T addChild(T widget) {
            this.children.add(widget);
            return widget;
        }

        @Override
        public void visitWidgets(java.util.function.Consumer<AbstractWidget> consumer) {
            this.children.forEach(consumer);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return this.children;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return this.children;
        }

        @Override
        public void setFocused(boolean focused) {
            super.setFocused(focused);
            if (!focused) {
                this.setFocused(null);
            }
        }

        protected void renderWidgetAt(GuiGraphicsExtractor context, int mouseX, int mouseY, float tickDelta, AbstractWidget widget, int x, int y) {
            widget.setX(x);
            widget.setY(y);
            widget.extractRenderState(context, mouseX, mouseY, tickDelta);
        }
    }

    public class Entry extends AbstractEntry {
        private final CameraPosEntry pos;

        private final EditBox name;
        private final EditBox x;
        private final EditBox y;
        private final EditBox z;
        private final IconButtonWidget edit;
        private final IconButtonWidget save;
        private final IconButtonWidget delete;

        private boolean editing;

        public Entry() {
            this(AngleSnap.CONFIG.createCameraPosition());
            this.setEditing(true);
            this.setFocused(this.name);
            this.name.setFocused(true);
        }

        public Entry(CameraPosEntry pos) {
            if (pos == null) {
                throw new IllegalStateException("Cannot create a camera position entry before camera position data is loaded");
            }
            this.pos = pos;

            this.name = this.addChild(new EditBox(
                    this.client.font,
                    5 * CameraSnapListWidget.this.getRowWidth() / 17 - 5,
                    20,
                    NAME_TEXT
            ));
            this.name.setValue(this.pos.name);
            this.name.setResponder(name -> this.pos.name = name);
            this.name.setBordered(false);
            this.name.setTextColor(CommonColors.WHITE);
            this.name.setTextColorUneditable(CommonColors.WHITE);

            this.x = this.addChild(new EditBox(
                    this.client.font,
                    2 * CameraSnapListWidget.this.getRowWidth() / 17 - 5,
                    20,
                    X_TEXT
            ));
            this.x.setValue(String.valueOf(this.pos.x));
            this.x.setResponder(yaw -> {
                try {
                    this.pos.x = Double.parseDouble(yaw);
                } catch (NumberFormatException e) {
                    this.pos.x = 0.0;
                }
            });
            this.x.setBordered(false);
            this.x.setTextColor(CommonColors.WHITE);
            this.x.setTextColorUneditable(CommonColors.WHITE);

            this.y = this.addChild(new EditBox(
                    this.client.font,
                    2 * CameraSnapListWidget.this.getRowWidth() / 17 - 5,
                    20,
                    Y_TEXT
            ));
            this.y.setValue(String.valueOf(this.pos.y));
            this.y.setResponder(yaw -> {
                try {
                    this.pos.y = Double.parseDouble(yaw);
                } catch (NumberFormatException e) {
                    this.pos.y = 0.0;
                }
            });
            this.y.setBordered(false);
            this.y.setTextColor(CommonColors.WHITE);
            this.y.setTextColorUneditable(CommonColors.WHITE);

            this.z = this.addChild(new EditBox(
                    this.client.font,
                    2 * CameraSnapListWidget.this.getRowWidth() / 17 - 5,
                    20,
                    Z_TEXT
            ));
            this.z.setValue(String.valueOf(this.pos.z));
            this.z.setResponder(yaw -> {
                try {
                    this.pos.z = Double.parseDouble(yaw);
                } catch (NumberFormatException e) {
                    this.pos.z = 0.0;
                }
            });
            this.z.setBordered(false);
            this.z.setTextColor(CommonColors.WHITE);
            this.z.setTextColorUneditable(CommonColors.WHITE);

            this.edit = this.addChild(new IconButtonWidget(EDIT_TEXT, button -> this.toggleEditing(), EDIT_TEXTURE));
            this.save = this.addChild(new IconButtonWidget(SAVE_TEXT, button -> this.toggleEditing(), SAVE_TEXTURE));
            this.delete = this.addChild(new IconButtonWidget(DELETE_TEXT, button -> this.delete(), DELETE_TEXTURE));

            this.setEditing(false);
        }

        private void toggleEditing() {
            boolean wasEditing = this.editing;
            this.setEditing(!this.editing);
            if (wasEditing) {
                AngleSnap.CONFIG.saveCameraPositions();
            }
        }

        private void setEditing(boolean editing) {
            this.editing = editing;

            this.setEditing(this.name, editing);
            this.setEditing(this.x, editing);
            this.setEditing(this.y, editing);
            this.setEditing(this.z, editing);

            if (!editing) {
                this.x.setValue(String.valueOf(this.pos.x));
                this.y.setValue(String.valueOf(this.pos.y));
                this.z.setValue(String.valueOf(this.pos.z));
            }

            this.edit.visible = !editing;
            this.save.visible = editing;
        }

        private void setEditing(EditBox widget, boolean editing) {
            widget.active = editing;
            widget.setEditable(editing);
            if (!editing && this.getFocused() == widget) {
                this.setFocused(null);
            }
            widget.setFocused(false);
            widget.setCanLoseFocus(editing);
            widget.moveCursorToStart(false);
        }

        private void delete() {
            AngleSnap.clearCurrentCameraPos(this.pos);
            AngleSnap.CONFIG.removeCameraPosition(this.pos);
            AngleSnap.CONFIG.saveCameraPositions();
            CameraSnapListWidget.this.removeEntry(this);
        }

        @Override
        public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            int x = this.getX();
            int y = this.getY();
            int entryWidth = this.getWidth();
            int entryHeight = this.getHeight();

            if (hovered) {
                context.fill(x, y, x + entryWidth, y + entryHeight, HOVERED_COLOR);
            }
            int textY = y + (entryHeight - this.client.font.lineHeight + 1) / 2;
            this.renderStringWidgetAt(context, mouseX, mouseY, deltaTicks, this.name, x + 5, textY);
            this.renderNumberWidgetAt(context, mouseX, mouseY, deltaTicks, this.x, x + 5 + 5 * entryWidth / 17, textY);
            this.renderNumberWidgetAt(context, mouseX, mouseY, deltaTicks, this.y, x + 5 + 7 * entryWidth / 17, textY);
            this.renderNumberWidgetAt(context, mouseX, mouseY, deltaTicks, this.z, x + 5 + 9 * entryWidth / 17, textY);
            this.renderWidgetAt(context, mouseX, mouseY, deltaTicks, this.edit, x + entryWidth - 5 - 40, y);
            this.renderWidgetAt(context, mouseX, mouseY, deltaTicks, this.save, x + entryWidth - 5 - 40, y);
            this.renderWidgetAt(context, mouseX, mouseY, deltaTicks, this.delete, x + entryWidth - 5 - 20, y);
        }

        private void renderStringWidgetAt(GuiGraphicsExtractor context, int mouseX, int mouseY, float tickDelta, EditBox widget, int x, int y) {
            if (this.editing) {
                this.renderWidgetAt(context, mouseX, mouseY, tickDelta, widget, x, y);
            } else {
                context.text(this.client.font, widget.getValue(), x, y, CommonColors.WHITE, true);
            }
        }

        private void renderNumberWidgetAt(GuiGraphicsExtractor context, int mouseX, int mouseY, float tickDelta, EditBox widget, int x, int y) {
            if (this.isNumberOrEmpty(widget.getValue())) {
                this.renderStringWidgetAt(context, mouseX, mouseY, tickDelta, widget, x, y);
            } else {
                widget.setTextColor(CommonColors.SOFT_RED);
                widget.setTextColorUneditable(CommonColors.SOFT_RED);
                this.renderStringWidgetAt(context, mouseX, mouseY, tickDelta, widget, x, y);
                widget.setTextColor(CommonColors.WHITE);
                widget.setTextColorUneditable(CommonColors.WHITE);
            }
        }

        private boolean isNumberOrEmpty(String text) {
            if (text.isEmpty()) {
                return true;
            }
            try {
                Double.parseDouble(text);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
            if (super.mouseClicked(click, doubled)) {
                return true;
            }
            if (!this.editing && click.button() == 0) {
                AngleSnap.setCurrentCameraPos(this.pos);
                return true;
            }
            return false;
        }
    }

    public class AddAngleEntry extends AbstractEntry {
        private final Button add;

        public AddAngleEntry() {
            this.add = this.addChild(new IconButtonWidget(ADD_TEXT, button -> this.add(), ADD_TEXTURE));
        }

        @Override
        public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            int x = this.getX();
            int y = this.getY();
            int entryHeight = this.getHeight();
            this.renderWidgetAt(context, mouseX, mouseY, deltaTicks, this.add, x + 5, y + (entryHeight - this.client.font.lineHeight) / 2);
        }

        private void add() {
            Entry entry = new Entry();
            CameraSnapListWidget.this.removeEntry(this);
            CameraSnapListWidget.this.addEntry(entry);
            CameraSnapListWidget.this.addEntry(this);
            CameraSnapListWidget.this.setFocused(entry);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
            return super.mouseClicked(click, doubled);
        }
    }
}
