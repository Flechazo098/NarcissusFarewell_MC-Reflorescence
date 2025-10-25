package xin.vanilla.narcissus.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xin.vanilla.narcissus.NarcissusFarewell;
import xin.vanilla.narcissus.config.CommonConfig;
import xin.vanilla.narcissus.config.CustomConfig;
import xin.vanilla.narcissus.data.Coordinate;
import xin.vanilla.narcissus.data.TeleportRecord;
import xin.vanilla.narcissus.data.TeleportRequest;
import xin.vanilla.narcissus.data.player.PlayerDataManager;
import xin.vanilla.narcissus.data.player.PlayerTeleportData;
import xin.vanilla.narcissus.enums.EnumI18nType;
import xin.vanilla.narcissus.enums.EnumTeleportType;
import xin.vanilla.narcissus.util.DateUtils;
import xin.vanilla.narcissus.util.I18nUtils;
import xin.vanilla.narcissus.util.NarcissusUtils;

import java.util.Comparator;
import java.util.Date;

public class EventHandlerProxy {
    private static final Logger LOGGER = LogManager.getLogger();

    private static long lastSaveConfTime = System.currentTimeMillis();
    private static long lastReadConfTime = System.currentTimeMillis();

    public static void registerEvents() {
        // 注册服务器Tick事件
        ServerTickEvents.END_SERVER_TICK.register(EventHandlerProxy::onServerTick);

        // 注册玩家连接事件
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            PlayerDataManager.onPlayerLogin(handler.getPlayer());
            onPlayerJoinWorld(handler.getPlayer());
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            PlayerDataManager.onPlayerLogout(handler.getPlayer());
        });

        // 注册玩家重生事件
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (!alive) { // 死亡重生
                onPlayerCloned(oldPlayer, newPlayer, true);
            }
        });

        // 注册实体死亡事件
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayer player) {
                onPlayerDeath(player, damageSource);
            }
        });
    }

    public static void onServerTick(MinecraftServer server) {
        if (server.getTickCount() % 20 == 0) {
            long currentTimeMillis = System.currentTimeMillis();
            NarcissusFarewell.getTeleportRequest().entrySet().stream()
                    .filter(entry -> entry.getValue().getExpireTime() < currentTimeMillis)
                    .forEach(entry -> {
                        TeleportRequest request = NarcissusFarewell.getTeleportRequest().remove(entry.getKey());
                        if (request != null) {
                            if (request.getTeleportType() == EnumTeleportType.TP_ASK) {
                                NarcissusUtils.sendTranslatableMessage(request.getRequester(), I18nUtils.getKey(EnumI18nType.MESSAGE, "tp_ask_expired"), request.getTarget().getDisplayName().getString());
                            } else if (request.getTeleportType() == EnumTeleportType.TP_HERE) {
                                NarcissusUtils.sendTranslatableMessage(request.getRequester(), I18nUtils.getKey(EnumI18nType.MESSAGE, "tp_here_expired"), request.getTarget().getDisplayName().getString());
                            }
                        }
                    });
        }

        // 保存通用配置
        if (System.currentTimeMillis() - lastSaveConfTime >= 10 * 1000) {
            lastSaveConfTime = System.currentTimeMillis();
            CustomConfig.saveCustomConfig();
        }
        // 读取通用配置
        else if (System.currentTimeMillis() - lastReadConfTime >= 2 * 60 * 1000) {
            lastReadConfTime = System.currentTimeMillis();
            CustomConfig.loadCustomConfig(true);
        }
    }

    public static void onPlayerCloned(ServerPlayer original, ServerPlayer newPlayer, boolean wasDeath) {
        original.unsetRemoved();
        NarcissusUtils.clonePlayerLanguage(original, newPlayer);

        // 如果是死亡，则记录死亡记录
        if (wasDeath) {
            TeleportRecord record = new TeleportRecord();
            record.setTeleportTime(new Date());
            record.setTeleportType(EnumTeleportType.DEATH);
            record.setBefore(new Coordinate().setX(original.getX()).setY(original.getY()).setZ(original.getZ()).setDimension(original.level().dimension()));
            record.setAfter(new Coordinate().setX(newPlayer.getX()).setY(newPlayer.getY()).setZ(newPlayer.getZ()).setDimension(newPlayer.level().dimension()));
            PlayerTeleportData.getData(newPlayer).addTeleportRecords(record);
        }
    }

    public static void onPlayerJoinWorld(ServerPlayer player) {
        // 给予传送卡
        if (CommonConfig.TELEPORT_CARD.get()) {
            PlayerTeleportData data = PlayerTeleportData.getData(player);
            Date current = new Date();
            if (DateUtils.toDateInt(data.getLastCardTime()) < DateUtils.toDateInt(current)) {
                data.setLastCardTime(current);
                data.plusTeleportCard(CommonConfig.TELEPORT_CARD_DAILY.get());
            }
        }
    }

    public static void onPlayerDeath(ServerPlayer player, DamageSource damageSource) {
        // 记录死亡位置用于返回
        TeleportRecord record = new TeleportRecord();
        record.setTeleportTime(new Date());
        record.setTeleportType(EnumTeleportType.DEATH);
        record.setBefore(new Coordinate(player));
        PlayerTeleportData.getData(player).addTeleportRecords(record);
    }

    public static void onEntityTeleport(ServerPlayer player, double prevX, double prevY, double prevZ, double newX, double newY, double newZ) {
        TeleportRecord record = new TeleportRecord();
        record.setTeleportTime(new Date());
        record.setTeleportType(EnumTeleportType.OTHER);
        record.setBefore(new Coordinate(player).setX(prevX).setY(prevY).setZ(prevZ));
        record.setAfter(new Coordinate(player).setX(newX).setY(newY).setZ(newZ));
        PlayerTeleportData data = PlayerTeleportData.getData(player);
        TeleportRecord otherRecord = data.getTeleportRecords().stream().max(Comparator.comparing(o -> o.getTeleportTime().getTime())).orElse(null);
        if (otherRecord != null && otherRecord.getTeleportType() == EnumTeleportType.OTHER && otherRecord.getBefore().toXyzString().equals(record.getBefore().toXyzString())) {
            otherRecord.setAfter(record.getAfter());
        } else {
            data.addTeleportRecords(record);
        }
    }
}
