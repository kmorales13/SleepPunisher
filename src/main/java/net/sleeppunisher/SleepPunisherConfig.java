package net.sleeppunisher;

import java.io.File;
import java.util.List;

import com.oroarmor.config.*;

import net.fabricmc.loader.api.FabricLoader;
import static com.google.common.collect.ImmutableList.of;

public class SleepPunisherConfig extends Config {

    public static final ConfigItemGroup punishmentsGroup = new PunishmentsGroup();
    public static final List<ConfigItemGroup> configs = of(punishmentsGroup);

    public SleepPunisherConfig() {
        super(configs, new File(FabricLoader.getInstance().getConfigDir().toFile(), "sleep_punisher.json"),
                "sleeppunisher");
    }

    public static class PunishmentsGroup extends ConfigItemGroup {
        public PunishmentsGroup() {
            super(of(new KillOptions(), new DamageOptions(), new StarveOptions(), new TeleportOptions(),
                    new RaidOptions(), new StoveOptions(), new ThunderOptions()), "punishments");
        }

        public static class KillOptions extends ConfigItemGroup {
            public static final ConfigItem<Boolean> killEnabled = new ConfigItem<>("enabled", true, "Enabled");
            public static final ConfigItem<Integer> killProb = new ConfigItem<>("probability", 5,
                    "Percentage probability 1-100");

            public KillOptions() {
                super(of(killEnabled, killProb), "killPlayer");
            }
        }

        public static class DamageOptions extends ConfigItemGroup {
            public static final ConfigItem<Boolean> damageEnabled = new ConfigItem<>("enabled", true, "Enabled");
            public static final ConfigItem<Integer> damageProb = new ConfigItem<>("probability", 5,
                    "Percentage probability 1-100");

            public DamageOptions() {
                super(of(damageEnabled, damageProb), "damagePlayer");
            }
        }

        public static class StarveOptions extends ConfigItemGroup {
            public static final ConfigItem<Boolean> starveEnabled = new ConfigItem<>("enabled", true, "Enabled");
            public static final ConfigItem<Integer> starveProb = new ConfigItem<>("probability", 5,
                    "Percentage probability 1-100");

            public StarveOptions() {
                super(of(starveEnabled, starveProb), "starvePlayer");
            }
        }

        public static class TeleportOptions extends ConfigItemGroup {
            public static final ConfigItem<Boolean> teleportEnabled = new ConfigItem<>("enabled", true, "Enabled");
            public static final ConfigItem<Integer> teleportProb = new ConfigItem<>("probability", 5,
                    "Percentage probability 1-100");
            public static final ConfigItem<Integer> teleportDistance = new ConfigItem<>("maxDistance", 32,
                    "Max distance player can sleep walk to");

            public TeleportOptions() {
                super(of(teleportEnabled, teleportProb, teleportDistance), "teleportPlayer");
            }
        }

        public static class RaidOptions extends ConfigItemGroup {
            public static final ConfigItem<Boolean> raidEnabled = new ConfigItem<>("enabled", true, "Enabled");
            public static final ConfigItem<Integer> raidProb = new ConfigItem<>("probability", 5,
                    "Percentage probability 1-100");
            public static final ConfigItem<Integer> raidEntities = new ConfigItem<>("maxEntities", 4,
                    "Max entities that can spawn");

            public RaidOptions() {
                super(of(raidEnabled, raidProb, raidEntities), "raidPlayer");
            }
        }

        public static class StoveOptions extends ConfigItemGroup {
            public static final ConfigItem<Boolean> stoveEnabled = new ConfigItem<>("enabled", true, "Enabled");
            public static final ConfigItem<Integer> stoveProb = new ConfigItem<>("probability", 5,
                    "Percentage probability 1-100");
            public static final ConfigItem<Integer> stoveRadius = new ConfigItem<>("findRadius", 5,
                    "Block radius 1-10");

            public StoveOptions() {
                super(of(stoveEnabled, stoveProb, stoveRadius), "stoveFire");
            }
        }

        public static class ThunderOptions extends ConfigItemGroup {
            public static final ConfigItem<Boolean> thunderEnabled = new ConfigItem<>("enabled", true, "Enabled");
            public static final ConfigItem<Integer> thunderProb = new ConfigItem<>("probability", 5,
                    "Percentage probability 1-100");

            public ThunderOptions() {
                super(of(thunderEnabled, thunderProb), "thunderWeather");
            }
        }
    }
}