package xin.vanilla.narcissus.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import lombok.NonNull;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xin.vanilla.narcissus.NarcissusFarewell;
import xin.vanilla.narcissus.config.*;
import xin.vanilla.narcissus.data.TeleportRecord;
import xin.vanilla.narcissus.data.player.IPlayerTeleportData;
import xin.vanilla.narcissus.data.player.PlayerTeleportDataProvider;
import xin.vanilla.narcissus.data.world.WorldStageData;
import xin.vanilla.narcissus.enums.*;

import javax.annotation.Nullable;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static xin.vanilla.narcissus.config.ConfigManager.config;

public class NarcissusUtils {

    private static final Logger LOGGER = LogManager.getLogger();

    // 移除静态初始化的列表，改为延迟加载
    private static List<String> biomeNames;
    private static List<String> structureNames;

    /**
     * 获取生物群系名称列表
     */
    public static List<String> getBiomeNames() {
        if (biomeNames == null) {
            ServerLevel level = getServerLevel();
            if (level == null) {
                return new ArrayList<>();
            }
            biomeNames = level.registryAccess()
                    .registryOrThrow(Registries.BIOME)
                    .keySet()
                    .stream()
                    .map(ResourceLocation::toString)  // 直接调用 ResourceLocation 的 toString 方法
                    .collect(Collectors.toList());
        }
        return biomeNames;
    }

    /**
     * 获取结构名称列表
     */
    public static List<String> getStructureNames() {
        if (structureNames == null) {
            ServerLevel level = getServerLevel();
            if (level == null) {
                return new ArrayList<>();
            }
            structureNames = level.registryAccess()
                    .registryOrThrow(Registries.STRUCTURE)
                    .keySet()
                    .stream()
                    .map(ResourceLocation::toString)  // 直接调用 ResourceLocation 的 toString 方法
                    .collect(Collectors.toList());
        }
        return structureNames;
    }

    // region 指令相关

    public static String getCommandPrefix() {
        String commandPrefix = config.commandPrefix;
        if (StringUtils.isNullOrEmptyEx(commandPrefix) || !commandPrefix.matches("^(\\w ?)+$")) {
            config.commandPrefix = NarcissusFarewell.DEFAULT_COMMAND_PREFIX;
            AutoConfig.getConfigHolder(ServerConfig.class).save();
        }
        return config.commandPrefix.trim();
    }
    /**
     * 判断指令类型是否开启
     *
     * @param type 指令类型
     */
    public static boolean isCommandEnabled(ECommandType type) {
        return switch (type) {
            case FEED, FEED_OTHER, FEED_CONCISE, FEED_OTHER_CONCISE -> config.switchConfig.switchFeed;
            case TP_COORDINATE, TP_COORDINATE_CONCISE -> config.switchConfig.switchTpCoordinate;
            case TP_STRUCTURE, TP_STRUCTURE_CONCISE -> config.switchConfig.switchTpStructure;
            case TP_ASK, TP_ASK_YES, TP_ASK_NO, TP_ASK_CONCISE, TP_ASK_YES_CONCISE, TP_ASK_NO_CONCISE ->
                    config.switchConfig.switchTpAsk;
            case TP_HERE, TP_HERE_YES, TP_HERE_NO, TP_HERE_CONCISE, TP_HERE_YES_CONCISE, TP_HERE_NO_CONCISE ->
                    config.switchConfig.switchTpHere;
            case TP_RANDOM, TP_RANDOM_CONCISE -> config.switchConfig.switchTpRandom;
            case TP_SPAWN, TP_SPAWN_OTHER, TP_SPAWN_CONCISE, TP_SPAWN_OTHER_CONCISE ->
                    config.switchConfig.switchTpSpawn;
            case TP_WORLD_SPAWN, TP_WORLD_SPAWN_CONCISE -> config.switchConfig.switchTpWorldSpawn;
            case TP_TOP, TP_TOP_CONCISE -> config.switchConfig.switchTpTop;
            case TP_BOTTOM, TP_BOTTOM_CONCISE -> config.switchConfig.switchTpBottom;
            case TP_UP, TP_UP_CONCISE -> config.switchConfig.switchTpUp;
            case TP_DOWN, TP_DOWN_CONCISE -> config.switchConfig.switchTpDown;
            case TP_VIEW, TP_VIEW_CONCISE -> config.switchConfig.switchTpView;
            case TP_HOME, SET_HOME, DEL_HOME, GET_HOME, TP_HOME_CONCISE, SET_HOME_CONCISE, DEL_HOME_CONCISE,
                 GET_HOME_CONCISE -> config.switchConfig.switchTpHome;
            case TP_STAGE, SET_STAGE, DEL_STAGE, GET_STAGE, TP_STAGE_CONCISE, SET_STAGE_CONCISE, DEL_STAGE_CONCISE,
                 GET_STAGE_CONCISE -> config.switchConfig.switchTpStage;
            case TP_BACK, TP_BACK_CONCISE -> config.switchConfig.switchTpBack;
            default -> true;
        };
    }

    public static String getCommand(ETeleportType type) {
        return switch (type) {
            case TP_COORDINATE -> config.commandConfig.commandTpCoordinate;
            case TP_STRUCTURE -> config.commandConfig.commandTpStructure;
            case TP_ASK -> config.commandConfig.commandTpAsk;
            case TP_HERE -> config.commandConfig.commandTpHere;
            case TP_RANDOM -> config.commandConfig.commandTpRandom;
            case TP_SPAWN -> config.commandConfig.commandTpSpawn;
            case TP_WORLD_SPAWN -> config.commandConfig.commandTpWorldSpawn;
            case TP_TOP -> config.commandConfig.commandTpTop;
            case TP_BOTTOM -> config.commandConfig.commandTpBottom;
            case TP_UP -> config.commandConfig.commandTpUp;
            case TP_DOWN -> config.commandConfig.commandTpDown;
            case TP_VIEW -> config.commandConfig.commandTpView;
            case TP_HOME -> config.commandConfig.commandTpHome;
            case TP_STAGE -> config.commandConfig.commandTpStage;
            case TP_BACK -> config.commandConfig.commandTpBack;
            default -> "";
        };
    }

    public static String getCommand(ECommandType type) {
        String prefix = NarcissusUtils.getCommandPrefix();
        return switch (type) {
            case HELP -> prefix + " help";
            case DIMENSION -> prefix + " " + config.commandConfig.commandDimension;
            case DIMENSION_CONCISE -> isConciseEnabled(type) ? config.commandConfig.commandDimension : "";
            case UUID -> prefix + " " + config.commandConfig.commandUuid;
            case UUID_CONCISE -> isConciseEnabled(type) ? config.commandConfig.commandUuid : "";
            case FEED, FEED_OTHER -> prefix + " " + config.commandConfig.commandFeed;
            case FEED_CONCISE, FEED_OTHER_CONCISE -> isConciseEnabled(type) ? config.commandConfig.commandFeed : "";
            case TP_COORDINATE -> prefix + " " + config.commandConfig.commandTpCoordinate;
            case TP_COORDINATE_CONCISE -> isConciseEnabled(type) ? config.commandConfig.commandTpCoordinate : "";
            case TP_STRUCTURE -> prefix + " " + config.commandConfig.commandTpStructure;
            case TP_STRUCTURE_CONCISE -> isConciseEnabled(type) ? config.commandConfig.commandTpStructure : "";
            case TP_ASK -> prefix + " " + config.commandConfig.commandTpAsk;
            case TP_ASK_CONCISE -> isConciseEnabled(type) ? config.commandConfig.commandTpAsk : "";
            case TP_ASK_YES -> prefix + " " + config.commandConfig.commandTpAskYes;
            case TP_ASK_YES_CONCISE -> isConciseEnabled(type) ? config.commandConfig.commandTpAskYes : "";
            case TP_ASK_NO -> prefix + " " + config.commandConfig.commandTpAskNo;
            case TP_ASK_NO_CONCISE -> isConciseEnabled(type) ? config.commandConfig.commandTpAskNo : "";
            case TP_HERE -> prefix + " " + config.commandConfig.commandTpHere;
            case TP_HERE_CONCISE -> isConciseEnabled(type) ? config.commandConfig.commandTpHere : "";
            case TP_HERE_YES -> prefix + " " + config.commandConfig.commandTpHereYes;
            case TP_HERE_YES_CONCISE -> isConciseEnabled(type) ? config.commandConfig.commandTpHereYes : "";
            case TP_HERE_NO -> prefix + " " + config.commandConfig.commandTpHereNo;
            case TP_HERE_NO_CONCISE -> isConciseEnabled(type) ? config.commandConfig.commandTpHereNo : "";
            case TP_RANDOM -> prefix + " " + config.commandConfig.commandTpRandom;
            case TP_RANDOM_CONCISE -> isConciseEnabled(type) ? config.commandConfig.commandTpRandom : "";
            case TP_SPAWN, TP_SPAWN_OTHER -> prefix + " " + config.commandConfig.commandTpSpawn;
            case TP_SPAWN_CONCISE, TP_SPAWN_OTHER_CONCISE ->
                    isConciseEnabled(type) ? config.commandConfig.commandTpSpawn : "";
            case TP_WORLD_SPAWN -> prefix + " " + config.commandConfig.commandTpWorldSpawn;
            case TP_WORLD_SPAWN_CONCISE -> isConciseEnabled(type) ? config.commandConfig.commandTpWorldSpawn : "";
            case TP_TOP -> prefix + " " + config.commandConfig.commandTpTop;
            case TP_TOP_CONCISE -> isConciseEnabled(type) ? config.commandConfig.commandTpTop : "";
            case TP_BOTTOM -> prefix + " " + config.commandConfig.commandTpBottom;
            case TP_BOTTOM_CONCISE -> isConciseEnabled(type) ? config.commandConfig.commandTpBottom : "";
            case TP_UP -> prefix + " " + config.commandConfig.commandTpUp;
            case TP_UP_CONCISE -> isConciseEnabled(type) ? config.commandConfig.commandTpUp : "";
            case TP_DOWN -> prefix + " " + config.commandConfig.commandTpDown;
            case TP_DOWN_CONCISE -> isConciseEnabled(type) ? config.commandConfig.commandTpDown : "";
            case TP_VIEW -> prefix + " " + config.commandConfig.commandTpView;
            case TP_VIEW_CONCISE -> isConciseEnabled(type) ? config.commandConfig.commandTpView : "";
            case TP_HOME -> prefix + " " + config.commandConfig.commandTpHome;
            case TP_HOME_CONCISE -> isConciseEnabled(type) ? config.commandConfig.commandTpHome : "";
            case SET_HOME -> prefix + " " + config.commandConfig.commandSetHome;
            case SET_HOME_CONCISE -> isConciseEnabled(type) ? config.commandConfig.commandSetHome : "";
            case DEL_HOME -> prefix + " " + config.commandConfig.commandDelHome;
            case DEL_HOME_CONCISE -> isConciseEnabled(type) ? config.commandConfig.commandDelHome : "";
            case GET_HOME -> prefix + " " + config.commandConfig.commandGetHome;
            case GET_HOME_CONCISE -> isConciseEnabled(type) ? config.commandConfig.commandGetHome : "";
            case TP_STAGE -> prefix + " " + config.commandConfig.commandTpStage;
            case TP_STAGE_CONCISE -> isConciseEnabled(type) ? config.commandConfig.commandTpStage : "";
            case SET_STAGE -> prefix + " " + config.commandConfig.commandSetStage;
            case SET_STAGE_CONCISE -> isConciseEnabled(type) ? config.commandConfig.commandSetStage : "";
            case DEL_STAGE -> prefix + " " + config.commandConfig.commandDelStage;
            case DEL_STAGE_CONCISE -> isConciseEnabled(type) ? config.commandConfig.commandDelStage : "";
            case GET_STAGE -> prefix + " " + config.commandConfig.commandGetStage;
            case GET_STAGE_CONCISE -> isConciseEnabled(type) ? config.commandConfig.commandGetStage : "";
            case TP_BACK -> prefix + " " + config.commandConfig.commandTpBack;
            case TP_BACK_CONCISE -> isConciseEnabled(type) ? config.commandConfig.commandTpBack : "";
            case VIRTUAL_OP -> prefix + " " + config.commandConfig.commandVirtualOp;
            case VIRTUAL_OP_CONCISE -> isConciseEnabled(type) ? config.commandConfig.commandVirtualOp : "";
        };
    }

