package com.pulse.datapacktools.mixin.client.compat;

import bettercommandblockui.main.ui.screen.AbstractBetterCommandBlockScreen;
import com.pulse.datapacktools.client.buttons.IconButtonWidget;
import com.pulse.datapacktools.client.keybindings.ExportKeybinding;
import com.pulse.datapacktools.client.screen.ExportFunctionScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBetterCommandBlockScreen.class)
public abstract class AbstractBetterCommandBlockScreenMixin extends Screen {

    protected AbstractBetterCommandBlockScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        if (client == null) return;

        this.addDrawableChild(new IconButtonWidget(
                this.width - (10 + 20),
                this.height - (10 + 20 + 30),
                20, 20,
                Identifier.of("datapacktools", "textures/gui/export_button.png"),
                b -> client.setScreen(new ExportFunctionScreen())
        ));
    }

    @Inject(method = "method_25404", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (ExportKeybinding.exportKey.matchesKey(new KeyInput(keyCode, scanCode, modifiers))) {
            if (client != null) {
                client.setScreen(new ExportFunctionScreen());
                cir.setReturnValue(true);
            }
        }
    }
}
