package xin.vanilla.narcissus.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import xin.vanilla.narcissus.NarcissusFarewell;
import xin.vanilla.narcissus.data.player.PlayerTeleportDataComponent;
import xin.vanilla.narcissus.data.player.PlayerTeleportDataProvider;

/**
 * 客户端语言同步数据包
 */
public class ClientLanguagePacket {
    private final String language;

    public ClientLanguagePacket(String language) {
        this.language = language;
    }

    public ClientLanguagePacket(FriendlyByteBuf buf) {
        this.language = buf.readUtf();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(this.language);
    }

    /**
     * 处理客户端语言同步
     */
    public static void handle(ServerPlayer player, String language) {
        if (player != null && language != null) {
            // 设置玩家语言
            NarcissusFarewell.setPlayerLanguage(player, language);

            // 立即同步数据到客户端
            PlayerTeleportDataComponent component = PlayerTeleportDataComponent.get(player);
            component.setLanguage(language);

            // 强制同步
            PlayerTeleportDataComponent.syncPlayerData(player);
            PlayerTeleportDataProvider.syncPlayerData(player);
        }
    }
}