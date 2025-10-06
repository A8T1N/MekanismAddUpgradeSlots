package mekanismaddupgradeslots.mixins.generator.reactor;

import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.generators.client.gui.GuiReactorController;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiReactorController.class)
public abstract class MixinGuiReactorController extends GuiMekanismTile<TileEntityReactorController> {
    public MixinGuiReactorController(TileEntityReactorController tileEntityReactorController, Container container) {
        super(tileEntityReactorController, container);
    }

//    @Inject(
//            method = "<init>(Lnet/minecraft/entity/player/InventoryPlayer;Lmekanism.generators.common.tile.reactor.TileEntityReactorController;)V",
//            at = @At("TAIL")
//    )
//    private void injectGuiUpgradeTab(InventoryPlayer inventory, TileEntityReactorController tile, CallbackInfo ci) {
//        ResourceLocation resource = this.getGuiLocation();
//        this.addGuiElement(new GuiUpgradeTab(this, tile, resource));
//    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void injectGuiUpgradeTab(InventoryPlayer inventory, TileEntityReactorController tile, CallbackInfo ci) {
        if (this.tileEntity.isFormed()) {
            ResourceLocation resource = this.getGuiLocation();
            this.addGuiElement(new GuiUpgradeTab(this, tile, resource));
        }
    }
}
