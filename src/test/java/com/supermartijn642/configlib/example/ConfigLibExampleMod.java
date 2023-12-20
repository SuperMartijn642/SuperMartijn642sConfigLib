package com.supermartijn642.configlib.example;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

/**
 * Created 1/21/2021 by SuperMartijn642
 */
@Mod("configlibexamplemod")
public class ConfigLibExampleMod {

    public ConfigLibExampleMod(){
        ExampleModConfig.booleanValue.get();
    }

    @Mod.EventBusSubscriber
    public static class Events {

        @SubscribeEvent
        public static void playerDrop(ItemTossEvent e){
            System.out.println("value: " + ExampleModConfig.booleanValue.get());
        }

        @SubscribeEvent
        public static void playerDrop(LivingEvent.LivingJumpEvent e){
            System.out.println("value: " + ExampleModConfig.booleanValue.get());
        }
    }
}
