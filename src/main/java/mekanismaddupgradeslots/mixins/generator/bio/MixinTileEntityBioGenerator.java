package mekanismaddupgradeslots.mixins.generator.bio;

import mekanism.common.FluidSlot;
import mekanism.common.Upgrade;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NonNullListSynchronized;
import mekanism.generators.common.tile.TileEntityBioGenerator;
import mekanism.generators.common.tile.TileEntityGenerator;
import mekanismaddupgradeslots.MekanismAUSUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnull;

@Mixin(TileEntityBioGenerator.class)
public abstract class MixinTileEntityBioGenerator extends TileEntityGenerator implements IUpgradeTile {
    @Shadow
    public FluidSlot bioFuelSlot;
    @Unique
    private TileComponentUpgrade upgradeComponent; // アップグレードを追加するクラス

    public MixinTileEntityBioGenerator(String soundPath, String name, double maxEnergy, double out) {
        super(soundPath, name, maxEnergy, out);
    }

    /* ==============================================
     * アップグレードスロット関連
     * ============================================== */

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        TileEntityBioGenerator self = (TileEntityBioGenerator) (Object) this;
        NonNullListSynchronized<ItemStack> newInventory = NonNullListSynchronized.withSize(3,  new ItemStack((Item)null));
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
    public boolean canOperate() {
        return this.electricityStored.get() < this.BASE_MAX_ENERGY * MekanismAUSUtils.getUpgradeMultiplier(Upgrade.ENERGY, this.upgradeComponent) && this.bioFuelSlot.fluidStored > 0 && MekanismUtils.canFunction(this);
    }

    @Redirect(
            method = "onAsyncUpdateServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lmekanism/generators/common/tile/TileEntityBioGenerator;setEnergy(D)V"
            )
    )
    private void redirectSetEnergy(TileEntityBioGenerator instance, double originalArg) {
        originalArg = instance.electricityStored.get()
                + MekanismConfig.current().generators.bioGeneration.val()
                * MekanismAUSUtils.getUpgradeMultiplier(Upgrade.SPEED, this.upgradeComponent);
        instance.setEnergy(originalArg);
    }

    /*===========================================
     * エネルギーアップグレードによる排出RF量の動的変更
     * ========================================== */

    @Override
    public double getMaxOutput() {
        return MekanismConfig.current().generators.bioGeneration.val() * 2.0 * MekanismAUSUtils.getUpgradeMultiplier(Upgrade.ENERGY, this.upgradeComponent);
    }

}
