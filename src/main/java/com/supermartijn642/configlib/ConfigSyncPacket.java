package com.supermartijn642.configlib;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.commons.io.serialization.ValidatingObjectInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created 1/19/2021 by SuperMartijn642
 */
public class ConfigSyncPacket {

    private String modid;
    private ModConfig.Type type;
    private final Map<String,Object> values = new HashMap<>();

    protected ConfigSyncPacket(ModConfig config){
        this.modid = config.getModid();
        this.type = config.getType();
        this.values.putAll(config.getValuesToSync());
    }

    protected ConfigSyncPacket(PacketBuffer buffer){
        this.decode(buffer);
    }

    protected void encode(PacketBuffer buffer){
        buffer.writeString(this.modid);
        buffer.writeEnumValue(this.type);
        buffer.writeInt(this.values.size());
        for(Map.Entry<String,Object> entry : this.values.entrySet()){
            buffer.writeString(entry.getKey());
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
            }else if(object instanceof Enum<?>){
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

    private void decode(PacketBuffer buffer){
        this.modid = buffer.readString();
        this.type = buffer.readEnumValue(ModConfig.Type.class);
        ModConfig config = ConfigLib.getConfig(this.modid, this.type);
        if(config == null){
            System.out.println("Failed to find config: " + this.modid + "-" + this.type.name().toLowerCase(Locale.ROOT));
            return;
        }
        int size = buffer.readInt();
        for(int i = 0; i < size; i++){
            String path = buffer.readString(32767);
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
                    buffer.readBytes(bytes);
                    try{
                        ValidatingObjectInputStream stream = new ValidatingObjectInputStream(new ByteArrayInputStream(bytes));
                        config.getExpectedValueTypes().forEach(stream::accept);
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

    public void handle(Supplier<NetworkEvent.Context> contextSupplier){
        contextSupplier.get().setPacketHandled(true);

        ModConfig config = ConfigLib.getConfig(this.modid, this.type);
        if(config == null)
            System.out.println("Failed to find config: " + this.modid + "-" + this.type.name().toLowerCase(Locale.ROOT));
        else{
            for(Map.Entry<String,Object> entry : this.values.entrySet())
                config.setSyncValue(entry.getKey(), entry.getValue());
        }
    }
}
