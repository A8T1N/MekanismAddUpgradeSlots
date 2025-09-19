package mekanismaddupgradeslots;

import mekanism.common.Upgrade;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanismaddupgradeslots.config.ConfigHandler;

public final class MekanismAUSUtils {
    private MekanismAUSUtils() {
    }

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
     * <p>
     * MekanismTweaksMode（設定ファイルで変更可能、デフォルトはFalse）が有効時、9以上で10.0xのべき乗を適用。
     *
     * @return 計算された速度倍率（最低でも {@code 1.0}）
     */
    public static float getUpgradeMultiplier(Upgrade type, TileComponentUpgrade upgradeComponent) {
        if (!upgradeComponent.isUpgradeInstalled(type)) {
            return 1.0F;
        }

        int installCount = upgradeComponent.getUpgrades(type); // 0～8段階
        float[] multipliers = {
                1.0F,   // インストール数 : 0
                1.33F,  // インストール数 : 1
                1.78F,  // インストール数 : 2
                2.37F,  // インストール数 : 3
                3.16F,  // インストール数 : 4
                4.22F,  // インストール数 : 5
                5.62F,  // インストール数 : 6
                7.5F,   // インストール数 : 7
                10.0F   // インストール数 : 8
        };

        return ConfigHandler.MekanismTweaksMode && installCount >= 9
                ? (float) Math.pow(10.0, installCount / 8) * multipliers[installCount % 8]
                : multipliers[Math.min(installCount, 8)];
    }

}
