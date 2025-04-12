package xin.vanilla.narcissus.data.player;

import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;

/**
 * 玩家组件初始化器
 * 用于注册Cardinal Components API的组件
 */
public class PlayerComponentInitializer implements EntityComponentInitializer {

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        // 注册玩家传送数据组件
        registry.registerForPlayers(
                PlayerTeleportDataComponent.KEY,
                player -> new PlayerTeleportDataComponent(player),
                RespawnCopyStrategy.ALWAYS_COPY
        );
    }
}