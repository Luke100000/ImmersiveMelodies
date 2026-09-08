package immersive_melodies.client.gui.widget;

import immersive_melodies.client.gui.ImmersiveMelodiesScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.Objects;

public class MelodyListWidget extends ObjectSelectionList<MelodyListWidget.MelodyEntry> {
    private final ImmersiveMelodiesScreen currentScreen;
    private final boolean showSelection;

    public MelodyListWidget(Minecraft client, ImmersiveMelodiesScreen currentScreen, int left, int width, int listY, int listH, boolean showSelection) {
        super(client, width, listH, listY, 10);

        this.currentScreen = currentScreen;

        this.setX(left);

        this.showSelection = showSelection;

    }

    @Override
    protected void extractListBackground(GuiGraphicsExtractor guiGraphics) {
        // Nop
    }

    @Override
    public void clearEntries() {
        super.clearEntries();
    }

    public void addEntry(Identifier identifier, Component name, Runnable onPress) {
        super.addEntry(new MelodyEntry(identifier, name, onPress));
    }

    @Override
    public void replaceEntries(Collection<MelodyEntry> newEntries) {
        super.replaceEntries(newEntries);
    }

    @Override
    protected int scrollBarX() {
        return getX() + width + 2;
    }

    @Override
    public int getRowWidth() {
        return width;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= getX() && mouseX <= getX() + getWidth() + width + 10 && mouseY >= this.getY() && mouseY <= this.getY() + getHeight();
    }

    @Override
    protected void enableScissor(GuiGraphicsExtractor context) {
        context.enableScissor(getX() - 15, getY(), getX() + getWidth(), getY() + getHeight());
    }

    @Override
    protected void extractSelection(GuiGraphicsExtractor context, MelodyEntry entry, int outlineColor) {
        if (showSelection) {
            context.fill(getX() - 1, entry.getY() - 1, getX() + width, entry.getY() + entry.getContentHeight() + 3, 0x40000000);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        this.setScrollAmount(this.scrollAmount() - scrollY * (double) this.defaultEntryHeight);
        return true;
    }

    public class MelodyEntry extends ObjectSelectionList.Entry<MelodyEntry> {
        final Identifier identifier;
        final Component name;
        final Runnable onPress;

        public MelodyEntry(Identifier identifier, Component melody, Runnable onPress) {
            this.identifier = identifier;
            this.name = melody;
            this.onPress = onPress;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.text(currentScreen.getTextRenderer(), name, getX() + (onPress == null ? -2 : 2), getY() + 1, 0xFF404040, false);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            if (event.button() == 0 && onPress != null) {
                onPress.run();
                return true;
            }
            return false;
        }

        @Override
        public Component getNarration() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MelodyEntry that)) return false;
            return Objects.equals(identifier, that.identifier);
        }

        @Override
        public int hashCode() {
            return Objects.hash(identifier);
        }
    }
}
