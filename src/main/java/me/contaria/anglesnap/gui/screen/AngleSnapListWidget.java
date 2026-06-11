package me.contaria.anglesnap.gui.screen;

import me.contaria.anglesnap.AngleEntry;
import me.contaria.anglesnap.AngleSnap;
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
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AngleSnapListWidget extends ContainerObjectSelectionList<AngleSnapListWidget.AbstractEntry> {
    private static final Component NAME_TEXT = Component.translatable("anglesnap.gui.screen.name");
    private static final Component YAW_TEXT = Component.translatable("anglesnap.gui.screen.yaw");
    private static final Component PITCH_TEXT = Component.translatable("anglesnap.gui.screen.pitch");
    private static final Component ICON_TEXT = Component.translatable("anglesnap.gui.screen.icon");
    private static final Component COLOR_TEXT = Component.translatable("anglesnap.gui.screen.color");
    private static final Component ADD_TEXT = Component.translatable("anglesnap.gui.screen.add");
    private static final Component DELETE_TEXT = Component.translatable("anglesnap.gui.screen.delete");
    private static final Component EDIT_TEXT = Component.translatable("anglesnap.gui.screen.edit");
    private static final Component SAVE_TEXT = Component.translatable("anglesnap.gui.screen.save");

    private static final Identifier ADD_TEXTURE = Identifier.fromNamespaceAndPath("anglesnap", "textures/gui/add.png");
    private static final Identifier DELETE_TEXTURE = Identifier.fromNamespaceAndPath("anglesnap", "textures/gui/delete.png");
    private static final Identifier EDIT_TEXTURE = Identifier.fromNamespaceAndPath("anglesnap", "textures/gui/edit.png");
    private static final Identifier SAVE_TEXTURE = Identifier.fromNamespaceAndPath("anglesnap", "textures/gui/save.png");

    private static final int HOVERED_COLOR = net.minecraft.util.ARGB.color(100, 200, 200, 200);

    private final AngleSnapScreen parent;

    public AngleSnapListWidget(Minecraft minecraftClient, int width, int height, int y, AngleSnapScreen parent) {
        super(minecraftClient, width, height, y, 20);
        this.parent = parent;

        for (AngleEntry angle : AngleSnap.CONFIG.getAngles()) {
            this.addEntry(new Entry(angle));
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
        context.text(textRenderer, YAW_TEXT, x + 5 + 5 * this.getRowWidth() / 17, y + (20 - textRenderer.lineHeight) / 2, CommonColors.WHITE, true);
        context.text(textRenderer, PITCH_TEXT, x + 5 + 7 * this.getRowWidth() / 17, y + (20 - textRenderer.lineHeight) / 2, CommonColors.WHITE, true);
        context.text(textRenderer, ICON_TEXT, x + 5 + 9 * this.getRowWidth() / 17, y + (20 - textRenderer.lineHeight) / 2, CommonColors.WHITE, true);
        context.text(textRenderer, COLOR_TEXT, x + 5 + 11 * this.getRowWidth() / 17, y + (20 - textRenderer.lineHeight) / 2, CommonColors.WHITE, true);
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
        private final AngleEntry angle;

        private final EditBox name;
        private final EditBox yaw;
        private final EditBox pitch;
        private final IconButtonWidget icon;
        private final EditBox color;
        private final IconButtonWidget edit;
        private final IconButtonWidget save;
        private final IconButtonWidget delete;

        private boolean editing;

        public Entry() {
            this(AngleSnap.CONFIG.createAngle());
            this.setEditing(true);
            this.setFocused(this.name);
            this.name.setFocused(true);
        }

        public Entry(AngleEntry angle) {
            if (angle == null) {
                throw new IllegalStateException("Cannot create an angle entry before angle data is loaded");
            }
            this.angle = angle;

            this.name = this.addChild(new EditBox(
                    this.client.font,
                    5 * AngleSnapListWidget.this.getRowWidth() / 17 - 5,
                    20,
                    NAME_TEXT
            ));
            this.name.setValue(this.angle.name);
            this.name.setResponder(name -> this.angle.name = name);
            this.name.setBordered(false);
            this.name.setTextColor(CommonColors.WHITE);
            this.name.setTextColorUneditable(CommonColors.WHITE);

            this.yaw = this.addChild(new EditBox(
                    this.client.font,
                    2 * AngleSnapListWidget.this.getRowWidth() / 17 - 5,
                    20,
                    YAW_TEXT
            ));
            this.yaw.setValue(String.valueOf(this.angle.yaw));
            this.yaw.setResponder(yaw -> {
                try {
                    this.angle.yaw = Float.parseFloat(yaw);
                } catch (NumberFormatException e) {
                    this.angle.yaw = 0.0f;
                }
            });
            this.yaw.setBordered(false);
            this.yaw.setTextColor(CommonColors.WHITE);
            this.yaw.setTextColorUneditable(CommonColors.WHITE);

            this.pitch = this.addChild(new EditBox(
                    this.client.font,
                    2 * AngleSnapListWidget.this.getRowWidth() / 17 - 5,
                    20,
                    PITCH_TEXT
            ));
            this.pitch.setValue(String.valueOf(this.angle.pitch));
            this.pitch.setResponder(pitch -> {
                try {
                    this.angle.pitch = Float.parseFloat(pitch);
                } catch (NumberFormatException e) {
                    this.angle.pitch = 0.0f;
                }
            });
            this.pitch.setBordered(false);
            this.pitch.setTextColor(CommonColors.WHITE);
            this.pitch.setTextColorUneditable(CommonColors.WHITE);

            this.icon = this.addChild(new IconButtonWidget(Component.empty(), button -> ((IconButtonWidget) button).setTexture(this.angle.nextIcon()), this.angle.getIcon()));

            this.color = this.addChild(new EditBox(
                    this.client.font,
                    3 * AngleSnapListWidget.this.getRowWidth() / 17 - 5,
                    20,
                    COLOR_TEXT
            ));
            this.color.setValue(colorToString(this.angle.color));
            this.color.setResponder(color -> this.angle.color = this.parseColor(color));
            this.color.setMaxLength(9);
            this.color.setBordered(false);
            this.color.setTextColor(CommonColors.WHITE);
            this.color.setTextColorUneditable(CommonColors.WHITE);

            this.edit = this.addChild(new IconButtonWidget(EDIT_TEXT, button -> this.toggleEditing(), EDIT_TEXTURE));
            this.save = this.addChild(new IconButtonWidget(SAVE_TEXT, button -> this.toggleEditing(), SAVE_TEXTURE));
            this.delete = this.addChild(new IconButtonWidget(DELETE_TEXT, button -> this.delete(), DELETE_TEXTURE));

            this.setEditing(false);
        }

        private String colorToString(int argb) {
            int r = ARGB.red(argb);
            int g = ARGB.green(argb);
            int b = ARGB.blue(argb);
            int a = ARGB.alpha(argb);
            return String.format("#%02X%02X%02X%02X", r, g, b, a);
        }

        private int parseColor(String color) {
            if (color.startsWith("#")) {
                color = color.substring(1);
            }
            color = color.toLowerCase(Locale.ROOT);

            try {
                String hex = StringUtils.leftPad(color, 6, '0');
                if (hex.length() == 6) {
                    int rgb = Integer.parseUnsignedInt(hex, 16);
                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;
                    return ARGB.color(255, r, g, b);
                }

                hex = StringUtils.rightPad(hex, 8, 'f');
                int rgba = Integer.parseUnsignedInt(hex, 16);
                int r = (rgba >> 24) & 0xFF;
                int g = (rgba >> 16) & 0xFF;
                int b = (rgba >> 8) & 0xFF;
                int a = rgba & 0xFF;
                return ARGB.color(a, r, g, b);
            } catch (NumberFormatException e) {
                return CommonColors.WHITE;
            }
        }

        private void toggleEditing() {
            boolean wasEditing = this.editing;
            this.setEditing(!this.editing);
            if (wasEditing) {
                AngleSnap.CONFIG.saveAngles();
            }
        }

        private void setEditing(boolean editing) {
            this.editing = editing;

            this.setEditing(this.name, editing);
            this.setEditing(this.yaw, editing);
            this.setEditing(this.pitch, editing);
            this.setEditing(this.icon, editing);
            this.setEditing(this.color, editing);

            if (!editing) {
                this.yaw.setValue(String.valueOf(this.angle.yaw));
                this.pitch.setValue(String.valueOf(this.angle.pitch));
                this.color.setValue(this.colorToString(this.angle.color));
            }

            this.edit.visible = !editing;
            this.save.visible = editing;
        }

        private void setEditing(AbstractWidget widget, boolean editing) {
            widget.active = editing;
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
            AngleSnap.CONFIG.removeAngle(this.angle);
            AngleSnap.CONFIG.saveAngles();
            AngleSnapListWidget.this.removeEntry(this);
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
            this.renderNumberWidgetAt(context, mouseX, mouseY, deltaTicks, this.yaw, x + 5 + 5 * entryWidth / 17, textY);
            this.renderNumberWidgetAt(context, mouseX, mouseY, deltaTicks, this.pitch, x + 5 + 7 * entryWidth / 17, textY);
            this.renderWidgetAt(context, mouseX, mouseY, deltaTicks, this.icon, x + 5 + 9 * entryWidth / 17, y);
            this.renderHexadecimalWidgetAt(context, mouseX, mouseY, deltaTicks, this.color, x + 5 + 11 * entryWidth / 17, textY);
            this.renderWidgetAt(context, mouseX, mouseY, deltaTicks, this.edit, x + entryWidth - 5 - 40, y);
            this.renderWidgetAt(context, mouseX, mouseY, deltaTicks, this.save, x + entryWidth - 5 - 40, y);
            this.renderWidgetAt(context, mouseX, mouseY, deltaTicks, this.delete, x + entryWidth - 5 - 20, y);
        }

        private void renderStringWidgetAt(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks, EditBox widget, int x, int y) {
            if (this.editing) {
                this.renderWidgetAt(context, mouseX, mouseY, deltaTicks, widget, x, y);
            } else {
                context.text(this.client.font, widget.getValue(), x, y, CommonColors.WHITE, true);
            }
        }

        private void renderNumberWidgetAt(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks, EditBox widget, int x, int y) {
            if (this.isNumberOrEmpty(widget.getValue())) {
                this.renderStringWidgetAt(context, mouseX, mouseY, deltaTicks, widget, x, y);
            } else {
                widget.setTextColor(CommonColors.SOFT_RED);
                widget.setTextColorUneditable(CommonColors.SOFT_RED);
                this.renderStringWidgetAt(context, mouseX, mouseY, deltaTicks, widget, x, y);
                widget.setTextColor(CommonColors.WHITE);
                widget.setTextColorUneditable(CommonColors.WHITE);
            }
        }

        private boolean isNumberOrEmpty(String text) {
            if (text.isEmpty()) {
                return true;
            }
            try {
                Float.parseFloat(text);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        private void renderHexadecimalWidgetAt(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks, EditBox widget, int x, int y) {
            if (this.isHexadecimalOrEmpty(widget.getValue())) {
                this.renderStringWidgetAt(context, mouseX, mouseY, deltaTicks, widget, x, y);
            } else {
                widget.setTextColor(CommonColors.SOFT_RED);
                widget.setTextColorUneditable(CommonColors.SOFT_RED);
                this.renderStringWidgetAt(context, mouseX, mouseY, deltaTicks, widget, x, y);
                widget.setTextColor(CommonColors.WHITE);
                widget.setTextColorUneditable(CommonColors.WHITE);
            }
        }

        private boolean isHexadecimalOrEmpty(String text) {
            if (text.startsWith("#")) {
                text = text.substring(1);
            }
            if (text.isEmpty()) {
                return true;
            }
            text = text.toLowerCase(Locale.ROOT);
            try {
                // noinspection ResultOfMethodCallIgnored
                Integer.parseUnsignedInt(text, 16);
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
                AngleSnapListWidget.this.parent.snap(this.angle);
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
            AngleSnapListWidget.this.removeEntry(this);
            AngleSnapListWidget.this.addEntry(entry);
            AngleSnapListWidget.this.addEntry(this);
            AngleSnapListWidget.this.setFocused(entry);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
            return super.mouseClicked(click, doubled);
        }
    }
}
