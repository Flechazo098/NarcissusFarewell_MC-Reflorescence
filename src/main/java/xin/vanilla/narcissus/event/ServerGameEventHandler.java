package xin.vanilla.narcissus.event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 服务端 Game事件处理器
 */
public class ServerGameEventHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void registerEvents() {
        // 服务端事件已在EventHandlerProxy中注册
        // 这个类现在主要作为服务端事件的代理
        EventHandlerProxy.registerEvents();
    }
}
