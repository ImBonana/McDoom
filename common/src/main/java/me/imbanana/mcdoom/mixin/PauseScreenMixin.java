package me.imbanana.mcdoom.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.imbanana.mcdoom.McDoom;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {
    protected PauseScreenMixin(Component title) {
        super(title);
    }

    @Inject(
            method = "createPauseMenu",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;I)Lnet/minecraft/client/gui/layouts/LayoutElement;",
                    shift = At.Shift.AFTER
            )
    )
    private void addStartGameButton(CallbackInfo ci, @Local(name = "helper") GridLayout.RowHelper helper) {
        helper.addChild(
            Button.builder(
                Component.translatable(McDoom.MOD_ID + ".menu.play"),
                button -> {
                    button.active = false;
                    McDoom.LOGGER.info("Starting DOOM");
                }
            )
            .width(204)
            .build(),
    2
        );
    }
}
