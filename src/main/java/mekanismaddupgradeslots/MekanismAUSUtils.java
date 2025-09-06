package mekanismaddupgradeslots;

import mekanism.common.Upgrade;
import mekanism.common.tile.component.TileComponentUpgrade;

public final class MekanismAUSUtils {
    private MekanismAUSUtils(){}

    /**
     * インストールされている {@link Upgrade} のインストール数に基づいて、
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
     * アップグレードがインストールされていない場合、または段階が範囲外の場合は、
     * デフォルト値として {@code 1.0} を返します。
     *
     * @return 計算された速度倍率（最低でも {@code 1.0}）
     */
    public  static float getMultiplier(Upgrade type,TileComponentUpgrade upgradeComponent) {
        if (!upgradeComponent.isUpgradeInstalled(type)) {
            return 1.0F;
        }

        int installCount = upgradeComponent.getUpgrades(type); // 0～8段階
        float[] multipliers = {
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

        if (installCount < 0 || installCount >= multipliers.length) {
            return 1.0F;
        }

        return multipliers[installCount];
    }

}
