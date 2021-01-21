package com.supermartijn642.configlib;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Created 1/19/2021 by SuperMartijn642
 */
public class ClientProxy {

    public static EntityPlayer getPlayer(){
        return Minecraft.getMinecraft().player;
    }

    public static void queTask(Runnable task){
        Minecraft.getMinecraft().addScheduledTask(task);
    }
}
