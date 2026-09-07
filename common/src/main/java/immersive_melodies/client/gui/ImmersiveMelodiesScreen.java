package immersive_melodies.client.gui;

import immersive_melodies.Common;
import immersive_melodies.Config;
import immersive_melodies.client.gui.widget.MelodyListWidget;
import immersive_melodies.client.gui.widget.TexturedButtonWidget;
import immersive_melodies.item.InstrumentItem;
import immersive_melodies.network.Network;
import immersive_melodies.network.PacketSplitter;
import immersive_melodies.network.c2s.ItemActionMessage;
import immersive_melodies.network.c2s.MelodyDeleteRequest;
import immersive_melodies.network.c2s.TrackToggleMessage;
import immersive_melodies.resources.ClientMelodyManager;
import immersive_melodies.resources.Melody;
import immersive_melodies.resources.MelodyDescriptor;
import immersive_melodies.resources.Track;
import immersive_melodies.util.MidiConverter;
import immersive_melodies.util.MidiParser;
import immersive_melodies.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.*;

public class ImmersiveMelodiesScreen extends Screen {
    public static final ResourceLocation BACKGROUND_TEXTURE = Common.locate("textures/gui/paper.png");
    private MelodyListWidget list;
    private MelodyListWidget trackList;
    private EditBox search;

    private Component error;
    private long lastError;
    private boolean showTrackSelection;
    private List<Integer> enabledTracks = new ArrayList<>();
    private ResourceLocation selected;

    private void setError(Component error) {
        this.error = error;
        this.lastError = System.currentTimeMillis();
        this.search.setValue("");
        this.search.setSuggestion(null);
    }

