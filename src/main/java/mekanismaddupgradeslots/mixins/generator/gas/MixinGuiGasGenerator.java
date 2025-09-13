package mekanismaddupgradeslots.mixins.generator.gas;

import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.generators.client.gui.GuiGasGenerator;
import mekanism.generators.common.tile.TileEntityGasGenerator;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGasGenerator.class)
public class MixinGuiGasGenerator extends GuiMekanismTile<TileEntityGasGenerator> {
    public MixinGuiGasGenerator(TileEntityGasGenerator tileEntityGasGenerator, Container container) {
        super(tileEntityGasGenerator, container);
    }

    @Inject(
            method = "<init>(Lnet/minecraft/entity/player/InventoryPlayer;Lmekanism/generators/common/tile/TileEntityGasGenerator;)V",
            at = @At("TAIL")
    )
    private void injectGuiUpgradeTab(InventoryPlayer inventory, TileEntityGasGenerator tile, CallbackInfo ci) {
        ResourceLocation resource = this.getGuiLocation();
        this.addGuiElement(new GuiUpgradeTab(this, tile, resource));
    }
}
