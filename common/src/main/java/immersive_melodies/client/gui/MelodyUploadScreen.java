package immersive_melodies.client.gui;

import immersive_melodies.Common;
import immersive_melodies.client.gui.widget.TexturedButtonWidget;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class MelodyUploadScreen extends Screen {
    private final ImmersiveMelodiesScreen parent;
    private EditBox search;
    private LocalFileList fileList;
    private Path directory;
    private Component error;

    public MelodyUploadScreen(ImmersiveMelodiesScreen parent) {
        super(Component.translatable("immersive_melodies.upload"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        assert minecraft != null;
        directory = minecraft.gameDirectory.toPath().resolve("immersive_melodies").toAbsolutePath().normalize();
        int top = (height - 230) / 2;

        search = new EditBox(font, width / 2 - 70, top + 12, 140, 20, Component.translatable("immersive_melodies.search"));
        search.setMaxLength(128);
        search.setSuggestion("Search");
        search.setResponder(value -> refreshFiles());
        search.setBordered(false);
        search.setTextColor(0x808080);

        fileList = new LocalFileList(minecraft, width / 2 - 75, 150, top + 22, 145);
        setInitialFocus(search);
        refreshFiles();
    }

    private void refreshFiles() {
        if (fileList == null) {
            return;
        }

        Path selected = fileList.getSelectedPath();
        fileList.clearFiles();
        error = null;
        try {
            Files.createDirectories(directory);
            String query = search.getValue().toLowerCase(Locale.ROOT);
            try (Stream<Path> paths = Files.walk(directory)) {
                paths.filter(Files::isRegularFile)
                        .filter(MelodyUploadScreen::isSupported)
                        .filter(path -> directory.relativize(path).toString().toLowerCase(Locale.ROOT).contains(query))
                        .sorted(Comparator.comparing(path -> directory.relativize(path).toString().toLowerCase(Locale.ROOT)))
                        .forEach(fileList::addFile);
            }
            fileList.select(selected);
        } catch (IOException e) {
            Common.LOGGER.error("Could not list local melody files in {}", directory, e);
            error = Component.literal(e.getLocalizedMessage());
        }
        refreshWidgets();
    }

    private static boolean isSupported(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".mid") || name.endsWith(".midi") || name.endsWith(".abc");
    }

    private void refreshWidgets() {
        clearWidgets();
        addRenderableWidget(search);
        addRenderableWidget(fileList);

        int y = height / 2 + 69;
        addIconButton(width / 2 - 33, y, 240, 0, "immersive_melodies.back", this::onClose);

        if (fileList.getSelectedPath() != null) {
            addIconButton(width / 2 - 12, y, 256 - 48, 48, "immersive_melodies.upload", this::uploadSelected);
        }

        addIconButton(width / 2 + 9, y, 256 - 32, 48, "immersive_melodies.refresh", this::refreshFiles);
        addIconButton(width / 2 + 30, y, 256 - 16, 48, this::openDirectory,
                () -> font.split(Component.literal(directory.toString()), 300));
    }

    private void addIconButton(int x, int y, int u, int v, String tooltip, Runnable action) {
        addIconButton(x, y, u, v, action,
                () -> List.of(Component.translatable(tooltip).getVisualOrderText()));
    }

    private void addIconButton(int x, int y, int u, int v, Runnable action,
                               Supplier<List<FormattedCharSequence>> tooltipSupplier) {
        addRenderableWidget(new TexturedButtonWidget(x, y, 16, 16, ImmersiveMelodiesScreen.BACKGROUND_TEXTURE,
                u, v, 256, 256, Component.empty(), button -> action.run(), tooltipSupplier));
    }

    private void openDirectory() {
        try {
            Files.createDirectories(directory);
            Util.getPlatform().openFile(directory.toFile());
        } catch (IOException e) {
            Common.LOGGER.error("Could not open local melody directory {}", directory, e);
            error = Component.literal(e.getLocalizedMessage());
        }
    }

    private void uploadSelected() {
        Path selected = fileList.getSelectedPath();
        if (selected != null) {
            parent.onFilesDrop(List.of(selected));
            onClose();
        }
    }

    @Override
    public void onFilesDrop(List<Path> paths) {
        parent.onFilesDrop(paths);
        onClose();
    }

    @Override
    public void onClose() {
        assert minecraft != null;
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics);

        int x = (width - 192) / 2;
        int y = (height - 230) / 2;

        graphics.blit(ImmersiveMelodiesScreen.BACKGROUND_TEXTURE, x, y, 0, 0, 192, 215);

        if (error != null) {
            graphics.drawCenteredString(font, error, width / 2, y + 36, 0xFF0000);
        } else if (fileList != null && fileList.children().isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable("immersive_melodies.upload.empty"), width / 2, y + 66, 0x808080);
        }

        super.render(graphics, mouseX, mouseY, delta);

        Component dropHint = Component.literal("[").append(Component.translatable("immersive_melodies.upload.drop_here")).append("]");
        graphics.drawString(font, dropHint, width / 2 - font.width(dropHint) / 2, y + 172, 0x606060, false);
        int dropHintWidth = font.width(dropHint);
        if (mouseX >= width / 2 - dropHintWidth / 2 && mouseX <= width / 2 + dropHintWidth / 2
            && mouseY >= y + 168 && mouseY <= y + 180) {
            graphics.renderTooltip(font, List.of(
                    Component.translatable("immersive_melodies.upload.drag_drop").getVisualOrderText(),
                    Component.translatable("immersive_melodies.upload.fallback").getVisualOrderText()), mouseX, mouseY);
        }
    }

    private class LocalFileList extends ObjectSelectionList<LocalFileList.FileEntry> {
        LocalFileList(Minecraft client, int left, int width, int top, int height) {
            super(client, width, MelodyUploadScreen.this.height, top, top + height, 10);
            x0 = left;
            x1 = left + width;
            setRenderBackground(false);
            setRenderTopAndBottom(false);
        }

        void addFile(Path path) {
            addEntry(new FileEntry(path));
        }

        void clearFiles() {
            clearEntries();
        }

        Path getSelectedPath() {
            return getSelected() == null ? null : getSelected().path;
        }

        void select(Path path) {
            children().stream().filter(entry -> entry.path.equals(path)).findFirst().ifPresent(this::setSelected);
        }

        @Override
        public int getRowWidth() {
            return width;
        }

        @Override
        protected int getScrollbarPosition() {
            return x0 + width + 2;
        }

        @Override
        protected void enableScissor(GuiGraphics graphics) {
            graphics.enableScissor(x0 - 15, y0, x0 + width, y1);
        }

        @Override
        protected void renderSelection(GuiGraphics graphics, int y, int entryWidth, int entryHeight, int borderColor, int fillColor) {
            graphics.fill(x0 - 1, y - 1, x0 + width, y + entryHeight + 3, 0x40000000);
        }

        private class FileEntry extends ObjectSelectionList.Entry<FileEntry> {
            private final Path path;

            FileEntry(Path path) {
                this.path = path;
            }

            @Override
            public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight,
                               int mouseX, int mouseY, boolean hovered, float delta) {
                String label = directory.relativize(path).toString();
                graphics.drawString(font, font.plainSubstrByWidth(label, entryWidth - 4), x0 + 2, y + 1, 0x404040, false);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (button == 0) {
                    LocalFileList.this.setSelected(this);
                    refreshWidgets();
                    return true;
                }
                return false;
            }

            @Override
            public Component getNarration() {
                return Component.literal(directory.relativize(path).toString());
            }
        }
    }
}
