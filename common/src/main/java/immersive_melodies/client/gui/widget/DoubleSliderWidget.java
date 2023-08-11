package immersive_melodies.client.gui.widget;

import net.minecraft.text.Text;

import java.util.function.Consumer;

public class DoubleSliderWidget extends ExtendedSliderWidget<Double> {
    final double min;
    final double max;
    final String text;

    public DoubleSliderWidget(int x, int y, int width, int height, String text, double value, double min, double max, Consumer<Double> onApplyValue) {
        super(x, y, width, height, Text.literal(""), (value - min) / (max - min), onApplyValue);
        this.min = min;
        this.max = max;
        this.text = text;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        setMessage(Text.translatable(text, String.format("%.2f", getValue())));
    }

    @Override
    Double getValue() {
        return value * (max - min) + min;
    }
}
