package mekanismaddupgradeslots.mixins.generator.wind;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import mekanism.common.Upgrade;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NonNullListSynchronized;
import mekanism.generators.common.tile.TileEntityGenerator;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import mekanismaddupgradeslots.MekanismAUSUtils;
import net.minecraft.item.Item;
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

    // TODO
    //  Muffling Upgradeに対応
    //  Multiplierによってプロペラの回転数を同期する。
    //  アップグレードGUIで戻るボタンが動作しない問題の修正 (GUI側の問題?)


    @Unique
    private static final int[] NEW_SLOTS = {0, 1}; // スロット0: エネルギー, 1:アップグレード
    @Unique
    private TileComponentUpgrade upgradeComponent; // アップグレードを追加するクラス

    // TileEntityGeneratorを継承したためコンストラクタを作成 意味は無い
    public MixinTileEntityWindGenerator(String soundPath, String name, double maxEnergy, double out) {
        super(soundPath, name, maxEnergy, out);
    }

    /* ==============================================
     * アップグレードスロット関連
     * ============================================== */

    // コンストラクタでインベントリサイズを拡張し、TileComponentUpgradeを初期化
    // SPEED,ENERGYアップグレードに対応
    @Inject(method = "<init>", at = @At("RETURN"))
    public void init(CallbackInfo ci) {
        TileEntityWindGenerator self = (TileEntityWindGenerator) (Object) this;
        // インベントリを拡張（サイズ2: スロット0: エネルギー, 1:アップグレード）
        NonNullListSynchronized<ItemStack> newInventory = NonNullListSynchronized.withSize(NEW_SLOTS.length, new ItemStack((Item) null));
        // 既存のスロット0のアイテムをコピー
        newInventory.set(0, self.inventory.get(0));
        self.inventory = newInventory;
        // TileComponentUpgradeをスロットID 1で初期化
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

    /**
     * インベントリスロット拡張のための Mixin:<br></br>
     * 元の TileEntity は SLOTS = {0} のみを返すが、
     * アップグレードスロットを追加したため {0, 1} を返すように差し替える。
     * getSlotsForFace の戻り値を強制的に上書き。
     */
    @Inject(method = "getSlotsForFace", at = @At("HEAD"), cancellable = true)
    private void modifyGetSlotsForFace(EnumFacing side, CallbackInfoReturnable<int[]> cir) {
        cir.setReturnValue(NEW_SLOTS);
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

    // 最終倍率にアップグレードのインストール数に基づいた倍率を掛ける。
    @ModifyReturnValue(method = "getMultiplier()F", at = @At("RETURN"))
    private float modifyMultiplier(float original) {
        // ローカル変数minGを直接参照できないため、元の戻り値(original = toGen / minG)を使用
        // original * minGでtoGenを復元し、アップグレード乗数を適用して再度minGで割る
        // 実際にはminGを@Localでキャプチャ可能だが、今回は簡略化
        return original * MekanismAUSUtils.getUpgradeMultiplier(Upgrade.SPEED, this.upgradeComponent);
    }

    /*
     * =========================================
     * エネルギーアップグレードによる排出RF量の動的変更
     * =========================================
     */

    /**
     * WindGenerator の base 出力上限 (windGenerationMax * 2) に Speed() の倍率を掛けた値を返す。
     */
    @Override
    public double getMaxOutput() {
        return MekanismConfig.current().generators.windGenerationMax.val() * 2.0 * MekanismAUSUtils.getUpgradeMultiplier(Upgrade.ENERGY, this.upgradeComponent);
    }

}
