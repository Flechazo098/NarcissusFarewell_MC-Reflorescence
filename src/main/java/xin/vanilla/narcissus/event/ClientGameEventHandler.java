package xin.vanilla.narcissus.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xin.vanilla.narcissus.network.ModNetworkHandler;
import xin.vanilla.narcissus.network.packet.*;
import xin.vanilla.narcissus.util.NarcissusUtils;

/**
 * 客户端 Game事件处理器
 */
@Environment(EnvType.CLIENT)
public class ClientGameEventHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    private static boolean keyDown = false;

    public static void registerEvents() {
        // 注册客户端Tick事件
        ClientTickEvents.END_CLIENT_TICK.register(ClientGameEventHandler::onClientTick);

        // 注册客户端连接事件
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            onPlayerLoggedIn();
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            onPlayerLoggedOut();
        });
    }

    /**
     * 客户端Tick事件
     */
    public static void onClientTick(Minecraft client) {
        if (client.screen == null) {
            // 快捷回家
            if (ClientModEventHandler.TP_HOME_KEY.consumeClick()) {
                if (!keyDown) {
                    NarcissusUtils.sendPacketToServer(ModNetworkHandler.TP_HOME, new TpHomeNotice());
                    keyDown = true;
                }
            }
            // 快捷返回
            else if (ClientModEventHandler.TP_BACK_KEY.consumeClick()) {
                if (!keyDown) {
                    NarcissusUtils.sendPacketToServer(ModNetworkHandler.TP_BACK, new TpBackNotice());
                    keyDown = true;
                }
            }
            // 快捷同意最近一条传送请求
            else if (ClientModEventHandler.TP_REQ_YES.consumeClick()) {
                if (!keyDown) {
                    NarcissusUtils.sendPacketToServer(ModNetworkHandler.TP_YES, new TpYesNotice());
                    keyDown = true;
                }
            }
            // 快捷拒绝最近一条传送请求
            else if (ClientModEventHandler.TP_REQ_NO.consumeClick()) {
                if (!keyDown) {
                    NarcissusUtils.sendPacketToServer(ModNetworkHandler.TP_NO, new TpNoNotice());
                    keyDown = true;
                }
            } else {
                keyDown = false;
            }
        }
    }

    public static void onPlayerLoggedIn() {
        LOGGER.debug("Client: Player logged in.");
        // 同步客户端配置到服务器
        NarcissusUtils.sendPacketToServer(ModNetworkHandler.CLIENT_MOD_LOADED, new ClientModLoadedNotice());
    }

    public static void onPlayerLoggedOut() {
        LOGGER.debug("Client: Player logged out.");
    }
}
