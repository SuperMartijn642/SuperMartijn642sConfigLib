package com.supermartijn642.configlib.toml;

import com.supermartijn642.configlib.BaseConfigEntry;

import java.nio.ByteBuffer;

/**
 * Created 24/03/2022 by SuperMartijn642
 */
public class TomlDoubleConfigEntry extends BaseConfigEntry<Double,TomlElement> {

    private final double minimum, maximum;

    public TomlDoubleConfigEntry(Double defaultValue, double minimum, double maximum, boolean shouldBeSynced, boolean requiresGameRestart, boolean isClientOnly, boolean isServerOnly, String comment){
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
    public TomlElement serialize(Double value){
        return TomlPrimitive.of(value);
    }

    @Override
    public Double deserialize(TomlElement serialized){
        return serialized.isInteger() ? (Double)(double)serialized.getAsInteger()
            : serialized.isLong() ? (Double)(double)serialized.getAsLong()
            : serialized.isDouble() ? (Double)serialized.getAsDouble() : null;
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
