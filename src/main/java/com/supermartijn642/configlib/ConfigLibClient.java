package com.supermartijn642.configlib;

import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.function.Consumer;

/**
 * Created 08/04/2022 by SuperMartijn642
 */
public class ConfigLibClient {

    protected static void registerEventListeners(){
        NeoForge.EVENT_BUS.addListener((Consumer<ClientPlayerNetworkEvent.LoggingIn>)e -> ConfigLib.onLoadGame());
        NeoForge.EVENT_BUS.addListener((Consumer<ClientPlayerNetworkEvent.LoggingOut>)e -> ConfigLib.onLeaveGame());
    }
}
