package mekanismaddupgradeslots.config;


import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class ConfigHandler {
    public static Configuration config;

    public static boolean MekanismTweaksMode = false;

    public static void init(File configFile) {
        config = new Configuration(configFile);
        try {
            config.load();

            MekanismTweaksMode = config.getBoolean(
                    "Enable MekanismTweaks Mode",
                    "Upgrade",
                    false,
                    "Enabling MekanismTweaks Mode allows up to 64 upgrades to be applied."

            );
        } catch (Exception e) {
            System.err.println("Error loading MekanismAddUpgradeSlots config: " + e.getMessage());
        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }
}
