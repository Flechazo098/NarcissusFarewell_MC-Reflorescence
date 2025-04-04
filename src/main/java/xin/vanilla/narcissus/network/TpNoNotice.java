package xin.vanilla.narcissus.network;

import net.minecraft.server.level.ServerPlayer;
import xin.vanilla.narcissus.NarcissusFarewell;
import xin.vanilla.narcissus.config.TeleportRequest;
import xin.vanilla.narcissus.enums.ECommandType;
import xin.vanilla.narcissus.enums.EI18nType;
import xin.vanilla.narcissus.enums.ETeleportType;
import xin.vanilla.narcissus.util.I18nUtils;
import xin.vanilla.narcissus.util.NarcissusUtils;

import java.util.Comparator;
import java.util.Objects;

/**
 * 传送拒绝通知工具类
 */
public final class TpNoNotice {

    private TpNoNotice() {
    }

    /**
     * 处理传送拒绝通知
     */
    public static void handle(ServerPlayer player) {
        if (player != null) {
            ETeleportType teleportType = NarcissusFarewell.getTeleportRequest().values().stream()
                    .filter(request -> request.getTarget().getUUID().equals(player.getUUID()))
                    .max(Comparator.comparing(TeleportRequest::getRequestTime))
                    .orElse(new TeleportRequest())
                    .getTeleportType();

            if (ETeleportType.TP_ASK == teleportType || ETeleportType.TP_HERE == teleportType) {
                ECommandType type = ETeleportType.TP_HERE == teleportType ? ECommandType.TP_HERE_NO : ECommandType.TP_ASK_NO;
                Objects.requireNonNull(player.getServer()).getCommands().performPrefixedCommand(
                        player.createCommandSourceStack(),
                        NarcissusUtils.getCommand(type)
                );
            } else {
                NarcissusUtils.sendTranslatableMessage(player, I18nUtils.getKey(EI18nType.MESSAGE, "tp_ask_not_found"));
            }
        }
    }
}