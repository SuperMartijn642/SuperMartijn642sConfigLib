package com.supermartijn642.configlib;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Consumer;

/**
 * Created 08/04/2022 by SuperMartijn642
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ConfigLibClient {

    protected static void registerEventListeners(){
        MinecraftForge.EVENT_BUS.addListener((Consumer<ClientPlayerNetworkEvent.LoggingIn>)e -> ConfigLib.onLoadGame());
        MinecraftForge.EVENT_BUS.addListener((Consumer<ClientPlayerNetworkEvent.LoggingOut>)e -> ConfigLib.onLeaveGame());
    }
}
