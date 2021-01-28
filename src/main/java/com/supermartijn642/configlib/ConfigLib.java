package com.supermartijn642.configlib;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
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

    protected static void addConfig(ModConfig config){
        CONFIGS.add(config);

        CONFIGS_PER_MOD.putIfAbsent(config.getModid(), new EnumMap<>(ModConfig.Type.class));
        CONFIGS_PER_MOD.get(config.getModid()).put(config.getType(), config);

        CONFIGS_PER_TYPE.get(config.getType()).add(config);

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
                config.updateValues();
        }

        @SubscribeEvent
        public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent e){
            if(!e.getPlayer().world.isRemote){
                for(ModConfig config : SYNCABLE_CONFIGS)
                    CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)e.getPlayer()), new ConfigSyncPacket(config));
            }
        }

        @SubscribeEvent
        public static void onConfigLoadServer(FMLServerAboutToStartEvent e){
            for(ModConfig config : CONFIGS_PER_TYPE.get(ModConfig.Type.SERVER))
                config.updateValues();
        }

        @SubscribeEvent
        public static void onConfigLoadCommon(FMLCommonSetupEvent e){
            for(ModConfig config : CONFIGS_PER_TYPE.get(ModConfig.Type.COMMON))
                config.updateValues();

            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                for(ModConfig config : CONFIGS_PER_TYPE.get(ModConfig.Type.SERVER))
                    config.updateValues();
            });
        }
    }
}
