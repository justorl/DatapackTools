package com.pulse.datapacktools.mixin.client;

import com.pulse.datapacktools.client.buttons.IconButtonWidget;
import com.pulse.datapacktools.client.keybindings.ExportKeybinding;
import com.pulse.datapacktools.client.screen.ExportFunctionScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractCommandBlockScreen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractCommandBlockScreen.class)
public abstract class AbstractCommandBlockScreenMixin extends Screen {

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
                Identifier.of("datapacktools", "textures/gui/export_button.png"),
                b -> client.setScreen(new ExportFunctionScreen())
        ));
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(KeyInput input, CallbackInfoReturnable<Boolean> cir) {
        if (ExportKeybinding.exportKey.matchesKey(input)) {
            if (client != null) {
                client.setScreen(new ExportFunctionScreen());
                cir.setReturnValue(true);
            }
        }
    }
}
