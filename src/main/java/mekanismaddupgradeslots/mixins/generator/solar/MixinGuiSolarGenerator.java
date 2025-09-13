package mekanismaddupgradeslots.mixins.generator.solar;

import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.generators.client.gui.GuiSolarGenerator;
import mekanism.generators.common.tile.TileEntitySolarGenerator;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiSolarGenerator.class)
public class MixinGuiSolarGenerator extends GuiMekanismTile<TileEntitySolarGenerator> {
    public MixinGuiSolarGenerator(TileEntitySolarGenerator tileEntitySolarGenerator, Container container) {
        super(tileEntitySolarGenerator, container);
    }

    @Inject(
            method = "<init>(Lnet/minecraft/entity/player/InventoryPlayer;Lmekanism/generators/common/tile/TileEntitySolarGenerator;)V",
            at = @At("TAIL")
    )
    private void injectGuiUpgradeTab(InventoryPlayer inventory, TileEntitySolarGenerator tile, CallbackInfo ci) {
        ResourceLocation resource = this.getGuiLocation();
        this.addGuiElement(new GuiUpgradeTab(this, tile, resource));
    }
}
