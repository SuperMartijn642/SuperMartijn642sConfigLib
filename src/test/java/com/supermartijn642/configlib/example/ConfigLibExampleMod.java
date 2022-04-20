package com.supermartijn642.configlib.example;

import com.google.common.reflect.Reflection;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.world.InteractionResult;

/**
 * Created 1/21/2021 by SuperMartijn642
 */
public class ConfigLibExampleMod {

    public ConfigLibExampleMod(){
        Reflection.initialize(ExampleModConfig.class);
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            System.out.println("ENUM VALUE: " + ExampleModConfig.enumValue.get());
            return InteractionResult.PASS;
        });
    }
}
