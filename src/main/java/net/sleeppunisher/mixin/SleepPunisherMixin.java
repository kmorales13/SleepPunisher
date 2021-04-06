package net.sleeppunisher.mixin;

import net.sleeppunisher.PlayerSleepCallback;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(ServerPlayerEntity.class)
public class SleepPunisherMixin {

    @Inject(at = @At("TAIL"), method = "wakeUp")
    private void onSleep(CallbackInfo info) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
        PlayerSleepCallback.EVENT.invoker().interact(self);
    }
}