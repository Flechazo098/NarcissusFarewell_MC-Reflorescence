package xin.vanilla.narcissus.network;

import net.minecraft.server.level.ServerPlayer;
import xin.vanilla.narcissus.enums.ECommandType;
import xin.vanilla.narcissus.util.NarcissusUtils;

import java.util.Objects;

/**
 * 传送回家通知工具类
 */
public final class TpHomeNotice {

    private TpHomeNotice() {
    }

    /**
     * 处理传送回家通知
     */
    public static void handle(ServerPlayer player) {
        if (player != null) {
            Objects.requireNonNull(player.getServer()).getCommands().performPrefixedCommand(
                    player.createCommandSourceStack(),
                    NarcissusUtils.getCommand(ECommandType.TP_HOME)
            );
        }
    }
}