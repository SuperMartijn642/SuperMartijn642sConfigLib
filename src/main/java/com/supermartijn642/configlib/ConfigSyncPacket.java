package com.supermartijn642.configlib;

/**
 * Created 1/19/2021 by SuperMartijn642
 */
public class ConfigSyncPacket {

    public final ModConfig<?> config;

    protected ConfigSyncPacket(ModConfig<?> config){
        this.config = config;
    }

    public ConfigSyncPacket(){
        this(null);
    }
}
