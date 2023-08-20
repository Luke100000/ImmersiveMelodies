package immersive_melodies.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class TexturedButtonWidget extends ButtonWidget {
    private final int u, v, tw, th, w, h;
    private final Identifier texture;

    public TexturedButtonWidget(int x, int y, int width, int height, Identifier texture, int u, int v, int tw, int th, Text message, ButtonWidget.PressAction onPress, TooltipSupplier tooltipSupplier) {
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
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        TextRenderer textRenderer = minecraftClient.textRenderer;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);

        if (hovered) {
            RenderSystem.setShaderColor(1.0f, 0.75f, 0.75f, this.alpha);
        } else {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        drawTexture(matrices, x, y, this.u, this.v + (active ? 0 : 16), this.w, this.h, this.tw, this.th);

        int j = this.active ? 0xFFFFFF : 0xA0A0A0;
        ClickableWidget.drawCenteredText(matrices, textRenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0f) << 24);
    }
}

