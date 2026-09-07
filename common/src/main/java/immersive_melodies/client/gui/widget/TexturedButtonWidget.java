package immersive_melodies.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.function.Supplier;

public class TexturedButtonWidget extends DefaultButtonWidget {
    private final int u, v, tw, th, w, h;
    private final ResourceLocation texture;

    public TexturedButtonWidget(int x, int y, int width, int height, ResourceLocation texture, int u, int v, int tw, int th, Component message, OnPress onPress, Supplier<List<FormattedCharSequence>> tooltipSupplier) {
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
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if (isHovered) {
            RenderSystem.setShaderColor(1.0f, 0.75f, 0.75f, this.alpha);
        } else {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
        }

        context.blit(texture, getX(), getY(), this.u, this.v + (active ? 0 : 16), this.w, this.h, this.tw, this.th);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        int j = this.active ? 0xFFFFFF : 0xA0A0A0;
        context.drawCenteredString(Minecraft.getInstance().font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, j | Mth.ceil(this.alpha * 255.0f) << 24);
    }
}
