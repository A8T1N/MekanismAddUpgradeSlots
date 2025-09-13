package mekanismaddupgradeslots.mixins.generator.heat;

import mekanism.common.Upgrade;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NonNullListSynchronized;
import mekanism.generators.common.tile.TileEntityGenerator;
import mekanism.generators.common.tile.TileEntityHeatGenerator;
import mekanismaddupgradeslots.MekanismAUSUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidTank;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnull;

@Mixin(TileEntityHeatGenerator.class)
public abstract class MixinTileEntityHeatGenerator extends TileEntityGenerator implements IUpgradeTile {
    @Shadow
    public FluidTank lavaTank;
    @Shadow
    public double thermalEfficiency;
    @Unique
    private TileComponentUpgrade upgradeComponent; // アップグレードを追加するクラス

    public MixinTileEntityHeatGenerator(String soundPath, String name, double maxEnergy, double out) {
        super(soundPath, name, maxEnergy, out);
    }

    /* ==============================================
     * アップグレードスロット関連
     * ============================================== */

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        TileEntityHeatGenerator self = (TileEntityHeatGenerator) (Object) this;
        NonNullListSynchronized<ItemStack> newInventory = NonNullListSynchronized.withSize(3, ItemStack.EMPTY);
        newInventory.set(0, self.inventory.get(0));
        newInventory.set(1, self.inventory.get(1));
        self.inventory = newInventory;
        upgradeComponent = new TileComponentUpgrade(self, 2);
        upgradeComponent.setSupported(Upgrade.SPEED);
        upgradeComponent.setSupported(Upgrade.ENERGY);
    }

    /**
     * IUpgradeTileの必須メソッド: アップグレードコンポーネントを返す
     */
    @Override
    public TileComponentUpgrade getComponent() {
        return upgradeComponent;
    }


    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull EnumFacing side) {
        if (side == MekanismUtils.getRight(this.facing)) {
            return new int[]{1};
        }
        // それ以外の面からはスロット0と2 アップグレードスロット
        return new int[]{0, 2};
    }

    /*　=========================
     * エネルギーアップグレード 対応
     *  ======================== */
    @Override
    public void recalculateUpgradables(Upgrade upgrade) {
        super.recalculateUpgradables(upgrade);
        if (upgrade == Upgrade.ENERGY) {
            maxEnergy = MekanismUtils.getMaxEnergy(this, BASE_MAX_ENERGY);
            setEnergy(Math.min(getMaxEnergy(), getEnergy()));
        }
    }

    /* =========================
     *  スピードアップグレード 対応
     * ========================= */

    // 発電動作条件にエネルギーアップグレードを対応
    @Override
    public boolean canOperate() {
        return this.electricityStored.get() < this.BASE_MAX_ENERGY * MekanismAUSUtils.getUpgradeMultiplier(Upgrade.ENERGY, this.upgradeComponent) && this.lavaTank.getFluid() != null && this.lavaTank.getFluid().amount >= 10 && MekanismUtils.canFunction(this);
    }

    @Shadow
    public double getTemp() {
        return 0;
    }

    @Shadow
    public void transferHeatTo(double heat) {

    }

    public double[] simulateHeat() {
        TileEntityHeatGenerator self = (TileEntityHeatGenerator) (Object) this;
        if (this.getTemp() > 0.0) {
            double carnotEfficiency = this.getTemp() / (this.getTemp() + 300.0);
            double heatLost = this.thermalEfficiency * this.getTemp();
            double workDone = heatLost * carnotEfficiency * MekanismAUSUtils.getUpgradeMultiplier(Upgrade.SPEED, this.upgradeComponent);
            this.transferHeatTo(-heatLost);
            this.setEnergy(this.getEnergy() + workDone);
        }

        return HeatUtils.simulate(self);
    }

    /* ==============================================
     * エネルギーアップグレードによる排出RF量の動的変更
     * ============================================== */

    @Override
    public double getMaxOutput() {
        return MekanismConfig.current().generators.heatGeneration.val() * 2.0 * MekanismAUSUtils.getUpgradeMultiplier(Upgrade.ENERGY, this.upgradeComponent);
    }

}
