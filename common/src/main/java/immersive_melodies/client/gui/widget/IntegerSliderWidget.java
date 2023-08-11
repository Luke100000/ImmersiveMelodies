package immersive_melodies.client.gui.widget;

import net.minecraft.text.Text;

import java.util.function.Consumer;

public class IntegerSliderWidget extends ExtendedSliderWidget<Integer> {
    private final int min;
    private final int max;
    private final String text;

    public IntegerSliderWidget(int x, int y, int width, int height, String text, double value, int min, int max, Consumer<Integer> onApplyValue) {
        super(x, y, width, height, Text.literal(""), (value - min) / (max - min), onApplyValue);
        this.min = min;
        this.max = max;
        this.text = text;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        setMessage(Text.translatable(text, getValue()));
    }

    @Override
    Integer getValue() {
        return (int)(value * (max - min) + min);
    }

    @Override
    protected double getOpticalValue() {
        return ((double)getValue() - min) / (max - min);
    }
}
