package com.supermartijn642.configlib;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

/**
 * Created 08/04/2022 by SuperMartijn642
 */
public class ConfigLibClient implements ClientModInitializer {

    @Override
    public void onInitializeClient(){
        registerEventListeners();
    }

    protected static void registerEventListeners(){
        ClientConfigurationConnectionEvents.INIT.register((handler, client) -> ConfigLib.onLoadGame());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ConfigLib.onLeaveGame());
        ClientConfigurationNetworking.registerGlobalReceiver(ConfigLib.CHANNEL_ID, (client, handler, buffer, responseSender) -> ConfigLib.handleSyncConfigPacket(buffer));
    }
}
