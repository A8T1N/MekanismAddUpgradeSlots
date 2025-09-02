package mekanismaddupgradeslots;

import com.google.common.collect.Lists;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.List;

public class MixinLoader implements ILateMixinLoader {
    @Override
    public List<String> getMixinConfigs() {
        return Lists.newArrayList("mixins.mekanismaddupgradeslots.json");
    }
}

