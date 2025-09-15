package mekanismaddupgradeslots.mixins.generator.solar;

import mekanism.common.Upgrade;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NonNullListSynchronized;
import mekanism.generators.common.tile.TileEntityGenerator;
import mekanism.generators.common.tile.TileEntitySolarGenerator;
import mekanismaddupgradeslots.MekanismAUSUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nonnull;

@Mixin(TileEntitySolarGenerator.class)
public abstract class MixinTileEntitySolarGenerator extends TileEntityGenerator implements IUpgradeTile {
    @Unique
    private TileComponentUpgrade upgradeComponent; // アップグレードを追加するクラス

    public MixinTileEntitySolarGenerator(String soundPath, String name, double maxEnergy, double out) {
        super(soundPath, name, maxEnergy, out);
    }

    /* ==============================================
     * アップグレードスロット関連
     * ============================================== */

    @Inject(method = "<init>(Ljava/lang/String;DD)V", at = @At("TAIL"))
    private void init(String name, double maxEnergy, double output, CallbackInfo ci) {
        TileEntitySolarGenerator self = (TileEntitySolarGenerator) (Object) this;
        NonNullListSynchronized<ItemStack> newInventory = NonNullListSynchronized.withSize(2,  new ItemStack((Item)null));
        newInventory.set(0, self.inventory.get(0));
        self.inventory = newInventory;
        upgradeComponent = new TileComponentUpgrade(self, 1);
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
        return new int[]{0, 1};
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


    @Inject(method = "getProduction", at = @At("RETURN"), cancellable = true, remap = false)
    private void modifyProduction(CallbackInfoReturnable<Double> cir) {
        // Get the original production value
        double originalProduction = cir.getReturnValue();
        // Multiply by 100 as requested
        cir.setReturnValue(originalProduction * MekanismAUSUtils.getUpgradeMultiplier(Upgrade.SPEED, this.upgradeComponent));
    }

    @Shadow
    public double getProduction() {
        return 0;
    }

    public String getEfficiencyStr() {
        return String.format("%2.0f", (getProduction() / getMaxOutput()) * 100);
    }

    /* ==============================================
     * エネルギーアップグレードによる排出RF量の動的変更
     * ============================================== */

    @Override
    public double getMaxOutput() {
        return MekanismConfig.current().generators.solarGeneration.val() * 2 * MekanismAUSUtils.getUpgradeMultiplier(Upgrade.ENERGY, this.upgradeComponent);
    }
}
