package com.supermartijn642.configlib.toml;

import com.supermartijn642.configlib.BaseConfigEntry;

import java.nio.ByteBuffer;

/**
 * Created 24/03/2022 by SuperMartijn642
 */
public class TomlLongConfigEntry extends BaseConfigEntry<Long,TomlElement> {

    private final long minimum, maximum;

    public TomlLongConfigEntry(Long defaultValue, long minimum, long maximum, boolean shouldBeSynced, boolean requiresGameRestart, boolean isClientOnly, boolean isServerOnly, String comment){
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
    public TomlElement serialize(Long value){
        return TomlPrimitive.of(value);
    }

    @Override
    public Long deserialize(TomlElement serialized){
        return serialized.isInteger() ? serialized.getAsInteger() : serialized.isLong() ? serialized.getAsLong() : null;
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
