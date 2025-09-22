package mekanismaddupgradeslots.mixins.laser.amplifier;

import mekanism.common.Upgrade;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.laser.TileEntityLaserAmplifier;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NonNullListSynchronized;
import mekanismaddupgradeslots.MekanismAUSUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnull;

@Mixin(TileEntityLaserAmplifier.class)
public abstract class MixinTileEntityLaserAmplifier extends TileEntityContainerBlock implements IUpgradeTile {

    @Shadow
    public double collectedEnergy;
    @Shadow
    public double maxThreshold;
    @Unique
    public double maxEnergy = 5.0E9;
    @Unique
    private TileComponentUpgrade upgradeComponent; // アップグレードを追加するクラス

    /* ==============================================
     * アップグレードスロット関連
     * ============================================== */

    public MixinTileEntityLaserAmplifier(String name) {
        super(name);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(CallbackInfo ci) {
        TileEntityLaserAmplifier self = (TileEntityLaserAmplifier) (Object) this;
        NonNullListSynchronized<ItemStack> newInventory = NonNullListSynchronized.withSize(1, new ItemStack((Item) null));
        self.inventory = newInventory;
        upgradeComponent = new TileComponentUpgrade(self, 0); // インベントリ外で管理
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

    /*　=========================
     * エネルギーアップグレード 対応
     *  ======================== */

    @Shadow
    public double getEnergy() {
        return 0;
    }

    public void setEnergy(double energy) {
        this.collectedEnergy = Math.max(0.0, Math.min(energy, maxEnergy));
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull EnumFacing side) {
        return new int[]{0};
    }

    public double getMaxEnergy() {
        return maxEnergy;
    }

    // 最大蓄電容量と最大しきい値をエネルギーアップグレードに対応
    @Override
    public void recalculateUpgradables(Upgrade upgrade) {
        super.recalculateUpgradables(upgrade);
        if (upgrade == Upgrade.ENERGY) {
            maxEnergy = MekanismUtils.getMaxEnergy(this, 5.0E9);
            maxThreshold = maxEnergy;
            setEnergy(Math.min(getMaxEnergy(), getEnergy()));
        }
    }

    // GUI側からの入力受け付け値の上限を最大蓄電容量に設定
    @ModifyConstant(
            method = "handlePacketData",
            constant = @Constant(doubleValue = 5.0E9)
    )
    private double modifyMaxThresholdLimit(double original) {
        return getMaxEnergy();
    }

    /* =========================
     *  スピードアップグレード 対応
     * ========================= */

    public void receiveLaserEnergy(double energy, EnumFacing side) {
        this.setEnergy(this.getEnergy() + energy * MekanismAUSUtils.getUpgradeMultiplier(Upgrade.SPEED, this.upgradeComponent));
    }

}
