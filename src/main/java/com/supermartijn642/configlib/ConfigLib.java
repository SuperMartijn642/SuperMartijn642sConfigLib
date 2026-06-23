package com.supermartijn642.configlib;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * Created 7/7/2020 by SuperMartijn642
 */
public class ConfigLib implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("configlib");

    private static final List<ModConfig<?>> CONFIGS = new ArrayList<>();
    private static final Set<String> CONFIG_NAMES = new HashSet<>();
    private static final List<ModConfig<?>> SYNCABLE_CONFIGS = new ArrayList<>();
    private static final Map<String,ModConfig<?>> SYNCABLE_CONFIGS_BY_IDENTIFIER = new HashMap<>();

    public ConfigLib(){
        ServerLifecycleEvents.SERVER_STARTING.register(server -> onLoadGame());
        ServerConfigurationConnectionEvents.CONFIGURE.register(ConfigLib::onPlayerJoinServer);
        PayloadTypeRegistry.clientboundConfiguration().register(ConfigSyncPacket.TYPE, ConfigSyncPacket.CODEC);
    }

    @Override
    public void onInitialize(){
    }

    public static boolean isClientEnvironment(){
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    public static boolean isServerEnvironment(){
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;
    }

    public static File getConfigFolder(){
        return FabricLoader.getInstance().getConfigDir().toFile();
    }

    protected static synchronized void addConfig(ModConfig<?> config){
        if(CONFIG_NAMES.contains(config.getIdentifier()))
            throw new IllegalStateException("Config '" + config.getIdentifier() + "' for mod '" + config.getModid() + "' already exists!");

        CONFIGS.add(config);
        CONFIG_NAMES.add(config.getIdentifier());
        if(config.hasSyncableEntries()){
            SYNCABLE_CONFIGS.add(config);
            SYNCABLE_CONFIGS_BY_IDENTIFIER.put(config.getIdentifier(), config);
        }

        config.initialize();
    }

    protected static void onLoadGame(){
        CONFIGS.forEach(ModConfig::onJoinGame);
    }

    protected static void onLeaveGame(){
        CONFIGS.forEach(ModConfig::onLeaveGame);
    }

    protected static void onPlayerJoinServer(ServerConfigurationPacketListenerImpl handler, MinecraftServer server){
        sendSyncConfigPackets(handler);
    }

    private static void sendSyncConfigPackets(ServerConfigurationPacketListenerImpl handler){
        for(ModConfig<?> config : SYNCABLE_CONFIGS)
            ServerConfigurationNetworking.send(handler, new ConfigSyncPacket(config));
    }

    protected static void writeSyncedEntriesPacket(FriendlyByteBuf buffer, ConfigSyncPacket packet){
        ModConfig<?> config = packet.config;
        buffer.writeUtf(config.getIdentifier());
        try{
            config.writeSyncableEntries(buffer);
        }catch(Exception e){
            throw new RuntimeException("Failed to write syncable config entries for config '" + config.getIdentifier() + "' from mod '" + config.getModid() + "'!", e);
        }
    }

    protected static ConfigSyncPacket handleSyncConfigPacket(FriendlyByteBuf buffer){
        String identifier = buffer.readUtf();
        ModConfig<?> config = SYNCABLE_CONFIGS_BY_IDENTIFIER.get(identifier);
        if(config == null)
            throw new RuntimeException("Received config sync packet for unknown config '" + identifier + "'!");

        try{
            config.readSyncableValues(buffer);
        }catch(Exception e){
            LOGGER.error("Failed to read syncable config entries for config '" + config.getIdentifier() + "' from mod '" + config.getModid() + "'!", e);
        }
        return new ConfigSyncPacket();
    }
}
