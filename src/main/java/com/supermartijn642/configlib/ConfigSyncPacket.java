package com.supermartijn642.configlib;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Created 1/19/2021 by SuperMartijn642
 */
public class ConfigSyncPacket implements CustomPacketPayload {

    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("supermartijn642configlib", "sync_packet");
    public static final CustomPacketPayload.Type<ConfigSyncPacket> TYPE = new Type<>(IDENTIFIER);
    public static final StreamCodec<FriendlyByteBuf,ConfigSyncPacket> CODEC = StreamCodec.of(ConfigLib::writeSyncedEntriesPacket, ConfigLib::handleSyncConfigPacket);
    public final ModConfig<?> config;

    protected ConfigSyncPacket(ModConfig<?> config){
        this.config = config;
    }

    public ConfigSyncPacket(){
        this(null);
    }

    @Override
    public Type<? extends CustomPacketPayload> type(){
        return TYPE;
    }
}
