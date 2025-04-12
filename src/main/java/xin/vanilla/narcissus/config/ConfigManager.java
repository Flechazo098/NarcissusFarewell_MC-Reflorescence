package xin.vanilla.narcissus.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;

/**
 * 配置管理类
 */
public class ConfigManager {
    // 静态配置实例
    private static ServerConfig config;

    /**
     * 初始化配置
     */
    public static void init() {
        config = getHolder().getConfig();
    }

    /**
     * 重新加载配置
     */
    public static void reload() {
        config = getHolder().getConfig();
    }

    /**
     * 保存当前配置
     */
    public static void save() {
        getHolder().save();
    }

    /**
     * 获取配置实例
     *
     * @return 当前的配置实例
     */
    public static ServerConfig getConfig() {
        if (config == null) {
            init(); // 如果配置未初始化，则自动初始化
        }
        return config;
    }

    /**
     * 获取 AutoConfig 的 ConfigHolder 实例
     *
     * @return ConfigHolder<ServerConfig>
     */
    private static ConfigHolder<ServerConfig> getHolder() {
        return AutoConfig.getConfigHolder(ServerConfig.class);
    }
}