package xin.vanilla.narcissus.network.packet;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import xin.vanilla.narcissus.enums.EnumCommandType;
import xin.vanilla.narcissus.network.Packet;
import xin.vanilla.narcissus.util.NarcissusUtils;

public class TpBackNotice implements Packet {

    public TpBackNotice() {
    }

    public TpBackNotice(FriendlyByteBuf buf) {
    }


    public static void handle(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        server.execute(() -> {
            if (player != null) {
                NarcissusUtils.executeCommand(player, NarcissusUtils.getCommand(EnumCommandType.TP_BACK));
            }
        });
    }

    @Override
    public void encode(FriendlyByteBuf buf) {

    }
}
