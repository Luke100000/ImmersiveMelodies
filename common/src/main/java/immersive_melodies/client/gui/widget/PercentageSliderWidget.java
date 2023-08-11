package immersive_melodies.client.gui.widget;

import net.minecraft.text.Text;

import java.util.function.Consumer;

public class PercentageSliderWidget extends DoubleSliderWidget {
    public PercentageSliderWidget(int x, int y, int width, int height, String text, double value, Consumer<Double> onApplyValue) {
        this(x, y, width, height, text, value, 0, 1, onApplyValue);
    }

    public PercentageSliderWidget(int x, int y, int width, int height, String text, double value, double min, double max, Consumer<Double> onApplyValue) {
        super(x, y, width, height, text, value, min, max, onApplyValue);
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        setMessage(Text.translatable(text, (int)(getValue() * 100)));
    }

    @Override
    Double getValue() {
        return (int)(super.getValue() * 100.0 + 0.5) / 100.0;
    }

    @Override
    protected double getOpticalValue() {
        return (getValue() - min) / (max - min);
    }
}
