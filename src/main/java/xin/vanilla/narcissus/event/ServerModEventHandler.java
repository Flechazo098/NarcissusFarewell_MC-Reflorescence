package xin.vanilla.narcissus.event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 服务端 Mod事件处理器
 */
public class ServerModEventHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void registerEvents() {
        // 服务端模组事件注册
        // 目前没有特定的服务端模组事件需要注册
        LOGGER.debug("Server mod events registered");
    }
}
