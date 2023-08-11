package immersive_melodies.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public abstract class ExtendedSliderWidget<T> extends SliderWidget {
    private T oldValue;
    final Consumer<T> onApplyValue;

    public ExtendedSliderWidget(int x, int y, int width, int height, Text text, double value, Consumer<T> onApplyValue) {
        super(x, y, width, height, text, value);
        this.onApplyValue= onApplyValue;
    }

    protected double getOpticalValue() {
        return value;
    }

    abstract T getValue();

    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        int i = (this.isHovered() ? 2 : 1) * 20;
        context.drawTexture(WIDGETS_TEXTURE, this.getX() + (int)(getOpticalValue() * (double)(this.width - 8)), this.getY(), 0, 46 + i, 4, 20);
        context.drawTexture(WIDGETS_TEXTURE, this.getX() + (int)(getOpticalValue() * (double)(this.width - 8)) + 4, this.getY(), 196, 46 + i, 4, 20);

        super.renderButton(context, mouseX, mouseY, delta);
    }

    @Override
    protected void applyValue() {
        T v = getValue();
        if (v != oldValue) {
            oldValue = v;
            onApplyValue.accept(v);
        }
    }
}
