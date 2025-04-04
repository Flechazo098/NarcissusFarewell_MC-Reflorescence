package xin.vanilla.narcissus.config;

import me.shedaniel.autoconfig.AutoConfig;

/**
 * 配置管理类
 */
public class ConfigManager {
    // 静态配置实例
    public static ServerConfig config;

    /**
     * 初始化配置
     */
    public static void init() {
        config = AutoConfig.getConfigHolder(ServerConfig.class).getConfig();
    }

    /**
     * 重新加载配置
     */
    public static void reload() {
        config = AutoConfig.getConfigHolder(ServerConfig.class).getConfig();
    }
}