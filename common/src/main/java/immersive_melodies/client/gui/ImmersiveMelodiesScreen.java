package immersive_melodies.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import immersive_melodies.Config;
import immersive_melodies.client.gui.widget.MelodyListWidget;
import immersive_melodies.client.gui.widget.TexturedButtonWidget;
import immersive_melodies.cobalt.network.NetworkHandler;
import immersive_melodies.network.c2s.ItemActionMessage;
import immersive_melodies.network.c2s.MelodyDeleteRequest;
import immersive_melodies.network.c2s.UploadMelodyRequest;
import immersive_melodies.resources.ClientMelodyManager;
import immersive_melodies.resources.Melody;
import immersive_melodies.resources.MelodyDescriptor;
import immersive_melodies.util.MidiConverter;
import immersive_melodies.util.MidiParser;
import immersive_melodies.util.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ImmersiveMelodiesScreen extends Screen {
    public static final Identifier BACKGROUND_TEXTURE = new Identifier("immersive_melodies:textures/gui/paper.png");
    private MelodyListWidget list;
    private TextFieldWidget search;

    @Nullable
    private Identifier selected = null;

    public ImmersiveMelodiesScreen() {
        super(new TranslatableText("itemGroup.immersive_melodies.immersive_melodies_tab"));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    protected void init() {
        this.search = new TextFieldWidget(this.textRenderer, this.width / 2 - 70, this.height / 2 - 103, 140, 20, new TranslatableText("immersive_melodies.search"));
        this.search.setMaxLength(128);
        this.search.setChangedListener(a -> {
            this.refreshPage();
            this.search.setSuggestion(null);
        });
        this.search.setDrawsBackground(false);
        this.search.setEditableColor(0x808080);
        this.search.setSuggestion("Search");
        setInitialFocus(this.search);

        list = new MelodyListWidget(this.client, this);

        refreshPage();
    }

    private void openHelp() {
        try {
            Util.getOperatingSystem().open(URI.create("https://github.com/Luke100000/ImmersiveMelodies/wiki/Custom-Melodies"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void filesDragged(List<Path> paths) {
        PathMatcher midiMatcher = FileSystems.getDefault().getPathMatcher("glob:*{.mid,.midi,.MID,.MIDI}");
        PathMatcher abcMatcher = FileSystems.getDefault().getPathMatcher("glob:*{.abc,.ABC}");
        for (Path path : paths) {
            try {
                String rawName = path.getFileName().toString();
                String name = rawName.substring(0, rawName.lastIndexOf('.'));
                if (midiMatcher.matches(path.getFileName())) {
                    // This is a midi file, parse it
                    InputStream inputStream = new FileInputStream(path.toFile());
                    parseMidi(name, inputStream);
                } else if (abcMatcher.matches(path.getFileName())) {
                    // This is an abc file, convert it to midi and then parse it
                    byte[] bytes = Files.readAllBytes(path);
                    MinecraftClient.getInstance().execute(() -> {
                        try {
                            MidiConverter.Response request = MidiConverter.request(bytes);
                            ByteArrayInputStream inputStream = new ByteArrayInputStream(request.getBody());
                            parseMidi(name, inputStream);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void parseMidi(String name, InputStream inputStream) {
        List<Melody> melodies = MidiParser.parseMidi(inputStream, name, Config.getInstance().parseAllMidiTracks);
        if (Config.getInstance().parseAllMidiTracks) {
            // Use all tracks and just add a track name prefix
            int i = 0;
            for (Melody melody : melodies) {
                NetworkHandler.sendToServer(new UploadMelodyRequest(name + i++, melody));
                search.setText(name);
            }
        } else {
            // Only use the track with the most notes
            melodies.stream().max(Comparator.comparingInt(m -> m.getNotes().size())).ifPresent(melody -> {
                NetworkHandler.sendToServer(new UploadMelodyRequest(name, melody));
                search.setText(name);
            });
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        int x = (this.width - 192) / 2;
        int y = (this.height - 230) / 2;

        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        drawTexture(matrices, x, y, 0, 0, 192, 215);

        super.render(matrices, mouseX, mouseY, delta);
    }

    public void refreshPage() {
        clearChildren();

        addDrawableChild(search);
        addDrawableChild(list);

        list.children().clear();
        String lastPath = "";
        for (Map.Entry<Identifier, MelodyDescriptor> entry : ClientMelodyManager.getMelodiesList().entrySet().stream()
                .filter(e -> this.search.getText().isEmpty() || e.getValue().getName().toLowerCase(Locale.ROOT).contains(this.search.getText().toLowerCase(Locale.ROOT)))
                .sorted((a, b) -> {
                    int primarySortA = Utils.ownsMelody(a.getKey(), MinecraftClient.getInstance().player) ? 2 : Utils.isPlayerMelody(a.getKey()) ? 0 : 1;
                    int primarySortB = Utils.ownsMelody(b.getKey(), MinecraftClient.getInstance().player) ? 2 : Utils.isPlayerMelody(b.getKey()) ? 0 : 1;
                    if (primarySortA != primarySortB) {
                        return primarySortB - primarySortA;
                    } else {
                        return a.getValue().getName().compareTo(b.getValue().getName());
                    }
                })
                .toList()) {

            String dir = Utils.removeLastPart(entry.getKey().getPath(), "/");
            String path = entry.getKey().getNamespace() + "/" + dir;

            if (!path.equals(lastPath)) {
                list.addEntry(new Identifier(path), new LiteralText(dir).formatted(Formatting.ITALIC).formatted(Formatting.GRAY), null);
                lastPath = path;
            }

            list.addEntry(entry.getKey(), new LiteralText(entry.getValue().getName()), () -> {
                NetworkHandler.sendToServer(new ItemActionMessage(ItemActionMessage.State.PLAY, entry.getKey()));
                selected = entry.getKey();
                refreshPage();
            });
        }

        int y = this.height / 2 + 69;

        // Close
        addDrawableChild(new TexturedButtonWidget(width / 2 - 75, y, 16, 16, BACKGROUND_TEXTURE, 256 - 16, 0, 256, 256, Text.of(null), button -> {
            close();
        }, (ButtonWidget b, MatrixStack matrixStack, int mx, int my) -> renderTooltip(matrixStack, new TranslatableText("immersive_melodies.close"), mx, my)));

        // Delete
        if (selected != null && (Utils.canDelete(selected, MinecraftClient.getInstance().player))) {
            addDrawableChild(new TexturedButtonWidget(width / 2 + 30, y, 16, 16, BACKGROUND_TEXTURE, 256 - 16, 16, 256, 256, Text.of(null), button -> {
                NetworkHandler.sendToServer(new MelodyDeleteRequest(selected));
                selected = null;
            }, (ButtonWidget b, MatrixStack matrixStack, int mx, int my) -> renderTooltip(matrixStack, new TranslatableText("immersive_melodies.delete"), mx, my)));
        }

        // Pause
        addDrawableChild(new TexturedButtonWidget(width / 2 - 10 - 8, y, 16, 16, BACKGROUND_TEXTURE, 256 - 32, 32, 256, 256, Text.of(null), button -> {
            NetworkHandler.sendToServer(new ItemActionMessage(ItemActionMessage.State.PAUSE));
        }, (ButtonWidget b, MatrixStack matrixStack, int mx, int my) -> renderTooltip(matrixStack, new TranslatableText("immersive_melodies.pause"), mx, my)));

        // Play
        addDrawableChild(new TexturedButtonWidget(width / 2, y, 16, 16, BACKGROUND_TEXTURE, 256 - 16, 32, 256, 256, Text.of(null), button -> {
            NetworkHandler.sendToServer(new ItemActionMessage(ItemActionMessage.State.CONTINUE));
        }, (ButtonWidget b, MatrixStack matrixStack, int mx, int my) -> renderTooltip(matrixStack, new TranslatableText("immersive_melodies.play"), mx, my)));

        // Help
        addDrawableChild(new TexturedButtonWidget(width / 2 + 50, y, 16, 16, BACKGROUND_TEXTURE, 256 - 48, 32, 256, 256, Text.of(null), button -> {
            openHelp();
        }, (ButtonWidget b, MatrixStack matrixStack, int mx, int my) -> renderTooltip(matrixStack, new TranslatableText("immersive_melodies.help"), mx, my)));
    }

    public TextRenderer getTextRenderer() {
        return this.textRenderer;
    }
}
