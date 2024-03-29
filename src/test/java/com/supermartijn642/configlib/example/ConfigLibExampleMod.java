package com.supermartijn642.configlib.example;

import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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
