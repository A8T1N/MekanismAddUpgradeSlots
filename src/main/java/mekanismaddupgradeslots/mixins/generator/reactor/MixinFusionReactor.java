package mekanismaddupgradeslots.mixins.generator.reactor;

import mekanism.common.Upgrade;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.config.options.DoubleOption;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.generators.common.FusionReactor;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import mekanismaddupgradeslots.MekanismAUSUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FusionReactor.class)
public abstract class MixinFusionReactor {

    @Shadow
    public TileEntityReactorController controller;


    /* =========================
     *  THREADアップグレード 対応
     * ========================= */

    // プラズマ温度の強化
    @Redirect(
            method = "burnFuel",
            at = @At(
                    value = "INVOKE",
                    target = "Lmekanism/common/config/options/DoubleOption;val()D"
            )
    )
    private double ThreadPlasmaTemperature(DoubleOption option) {
        IUpgradeTile upgradeTile = (IUpgradeTile) this.controller;
        TileComponentUpgrade upgradeComponent = upgradeTile.getComponent();
        float multiplier = MekanismAUSUtils.getUpgradeMultiplier(Upgrade.THREAD, upgradeComponent);
        return option.val() * (multiplier * 2); // 10倍だと物足りない
    }

    /* ==============================================
     * THREADアップグレードによるタンク容量の変更
     * ============================================== */

    // WaterTank
    @ModifyArg(
            method = "setInjectionRate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/fluids/FluidTank;setCapacity(I)V"
            ),
            index = 0
    )
    private int applyThreadMultiplierWaterTank(int original) {
        IUpgradeTile upgradeTile = (IUpgradeTile) this.controller;
        TileComponentUpgrade comp = upgradeTile.getComponent();
        float multiplier = MekanismAUSUtils.getUpgradeMultiplier(Upgrade.THREAD, comp);
        return (int) (original * multiplier);
    }

    // SteamTank
    @ModifyArg(
            method = "setInjectionRate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/fluids/FluidTank;setCapacity(I)V",
                    ordinal = 1
            ),
            index = 0
    )
    private int applyThreadMultiplierSteamTank(int original) {
        IUpgradeTile upgradeTile = (IUpgradeTile) this.controller;
        TileComponentUpgrade comp = upgradeTile.getComponent();
        float multiplier = MekanismAUSUtils.getUpgradeMultiplier(Upgrade.THREAD, comp);
        return (int) (original * multiplier);
    }


    /* ==============================================
     * THREADアップグレードによる水搬入量の強化
     * ============================================== */

    @ModifyVariable(
            method = "transferHeat",
            at = @At(
                    value = "STORE",
                    ordinal = 0 // waterToVaporizeの初期代入箇所
            ),
            ordinal = 0
    )
    private int applyThreadMultiplierWaterIntake(int original) {
        IUpgradeTile upgradeTile = (IUpgradeTile) this.controller;
        TileComponentUpgrade comp = upgradeTile.getComponent();
        float multiplier = MekanismAUSUtils.getUpgradeMultiplier(Upgrade.THREAD, comp);
        return (int) (original * multiplier);
    }

    /* ==============================================
     * THREADアップグレードによる蒸気の排出量の強化
     * ============================================== */

    @ModifyVariable(
            method = "transferHeat",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lmekanism/forge/fluids/FluidTank;fill(Lnet/minecraftforge/fluids/FluidStack;Z)I"
            ),
            ordinal = 0
    )
    private int modifySteamOutput(int original) {
        IUpgradeTile upgradeTile = (IUpgradeTile) this.controller;
        TileComponentUpgrade upgradeComponent = upgradeTile.getComponent();
        float multiplier = MekanismAUSUtils.getUpgradeMultiplier(Upgrade.THREAD, upgradeComponent);
        return (int) (original * multiplier);
    }


}
