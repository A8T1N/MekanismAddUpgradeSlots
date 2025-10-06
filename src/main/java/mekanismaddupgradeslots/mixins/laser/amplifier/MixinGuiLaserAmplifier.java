package mekanismaddupgradeslots.mixins.laser.amplifier;

import mekanism.client.gui.GuiLaserAmplifier;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.gauge.GuiNumberGauge;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.common.tile.laser.TileEntityLaserAmplifier;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiLaserAmplifier.class)
public class MixinGuiLaserAmplifier extends GuiMekanismTile<TileEntityLaserAmplifier> {
    @Shadow
    private GuiTextField maxField;

    public MixinGuiLaserAmplifier(TileEntityLaserAmplifier tileEntityLaserAmplifier, Container container) {
        super(tileEntityLaserAmplifier, container);
    }

    // アップグレードスロットの追加
    @Inject(
            method = "<init>(Lnet/minecraft/entity/player/InventoryPlayer;Lmekanism/common/tile/laser/TileEntityLaserAmplifier;)V",
            at = @At("TAIL")
    )
    private void injectGuiUpgradeTab(InventoryPlayer inventory, TileEntityLaserAmplifier tile, CallbackInfo ci) {
        ResourceLocation resource = this.getGuiLocation();
        this.addGuiElement(new GuiUpgradeTab(this, tile, resource));
    }

    // 入力受け付け値の上限を最大蓄電容量と同期
    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lmekanism/client/gui/element/GuiNumberGauge$INumberInfoHandler;getMaxLevel()D"
            )
    )
    private double redirectGetMaxLevel(GuiNumberGauge.INumberInfoHandler handler) {
        return this.tileEntity.getMaxEnergy();
    }

}
