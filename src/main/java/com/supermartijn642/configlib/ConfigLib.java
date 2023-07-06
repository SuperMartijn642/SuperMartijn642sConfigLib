package com.supermartijn642.configlib;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.PacketDistributor;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;
import net.minecraftforge.fmlserverevents.FMLServerAboutToStartEvent;
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

    protected static final ResourceLocation CHANNEL_ID = new ResourceLocation("supermartijn642configlib", "sync_configs");
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(CHANNEL_ID, ConfigLib::getModVersion, ConfigLib::canConnectWith, ConfigLib::canConnectWith);

    private static final List<ModConfig<?>> CONFIGS = new ArrayList<>();
    private static final Set<String> CONFIG_NAMES = new HashSet<>();
    private static final List<ModConfig<?>> SYNCABLE_CONFIGS = new ArrayList<>();
    private static final Map<String,ModConfig<?>> SYNCABLE_CONFIGS_BY_IDENTIFIER = new HashMap<>();

    public ConfigLib(){
        // Allow connection if there are no syncable configs or if the server has the same mod version
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(ConfigLib::getModVersion, (remoteVersion, isFromServer) -> canConnectWith(remoteVersion)));

        // Register event listeners
        MinecraftForge.EVENT_BUS.addListener((Consumer<FMLServerAboutToStartEvent>)e -> onLoadGame());
        MinecraftForge.EVENT_BUS.addListener((Consumer<PlayerEvent.PlayerLoggedInEvent>)e -> {
            if(e.getPlayer() instanceof ServerPlayer)
                onPlayerJoinServer((ServerPlayer)e.getPlayer());
        });
        if(isClientEnvironment())
            ConfigLibClient.registerEventListeners();

        CHANNEL.registerMessage(0, ConfigSyncPacket.class, ConfigLib::createSyncedEntriesPacket, buffer -> handleSyncConfigPacket(buffer), (packet, context) -> context.get().setPacketHandled(true));
    }

    public static boolean isClientEnvironment(){
        return FMLEnvironment.dist == Dist.CLIENT;
    }

    public static boolean isServerEnvironment(){
        return FMLEnvironment.dist == Dist.DEDICATED_SERVER;
    }

    public static String getModVersion(){
        return ModList.get().getModContainerById("supermartijn642configlib").orElseThrow().getModInfo().getVersion().toString();
    }

    public static boolean canConnectWith(String remoteVersion){
        return SYNCABLE_CONFIGS.isEmpty() || getModVersion().equals(remoteVersion);
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

    protected static void onPlayerJoinServer(ServerPlayer sender){
        sendSyncConfigPackets(sender);
    }

    private static void sendSyncConfigPackets(ServerPlayer sender){
        for(ModConfig<?> config : SYNCABLE_CONFIGS){
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> sender), new ConfigSyncPacket(config));
        }
    }

    private static void createSyncedEntriesPacket(ConfigSyncPacket packet, FriendlyByteBuf buffer){
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
