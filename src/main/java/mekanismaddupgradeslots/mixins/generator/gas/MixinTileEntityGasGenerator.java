package mekanismaddupgradeslots.mixins.generator.gas;

import mekanism.api.gas.GasTank;
import mekanism.common.Upgrade;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NonNullListSynchronized;
import mekanism.generators.common.tile.TileEntityGasGenerator;
import mekanism.generators.common.tile.TileEntityGenerator;
import mekanismaddupgradeslots.MekanismAUSUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnull;

@Mixin(TileEntityGasGenerator.class)
public abstract class MixinTileEntityGasGenerator extends TileEntityGenerator implements IUpgradeTile {
    @Shadow
    public GasTank fuelTank;

    @Shadow
    public double generationRate;
    private TileComponentUpgrade upgradeComponent;

    public MixinTileEntityGasGenerator(String soundPath, String name, double maxEnergy, double out) {
        super(soundPath, name, maxEnergy, out);
    }

    /* ==============================================
     * アップグレードスロット関連
     * ============================================== */

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        TileEntityGasGenerator self = (TileEntityGasGenerator) (Object) this;
        NonNullListSynchronized<ItemStack> newInventory = NonNullListSynchronized.withSize(3,  new ItemStack((Item)null));
        newInventory.set(0, self.inventory.get(0));
        newInventory.set(1, self.inventory.get(1));
        self.inventory = newInventory;
        this.upgradeComponent = new TileComponentUpgrade(self, 2);
        this.upgradeComponent.setSupported(Upgrade.SPEED);
        this.upgradeComponent.setSupported(Upgrade.ENERGY);
    }

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
        return new int[]{0, 2};
    }

    /* ==============================================
     * エネルギーアップグレード対応
     * ============================================== */

    @Override
    public void recalculateUpgradables(Upgrade upgrade) {
        super.recalculateUpgradables(upgrade);
        if (upgrade == Upgrade.ENERGY) {
            maxEnergy = MekanismUtils.getMaxEnergy(this, BASE_MAX_ENERGY);
            setEnergy(Math.min(getMaxEnergy(), getEnergy()));
        }
    }

    /* ==============================================
     * スピードアップグレード対応
     * ============================================== */

    @Redirect(
            method = "onAsyncUpdateServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lmekanism/generators/common/tile/TileEntityGasGenerator;setEnergy(D)V"
            )
    )
    private void redirectSetEnergy(TileEntityGasGenerator instance, double originalArg) {
        int redstoneLevel = instance.getToUse();
        originalArg = instance.getEnergy()
                + generationRate * redstoneLevel
                * MekanismAUSUtils.getUpgradeMultiplier(Upgrade.SPEED, this.upgradeComponent);
        instance.setEnergy(originalArg);
    }

    /* ==============================================
     * エネルギーアップグレードによる排出RF量の動的変更
     * ============================================== */

    //TODO 今のところ問題ないかも？
}