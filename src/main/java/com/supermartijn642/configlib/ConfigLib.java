package com.supermartijn642.configlib;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ConfigurationTask;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

/**
 * Created 7/7/2020 by SuperMartijn642
 */
@Mod("supermartijn642configlib")
public class ConfigLib {

    public static final Logger LOGGER = LoggerFactory.getLogger("configlib");

    private static final List<ModConfig<?>> CONFIGS = new ArrayList<>();
    private static final Set<String> CONFIG_NAMES = new HashSet<>();
    private static final List<ModConfig<?>> SYNCABLE_CONFIGS = new ArrayList<>();
    private static final Map<String,ModConfig<?>> SYNCABLE_CONFIGS_BY_IDENTIFIER = new HashMap<>();

    public ConfigLib(IEventBus eventBus){
        // Register event listeners
        NeoForge.EVENT_BUS.addListener((Consumer<ServerAboutToStartEvent>)e -> onLoadGame());
        if(isClientEnvironment())
            ConfigLibClient.registerEventListeners();

        eventBus.addListener((Consumer<RegisterPayloadHandlersEvent>)e ->
            e.registrar("supermartijn642configlib")
                .versioned(getModVersion())
                .optional()
                .configurationToClient(
                    ConfigSyncPacket.TYPE,
                    ConfigSyncPacket.CODEC,
                    (p, c) -> {}
                )
        );
        ConfigurationTask.Type type = new ConfigurationTask.Type(ResourceLocation.fromNamespaceAndPath("supermartijn642configlib", "sync_configs"));
        eventBus.addListener((Consumer<RegisterConfigurationTasksEvent>)e -> {
            ServerConfigurationPacketListener listener = e.getListener();
            if(listener.hasChannel(ConfigSyncPacket.TYPE)){
                e.register(new ICustomConfigurationTask() {
                    @Override
                    public void run(Consumer<CustomPacketPayload> sender){
                        sendSyncConfigPackets(sender);
                        listener.finishCurrentTask(type);
                    }

                    @Override
                    public Type type(){
                        return type;
                    }
                });
            }
        });
    }

    public static boolean isClientEnvironment(){
        return FMLEnvironment.getDist().isClient();
    }

    public static boolean isServerEnvironment(){
        return FMLEnvironment.getDist().isDedicatedServer();
    }

    public static String getModVersion(){
        return ModList.get().getModContainerById("supermartijn642configlib").orElseThrow().getModInfo().getVersion().toString();
    }

    public static File getConfigFolder(){
        return FMLPaths.CONFIGDIR.get().toFile();
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

    private static void sendSyncConfigPackets(Consumer<CustomPacketPayload> consumer){
        for(ModConfig<?> config : SYNCABLE_CONFIGS)
            consumer.accept(new ConfigSyncPacket(config));
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
