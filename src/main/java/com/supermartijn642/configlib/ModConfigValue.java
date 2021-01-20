package com.supermartijn642.configlib;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Created 1/19/2021 by SuperMartijn642
 */
public abstract class ModConfigValue<T> {

    private final String path;
    private final String comment;
    private final boolean requiresGameRestart;
    private final boolean syncWithClient;
    private final T defaultValue;

    protected ForgeConfigSpec.ConfigValue<T> config;

    private T value;

    private boolean synced = false;
    private T syncedValue;

    protected ModConfigValue(String path, String comment, boolean requiresGameRestart, boolean syncWithClient, T defaultValue){
        this.path = path;
        this.comment = comment;
        this.requiresGameRestart = requiresGameRestart;
        this.syncWithClient = syncWithClient;
        this.defaultValue = defaultValue;
    }

    protected void build(ForgeConfigSpec.Builder builder){
        builder.worldRestart();

        if(this.comment != null && !this.comment.isEmpty())
            builder.comment(this.comment);

        this.config = this.build(this.path, this.defaultValue, builder);
    }

    protected abstract ForgeConfigSpec.ConfigValue<T> build(String path, T defaultValue, ForgeConfigSpec.Builder builder);

    protected void updateValue(boolean initialUpdate){
        if(initialUpdate || !this.requiresGameRestart)
            this.value = config.get();
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
        return this.synced ? this.syncedValue : this.value;
    }

    public static class BooleanValue extends ModConfigValue<Boolean> {
        protected BooleanValue(String path, String comment, boolean requiresGameRestart, boolean syncWithClient, Boolean defaultValue){
            super(path, comment, requiresGameRestart, syncWithClient, defaultValue);
        }

        @Override
        protected ForgeConfigSpec.ConfigValue<Boolean> build(String path, Boolean defaultValue, ForgeConfigSpec.Builder builder){
            return builder.define(path, defaultValue);
        }
    }

    public static class IntegerValue extends ModConfigValue<Integer> {
        private int min, max;

        protected IntegerValue(String path, String comment, boolean requiresGameRestart, boolean syncWithClient, Integer defaultValue, int minValue, int maxValue){
            super(path, comment, requiresGameRestart, syncWithClient, defaultValue);
            this.min = minValue;
            this.max = maxValue;
        }

        @Override
        protected ForgeConfigSpec.ConfigValue<Integer> build(String path, Integer defaultValue, ForgeConfigSpec.Builder builder){
            return builder.defineInRange(path, defaultValue, this.min, this.max);
        }
    }

    public static class FloatingValue extends ModConfigValue<Double> {
        private double min, max;

        protected FloatingValue(String path, String comment, boolean requiresGameRestart, boolean syncWithClient, Double defaultValue, double minValue, double maxValue){
            super(path, comment, requiresGameRestart, syncWithClient, defaultValue);
            this.min = minValue;
            this.max = maxValue;
        }

        @Override
        protected ForgeConfigSpec.ConfigValue<Double> build(String path, Double defaultValue, ForgeConfigSpec.Builder builder){
            return builder.defineInRange(path, defaultValue, this.min, this.max);
        }
    }

    public static class EnumValue<T extends Enum<T>> extends ModConfigValue<T> {
        protected EnumValue(String path, String comment, boolean requiresGameRestart, boolean syncWithClient, T defaultValue){
            super(path, comment, requiresGameRestart, syncWithClient, defaultValue);
        }

        @Override
        protected ForgeConfigSpec.ConfigValue<T> build(String path, T defaultValue, ForgeConfigSpec.Builder builder){
            return builder.defineEnum(path, defaultValue);
        }
    }

}
