package xin.vanilla.narcissus.network.packet;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import xin.vanilla.narcissus.NarcissusFarewell;
import xin.vanilla.narcissus.data.TeleportRequest;
import xin.vanilla.narcissus.enums.EnumCommandType;
import xin.vanilla.narcissus.enums.EnumI18nType;
import xin.vanilla.narcissus.enums.EnumTeleportType;
import xin.vanilla.narcissus.network.Packet;
import xin.vanilla.narcissus.util.I18nUtils;
import xin.vanilla.narcissus.util.NarcissusUtils;

import java.util.Comparator;

public class TpNoNotice implements Packet {

    public TpNoNotice() {
    }

    public TpNoNotice(FriendlyByteBuf buf) {
    }


    public static void handle(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        server.execute(() -> {
            if (player != null) {
                EnumTeleportType teleportType = NarcissusFarewell.getTeleportRequest().values().stream()
                        .filter(request -> !request.isIgnore())
                        .filter(request -> request.getTarget().getUUID().equals(player.getUUID()))
                        .max(Comparator.comparing(TeleportRequest::getRequestTime))
                        .orElse(new TeleportRequest())
                        .getTeleportType();
                if (EnumTeleportType.TP_ASK == teleportType || EnumTeleportType.TP_HERE == teleportType) {
                    EnumCommandType type = EnumTeleportType.TP_HERE == teleportType ? EnumCommandType.TP_HERE_NO : EnumCommandType.TP_ASK_NO;
                    NarcissusUtils.executeCommand(player, NarcissusUtils.getCommand(type));
                } else {
                    NarcissusUtils.sendTranslatableMessage(player, I18nUtils.getKey(EnumI18nType.MESSAGE, "tp_ask_not_found"));
                }
            }
        });
    }

    @Override
    public void encode(FriendlyByteBuf buf) {

    }
}
