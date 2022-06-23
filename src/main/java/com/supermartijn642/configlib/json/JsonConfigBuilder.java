package com.supermartijn642.configlib.json;

import com.google.gson.JsonElement;
import com.supermartijn642.configlib.BaseConfigBuilder;
import com.supermartijn642.configlib.ConfigFile;

import java.io.File;
import java.util.function.Supplier;

/**
 * Created 24/03/2022 by SuperMartijn642
 */
public class JsonConfigBuilder extends BaseConfigBuilder<JsonElement> {

    public JsonConfigBuilder(String modid, String name, boolean createSubDirectory){
        super(modid, name, ".json", createSubDirectory);
    }

    @Override
    protected ConfigFile<JsonElement> createConfigFile(File file){
        return new JsonConfigFile(file);
    }

    @Override
    protected String[] getIllegalCharacters(){
        return new String[0];
    }

    @Override
    public Supplier<Boolean> define(String key, boolean defaultValue){
        if(key == null)
            throw new IllegalArgumentException("Key must not be null!");
        if(key.isEmpty())
            throw new IllegalArgumentException("Key must not be empty!");
        for(String characters : this.getIllegalCharacters())
            if(key.contains(characters))
                throw new IllegalArgumentException("Key must not contain character '" + characters + "'!");

        JsonBooleanConfigEntry entry = new JsonBooleanConfigEntry(defaultValue, this.shouldBeSynced, this.requiresGameRestart, this.isClientOnly, this.isServerOnly, this.comment);
        this.resetState();
        return this.addEntry(this.getPath(key), entry);
    }

    @Override
    public Supplier<Integer> define(String key, int defaultValue, int minValue, int maxValue){
        if(key == null)
            throw new IllegalArgumentException("Key must not be null!");
        if(key.isEmpty())
            throw new IllegalArgumentException("Key must not be empty!");
        for(String characters : this.getIllegalCharacters())
            if(key.contains(characters))
                throw new IllegalArgumentException("Key must not contain character '" + characters + "'!");
        if(defaultValue < minValue || defaultValue > maxValue)
            throw new IllegalArgumentException("Default value must be between the minimum and maximum values!");

        JsonIntegerConfigEntry entry = new JsonIntegerConfigEntry(defaultValue, minValue, maxValue, this.shouldBeSynced, this.requiresGameRestart, this.isClientOnly, this.isServerOnly, this.comment);
        this.resetState();
        return this.addEntry(this.getPath(key), entry);
    }

    @Override
    public Supplier<Long> define(String key, long defaultValue, long minValue, long maxValue){
        if(key == null)
            throw new IllegalArgumentException("Key must not be null!");
        if(key.isEmpty())
            throw new IllegalArgumentException("Key must not be empty!");
        for(String characters : this.getIllegalCharacters())
            if(key.contains(characters))
                throw new IllegalArgumentException("Key must not contain character '" + characters + "'!");
        if(defaultValue < minValue || defaultValue > maxValue)
            throw new IllegalArgumentException("Default value must be between the minimum and maximum values!");

        JsonLongConfigEntry entry = new JsonLongConfigEntry(defaultValue, minValue, maxValue, this.shouldBeSynced, this.requiresGameRestart, this.isClientOnly, this.isServerOnly, this.comment);
        this.resetState();
        return this.addEntry(this.getPath(key), entry);
    }

    @Override
    public Supplier<Double> define(String key, double defaultValue, double minValue, double maxValue){
        if(key == null)
            throw new IllegalArgumentException("Key must not be null!");
        if(key.isEmpty())
            throw new IllegalArgumentException("Key must not be empty!");
        for(String characters : this.getIllegalCharacters())
            if(key.contains(characters))
                throw new IllegalArgumentException("Key must not contain character '" + characters + "'!");
        if(defaultValue < minValue || defaultValue > maxValue)
            throw new IllegalArgumentException("Default value must be between the minimum and maximum values!");

        JsonDoubleConfigEntry entry = new JsonDoubleConfigEntry(defaultValue, minValue, maxValue, this.shouldBeSynced, this.requiresGameRestart, this.isClientOnly, this.isServerOnly, this.comment);
        this.resetState();
        return this.addEntry(this.getPath(key), entry);
    }

    @Override
    public <T extends Enum<T>> Supplier<T> define(String key, T defaultValue){
        if(key == null)
            throw new IllegalArgumentException("Key must not be null!");
        if(key.isEmpty())
            throw new IllegalArgumentException("Key must not be empty!");
        for(String characters : this.getIllegalCharacters())
            if(key.contains(characters))
                throw new IllegalArgumentException("Key must not contain character '" + characters + "'!");
        if(defaultValue == null)
            throw new IllegalArgumentException("Default value must not be null!");

        JsonEnumConfigEntry<T> entry = new JsonEnumConfigEntry<>(defaultValue, this.shouldBeSynced, this.requiresGameRestart, this.isClientOnly, this.isServerOnly, this.comment);
        this.resetState();
        return this.addEntry(this.getPath(key), entry);
    }

    @Override
    public Supplier<String> define(String key, String defaultValue, int minLength, int maxLength){
        if(key == null)
            throw new IllegalArgumentException("Key must not be null!");
        if(key.isEmpty())
            throw new IllegalArgumentException("Key must not be empty!");
        for(String characters : this.getIllegalCharacters())
            if(key.contains(characters))
                throw new IllegalArgumentException("Key must not contain character '" + characters + "'!");
        if(defaultValue == null)
            throw new IllegalArgumentException("Default value must not be null!");
        if(defaultValue.length() < minLength || defaultValue.length() > maxLength)
            throw new IllegalArgumentException("Default value's length must be between the minimum and maximum length!");

        JsonStringConfigEntry entry = new JsonStringConfigEntry(defaultValue, minLength, maxLength, this.shouldBeSynced, this.requiresGameRestart, this.isClientOnly, this.isServerOnly, this.comment);
        this.resetState();
        return this.addEntry(this.getPath(key), entry);
    }
}
