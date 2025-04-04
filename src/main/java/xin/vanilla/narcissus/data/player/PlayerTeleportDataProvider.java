package xin.vanilla.narcissus.data.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import xin.vanilla.narcissus.network.ModNetworkHandler;
import xin.vanilla.narcissus.network.PlayerDataSyncPacket;

/**
 * 玩家传送数据提供者类
 * 在 Fabric 中使用 Cardinal Components API，这个类主要作为辅助工具类
 */
public class PlayerTeleportDataProvider {

    /**
     * 获取玩家的传送数据
     *
     * @param player 玩家实体
     * @return 玩家传送数据
     */
    public static IPlayerTeleportData getData(Player player) {
        return PlayerTeleportDataComponent.get(player);
    }

    /**
     * 保存玩家传送数据
     *
     * @param player 玩家实体
     * @param data 要保存的数据
     */
    public static void saveData(Player player, IPlayerTeleportData data) {
        PlayerTeleportDataComponent component = PlayerTeleportDataComponent.get(player);
        component.copyFrom(data);
    }

    /**
     * 序列化玩家传送数据为NBT格式
     *
     * @param player 玩家实体
     * @return 包含玩家传送数据的CompoundTag对象
     */
    public static CompoundTag serializeNBT(Player player) {
        return PlayerTeleportDataComponent.get(player).serializeNBT();
    }

    /**
     * 从NBT格式的数据中反序列化玩家传送数据
     *
     * @param player 玩家实体
     * @param nbt 包含玩家传送数据的CompoundTag对象
     */
    public static void deserializeNBT(Player player, CompoundTag nbt) {
        PlayerTeleportDataComponent.get(player).deserializeNBT(nbt);
    }

    /**
     * 同步玩家传送数据到客户端
     *
     * @param player 服务器玩家
     */
    public static void syncPlayerData(ServerPlayer player) {
        // 创建自定义包并发送到客户端
        PlayerDataSyncPacket packet = new PlayerDataSyncPacket(player.getUUID(), PlayerTeleportDataComponent.get(player));
        for (PlayerDataSyncPacket syncPacket : packet.split()) {
            ModNetworkHandler.sendToPlayer(player, syncPacket);
        }
    }

}