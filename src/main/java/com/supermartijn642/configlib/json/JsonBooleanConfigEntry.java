package com.supermartijn642.configlib.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.supermartijn642.configlib.BaseConfigEntry;

import java.nio.ByteBuffer;

/**
 * Created 24/03/2022 by SuperMartijn642
 */
public class JsonBooleanConfigEntry extends BaseConfigEntry<Boolean,JsonElement> {

    public JsonBooleanConfigEntry(Boolean defaultValue, boolean shouldBeSynced, boolean requiresGameRestart, boolean isClientOnly, boolean isServerOnly, String comment){
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
    public JsonElement serialize(Boolean value){
        return new JsonPrimitive(value);
    }

    @Override
    public Boolean deserialize(JsonElement serialized){
        return serialized.isJsonPrimitive() && ((JsonPrimitive)serialized).isBoolean() ? serialized.getAsBoolean() : null;
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
