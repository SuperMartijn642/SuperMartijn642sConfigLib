package com.supermartijn642.configlib.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.supermartijn642.configlib.BaseConfigEntry;

import java.nio.ByteBuffer;

/**
 * Created 24/03/2022 by SuperMartijn642
 */
public class JsonLongConfigEntry extends BaseConfigEntry<Long,JsonElement> {

    private final long minimum, maximum;

    public JsonLongConfigEntry(Long defaultValue, long minimum, long maximum, boolean shouldBeSynced, boolean requiresGameRestart, boolean isClientOnly, boolean isServerOnly, String comment){
        super(defaultValue, shouldBeSynced, requiresGameRestart, isClientOnly, isServerOnly, comment);
        this.minimum = minimum;
        this.maximum = maximum;
    }

    @Override
    public String getAllowedValuesHint(){
        return "Allowed range: " + this.minimum + " ~ " + this.maximum + " - Default: " + this.defaultValue;
    }

    @Override
    public boolean validateValue(Long value){
        return value >= this.minimum && value <= this.maximum;
    }

    @Override
    public JsonElement serialize(Long value){
        return new JsonPrimitive(value);
    }

    @Override
    public Long deserialize(JsonElement serialized){
        return serialized.isJsonPrimitive() && ((JsonPrimitive)serialized).isNumber() ? serialized.getAsLong() : null;
    }

    @Override
    public byte[] write(Long value){
        return ByteBuffer.allocate(8).putLong(value).array();
    }

    @Override
    public Long read(ByteBuffer buffer){
        return buffer.getLong();
    }
}
