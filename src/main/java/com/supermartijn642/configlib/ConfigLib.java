package com.supermartijn642.configlib;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.DimensionType;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import java.util.*;

/**
 * Created 7/7/2020 by SuperMartijn642
 */
@Mod(modid = ConfigLib.MODID, name = ConfigLib.NAME, version = ConfigLib.VERSION)
public class ConfigLib {

    public static final String MODID = "supermartijn642configlib";
    public static final String NAME = "SuperMartijn642's Config Library";
    public static final String VERSION = "1.0.9a";

    private static final List<ModConfig> CONFIGS = new ArrayList<>();
    private static final Map<ModConfig.Type,List<ModConfig>> CONFIGS_PER_TYPE = new EnumMap<>(ModConfig.Type.class);

    static{
        for(ModConfig.Type type : ModConfig.Type.values())
            CONFIGS_PER_TYPE.put(type, new ArrayList<>());
    }

    private static final Map<String,Map<ModConfig.Type,ModConfig>> CONFIGS_PER_MOD = new HashMap<>();
    private static final List<ModConfig> SYNCABLE_CONFIGS = new ArrayList<>();

    public static SimpleNetworkWrapper channel;

    public ConfigLib(){
        channel = NetworkRegistry.INSTANCE.newSimpleChannel("supermartijnconfig");
        channel.registerMessage(ConfigSyncPacket.class, ConfigSyncPacket.class, 0, Side.CLIENT);
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
            if(e.getWorld().isRemote || e.getWorld().provider.getDimensionType() == DimensionType.OVERWORLD)
                return;

            for(ModConfig config : CONFIGS)
                config.updateValues();
        }

        @SubscribeEvent
        public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent e){
            if(!e.player.world.isRemote){
                for(ModConfig config : SYNCABLE_CONFIGS)
                    channel.sendTo(new ConfigSyncPacket(config), (EntityPlayerMP)e.player);
            }
        }

        @SubscribeEvent
        public static void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent e){
            clearSyncedValues();
        }
    }
}
