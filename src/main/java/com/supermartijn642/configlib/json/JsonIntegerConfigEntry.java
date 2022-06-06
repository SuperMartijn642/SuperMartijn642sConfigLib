package com.supermartijn642.configlib.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.supermartijn642.configlib.BaseConfigEntry;

import java.nio.ByteBuffer;

/**
 * Created 24/03/2022 by SuperMartijn642
 */
public class JsonIntegerConfigEntry extends BaseConfigEntry<Integer,JsonElement> {

    private final int minimum, maximum;

    public JsonIntegerConfigEntry(Integer defaultValue, int minimum, int maximum, boolean shouldBeSynced, boolean requiresGameRestart, boolean isClientOnly, boolean isServerOnly, String comment){
        super(defaultValue, shouldBeSynced, requiresGameRestart, isClientOnly, isServerOnly, comment);
        this.minimum = minimum;
        this.maximum = maximum;
    }

    @Override
    public String getAllowedValuesHint(){
        return "Allowed range: " + this.minimum + " ~ " + this.maximum + " - Default: " + this.defaultValue;
    }

    @Override
    public boolean validateValue(Integer value){
        return value >= this.minimum && value <= this.maximum;
    }

    @Override
    public JsonElement serialize(Integer value){
        return new JsonPrimitive(value);
    }

    @Override
    public Integer deserialize(JsonElement serialized){
        return serialized.isJsonPrimitive() && ((JsonPrimitive)serialized).isNumber() ? serialized.getAsInt() : null;
    }

    @Override
    public byte[] write(Integer value){
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    @Override
    public Integer read(ByteBuffer buffer){
        return buffer.getInt();
    }
}
