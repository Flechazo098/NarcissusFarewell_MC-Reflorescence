package xin.vanilla.narcissus.network;

import net.minecraft.server.level.ServerPlayer;
import xin.vanilla.narcissus.NarcissusFarewell;
import xin.vanilla.narcissus.data.player.PlayerTeleportDataProvider;

public class ClientModLoadedNotice {
    /**
     * 处理客户端mod加载通知
     */
    public static void handle(ServerPlayer player) {
        if (player != null) {
            NarcissusFarewell.getPlayerCapabilityStatus().put(player.getStringUUID(), false);
            // 同步玩家传送数据到客户端
            PlayerTeleportDataProvider.syncPlayerData(player);
        }
    }
}