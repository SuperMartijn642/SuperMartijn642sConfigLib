package com.supermartijn642.configlib;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * Created 08/04/2022 by SuperMartijn642
 */
public class ConfigLibClient {

    protected static void registerEventListeners(){
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> ConfigLib.onLoadGame());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ConfigLib.onLeaveGame());
        ClientPlayNetworking.registerGlobalReceiver(ConfigLib.CHANNEL_ID, (client, handler, buffer, responseSender) -> ConfigLib.handleSyncConfigPacket(buffer));
    }
}
