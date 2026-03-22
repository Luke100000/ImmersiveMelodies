package immersive_melodies.client.gui;

import immersive_melodies.Client;
import immersive_melodies.Config;
import immersive_melodies.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

import static immersive_melodies.client.gui.ImmersiveMelodiesScreen.BACKGROUND_TEXTURE;

public class ImmersiveMelodiesFreePlayingScreen extends Screen {
    public static final MutableComponent TEXT = Component.translatable("immersive_melodies.free_playing");

    protected ImmersiveMelodiesFreePlayingScreen() {
        super(TEXT);
    }

    @Override
    protected void init() {
        super.init();

        // Exit
        addRenderableWidget(new TexturedButtonWidget(width / 2, height / 2 + 50, 16, 16, BACKGROUND_TEXTURE, 256 - 16, 16, 256, 256, Component.nullToEmpty(null), button -> {
            onClose();
        }, () -> List.of(Component.translatable("immersive_melodies.close").getVisualOrderText())));
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        // Hint
        context.drawCenteredString(
                this.font,
                TEXT,
                this.width / 2,
                this.height / 2 + 70,
                0xFFFFFF
        );

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (Config.getInstance().keycodeToMidi.containsKey(keyCode)) {
            int midi = Config.getInstance().keycodeToMidi.get(keyCode);
            Client.playNote(midi, 127);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (Config.getInstance().keycodeToMidi.containsKey(keyCode)) {
            int midi = Config.getInstance().keycodeToMidi.get(keyCode);
            Client.playNote(midi, 0);
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
