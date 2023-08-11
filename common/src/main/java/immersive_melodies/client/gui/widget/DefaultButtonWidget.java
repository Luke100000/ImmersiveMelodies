package immersive_melodies.client.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Supplier;

public class DefaultButtonWidget extends ButtonWidget {
    private final Supplier<List<OrderedText>> tooltipSupplier;

    public DefaultButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress) {
        this(x, y, width, height, message, onPress, null);
    }

    public DefaultButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress, Supplier<List<OrderedText>> tooltipSupplier) {
        super(x, y, width, height, message, onPress, Supplier::get);

        this.tooltipSupplier = tooltipSupplier;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        if (visible) {
            applyTooltip();
        }
    }

    private void applyTooltip() {
        if (this.tooltipSupplier != null && isHovered()) {
            Screen screen = MinecraftClient.getInstance().currentScreen;
            if (screen != null) {
                screen.setTooltip(this.tooltipSupplier.get(), this.getTooltipPositioner(), this.isFocused());
            }
        }
    }
}
