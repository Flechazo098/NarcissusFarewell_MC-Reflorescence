package xin.vanilla.narcissus.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProgressListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xin.vanilla.narcissus.data.player.PlayerDataManager;

import javax.annotation.Nullable;

@Mixin(ServerLevel.class)
public class ServerLevelSaveMixin {

    @Inject(method = "save", at = @At("RETURN"))
    private void onSave(@Nullable ProgressListener progressListener, boolean flush, boolean suppressLogs, CallbackInfo ci) {
        PlayerDataManager.instance().saveAllForWorld();
    }
}
