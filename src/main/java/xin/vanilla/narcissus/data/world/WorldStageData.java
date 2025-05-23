package xin.vanilla.narcissus.data.world;

import lombok.Getter;
import lombok.NonNull;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import xin.vanilla.narcissus.NarcissusFarewell;
import xin.vanilla.narcissus.config.Coordinate;
import xin.vanilla.narcissus.config.KeyValue;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 世界驿站数据
 */
@Getter
public class WorldStageData extends SavedData {
    private static final String DATA_NAME = "world_stage_data";

    // dimension:name coordinate
    private Map<KeyValue<String, String>, Coordinate> stageCoordinate = new LinkedHashMap<>();

    public WorldStageData() {
    }


    public static WorldStageData load(CompoundTag nbt) {
        WorldStageData data = new WorldStageData();
        ListTag stageCoordinateNBT = nbt.getList("stageCoordinate", 10);
        Map<KeyValue<String, String>, Coordinate> stageCoordinate = new HashMap<>();
        for (int i = 0; i < stageCoordinateNBT.size(); i++) {
            CompoundTag stageCoordinateTag = stageCoordinateNBT.getCompound(i);
            stageCoordinate.put(new KeyValue<>(stageCoordinateTag.getString("key"), stageCoordinateTag.getString("value")),
                    Coordinate.readFromNBT(stageCoordinateTag.getCompound("coordinate")));
        }
        data.setCoordinate(stageCoordinate);
        return data;
    }

    @Override
    @NonNull
    @ParametersAreNonnullByDefault
    public CompoundTag save(CompoundTag nbt) {
        ListTag stageCoordinateNBT = new ListTag();
        for (Map.Entry<KeyValue<String, String>, Coordinate> entry : this.getStageCoordinate().entrySet()) {
            CompoundTag stageCoordinateTag = new CompoundTag();
            stageCoordinateTag.putString("key", entry.getKey().getKey());
            stageCoordinateTag.putString("value", entry.getKey().getValue());
            stageCoordinateTag.put("coordinate", entry.getValue().writeToNBT());
            stageCoordinateNBT.add(stageCoordinateTag);
        }
        nbt.put("stageCoordinate", stageCoordinateNBT);
        return nbt;
    }

    public void setCoordinate(Map<KeyValue<String, String>, Coordinate> stageCoordinate) {
        this.stageCoordinate = stageCoordinate;
        super.setDirty();
    }

    public void addCoordinate(KeyValue<String, String> key, Coordinate coordinate) {
        this.stageCoordinate.put(key, coordinate);
        super.setDirty();
    }

    public int getCoordinateSize(String name) {
        return (int) this.getStageCoordinate().keySet().stream().filter(keyValue -> keyValue.getValue().equals(name)).count();
    }

    public int getCoordinateSize(String dimension, String name) {
        return (int) this.getStageCoordinate().keySet().stream().filter(keyValue -> keyValue.getKey().equals(dimension) && keyValue.getValue().equals(name)).count();
    }

    public Coordinate getCoordinate(String name) {
        return getCoordinateSize(name) == 1 ? this.getStageCoordinate().entrySet().stream()
                .filter(entry -> entry.getKey().getValue().equals(name))
                .findFirst().map(Map.Entry::getValue).orElse(null)
                : null;
    }

    public Coordinate getCoordinate(String dimension, String name) {
        return getCoordinateSize(dimension, name) == 1 ? this.getStageCoordinate().entrySet().stream()
                .filter(entry -> entry.getKey().getKey().equals(dimension) && entry.getKey().getValue().equals(name))
                .findFirst().map(Map.Entry::getValue).orElse(null)
                : null;
    }

    public static WorldStageData get() {
        return get(NarcissusFarewell.getServerInstance().getAllLevels().iterator().next());
    }

    public static WorldStageData get(ServerPlayer player) {
        return get(player.serverLevel());
    }

    /**
     * 从服务器世界获取驿站数据
     * @param level 服务器世界
     * @return 驿站数据
     */
    public static WorldStageData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(WorldStageData::load, WorldStageData::new, DATA_NAME);
    }

    /**
     * 获取所有驿站坐标数据
     * @param level 服务器世界
     * @return 驿站坐标数据
     */
    public static Map<KeyValue<String, String>, Coordinate> getStageCoordinates(ServerLevel level) {
        return get(level).getStageCoordinate();
    }
}
