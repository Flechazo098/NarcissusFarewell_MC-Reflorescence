package xin.vanilla.narcissus;

import lombok.Getter;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xin.vanilla.narcissus.command.FarewellCommand;
import xin.vanilla.narcissus.config.ConfigManager;
import xin.vanilla.narcissus.config.ServerConfig;
import xin.vanilla.narcissus.config.TeleportRequest;
import xin.vanilla.narcissus.event.ServerEventHandler;
import xin.vanilla.narcissus.network.ModNetworkHandler;
import xin.vanilla.narcissus.network.SplitPacket;
import xin.vanilla.narcissus.util.NarcissusUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * NarcissusFarewell 主类
 */
public class NarcissusFarewell implements ModInitializer {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "narcissus_farewell";
    public static final String DEFAULT_COMMAND_PREFIX = "farewell";
    public static final String DEFAULT_LANGUAGE = "en_us";

    /**
     * -- GETTER --
     *  获取服务器实例
     *
     */
    // 服务器实例
    @Getter
    private static MinecraftServer serverInstance;

    /**
     * -- GETTER --
     *  获取玩家能力组件同步状态
     *
     */
    // 玩家能力组件同步状态
    @Getter
    private static final Map<String, Boolean> playerCapabilityStatus = new HashMap<>();

    /**
     * -- GETTER --
     *  获取传送请求
     *
     */
    // 传送请求 - 修改为使用String作为键
    @Getter
    private static final Map<String, TeleportRequest> teleportRequest = new HashMap<>();

    /**
     * -- GETTER --
     *  获取最近的传送请求
     *
     */
    // 最近的传送请求
    @Getter
    private static final Map<ServerPlayer, ServerPlayer> lastTeleportRequest = new HashMap<>();

    /**
     * -- GETTER --
     *  获取数据包缓存
     *
     */
    // 添加到类的字段部分
    @Getter
    private static final Map<String, Map<Integer, SplitPacket>> packetCache = new HashMap<>();

    // 添加玩家语言映射
    @Getter
    private static final Map<String, String> playerLanguages = new HashMap<>();


    @Override
    public void onInitialize() {
        LOGGER.info("Initializing NarcissusFarewell...");

        // 注册配置
        registerConfig();

        // 注册服务器生命周期事件
        registerServerLifecycleEvents();

        // 注册服务器事件
        ServerEventHandler.registerEvents();

        // 注册命令
        registerCommands();

        // 注册网络包处理器
        registerNetworkHandlers();

        LOGGER.info("NarcissusFarewell initialized successfully!");
    }

    /**
     * 注册配置
     */
    private void registerConfig() {
        AutoConfig.register(ServerConfig.class, JanksonConfigSerializer::new);
        ConfigManager.init();
    }

    private void registerServerLifecycleEvents() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            serverInstance = server;
            LOGGER.info("Server starting, instance saved.");
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            serverInstance = null;
            LOGGER.info("Server stopping, instance cleared.");

            // 清理所有缓存的数据
            NarcissusUtils.clearCache();
            if (FarewellCommand.helpMessage != null) {
                FarewellCommand.helpMessage = null;
            }
        });
    }


    /**
     * 注册命令
     */
    private void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> FarewellCommand.register(dispatcher));
    }

    /**
     * 注册网络包处理器
     */
    private void registerNetworkHandlers() {
        ModNetworkHandler.registerHandlers();
    }

    /**
     * 获取玩家的语言设置
     *
     * @param player 玩家
     * @return 玩家的语言设置，如果未知则返回默认语言
     */
    public static String getPlayerLanguage(Player player) {
        return playerLanguages.getOrDefault(player.getStringUUID(), DEFAULT_LANGUAGE);
    }

    /**
     * 设置玩家的语言设置
     *
     * @param player 玩家
     * @param language 语言代码
     */
    public static void setPlayerLanguage(Player player, String language) {
        playerLanguages.put(player.getStringUUID(), language);
    }

    /**
     * 克隆玩家语言设置
     *
     * @param source 源玩家
     * @param target 目标玩家
     */
    public static void clonePlayerLanguage(Player source, Player target) {
        if (source instanceof ServerPlayer && target instanceof ServerPlayer) {
            String sourceUUID = source.getStringUUID();
            String targetUUID = target.getStringUUID();

            if (playerCapabilityStatus.containsKey(sourceUUID)) {
                playerCapabilityStatus.put(targetUUID, playerCapabilityStatus.get(sourceUUID));
            }
        }
    }
}