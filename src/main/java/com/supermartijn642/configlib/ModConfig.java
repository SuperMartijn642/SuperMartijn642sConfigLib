package com.supermartijn642.configlib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created 11/30/2020 by SuperMartijn642
 */
public class ModConfig {

    public enum Type {
        CLIENT(net.minecraftforge.fml.config.ModConfig.Type.CLIENT),
        SERVER(net.minecraftforge.fml.config.ModConfig.Type.SERVER),
        COMMON(net.minecraftforge.fml.config.ModConfig.Type.COMMON);

        public final net.minecraftforge.fml.config.ModConfig.Type forgeType;

        Type(net.minecraftforge.fml.config.ModConfig.Type type){
            this.forgeType = type;
        }

        public static Type fromForge(net.minecraftforge.fml.config.ModConfig.Type type){
            for(Type type1 : values())
                if(type1.forgeType == type)
                    return type1;
            return null;
        }
    }

    private final Object threadLock = new Object();

    private final String modid;
    private final Type type;
    private final Map<String,ModConfigValue<?>> valuesByPath = new HashMap<>();
    private final List<ModConfigValue<?>> values;
    private final List<ModConfigValue<?>> updatableValues = new ArrayList<>();
    private final List<ModConfigValue<?>> syncableValues = new ArrayList<>();

    private final Map<String,Object> valuesToSync = new HashMap<>();

    protected ModConfig(String modid, Type type, List<ModConfigValue<?>> values){
        this.modid = modid;
        this.type = type;
        this.values = values;
        for(ModConfigValue<?> value : values){
            if(!value.isGameRestartRequired())
                this.updatableValues.add(value);
            if(value.shouldBeSynced())
                this.syncableValues.add(value);
            this.valuesByPath.put(value.getPath(), value);
        }
    }

    protected String getModid(){
        return this.modid;
    }

    protected Type getType(){
        return this.type;
    }

    protected void updateValues(){
        synchronized(this.threadLock){
            for(ModConfigValue<?> value : this.updatableValues)
                value.updateValue();

            valuesToSync.clear();
            for(ModConfigValue<?> value : this.syncableValues)
                valuesToSync.put(value.getPath(), value.get());
        }
    }

    protected Map<String,Object> getValuesToSync(){
        return this.valuesToSync;
    }

    protected void setSyncValue(String path, Object value){
        synchronized(this.threadLock){
            ModConfigValue<?> configValue = this.valuesByPath.get(path);
            if(configValue != null)
                configValue.setSyncedValue(value);
        }
    }

    protected void clearSyncedValues(){
        synchronized(this.threadLock){
            for(ModConfigValue<?> value : this.syncableValues)
                value.clearSyncedValue();
        }
    }

    @Override
    public String toString(){
        return "ModConfig{" +
            "modid='" + modid + '\'' +
            ", type=" + type +
            '}';
    }
}
