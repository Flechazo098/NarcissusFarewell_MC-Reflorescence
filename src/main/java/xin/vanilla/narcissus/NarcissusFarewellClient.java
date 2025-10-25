package xin.vanilla.narcissus;

import net.fabricmc.api.ClientModInitializer;
import xin.vanilla.narcissus.event.ClientGameEventHandler;
import xin.vanilla.narcissus.event.ClientModEventHandler;
import xin.vanilla.narcissus.network.ClientProxy;

public class NarcissusFarewellClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientProxy.registerClientPackets();
        ClientModEventHandler.registerKeyBindings();
        ClientGameEventHandler.registerEvents();
    }
}
