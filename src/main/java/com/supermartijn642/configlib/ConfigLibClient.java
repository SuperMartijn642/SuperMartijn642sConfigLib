package com.supermartijn642.configlib;

import net.minecraftforge.client.event.ClientPlayerNetworkEvent;

/**
 * Created 08/04/2022 by SuperMartijn642
 */
public class ConfigLibClient {

    protected static void registerEventListeners(){
        ClientPlayerNetworkEvent.LoggingIn.BUS.addListener(e -> ConfigLib.onLoadGame());
        ClientPlayerNetworkEvent.LoggingOut.BUS.addListener(e -> ConfigLib.onLeaveGame());
    }
}
