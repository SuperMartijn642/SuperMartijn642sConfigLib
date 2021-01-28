package com.supermartijn642.configlib;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created 1/19/2021 by SuperMartijn642
 */
public class ConfigSyncPacket implements IMessage, IMessageHandler<ConfigSyncPacket,IMessage> {

    private String modid;
    private ModConfig.Type type;
    private final Map<String,Object> values = new HashMap<>();

    protected ConfigSyncPacket(ModConfig config){
        this.modid = config.getModid();
        this.type = config.getType();
        this.values.putAll(config.getValuesToSync());
    }

    public ConfigSyncPacket(){
    }

    @Override
    public void toBytes(ByteBuf buffer){
        ByteBufUtils.writeUTF8String(buffer, this.modid);
        buffer.writeInt(this.type.ordinal());
        buffer.writeInt(this.values.size());
        for(Map.Entry<String,Object> entry : this.values.entrySet()){
            ByteBufUtils.writeUTF8String(buffer, entry.getKey());
            Object object = entry.getValue();
            if(object instanceof Boolean){
                buffer.writeByte(1);
                buffer.writeBoolean((boolean)object);
            }else if(object instanceof Integer){
                buffer.writeByte(2);
                buffer.writeInt((int)object);
            }else if(object instanceof Double){
                buffer.writeByte(3);
                buffer.writeDouble((double)object);
            }else if(object instanceof Float){
                buffer.writeByte(4);
                buffer.writeFloat((float)object);
            }else if(object instanceof Enum<?>){ // TODO fix this
                byte[] bytes = null;
                try{
                    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                    ObjectOutputStream stream = new ObjectOutputStream(byteStream);
                    stream.writeObject(object);
                    stream.flush();
                    stream.close();
                    bytes = byteStream.toByteArray();
                }catch(Exception e){
                    System.err.println("Failed to write enum: " + object.getClass() + "#" + object);
                    e.printStackTrace();
                    buffer.writeInt(0);
                }
                if(bytes != null){
                    buffer.writeByte(5);
                    buffer.writeInt(bytes.length);
                    buffer.writeBytes(bytes);
                }
            }else{
                System.err.println("Don't know how to write object: " + object.getClass());
                buffer.writeByte(0);
            }
        }
    }

    @Override
    public void fromBytes(ByteBuf buffer){
        this.modid = ByteBufUtils.readUTF8String(buffer);
        int typeIndex = buffer.readInt();
        if(typeIndex < 0 || typeIndex >= ModConfig.Type.values().length){
            this.type = ModConfig.Type.COMMON;
            System.err.println("Received invalid ordinal '" + typeIndex + "' for enum " + ModConfig.Type.class);
            return;
        }
        this.type = ModConfig.Type.values()[typeIndex];
        int size = buffer.readInt();
        for(int i = 0; i < size; i++){
            String path = ByteBufUtils.readUTF8String(buffer);
            Object object;
            int objectType = buffer.readByte();
            switch(objectType){
                case 1:
                    object = buffer.readBoolean();
                    break;
                case 2:
                    object = buffer.readInt();
                    break;
                case 3:
                    object = buffer.readDouble();
                    break;
                case 4:
                    object = buffer.readFloat();
                    break;
                case 5:
                    byte[] bytes = new byte[buffer.readInt()];
                    try{
                        ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(bytes));
                        object = (Enum<?>)stream.readObject();
                    }catch(Exception e){
                        System.err.println("Failed to decode enum value:");
                        e.printStackTrace();
                        object = null;
                    }
                    break;
                case 0:
                default:
                    object = null;
                    break;
            }

            if(object != null)
                this.values.put(path, object);
        }
    }

    @Override
    public IMessage onMessage(ConfigSyncPacket message, MessageContext ctx){
        EntityPlayer player = ctx.side == Side.SERVER ? ctx.getServerHandler().player : ClientProxy.getPlayer();
        if(player != null && player.world != null){
            if(ctx.side == Side.SERVER)
                player.getServer().addScheduledTask(() -> handle(message, player, player.world));
            else
                ClientProxy.queTask(() -> handle(message, player, player.world));
        }
        return null;
    }

    protected void handle(ConfigSyncPacket message, EntityPlayer player, World world){
        ModConfig config = ConfigLib.getConfig(message.modid, message.type);
        if(config == null)
            System.out.println("Failed to find config: " + message.modid + "-" + message.type.name().toLowerCase(Locale.ROOT));
        else{
            for(Map.Entry<String,Object> entry : message.values.entrySet())
                config.setSyncValue(entry.getKey(), entry.getValue());
        }
    }
}
