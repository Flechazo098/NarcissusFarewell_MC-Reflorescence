package xin.vanilla.narcissus.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 客户端 Mod事件处理器
 */
@Environment(EnvType.CLIENT)
public class ClientModEventHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String CATEGORIES = "key.narcissus_farewell.categories";

    // 定义按键绑定
    public static KeyMapping TP_HOME_KEY = new KeyMapping("key.narcissus_farewell.tp_home",
            -1, CATEGORIES);
    public static KeyMapping TP_BACK_KEY = new KeyMapping("key.narcissus_farewell.tp_back",
            -1, CATEGORIES);
    public static KeyMapping TP_REQ_YES = new KeyMapping("key.narcissus_farewell.tp_req_yes",
            -1, CATEGORIES);
    public static KeyMapping TP_REQ_NO = new KeyMapping("key.narcissus_farewell.tp_req_no",
            -1, CATEGORIES);

    /**
     * 注册键绑定
     */
    public static void registerKeyBindings() {
        // 注册键绑定
        LOGGER.debug("Registering key bindings");
        KeyBindingHelper.registerKeyBinding(TP_HOME_KEY);
        KeyBindingHelper.registerKeyBinding(TP_BACK_KEY);
        KeyBindingHelper.registerKeyBinding(TP_REQ_YES);
        KeyBindingHelper.registerKeyBinding(TP_REQ_NO);
    }

}
