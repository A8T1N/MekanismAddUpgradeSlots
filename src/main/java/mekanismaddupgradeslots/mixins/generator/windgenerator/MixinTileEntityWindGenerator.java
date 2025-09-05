package mekanismaddupgradeslots.mixins.generator.windgenerator;

import mekanism.common.Upgrade;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NonNullListSynchronized;
import mekanism.generators.common.tile.TileEntityGenerator;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TileEntityWindGenerator.class)
public abstract class MixinTileEntityWindGenerator extends TileEntityGenerator implements IUpgradeTile {

    /* ==============================================
     * アップグレードスロット関連メンバ・メソッド START
     * ============================================== */

    @Unique
    private static final int[] SLOTS = {0, 1}; // スロット0: エネルギー, 1:アップグレード
    @Unique
    private TileComponentUpgrade upgradeComponent;

    public MixinTileEntityWindGenerator(String soundPath, String name, double maxEnergy, double out) {
        super(soundPath, name, maxEnergy, out);
    }

    // コンストラクタでインベントリサイズを拡張し、TileComponentUpgradeを初期化
    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        TileEntityWindGenerator tile = (TileEntityWindGenerator) (Object) this;
        // インベントリを拡張（サイズ3: スロット0=エネルギー, 1,2=アップグレード）
        NonNullListSynchronized<ItemStack> newInventory = NonNullListSynchronized.withSize(SLOTS.length, ItemStack.EMPTY);
        // 既存のスロット0のアイテムをコピー
        newInventory.set(0, tile.inventory.get(0));
        tile.inventory = newInventory;
        // TileComponentUpgradeをスロットID 1で初期化
        upgradeComponent = new TileComponentUpgrade(tile, 1);
        upgradeComponent.setSupported(Upgrade.SPEED);
        upgradeComponent.setSupported(Upgrade.ENERGY);
    }

    // IUpgradeTileの必須メソッド: アップグレードコンポーネントを返す
    @Override
    public TileComponentUpgrade getComponent() {
        return upgradeComponent;
    }

    // getSlotsForFaceをオーバーライドしてアップグレードスロットを扱う
    @Inject(method = "getSlotsForFace", at = @At("HEAD"), cancellable = true)
    private void modifyGetSlotsForFace(EnumFacing side, CallbackInfoReturnable<int[]> cir) {
        cir.setReturnValue(SLOTS);
    }

    /* ==============================================
     * アップグレードスロット関連メンバ・メソッド END
     * ============================================== */

    /**
     *  エネルギーアップグレード 対応
     */
    @Override
    public void recalculateUpgradables(Upgrade upgrade) {
        super.recalculateUpgradables(upgrade);
        if (upgrade == Upgrade.ENERGY) {
            maxEnergy = MekanismUtils.getMaxEnergy(this, BASE_MAX_ENERGY);
            setEnergy(Math.min(getMaxEnergy(), getEnergy()));
        }
    }

}
