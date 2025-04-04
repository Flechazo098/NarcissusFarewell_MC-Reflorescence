package xin.vanilla.narcissus.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import xin.vanilla.narcissus.NarcissusFarewell;

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
            NarcissusFarewell.setPlayerLanguage(player, language);
        }
    }
}