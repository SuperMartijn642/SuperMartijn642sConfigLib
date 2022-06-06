package com.supermartijn642.configlib;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Created 1/19/2021 by SuperMartijn642
 */
public class ConfigSyncPacket implements IMessage, IMessageHandler<ConfigSyncPacket,IMessage> {

    public final ModConfig<?> config;

    protected ConfigSyncPacket(ModConfig<?> config){
        this.config = config;
    }

    public ConfigSyncPacket(){
        this(null);
    }

    @Override
    public void fromBytes(ByteBuf buf){
        ConfigLib.handleSyncConfigPacket(new PacketBuffer(buf));
    }

    @Override
    public void toBytes(ByteBuf buf){
        ConfigLib.createSyncedEntriesPacket(this, new PacketBuffer(buf));
    }

    @Override
    public IMessage onMessage(ConfigSyncPacket message, MessageContext ctx){
        return null;
    }
}
