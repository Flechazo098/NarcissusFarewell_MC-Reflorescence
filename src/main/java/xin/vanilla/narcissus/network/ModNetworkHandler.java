package xin.vanilla.narcissus.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xin.vanilla.narcissus.NarcissusFarewell;

import java.util.HashMap;
import java.util.Map;

public class ModNetworkHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    // 网络通道标识符
    public static final ResourceLocation PLAYER_DATA_SYNC = new ResourceLocation(NarcissusFarewell.MOD_ID, "player_data_sync");
    public static final ResourceLocation PLAYER_DATA_RECEIVED = new ResourceLocation(NarcissusFarewell.MOD_ID, "player_data_received");
    public static final ResourceLocation CLIENT_MOD_LOADED = new ResourceLocation(NarcissusFarewell.MOD_ID, "client_mod_loaded");
    public static final ResourceLocation TP_HOME = new ResourceLocation(NarcissusFarewell.MOD_ID, "tp_home");
    public static final ResourceLocation TP_BACK = new ResourceLocation(NarcissusFarewell.MOD_ID, "tp_back");
    public static final ResourceLocation TP_YES = new ResourceLocation(NarcissusFarewell.MOD_ID, "tp_yes");
    public static final ResourceLocation TP_NO = new ResourceLocation(NarcissusFarewell.MOD_ID, "tp_no");
    public static final ResourceLocation CLIENT_LANGUAGE = new ResourceLocation(NarcissusFarewell.MOD_ID, "client_language");

    // 分包缓存
    private static final Map<String, Map<Integer, PlayerDataSyncPacket>> packetCache = new HashMap<>();

    /**
     * 注册所有网络处理器
     */
    public static void registerHandlers() {
        // 服务端处理来自客户端的数据包
        registerServerReceivers();
    }

    /**
     * 注册服务端接收器
     */
    private static void registerServerReceivers() {
        // 客户端mod加载通知
        ServerPlayNetworking.registerGlobalReceiver(CLIENT_MOD_LOADED, (server, player, handler, buf, responseSender) -> server.execute(() -> ClientModLoadedNotice.handle(player)));

        // 玩家数据接收通知
        ServerPlayNetworking.registerGlobalReceiver(PLAYER_DATA_RECEIVED, (server, player, handler, buf, responseSender) -> server.execute(() -> PlayerDataReceivedNotice.handle(player)));

        // 传送回家通知
        ServerPlayNetworking.registerGlobalReceiver(TP_HOME, (server, player, handler, buf, responseSender) -> server.execute(() -> TpHomeNotice.handle(player)));

        // 传送返回通知
        ServerPlayNetworking.registerGlobalReceiver(TP_BACK, (server, player, handler, buf, responseSender) -> server.execute(() -> TpBackNotice.handle(player)));

        // 传送同意通知
        ServerPlayNetworking.registerGlobalReceiver(TP_YES, (server, player, handler, buf, responseSender) -> server.execute(() -> TpYesNotice.handle(player)));

        // 传送拒绝通知
        ServerPlayNetworking.registerGlobalReceiver(TP_NO, (server, player, handler, buf, responseSender) -> server.execute(() -> TpNoNotice.handle(player)));

        // 客户端语言同步
        ServerPlayNetworking.registerGlobalReceiver(CLIENT_LANGUAGE, (server, player, handler, buf, responseSender) -> {
            String language = buf.readUtf();
            server.execute(() -> ClientLanguagePacket.handle(player, language));
        });
    }

    /**
     * 注册客户端接收器
     */
    public static void registerClientReceivers() {
        // 玩家数据同步
        ClientPlayNetworking.registerGlobalReceiver(PLAYER_DATA_SYNC, (client, handler, buf, responseSender) -> {
            PlayerDataSyncPacket packet = new PlayerDataSyncPacket(buf);
            client.execute(() -> handleSplitPacket(packet));
        });
    }

    /**
     * 处理分包
     */
    private static void handleSplitPacket(PlayerDataSyncPacket packet) {
        String id = packet.getId();
        int total = packet.getTotal();
        int sort = packet.getSort();

        // 确保缓存中有此ID的映射
        packetCache.computeIfAbsent(id, k -> new HashMap<>());

        // 添加到缓存
        packetCache.get(id).put(sort, packet);

        // 检查是否收到所有分包
        if (packetCache.get(id).size() == total) {
            // 按顺序合并所有分包
            PlayerDataSyncPacket mergedPacket = PlayerDataSyncPacket.merge(packetCache.get(id).values());

            // 处理合并后的数据包
            ClientProxy.handleSyncPlayerData(mergedPacket);

            // 移除缓存
            packetCache.remove(id);

            // 清理过期缓存
            cleanupCache();
        }
    }

    /**
     * 清理过期缓存
     */
    private static void cleanupCache() {
        long currentTime = System.currentTimeMillis();
        packetCache.keySet().removeIf(id -> {
            try {
                long packetTime = Long.parseLong(id.split("\\.")[0]);
                return currentTime - packetTime > 300000; // 5分钟
            } catch (Exception e) {
                return true; // 解析失败则移除
            }
        });
    }

    /**
     * 向玩家发送数据包
     */
    public static void sendToPlayer(ServerPlayer player, Object packet) {
        if (packet instanceof PlayerDataSyncPacket dataPacket) {
            FriendlyByteBuf buf = PacketByteBufs.create();
            dataPacket.toBytes(buf);
            ServerPlayNetworking.send(player, PLAYER_DATA_SYNC, buf);
        }
    }

    /**
     * 向服务器发送数据包
     */
    public static void sendToServer(ResourceLocation id) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        ClientPlayNetworking.send(id, buf);
    }

    /**
     * 发送传送回家通知到服务器
     */
    public static void sendTpHomeToServer() {
        sendToServer(TP_HOME);
    }

    /**
     * 发送传送返回通知到服务器
     */
    public static void sendTpBackToServer() {
        sendToServer(TP_BACK);
    }

    /**
     * 发送传送同意通知到服务器
     */
    public static void sendTpYesToServer() {
        sendToServer(TP_YES);
    }

    /**
     * 发送传送拒绝通知到服务器
     */
    public static void sendTpNoToServer() {
        sendToServer(TP_NO);
    }

    /**
     * 向服务器发送客户端语言
     */
    public static void sendClientLanguageToServer(String language) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUtf(language);
        ClientPlayNetworking.send(CLIENT_LANGUAGE, buf);
    }

    /**
     * 在资源重载时同步客户端语言到服务器
     * 这个方法应该在客户端资源重载事件中调用
     */
    public static void syncClientLanguageOnResourceReload() {
        if (Minecraft.getInstance().player != null) {
            String currentLanguage = Minecraft.getInstance().getLanguageManager().getSelected();
            sendClientLanguageToServer(currentLanguage);
            LOGGER.info("Resource reload detected, syncing language: {}", currentLanguage);
        }
    }
}