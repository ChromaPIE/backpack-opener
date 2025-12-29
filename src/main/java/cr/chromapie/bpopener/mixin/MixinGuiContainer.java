package cr.chromapie.bpopener.mixin;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cr.chromapie.bpopener.handler.BPOHandler;

/** Intercepts right-click in GuiContainer since MouseEvent doesn't fire when GUI is open. */
@Mixin(GuiContainer.class)
public abstract class MixinGuiContainer {

    @Shadow
    private Slot theSlot;

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
        if (mouseButton == 1) {
            GuiContainer self = (GuiContainer) (Object) this;
            if (BPOHandler.onRightClickInGui(self, theSlot)) {
                ci.cancel();
            }
        }
    }
}
