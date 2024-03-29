package net.sleeppunisher;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import com.oroarmor.config.Config;
import com.oroarmor.config.ConfigItem;
import com.oroarmor.config.ConfigItemGroup;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.feature.StructureFeature;
import net.sleeppunisher.utils.Probability;

public class SleepPunisher implements ModInitializer {
    public static Config CONFIG = new SleepPunisherConfig();

    private List<Integer> inx = new ArrayList<Integer>();
    private List<Integer> freqs = new ArrayList<Integer>();
    private List<ConfigItem<?>> punishments;

    private Random rand = new Random();
    private boolean runThunder = false;

    @Override
    public void onInitialize() {
        System.out.println("[SleepPunisher] Loading punishments...");

        initConfig();
        readConfig();

        PlayerSleepCallback.EVENT.register((player) -> {
            rollPunishment(player);
            return ActionResult.PASS;
        });

        System.out.println("[SleepPunisher] We are ready!");
    }

    private void initConfig() {
        CONFIG.readConfigFromFile();
        CONFIG.saveConfigToFile();
        ServerLifecycleEvents.SERVER_STOPPED.register(instance -> CONFIG.saveConfigToFile());
    }

    private void readConfig() {
        try {
            punishments = CONFIG.getConfigs().get(0).getConfigs();
        } catch (Exception e) {
            System.out
                    .println("[SleepPunisher] Error while reading config file, maybe it is malformed? Exiting now :(");
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
                continue;
            }

            Integer prob = 0;
            try {
                prob = item.toJson().get("probability").getAsInt();
                prob = Math.max(0, Math.min(prob, 100));
            } catch (Exception e) {
                System.out.println(
                        "[SleepPunisher] Error getting probability for '" + item.getName() + "', defaulting to 0");
            }

            inx.add(x);
            freqs.add(prob);

            sum += prob;
        }

