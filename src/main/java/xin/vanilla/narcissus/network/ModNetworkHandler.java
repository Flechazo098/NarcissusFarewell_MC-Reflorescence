package xin.vanilla.narcissus.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import xin.vanilla.narcissus.NarcissusFarewell;
import xin.vanilla.narcissus.network.packet.*;

public class ModNetworkHandler {
    // 定义网络通道ID
    public static final ResourceLocation PLAYER_DATA_SYNC = NarcissusFarewell.createResource("player_data_sync");
    public static final ResourceLocation CLIENT_MOD_LOADED = NarcissusFarewell.createResource("client_mod_loaded");
    public static final ResourceLocation TP_HOME = NarcissusFarewell.createResource("tp_home");
    public static final ResourceLocation TP_BACK = NarcissusFarewell.createResource("tp_back");
    public static final ResourceLocation TP_YES = NarcissusFarewell.createResource("tp_yes");
    public static final ResourceLocation TP_NO = NarcissusFarewell.createResource("tp_no");

    public static void registerPackets() {
        // 注册服务端接收的数据包
        ServerPlayNetworking.registerGlobalReceiver(CLIENT_MOD_LOADED, ClientModLoadedNotice::handle);
        ServerPlayNetworking.registerGlobalReceiver(TP_HOME, TpHomeNotice::handle);
        ServerPlayNetworking.registerGlobalReceiver(TP_BACK, TpBackNotice::handle);
        ServerPlayNetworking.registerGlobalReceiver(TP_YES, TpYesNotice::handle);
        ServerPlayNetworking.registerGlobalReceiver(TP_NO, TpNoNotice::handle);
    }

    public static void sendToPlayer(ServerPlayer player, ResourceLocation id, Packet msg) {
        FriendlyByteBuf buf = new FriendlyByteBuf(PacketByteBufs.create());
        msg.encode(buf);
        ServerPlayNetworking.send(player, id, buf);
    }

    public static void sendToServer(ResourceLocation id, Packet msg) {
        FriendlyByteBuf buf = new FriendlyByteBuf(PacketByteBufs.create());
        msg.encode(buf);
        ClientPlayNetworking.send(id, buf);
    }
}
