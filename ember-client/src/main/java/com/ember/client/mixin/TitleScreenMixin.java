package com.ember.client.mixin;

import com.ember.client.EmberClient;
import com.ember.client.util.Colors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Rainbow Ember branding on the main menu. */
@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Text title) { super(title); }

    @Inject(method = "render", at = @At("TAIL"))
    private void ember$brand(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        String s = EmberClient.NAME + " " + EmberClient.VERSION;
        var m = ctx.getMatrices();
        m.push();
        m.translate(6, 6, 0);
        m.scale(1.5f, 1.5f, 1f);
        int x = 0;
        for (int i = 0; i < s.length(); i++) {
            String c = String.valueOf(s.charAt(i));
            ctx.drawTextWithShadow(textRenderer, c, x, 0, Colors.rainbow(-i * 90L));
            x += textRenderer.getWidth(c);
        }
        m.pop();
        ctx.drawTextWithShadow(textRenderer, "Right Shift in-game for the ClickGUI", 6, 22, 0xFFAAAAAA);
    }
}
