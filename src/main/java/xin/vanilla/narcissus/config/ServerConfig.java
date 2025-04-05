package xin.vanilla.narcissus.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.world.level.block.Blocks;
import xin.vanilla.narcissus.enums.ECardType;
import xin.vanilla.narcissus.enums.ECoolDownType;
import xin.vanilla.narcissus.enums.ECostType;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 服务器配置
 */
@Config(name = "narcissus_server")
public class ServerConfig implements ConfigData {

    // region 基础设置

    /**
     * 传送卡
     */
    @ConfigEntry.Gui.Tooltip
    public boolean teleportCard = false;

    /**
     * 每日传送卡数量
     */
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 9999)
    public int teleportCardDaily = 0;

    /**
     * 传送卡应用方式
     */
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public ECardType teleportCardType = ECardType.REFUND_ALL_COST;

    /**
     * 历史传送记录数量限制
     */
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 99999)
    public int teleportRecordLimit = 100;

    /**
     * 跨维度传送
     */
    @ConfigEntry.Gui.Tooltip
    public boolean teleportAcrossDimension = true;

    /**
     * 传送代价中传送距离最大取值
     */
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = Integer.MAX_VALUE)
    public int teleportCostDistanceLimit = 10000;

    /**
     * 跨维度传送时传送代价中传送距离取值
     */
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = Integer.MAX_VALUE)
    public int teleportCostDistanceAcrossDimension = 10000;

    /**
     * 传送至视线尽头时最远传送距离限制
     */
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = Integer.MAX_VALUE)
    public int teleportViewDistanceLimit = 16 * 64;

    /**
     * 传送请求过期时间
     */
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 60 * 60)
    public int teleportRequestExpireTime = 60;

    /**
     * 传送请求冷却时间计算方式
     */
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public ECoolDownType teleportRequestCooldownType = ECoolDownType.INDIVIDUAL;

    /**
     * 传送请求冷却时间
     */
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 60 * 60 * 24)
    public int teleportRequestCooldown = 10;

    /**
     * 随机传送距离限制
     */
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 5, max = Integer.MAX_VALUE)
    public int teleportRandomDistanceLimit = 10000;

    /**
     * 家的数量
     */
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 1, max = 9999)
    public int teleportHomeLimit = 5;

    /**
     * 命令前缀
     */
    @ConfigEntry.Gui.Tooltip
    public String commandPrefix = "narcissus";

    /**
     * 不安全的方块
     */
    @ConfigEntry.Gui.Tooltip
    public List<String> unsafeBlocks = Stream.of(
                    Blocks.LAVA,
                    Blocks.FIRE,
                    Blocks.CAMPFIRE,
                    Blocks.SOUL_FIRE,
                    Blocks.SOUL_CAMPFIRE,
                    Blocks.CACTUS,
                    Blocks.MAGMA_BLOCK,
                    Blocks.SWEET_BERRY_BUSH
            ).map(block -> BlockStateParser.serialize(block.defaultBlockState()))
            .collect(Collectors.toList());

    /**
     * 窒息的方块
     */
    @ConfigEntry.Gui.Tooltip
    public List<String> suffocatingBlocks = Stream.of(
                    Blocks.LAVA,
                    Blocks.WATER
            ).map(block -> BlockStateParser.serialize(block.defaultBlockState()))
            .collect(Collectors.toList());

    /**
     * 当安全传送未找到安全坐标时，是否在脚下放置方块
     */
    @ConfigEntry.Gui.Tooltip
    public boolean setBlockWhenSafeNotFound = false;

    /**
     * 当安全传送未找到安全坐标时，是否从背包中获取被放置的方块
     */
    @ConfigEntry.Gui.Tooltip
    public boolean getBlockFromInventory = true;

    /**
     * 当安全传送未找到安全坐标时，放置的方块类型
     */
    @ConfigEntry.Gui.Tooltip
    public List<String> safeBlocks = Stream.of(
                    Blocks.GRASS_BLOCK,
                    Blocks.DIRT_PATH,
                    Blocks.DIRT,
                    Blocks.COBBLESTONE
            ).map(block -> BlockStateParser.serialize(block.defaultBlockState()))
            .collect(Collectors.toList());

    /**
     * 寻找安全坐标的区块范围
     */
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 1, max = 16)
    public int safeChunkRange = 1;

    /**
     * 虚拟权限
     */
    @ConfigEntry.Gui.Tooltip
    public String opList = "";

    /**
     * 帮助指令信息头部内容
     */
    @ConfigEntry.Gui.Tooltip
    public String helpHeader = "-----==== Narcissus Farewell Help (%d/%d) ====-----";

    /**
     * 传送音效
     */
    @ConfigEntry.Gui.Tooltip
    public String tpSound = "minecraft:entity.enderman.teleport";

    /**
     * 是否允许载具一起传送
     */
    @ConfigEntry.Gui.Tooltip
    public boolean tpWithVehicle = true;

    /**
     * 是否允许跟随的实体一起传送
     */
    @ConfigEntry.Gui.Tooltip
    public boolean tpWithFollower = true;

    /**
     * 跟随的实体识别范围半径
     */
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 1, max = 16 * 16)
    public int tpWithFollowerRange = 10;

    // endregion 基础设置

    // region 功能开关
    @ConfigEntry.Gui.CollapsibleObject
    public SwitchConfig switchConfig = new SwitchConfig();

    public static class SwitchConfig {
        /**
         * 自杀或毒杀 开关
         */
        @ConfigEntry.Gui.Tooltip
        public boolean switchFeed = true;

        /**
         * 传送到指定坐标 开关
         */
        @ConfigEntry.Gui.Tooltip
        public boolean switchTpCoordinate = true;

        /**
         * 传送到指定结构 开关
         */
        @ConfigEntry.Gui.Tooltip
        public boolean switchTpStructure = true;

        /**
         * 请求传送至玩家 开关
         */
        @ConfigEntry.Gui.Tooltip
        public boolean switchTpAsk = true;

        /**
         * 请求将玩家传送至当前位置 开关
         */
        @ConfigEntry.Gui.Tooltip
        public boolean switchTpHere = true;

        /**
         * 随机传送 开关
         */
        @ConfigEntry.Gui.Tooltip
        public boolean switchTpRandom = true;

        /**
         * 传送到玩家重生点 开关
         */
        @ConfigEntry.Gui.Tooltip
        public boolean switchTpSpawn = true;

        /**
         * 传送到世界重生点 开关
         */
        @ConfigEntry.Gui.Tooltip
        public boolean switchTpWorldSpawn = true;

        /**
         * 传送到顶部 开关
         */
        @ConfigEntry.Gui.Tooltip
        public boolean switchTpTop = true;

        /**
         * 传送到底部 开关
         */
        @ConfigEntry.Gui.Tooltip
        public boolean switchTpBottom = true;

        /**
         * 传送到上方 开关
         */
        @ConfigEntry.Gui.Tooltip
        public boolean switchTpUp = true;

        /**
         * 传送到下方 开关
         */
        @ConfigEntry.Gui.Tooltip
        public boolean switchTpDown = true;

        /**
         * 传送至视线尽头 开关
         */
        @ConfigEntry.Gui.Tooltip
        public boolean switchTpView = true;

        /**
         * 传送到家 开关
         */
        @ConfigEntry.Gui.Tooltip
        public boolean switchTpHome = true;

        /**
         * 传送到驿站 开关
         */
        @ConfigEntry.Gui.Tooltip
        public boolean switchTpStage = true;

        /**
         * 传送到上次传送点 开关
         */
        @ConfigEntry.Gui.Tooltip
        public boolean switchTpBack = true;
    }
    // endregion 功能开关

    // region 指令权限
    @ConfigEntry.Gui.CollapsibleObject
    public PermissionConfig permissionConfig = new PermissionConfig();

    public static class PermissionConfig {
        @ConfigEntry.Gui.Tooltip
        public int permissionTpCoordinate = 0;

        @ConfigEntry.Gui.Tooltip
        public int permissionFeedOther = 1;

        @ConfigEntry.Gui.Tooltip
        public int permissionTpStructure = 0;

        @ConfigEntry.Gui.Tooltip
        public int permissionTpAsk = 0;

        @ConfigEntry.Gui.Tooltip
        public int permissionTpHere = 0;

        @ConfigEntry.Gui.Tooltip
        public int permissionTpRandom = 0;

        @ConfigEntry.Gui.Tooltip
        public int permissionTpSpawn = 0;

        @ConfigEntry.Gui.Tooltip
        public int permissionTpSpawnOther = 1;

        @ConfigEntry.Gui.Tooltip
        public int permissionTpWorldSpawn = 0;

        @ConfigEntry.Gui.Tooltip
        public int permissionTpTop = 0;

        @ConfigEntry.Gui.Tooltip
        public int permissionTpBottom = 0;

        @ConfigEntry.Gui.Tooltip
        public int permissionTpUp = 0;

        @ConfigEntry.Gui.Tooltip
        public int permissionTpDown = 0;

        @ConfigEntry.Gui.Tooltip
        public int permissionTpView = 0;

        @ConfigEntry.Gui.Tooltip
        public int permissionTpHome = 0;

        @ConfigEntry.Gui.Tooltip
        public int permissionTpStage = 0;

        @ConfigEntry.Gui.Tooltip
        public int permissionSetStage = 0;

        @ConfigEntry.Gui.Tooltip
        public int permissionDelStage = 0;

        @ConfigEntry.Gui.Tooltip
        public int permissionGetStage = 0;

        @ConfigEntry.Gui.Tooltip
        public int permissionTpBack = 0;

        @ConfigEntry.Gui.Tooltip
        public int permissionVirtualOp = 2;

        /**
         * 跨维度传送到指定坐标权限
         */
        @ConfigEntry.Gui.Tooltip
        public int permissionTpCoordinateAcrossDimension = 0;

        /**
         * 跨维度传送到指定结构权限
         */
        @ConfigEntry.Gui.Tooltip
        public int permissionTpStructureAcrossDimension = 0;

        /**
         * 跨维度请求传送至玩家权限
         */
        @ConfigEntry.Gui.Tooltip
        public int permissionTpAskAcrossDimension = 0;

        /**
         * 跨维度请求将玩家传送至当前位置权限
         */
        @ConfigEntry.Gui.Tooltip
        public int permissionTpHereAcrossDimension = 0;

        /**
         * 跨维度随机传送权限
         */
        @ConfigEntry.Gui.Tooltip
        public int permissionTpRandomAcrossDimension = 0;

        /**
         * 跨维度传送到玩家重生点权限
         */
        @ConfigEntry.Gui.Tooltip
        public int permissionTpSpawnAcrossDimension = 0;

        /**
         * 跨维度传送到世界重生点权限
         */
        @ConfigEntry.Gui.Tooltip
        public int permissionTpWorldSpawnAcrossDimension = 0;

        /**
         * 跨维度传送到家权限
         */
        @ConfigEntry.Gui.Tooltip
        public int permissionTpHomeAcrossDimension = 0;

        /**
         * 跨维度传送到驿站权限
         */
        @ConfigEntry.Gui.Tooltip
        public int permissionTpStageAcrossDimension = 0;

        /**
         * 跨维度传送到上次传送点权限
         */
        @ConfigEntry.Gui.Tooltip
        public int permissionTpBackAcrossDimension = 0;
    }
    // endregion 指令权限

    // region 冷却时间
    @ConfigEntry.Gui.CollapsibleObject
    public CooldownConfig cooldownConfig = new CooldownConfig();

    public static class CooldownConfig {
        /**
         * 传送到指定坐标冷却时间
         */
        @ConfigEntry.Gui.Tooltip
        public int cooldownTpCoordinate = 0;

        /**
         * 传送到指定结构冷却时间
         */
        @ConfigEntry.Gui.Tooltip
        public int cooldownTpStructure = 0;

        /**
         * 请求传送至玩家冷却时间
         */
        @ConfigEntry.Gui.Tooltip
        public int cooldownTpAsk = 0;

        /**
         * 请求将玩家传送至当前位置冷却时间
         */
        @ConfigEntry.Gui.Tooltip
        public int cooldownTpHere = 0;

        /**
         * 随机传送冷却时间
         */
        @ConfigEntry.Gui.Tooltip
        public int cooldownTpRandom = 0;

        /**
         * 传送到玩家重生点冷却时间
         */
        @ConfigEntry.Gui.Tooltip
        public int cooldownTpSpawn = 0;

        /**
         * 传送到世界重生点冷却时间
         */
        @ConfigEntry.Gui.Tooltip
        public int cooldownTpWorldSpawn = 0;

        /**
         * 传送到顶部冷却时间
         */
        @ConfigEntry.Gui.Tooltip
        public int cooldownTpTop = 0;

        /**
         * 传送到底部冷却时间
         */
        @ConfigEntry.Gui.Tooltip
        public int cooldownTpBottom = 0;

        /**
         * 传送到上方冷却时间
         */
        @ConfigEntry.Gui.Tooltip
        public int cooldownTpUp = 0;

        /**
         * 传送到下方冷却时间
         */
        @ConfigEntry.Gui.Tooltip
        public int cooldownTpDown = 0;

        /**
         * 传送至视线尽头冷却时间
         */
        @ConfigEntry.Gui.Tooltip
        public int cooldownTpView = 0;

        /**
         * 传送到家冷却时间
         */
        @ConfigEntry.Gui.Tooltip
        public int cooldownTpHome = 0;

        /**
         * 传送到驿站冷却时间
         */
        @ConfigEntry.Gui.Tooltip
        public int cooldownTpStage = 0;

        /**
         * 传送到上次传送点冷却时间
         */
        @ConfigEntry.Gui.Tooltip
        public int cooldownTpBack = 0;
    }
    // endregion 冷却时间

    // region 自定义指令
    @ConfigEntry.Gui.CollapsibleObject
    public CommandConfig commandConfig = new CommandConfig();

    public static class CommandConfig {
        /**
         * 获取玩家的UUID
         */
        @ConfigEntry.Gui.Tooltip
        public String commandUuid = "uuid";

        /**
         * 获取当前世界的维度ID
         */
        @ConfigEntry.Gui.Tooltip
        public String commandDimension = "dim";

        /**
         * 自杀或毒杀(水仙是有毒的可不能吃哦)
         */
        @ConfigEntry.Gui.Tooltip
        public String commandFeed = "feed";

        /**
         * 传送到指定坐标
         */
        @ConfigEntry.Gui.Tooltip
        public String commandTpCoordinate = "tpc";

        /**
         * 传送到指定结构
         */
        @ConfigEntry.Gui.Tooltip
        public String commandTpStructure = "tps";

        /**
         * 请求传送至玩家
         */
        @ConfigEntry.Gui.Tooltip
        public String commandTpAsk = "tpa";

        /**
         * 接受请求传送至玩家
         */
        @ConfigEntry.Gui.Tooltip
        public String commandTpAskYes = "tpaccept";

        /**
         * 拒绝请求传送至玩家
         */
        @ConfigEntry.Gui.Tooltip
        public String commandTpAskNo = "tpdeny";

        /**
         * 请求将玩家传送至当前位置
         */
        @ConfigEntry.Gui.Tooltip
        public String commandTpHere = "tph";

        /**
         * 接受请求将玩家传送至当前位置
         */
        @ConfigEntry.Gui.Tooltip
        public String commandTpHereYes = "tphaccept";

        /**
         * 拒绝请求将玩家传送至当前位置
         */
        @ConfigEntry.Gui.Tooltip
        public String commandTpHereNo = "tphdeny";

        /**
         * 随机传送
         */
        @ConfigEntry.Gui.Tooltip
        public String commandTpRandom = "tpr";

        /**
         * 传送到玩家重生点
         */
        @ConfigEntry.Gui.Tooltip
        public String commandTpSpawn = "spawn";

        /**
         * 传送到世界重生点
         */
        @ConfigEntry.Gui.Tooltip
        public String commandTpWorldSpawn = "wspawn";

        /**
         * 传送到顶部
         */
        @ConfigEntry.Gui.Tooltip
        public String commandTpTop = "top";

        /**
         * 传送到底部
         */
        @ConfigEntry.Gui.Tooltip
        public String commandTpBottom = "bottom";

        /**
         * 传送到上方
         */
        @ConfigEntry.Gui.Tooltip
        public String commandTpUp = "up";

        /**
         * 传送到下方
         */
        @ConfigEntry.Gui.Tooltip
        public String commandTpDown = "down";

        /**
         * 传送至视线尽头
         */
        @ConfigEntry.Gui.Tooltip
        public String commandTpView = "tpv";

        /**
         * 传送到家
         */
        @ConfigEntry.Gui.Tooltip
        public String commandTpHome = "home";

        /**
         * 设置家
         */
        @ConfigEntry.Gui.Tooltip
        public String commandSetHome = "sethome";

        /**
         * 删除家
         */
        @ConfigEntry.Gui.Tooltip
        public String commandDelHome = "delhome";

        /**
         * 查询家
         */
        @ConfigEntry.Gui.Tooltip
        public String commandGetHome = "gethome";

        /**
         * 传送到驿站
         */
        @ConfigEntry.Gui.Tooltip
        public String commandTpStage = "stage";

        /**
         * 设置驿站
         */
        @ConfigEntry.Gui.Tooltip
        public String commandSetStage = "setstage";

        /**
         * 删除驿站
         */
        @ConfigEntry.Gui.Tooltip
        public String commandDelStage = "delstage";

        /**
         * 查询驿站
         */
        @ConfigEntry.Gui.Tooltip
        public String commandGetStage = "getstage";

        /**
         * 传送到上次传送点
         */
        @ConfigEntry.Gui.Tooltip
        public String commandTpBack = "back";

        /**
         * 设置虚拟权限
         */
        @ConfigEntry.Gui.Tooltip
        public String commandVirtualOp = "vop";
    }
    // endregion 自定义指令

    // region 简化指令
    @ConfigEntry.Gui.CollapsibleObject
    public ConciseConfig conciseConfig = new ConciseConfig();

    public static class ConciseConfig {
        /**
         * 获取玩家的UUID
         */
        @ConfigEntry.Gui.Tooltip
        public boolean conciseUuid = true;

        /**
         * 获取当前世界的维度ID
         */
        @ConfigEntry.Gui.Tooltip
        public boolean conciseDimension = true;

        /**
         * 自杀或毒杀
         */
        @ConfigEntry.Gui.Tooltip
        public boolean conciseFeed = true;

        /**
         * 传送到指定坐标
         */
        @ConfigEntry.Gui.Tooltip
        public boolean conciseTpCoordinate = true;

        /**
         * 传送到指定结构
         */
        @ConfigEntry.Gui.Tooltip
        public boolean conciseTpStructure = true;

        /**
         * 请求传送至玩家
         */
        @ConfigEntry.Gui.Tooltip
        public boolean conciseTpAsk = true;

        /**
         * 接受请求传送至玩家
         */
        @ConfigEntry.Gui.Tooltip
        public boolean conciseTpAskYes = true;

        /**
         * 拒绝请求传送至玩家
         */
        @ConfigEntry.Gui.Tooltip
        public boolean conciseTpAskNo = true;

        /**
         * 请求将玩家传送至当前位置
         */
        @ConfigEntry.Gui.Tooltip
        public boolean conciseTpHere = true;

        /**
         * 接受请求将玩家传送至当前位置
         */
        @ConfigEntry.Gui.Tooltip
        public boolean conciseTpHereYes = true;

        /**
         * 拒绝请求将玩家传送至当前位置
         */
        @ConfigEntry.Gui.Tooltip
        public boolean conciseTpHereNo = true;

        /**
         * 随机传送
         */
        @ConfigEntry.Gui.Tooltip
        public boolean conciseTpRandom = true;

        /**
         * 传送到玩家重生点
         */
        @ConfigEntry.Gui.Tooltip
        public boolean conciseTpSpawn = true;

        /**
         * 传送到世界重生点
         */
        @ConfigEntry.Gui.Tooltip
        public boolean conciseTpWorldSpawn = true;

        /**
         * 传送到顶部
         */
        @ConfigEntry.Gui.Tooltip
        public boolean conciseTpTop = true;

        /**
         * 传送到底部
         */
        @ConfigEntry.Gui.Tooltip
        public boolean conciseTpBottom = true;

        /**
         * 传送到上方
         */
        @ConfigEntry.Gui.Tooltip
        public boolean conciseTpUp = true;

        /**
         * 传送到下方
         */
        @ConfigEntry.Gui.Tooltip
        public boolean conciseTpDown = true;

        /**
         * 传送至视线尽头
         */
        @ConfigEntry.Gui.Tooltip
        public boolean conciseTpView = true;

        /**
         * 传送到家
         */
        @ConfigEntry.Gui.Tooltip
        public boolean conciseTpHome = true;

        /**
         * 设置家
         */
        @ConfigEntry.Gui.Tooltip
        public boolean conciseSetHome = true;

        /**
         * 删除家
         */
        @ConfigEntry.Gui.Tooltip
        public boolean conciseDelHome = true;

        /**
         * 查询家
         */
        @ConfigEntry.Gui.Tooltip
        public boolean conciseGetHome = true;

        /**
         * 传送到驿站
         */
        @ConfigEntry.Gui.Tooltip
        public boolean conciseTpStage = true;

        /**
         * 设置驿站
         */
        @ConfigEntry.Gui.Tooltip
        public boolean conciseSetStage = true;

        /**
         * 删除驿站
         */
        @ConfigEntry.Gui.Tooltip
        public boolean conciseDelStage = true;

        /**
         * 查询驿站
         */
        @ConfigEntry.Gui.Tooltip
        public boolean conciseGetStage = true;

        /**
         * 传送到上次传送点
         */
        @ConfigEntry.Gui.Tooltip
        public boolean conciseTpBack = true;

        /**
         * 设置虚拟权限
         */
        @ConfigEntry.Gui.Tooltip
        public boolean conciseVirtualOp = true;
    }
    // endregion 简化指令

    // region 传送代价
    @ConfigEntry.Gui.CollapsibleObject
    public CostConfig costConfig = new CostConfig();

    public static class CostConfig {
        /**
         * 代价类型
         */
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public ECostType costTpCoordinateType = ECostType.NONE;

        /**
         * 代价数量
         */
        @ConfigEntry.Gui.Tooltip
        public int costTpCoordinateNum = 0;

        /**
         * 代价配置
         */
        @ConfigEntry.Gui.Tooltip
        public String costTpCoordinateConf = "";

        /**
         * 代价倍率(以距离为基准)
         */
        @ConfigEntry.Gui.Tooltip
        public double costTpCoordinateRate = 0.0;

        /**
         * 传送到指定结构代价类型
         */
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public ECostType costTpStructureType = ECostType.NONE;

        /**
         * 传送到指定结构代价数量
         */
        @ConfigEntry.Gui.Tooltip
        public int costTpStructureNum = 0;

        /**
         * 传送到指定结构代价配置
         */
        @ConfigEntry.Gui.Tooltip
        public String costTpStructureConf = "";

        /**
         * 传送到指定结构代价倍率
         */
        @ConfigEntry.Gui.Tooltip
        public double costTpStructureRate = 0.0;

        /**
         * 请求传送至玩家代价类型
         */
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public ECostType costTpAskType = ECostType.NONE;

        /**
         * 请求传送至玩家代价数量
         */
        @ConfigEntry.Gui.Tooltip
        public int costTpAskNum = 0;

        /**
         * 请求传送至玩家代价配置
         */
        @ConfigEntry.Gui.Tooltip
        public String costTpAskConf = "";

        /**
         * 请求传送至玩家代价倍率
         */
        @ConfigEntry.Gui.Tooltip
        public double costTpAskRate = 0.0;

        /**
         * 请求将玩家传送至当前位置代价类型
         */
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public ECostType costTpHereType = ECostType.NONE;

        /**
         * 请求将玩家传送至当前位置代价数量
         */
        @ConfigEntry.Gui.Tooltip
        public int costTpHereNum = 0;

        /**
         * 请求将玩家传送至当前位置代价配置
         */
        @ConfigEntry.Gui.Tooltip
        public String costTpHereConf = "";

        /**
         * 请求将玩家传送至当前位置代价倍率
         */
        @ConfigEntry.Gui.Tooltip
        public double costTpHereRate = 0.0;

        /**
         * 随机传送代价类型
         */
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public ECostType costTpRandomType = ECostType.NONE;

        /**
         * 随机传送代价数量
         */
        @ConfigEntry.Gui.Tooltip
        public int costTpRandomNum = 0;

        /**
         * 随机传送代价配置
         */
        @ConfigEntry.Gui.Tooltip
        public String costTpRandomConf = "";

        /**
         * 随机传送代价倍率
         */
        @ConfigEntry.Gui.Tooltip
        public double costTpRandomRate = 0.0;

        /**
         * 传送到玩家重生点代价类型
         */
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public ECostType costTpSpawnType = ECostType.NONE;

        /**
         * 传送到玩家重生点代价数量
         */
        @ConfigEntry.Gui.Tooltip
        public int costTpSpawnNum = 0;

        /**
         * 传送到玩家重生点代价配置
         */
        @ConfigEntry.Gui.Tooltip
        public String costTpSpawnConf = "";

        /**
         * 传送到玩家重生点代价倍率
         */
        @ConfigEntry.Gui.Tooltip
        public double costTpSpawnRate = 0.0;

        /**
         * 传送到世界重生点代价类型
         */
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public ECostType costTpWorldSpawnType = ECostType.NONE;

        /**
         * 传送到世界重生点代价数量
         */
        @ConfigEntry.Gui.Tooltip
        public int costTpWorldSpawnNum = 0;

        /**
         * 传送到世界重生点代价配置
         */
        @ConfigEntry.Gui.Tooltip
        public String costTpWorldSpawnConf = "";

        /**
         * 传送到世界重生点代价倍率
         */
        @ConfigEntry.Gui.Tooltip
        public double costTpWorldSpawnRate = 0.0;

        /**
         * 传送到顶部代价类型
         */
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public ECostType costTpTopType = ECostType.NONE;

        /**
         * 传送到顶部代价数量
         */
        @ConfigEntry.Gui.Tooltip
        public int costTpTopNum = 0;

        /**
         * 传送到顶部代价配置
         */
        @ConfigEntry.Gui.Tooltip
        public String costTpTopConf = "";

        /**
         * 传送到顶部代价倍率
         */
        @ConfigEntry.Gui.Tooltip
        public double costTpTopRate = 0.0;

        /**
         * 传送到底部代价类型
         */
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public ECostType costTpBottomType = ECostType.NONE;

        /**
         * 传送到底部代价数量
         */
        @ConfigEntry.Gui.Tooltip
        public int costTpBottomNum = 0;

        /**
         * 传送到底部代价配置
         */
        @ConfigEntry.Gui.Tooltip
        public String costTpBottomConf = "";

        /**
         * 传送到底部代价倍率
         */
        @ConfigEntry.Gui.Tooltip
        public double costTpBottomRate = 0.0;

        /**
         * 传送到上方代价类型
         */
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public ECostType costTpUpType = ECostType.NONE;

        /**
         * 传送到上方代价数量
         */
        @ConfigEntry.Gui.Tooltip
        public int costTpUpNum = 0;

        /**
         * 传送到上方代价配置
         */
        @ConfigEntry.Gui.Tooltip
        public String costTpUpConf = "";

        /**
         * 传送到上方代价倍率
         */
        @ConfigEntry.Gui.Tooltip
        public double costTpUpRate = 0.0;

        /**
         * 传送到下方代价类型
         */
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public ECostType costTpDownType = ECostType.NONE;

        /**
         * 传送到下方代价数量
         */
        @ConfigEntry.Gui.Tooltip
        public int costTpDownNum = 0;

        /**
         * 传送到下方代价配置
         */
        @ConfigEntry.Gui.Tooltip
        public String costTpDownConf = "";

        /**
         * 传送到下方代价倍率
         */
        @ConfigEntry.Gui.Tooltip
        public double costTpDownRate = 0.0;

        /**
         * 传送至视线尽头代价类型
         */
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public ECostType costTpViewType = ECostType.NONE;

        /**
         * 传送至视线尽头代价数量
         */
        @ConfigEntry.Gui.Tooltip
        public int costTpViewNum = 0;

        /**
         * 传送至视线尽头代价配置
         */
        @ConfigEntry.Gui.Tooltip
        public String costTpViewConf = "";

        /**
         * 传送至视线尽头代价倍率
         */
        @ConfigEntry.Gui.Tooltip
        public double costTpViewRate = 0.0;

        /**
         * 传送到家代价类型
         */
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public ECostType costTpHomeType = ECostType.NONE;

        /**
         * 传送到家代价数量
         */
        @ConfigEntry.Gui.Tooltip
        public int costTpHomeNum = 0;

        /**
         * 传送到家代价配置
         */
        @ConfigEntry.Gui.Tooltip
        public String costTpHomeConf = "";

        /**
         * 传送到家代价倍率
         */
        @ConfigEntry.Gui.Tooltip
        public double costTpHomeRate = 0.0;

        /**
         * 传送到驿站代价类型
         */
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public ECostType costTpStageType = ECostType.NONE;

        /**
         * 传送到驿站代价数量
         */
        @ConfigEntry.Gui.Tooltip
        public int costTpStageNum = 0;

        /**
         * 传送到驿站代价配置
         */
        @ConfigEntry.Gui.Tooltip
        public String costTpStageConf = "";

        /**
         * 传送到驿站代价倍率
         */
        @ConfigEntry.Gui.Tooltip
        public double costTpStageRate = 0.0;

        /**
         * 传送到上次传送点代价类型
         */
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public ECostType costTpBackType = ECostType.NONE;

        /**
         * 传送到上次传送点代价数量
         */
        @ConfigEntry.Gui.Tooltip
        public int costTpBackNum = 0;

        /**
         * 传送到上次传送点代价配置
         */
        @ConfigEntry.Gui.Tooltip
        public String costTpBackConf = "";

        /**
         * 传送到上次传送点代价倍率
         */
        @ConfigEntry.Gui.Tooltip
        public double costTpBackRate = 0.0;
    }
// endregion 传送代价
    @Override
    public void validatePostLoad() {
        // 在配置加载后进行验证
        // 可以在这里添加配置验证逻辑
    }
}
