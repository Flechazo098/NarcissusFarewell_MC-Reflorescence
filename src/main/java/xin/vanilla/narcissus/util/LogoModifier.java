package xin.vanilla.narcissus.util;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Random;

/**
 *  ModMenu 只支持正方形icon，这个功能废了（不会画icon）
 */
@Deprecated
public class LogoModifier {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String MOD_ID = "narcissus_farewell";
    private static final Random RANDOM = new Random();
    private static boolean initialized = false;

    /**
     * 初始化图标修改器
     */
    public static void init() {
        if (initialized) return;
        initialized = true;

        try {
            // 确保图标目录存在
            ensureIconDirectoryExists();

            // 复制默认图标
            copyDefaultIcon();

            // 注册资源重载监听器用于动态更换图标
            registerResourceReloadListener();

            LOGGER.info("图标修改器已初始化");
        } catch (Exception e) {
            LOGGER.error("初始化图标修改器时出错", e);
        }
    }

    /**
     * 确保图标目录存在
     */
    private static void ensureIconDirectoryExists() throws IOException {
        Path iconDir = FabricLoader.getInstance().getGameDir()
                .resolve("config")
                .resolve(MOD_ID)
                .resolve("icon");
        Files.createDirectories(iconDir);
    }

    /**
     * 复制默认图标
     */
    private static void copyDefaultIcon() {
        try {
            // 源图标路径
            String iconResourcePath = "assets/narcissus_farewell/logo.png";
            InputStream iconStream = LogoModifier.class.getResourceAsStream(iconResourcePath);

            if (iconStream == null) {
                LOGGER.error("无法从资源路径加载图标: {}", iconResourcePath);
                return;
            }

            // 目标图标路径
            Path iconDir = FabricLoader.getInstance().getGameDir()
                    .resolve("assets")
                    .resolve(MOD_ID)
                    .resolve("icon");
            Path iconPath = iconDir.resolve("icon.png");

            // 复制图标
            Files.copy(iconStream, iconPath, StandardCopyOption.REPLACE_EXISTING);
            iconStream.close();

            LOGGER.info("已成功复制默认图标到: {}", iconPath);
        } catch (Exception e) {
            LOGGER.error("复制默认图标时出错", e);
        }
    }

    /**
     * 注册资源重载监听器
     */
    private static void registerResourceReloadListener() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(
                new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public ResourceLocation getFabricId() {
                        return new ResourceLocation(MOD_ID, "logo_modifier");
                    }

                    @Override
                    public void onResourceManagerReload(ResourceManager resourceManager) {
                        try {
                            // 随机选择一个图标
                            String logoName = getRandomLogoName();
                            String iconResourcePath = logoName;
                            InputStream iconStream = LogoModifier.class.getResourceAsStream(iconResourcePath);

                            if (iconStream == null) {
                                LOGGER.error("无法从资源路径加载图标: {}", iconResourcePath);
                                return;
                            }

                            // 目标图标路径
                            Path iconDir = FabricLoader.getInstance().getGameDir()
                                    .resolve("assets")
                                    .resolve(MOD_ID)
                                    .resolve("icon");
                            Path iconPath = iconDir.resolve("icon.png");

                            // 复制图标
                            Files.copy(iconStream, iconPath, StandardCopyOption.REPLACE_EXISTING);
                            iconStream.close();

                            LOGGER.info("已成功更新图标: {}", logoName);
                        } catch (Exception e) {
                            LOGGER.error("更新图标时出错", e);
                        }
                    }
                });
    }

    /**
     * 获取随机图标名称
     */
    private static String getRandomLogoName() {
        String[] logoNames = {"assets/narcissus_farewell/logo.png", "logo_.png"};
        return logoNames[RANDOM.nextInt(logoNames.length)];
    }
}