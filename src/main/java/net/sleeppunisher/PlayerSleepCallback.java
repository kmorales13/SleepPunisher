package net.sleeppunisher;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;

public interface PlayerSleepCallback {

    Event<PlayerSleepCallback> EVENT = EventFactory.createArrayBacked(PlayerSleepCallback.class,
            (listeners) -> (player) -> {
                for (PlayerSleepCallback listener : listeners) {
                    ActionResult result = listener.interact(player);

                    if (result != ActionResult.PASS) {
                        return result;
                    }
                }

                return ActionResult.PASS;
            });

    ActionResult interact(ServerPlayerEntity player);
}