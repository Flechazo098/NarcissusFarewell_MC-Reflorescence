package xin.vanilla.narcissus.util;

import lombok.Getter;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class LogoModifier {

    private static final Logger LOGGER = LogManager.getLogger();
    @Getter
    private static String selectedLogo = null;
    @Getter
    private static final String previousLogo = null;

    public static void modifyLogo() {
        if (selectedLogo == null) {
            selectedLogo = getLogoName();
        }

        try {
            if (switchIconFile()) {
                LOGGER.info("[LogoModifier] Successfully switched icon to {}", selectedLogo);
            } else {
                LOGGER.warn("[LogoModifier] Failed to switch icon file");
            }
        } catch (Exception e) {
            LOGGER.error("[LogoModifier] Failed to modify logo", e);
        }
    }

    private static boolean switchIconFile() {
        try {
            Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer("narcissus_farewell");
            if (modContainer.isEmpty()) {
                LOGGER.warn("[LogoModifier] Mod container not found");
                return false;
            }

            // 获取资源文件路径
            Optional<Path> logoPathOpt = modContainer.get().findPath("assets/narcissus_farewell/logo.png");
            Optional<Path> logo_PathOpt = modContainer.get().findPath("assets/narcissus_farewell/logo_.png");
            Optional<Path> iconPathOpt = modContainer.get().findPath("assets/narcissus_farewell/icon.png");

            if (logoPathOpt.isEmpty() || logo_PathOpt.isEmpty()) {
                LOGGER.warn("[LogoModifier] Source logo files not found");
                return false;
            }

            Path logoPath = logoPathOpt.get();
            Path logo_Path = logo_PathOpt.get();
            Path iconPath = iconPathOpt.orElse(logoPath.getParent().resolve("icon.png"));

            // 确保源文件存在
            if (!Files.exists(logoPath) || !Files.exists(logo_Path)) {
                LOGGER.warn("[LogoModifier] Source files do not exist: logo.png={}, logo_.png={}",
                        Files.exists(logoPath), Files.exists(logo_Path));
                return false;
            }

            // 选择要复制的源文件
            Path sourceFile;
            if ("logo.png".equals(selectedLogo)) {
                sourceFile = logoPath;
            } else if ("logo_.png".equals(selectedLogo)) {
                sourceFile = logo_Path;
            } else {
                LOGGER.warn("[LogoModifier] Unknown logo selection: {}", selectedLogo);
                return false;
            }

            // 复制文件到icon.png
            Files.copy(sourceFile, iconPath, StandardCopyOption.REPLACE_EXISTING);

            LOGGER.info("[LogoModifier] Successfully copied {} to icon.png", selectedLogo);
            return true;

        } catch (IOException e) {
            LOGGER.error("[LogoModifier] IO error during file switching", e);
            return false;
        } catch (Exception e) {
            LOGGER.error("[LogoModifier] Unexpected error during file switching", e);
            return false;
        }
    }

    public static String getLogoName() {
        return Math.random() > 0.5 ? "logo_.png" : "logo.png";
    }
}
