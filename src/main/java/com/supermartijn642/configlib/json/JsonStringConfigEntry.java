package com.supermartijn642.configlib.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.supermartijn642.configlib.BaseConfigEntry;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Created 25/03/2022 by SuperMartijn642
 */
public class JsonStringConfigEntry extends BaseConfigEntry<String,JsonElement> {

    private final int minLength, maxLength;

    public JsonStringConfigEntry(String defaultValue, int minLength, int maxLength, boolean shouldBeSynced, boolean requiresGameRestart, boolean isClientOnly, boolean isServerOnly, String comment){
        super(defaultValue, shouldBeSynced, requiresGameRestart, isClientOnly, isServerOnly, comment);
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    @Override
    public String getAllowedValuesHint(){
        return "Allowed length: " + this.minLength + " ~ " + this.maxLength + " - Default: '" + this.defaultValue + "'";
    }

    @Override
    public boolean validateValue(String value){
        return value.length() >= this.minLength && value.length() <= this.maxLength;
    }

    @Override
    public JsonElement serialize(String value){
        return new JsonPrimitive(value);
    }

    @Override
    public String deserialize(JsonElement serialized){
        return serialized.isJsonPrimitive() && ((JsonPrimitive)serialized).isString() ? serialized.getAsString() : null;
    }

    @Override
    public byte[] write(String value){
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        return ByteBuffer.allocate(bytes.length + 4).putInt(bytes.length).put(bytes).array();
    }

    @Override
    public String read(ByteBuffer buffer){
        int length = buffer.getInt();
        if(length > this.maxLength)
            return null;
        byte[] bytes = new byte[length];
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
