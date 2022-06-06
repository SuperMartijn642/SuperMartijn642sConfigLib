package com.supermartijn642.configlib;

/**
 * Created 25/03/2022 by SuperMartijn642
 */
public abstract class BaseConfigEntry<T, S> implements ConfigEntry<T,S> {

    protected final T defaultValue;
    protected final boolean shouldBeSynced;
    protected final boolean requiresGameRestart;
    protected final boolean isClientOnly, isServerOnly;
    protected final String comment;

    public BaseConfigEntry(T defaultValue, boolean shouldBeSynced, boolean requiresGameRestart, boolean isClientOnly, boolean isServerOnly, String comment){
        this.defaultValue = defaultValue;
        this.shouldBeSynced = shouldBeSynced;
        this.requiresGameRestart = requiresGameRestart;
        this.isClientOnly = isClientOnly;
        this.isServerOnly = isServerOnly;
        this.comment = comment;
    }

    @Override
    public T defaultValue(){
        return this.defaultValue;
    }

    @Override
    public boolean shouldBeSynced(){
        return this.shouldBeSynced;
    }

    @Override
    public boolean requiresGameRestart(){
        return this.requiresGameRestart;
    }

    @Override
    public boolean isClientOnly(){
        return this.isClientOnly;
    }

    @Override
    public boolean isServerOnly(){
        return this.isServerOnly;
    }

    @Override
    public String getComment(){
        return this.comment;
    }
}
