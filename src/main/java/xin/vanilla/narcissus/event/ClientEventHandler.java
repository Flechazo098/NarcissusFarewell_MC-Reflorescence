package xin.vanilla.narcissus.event;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import xin.vanilla.narcissus.NarcissusFarewell;
import xin.vanilla.narcissus.network.*;

/**
 * 客户端事件处理器
 */
@Environment(EnvType.CLIENT)
public class ClientEventHandler implements ClientModInitializer {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String CATEGORIES = "key.narcissus_farewell.categories";

    // 定义按键绑定
    public static KeyMapping TP_HOME_KEY;
    public static KeyMapping TP_BACK_KEY;
    public static KeyMapping TP_REQ_YES;
    public static KeyMapping TP_REQ_NO;

    private static boolean keyDown = false;

    @Override
    public void onInitializeClient() {
        // 注册键绑定
        registerKeyBindings();
        ModNetworkHandler.registerClientReceivers();
        registerSyncClientLanguage();

        // 注册客户端Tick事件
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (Minecraft.getInstance().screen == null) {
                // 快捷回家
                if (TP_HOME_KEY.consumeClick()) {
                    if (!keyDown) {
                        ModNetworkHandler.sendTpHomeToServer();
                        keyDown = true;
                    }
                }
                // 快捷返回
                else if (TP_BACK_KEY.consumeClick()) {
                    if (!keyDown) {
                        ModNetworkHandler.sendTpBackToServer();
                        keyDown = true;
                    }
                }
                // 快捷同意最近一条传送请求
                else if (TP_REQ_YES.consumeClick()) {
                    if (!keyDown) {
                        ModNetworkHandler.sendTpYesToServer();
                        keyDown = true;
                    }
                }
                // 快捷拒绝最近一条传送请求
                else if (TP_REQ_NO.consumeClick()) {
                    if (!keyDown) {
                        ModNetworkHandler.sendTpNoToServer();
                        keyDown = true;
                    }
                } else {
                    keyDown = false;
                }
            }
        });

        // 注册客户端连接事件
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            // 当客户端连接到服务器时，发送语言设置
            String language = Minecraft.getInstance().options.languageCode;
            ModNetworkHandler.sendClientLanguageToServer(language);
        });

        // 注册资源重载事件
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            // 当客户端启动完成时，准备好发送语言信息
            client.getLanguageManager().onResourceManagerReload(client.getResourceManager());
        });
    }

    /**
     * 注册键绑定
     */
    private void registerKeyBindings() {
        TP_HOME_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.narcissus_farewell.tp_home",
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORIES
        ));

        TP_BACK_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.narcissus_farewell.tp_back",
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORIES
        ));

        TP_REQ_YES = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.narcissus_farewell.tp_req_yes",
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORIES
        ));

        TP_REQ_NO = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.narcissus_farewell.tp_req_no",
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORIES
        ));
    }
    /**
     * 注册客户端事件
     */
    public static void registerSyncClientLanguage() {
        // 注册资源重载监听器
        registerResourceReloadListener();

        // 注册客户端启动完成事件
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            // 客户端启动完成后同步语言
            ModNetworkHandler.syncClientLanguageOnResourceReload();
        });
    }
    /**
     * 注册资源重载监听器
     */
    private static void registerResourceReloadListener() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(
                new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public ResourceLocation getFabricId() {
                        return new ResourceLocation(NarcissusFarewell.MOD_ID, "language_sync");
                    }

                    @Override
                    public void onResourceManagerReload(ResourceManager resourceManager) {
                        // 资源重载时同步语言
                        LOGGER.info("Resource reload detected, syncing language...");
                        ModNetworkHandler.syncClientLanguageOnResourceReload();
                    }
                }
        );
    }
}