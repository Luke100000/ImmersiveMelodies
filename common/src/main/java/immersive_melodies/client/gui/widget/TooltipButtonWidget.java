package immersive_melodies.client.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;

public class TooltipButtonWidget extends DefaultButtonWidget {
    public TooltipButtonWidget(int x, int y, int width, int height, Text message, Text tooltip, PressAction onPress) {
        super(x, y, width, height, message, onPress, () -> Tooltip.wrapLines(MinecraftClient.getInstance(), tooltip));
    }
}
