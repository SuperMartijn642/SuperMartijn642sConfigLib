package com.supermartijn642.configlib.toml;

import com.supermartijn642.configlib.BaseConfigEntry;

import java.nio.ByteBuffer;

/**
 * Created 24/03/2022 by SuperMartijn642
 */
public class TomlBooleanConfigEntry extends BaseConfigEntry<Boolean,TomlElement> {

    public TomlBooleanConfigEntry(Boolean defaultValue, boolean shouldBeSynced, boolean requiresGameRestart, boolean isClientOnly, boolean isServerOnly, String comment){
        super(defaultValue, shouldBeSynced, requiresGameRestart, isClientOnly, isServerOnly, comment);
    }

    @Override
    public String getAllowedValuesHint(){
        return "Allowed values: true, false - Default: " + this.defaultValue;
    }

    @Override
    public boolean validateValue(Boolean value){
        return true;
    }

    @Override
    public TomlElement serialize(Boolean value){
        return TomlPrimitive.of(value);
    }

    @Override
    public Boolean deserialize(TomlElement serialized){
        return serialized.isBoolean() ? serialized.getAsBoolean() : null;
    }

    @Override
    public byte[] write(Boolean value){
        return new byte[]{(byte)(value ? 1 : 0)};
    }

    @Override
    public Boolean read(ByteBuffer buffer){
        return buffer.get() == 1;
    }
}
