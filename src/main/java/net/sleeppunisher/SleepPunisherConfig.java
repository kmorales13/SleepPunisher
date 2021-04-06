package net.sleeppunisher;

import java.io.File;
import java.util.List;

import com.oroarmor.config.*;

import net.fabricmc.loader.api.FabricLoader;
import static com.google.common.collect.ImmutableList.of;

public class SleepPunisherConfig extends Config {

    public static final ConfigItemGroup mainGroup = new ConfigGroup();
    public static final List<ConfigItemGroup> configs = of(mainGroup);

    public SleepPunisherConfig() {
        super(configs, new File(FabricLoader.getInstance().getConfigDir().toFile(), "sleep_punisher.json"),
                "sleeppunisher");
    }

    public static class ConfigGroup extends ConfigItemGroup {
        public ConfigGroup() {
            super(of(new KillOptions(), new DamageOptions(), new StarveOptions()), "punishments");
        }

        public static class KillOptions extends ConfigItemGroup {
            public static final ConfigItem<Boolean> killEnabled = new ConfigItem<>("enabled", true, "Enabled");
            public static final ConfigItem<Integer> killProb = new ConfigItem<>("probability", 10, "Percentage probability 1-100");

            public KillOptions() {
                super(of(killEnabled, killProb), "killPlayer");
            }
        }

        public static class DamageOptions extends ConfigItemGroup {
            public static final ConfigItem<Boolean> damageEnabled = new ConfigItem<>("enabled", true, "Enabled");
            public static final ConfigItem<Integer> damageProb = new ConfigItem<>("probability", 20, "Percentage probability 1-100");

            public DamageOptions() {
                super(of(damageEnabled, damageProb), "damagePlayer");
            }
        }

        public static class StarveOptions extends ConfigItemGroup {
            public static final ConfigItem<Boolean> starveEnabled = new ConfigItem<>("enabled", true, "Enabled");
            public static final ConfigItem<Integer> starveProb = new ConfigItem<>("probability", 30, "Percentage probability 1-100");

            public StarveOptions() {
                super(of(starveEnabled, starveProb), "starvePlayer");
            }
        }
    }
}