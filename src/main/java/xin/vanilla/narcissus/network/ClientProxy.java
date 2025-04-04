package xin.vanilla.narcissus.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xin.vanilla.narcissus.data.player.PlayerTeleportDataProvider;

@Environment(EnvType.CLIENT)
public class ClientProxy {
    public static final Logger LOGGER = LogManager.getLogger();

    /**
     * 处理同步的玩家数据
     */
    public static void handleSyncPlayerData(PlayerDataSyncPacket packet) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            try {
                PlayerTeleportDataProvider.deserializeNBT(player, packet.getData().serializeNBT());
                ModNetworkHandler.sendToServer(ModNetworkHandler.PLAYER_DATA_RECEIVED);
                LOGGER.debug("Client: Player data received successfully.");
            } catch (Exception e) {
                LOGGER.error("Client: Player data received failed.", e);
            }
        }
    }
}