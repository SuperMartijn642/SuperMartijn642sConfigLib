package com.supermartijn642.configlib;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;

/**
 * Created 7/7/2020 by SuperMartijn642
 */
@Mod(modid = ConfigLib.MODID, name = ConfigLib.NAME, version = ConfigLib.VERSION)
public class ConfigLib {

    public static final String MODID = "supermartijn642configlib";
    public static final String NAME = "SuperMartijn642's Config Library";
    public static final String VERSION = "1.0.9a";

    public static final Logger LOGGER = LogManager.getLogger("configlib");

    protected static final ResourceLocation CHANNEL_ID = new ResourceLocation("supermartijn642configlib", "sync_configs");
    private static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("sm642:" + CHANNEL_ID.getResourcePath());

    private static final List<ModConfig<?>> CONFIGS = new ArrayList<>();
    private static final Set<String> CONFIG_NAMES = new HashSet<>();
    private static final List<ModConfig<?>> SYNCABLE_CONFIGS = new ArrayList<>();
    private static final Map<String,ModConfig<?>> SYNCABLE_CONFIGS_BY_IDENTIFIER = new HashMap<>();

    public ConfigLib(){
        MinecraftForge.EVENT_BUS.register(new Object() {
            @SubscribeEvent
            public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent e){
                if(e.player instanceof EntityPlayerMP)
                    onPlayerJoinServer((EntityPlayerMP)e.player);
            }
        });
        if(isClientEnvironment())
            ConfigLibClient.registerEventListeners();

        CHANNEL.registerMessage(ConfigSyncPacket.class, ConfigSyncPacket.class, 0, Side.CLIENT);
    }

    @Mod.EventHandler
    public static void onServerAboutToStart(FMLServerAboutToStartEvent e){
        onLoadGame();
    }

    public static boolean isClientEnvironment(){
        return FMLCommonHandler.instance().getSide() == Side.CLIENT;
    }

    public static boolean isServerEnvironment(){
        return FMLCommonHandler.instance().getSide() == Side.SERVER;
    }

    public static File getConfigFolder(){
        return Loader.instance().getConfigDir();
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

    protected static void onPlayerJoinServer(EntityPlayerMP sender){
        sendSyncConfigPackets(sender);
    }

    private static void sendSyncConfigPackets(EntityPlayerMP sender){
        for(ModConfig<?> config : SYNCABLE_CONFIGS){
            CHANNEL.sendTo(new ConfigSyncPacket(config), sender);
        }
    }

    protected static void createSyncedEntriesPacket(ConfigSyncPacket packet, PacketBuffer buffer){
        ModConfig<?> config = packet.config;
        buffer.writeString(config.getIdentifier());
        try{
            config.writeSyncableEntries(buffer);
        }catch(Exception e){
            throw new RuntimeException("Failed to write syncable config entries for config '" + config.getIdentifier() + "' from mod '" + config.getModid() + "'!", e);
        }
    }

    protected static ConfigSyncPacket handleSyncConfigPacket(PacketBuffer buffer){
        String identifier = buffer.readString(1024);
        ModConfig<?> config = SYNCABLE_CONFIGS_BY_IDENTIFIER.get(identifier);
        if(config == null){
            LOGGER.error("Received config sync packet for unknown config '" + identifier + "'!");
            return null;
        }

        try{
            config.readSyncableValues(buffer);
        }catch(Exception e){
            LOGGER.error("Failed to read syncable config entries for config '" + config.getIdentifier() + "' from mod '" + config.getModid() + "'!", e);
        }
        return new ConfigSyncPacket();
    }
}
