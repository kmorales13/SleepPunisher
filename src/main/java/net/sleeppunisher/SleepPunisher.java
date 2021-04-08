package net.sleeppunisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.oroarmor.config.Config;
import com.oroarmor.config.ConfigItem;
import com.oroarmor.config.ConfigItemGroup;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.sleeppunisher.utils.Probability;

public class SleepPunisher implements ModInitializer {
    public static Config CONFIG = new SleepPunisherConfig();

    private List<Integer> inx = new ArrayList<Integer>();
    private List<Integer> freq = new ArrayList<Integer>();
    private List<ConfigItem<?>> punishments;

    private Random rand = new Random();

    @Override
    public void onInitialize() {
        System.out.println("[SleepPunisher] Loading punishments...");

        initConfig();
        registerCallback();

        System.out.println("[SleepPunisher] We are ready!");
    }

    private void initConfig() {
        CONFIG.readConfigFromFile();
        CONFIG.saveConfigToFile();
        ServerLifecycleEvents.SERVER_STOPPED.register(instance -> CONFIG.saveConfigToFile());

        try {
            punishments = CONFIG.getConfigs().get(0).getConfigs();
        } catch (Exception e) {
            System.out.println("[SleepPunisher] Error while reading config file, maybe it is malformed? Exiting now :(");
            e.printStackTrace();
            System.exit(0);
        }

        int sum = 0;
        for (int x = 0; x < punishments.size(); x++) {
            ConfigItemGroup item = (ConfigItemGroup) punishments.get(x);

            try {
                if (!item.toJson().get("enabled").getAsBoolean())
                    continue;
            } catch (Exception e) {
                System.out.println("[SleepPunisher] Error reading config for '" + item.getName() + "', ommiting");
            }

            Integer prob;
            try {
                prob = item.toJson().get("probability").getAsInt();
            } catch (Exception e) {
                prob = 0;
                System.out.println(
                        "[SleepPunisher] Error getting probability for '" + item.getName() + "', defaulting to 0");
            }

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

        try {
            ConfigItem<?> config = punishments.get(index);

            switch (config.getName()) {
            case "killPlayer":
                killPlayer(player);
                break;
            case "starvePlayer":
                starvePlayer(player);
                break;
            case "damagePlayer":
                damagePlayer(player);
                break;
            case "teleportPlayer":
                teleportPlayer(player, (ConfigItemGroup) config);
                break;
            case "raidPlayer":
                raidPlayer(player, (ConfigItemGroup) config);
                break;
            }
        } catch (Exception e) {
            System.out.println("[SleepPunisher] Uh-oh an error ocurred! Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void killPlayer(ServerPlayerEntity player) {
        player.setSpawnPoint(player.getSpawnPointDimension(), null, 0, false, false);
        player.kill();
        player.sendMessage(
                new LiteralText(
                        "\u00A7c[SleepPunisher]: You have died in your dreams and are back where you started..."),
                false);
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

    private void teleportPlayer(ServerPlayerEntity player, ConfigItemGroup config) {
        Vec3d pos = player.getPos();
        int maxDistance;
        try {
            maxDistance = config.toJson().get("maxDistance").getAsInt();
        } catch (Exception e) {
            maxDistance = 32;
            System.out.println("[SleepPunisher] Error getting maxDistance for 'teleportPlayer', defaulting to 32");
        }

        double posX = pos.x + (rand.nextDouble() + maxDistance);
        double posZ = pos.z + (rand.nextDouble() + maxDistance);

        int posY = player.getServerWorld().getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, (int) posX, (int) posZ);

        player.refreshPositionAfterTeleport(posX, posY, posZ);
        player.sendMessage(
                new LiteralText("\u00A7c[SleepPunisher]: You have walked away from bed on your sleep, be careful!"),
                false);
    }

    private void raidPlayer(ServerPlayerEntity player, ConfigItemGroup config) {
        ServerWorld world = player.getServerWorld();
        ItemStack bowItem = new ItemStack(Items.CROSSBOW);
        Vec3d pos = player.getPos();

        int maxEntities;
        try {
            maxEntities = config.toJson().get("maxEntities").getAsInt();
        } catch (Exception e) {
            maxEntities = 3;
            System.out.println("[SleepPunisher] Error getting maxEntities for 'raidPlayer', defaulting to 3");
        }

        int entities = rand.nextInt(maxEntities - 1) + 1;

        for (int x = 0; x < entities; x++) {
            PillagerEntity pillager = new PillagerEntity(EntityType.PILLAGER, world);
            pillager.setStackInHand(Hand.MAIN_HAND, bowItem);
            pillager.updatePosition(pos.x + (x + 1), pos.y, pos.z + (x + 1));
            world.spawnEntity(pillager);
        }

        player.sendMessage(new LiteralText("\u00A7c[SleepPunisher]: Wake up, you are being raided!"), false);
    }
}
