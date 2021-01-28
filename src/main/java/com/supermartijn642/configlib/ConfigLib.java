package com.supermartijn642.configlib;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.*;

/**
 * Created 7/7/2020 by SuperMartijn642
 */
@Mod("supermartijn642configlib")
public class ConfigLib {

    private static final List<ModConfig> CONFIGS = new ArrayList<>();
    private static final Map<String,Map<ModConfig.Type,ModConfig>> CONFIGS_PER_MOD = new HashMap<>();
    private static final List<ModConfig> SYNCABLE_CONFIGS = new ArrayList<>();

    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation("supermartijn642configlib", "main"), () -> "1", "1"::equals, "1"::equals);

    public ConfigLib(){
        CHANNEL.registerMessage(0, ConfigSyncPacket.class, ConfigSyncPacket::encode, ConfigSyncPacket::new, ConfigSyncPacket::handle);
    }

    protected static void addConfig(ModConfig config){
        CONFIGS.add(config);

        CONFIGS_PER_MOD.putIfAbsent(config.getModid(), new EnumMap<>(ModConfig.Type.class));
        CONFIGS_PER_MOD.get(config.getModid()).put(config.getType(), config);

        if(config.getType() == ModConfig.Type.SERVER || config.getType() == ModConfig.Type.COMMON)
            SYNCABLE_CONFIGS.add(config);
    }

    protected static ModConfig getConfig(String modid, ModConfig.Type type){
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
            if(e.getWorld().isRemote() || !(e.getWorld() instanceof World) || ((World)e.getWorld()).getDimensionKey() == World.OVERWORLD)
                return;

            for(ModConfig config : CONFIGS)
                config.updateValues(false);
        }

        @SubscribeEvent
        public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent e){
            if(!e.getPlayer().world.isRemote){
                for(ModConfig config : SYNCABLE_CONFIGS)
                    CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)e.getPlayer()), new ConfigSyncPacket(config));
            }
        }

        @SubscribeEvent
        public static void onConfigLoad(net.minecraftforge.fml.config.ModConfig.Loading e){
            ModConfig config = CONFIGS_PER_MOD.get(e.getConfig().getModId()).get(ModConfig.Type.fromForge(e.getConfig().getType()));
            config.updateValues(true);
        }
    }
}
