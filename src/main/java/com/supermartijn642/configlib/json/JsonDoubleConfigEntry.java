package com.supermartijn642.configlib.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.supermartijn642.configlib.BaseConfigEntry;

import java.nio.ByteBuffer;

/**
 * Created 24/03/2022 by SuperMartijn642
 */
public class JsonDoubleConfigEntry extends BaseConfigEntry<Double,JsonElement> {

    private final double minimum, maximum;

    public JsonDoubleConfigEntry(Double defaultValue, double minimum, double maximum, boolean shouldBeSynced, boolean requiresGameRestart, boolean isClientOnly, boolean isServerOnly, String comment){
        super(defaultValue, shouldBeSynced, requiresGameRestart, isClientOnly, isServerOnly, comment);
        this.minimum = minimum;
        this.maximum = maximum;
    }

    @Override
    public String getAllowedValuesHint(){
        return "Allowed range: " + this.minimum + " ~ " + this.maximum + " - Default: " + this.defaultValue;
    }

    @Override
    public boolean validateValue(Double value){
        return value >= this.minimum && value <= this.maximum;
    }

    @Override
    public JsonElement serialize(Double value){
        return new JsonPrimitive(value);
    }

    @Override
    public Double deserialize(JsonElement serialized){
        return (serialized.isJsonPrimitive() && ((JsonPrimitive)serialized).isNumber()) ? serialized.getAsDouble() : null;
    }

    @Override
    public byte[] write(Double value){
        return ByteBuffer.allocate(8).putDouble(value).array();
    }

    @Override
    public Double read(ByteBuffer buffer){
        return buffer.getDouble();
    }
}
