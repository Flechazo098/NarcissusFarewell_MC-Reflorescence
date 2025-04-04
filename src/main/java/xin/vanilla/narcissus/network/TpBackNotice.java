package xin.vanilla.narcissus.network;

import net.minecraft.server.level.ServerPlayer;
import xin.vanilla.narcissus.enums.ECommandType;
import xin.vanilla.narcissus.util.NarcissusUtils;

import java.util.Objects;

/**
 * 传送返回通知工具类
 */
public final class TpBackNotice {

    private TpBackNotice() {
    }

    /**
     * 处理传送返回通知
     */
    public static void handle(ServerPlayer player) {
        if (player != null) {
            Objects.requireNonNull(player.getServer()).getCommands().performPrefixedCommand(
                    player.createCommandSourceStack(),
                    NarcissusUtils.getCommand(ECommandType.TP_BACK)
            );
        }
    }
}