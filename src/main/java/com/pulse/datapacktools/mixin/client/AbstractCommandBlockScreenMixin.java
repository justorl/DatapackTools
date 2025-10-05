package com.pulse.datapacktools.mixin.client;

import com.pulse.datapacktools.client.buttons.IconButtonWidget;
import com.pulse.datapacktools.client.keybindings.ExportKeybinding;
import com.pulse.datapacktools.client.screen.ExportFunctionScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractCommandBlockScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractCommandBlockScreen.class)
public abstract class AbstractCommandBlockScreenMixin extends Screen {
    @Shadow protected TextFieldWidget consoleCommandTextField;

    protected AbstractCommandBlockScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        if (client == null) return;

        this.addDrawableChild(new IconButtonWidget(
                this.width / 2 + 4 + 150 + 4,
                this.height / 4 + 120 + 12,
                20, 20,
                new Identifier("datapacktools", "textures/gui/export_button.png"),
                b -> client.setScreen(new ExportFunctionScreen())
        ));
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (ExportKeybinding.exportKey.matchesKey(keyCode, scanCode)) {
            if (client != null) {
                client.setScreen(new ExportFunctionScreen());
                cir.setReturnValue(true);
            }
        }
    }
}
