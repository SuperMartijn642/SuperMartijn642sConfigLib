package com.supermartijn642.configlib;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Created 1/19/2021 by SuperMartijn642
 */
public class ClientProxy {

    public static PlayerEntity getPlayer(){
        return Minecraft.getInstance().player;
    }
}