    public ImmersiveMelodiesScreen() {
        super(Component.translatable("itemGroup.immersive_melodies.immersive_melodies_tab"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        this.search = new EditBox(this.font, this.width / 2 - 70, this.height / 2 - 103, 140, 20, Component.translatable("immersive_melodies.search"));
        this.search.setMaxLength(128);
        this.search.setResponder(search -> {
            this.refreshPage();
            this.search.setSuggestion(null);
        });
        this.search.setBordered(false);
        this.search.setTextColor(0x808080);
        this.search.setSuggestion("Search");
        setInitialFocus(this.search);

        int y = (height - 230) / 2 + 22;
        list = new MelodyListWidget(this.minecraft, this, this.width / 2 - 75, 150, y, 162, true);
        trackList = new MelodyListWidget(this.minecraft, this, this.width / 2 + 99, 92, y + 8, 142, false);

        refreshPage();

        // Select the current melody
        ItemStack stack = getInstrumentStack();
        if (stack.getItem() instanceof InstrumentItem item) {
            selected = item.getMelody(stack);
        }
    }

    private void updateTrackList() {
        ItemStack stack = getInstrumentStack();
        if (stack.getItem() instanceof InstrumentItem item) {
            List<Integer> newEnabledTracks = item.getEnabledTracks(stack);
            if (!Objects.equals(newEnabledTracks, enabledTracks)) {
                enabledTracks = new ArrayList<>(newEnabledTracks);
                refreshPage();
            }
        } else {
            enabledTracks = new ArrayList<>();
        }
    }

    private ItemStack getInstrumentStack() {
        if (minecraft == null || minecraft.player == null) {
            return ItemStack.EMPTY;
        }

        ItemStack mainHand = minecraft.player.getMainHandItem();
        return mainHand.getItem() instanceof InstrumentItem ? mainHand : minecraft.player.getOffhandItem();
    }

    private void openHelp() {
        try {
            Util.getPlatform().openUri(URI.create("https://github.com/Luke100000/ImmersiveMelodies/wiki/Custom-Melodies"));
        } catch (Exception e) {
            Common.LOGGER.error(e);
        }
    }

    @Override
    public void onFilesDrop(List<Path> paths) {
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
                    Minecraft.getInstance().execute(() -> {
                        try {
                            MidiConverter.Response request = MidiConverter.request(bytes);
                            ByteArrayInputStream inputStream = new ByteArrayInputStream(request.getBody());
                            parseMidi(name, inputStream);
                        } catch (Exception e) {
                            Common.LOGGER.error(e);
                            setError(Component.translatable("immersive_melodies.error.empty"));
                        }
                    });
                } else {
                    setError(Component.translatable("immersive_melodies.error.unknown_file_type"));
                }
            } catch (Exception e) {
                Common.LOGGER.error(e);
                setError(Component.literal(e.getLocalizedMessage()));
            }
        }
    }

    private void parseMidi(String name, InputStream inputStream) {
        if (name.isEmpty()) {
            name = "empty";
        }

        Melody melody = MidiParser.parseMidi(inputStream, name);
        if (!melody.getTracks().isEmpty()) {
            PacketSplitter.sendToServer(name, melody);
            setSearch(name);
        } else {
            setError(Component.translatable("immersive_melodies.error.empty"));
        }
    }

    public void setSearch(String query) {
        search.setValue(query);
        list.setScrollAmount(0);
    }

    @Override
    public void tick() {
        super.tick();

        updateTrackList();
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);

        renderPaperBackground(context);
    }

    private void renderPaperBackground(GuiGraphics context) {
        // Draw the track selection background
        int cx = (this.width - 192) / 2;
        int cy = (this.height - 230) / 2;
        if (showTrackSelection) {
            int overlap = 10;
            int trackListWidth = 75;
            context.blit(BACKGROUND_TEXTURE, cx + 192 - overlap, cy + 8, 0, 0, 32, 100);
            context.blit(BACKGROUND_TEXTURE, cx + 192 - overlap, cy + 108, 0, 115, 32, 100);
            context.blit(BACKGROUND_TEXTURE, cx + 192 - overlap + 32, cy + 8, 192 - overlap - trackListWidth, 0, overlap + trackListWidth, 100);
            context.blit(BACKGROUND_TEXTURE, cx + 192 - overlap + 32, cy + 108, 192 - overlap - trackListWidth, 115, overlap + trackListWidth, 100);

            // Track selection title
            context.drawString(font, Component.translatable("immersive_melodies.tracks"), this.width / 2 + 100, this.height / 2 - 94, 0x000000, false);
        }

        // Draw background
        context.blit(BACKGROUND_TEXTURE, cx, cy, 0, 0, 192, 215);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        // Print help for noobs
        if (!Config.getInstance().clickedHelp) {
            context.renderTooltip(font, Component.translatable("immersive_melodies.read"), width / 2 + 55, height / 2 + 69 + 17);
        }

        // Print error
        if (error != null && System.currentTimeMillis() - lastError < 5000) {
            context.drawCenteredString(font, error, width / 2, this.height / 2 - 103, 0xFF0000);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    public void refreshPage() {
        clearWidgets();

        addRenderableWidget(search);
        addRenderableWidget(list);

        // Build the melody list
        list.clearEntries();
        String lastPath = "";
        for (Map.Entry<ResourceLocation, MelodyDescriptor> entry : ClientMelodyManager.getMelodiesList().entrySet().stream()
                .filter(e -> this.search.getValue().isEmpty() || e.getValue().getName().toLowerCase(Locale.ROOT).contains(this.search.getValue().toLowerCase(Locale.ROOT)))
                .sorted((a, b) -> {
                    int primarySortA = getSortIndex(a);
                    int primarySortB = getSortIndex(b);
                    if (primarySortA != primarySortB) {
                        return primarySortB - primarySortA;
                    } else {
                        return a.getKey().compareTo(b.getKey());
                    }
                })
                .toList()) {

            String dir = Utils.removeLastPart(entry.getKey().getPath(), "/");
            String path = entry.getKey().getNamespace() + "/" + dir;

            if (!path.equals(lastPath)) {
                list.addEntry(ResourceLocation.parse(path), Component.literal(dir).withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY), null);
                lastPath = path;
            }

            String key = entry.getKey().getPath().replace(".midi", "").replace(".mid", "");
            String[] split = key.split("/", 2);
            MutableComponent name = Component.translatableWithFallback("immersive_melodies.melodies." + split[split.length - 1], entry.getValue().getName());
            list.addEntry(entry.getKey(), name, () -> {
                Network.sendToServer(ItemActionMessage.fromStateAndMelody(ItemActionMessage.State.PLAY, entry.getKey()));
                selected = entry.getKey();
                refreshPage();
            });

            if (entry.getKey().equals(selected)) {
                list.setSelected(list.children().getLast());
            }
        }

        // Build the track list
        trackList.clearEntries();
        if (selected != null && showTrackSelection) {
            addRenderableWidget(trackList);
            Melody melody = ClientMelodyManager.getMelody(selected);
            if (melody != Melody.DEFAULT) {
                for (int i = 0; i < melody.getTracks().size(); i++) {
                    Track track = melody.getTracks().get(i);
                    int trackId = i;
                    trackList.addEntry(
                            ResourceLocation.parse(selected.getPath() + "/" + i),
                            Component.translatable(track.getName()).withStyle(enabledTracks.contains(i) ? ChatFormatting.DARK_GRAY : ChatFormatting.STRIKETHROUGH),
                            () -> {
                                boolean enabled = enabledTracks.contains(trackId);
                                Network.sendToServer(new TrackToggleMessage(selected, trackId, !enabled));
                                refreshPage();
                            });
                }
            }
        }

        this.list.setScrollAmount(this.list.getScrollAmount());

        int y = this.height / 2 + 69;

        // Close
        addRenderableWidget(new TexturedButtonWidget(width / 2 - 75, y, 16, 16, BACKGROUND_TEXTURE, 256 - 16, 0, 256, 256, Component.nullToEmpty(null), button -> {
            onClose();
        }, () -> List.of(Component.translatable("immersive_melodies.close").getVisualOrderText())));

        // Track selection
        if (selected != null) {
            addRenderableWidget(new TexturedButtonWidget(width / 2 - 55, y, 16, 16, BACKGROUND_TEXTURE, 256 - 48, 16, 256, 256, Component.nullToEmpty(null), button -> {
                this.showTrackSelection = !this.showTrackSelection;
                refreshPage();
            }, () -> List.of(Component.translatable("immersive_melodies.tracks").getVisualOrderText())));
        }

        // Free playing
        addRenderableWidget(new TexturedButtonWidget(width / 2 - 35, y, 16, 16, BACKGROUND_TEXTURE, 256 - 48, 0, 256, 256, Component.nullToEmpty(null), button -> {
            if (minecraft != null) {
                minecraft.setScreen(new ImmersiveMelodiesFreePlayingScreen());
            }
        }, () -> List.of(Component.translatable("immersive_melodies.keyboard").getVisualOrderText())));

        // Pause
        addRenderableWidget(new TexturedButtonWidget(width / 2 - 15, y, 16, 16, BACKGROUND_TEXTURE, 256 - 32, 32, 256, 256, Component.nullToEmpty(null), button -> {
            Network.sendToServer(ItemActionMessage.fromState(ItemActionMessage.State.PAUSE));
        }, () -> List.of(Component.translatable("immersive_melodies.pause").getVisualOrderText())));

        // Play
        addRenderableWidget(new TexturedButtonWidget(width / 2 + 5, y, 16, 16, BACKGROUND_TEXTURE, 256 - 16, 32, 256, 256, Component.nullToEmpty(null), button -> {
            Network.sendToServer(ItemActionMessage.fromState(ItemActionMessage.State.CONTINUE));
        }, () -> List.of(Component.translatable("immersive_melodies.play").getVisualOrderText())));

        int actionX = width / 2 + 25;

        // Delete
        if (selected != null && (Utils.canDelete(selected, Minecraft.getInstance().player))) {
            addRenderableWidget(new TexturedButtonWidget(actionX, y, 16, 16, BACKGROUND_TEXTURE, 256 - 16, 16, 256, 256, Component.nullToEmpty(null), button -> {
                Network.sendToServer(new MelodyDeleteRequest(selected));
                selected = null;
            }, () -> List.of(Component.translatable("immersive_melodies.delete").getVisualOrderText())));
            actionX += 20;
        }

        // Upload
        addRenderableWidget(new TexturedButtonWidget(actionX, y, 16, 16, BACKGROUND_TEXTURE, 256 - 48, 48, 256, 256, Component.empty(), button -> {
            if (minecraft != null) {
                minecraft.setScreen(new MelodyUploadScreen(this));
            }
        }, () -> List.of(Component.translatable("immersive_melodies.upload").getVisualOrderText())));
        actionX += 20;

        // Help
        addRenderableWidget(new TexturedButtonWidget(actionX, y, 16, 16, BACKGROUND_TEXTURE, 256 - 48, 32, 256, 256, Component.nullToEmpty(null), button -> {
            openHelp();
            if (!Config.getInstance().clickedHelp) {
                Config.getInstance().clickedHelp = true;
                Config.getInstance().save();
            }
        }, () -> List.of(Component.translatable("immersive_melodies.help").getVisualOrderText())));
    }

    private static int getSortIndex(Map.Entry<ResourceLocation, MelodyDescriptor> entry) {
        return Utils.ownsMelody(entry.getKey(), Minecraft.getInstance().player) ? 2 : Utils.isPlayerMelody(entry.getKey()) ? 0 : 1;
    }

    public Font getTextRenderer() {
        return this.font;
    }
}
