package immersive_melodies.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import immersive_melodies.Client;
import immersive_melodies.Config;
import immersive_melodies.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static immersive_melodies.client.gui.ImmersiveMelodiesScreen.BACKGROUND_TEXTURE;

public class ImmersiveMelodiesFreePlayingScreen extends Screen {
    public static final MutableComponent TEXT = Component.translatable("immersive_melodies.free_playing");

    private static final int KEY_WIDTH = 36;
    private static final int KEY_HEIGHT = 28;
    private static final int MAX_KEY_SPACING = 42;
    private static final int SHARP_ROW_OFFSET = -18;
    private static final int NATURAL_ROW_OFFSET = 16;

    private final Set<Integer> pressedKeys = new HashSet<>();
    private Integer pressedMouseKey;
    private KeyLayout keyLayout;

    protected ImmersiveMelodiesFreePlayingScreen() {
        super(TEXT);
    }

    @Override
    protected void init() {
        super.init();
        keyLayout = createKeyLayout(mappings());

        // Exit
        addRenderableWidget(new TexturedButtonWidget(width / 2 - 8, height / 2 + 58, 16, 16, BACKGROUND_TEXTURE, 256 - 16, 16, 256, 256, Component.nullToEmpty(null), button -> {
            onClose();
        }, () -> List.of(Component.translatable("immersive_melodies.close").getVisualOrderText())));
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
        // Nop
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        List<Map.Entry<Integer, Integer>> mappings = mappings();

        context.drawCenteredString(
                this.font,
                TEXT,
                this.width / 2,
                this.height / 2 - 50,
                0xFFFFFF
        );

        if (!mappings.isEmpty()) {
            for (Map.Entry<Integer, Integer> entry : mappings) {
                int keyCode = entry.getKey();
                int midi = entry.getValue();
                renderKey(context, keyLayout.x(midi), keyLayout.y(midi), keyCode, midi, pressedKeys.contains(keyCode) || pressedMouseKey != null && pressedMouseKey == keyCode);
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderKey(GuiGraphics context, int x, int y, int keyCode, int midi, boolean pressed) {
        int borderColor = pressed ? 0xFFFFE59A : 0xFF8A8A8A;
        int backgroundColor = pressed ? 0xFFE2B84B : 0xCC171717;
        int keyColor = pressed ? 0xFF241B0A : 0xFFFFFFFF;
        int noteColor = pressed ? 0xFF3D2C0B : 0xFFC8C8C8;

        context.fill(x - 1, y - 1, x + KEY_WIDTH + 1, y + KEY_HEIGHT + 1, borderColor);
        context.fill(x, y, x + KEY_WIDTH, y + KEY_HEIGHT, backgroundColor);

        Component keyName = InputConstants.Type.KEYSYM.getOrCreate(keyCode).getDisplayName();
        context.drawCenteredString(this.font, keyName, x + KEY_WIDTH / 2, y + 4, keyColor);
        context.drawCenteredString(this.font, midiName(midi), x + KEY_WIDTH / 2, y + 16, noteColor);
    }

    private static String midiName(int midi) {
        String[] names = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
        return names[Math.floorMod(midi, 12)] + (Math.floorDiv(midi, 12) - 1);
    }

    private static boolean isSharp(int midi) {
        return switch (Math.floorMod(midi, 12)) {
            case 1, 3, 6, 8, 10 -> true;
            default -> false;
        };
    }

    private static double notePosition(int midi) {
        int octave = Math.floorDiv(midi, 12);
        return octave * 7 + switch (Math.floorMod(midi, 12)) {
            case 0 -> 0.0;
            case 1 -> 0.5;
            case 2 -> 1.0;
            case 3 -> 1.5;
            case 4 -> 2.0;
            case 5 -> 3.0;
            case 6 -> 3.5;
            case 7 -> 4.0;
            case 8 -> 4.5;
            case 9 -> 5.0;
            case 10 -> 5.5;
            case 11 -> 6.0;
            default -> throw new IllegalStateException();
        };
    }

    private Integer keyAt(double mouseX, double mouseY) {
        for (Map.Entry<Integer, Integer> entry : mappings()) {
            int midi = entry.getValue();
            int x = keyLayout.x(midi);
            int y = keyLayout.y(midi);
            if (mouseX >= x && mouseX < x + KEY_WIDTH && mouseY >= y && mouseY < y + KEY_HEIGHT) {
                return entry.getKey();
            }
        }
        return null;
    }

    private static List<Map.Entry<Integer, Integer>> mappings() {
        List<Map.Entry<Integer, Integer>> mappings = new ArrayList<>(Config.getInstance().keycodeToMidi.entrySet());
        mappings.sort(Comparator.comparingInt(Map.Entry::getValue));
        return mappings;
    }

    private KeyLayout createKeyLayout(List<Map.Entry<Integer, Integer>> mappings) {
        double minPosition = mappings.stream().mapToDouble(entry -> notePosition(entry.getValue())).min().orElse(0.0);
        double maxPosition = mappings.stream().mapToDouble(entry -> notePosition(entry.getValue())).max().orElse(minPosition);
        double positionRange = Math.max(1.0, maxPosition - minPosition);
        int availableWidth = Math.max(KEY_WIDTH, this.width - 32);
        int keySpacing = Math.min(MAX_KEY_SPACING, Math.max(18, (int) ((availableWidth - KEY_WIDTH) / positionRange)));
        int totalWidth = (int) Math.round((maxPosition - minPosition) * keySpacing) + KEY_WIDTH;
        return new KeyLayout(minPosition, keySpacing, this.width / 2 - totalWidth / 2, this.height / 2);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        Integer midi = Config.getInstance().keycodeToMidi.get(keyCode);
        if (midi != null) {
            if (pressedKeys.add(keyCode)) {
                Client.playNote(midi, 127);
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        Integer midi = Config.getInstance().keycodeToMidi.get(keyCode);
        if (midi != null) {
            pressedKeys.remove(keyCode);
            Client.playNote(midi, 0);
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        Integer keyCode = keyAt(mouseX, mouseY);
        if (keyCode != null) {
            pressedMouseKey = keyCode;
            Client.playNote(Config.getInstance().keycodeToMidi.get(keyCode), 127);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (pressedMouseKey != null) {
            Client.playNote(Config.getInstance().keycodeToMidi.get(pressedMouseKey), 0);
            pressedMouseKey = null;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void removed() {
        for (int keyCode : pressedKeys) {
            Integer midi = Config.getInstance().keycodeToMidi.get(keyCode);
            if (midi != null) {
                Client.playNote(midi, 0);
            }
        }
        if (pressedMouseKey != null) {
            Client.playNote(Config.getInstance().keycodeToMidi.get(pressedMouseKey), 0);
        }
        pressedKeys.clear();
        pressedMouseKey = null;
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record KeyLayout(double minPosition, int keySpacing, int startX, int centerY) {
        private int x(int midi) {
            return startX + (int) Math.round((notePosition(midi) - minPosition) * keySpacing);
        }

        private int y(int midi) {
            return centerY + (isSharp(midi) ? SHARP_ROW_OFFSET : NATURAL_ROW_OFFSET);
        }
    }
}
