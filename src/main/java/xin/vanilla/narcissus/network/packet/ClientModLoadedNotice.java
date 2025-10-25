package xin.vanilla.narcissus.network.packet;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import xin.vanilla.narcissus.data.player.PlayerTeleportData;
import xin.vanilla.narcissus.network.Packet;

public class ClientModLoadedNotice implements Packet {

    public ClientModLoadedNotice() {
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
    }

    public static void handle(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        server.execute(() -> {
            if (player != null) {
                // 同步玩家传送数据到客户端
                PlayerTeleportData.syncPlayerData(player);
            }
        });
    }
}
