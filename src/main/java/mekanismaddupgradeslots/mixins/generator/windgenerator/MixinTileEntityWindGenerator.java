package mekanismaddupgradeslots.mixins.generator.windgenerator;

import mekanism.common.Upgrade;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NonNullListSynchronized;
import mekanism.generators.common.tile.TileEntityGenerator;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
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
        TileEntityWindGenerator self = (TileEntityWindGenerator) (Object) this;
        // インベントリを拡張（サイズ3: スロット0=エネルギー, 1,2=アップグレード）
        NonNullListSynchronized<ItemStack> newInventory = NonNullListSynchronized.withSize(SLOTS.length, ItemStack.EMPTY);
        // 既存のスロット0のアイテムをコピー
        newInventory.set(0, self.inventory.get(0));
        self.inventory = newInventory;
        // TileComponentUpgradeをスロットID 1で初期化
        upgradeComponent = new TileComponentUpgrade(self, 1);
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

    /**
     * インストールされている {@link Upgrade#SPEED} アップグレードの数に基づいて、
     * 倍率を返します。
     * <p>
     * アップグレード段階と倍率の対応は以下の通りです:
     * <pre>
     * 0 -> 1.00x
     * 1 -> 1.33x
     * 2 -> 1.78x
     * 3 -> 2.37x
     * 4 -> 3.16x
     * 5 -> 4.22x
     * 6 -> 5.62x
     * 7 -> 7.50x
     * 8 -> 10.00x
     * </pre>
     * SPEED アップグレードがインストールされていない場合、または段階が範囲外の場合は、
     * デフォルト値として {@code 1.0} を返します。
     *
     * @return 計算された速度倍率（最低でも {@code 1.0}）
     */
    public float Speed() {
        if (!upgradeComponent.isUpgradeInstalled(Upgrade.SPEED)) {
            return 1.0F;
        }

        int speed = upgradeComponent.getUpgrades(Upgrade.SPEED); // 0～8段階
        float[] speedMultipliers = {
                1.0F,   // 0段階
                1.33F,  // 1段階
                1.78F,  // 2段階
                2.37F,  // 3段階
                3.16F,  // 4段階
                4.22F,  // 5段階
                5.62F,  // 6段階
                7.5F,   // 7段階
                10.0F   // 8段階
        };

        if (speed < 0 || speed >= speedMultipliers.length) {
            return 1.0F;
        }

        return speedMultipliers[speed];
    }

    public float getMultiplier() {
        BlockPos top = this.getPos().up(4);
        if (this.world.canSeeSky(top)) {
            int minY = MekanismConfig.current().generators.windGenerationMinY.val();
            int maxY = MekanismConfig.current().generators.windGenerationMaxY.val();
            float clampedY = (float) Math.min(maxY, Math.max(minY, top.getY()));
            float minG = (float) MekanismConfig.current().generators.windGenerationMin.val();
            float maxG = (float) MekanismConfig.current().generators.windGenerationMax.val();
            int rangeY = maxY < minY ? minY - maxY : maxY - minY;
            float rangG = maxG < minG ? minG - maxG : maxG - minG;
            float slope = rangG / (float) rangeY;
            float toGen = minG + slope * (clampedY - (float) minY);
            return toGen * Speed() / minG;
        } else {
            return 0.0F;
        }
    }


    /*
     * =========================================
     * スピードアップグレードによる排出RF量の動的変更
     * =========================================
     */

    /**
     * WindGenerator の base 出力上限 (windGenerationMax * 2) に Speed() の倍率を掛けた値を返す。
     * これによりアップグレードの枚数に応じて排出レートが上昇する。
     */
    @Override
    public double getMaxOutput() {
        return MekanismConfig.current().generators.windGenerationMax.val() * 2.0 * Speed();
    }
    
}
