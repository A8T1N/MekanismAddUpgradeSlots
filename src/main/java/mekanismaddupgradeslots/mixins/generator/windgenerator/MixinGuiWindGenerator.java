package mekanismaddupgradeslots.mixins.generator.windgenerator;

import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.generators.client.gui.GuiWindGenerator;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiWindGenerator.class)
public abstract class MixinGuiWindGenerator extends GuiMekanismTile<TileEntityWindGenerator> {
    public MixinGuiWindGenerator(TileEntityWindGenerator tileEntityWindGenerator, Container container) {
        super(tileEntityWindGenerator, container);
    }

    @Inject(
            method = "<init>(Lnet/minecraft/entity/player/InventoryPlayer;Lmekanism/generators/common/tile/TileEntityWindGenerator;)V",
            at = @At("TAIL")
    )
    private void injectGuiUpgradeTab(InventoryPlayer inventory, TileEntityWindGenerator tile, CallbackInfo ci) {
        ResourceLocation resource = this.getGuiLocation();
        this.addGuiElement(new GuiUpgradeTab(this, tile, resource));
    }
}
