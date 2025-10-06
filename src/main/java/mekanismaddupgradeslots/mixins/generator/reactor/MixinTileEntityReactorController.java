package mekanismaddupgradeslots.mixins.generator.reactor;

import mekanism.common.Upgrade;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NonNullListSynchronized;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.block.states.BlockStateReactor;
import mekanism.generators.common.tile.reactor.TileEntityReactorBlock;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
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

import javax.annotation.Nonnull;

@Mixin(TileEntityReactorController.class)
public abstract class MixinTileEntityReactorController extends TileEntityReactorBlock implements IUpgradeTile {

    @Unique
    private TileComponentUpgrade upgradeComponent; // アップグレードを追加するクラス

    /* ==============================================
     * アップグレードスロット関連
     * ============================================== */

    @Inject(method = "<init>", at = @At("RETURN"))
    public void init(CallbackInfo ci) {
        TileEntityReactorController self = (TileEntityReactorController) (Object) this;
        NonNullListSynchronized<ItemStack> newInventory = NonNullListSynchronized.withSize(2, new ItemStack((Item) null));
        newInventory.set(0, self.inventory.get(0));
        self.inventory = newInventory;
        // THREADアップグレード、エネルギーアップグレードにに対応
        this.upgradeComponent = new TileComponentUpgrade(this, 1, Upgrade.THREAD, MekanismGenerators.proxy, BlockStateReactor.ReactorBlockType.REACTOR_CONTROLLER.blockType.getBlock(), BlockStateReactor.ReactorBlockType.REACTOR_CONTROLLER.meta, BlockStateReactor.ReactorBlockType.REACTOR_CONTROLLER.guiId);
        upgradeComponent.setSupported(Upgrade.ENERGY);
    }

    /**
     * IUpgradeTileの必須メソッド: アップグレードコンポーネントを返す
     */
    @Override
    public TileComponentUpgrade getComponent() {
        return upgradeComponent;
    }

    @Shadow
    public boolean isFormed() {
        return false;
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull EnumFacing side) {
        return this.isFormed() ? new int[]{0, 1} : InventoryUtils.EMPTY;
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

    /* ==============================================
     * エネルギーアップグレードによる排出RF量の動的変更
     * ============================================== */

    @Override
    public double getMaxOutput() {
        return MekanismConfig.current().generators.solarGeneration.val() * 2 * MekanismAUSUtils.getUpgradeMultiplier(Upgrade.ENERGY, this.upgradeComponent);
    }

}
