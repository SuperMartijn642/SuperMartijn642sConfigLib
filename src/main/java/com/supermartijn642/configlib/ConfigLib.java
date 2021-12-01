package com.supermartijn642.configlib;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.*;

/**
 * Created 7/7/2020 by SuperMartijn642
 */
@Mod("supermartijn642configlib")
public class ConfigLib {

    private static final List<ModConfig> CONFIGS = new ArrayList<>();
    private static final Map<ModConfig.Type,List<ModConfig>> CONFIGS_PER_TYPE = new EnumMap<>(ModConfig.Type.class);

    static{
        for(ModConfig.Type type : ModConfig.Type.values())
            CONFIGS_PER_TYPE.put(type, new ArrayList<>());
    }

    private static final Map<String,Map<ModConfig.Type,ModConfig>> CONFIGS_PER_MOD = new HashMap<>();
    private static final List<ModConfig> SYNCABLE_CONFIGS = new ArrayList<>();

    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation("supermartijn642configlib", "main"), () -> "1", "1"::equals, "1"::equals);

    public ConfigLib(){
        CHANNEL.registerMessage(0, ConfigSyncPacket.class, ConfigSyncPacket::encode, ConfigSyncPacket::new, ConfigSyncPacket::handle);
    }

    protected static synchronized void addConfig(ModConfig config){
        CONFIGS.add(config);

        CONFIGS_PER_MOD.putIfAbsent(config.getModid(), new EnumMap<>(ModConfig.Type.class));
        CONFIGS_PER_MOD.get(config.getModid()).put(config.getType(), config);

        CONFIGS_PER_TYPE.get(config.getType()).add(config);

        if(config.getType() == ModConfig.Type.SERVER || config.getType() == ModConfig.Type.COMMON)
            SYNCABLE_CONFIGS.add(config);
    }

    protected static synchronized ModConfig getConfig(String modid, ModConfig.Type type){
        Map<ModConfig.Type,ModConfig> configs = CONFIGS_PER_MOD.get(modid);
        if(configs != null)
            return configs.get(type);
        return null;
    }

    protected static void clearSyncedValues(){
        for(ModConfig config : SYNCABLE_CONFIGS)
            config.clearSyncedValues();
    }

    @Mod.EventBusSubscriber
    public static class ConfigEvents {

        @SubscribeEvent
        public static void onWorldLoad(WorldEvent.Load e){
            if(e.getWorld().isClientSide() || !(e.getWorld() instanceof Level) || ((Level)e.getWorld()).dimension() == Level.OVERWORLD)
                return;

            for(ModConfig config : CONFIGS)
                config.updateValues();
        }

        @SubscribeEvent
        public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent e){
            if(!e.getPlayer().level.isClientSide){
                for(ModConfig config : SYNCABLE_CONFIGS)
                    CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)e.getPlayer()), new ConfigSyncPacket(config));
            }
        }
    }
}
