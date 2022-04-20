package com.supermartijn642.configlib.toml;

import com.supermartijn642.configlib.BaseConfigEntry;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Created 25/03/2022 by SuperMartijn642
 */
public class TomlEnumConfigEntry<T extends Enum<T>> extends BaseConfigEntry<T,TomlElement> {

    private final Class<T> enumClass;

    public TomlEnumConfigEntry(T defaultValue, boolean shouldBeSynced, boolean requiresGameRestart, boolean isClientOnly, boolean isServerOnly, String comment){
        super(defaultValue, shouldBeSynced, requiresGameRestart, isClientOnly, isServerOnly, comment);
        this.enumClass = defaultValue.getDeclaringClass();

        for(T value : this.enumClass.getEnumConstants())
            if(value.name().getBytes(StandardCharsets.UTF_8).length > 512)
                throw new IllegalArgumentException("Cannot use enum whose values' names are more than 512 bytes!");
    }

    @Override
    public String getAllowedValuesHint(){
        String values = Arrays.stream(this.enumClass.getEnumConstants()).map(Enum::name).collect(Collectors.joining(", "));
        return "Allowed values: " + values + " - Default: " + this.defaultValue.name();
    }

    @Override
    public boolean validateValue(T value){
        return true;
    }

    @Override
    public TomlElement serialize(T value){
        return TomlPrimitive.of(value.name());
    }

    @Override
    public T deserialize(TomlElement serialized){
        if(!serialized.isString())
            return null;

        String name = serialized.getAsString();
        T value = null;
        try{
            value = Enum.valueOf(this.enumClass, name);
        }catch(IllegalArgumentException ignore){}
        return value;
    }

    @Override
    public byte[] write(T value){
        byte[] bytes = value.name().getBytes(StandardCharsets.UTF_8);
        return ByteBuffer.allocate(bytes.length + 4).putInt(bytes.length).put(bytes).array();
    }

    @Override
    public T read(ByteBuffer buffer){
        int length = buffer.getInt();
        if(length > 512)
            return null;
        byte[] bytes = new byte[length];
        String name = new String(bytes, StandardCharsets.UTF_8);
        T value = null;
        try{
            value = Enum.valueOf(this.enumClass, name);
        }catch(IllegalArgumentException ignore){}
        return value;
    }
}
