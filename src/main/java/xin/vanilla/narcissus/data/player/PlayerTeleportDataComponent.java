package xin.vanilla.narcissus.data.player;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import xin.vanilla.narcissus.NarcissusFarewell;
import xin.vanilla.narcissus.network.ModNetworkHandler;
import xin.vanilla.narcissus.network.PlayerDataSyncPacket;

public class PlayerTeleportDataComponent implements IPlayerTeleportData, AutoSyncedComponent {
    // 定义组件键 - 使用延迟初始化
    public static final ComponentKey<PlayerTeleportDataComponent> KEY = ComponentRegistry.getOrCreate(
            new ResourceLocation(NarcissusFarewell.MOD_ID, "player_teleport_data"),
            PlayerTeleportDataComponent.class
    );

    private final PlayerTeleportData data = new PlayerTeleportData();
    private final Player player;

    public PlayerTeleportDataComponent(Player player) {
        this.player = player;
    }


    /**
     * 获取玩家传送数据
     *
     * @param player 玩家实体
     * @return 玩家的传送数据组件
     */
    public static PlayerTeleportDataComponent get(Player player) {
        return KEY.get(player);
    }

    /**
     * 同步玩家传送数据到客户端
     */
    public static void syncPlayerData(ServerPlayer player) {
        // 创建自定义包并发送到客户端
        PlayerDataSyncPacket packet = new PlayerDataSyncPacket(player.getUUID(), get(player));
        for (PlayerDataSyncPacket syncPacket : packet.split()) {
            ModNetworkHandler.sendToPlayer(player, syncPacket);
        }
    }

    @Override
    public int getTeleportCard() {
        return data.getTeleportCard();
    }

    @Override
    public int plusTeleportCard() {
        int result = data.plusTeleportCard();
        KEY.sync(player);
        return result;
    }

    @Override
    public int plusTeleportCard(int num) {
        int result = data.plusTeleportCard(num);
        KEY.sync(player);
        return result;
    }

    @Override
    public int subTeleportCard() {
        int result = data.subTeleportCard();
        KEY.sync(player);
        return result;
    }

    @Override
    public int subTeleportCard(int num) {
        int result = data.subTeleportCard(num);
        KEY.sync(player);
        return result;
    }

    @Override
    public void setTeleportCard(int num) {
        data.setTeleportCard(num);
        KEY.sync(player);
    }

    @Override
    public java.util.Date getLastCardTime() {
        return data.getLastCardTime();
    }

    @Override
    public void setLastCardTime(java.util.Date time) {
        data.setLastCardTime(time);
        KEY.sync(player);
    }

    @Override
    public java.util.Date getLastTpTime() {
        return data.getLastTpTime();
    }

    @Override
    public void setLastTpTime(java.util.Date time) {
        data.setLastTpTime(time);
        KEY.sync(player);
    }

    @Override
    public java.util.@NotNull List<xin.vanilla.narcissus.data.TeleportRecord> getTeleportRecords() {
        return data.getTeleportRecords();
    }

    @Override
    public java.util.@NotNull List<xin.vanilla.narcissus.data.TeleportRecord> getTeleportRecords(xin.vanilla.narcissus.enums.ETeleportType type) {
        return data.getTeleportRecords(type);
    }

    @Override
    public void setTeleportRecords(java.util.List<xin.vanilla.narcissus.data.TeleportRecord> records) {
        data.setTeleportRecords(records);
        KEY.sync(player);
    }

    @Override
    public void addTeleportRecords(xin.vanilla.narcissus.data.TeleportRecord... records) {
        data.addTeleportRecords(records);
        KEY.sync(player);
    }

    @Override
    public java.util.Map<xin.vanilla.narcissus.config.KeyValue<String, String>, xin.vanilla.narcissus.config.Coordinate> getHomeCoordinate() {
        return data.getHomeCoordinate();
    }

    @Override
    public void setHomeCoordinate(java.util.Map<xin.vanilla.narcissus.config.KeyValue<String, String>, xin.vanilla.narcissus.config.Coordinate> homeCoordinate) {
        data.setHomeCoordinate(homeCoordinate);
        KEY.sync(player);
    }

    @Override
    public void addHomeCoordinate(xin.vanilla.narcissus.config.KeyValue<String, String> key, xin.vanilla.narcissus.config.Coordinate coordinate) {
        data.addHomeCoordinate(key, coordinate);
        KEY.sync(player);
    }

    @Override
    public java.util.Map<String, String> getDefaultHome() {
        return data.getDefaultHome();
    }

    @Override
    public void setDefaultHome(java.util.Map<String, String> defaultHome) {
        data.setDefaultHome(defaultHome);
        KEY.sync(player);
    }

    @Override
    public void addDefaultHome(String key, String value) {
        data.addDefaultHome(key, value);
        KEY.sync(player);
    }

    @Override
    public xin.vanilla.narcissus.config.KeyValue<String, String> getDefaultHome(String key) {
        return data.getDefaultHome(key);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        data.writeToBuffer(buffer);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        data.readFromBuffer(buffer);
    }

    @Override
    public void copyFrom(IPlayerTeleportData capability) {
        data.copyFrom(capability);
        KEY.sync(player);
    }

    @Override
    public void save(ServerPlayer player) {
        // 在组件中不需要额外的保存操作，Cardinal Components API 会自动处理
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        data.deserializeNBT(tag);
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        CompoundTag dataTag = data.serializeNBT();
        tag.put("data", dataTag);
    }

    @Override
    public CompoundTag serializeNBT() {
        return data.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        data.deserializeNBT(nbt);
    }

}