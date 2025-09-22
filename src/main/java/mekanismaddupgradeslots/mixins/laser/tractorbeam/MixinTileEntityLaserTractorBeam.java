package mekanismaddupgradeslots.mixins.laser.tractorbeam;

import mekanism.common.Upgrade;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.laser.TileEntityLaserTractorBeam;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import mekanism.common.util.NonNullListSynchronized;
import mekanismaddupgradeslots.MekanismAUSUtils;
import mekanismaddupgradeslots.config.ConfigHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityLaserTractorBeam.class)
public abstract class MixinTileEntityLaserTractorBeam extends TileEntityContainerBlock implements IUpgradeTile {

    @Shadow
    public double collectedEnergy;

    /* ==============================================
     * アップグレードスロット関連
     * ============================================== */
    @Unique
    private TileComponentUpgrade upgradeComponent; // アップグレードを追加するクラス

    public MixinTileEntityLaserTractorBeam(String name) {
        super(name);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void init(CallbackInfo ci) {
        TileEntityLaserTractorBeam self = (TileEntityLaserTractorBeam) (Object) this;
        // インベントリを拡張（サイズ28: スロット27: エネルギー, 1:アップグレード）
        NonNullListSynchronized<ItemStack> newInventory = NonNullListSynchronized.withSize(28, new ItemStack((Item) null));
        // 既存のスロット0のアイテムをコピー
        for (int i = 0; i < 27; i++) {
            newInventory.set(i, self.inventory.get(i));
        }
        self.inventory = newInventory;
        // TileComponentUpgradeをスロットID 27で初期化
        upgradeComponent = new TileComponentUpgrade(self, 27);
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
        this.collectedEnergy = Math.max(0.0, Math.min(energy, 5.0E9 * MekanismAUSUtils.getUpgradeMultiplier(Upgrade.ENERGY, this.upgradeComponent)));
    }

    /* =========================
     *  スピードアップグレード 対応
     * ========================= */

    public void receiveLaserEnergy(double energy, EnumFacing side) {
        this.setEnergy(this.getEnergy() + energy * MekanismAUSUtils.getUpgradeMultiplier(Upgrade.SPEED, this.upgradeComponent));
        System.out.println(ConfigHandler.MekanismTweaksMode);
    }
}
