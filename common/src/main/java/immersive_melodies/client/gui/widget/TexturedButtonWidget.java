package immersive_melodies.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.function.Supplier;

public class TexturedButtonWidget extends DefaultButtonWidget {
    private final int u, v, tw, th, w, h;
    private final Identifier texture;

    public TexturedButtonWidget(int x, int y, int width, int height, Identifier texture, int u, int v, int tw, int th, Text message, PressAction onPress, Supplier<List<OrderedText>> tooltipSupplier) {
        super(x, y, width, height, message, onPress, tooltipSupplier);
        this.texture = texture;
        this.w = width;
        this.h = height;
        this.u = u;
        this.v = v;
        this.tw = tw;
        this.th = th;
    }

    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        if (hovered) {
            RenderSystem.setShaderColor(1.0f, 0.75f, 0.75f, this.alpha);
        } else {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
        }

        context.drawTexture( texture, getX(), getY(), this.u, this.v + (active ? 0 : 16), this.w, this.h, this.tw, this.th);

        int j = this.active ? 0xFFFFFF : 0xA0A0A0;
        context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0f) << 24);
    }
}

