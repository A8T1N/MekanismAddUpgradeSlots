package mekanismaddupgradeslots.mixins.laser.tractorbeam;

import mekanism.client.gui.GuiLaserTractorBeam;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.common.tile.laser.TileEntityLaserTractorBeam;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiLaserTractorBeam.class)
public class MixinGuiLaserTractorBeam extends GuiMekanismTile<TileEntityLaserTractorBeam> {
    public MixinGuiLaserTractorBeam(TileEntityLaserTractorBeam tileEntityLaserTractorBeam, Container container) {
        super(tileEntityLaserTractorBeam, container);
    }

    // アップグレードスロットの追加
    @Inject(
            method = "<init>(Lnet/minecraft/entity/player/InventoryPlayer;Lmekanism/common/tile/laser/TileEntityLaserTractorBeam;)V",
            at = @At("TAIL")
    )
    private void injectGuiUpgradeTab(InventoryPlayer inventory, TileEntityLaserTractorBeam tile, CallbackInfo ci) {
        ResourceLocation resource = this.getGuiLocation();
        this.addGuiElement(new GuiUpgradeTab(this, tile, resource));
    }
}
