package immersive_melodies.client.gui.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.function.Supplier;

public class DefaultButtonWidget extends Button {
    private final Supplier<List<FormattedCharSequence>> tooltipSupplier;

    public DefaultButtonWidget(int x, int y, int width, int height, Component message, OnPress onPress) {
        this(x, y, width, height, message, onPress, null);
    }

    public DefaultButtonWidget(int x, int y, int width, int height, Component message, OnPress onPress, Supplier<List<FormattedCharSequence>> tooltipSupplier) {
        super(x, y, width, height, message, onPress, Supplier::get);

        this.tooltipSupplier = tooltipSupplier;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.extractDefaultSprite(graphics);
        this.extractDefaultLabel(graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));

        extractTooltip(graphics, mouseX, mouseY);
    }

    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (this.tooltipSupplier != null && isHovered()) {
            graphics.setTooltipForNextFrame(this.tooltipSupplier.get(), mouseX, mouseY);
        }
    }
}
