package mekanismaddupgradeslots.mixins.generators.windgenerator;

import mekanism.common.Upgrade;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.NonNullListSynchronized;
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
public abstract class MixinTileEntityWindGenerator implements IUpgradeTile{

    @Unique
    private TileComponentUpgrade upgradeComponent;
    @Unique
    private static final int[] SLOTS = {0, 1, 2}; // スロット0: エネルギー, 1,2: アップグレード

//     コンストラクタでインベントリサイズを拡張し、TileComponentUpgradeを初期化
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
}
