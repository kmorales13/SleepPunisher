package net.sleeppunisher;

import java.util.ArrayList;
import java.util.List;

import com.oroarmor.config.Config;
import com.oroarmor.config.ConfigItem;
import com.oroarmor.config.ConfigItemGroup;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.sleeppunisher.utils.Probability;

public class SleepPunisher implements ModInitializer {
    public static Config CONFIG = new SleepPunisherConfig();

    private List<Integer> inx = new ArrayList<Integer>();
    private List<Integer> freq = new ArrayList<Integer>();
    private List<ConfigItem<?>> punishments;

    @Override
    public void onInitialize() {
        System.out.println("[SleepPunisher] Loading...");

        initConfig();
        registerCallback();

        System.out.println("[SleepPunisher] We are ready!");
    }

    private void initConfig() {
        CONFIG.readConfigFromFile();
        CONFIG.saveConfigToFile();
        ServerLifecycleEvents.SERVER_STOPPED.register(instance -> CONFIG.saveConfigToFile());

        punishments = CONFIG.getConfigs().get(0).getConfigs();

        int sum = 0;

        for (int x = 0; x < punishments.size(); x++) {
            ConfigItemGroup item = (ConfigItemGroup) punishments.get(x);

            if (!item.toJson().get("enabled").getAsBoolean())
                continue;

            Integer prob = item.toJson().get("probability").getAsInt();

            inx.add(x);
            freq.add(prob);

            sum += prob;
        }

        if (sum < 100) {
            inx.add(-1);
            freq.add(100 - sum);
        }
    }

    private void registerCallback() {
        PlayerSleepCallback.EVENT.register((player) -> {
            doPunishment(player);
            return ActionResult.PASS;
        });
    }

    private void doPunishment(ServerPlayerEntity player) {
        Integer index = Probability.randNum(inx.toArray(new Integer[0]), freq.toArray(new Integer[0]), inx.size());
        System.out.println("[Sleep Punisher] Got: " + index);

        try {
            ConfigItem<?> item = punishments.get(index);

            switch (item.getName()) {
            case "killPlayer":
                killPlayer(player);
                break;
            case "starvePlayer":
                starvePlayer(player);
                break;
            case "damagePlayer":
                damagePlayer(player);
                break;
            }
        } catch (Exception e) {
            // Do nothing :D
        }
    }

    private void killPlayer(ServerPlayerEntity player) {
        //System.out.println("[SleepPunisher] ree " + player.isAlive() + " " + player.isSleeping());
        player.kill();
        player.sendMessage(new LiteralText("\u00A7c[SleepPunisher]: You have died in your dreams..."), false);
    }

    private void starvePlayer(ServerPlayerEntity player) {
        player.getHungerManager().setFoodLevel(0);
        player.sendMessage(new LiteralText("\u00A7c[SleepPunisher]: You slept for too long and are now starving..."),
                false);
    }

    private void damagePlayer(ServerPlayerEntity player) {
        player.setHealth(player.getMaxHealth() / 2f);
        player.sendMessage(new LiteralText("\u00A7c[SleepPunisher]: You have awoken with bruises, what happened?"),
                false);
    }
}
