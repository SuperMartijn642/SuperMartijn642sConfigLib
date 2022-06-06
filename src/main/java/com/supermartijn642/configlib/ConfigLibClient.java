package com.supermartijn642.configlib;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

/**
 * Created 08/04/2022 by SuperMartijn642
 */
public class ConfigLibClient {

    protected static void registerEventListeners(){
        MinecraftForge.EVENT_BUS.register(ConfigLibClient.class);
    }

    @SubscribeEvent
    public static void onLoggedIn(FMLNetworkEvent.ClientConnectedToServerEvent e){
        ConfigLib.onLoadGame();
    }

    @SubscribeEvent
    public static void onLoggedOut(FMLNetworkEvent.ClientDisconnectionFromServerEvent e){
        ConfigLib.onLeaveGame();
    }
}
