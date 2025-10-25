package xin.vanilla.narcissus.mixin;

import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xin.vanilla.narcissus.util.mixin.IServerPlayerLanguage;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin implements IServerPlayerLanguage {
    @Unique
    private String language = "en_us";

    @Override
    public String getLanguage() {
        return this.language;
    }

    @Inject(method = "updateOptions", at = @At("TAIL"))
    private void onUpdateOptions(ServerboundClientInformationPacket packet, CallbackInfo ci) {
        this.language = packet.language();
    }
}