    public static int getCommandPermissionLevel(ECommandType type) {
        return switch (type) {
            case FEED_OTHER, FEED_OTHER_CONCISE -> config.permissionConfig.permissionFeedOther;
            case TP_COORDINATE, TP_COORDINATE_CONCISE -> config.permissionConfig.permissionTpCoordinate;
            case TP_STRUCTURE, TP_STRUCTURE_CONCISE -> config.permissionConfig.permissionTpStructure;
            // case TP_ASK_YES:
            // case TP_ASK_NO:
            case TP_ASK, TP_ASK_CONCISE ->
                // case TP_ASK_YES_CONCISE:
                // case TP_ASK_NO_CONCISE:
                    config.permissionConfig.permissionTpAsk;
            // case TP_HERE_YES:
            // case TP_HERE_NO:
            case TP_HERE, TP_HERE_CONCISE ->
                // case TP_HERE_YES_CONCISE:
                // case TP_HERE_NO_CONCISE:
                    config.permissionConfig.permissionTpHere;
            case TP_RANDOM, TP_RANDOM_CONCISE -> config.permissionConfig.permissionTpRandom;
            case TP_SPAWN, TP_SPAWN_CONCISE -> config.permissionConfig.permissionTpSpawn;
            case TP_SPAWN_OTHER, TP_SPAWN_OTHER_CONCISE -> config.permissionConfig.permissionTpSpawnOther;
            case TP_WORLD_SPAWN, TP_WORLD_SPAWN_CONCISE -> config.permissionConfig.permissionTpWorldSpawn;
            case TP_TOP, TP_TOP_CONCISE -> config.permissionConfig.permissionTpTop;
            case TP_BOTTOM, TP_BOTTOM_CONCISE -> config.permissionConfig.permissionTpBottom;
            case TP_UP, TP_UP_CONCISE -> config.permissionConfig.permissionTpUp;
            case TP_DOWN, TP_DOWN_CONCISE -> config.permissionConfig.permissionTpDown;
            case TP_VIEW, TP_VIEW_CONCISE -> config.permissionConfig.permissionTpView;
            case TP_HOME, SET_HOME, DEL_HOME, GET_HOME, TP_HOME_CONCISE, SET_HOME_CONCISE, DEL_HOME_CONCISE,
                 GET_HOME_CONCISE -> config.permissionConfig.permissionTpHome;
            case TP_STAGE, TP_STAGE_CONCISE -> config.permissionConfig.permissionTpStage;
            case SET_STAGE, SET_STAGE_CONCISE -> config.permissionConfig.permissionGetStage;
            case DEL_STAGE, DEL_STAGE_CONCISE -> config.permissionConfig.permissionDelStage;
            case GET_STAGE, GET_STAGE_CONCISE -> config.permissionConfig.permissionGetStage;
            case TP_BACK, TP_BACK_CONCISE -> config.permissionConfig.permissionTpBack;
            case VIRTUAL_OP, VIRTUAL_OP_CONCISE -> config.permissionConfig.permissionVirtualOp;
            default -> 0;
        };
    }

    public static int getCommandPermissionLevel(ETeleportType type) {
        return switch (type) {
            case TP_COORDINATE -> config.permissionConfig.permissionTpCoordinate;
            case TP_STRUCTURE -> config.permissionConfig.permissionTpStructure;
            case TP_ASK -> config.permissionConfig.permissionTpAsk;
            case TP_HERE -> config.permissionConfig.permissionTpHere;
            case TP_RANDOM -> config.permissionConfig.permissionTpRandom;
            case TP_SPAWN -> config.permissionConfig.permissionTpSpawn;
            case TP_WORLD_SPAWN -> config.permissionConfig.permissionTpWorldSpawn;
            case TP_TOP -> config.permissionConfig.permissionTpTop;
            case TP_BOTTOM -> config.permissionConfig.permissionTpBottom;
            case TP_UP -> config.permissionConfig.permissionTpUp;
            case TP_DOWN -> config.permissionConfig.permissionTpDown;
            case TP_VIEW -> config.permissionConfig.permissionTpView;
            case TP_HOME -> config.permissionConfig.permissionTpHome;
            case TP_STAGE -> config.permissionConfig.permissionTpStage;
            case TP_BACK -> config.permissionConfig.permissionTpBack;
            default -> 0;
        };
    }

    public static boolean isConciseEnabled(ECommandType type) {
        return switch (type) {
            case UUID, UUID_CONCISE -> config.conciseConfig.conciseUuid;
            case DIMENSION, DIMENSION_CONCISE -> config.conciseConfig.conciseDimension;
            case FEED, FEED_OTHER, FEED_CONCISE, FEED_OTHER_CONCISE -> config.conciseConfig.conciseFeed;
            case TP_COORDINATE, TP_COORDINATE_CONCISE -> config.conciseConfig.conciseTpCoordinate;
            case TP_STRUCTURE, TP_STRUCTURE_CONCISE -> config.conciseConfig.conciseTpStructure;
            case TP_ASK, TP_ASK_CONCISE -> config.conciseConfig.conciseTpAsk;
            case TP_ASK_YES, TP_ASK_YES_CONCISE -> config.conciseConfig.conciseTpAskYes;
            case TP_ASK_NO, TP_ASK_NO_CONCISE -> config.conciseConfig.conciseTpAskNo;
            case TP_HERE, TP_HERE_CONCISE -> config.conciseConfig.conciseTpHere;
            case TP_HERE_YES, TP_HERE_YES_CONCISE -> config.conciseConfig.conciseTpHereYes;
            case TP_HERE_NO, TP_HERE_NO_CONCISE -> config.conciseConfig.conciseTpHereNo;
            case TP_RANDOM, TP_RANDOM_CONCISE -> config.conciseConfig.conciseTpRandom;
            case TP_SPAWN, TP_SPAWN_OTHER, TP_SPAWN_CONCISE, TP_SPAWN_OTHER_CONCISE ->
                    config.conciseConfig.conciseTpSpawn;
            case TP_WORLD_SPAWN, TP_WORLD_SPAWN_CONCISE -> config.conciseConfig.conciseTpWorldSpawn;
            case TP_TOP, TP_TOP_CONCISE -> config.conciseConfig.conciseTpTop;
            case TP_BOTTOM, TP_BOTTOM_CONCISE -> config.conciseConfig.conciseTpBottom;
            case TP_UP, TP_UP_CONCISE -> config.conciseConfig.conciseTpUp;
            case TP_DOWN, TP_DOWN_CONCISE -> config.conciseConfig.conciseTpDown;
            case TP_VIEW, TP_VIEW_CONCISE -> config.conciseConfig.conciseTpView;
            case TP_HOME, TP_HOME_CONCISE -> config.conciseConfig.conciseTpHome;
            case SET_HOME, SET_HOME_CONCISE -> config.conciseConfig.conciseSetHome;
            case DEL_HOME, DEL_HOME_CONCISE -> config.conciseConfig.conciseDelHome;
            case GET_HOME, GET_HOME_CONCISE -> config.conciseConfig.conciseGetHome;
            case TP_STAGE, TP_STAGE_CONCISE -> config.conciseConfig.conciseTpStage;
            case SET_STAGE, SET_STAGE_CONCISE -> config.conciseConfig.conciseSetStage;
            case DEL_STAGE, DEL_STAGE_CONCISE -> config.conciseConfig.conciseDelStage;
            case GET_STAGE, GET_STAGE_CONCISE -> config.conciseConfig.conciseGetStage;
            case TP_BACK, TP_BACK_CONCISE -> config.conciseConfig.conciseTpBack;
            case VIRTUAL_OP, VIRTUAL_OP_CONCISE -> config.conciseConfig.conciseVirtualOp;
            default -> false;
        };
    }

    public static boolean hasCommandPermission(CommandSourceStack source, ECommandType type) {
        return source.hasPermission(getCommandPermissionLevel(type)) || hasVirtualPermission(source.getEntity(), type);
    }

    public static boolean hasVirtualPermission(Entity source, ECommandType type) {
        // 若为玩家
        if (source instanceof Player) {
            return VirtualPermissionManager.getVirtualPermission((Player) source).stream()
                    .filter(Objects::nonNull)
                    .anyMatch(s -> s.replaceConcise() == type.replaceConcise());
        } else {
            return false;
        }
    }

    // endregion 指令相关

    // region 安全坐标

    /**
     * 安全的方块
     */
    private static List<BlockState> SAFE_BLOCKS;
    /**
     * 不安全的方块
     */
    private static List<BlockState> UNSAFE_BLOCKS;
    private static List<BlockState> SUFFOCATING_BLOCKS;

