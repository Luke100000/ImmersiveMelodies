package immersive_melodies.client.gui.widget;

import immersive_melodies.client.gui.ImmersiveMelodiesScreen;
import immersive_melodies.cobalt.network.NetworkHandler;
import immersive_melodies.network.c2s.ItemActionMessage;
import immersive_melodies.resources.MelodyDescriptor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Objects;

public class MelodyListWidget extends AlwaysSelectedEntryListWidget<MelodyListWidget.MelodyEntry> {
    private final ImmersiveMelodiesScreen currentScreen;

    public MelodyListWidget(MinecraftClient client, ImmersiveMelodiesScreen currentScreen) {
        super(client, currentScreen.width, currentScreen.height, 40, 200, 10);

        this.currentScreen = currentScreen;

        setRenderBackground(false);
        setRenderHorizontalShadows(false);
        setRenderHeader(false, 0);
    }

    @Override
    public void clearEntries() {
        super.clearEntries();
    }

    public void addEntry(Identifier identifier, MelodyDescriptor descriptor, Runnable onPress) {
        super.addEntry(new MelodyEntry(identifier, descriptor, onPress));
    }

    @Override
    public void replaceEntries(Collection<MelodyEntry> newEntries) {
        super.replaceEntries(newEntries);
    }

    @Override
    protected int getScrollbarPositionX() {
        return super.getScrollbarPositionX() - 50;
    }

    @Override
    public int getRowWidth() {
        return 160;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= currentScreen.width / 2.0 - 120 && mouseX <= currentScreen.width / 2.0 + 120 && mouseY >= this.top && mouseY <= this.bottom;
    }

    @Override
    protected void enableScissor(DrawContext context) {
        context.enableScissor(currentScreen.width / 2 - 100, this.top, currentScreen.width / 2 + 70, this.bottom);
    }

    @Override
    protected void drawSelectionHighlight(DrawContext context, int y, int entryWidth, int entryHeight, int borderColor, int fillColor) {
        int x0 = currentScreen.width / 2 - 75;
        int x1 = currentScreen.width / 2 + 80;
        context.fill(x0 - 1, y - 1, x1, y + entryHeight + 3, 0x40000000);
    }

    public class MelodyEntry extends AlwaysSelectedEntryListWidget.Entry<MelodyEntry> {
        final Identifier identifier;
        final MelodyDescriptor melody;
        final Runnable onPress;

        public MelodyEntry(Identifier identifier, MelodyDescriptor melody, Runnable onPress) {
            this.identifier = identifier;
            this.melody = melody;
            this.onPress = onPress;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawText(currentScreen.getTextRenderer(), melody.getName(), currentScreen.width / 2 - 75, y + 1, 0x404040, false);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                onPressed();
                return true;
            }
            return false;
        }

        void onPressed() {
            NetworkHandler.sendToServer(new ItemActionMessage(ItemActionMessage.State.PLAY, identifier));
            onPress.run();
        }

        @Override
        public Text getNarration() {
            return Text.translatable("narrator.select", melody.getName());
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