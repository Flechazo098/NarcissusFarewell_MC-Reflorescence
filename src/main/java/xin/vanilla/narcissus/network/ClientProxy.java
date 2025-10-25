package xin.vanilla.narcissus.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xin.vanilla.narcissus.data.player.PlayerTeleportData;
import xin.vanilla.narcissus.network.packet.PlayerDataSyncPacket;

@Environment(EnvType.CLIENT)
public class ClientProxy {
    public static final Logger LOGGER = LogManager.getLogger();

    public static void registerClientPackets() {
        ClientPlayNetworking.registerGlobalReceiver(ModNetworkHandler.PLAYER_DATA_SYNC,
                (client, handler, buf, responseSender) -> {
                    PlayerDataSyncPacket packet = new PlayerDataSyncPacket(buf);
                    client.execute(() -> handleSynPlayerData(packet));
                });
    }

    public static void handleSynPlayerData(PlayerDataSyncPacket packet) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            try {
                PlayerTeleportData clientData = PlayerTeleportData.getData(player);
                clientData.copyFrom(packet.getData());
                LOGGER.debug("Client: Player data received successfully.");
            } catch (Exception ignored) {
                LOGGER.debug("Client: Player data received failed.");
            }
        }
    }

    public static PlayerTeleportData createClientData() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            return PlayerTeleportData.getData(player);
        }
        return null;
    }
}
