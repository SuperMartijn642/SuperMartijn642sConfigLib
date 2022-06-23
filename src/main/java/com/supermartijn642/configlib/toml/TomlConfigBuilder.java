package com.supermartijn642.configlib.toml;

import com.supermartijn642.configlib.BaseConfigBuilder;
import com.supermartijn642.configlib.ConfigFile;

import java.io.File;
import java.util.function.Supplier;

/**
 * Created 24/03/2022 by SuperMartijn642
 */
public class TomlConfigBuilder extends BaseConfigBuilder<TomlElement> {

    public TomlConfigBuilder(String modid, String name, boolean createSubDirectory){
        super(modid, name, ".toml", createSubDirectory);
    }

    @Override
    protected ConfigFile<TomlElement> createConfigFile(File file){
        return new TomlConfigFile(file);
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

        TomlBooleanConfigEntry entry = new TomlBooleanConfigEntry(defaultValue, this.shouldBeSynced, this.requiresGameRestart, this.isClientOnly, this.isServerOnly, this.comment);
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

        TomlIntegerConfigEntry entry = new TomlIntegerConfigEntry(defaultValue, minValue, maxValue, this.shouldBeSynced, this.requiresGameRestart, this.isClientOnly, this.isServerOnly, this.comment);
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

        TomlLongConfigEntry entry = new TomlLongConfigEntry(defaultValue, minValue, maxValue, this.shouldBeSynced, this.requiresGameRestart, this.isClientOnly, this.isServerOnly, this.comment);
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

        TomlDoubleConfigEntry entry = new TomlDoubleConfigEntry(defaultValue, minValue, maxValue, this.shouldBeSynced, this.requiresGameRestart, this.isClientOnly, this.isServerOnly, this.comment);
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

        TomlEnumConfigEntry<T> entry = new TomlEnumConfigEntry<>(defaultValue, this.shouldBeSynced, this.requiresGameRestart, this.isClientOnly, this.isServerOnly, this.comment);
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

        TomlStringConfigEntry entry = new TomlStringConfigEntry(defaultValue, minLength, maxLength, this.shouldBeSynced, this.requiresGameRestart, this.isClientOnly, this.isServerOnly, this.comment);
        this.resetState();
        return this.addEntry(this.getPath(key), entry);
    }
}
