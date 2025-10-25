package xin.vanilla.narcissus;

import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import lombok.Getter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xin.vanilla.narcissus.command.FarewellCommand;
import xin.vanilla.narcissus.config.CommonConfig;
import xin.vanilla.narcissus.config.CustomConfig;
import xin.vanilla.narcissus.config.ServerConfig;
import xin.vanilla.narcissus.data.SafeBlock;
import xin.vanilla.narcissus.data.TeleportRequest;
import xin.vanilla.narcissus.data.player.PlayerDataManager;
import xin.vanilla.narcissus.data.player.PlayerTeleportData;
import xin.vanilla.narcissus.event.ServerGameEventHandler;
import xin.vanilla.narcissus.network.ModNetworkHandler;
import xin.vanilla.narcissus.network.SplitPacket;
import xin.vanilla.narcissus.util.LogoModifier;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NarcissusFarewell 主类
 */
public class NarcissusFarewell implements ModInitializer {

    public final static String DEFAULT_COMMAND_PREFIX = "narcissus";

    public static final String MODID = "narcissus_farewell";
    public static final String ARTIFACT_ID = "xin.vanilla";

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * 服务端实例
     */
    @Getter
    private static MinecraftServer serverInstance;

    /**
     * 分片网络包缓存
     */
    @Getter
    private static final Map<String, List<? extends SplitPacket>> packetCache = new ConcurrentHashMap<>();

    /**
     * 最近一次传送请求
     */
    @Getter
    private static final Map<ServerPlayer, ServerPlayer> lastTeleportRequest = new ConcurrentHashMap<>();

    /**
     * 待处理的传送请求列表
     */
    @Getter
    private static final Map<String, TeleportRequest> teleportRequest = new ConcurrentHashMap<>();

    @Getter
    private static final SafeBlock safeBlock = new SafeBlock();

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing NarcissusFarewell");
        ForgeConfigRegistry.INSTANCE.register(MODID, ModConfig.Type.COMMON, CommonConfig.COMMON_CONFIG);
        ForgeConfigRegistry.INSTANCE.register(MODID, ModConfig.Type.SERVER, ServerConfig.SERVER_CONFIG);
        ModNetworkHandler.registerPackets();

        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);

        ServerGameEventHandler.registerEvents();
        PlayerDataManager.register();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LOGGER.debug("Registering commands");
            FarewellCommand.register(dispatcher);
        });

        CustomConfig.loadCustomConfig(false);

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            try {
                FabricLoader.getInstance().getModContainer(MODID).ifPresent(modContainer -> LogoModifier.modifyLogo());
            } catch (Exception e) {
                LOGGER.warn("Failed to modify logo", e);
            }
        }
    }

    private void onServerStarting(MinecraftServer server) {
        serverInstance = server;
        LOGGER.debug("Server starting");
    }

    private void onServerStarted(MinecraftServer server) {
        LOGGER.debug("Server started");
    }

    private void onServerStopping(MinecraftServer server) {
        PlayerTeleportData.clear();
        LOGGER.debug("Server stopping");
    }
    // region 资源ID

    public static ResourceLocation emptyResource() {
        return createResource("", "");
    }

    public static ResourceLocation createResource(String path) {
        return createResource(NarcissusFarewell.MODID, path);
    }

    public static ResourceLocation createResource(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }

    public static ResourceLocation parseResource(String location) {
        return ResourceLocation.tryParse(location);
    }

    // endregion 资源ID


    // region 外部方法
    public void reloadCustomConfig() {
        CustomConfig.loadCustomConfig(false);
    }
    // endregion 外部方法

}
