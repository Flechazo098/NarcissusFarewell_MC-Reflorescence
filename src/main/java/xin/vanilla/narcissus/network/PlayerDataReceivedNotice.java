package xin.vanilla.narcissus.network;

import net.minecraft.server.level.ServerPlayer;
import xin.vanilla.narcissus.NarcissusFarewell;

public class PlayerDataReceivedNotice {
    /**
     * 处理玩家数据接收通知
     */
    public static void handle(ServerPlayer player) {
        if (player != null) {
            NarcissusFarewell.getPlayerCapabilityStatus().put(player.getStringUUID(), true);
        }
    }
}