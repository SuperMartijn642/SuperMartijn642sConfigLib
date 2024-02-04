package com.supermartijn642.configlib;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Created 1/19/2021 by SuperMartijn642
 */
public class ConfigSyncPacket implements CustomPacketPayload {

    public static final ResourceLocation IDENTIFIER = new ResourceLocation("supermartijn642configlib", "sync_packet");
    public final ModConfig<?> config;

    protected ConfigSyncPacket(ModConfig<?> config){
        this.config = config;
    }

    public ConfigSyncPacket(){
        this(null);
    }

    @Override
    public void write(FriendlyByteBuf buffer){
        ConfigLib.writeSyncedEntriesPacket(this, buffer);
    }

    @Override
    public ResourceLocation id(){
        return IDENTIFIER;
    }
}
