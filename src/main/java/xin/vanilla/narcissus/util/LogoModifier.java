package xin.vanilla.narcissus.util;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Random;

public class LogoModifier {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String MOD_ID = "narcissus";
    private static final Random RANDOM = new Random();
    private static boolean initialized = false;

    /**
     * 初始化图标修改器
     */
    public static void init() {
        if (initialized) return;
        initialized = true;

        try {
            // 获取模组目录
            Path modDir = FabricLoader.getInstance().getModContainer(MOD_ID)
                    .map(container -> new File(container.getOrigin().toString()).toPath())
                    .orElse(null);

            if (modDir == null) {
                LOGGER.error("无法找到模组目录");
                return;
            }

            // 注册资源重载监听器
            ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(
                    new SimpleSynchronousResourceReloadListener() {
                        @Override
                        public ResourceLocation getFabricId() {
                            return new ResourceLocation(MOD_ID, "logo_modifier");
                        }

                        @Override
                        public void onResourceManagerReload(ResourceManager resourceManager) {
                            try {
                                updateModIcon(modDir);
                            } catch (Exception e) {
                                LOGGER.error("更新模组图标时出错", e);
                            }
                        }
                    }
            );

            // 初始化时也更新一次图标
            updateModIcon(modDir);
            LOGGER.info("图标修改器已初始化");
        } catch (Exception e) {
            LOGGER.error("初始化图标修改器时出错", e);
        }
    }

    /**
     * 更新模组图标
     * @param modDir 模组目录
     */
    private static void updateModIcon(Path modDir) throws IOException {
        // 获取随机图标名称
        String logoName = getLogoName();

        // 源图标路径（从资源目录）
        Path sourcePath = modDir.resolve("resources").resolve(logoName);

        // 目标图标路径（fabric.mod.json 中引用的图标）
        Path targetPath = modDir.resolve("icon.png");

        if (Files.exists(sourcePath)) {
            // 复制图标文件
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.debug("已将图标更新为: {}", logoName);
        } else {
            LOGGER.warn("找不到图标文件: {}", sourcePath);
        }
    }

    /**
     * 获取随机图标名称
     * @return 图标文件名
     */
    public static String getLogoName() {
        return RANDOM.nextDouble() > 0.5 ? "logo_.png" : "logo.png";
    }
}