        if (sum < 100) {
            inx.add(-1);
            freqs.add(100 - sum);
        }
    }

    private void rollPunishment(ServerPlayerEntity player) {
        Integer index = Probability.randNum(inx.toArray(new Integer[0]), freqs.toArray(new Integer[0]), inx.size());

        try {
            ConfigItemGroup config = (ConfigItemGroup) punishments.get(index);

            Method method = getClass().getDeclaredMethod(config.getName(), ServerPlayerEntity.class,
                    ConfigItemGroup.class);
            method.invoke(this, player, config);
        } catch (ArrayIndexOutOfBoundsException ae) {
            return;
        } catch (Exception e) {
            System.out.println(
                    "[SleepPunisher] An error ocurred while trying to punish a player: " + player.getName().asString());
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    private void killPlayer(ServerPlayerEntity player, ConfigItemGroup config) {
        player.setSpawnPoint(player.getSpawnPointDimension(), null, 0, false, false);
        player.kill();
        player.sendMessage(new LiteralText("\u00A7c~You have died in your dreams and are back where you started..."),
                false);
    }

    @SuppressWarnings("unused")
    private void starvePlayer(ServerPlayerEntity player, ConfigItemGroup config) {
        player.getHungerManager().setFoodLevel(0);
        player.sendMessage(new LiteralText("\u00A7c~You slept for too long and are now starving..."), false);
    }

    @SuppressWarnings("unused")
    private void damagePlayer(ServerPlayerEntity player, ConfigItemGroup config) {
        player.setHealth(player.getMaxHealth() / 2f);
        player.sendMessage(new LiteralText("\u00A7c~You have awoken with bruises, what happened?"), false);
    }

    @SuppressWarnings("unused")
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
        player.sendMessage(new LiteralText("\u00A7c~You have walked away from bed on your sleep, be careful!"), false);
    }

    @SuppressWarnings("unused")
    private void raidPlayer(ServerPlayerEntity player, ConfigItemGroup config) {
        ServerWorld world = player.getServerWorld();

        boolean isVillage = false;
        try {
            ChunkSectionPos chunkPos = ChunkSectionPos.from(player);
            Stream<?> nearStructures = world.getStructures(chunkPos, StructureFeature.VILLAGE);
            isVillage = nearStructures != null && nearStructures.count() > 0;
        } catch (Exception e) {
            System.out.println("[SleepPunisher] Error getting nearStructures for player '" + player.getName() + "'");
        }

        if (isVillage) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.BAD_OMEN, 999));
        } else {
            ItemStack bowItem = new ItemStack(Items.CROSSBOW);
            Vec3d pos = player.getPos();

            int maxEntities = 4;
            try {
                maxEntities = config.toJson().get("maxEntities").getAsInt();
            } catch (Exception e) {
                System.out.println("[SleepPunisher] Error getting maxEntities for 'raidPlayer', defaulting to 3");
            }

            int entities = rand.nextInt(maxEntities - 1) + 1;
            for (int x = 0; x < entities; x++) {
                PillagerEntity pillager = new PillagerEntity(EntityType.PILLAGER, world);
                pillager.setStackInHand(Hand.MAIN_HAND, bowItem);
                pillager.updatePosition(pos.x + (x + 1), pos.y, pos.z + (x + 1));
                world.spawnEntity(pillager);
            }
        }

        player.sendMessage(new LiteralText("\u00A7c~Wake up, you are being raided!"), false);
    }

    @SuppressWarnings("unused")
    private void stoveFire(ServerPlayerEntity player, ConfigItemGroup config) {
        ServerWorld world = player.getServerWorld();

        int findRadius = 5;
        try {
            findRadius = config.toJson().get("findRadius").getAsInt();
            findRadius = Math.max(1, Math.min(findRadius, 10));
        } catch (Exception e) {
            System.out.println("[SleepPunisher] Error getting findRadius for 'stoveFire', defaulting to 5");
        }

        BlockPos bPos = player.getBlockPos();
        BlockPos torchBlock = null;

        Iterable<BlockPos> findArea = BlockPos.iterateOutwards(bPos, findRadius, findRadius, findRadius);
        for (BlockPos blockPos : findArea) {
            try {
                BlockEntity block = world.getBlockEntity(blockPos);
                BlockEntityType<?> blockType = block.getType();

                if (blockType == BlockEntityType.FURNACE || blockType == BlockEntityType.BLAST_FURNACE
                        || blockType == BlockEntityType.SMOKER) {
                    int lit = block.getCachedState().getLuminance();

                    if (lit > 0) {
                        torchBlock = blockPos.mutableCopy();
                        break;
                    }
                }
            } catch (NullPointerException e) {
            }
        }

        if (torchBlock != null) {
            Iterable<BlockPos> torchArea = BlockPos.iterateOutwards(torchBlock, 2, 0, 2);

            player.sendMessage(new LiteralText("\u00A7c~Oh no, you left the stove on!"), false);

            for (BlockPos blockPos : torchArea) {
                try {
                    BlockPos.Mutable firePos = blockPos.mutableCopy();
                    int fireFloor = Math.min(firePos.getY(),
                            world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, firePos.getX(), firePos.getZ()));
                    firePos.setY(fireFloor);

                    if (AbstractFireBlock.method_30032(world, firePos, player.getHorizontalFacing())) {
                        BlockState fireState = AbstractFireBlock.getState(world, firePos);
                        world.setBlockState(firePos, fireState, 11);
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    @SuppressWarnings("unused")
    private void thunderWeather(ServerPlayerEntity player, ConfigItemGroup config) {
        ServerWorld world = player.getServerWorld();
        runThunder = true;

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (runThunder) {
                world.setWeather(0, 6000, true, true);
                player.sendMessage(new LiteralText("\u00A7c~You have awoken to the sounds of thunder..."), false);
                runThunder = false;
            }
        });
    }
}
