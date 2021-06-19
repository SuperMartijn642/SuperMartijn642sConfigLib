package com.supermartijn642.configlib;

import net.minecraftforge.common.config.Configuration;

import java.util.Arrays;
import java.util.Locale;

/**
 * Created 1/19/2021 by SuperMartijn642
 */
public abstract class ModConfigValue<T> {

    private final String name;
    private final String path;
    private final String comment;
    private final boolean requiresGameRestart;
    private final boolean syncWithClient;
    private final T defaultValue;

    protected Configuration config;

    private boolean initial = true;
    private T value;

    private boolean synced = false;
    private T syncedValue;

    protected ModConfigValue(String path, String comment, boolean requiresGameRestart, boolean syncWithClient, T defaultValue){
        int indexOf = path.lastIndexOf('.');
        this.name = indexOf >= 0 ? path.substring(indexOf + 1) : path;
        this.path = indexOf >= 0 ? path.substring(0, indexOf) : "";
        this.comment = comment == null ? "" : comment;
        this.requiresGameRestart = requiresGameRestart;
        this.syncWithClient = syncWithClient;
        this.defaultValue = defaultValue;
    }

    protected void build(Configuration configuration){
        this.getValue(this.name, this.path, this.defaultValue, this.comment, configuration);
        this.config = configuration;
    }

    protected abstract T getValue(String name, String path, T defaultValue, String comment, Configuration configuration);

    protected void updateValue(){
        if(this.initial || !this.requiresGameRestart){
            this.value = this.getValue(this.name, this.path, this.defaultValue, this.comment, this.config);
            this.initial = false;
        }
    }

    protected String getPath(){
        return this.path;
    }

    protected boolean isGameRestartRequired(){
        return this.requiresGameRestart;
    }

    @SuppressWarnings("unchecked")
    protected void setSyncedValue(Object value){
        try{
            this.syncedValue = (T)value;
        }catch(Exception e){
            e.printStackTrace();
        }
        this.synced = true;
    }

    protected void clearSyncedValue(){
        this.synced = false;
        this.syncedValue = null;
    }

    protected boolean shouldBeSynced(){
        return this.syncWithClient;
    }

    public T get(){
        if(this.value == null)
            this.updateValue();
        return this.synced ? this.syncedValue : this.value;
    }

    public static class BooleanValue extends ModConfigValue<Boolean> {

        protected BooleanValue(String path, String comment, boolean requiresGameRestart, boolean syncWithClient, Boolean defaultValue){
            super(path, comment, requiresGameRestart, syncWithClient, defaultValue);
        }

        @Override
        protected Boolean getValue(String name, String path, Boolean defaultValue, String comment, Configuration configuration){
            return configuration.getBoolean(name, path, defaultValue, comment);
        }
    }

    public static class IntegerValue extends ModConfigValue<Integer> {

        private final int min, max;

        protected IntegerValue(String path, String comment, boolean requiresGameRestart, boolean syncWithClient, Integer defaultValue, int minValue, int maxValue){
            super(path, comment, requiresGameRestart, syncWithClient, defaultValue);
            this.min = minValue;
            this.max = maxValue;
        }

        @Override
        protected Integer getValue(String name, String path, Integer defaultValue, String comment, Configuration configuration){
            return configuration.getInt(name, path, defaultValue, this.min, this.max, comment);
        }
    }

    public static class FloatingValue extends ModConfigValue<Double> {

        private final double min, max;

        protected FloatingValue(String path, String comment, boolean requiresGameRestart, boolean syncWithClient, Double defaultValue, double minValue, double maxValue){
            super(path, comment, requiresGameRestart, syncWithClient, defaultValue);
            this.min = minValue;
            this.max = maxValue;
        }

        @Override
        protected Double getValue(String name, String path, Double defaultValue, String comment, Configuration configuration){
            return (double)configuration.getFloat(name, path, (float)(double)defaultValue, (float)this.min, (float)this.max, comment);
        }
    }

    public static class EnumValue<T extends Enum<T>> extends ModConfigValue<T> {

        private String[] values;

        protected EnumValue(String path, String comment, boolean requiresGameRestart, boolean syncWithClient, T defaultValue){
            super(path, comment, requiresGameRestart, syncWithClient, defaultValue);
            this.values = Arrays.stream(defaultValue.getClass().getEnumConstants())
                .map(Enum::name).map(s -> s.toLowerCase(Locale.ROOT)).toArray(String[]::new);
        }

        @Override
        protected T getValue(String name, String path, T defaultValue, String comment, Configuration configuration){
            return Enum.valueOf(defaultValue.getDeclaringClass(), configuration.get(path, name, defaultValue.name().toLowerCase(Locale.ROOT), comment, this.values).getString());
        }
    }

}
