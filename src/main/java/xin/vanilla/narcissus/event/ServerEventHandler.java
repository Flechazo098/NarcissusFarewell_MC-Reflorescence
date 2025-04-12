package xin.vanilla.narcissus.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xin.vanilla.narcissus.NarcissusFarewell;
import xin.vanilla.narcissus.config.ConfigManager;
import xin.vanilla.narcissus.config.Coordinate;
import xin.vanilla.narcissus.config.TeleportRequest;
import xin.vanilla.narcissus.data.TeleportRecord;
import xin.vanilla.narcissus.data.player.IPlayerTeleportData;
import xin.vanilla.narcissus.data.player.PlayerTeleportDataProvider;
import xin.vanilla.narcissus.enums.EI18nType;
import xin.vanilla.narcissus.enums.ETeleportType;
import xin.vanilla.narcissus.util.DateUtils;
import xin.vanilla.narcissus.util.I18nUtils;
import xin.vanilla.narcissus.util.NarcissusUtils;

import java.util.Comparator;
import java.util.Date;


/**
 * 服务器事件处理
 */
public class ServerEventHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * 注册所有服务器事件
     */
    public static void registerEvents() {
        // 玩家加入服务器事件
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            // 初始化能力同步状态
            if (NarcissusFarewell.getPlayerCapabilityStatus().containsKey(player.getStringUUID())) {
                NarcissusFarewell.getPlayerCapabilityStatus().put(player.getStringUUID(), false);
            }

            // 给予传送卡
            if (ConfigManager.getConfig().teleportCard) {
                IPlayerTeleportData data = PlayerTeleportDataProvider.getData(player);
                Date current = new Date();
                if (DateUtils.toDateInt(data.getLastCardTime()) < DateUtils.toDateInt(current)) {
                    data.setLastCardTime(current);
                    data.plusTeleportCard(ConfigManager.getConfig().teleportCardDaily);
                }
            }
        });

        // 玩家离开服务器事件
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.getPlayer();
            // 玩家退出服务器时移除键(移除mod安装状态)
            NarcissusFarewell.getPlayerCapabilityStatus().remove(player.getStringUUID());
        });

        // 玩家复制事件（死亡或从末地返回）
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            NarcissusUtils.clonePlayerLanguage(oldPlayer, newPlayer);

            // 复制玩家数据
            IPlayerTeleportData oldData = PlayerTeleportDataProvider.getData(oldPlayer);
            IPlayerTeleportData newData = PlayerTeleportDataProvider.getData(newPlayer);
            newData.copyFrom(oldData);

            if (NarcissusFarewell.getPlayerCapabilityStatus().containsKey(newPlayer.getUUID().toString())) {
                NarcissusFarewell.getPlayerCapabilityStatus().put(newPlayer.getStringUUID(), false);
            }

            // 如果是死亡，则记录死亡记录
            if (!alive) {
                TeleportRecord record = new TeleportRecord();
                record.setTeleportTime(new Date());
                record.setTeleportType(ETeleportType.DEATH);
                record.setBefore(new Coordinate().setX(oldPlayer.getX()).setY(oldPlayer.getY()).setZ(oldPlayer.getZ()).setDimension(oldPlayer.level().dimension()));
                record.setAfter(new Coordinate().setX(newPlayer.getX()).setY(newPlayer.getY()).setZ(newPlayer.getZ()).setDimension(newPlayer.level().dimension()));
                PlayerTeleportDataProvider.getData(newPlayer).addTeleportRecords(record);
            }
        });

        // 实体维度变更事件
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            // 记录跨维度传送
            TeleportRecord record = new TeleportRecord();
            record.setTeleportTime(new Date());
            record.setTeleportType(ETeleportType.OTHER);
            record.setBefore(new Coordinate(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot(), origin.dimension()));
            record.setAfter(new Coordinate(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot(), destination.dimension()));

            IPlayerTeleportData data = PlayerTeleportDataProvider.getData(player);
            data.addTeleportRecords(record);
        });

        // 服务器Tick事件
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // 每秒检查一次过期的传送请求
            if (server.getTickCount() % 20 == 0) {
                long currentTimeMillis = System.currentTimeMillis();
                NarcissusFarewell.getTeleportRequest().entrySet().stream()
                        .filter(entry -> entry.getValue().getExpireTime() < currentTimeMillis)
                        .forEach(entry -> {
                            TeleportRequest request = NarcissusFarewell.getTeleportRequest().remove(entry.getKey());
                            if (request != null) {
                                if (request.getTeleportType() == ETeleportType.TP_ASK) {
                                    NarcissusUtils.sendTranslatableMessage(request.getRequester(), I18nUtils.getKey(EI18nType.MESSAGE, "tp_ask_expired"), request.getTarget().getDisplayName().getString());
                                } else if (request.getTeleportType() == ETeleportType.TP_HERE) {
                                    NarcissusUtils.sendTranslatableMessage(request.getRequester(), I18nUtils.getKey(EI18nType.MESSAGE, "tp_here_expired"), request.getTarget().getDisplayName().getString());
                                }
                            }
                        });
            }
        });

        // 玩家Tick事件
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                // 仅给安装了mod的玩家发送数据包
                if (NarcissusFarewell.getPlayerCapabilityStatus().containsKey(player.getUUID().toString())
                        && !NarcissusFarewell.getPlayerCapabilityStatus().get(player.getStringUUID())) {
                    // 如果玩家还活着则同步玩家传送数据到客户端
                    if (player.isAlive()) {
                        try {
                            PlayerTeleportDataProvider.syncPlayerData(player);
                        } catch (Exception e) {
                            LOGGER.error("Failed to sync player data to client", e);
                        }
                    }
                }
            }
        });
    }
}