    /**
     * 初始化方块列表
     */
    private static void initBlockLists() {
        if (SAFE_BLOCKS == null) {
            ServerLevel level = getServerLevel();
            if (level == null) {
                SAFE_BLOCKS = new ArrayList<>();
                UNSAFE_BLOCKS = new ArrayList<>();
                SUFFOCATING_BLOCKS = new ArrayList<>();
                return;
            }

            SAFE_BLOCKS = config.safeBlocks.stream()
                    .map(block -> {
                        try {
                            return BlockStateParser.parseForBlock(level.holderLookup(Registries.BLOCK), new StringReader(block), false).blockState();
                        } catch (CommandSyntaxException e) {
                            LOGGER.error("Invalid unsafe block: {}", block, e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            UNSAFE_BLOCKS = config.unsafeBlocks.stream()
                    .map(block -> {
                        try {
                            return BlockStateParser.parseForBlock(level.holderLookup(Registries.BLOCK), new StringReader(block), false).blockState();
                        } catch (CommandSyntaxException e) {
                            LOGGER.error("Invalid unsafe block: {}", block, e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            SUFFOCATING_BLOCKS = config.suffocatingBlocks.stream()
                    .map(block -> {
                        try {
                            return BlockStateParser.parseForBlock(level.holderLookup(Registries.BLOCK), new StringReader(block), false).blockState();
                        } catch (CommandSyntaxException e) {
                            LOGGER.error("Invalid unsafe block: {}", block, e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
        }
    }

    /**
     * 获取安全方块列表
     */
    public static List<BlockState> getSafeBlocks() {
        if (SAFE_BLOCKS == null) {
            initBlockLists();
        }
        return SAFE_BLOCKS;
    }

    /**
     * 获取不安全方块列表
     */
    public static List<BlockState> getUnsafeBlocks() {
        if (UNSAFE_BLOCKS == null) {
            initBlockLists();
        }
        return UNSAFE_BLOCKS;
    }

    /**
     * 获取窒息方块列表
     */
    public static List<BlockState> getSuffocatingBlocks() {
        if (SUFFOCATING_BLOCKS == null) {
            initBlockLists();
        }
        return SUFFOCATING_BLOCKS;
    }

    /**
     * 获取服务器世界
     */
    public static ServerLevel getServerLevel() {
        return getServerLevel(Level.OVERWORLD);
    }

    /**
     * 获取服务器世界
     */
    public static ServerLevel getServerLevel(ResourceKey<Level> dimension) {
        MinecraftServer server = NarcissusFarewell.getServerInstance();
        if (server == null) {
            return null; // 服务器实例不存在时返回null
        }
        return server.getLevel(dimension);
    }

    public static int getWorldMinY(Level world) {
        return world.getMinBuildHeight();
    }

    public static int getWorldMaxY(Level world) {
        return world.getMaxBuildHeight();
    }

    public static Coordinate findTopCandidate(ServerLevel world, Coordinate start) {
        if (start.getY() >= NarcissusUtils.getWorldMaxY(world)) return null;
        for (int y : IntStream.range((int) start.getY() + 1, NarcissusUtils.getWorldMaxY(world)).boxed()
                .sorted(Comparator.comparingInt(Integer::intValue).reversed())
                .toList()) {
            Coordinate candidate = new Coordinate().setX(start.getX()).setY(y).setZ(start.getZ())
                    .setYaw(start.getYaw()).setPitch(start.getPitch())
                    .setDimension(start.getDimension())
                    .setSafe(start.isSafe()).setSafeMode(start.getSafeMode());
            if (isSafeCoordinate(world, candidate)) {
                return candidate;
            }
        }
        return null;
    }

    public static Coordinate findBottomCandidate(ServerLevel world, Coordinate start) {
        if (start.getY() <= NarcissusUtils.getWorldMinY(world)) return null;
        for (int y : IntStream.range(NarcissusUtils.getWorldMinY(world), (int) start.getY() - 1).boxed()
                .sorted(Comparator.comparingInt(Integer::intValue))
                .toList()) {
            Coordinate candidate = new Coordinate().setX(start.getX()).setY(y).setZ(start.getZ())
                    .setYaw(start.getYaw()).setPitch(start.getPitch())
                    .setDimension(start.getDimension())
                    .setSafe(start.isSafe()).setSafeMode(start.getSafeMode());
            if (isSafeCoordinate(world, candidate)) {
                return candidate;
            }
        }
        return null;
    }

    public static Coordinate findUpCandidate(ServerLevel world, Coordinate start) {
        if (start.getY() >= NarcissusUtils.getWorldMaxY(world)) return null;
        for (int y : IntStream.range((int) start.getY() + 1, NarcissusUtils.getWorldMaxY(world)).boxed()
                .sorted(Comparator.comparingInt(a -> a - (int) start.getY()))
                .toList()) {
            Coordinate candidate = new Coordinate().setX(start.getX()).setY(y).setZ(start.getZ())
                    .setYaw(start.getYaw()).setPitch(start.getPitch())
                    .setDimension(start.getDimension())
                    .setSafe(start.isSafe()).setSafeMode(start.getSafeMode());
            if (isSafeCoordinate(world, candidate)) {
                return candidate;
            }
        }
        return null;
    }

    public static Coordinate findDownCandidate(ServerLevel world, Coordinate start) {
        if (start.getY() <= NarcissusUtils.getWorldMinY(world)) return null;
        for (int y : IntStream.range(NarcissusUtils.getWorldMinY(world), (int) start.getY() - 1).boxed()
                .sorted(Comparator.comparingInt(a -> (int) start.getY() - a))
                .toList()) {
            Coordinate candidate = new Coordinate().setX(start.getX()).setY(y).setZ(start.getZ())
                    .setYaw(start.getYaw()).setPitch(start.getPitch())
                    .setDimension(start.getDimension())
                    .setSafe(start.isSafe()).setSafeMode(start.getSafeMode());
            if (isSafeCoordinate(world, candidate)) {
                return candidate;
            }
        }
        return null;
    }

    public static Coordinate findViewEndCandidate(ServerPlayer player, boolean safe, int range) {
        double stepScale = 0.75;
        Coordinate start = new Coordinate(player);
        Coordinate result = null;

        // 获取玩家的起始位置
        Vec3 startPosition = player.getEyePosition(1.0F);

        // 获取玩家的视线方向
        Vec3 direction = player.getViewVector(1.0F).normalize();
        // 步长
        Vec3 stepVector = direction.scale(stepScale);

        // 初始化变量
        Vec3 currentPosition = startPosition;
        Level world = player.level();

        // 从近到远寻找碰撞点
        for (int stepCount = 0; stepCount <= range; stepCount++) {
            // 更新当前检测位置
            currentPosition = startPosition.add(stepVector.scale(stepCount));
            BlockPos currentBlockPos = new BlockPos((int) currentPosition.x, (int) currentPosition.y, (int) currentPosition.z);

            // 获取当前方块状态
            BlockState blockState = world.getBlockState(currentBlockPos);

            // 检测方块是否不可穿过
            if (!blockState.isPathfindable(world, currentBlockPos, PathComputationType.LAND)) {
                result = start.clone().fromVec3(startPosition.add(stepVector.scale(stepCount - 1)));
                break;
            }
        }

        // 若未找到碰撞点，则使用射线的终点
        if (result == null) {
            result = start.clone().fromVec3(currentPosition);
        }

        // 若需寻找安全坐标，则从碰撞点反向查找安全位置
        if (safe) {
            // 碰撞点的三维向量
            Vec3 collisionVector = result.toVec3();
            for (int stepCount = (int) Math.ceil(collisionVector.distanceTo(startPosition) / stepScale); stepCount >= 0; stepCount--) {
                currentPosition = startPosition.add(stepVector.scale(stepCount));
                BlockPos currentBlockPos = new BlockPos((int) currentPosition.x, (int) currentPosition.y, (int) currentPosition.z);
                for (int yOffset = -3; yOffset < 3; yOffset++) {
                    Coordinate candidate = start.clone().fromBlockPos(currentBlockPos).addY(yOffset);
                    // 判断当前候选坐标是否安全
                    if (isSafeCoordinate(world, candidate)) {
                        result = candidate.addX(0.5).addY(0.15).addZ(0.5);
                        stepCount = 0;
                        break;
                    }
                }
            }
        }
        // 若起点与结果相同则返回null
        if (start.equalsOfRange(result, 1)) {
            result = null;
        }
        return result;
    }

    public static Coordinate findSafeCoordinate(Coordinate coordinate, boolean belowAllowAir) {
        Level world = getWorld(coordinate.getDimension());

        int chunkX = (int) coordinate.getX() >> 4;
        int chunkZ = (int) coordinate.getZ() >> 4;

        initSuffocatingBlocks();
        return searchForSafeCoordinateInChunk(world, coordinate, chunkX, chunkZ, belowAllowAir);
    }

    private static int deterministicHash(Coordinate c) {
        int prime = 31;
        int hash = 1;
        hash = prime * hash + Integer.hashCode(c.getXInt());
        hash = prime * hash + Integer.hashCode(c.getYInt());
        hash = prime * hash + Integer.hashCode(c.getZInt());
        return hash;
    }

    private static Coordinate searchForSafeCoordinateInChunk(Level world, Coordinate coordinate, int chunkX, int chunkZ, boolean belowAllowAir) {
        // 搜索安全位置，限制在目标范围区块内
        int offset = (config.safeChunkRange - 1) * 16;
        int chunkMinX = (chunkX << 4) - offset;
        int chunkMinZ = (chunkZ << 4) - offset;
        int chunkMaxX = chunkMinX + 15 + offset;
        int chunkMaxZ = chunkMinZ + 15 + offset;

        Coordinate result = coordinate.clone();
        List<Coordinate> coordinates = new ArrayList<>();
        Comparator<Coordinate> comparator = (c1, c2) -> {
            // 计算各项距离
            double dist3D_1 = coordinate.distanceFrom(c1);
            double dist3D_2 = coordinate.distanceFrom(c2);
            double dist2D_1 = coordinate.distanceFrom2D(c1);
            double dist2D_2 = coordinate.distanceFrom2D(c2);
            double yDiff1 = Math.abs(coordinate.getY() - c1.getY());
            double yDiff2 = Math.abs(coordinate.getY() - c2.getY());

            // 分组
            int group1 = (dist3D_1 <= 16) ? 1 : (dist2D_1 <= 8 ? 2 : 3);
            int group2 = (dist3D_2 <= 16) ? 1 : (dist2D_2 <= 8 ? 2 : 3);

            // 先按组排序
            if (group1 != group2) {
                return group1 - group2;
            }

            // 同组内的排序规则：
            if (group1 == 1) {
                // 按三维距离排序
                return Double.compare(dist3D_1, dist3D_2);
            } else if (group1 == 2) {
                // 先按二维距离，再按 Y 轴偏差排序
                int cmp = Double.compare(dist2D_1, dist2D_2);
                if (cmp == 0) {
                    cmp = Double.compare(yDiff1, yDiff2);
                }
                return cmp;
            } else {
                // 使用确定性的伪随机排序
                int hash1 = deterministicHash(c1);
                int hash2 = deterministicHash(c2);
                return Integer.compare(hash1, hash2);
            }
        };

        LOGGER.debug("TimeMillis before generate: {}", System.currentTimeMillis());
        if (coordinate.getSafeMode() == ESafeMode.Y_DOWN) {
            IntStream.range((int) coordinate.getY(), NarcissusUtils.getWorldMinY(world))
                    .forEach(y -> coordinates.add(new Coordinate(coordinate.getX(), y, coordinate.getZ())));
        } else if (coordinate.getSafeMode() == ESafeMode.Y_UP) {
            IntStream.range((int) coordinate.getY(), NarcissusUtils.getWorldMaxY(world))
                    .forEach(y -> coordinates.add(new Coordinate(coordinate.getX(), y, coordinate.getZ())));
        } else if (coordinate.getSafeMode() == ESafeMode.Y_OFFSET_3) {
            IntStream.range((int) (coordinate.getY() - 3), (int) (coordinate.getY() + 3))
                    .forEach(y -> coordinates.add(new Coordinate(coordinate.getX(), y, coordinate.getZ())));
        } else {
            IntStream.range(chunkMinX, chunkMaxX)
                    .forEach(x -> IntStream.range(chunkMinZ, chunkMaxZ)
                            .forEach(z -> IntStream.range(NarcissusUtils.getWorldMinY(world), NarcissusUtils.getWorldMaxY(world))
                                    .forEach(y -> coordinates.add(new Coordinate(x, y, z)))
                            )
                    );
        }
        LOGGER.debug("TimeMillis before sorting: {}", System.currentTimeMillis());
        List<Coordinate> list = coordinates.stream().sorted(comparator).toList();
        LOGGER.debug("TimeMillis before searching: {}", System.currentTimeMillis());
        for (Coordinate c : list) {
            double offsetX = c.getX() >= 0 ? c.getX() + 0.5 : c.getX() - 0.5;
            double offsetZ = c.getZ() >= 0 ? c.getZ() + 0.5 : c.getZ() - 0.5;
            Coordinate candidate = new Coordinate().setX(offsetX).setY(c.getY() + 0.15).setZ(offsetZ)
                    .setYaw(coordinate.getYaw()).setPitch(coordinate.getPitch())
                    .setDimension(coordinate.getDimension())
                    .setSafe(coordinate.isSafe()).setSafeMode(coordinate.getSafeMode());
            if (belowAllowAir) {
                if (isAirCoordinate(world, candidate)) {
                    result = candidate;
                    break;
                }
            } else {
                initSuffocatingBlocks();
                if (isSafeCoordinate(world, candidate)) {
                    result = candidate;
                    break;
                }
            }
        }
        LOGGER.debug("TimeMillis after searching: {}", System.currentTimeMillis());
        LOGGER.debug("Target:{} | Safe:{}", coordinate.toXyzString(), result.toXyzString());
        return result;
    }

    private static boolean isAirCoordinate(Level world, Coordinate coordinate) {
        BlockState block = world.getBlockState(coordinate.toBlockPos());
        BlockState blockAbove = world.getBlockState(coordinate.toBlockPos().above());
        BlockState blockBelow = world.getBlockState(coordinate.toBlockPos().below());
        initUnsafeBlocks();
        return isSafeBlock(world, coordinate, true, block, blockAbove, blockBelow);
    }

    private static boolean isSafeCoordinate(Level world, Coordinate coordinate) {
        BlockState block = world.getBlockState(coordinate.toBlockPos());
        BlockState blockAbove = world.getBlockState(coordinate.toBlockPos().above());
        BlockState blockBelow = world.getBlockState(coordinate.toBlockPos().below());
        initUnsafeBlocks();
        initSuffocatingBlocks();
        return isSafeBlock(world, coordinate, false, block, blockAbove, blockBelow);
    }

    /**
     * 判断指定坐标是否安全
     *
     * @param block      方块
     * @param blockAbove 头部方块
     * @param blockBelow 脚下方块
     */
    private static boolean isSafeBlock(Level world, Coordinate coordinate, boolean belowAllowAir, BlockState block, BlockState blockAbove, BlockState blockBelow) {
        initUnsafeBlocks();
        initSuffocatingBlocks();
        boolean isCurrentPassable = !block.isCollisionShapeFullBlock(world, coordinate.toBlockPos())
                && !UNSAFE_BLOCKS.contains(block);

        boolean isHeadSafe = !blockAbove.isSuffocating(world, coordinate.above().toBlockPos())
                && !blockAbove.isCollisionShapeFullBlock(world, coordinate.above().toBlockPos())
                && !UNSAFE_BLOCKS.contains(blockAbove)
                && !SUFFOCATING_BLOCKS.contains(blockAbove);

        boolean isBelowValid;
        if (!blockBelow.getFluidState().isEmpty()) {
            isBelowValid = !UNSAFE_BLOCKS.contains(blockBelow);
        } else {
            isBelowValid = blockBelow.isSolidRender(world, coordinate.below().toBlockPos())
                    && !UNSAFE_BLOCKS.contains(blockBelow);
        }
        if (belowAllowAir) {
            isBelowValid = isBelowValid || blockBelow.is(Blocks.AIR) || blockBelow.is(Blocks.CAVE_AIR);
        }

        return isCurrentPassable && isHeadSafe && isBelowValid;
    }

    private static void initUnsafeBlocks() {
        if (UNSAFE_BLOCKS == null) {
            UNSAFE_BLOCKS = new ArrayList<>();

            // 完全从配置中加载不安全方块
            try {
                var config = AutoConfig.getConfigHolder(ServerConfig.class).getConfig();
                if (config.unsafeBlocks != null && !config.unsafeBlocks.isEmpty()) {
                    for (String blockId : config.unsafeBlocks) {
                        try {
                            BlockState block = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), new StringReader(blockId), false).blockState();
                            if (!UNSAFE_BLOCKS.contains(block)) {
                                UNSAFE_BLOCKS.add(block);
                            }
                        } catch (CommandSyntaxException e) {
                            LOGGER.error("Failed to parse unsafe block: " + blockId, e);
                        }
                    }
                } else {
                    // 如果配置为空，添加一些默认值以防止意外
                    LOGGER.warn("No unsafe blocks defined in config, using fallback defaults");
                    UNSAFE_BLOCKS.add(Blocks.LAVA.defaultBlockState());
                    UNSAFE_BLOCKS.add(Blocks.FIRE.defaultBlockState());
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load unsafe blocks from config", e);
                // 出错时添加基本的安全保障
                UNSAFE_BLOCKS.add(Blocks.LAVA.defaultBlockState());
                UNSAFE_BLOCKS.add(Blocks.FIRE.defaultBlockState());
            }
        }
    }

    /**
     * Initialize the SUFFOCATING_BLOCKS list if it hasn't been initialized yet
     */
    private static void initSuffocatingBlocks() {
        if (SUFFOCATING_BLOCKS == null) {
            SUFFOCATING_BLOCKS = new ArrayList<>();

            // 完全从配置中加载窒息方块
            try {
                var config = AutoConfig.getConfigHolder(ServerConfig.class).getConfig();
                if (config.suffocatingBlocks != null && !config.suffocatingBlocks.isEmpty()) {
                    for (String blockId : config.suffocatingBlocks) {
                        try {
                            BlockState block = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), new StringReader(blockId), false).blockState();
                            if (!SUFFOCATING_BLOCKS.contains(block)) {
                                SUFFOCATING_BLOCKS.add(block);
                            }
                        } catch (CommandSyntaxException e) {
                            LOGGER.error("Failed to parse suffocating block: {}", blockId, e);
                        }
                    }
                } else {
                    // 如果配置为空，添加一些默认值以防止意外
                    LOGGER.warn("No suffocating blocks defined in config, using fallback defaults");
                    SUFFOCATING_BLOCKS.add(Blocks.LAVA.defaultBlockState());
                    SUFFOCATING_BLOCKS.add(Blocks.WATER.defaultBlockState());
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load suffocating blocks from config", e);
                // 出错时添加基本的安全保障
                SUFFOCATING_BLOCKS.add(Blocks.LAVA.defaultBlockState());
                SUFFOCATING_BLOCKS.add(Blocks.WATER.defaultBlockState());
            }
        }
    }
    // endregion 安全坐标

    // region 坐标查找

    /**
     * 获取指定维度的世界实例
     */
    public static ServerLevel getWorld(ResourceKey<Level> dimension) {
        return NarcissusFarewell.getServerInstance().getLevel(dimension);
    }

    /**
     * 根据字符串ID获取生物群系的资源键
     * @param id 生物群系的字符串ID
     * @return 生物群系的资源键
     */
    public static ResourceKey<Biome> getBiome(String id) {
        return getBiome(new ResourceLocation(id));
    }

    /**
     * 根据资源位置获取生物群系的资源键
     * @param id 生物群系的资源位置
     * @return 生物群系的资源键
     */
    public static ResourceKey<Biome> getBiome(ResourceLocation id) {
        // 在 Fabric 中，我们只能创建资源键，无法在静态方法中验证其存在性
        return ResourceKey.create(Registries.BIOME, id);
    }

    /**
     * 检查生物群系是否存在
     * @param registryAccess 注册表访问
     * @param biomeKey 生物群系资源键
     * @return 是否存在
     */
    public static boolean biomeExists(RegistryAccess registryAccess, ResourceKey<Biome> biomeKey) {
        if (biomeKey == null) return false;
        return registryAccess.registryOrThrow(Registries.BIOME).containsKey(biomeKey.location());
    }

    /**
     * 获取指定范围内某个生物群系位置
     *
     * @param world       世界
     * @param start       开始位置
     * @param biome       目标生物群系
     * @param radius      搜索半径
     * @param minDistance 最小距离
     */
    public static Coordinate findNearestBiome(ServerLevel world, Coordinate start, ResourceKey<Biome> biome, int radius, int minDistance) {
        Pair<BlockPos, Holder<Biome>> nearestBiome = world.findClosestBiome3d(holder -> holder.is(biome), start.toBlockPos(), radius, minDistance, 64);
        if (nearestBiome != null) {
            BlockPos pos = nearestBiome.getFirst();
            if (pos != null) {
                return start.clone().setX(pos.getX()).setZ(pos.getZ()).setSafe(true);
            }
        }
        return null;
    }

    public static ResourceKey<Structure> getStructure(String id) {
        return getStructure(new ResourceLocation(id));
    }

    public static ResourceKey<Structure> getStructure(ResourceLocation id) {
        Map.Entry<ResourceKey<Structure>, Structure> mapEntry = NarcissusFarewell.getServerInstance().registryAccess()
                .registryOrThrow(Registries.STRUCTURE).entrySet().stream()
                .filter(entry -> entry.getKey().location().equals(id))
                .findFirst().orElse(null);
        return mapEntry != null ? mapEntry.getKey() : null;
    }

    public static TagKey<Structure> getStructureTag(String id) {
        return getStructureTag(new ResourceLocation(id));
    }

    public static TagKey<Structure> getStructureTag(ResourceLocation id) {
        return NarcissusFarewell.getServerInstance().registryAccess()
                .registryOrThrow(Registries.STRUCTURE).getTagNames()
                .filter(tag -> tag.location().equals(id))
                .findFirst().orElse(null);
    }

    /**
     * 获取指定范围内某个生物群系位置
     *
     * @param world  世界
     * @param start  开始位置
     * @param struct 目标结构
     * @param radius 搜索半径
     */
    public static Coordinate findNearestStruct(ServerLevel world, Coordinate start, ResourceKey<Structure> struct, int radius) {
        Registry<Structure> registry = world.registryAccess().registryOrThrow(Registries.STRUCTURE);
        Either<ResourceKey<Structure>, TagKey<Structure>> left = Either.left(struct);
        HolderSet.ListBacked<Structure> holderSet = left.map((resourceKey) -> registry.getHolder(resourceKey).map(HolderSet::direct), registry::getTag).orElse(null);
        if (holderSet != null) {
            Pair<BlockPos, Holder<Structure>> pair = world.getChunkSource().getGenerator().findNearestMapStructure(world, holderSet, start.toBlockPos(), radius, true);
            if (pair != null) {
                BlockPos pos = pair.getFirst();
                if (pos != null) {
                    return start.clone().setX(pos.getX()).setZ(pos.getZ()).setSafe(true);
                }
            }
        }
        return null;
    }

    /**
     * 获取指定范围内某个生物群系位置
     *
     * @param world  世界
     * @param start  开始位置
     * @param struct 目标结构
     * @param radius 搜索半径
     */
    public static Coordinate findNearestStruct(ServerLevel world, Coordinate start, TagKey<Structure> struct, int radius) {
        BlockPos pos = world.findNearestMapStructure(struct, start.toBlockPos(), radius, true);
        if (pos != null) {
            return start.clone().setX(pos.getX()).setZ(pos.getZ()).setSafe(true);
        }
        return null;
    }

    public static KeyValue<String, String> getPlayerHomeKey(ServerPlayer player, ResourceKey<Level> dimension, String name) {
        IPlayerTeleportData data = PlayerTeleportDataProvider.getData(player);
        Map<String, String> defaultHome = data.getDefaultHome();
        if (defaultHome.isEmpty() && dimension == null && StringUtils.isNullOrEmpty(name) && data.getHomeCoordinate().size() != 1) {
            return null;
        }
        KeyValue<String, String> keyValue = null;
        if (dimension == null && StringUtils.isNotNullOrEmpty(name)) {
            if (defaultHome.isEmpty() || !defaultHome.containsValue(name)) {
                keyValue = data.getHomeCoordinate().keySet().stream()
                        .filter(key -> key.getValue().equals(name))
                        .filter(key -> key.getKey().equals(player.level().dimension().location().toString()))
                        .findFirst().orElse(null);
            } else if (defaultHome.containsValue(name)) {
                List<Map.Entry<String, String>> entryList = defaultHome.entrySet().stream().filter(entry -> entry.getValue().equals(name)).toList();
                if (entryList.size() == 1) {
                    keyValue = new KeyValue<>(entryList.get(0).getKey(), entryList.get(0).getValue());
                }
            }
        } else if (dimension != null && StringUtils.isNullOrEmpty(name)) {
            if (defaultHome.containsKey(dimension.location().toString())) {
                keyValue = new KeyValue<>(dimension.location().toString(), defaultHome.get(dimension.location().toString()));
            }
        } else if (dimension != null && StringUtils.isNotNullOrEmpty(name)) {
            keyValue = data.getHomeCoordinate().keySet().stream()
                    .filter(key -> key.getValue().equals(name))
                    .filter(key -> key.getKey().equals(dimension.location().toString()))
                    .findFirst().orElse(null);
        } else if (!defaultHome.isEmpty() && dimension == null && StringUtils.isNullOrEmpty(name)) {
            if (defaultHome.size() == 1) {
                keyValue = new KeyValue<>(defaultHome.keySet().iterator().next(), defaultHome.values().iterator().next());
            } else {
                String value = defaultHome.getOrDefault(player.level().dimension().location().toString(), null);
                if (value != null) {
                    keyValue = new KeyValue<>(player.level().dimension().location().toString(), value);
                }
            }
        } else if (defaultHome.isEmpty() && dimension == null && StringUtils.isNullOrEmpty(name) && data.getHomeCoordinate().size() == 1) {
            keyValue = data.getHomeCoordinate().keySet().iterator().next();
        }
        return keyValue;
    }

    /**
     * 获取指定玩家的家坐标
     *
     * @param player    玩家
     * @param dimension 维度
     * @param name      名称
     */
    public static Coordinate getPlayerHome(ServerPlayer player, ResourceKey<Level> dimension, String name) {
        return PlayerTeleportDataProvider.getData(player).getHomeCoordinate().getOrDefault(getPlayerHomeKey(player, dimension, name), null);
    }

    /**
     * 获取距离玩家最近的驿站
     *
     * @param player 玩家
     * @return 驿站key
     */
    public static KeyValue<String, String> findNearestStageKey(ServerPlayer player) {
        WorldStageData stageData = WorldStageData.get(player);
        Map.Entry<KeyValue<String, String>, Coordinate> stageEntry = stageData.getStageCoordinate().entrySet().stream()
                .filter(entry -> entry.getKey().getKey().equals(player.level().dimension().location().toString()))
                .min(Comparator.comparingInt(entry -> {
                    Coordinate value = entry.getValue();
                    double dx = value.getX() - player.getX();
                    double dy = value.getY() - player.getY();
                    double dz = value.getZ() - player.getZ();
                    // 返回欧几里得距离的平方（避免开方操作，提高性能）
                    return (int) (dx * dx + dy * dy + dz * dz);
                })).orElse(null);
        return stageEntry != null ? stageEntry.getKey() : null;
    }

    /**
     * 获取并移除玩家离开的坐标
     *
     * @param player    玩家
     * @param type      传送类型
     * @param dimension 维度
     * @return 查询到的离开坐标（如果未找到则返回 null）
     */
    public static TeleportRecord getBackTeleportRecord(ServerPlayer player, @Nullable ETeleportType type, @Nullable ResourceKey<Level> dimension) {
        TeleportRecord result = null;
        // 获取玩家的传送数据
        IPlayerTeleportData data = PlayerTeleportDataProvider.getData(player);
        List<TeleportRecord> records = data.getTeleportRecords();
        Optional<TeleportRecord> optionalRecord = records.stream()
                .filter(record -> type == null || record.getTeleportType() == type)
                .filter(record -> type == ETeleportType.TP_BACK || record.getTeleportType() != ETeleportType.TP_BACK)
                .filter(record -> dimension == null || record.getBefore().getDimension().equals(dimension))
                .max(Comparator.comparing(TeleportRecord::getTeleportTime));
        if (optionalRecord.isPresent()) {
            result = optionalRecord.get();
        }
        return result;
    }

    public static void removeBackTeleportRecord(ServerPlayer player, TeleportRecord record) {
        PlayerTeleportDataProvider.getData(player).getTeleportRecords().remove(record);
    }

    // endregion 坐标查找

    // region 传送相关

    /**
     * 检查传送范围
     */
    public static int checkRange(ServerPlayer player, ETeleportType type, int range) {
        int maxRange = switch (type) {
            case TP_VIEW -> config.teleportViewDistanceLimit;
            default -> config.teleportRandomDistanceLimit;
        };
        if (range > maxRange) {
            NarcissusUtils.sendTranslatableMessage(player, I18nUtils.getKey(EI18nType.MESSAGE, "range_too_large"), maxRange);
        } else if (range <= 0) {
            NarcissusUtils.sendTranslatableMessage(player, I18nUtils.getKey(EI18nType.MESSAGE, "range_too_small"), 1);
        }
        return Math.min(Math.max(range, 1), maxRange);
    }

    /**
     * 执行传送请求
     */
    public static void teleportTo(@NonNull TeleportRequest request) {
        initSuffocatingBlocks();
        teleportTo(request.getRequester(), request.getTarget(), request.getTeleportType(), request.isSafe());
    }

    /**
     * 传送玩家到指定玩家
     *
     * @param from 传送者
     * @param to   目标玩家
     */
    public static void teleportTo(@NonNull ServerPlayer from, @NonNull ServerPlayer to, ETeleportType type, boolean safe) {
        if (ETeleportType.TP_HERE == type) {
            teleportTo(to, new Coordinate(from).setSafe(safe), type);
        } else {
            teleportTo(from, new Coordinate(to).setSafe(safe), type);
        }
    }

    /**
     * 传送玩家到指定坐标
     *
     * @param player 玩家
     * @param after  坐标
     */
    public static void teleportTo(@NonNull ServerPlayer player, @NonNull Coordinate after, ETeleportType type) {
        Coordinate before = new Coordinate(player);
        Level world = player.level();
        MinecraftServer server = player.getServer();
        // 别听Idea的
        if (world != null && server != null) {
            ServerLevel level = server.getLevel(after.getDimension());
            if (level != null) {
                if (after.isSafe()) {
                    // 异步的代价就是粪吗
                    player.displayClientMessage(Component.translatable(NarcissusUtils.getPlayerLanguage(player), EI18nType.MESSAGE, "safe_searching").toTextComponent(), true);
                    new Thread(() -> {
                        Coordinate finalAfter = after.clone();
                        finalAfter = findSafeCoordinate(finalAfter, false);
                        Runnable runnable;
                        // 判断是否需要在脚下放置方块
                        if (config.setBlockWhenSafeNotFound && !isSafeCoordinate(level, finalAfter)) {
                            BlockState blockState;
                            List<ItemStack> playerItemList = getPlayerItemList(player);
                            if (CollectionUtils.isNotNullOrEmpty(SAFE_BLOCKS)) {
                                if (config.getBlockFromInventory) {
                                    blockState = SAFE_BLOCKS.stream()
                                            .filter(block -> playerItemList.stream().map(ItemStack::getItem).anyMatch(item -> new ItemStack(block.getBlock()).getItem().equals(item)))
                                            .findFirst().orElse(null);
                                } else {
                                    blockState = SAFE_BLOCKS.get(0);
                                }
                            } else {
                                blockState = null;
                            }
                            if (blockState != null) {
                                initSuffocatingBlocks();
                                Coordinate airCoordinate = findSafeCoordinate(finalAfter, true);
                                if (!airCoordinate.toXyzString().equals(finalAfter.toXyzString())) {
                                    finalAfter = airCoordinate;
                                    runnable = () -> {
                                        Item blockItem = new ItemStack(blockState.getBlock()).getItem();
                                        Item remove = playerItemList.stream().map(ItemStack::getItem).filter(blockItem::equals).findFirst().orElse(null);
                                        if (remove != null) {
                                            ItemStack itemStack = new ItemStack(remove);
                                            itemStack.setCount(1);
                                            if (removeItemFromPlayerInventory(player, itemStack)) {
                                                level.setBlockAndUpdate(airCoordinate.toBlockPos().below(), blockState.getBlock().defaultBlockState());
                                            }
                                        }
                                    };
                                } else {
                                    runnable = null;
                                }
                            } else {
                                runnable = null;
                            }
                        } else {
                            runnable = null;
                        }
                        Coordinate finalAfter1 = finalAfter;
                        player.server.submit(() -> {
                            if (runnable != null) runnable.run();
                            teleportPlayer(player, finalAfter1, type, before, level);
                        });
                    }).start();
                } else {
                    teleportPlayer(player, after, type, before, level);
                }
            }
        }
    }

    private static void teleportPlayer(@NonNull ServerPlayer player, @NonNull Coordinate after, ETeleportType type, Coordinate before, ServerLevel level) {
        ResourceLocation sound = new ResourceLocation(config.tpSound);
        NarcissusUtils.playSound(player, sound, 1.0f, 1.0f);
        after.setY(Math.floor(after.getY()) + 0.1);

        // 传送跟随者
        teleportFollowers(player, after, level);
        // 传送载体与乘客
        Entity vehicle = teleportPassengers(player, null, player.getRootVehicle(), after, level);
        // 传送玩家
        doTeleport(player, after, level);
        // 使玩家重新坐上载体
        if (vehicle != null) {
            player.startRiding(vehicle, true);
            // 同步客户端状态
            broadcastPacket(new ClientboundSetPassengersPacket(vehicle));
        }

        NarcissusUtils.playSound(player, sound, 1.0f, 1.0f);
        TeleportRecord record = new TeleportRecord();
        record.setTeleportTime(new Date());
        record.setTeleportType(type);
        record.setBefore(before);
        record.setAfter(after);
        PlayerTeleportDataProvider.getData(player).addTeleportRecords(record);
    }

    /**
     * 传送载具及其所有乘客
     *
     * @param parent     载具
     * @param passenger  乘客
     * @param coordinate 目标坐标
     * @param level      目标世界
     * @return 玩家的坐骑
     */
    private static @Nullable Entity teleportPassengers(ServerPlayer player, Entity parent, Entity passenger, @NonNull Coordinate coordinate, ServerLevel level) {
        if (!config.tpWithVehicle || passenger == null) return null;

        Entity playerVehicle = null;
        List<Entity> passengers = new ArrayList<>(passenger.getPassengers());

        // 递归传送所有乘客
        for (Entity entity : passengers) {
            if (CollectionUtils.isNotNullOrEmpty(entity.getPassengers())) {
                Entity value = teleportPassengers(player, passenger, entity, coordinate, level);
                if (value != null) {
                    playerVehicle = value;
                }
            }
        }

        passengers.forEach(Entity::stopRiding);

        // 传送载具
        if (parent == null) {
            passenger = doTeleport(passenger, coordinate, level);
        }
        // 传送所有乘客
        for (Entity entity : passengers) {
            if (entity == player) {
                playerVehicle = passenger;
            } else if (entity.getVehicle() == null) {
                int oldId = entity.getId();
                entity = doTeleport(entity, coordinate, level);
                entity.startRiding(passenger, true);
                // 更新玩家乘坐的实体对象
                if (playerVehicle != null && oldId == playerVehicle.getId()) {
                    playerVehicle = entity;
                }
            }
        }
        // 同步客户端状态
        broadcastPacket(new ClientboundSetPassengersPacket(passenger));
        return playerVehicle;
    }

    /**
     * 传送跟随的实体
     */
    private static void teleportFollowers(@NonNull ServerPlayer player, @NonNull Coordinate coordinate, ServerLevel level) {
        if (!config.tpWithFollower) return;

        int followerRange = config.tpWithFollowerRange;

        // 传送主动跟随的实体
        for (TamableAnimal entity : player.level().getEntitiesOfClass(TamableAnimal.class, player.getBoundingBox().inflate(followerRange))) {
            if (entity.getOwnerUUID() != null && entity.getOwnerUUID().equals(player.getUUID()) && !entity.isOrderedToSit()) {
                doTeleport(entity, coordinate, level);
            }
        }

        // 传送拴绳实体
        for (Mob entity : player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(followerRange))) {
            if (entity.getLeashHolder() == player) {
                doTeleport(entity, coordinate, level);
            }
        }

        // 传送被吸引的非敌对实体
        for (Mob entity : player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(followerRange))) {
            // 排除敌对生物
            if (entity instanceof Monster) continue;

            // 使用反射获取 goalSelector 中的运行目标
            try {
                GoalSelector goalSelector = (GoalSelector) FieldUtils.getPrivateFieldValue(Mob.class, entity, "goalSelector");
                if (goalSelector != null) {
                    boolean isFollowing = goalSelector.getRunningGoals()
                            .anyMatch(goal -> goal.isRunning()
                                    && (goal.getGoal() instanceof TemptGoal)
                                    && FieldUtils.getPrivateFieldValue(TemptGoal.class, goal.getGoal(), FieldUtils.getTemptGoalPlayerFieldName()) == player
                            );
                    if (isFollowing) {
                        doTeleport(entity, coordinate, level);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Failed to check if entity is following player", e);
            }
        }
    }

    private static Entity doTeleport(@NonNull Entity entity, @NonNull Coordinate coordinate, ServerLevel level) {
        if (entity instanceof ServerPlayer player) {
            player.teleportTo(level, coordinate.getX(), coordinate.getY(), coordinate.getZ()
                    , coordinate.getYaw() == 0 ? player.getYRot() : (float) coordinate.getYaw()
                    , coordinate.getPitch() == 0 ? player.getXRot() : (float) coordinate.getPitch());
        } else {
            if (level == entity.level()) {
                entity.teleportToWithTicket(coordinate.getX(), coordinate.getY(), coordinate.getZ());
            } else {
                entity = entity.changeDimension(level);
            }
        }
        return entity;
    }

    // endregion 传送相关

    // region 玩家与玩家背包

    /**
     * 获取随机玩家
     */
    public static ServerPlayer getRandomPlayer() {
        try {
            List<ServerPlayer> players = NarcissusFarewell.getServerInstance().getPlayerList().getPlayers();
            return players.get(new Random().nextInt(players.size()));
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * 获取随机玩家UUID
     */
    public static UUID getRandomPlayerUUID() {
        Player randomPlayer = getRandomPlayer();
        return randomPlayer != null ? randomPlayer.getUUID() : null;
    }

    /**
     * 通过UUID获取对应的玩家
     *
     * @param uuid 玩家UUID
     */
    public static ServerPlayer getPlayer(UUID uuid) {
        try {
            return NarcissusFarewell.getServerInstance().getPlayerList().getPlayer(uuid);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * 移除玩家背包中的指定物品
     *
     * @param player       玩家
     * @param itemToRemove 要移除的物品
     * @return 是否全部移除成功
     */
    public static boolean removeItemFromPlayerInventory(ServerPlayer player, ItemStack itemToRemove) {
        Inventory inventory = player.getInventory();

        // 剩余要移除的数量
        int remainingAmount = itemToRemove.getCount();
        // 记录成功移除的物品数量，以便失败时进行回滚
        int successfullyRemoved = 0;

        // 遍历玩家背包的所有插槽
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            // 获取背包中的物品
            ItemStack stack = inventory.getItem(i);
            ItemStack copy = itemToRemove.copy();
            copy.setCount(stack.getCount());

            // 如果插槽中的物品是目标物品
            if (ItemStack.isSameItemSameTags(stack, copy)) {
                // 获取当前物品堆叠的数量
                int stackSize = stack.getCount();

                // 如果堆叠数量大于或等于剩余需要移除的数量
                if (stackSize >= remainingAmount) {
                    // 移除指定数量的物品
                    stack.shrink(remainingAmount);
                    // 记录成功移除的数量
                    successfullyRemoved += remainingAmount;
                    // 移除完毕
                    remainingAmount = 0;
                    break;
                } else {
                    // 移除该堆所有物品
                    stack.setCount(0);
                    // 记录成功移除的数量
                    successfullyRemoved += stackSize;
                    // 减少剩余需要移除的数量
                    remainingAmount -= stackSize;
                }
            }
        }

        // 如果没有成功移除所有物品，撤销已移除的部分
        if (remainingAmount > 0) {
            // 创建副本并还回成功移除的物品
            ItemStack copy = itemToRemove.copy();
            copy.setCount(successfullyRemoved);
            // 将已移除的物品添加回背包
            player.getInventory().add(copy);
        }

        // 是否成功移除所有物品
        return remainingAmount == 0;
    }
    public static List<ItemStack> getPlayerItemList(ServerPlayer player) {
        List<ItemStack> result = new ArrayList<>();
        if (player != null) {
            result.addAll(player.getInventory().items);
            result.addAll(player.getInventory().armor);
            result.addAll(player.getInventory().offhand);
            result = result.stream().filter(itemStack -> !itemStack.isEmpty() && itemStack.getItem() != Items.AIR).collect(Collectors.toList());
        }
        return result;
    }

    // endregion 玩家与玩家背包

    // region 消息相关

    /**
     * 广播消息
     *
     * @param player  发送者
     * @param message 消息
     */
    public static void broadcastMessage(ServerPlayer player, Component message) {
        player.server.getPlayerList().broadcastSystemMessage(net.minecraft.network.chat.Component.translatable("chat.type.announcement", player.getDisplayName(), message.toChatComponent()), false);
    }

    /**
     * 广播消息
     *
     * @param server  发送者
     * @param message 消息
     */
    public static void broadcastMessage(MinecraftServer server, Component message) {
        server.getPlayerList().broadcastSystemMessage(net.minecraft.network.chat.Component.translatable("chat.type.announcement", "Server", message.toChatComponent()), false);
    }

    /**
     * 发送消息
     *
     * @param player  玩家
     * @param message 消息
     */
    public static void sendMessage(ServerPlayer player, Component message) {
        player.sendSystemMessage(message.toChatComponent(NarcissusUtils.getPlayerLanguage(player)), false);
    }

    /**
     * 发送消息
     *
     * @param player  玩家
     * @param message 消息
     */
    public static void sendMessage(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal(message).toChatComponent(), false);
    }

    /**
     * 向玩家发送消息
     *
     * @param player  玩家
     * @param message 消息
     * @param overlay 是否覆盖上一条消息
     */
    public static void sendMessage(Player player, Component message, boolean overlay) {
        if (player == null || message == null) return;
        if (overlay) {
            player.displayClientMessage(message.toTextComponent(), false);
        } else {
            player.sendSystemMessage(message.toChatComponent(getPlayerLanguage(player)));
        }
    }

    /**
     * 发送翻译消息
     *
     * @param player 玩家
     * @param key    翻译键
     * @param args   参数
     */
    public static void sendTranslatableMessage(ServerPlayer player, String key, Object... args) {
        player.sendSystemMessage(Component.translatable(key, args).setLanguageCode(NarcissusUtils.getPlayerLanguage(player)).toChatComponent(), false);
    }

    /**
     * 发送翻译消息
     *
     * @param source  指令来源
     * @param success 是否成功
     * @param key     翻译键
     * @param args    参数
     */
    public static void sendTranslatableMessage(CommandSourceStack source, boolean success, String key, Object... args) {
        if (source.getEntity() != null && source.getEntity() instanceof ServerPlayer) {
            try {
                sendTranslatableMessage(source.getPlayerOrException(), key, args);
            } catch (CommandSyntaxException ignored) {
            }
        } else if (success) {
            source.sendSuccess(() -> Component.translatable(key, args).setLanguageCode(NarcissusFarewell.DEFAULT_LANGUAGE).toChatComponent(), false);
        } else {
            source.sendFailure(Component.translatable(key, args).setLanguageCode(NarcissusFarewell.DEFAULT_LANGUAGE).toChatComponent());
        }
    }

    /**
     * 广播数据包至所有玩家
     *
     * @param packet 数据包
     */
    public static void broadcastPacket(Packet<?> packet) {
        NarcissusFarewell.getServerInstance().getPlayerList().getPlayers().forEach(player -> player.connection.send(packet));
    }

    // endregion 消息相关

    // region 跨维度传送

    public static boolean isTeleportAcrossDimensionEnabled(ServerPlayer player, ResourceKey<Level> to, ETeleportType type) {
        boolean result = true;
        if (player.level().dimension() != to) {
            if (config.teleportAcrossDimension) {
                if (!NarcissusUtils.isTeleportTypeAcrossDimensionEnabled(player, type)) {
                    result = false;
                    NarcissusUtils.sendTranslatableMessage(player, I18nUtils.getKey(EI18nType.MESSAGE, "across_dimension_not_enable_for"), getCommand(type));
                }
            } else {
                result = false;
                NarcissusUtils.sendTranslatableMessage(player, I18nUtils.getKey(EI18nType.MESSAGE, "across_dimension_not_enable"));
            }
        }
        return result;
    }

    /**
     * 判断传送类型跨维度传送是否开启
     */
    public static boolean isTeleportTypeAcrossDimensionEnabled(ServerPlayer player, ETeleportType type) {
        int permission = switch (type) {
            case TP_COORDINATE -> config.permissionConfig.permissionTpCoordinateAcrossDimension;
            case TP_STRUCTURE -> config.permissionConfig.permissionTpStructureAcrossDimension;
            case TP_ASK -> config.permissionConfig.permissionTpAskAcrossDimension;
            case TP_HERE -> config.permissionConfig.permissionTpHereAcrossDimension;
            case TP_RANDOM -> config.permissionConfig.permissionTpRandomAcrossDimension;
            case TP_SPAWN -> config.permissionConfig.permissionTpSpawnAcrossDimension;
            case TP_WORLD_SPAWN -> config.permissionConfig.permissionTpWorldSpawnAcrossDimension;   
            case TP_HOME -> config.permissionConfig.permissionTpHomeAcrossDimension;
            case TP_STAGE -> config.permissionConfig.permissionTpStageAcrossDimension;
            case TP_BACK -> config.permissionConfig.permissionTpBackAcrossDimension;
            default -> 0;
        };
        return permission > -1 && player.hasPermissions(permission);
    }

    // endregion 跨维度传送

    // region 传送冷却

    /**
     * 获取传送/传送请求冷却时间
     *
     * @param player 玩家
     * @param type   传送类型
     */
    public static int getTeleportCoolDown(ServerPlayer player, ETeleportType type) {
        // 如果传送卡类型为抵消冷却时间，则不计算冷却时间
        if (config.teleportCardType == ECardType.REFUND_COOLDOWN || config.teleportCardType == ECardType.REFUND_ALL_COST_AND_COOLDOWN) {
            if (PlayerTeleportDataProvider.getData(player).getTeleportCard() > 0) {
                return 0;
            }
        }
        Instant current = Instant.now();
        int commandCoolDown = getCommandCoolDown(type);
        Instant lastTpTime = PlayerTeleportDataProvider.getData(player).getTeleportRecords(type).stream()
                .map(TeleportRecord::getTeleportTime)
                .max(Comparator.comparing(Date::toInstant))
                .orElse(new Date(0)).toInstant();
        switch (config.teleportRequestCooldownType) {
            case COMMON:
                return calculateCooldown(player.getUUID(), current, lastTpTime, config.teleportRequestCooldown, null);
            case INDIVIDUAL:
                return calculateCooldown(player.getUUID(), current, lastTpTime, commandCoolDown, type);
            case MIXED:
                int globalCommandCoolDown = config.teleportRequestCooldown;
                int individualCooldown = calculateCooldown(player.getUUID(), current, lastTpTime, commandCoolDown, type);
                int globalCooldown = calculateCooldown(player.getUUID(), current, lastTpTime, globalCommandCoolDown, null);
                return Math.max(individualCooldown, globalCooldown);
            default:
                return 0;
        }
    }

    /**
     * 获取传送命令冷却时间
     *
     * @param type 传送类型
     */
    public static int getCommandCoolDown(ETeleportType type) {
        return switch (type) {
            case TP_COORDINATE -> config.cooldownConfig.cooldownTpCoordinate;
            case TP_STRUCTURE -> config.cooldownConfig.cooldownTpStructure;
            case TP_ASK -> config.cooldownConfig.cooldownTpAsk;
            case TP_HERE -> config.cooldownConfig.cooldownTpHere;
            case TP_RANDOM -> config.cooldownConfig.cooldownTpRandom;
            case TP_SPAWN -> config.cooldownConfig.cooldownTpSpawn;
            case TP_WORLD_SPAWN -> config.cooldownConfig.cooldownTpWorldSpawn;
            case TP_TOP -> config.cooldownConfig.cooldownTpTop;
            case TP_BOTTOM -> config.cooldownConfig.cooldownTpBottom;
            case TP_UP -> config.cooldownConfig.cooldownTpUp;
            case TP_DOWN -> config.cooldownConfig.cooldownTpDown;
            case TP_VIEW -> config.cooldownConfig.cooldownTpView;
            case TP_HOME -> config.cooldownConfig.cooldownTpHome;
            case TP_STAGE -> config.cooldownConfig.cooldownTpStage;
            case TP_BACK -> config.cooldownConfig.cooldownTpBack;
            default -> 0;
        };
    }

    private static int calculateCooldown(UUID uuid, Instant current, Instant lastTpTime, int cooldown, ETeleportType type) {
        Optional<TeleportRequest> latestRequest = NarcissusFarewell.getTeleportRequest().values().stream()
                .filter(request -> request.getRequester().getUUID().equals(uuid))
                .filter(request -> type == null || request.getTeleportType() == type)
                .max(Comparator.comparing(TeleportRequest::getRequestTime));

        Instant lastRequestTime = latestRequest.map(r -> r.getRequestTime().toInstant()).orElse(current.minusSeconds(cooldown));
        return Math.max(0, Math.max(cooldown - (int) Duration.between(lastRequestTime, current).getSeconds(), cooldown - (int) Duration.between(lastTpTime, current).getSeconds()));
    }

    // endregion 传送冷却

    // region 传送代价

    /**
     * 验证传送代价
     *
     * @param player 请求传送的玩家
     * @param target 目标坐标
     * @param type   传送类型
     * @param submit 是否收取代价
     * @return 是否验证通过
     */
    public static boolean validTeleportCost(ServerPlayer player, Coordinate target, ETeleportType type, boolean submit) {
        return validateCost(player, target.getDimension(), calculateDistance(new Coordinate(player), target), type, submit);
    }

    /**
     * 验证并收取传送代价
     *
     * @param request 传送请求
     * @param submit  是否收取代价
     * @return 是否验证通过
     */
    public static boolean validTeleportCost(TeleportRequest request, boolean submit) {
        Coordinate requesterCoordinate = new Coordinate(request.getRequester());
        Coordinate targetCoordinate = new Coordinate(request.getTarget());
        return validateCost(request.getRequester(), request.getTarget().level().dimension(), calculateDistance(requesterCoordinate, targetCoordinate), request.getTeleportType(), submit);
    }

    /**
     * 通用的传送代价验证逻辑
     *
     * @param player       请求传送的玩家
     * @param targetDim    目标维度
     * @param distance     计算的距离
     * @param teleportType 传送类型
     * @param submit       是否收取代价
     * @return 是否验证通过
     */
    private static boolean validateCost(ServerPlayer player, ResourceKey<Level> targetDim, double distance, ETeleportType teleportType, boolean submit) {
        TeleportCost cost = NarcissusUtils.getCommandCost(teleportType);
        if (cost.getType() == ECostType.NONE) return true;

        double adjustedDistance;
        if (player.level().dimension() == targetDim) {
            adjustedDistance = Math.min(config.teleportCostDistanceLimit, distance);
        } else {
            adjustedDistance = config.teleportCostDistanceAcrossDimension;
        }

        double need = cost.getNum() * adjustedDistance * cost.getRate();
        int cardNeedTotal = getTeleportCardNeedPre(need);
        int cardNeed = getTeleportCardNeedPost(player, need);
        int costNeed = getTeleportCostNeedPost(player, need);
        boolean result = false;

        switch (cost.getType()) {
            case EXP_POINT:
                result = player.totalExperience >= costNeed && cardNeed == 0;
                if (!result && cardNeed == 0) {
                    NarcissusUtils.sendTranslatableMessage(player, I18nUtils.getKey(EI18nType.MESSAGE, "costConfig.not_enough"), Component.translatable(NarcissusUtils.getPlayerLanguage(player), EI18nType.WORD, "exp_point"), (int) Math.ceil(need));
                } else if (result && submit) {
                    player.giveExperiencePoints(-costNeed);
                    PlayerTeleportDataProvider.getData(player).subTeleportCard(cardNeedTotal);
                }
                break;
            case EXP_LEVEL:
                result = player.experienceLevel >= costNeed && cardNeed == 0;
                if (!result && cardNeed == 0) {
                    NarcissusUtils.sendTranslatableMessage(player, I18nUtils.getKey(EI18nType.MESSAGE, "costConfig.not_enough"), Component.translatable(NarcissusUtils.getPlayerLanguage(player), EI18nType.WORD, "exp_level"), (int) Math.ceil(need));
                } else if (result && submit) {
                    player.giveExperienceLevels(-costNeed);
                    PlayerTeleportDataProvider.getData(player).subTeleportCard(cardNeedTotal);
                }
                break;
            case HEALTH:
                result = player.getHealth() > costNeed && cardNeed == 0;
                if (!result && cardNeed == 0) {
                    NarcissusUtils.sendTranslatableMessage(player, I18nUtils.getKey(EI18nType.MESSAGE, "costConfig.not_enough"), Component.translatable(NarcissusUtils.getPlayerLanguage(player), EI18nType.WORD, "health"), (int) Math.ceil(need));
                } else if (result && submit) {
                    player.hurt(player.level().damageSources().magic(), costNeed);
                    PlayerTeleportDataProvider.getData(player).subTeleportCard(cardNeedTotal);
                }
                break;
            case HUNGER:
                result = player.getFoodData().getFoodLevel() >= costNeed && cardNeed == 0;
                if (!result && cardNeed == 0) {
                    NarcissusUtils.sendTranslatableMessage(player, I18nUtils.getKey(EI18nType.MESSAGE, "costConfig.not_enough"), Component.translatable(NarcissusUtils.getPlayerLanguage(player), EI18nType.WORD, "hunger"), (int) Math.ceil(need));
                } else if (result && submit) {
                    player.getFoodData().setFoodLevel(player.getFoodData().getFoodLevel() - costNeed);
                    PlayerTeleportDataProvider.getData(player).subTeleportCard(cardNeedTotal);
                }
                break;
            case ITEM:
                try {
                    ItemStack itemStack = ItemStack.of(TagParser.parseTag(cost.getConf()));
                    result = getItemCount(player.getInventory().items, itemStack) >= costNeed && cardNeed == 0;
                    if (!result && cardNeed == 0) {
                        NarcissusUtils.sendTranslatableMessage(player, I18nUtils.getKey(EI18nType.MESSAGE, "costConfig.not_enough"), itemStack.getDisplayName(), (int) Math.ceil(need));
                    } else if (result && submit) {
                        itemStack.setCount(costNeed);
                        result = removeItemFromPlayerInventory(player, itemStack);
                        // 代价不足
                        if (result) {
                            PlayerTeleportDataProvider.getData(player).subTeleportCard(cardNeedTotal);
                        } else {
                            NarcissusUtils.sendTranslatableMessage(player, I18nUtils.getKey(EI18nType.MESSAGE, "costConfig.not_enough"), itemStack.getDisplayName(), (int) Math.ceil(need));
                        }
                    }
                } catch (Exception ignored) {
                }
                break;
            case COMMAND:
                try {
                    result = cardNeed == 0;
                    if (result && submit) {
                        String command = cost.getConf().replaceAll("\\[num]", String.valueOf(costNeed));
                        int commandResult = NarcissusFarewell.getServerInstance().getCommands().performPrefixedCommand(player.createCommandSourceStack(), command);
                        if (commandResult > 0) {
                            PlayerTeleportDataProvider.getData(player).subTeleportCard(cardNeedTotal);
                        }
                        result = commandResult > 0;
                    }
                } catch (Exception ignored) {
                }
                break;
        }
        if (!result && cardNeed > 0) {
            NarcissusUtils.sendTranslatableMessage(player, I18nUtils.getKey(EI18nType.MESSAGE, "costConfig.not_enough"), Component.translatable(NarcissusUtils.getPlayerLanguage(player), EI18nType.WORD, "teleport_card"), (int) Math.ceil(need));
        }
        return result;
    }

    /**
     * 使用传送卡后还须支付多少代价
     */
    public static int getTeleportCostNeedPost(ServerPlayer player, double need) {
        int ceil = (int) Math.ceil(need);
        if (!config.teleportCard) return ceil;
        IPlayerTeleportData data = PlayerTeleportDataProvider.getData(player);
        return switch (config.teleportCardType) {
            case NONE -> data.getTeleportCard() > 0 ? ceil : -1;
            case LIKE_COST -> data.getTeleportCard() >= ceil ? ceil : -1;
            case REFUND_COST, REFUND_COST_AND_COOLDOWN -> Math.max(0, ceil - data.getTeleportCard());
            case REFUND_ALL_COST, REFUND_ALL_COST_AND_COOLDOWN -> data.getTeleportCard() > 0 ? 0 : ceil;
            default -> ceil;
        };
    }

    /**
     * 须支付多少传送卡
     */
    public static int getTeleportCardNeedPre(double need) {
        int ceil = (int) Math.ceil(need);
        if (!config.teleportCard) return 0;
        return switch (config.teleportCardType) {
            case LIKE_COST -> ceil;
            default -> 1;
        };
    }

    /**
     * 使用传送卡后还须支付多少传送卡
     */
    public static int getTeleportCardNeedPost(ServerPlayer player, double need) {
        int ceil = (int) Math.ceil(need);
        if (!config.teleportCard) return 0;
        IPlayerTeleportData data = PlayerTeleportDataProvider.getData(player);
        return switch (config.teleportCardType) {
            case NONE -> data.getTeleportCard() > 0 ? 0 : 1;
            case LIKE_COST -> Math.max(0, ceil - data.getTeleportCard());
            default -> 0;
        };
    }

    public static TeleportCost getCommandCost(ETeleportType type) {
        TeleportCost cost = new TeleportCost();
        switch (type) {
            case TP_COORDINATE:
                cost.setType(config.costConfig.costTpCoordinateType);
                cost.setNum(config.costConfig.costTpCoordinateNum);
                cost.setRate(config.costConfig.costTpCoordinateRate);
                cost.setConf(config.costConfig.costTpCoordinateConf);
                break;
            case TP_STRUCTURE:
                cost.setType(config.costConfig.costTpStructureType);
                cost.setNum(config.costConfig.costTpStructureNum);
                cost.setRate(config.costConfig.costTpStructureRate);
                cost.setConf(config.costConfig.costTpStructureConf);
                break;
            case TP_ASK:
                cost.setType(config.costConfig.costTpAskType);
                cost.setNum(config.costConfig.costTpAskNum);
                cost.setRate(config.costConfig.costTpAskRate);
                cost.setConf(config.costConfig.costTpAskConf);
                break;
            case TP_HERE:
                cost.setType(config.costConfig.costTpHereType);
                cost.setNum(config.costConfig.costTpHereNum);
                cost.setRate(config.costConfig.costTpHereRate);
                cost.setConf(config.costConfig.costTpHereConf);
                break;
            case TP_RANDOM:
                cost.setType(config.costConfig.costTpRandomType);
                cost.setNum(config.costConfig.costTpRandomNum);
                cost.setRate(config.costConfig.costTpRandomRate);
                cost.setConf(config.costConfig.costTpRandomConf);
                break;
            case TP_SPAWN:
                cost.setType(config.costConfig.costTpSpawnType);
                cost.setNum(config.costConfig.costTpSpawnNum);
                cost.setRate(config.costConfig.costTpSpawnRate);
                cost.setConf(config.costConfig.costTpSpawnConf);
                break;
            case TP_WORLD_SPAWN:
                cost.setType(config.costConfig.costTpWorldSpawnType);
                cost.setNum(config.costConfig.costTpWorldSpawnNum);
                cost.setRate(config.costConfig.costTpWorldSpawnRate);
                cost.setConf(config.costConfig.costTpWorldSpawnConf);
                break;
            case TP_TOP:
                cost.setType(config.costConfig.costTpTopType);
                cost.setNum(config.costConfig.costTpTopNum);
                cost.setRate(config.costConfig.costTpTopRate);
                cost.setConf(config.costConfig.costTpTopConf);
                break;
            case TP_BOTTOM:
                cost.setType(config.costConfig.costTpBottomType);
                cost.setNum(config.costConfig.costTpBottomNum);
                cost.setRate(config.costConfig.costTpBottomRate);
                cost.setConf(config.costConfig.costTpBottomConf);
                break;
            case TP_UP:
                cost.setType(config.costConfig.costTpUpType);
                cost.setNum(config.costConfig.costTpUpNum);
                cost.setRate(config.costConfig.costTpUpRate);
                cost.setConf(config.costConfig.costTpUpConf);
                break;
            case TP_DOWN:
                cost.setType(config.costConfig.costTpDownType);
                cost.setNum(config.costConfig.costTpDownNum);
                cost.setRate(config.costConfig.costTpDownRate);
                cost.setConf(config.costConfig.costTpDownConf);
                break;
            case TP_VIEW:
                cost.setType(config.costConfig.costTpViewType);
                cost.setNum(config.costConfig.costTpViewNum);
                cost.setRate(config.costConfig.costTpViewRate);
                cost.setConf(config.costConfig.costTpViewConf);
                break;
            case TP_HOME:
                cost.setType(config.costConfig.costTpHomeType);
                cost.setNum(config.costConfig.costTpHomeNum);
                cost.setRate(config.costConfig.costTpHomeRate);
                cost.setConf(config.costConfig.costTpHomeConf);
                break;
            case TP_STAGE:
                cost.setType(config.costConfig.costTpStageType);
                cost.setNum(config.costConfig.costTpStageNum);
                cost.setRate(config.costConfig.costTpStageRate);
                cost.setConf(config.costConfig.costTpStageConf);
                break;
            case TP_BACK:
                cost.setType(config.costConfig.costTpBackType);
                cost.setNum(config.costConfig.costTpBackNum);
                cost.setRate(config.costConfig.costTpBackRate);
                cost.setConf(config.costConfig.costTpBackConf);
                break;
            default:
                break;
        }
        return cost;
    }

    /**
     * 获取物品列表中特定物品的总数量
     *
     * @param items 物品列表
     * @param itemStack 要计数的物品
     * @return 物品总数
     */
    public static int getItemCount(List<ItemStack> items, ItemStack itemStack) {
        ItemStack copy = itemStack.copy();
        return items.stream().filter(item -> {
            copy.setCount(item.getCount());
            return ItemStack.isSameItemSameTags(item, copy);
        }).mapToInt(ItemStack::getCount).sum();
    }

    public static double calculateDistance(Coordinate coordinate1, Coordinate coordinate2) {
        return coordinate1.distanceFrom(coordinate2);
    }

    // endregion 传送代价

    // region 杂项


    /**
     * 获取玩家的语言设置
     *
     * @param player 玩家
     * @return 玩家的语言设置
     */
    public static String getPlayerLanguage(Player player) {
        return NarcissusFarewell.getPlayerLanguage(player);
    }

    /**
     * 复制玩家语言设置
     *
     * @param originalPlayer 原始玩家
     * @param targetPlayer   目标玩家
     */
    public static void clonePlayerLanguage(ServerPlayer originalPlayer, ServerPlayer targetPlayer) {
        FieldUtils.setPrivateFieldValue(ServerPlayer.class, targetPlayer, FieldUtils.getPlayerLanguageFieldName(originalPlayer), getPlayerLanguage(originalPlayer));
    }

    public static String getClientLanguage() {
        return Minecraft.getInstance().getLanguageManager().getSelected();
    }

    /**
     * 强行使玩家死亡
     */
    @SuppressWarnings("unchecked")
    public static boolean killPlayer(ServerPlayer player) {
        try {
            if (player.isSleeping() && !player.level().isClientSide) {
                player.stopSleeping();
            }
            player.getEntityData().set((EntityDataAccessor<? super Float>) FieldUtils.getPrivateFieldValue(LivingEntity.class, null, FieldUtils.getEntityHealthFieldName()), 0f);
            player.connection.send(new ClientboundPlayerCombatKillPacket(player.getId(), CommonComponents.EMPTY));
            if (!player.isSpectator()) {
                if (!player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
                    player.getInventory().dropAll();
                }
            }
            player.level().broadcastEntityEvent(player, (byte) 3);
            player.awardStat(Stats.DEATHS);
            player.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_DEATH));
            player.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
            player.clearFire();
            player.getCombatTracker().recheckStatus();
        } catch (Exception ignored) {
            return false;
        }
        return true;
    }

    /**
     * 获取当前mod支持的mc版本
     *
     * @return 主版本*1000000+次版本*1000+修订版本， 如 1.16.5 -> 1 * 1000000 + 16 * 1000 + 5 = 10016005
     */
    public static int getMcVersion() {
        int version = 0;
        Optional<ModContainer> container = FabricLoader.getInstance().getModContainer(NarcissusFarewell.MOD_ID);
        if (container.isPresent()) {
            String mcVersion = container.get().getMetadata().getVersion().getFriendlyString();
            // 解析版本号字符串，例如 "1.20.1"
            String[] parts = mcVersion.split("\\.");
            if (parts.length >= 3) {
                try {
                    int majorVersion = Integer.parseInt(parts[0]);
                    int minorVersion = Integer.parseInt(parts[1]);
                    int incrementalVersion = Integer.parseInt(parts[2]);
                    version = majorVersion * 1000000 + minorVersion * 1000 + incrementalVersion;
                } catch (NumberFormatException ignored) {
                    // 解析失败时返回 0
                }
            }
        }
        return version;
    }

    /**
     * 播放音效
     *
     * @param player 玩家
     * @param sound  音效
     * @param volume 音量
     * @param pitch  音调
     */
    public static void playSound(ServerPlayer player, ResourceLocation sound, float volume, float pitch) {
        SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.get(sound);
        if (soundEvent != null) {
            player.playNotifySound(soundEvent, SoundSource.PLAYERS, volume, pitch);
        }
    }

    /**
     * 清理缓存的数据
     */
    public static void clearCache() {
        biomeNames = null;
        structureNames = null;
    }

    // endregion 杂项
}
