package com.supermartijn642.configlib;

import com.supermartijn642.configlib.api.ConfigBuilders;
import com.supermartijn642.configlib.api.IConfigBuilder;
import net.minecraftforge.fml.ModLoadingContext;

import java.util.Locale;
import java.util.function.Supplier;

/**
 * Created 1/19/2021 by SuperMartijn642
 * @deprecated Use {@link ConfigBuilders} instead
 */
@Deprecated
public class ModConfigBuilder {

    private final IConfigBuilder configBuilder;

    private ModConfigBuilder(String modid, String name){
        this.configBuilder = ConfigBuilders.newTomlConfig(modid, name, false);
    }

    /**
     * @deprecated Use {@link ConfigBuilders#newTomlConfig(String, String, boolean)}
     */
    @Deprecated
    public ModConfigBuilder(String modid, ModConfig.Type type){
        this(modid, type.name().toLowerCase(Locale.ROOT));
    }

    /**
     * @deprecated Use {@link ConfigBuilders#newTomlConfig(String, String, boolean)}
     */
    @Deprecated
    public ModConfigBuilder(String modid){
        this(modid, ModConfig.Type.COMMON);
    }

    /**
     * @deprecated Use {@link ConfigBuilders#newTomlConfig(String, String, boolean)}
     */
    @Deprecated
    public ModConfigBuilder(ModConfig.Type type){
        this(ModLoadingContext.get().getActiveNamespace(), type);
    }

    /**
     * @deprecated Use {@link ConfigBuilders#newTomlConfig(String, String, boolean)}
     */
    @Deprecated
    public ModConfigBuilder(){
        this(ModLoadingContext.get().getActiveNamespace(), ModConfig.Type.COMMON);
    }

    /**
     * Pushes a new category
     * @param category the new category
     */
    @Deprecated
    public ModConfigBuilder push(String category){
        this.configBuilder.push(category);
        return this;
    }

    /**
     * Pops a category
     */
    @Deprecated
    public ModConfigBuilder pop(){
        this.configBuilder.pop();
        return this;
    }

    /**
     * Adds a comment to the current category
     * @param comment comment to be added
     */
    @Deprecated
    public ModConfigBuilder categoryComment(String comment){
        this.configBuilder.categoryComment(comment);
        return this;
    }

    /**
     * Makes the next defined value require a world game before being changed
     */
    @Deprecated
    public ModConfigBuilder gameRestart(){
        this.configBuilder.gameRestart();

        return this;
    }

    /**
     * Makes the next defined value not be synced with client
     */
    @Deprecated
    public ModConfigBuilder dontSync(){
        this.configBuilder.dontSync();
        return this;
    }

    /**
     * Adds a comment to the next defined value
     * @param comment comment to be added
     */
    @Deprecated
    public ModConfigBuilder comment(String comment){
        this.configBuilder.comment(comment);
        return this;
    }

    @Deprecated
    public Supplier<Boolean> define(String name, boolean defaultValue){
        return this.configBuilder.define(name, defaultValue);
    }

    @Deprecated
    public Supplier<Integer> define(String name, int defaultValue, int minValue, int maxValue){
        return this.configBuilder.define(name, defaultValue, minValue, maxValue);
    }

    @Deprecated
    public Supplier<Double> define(String name, double defaultValue, double minValue, double maxValue){
        return this.configBuilder.define(name, defaultValue, minValue, maxValue);
    }

    @Deprecated
    public <T extends Enum<T>> Supplier<T> define(String name, T defaultValue){
        return this.configBuilder.define(name, defaultValue);
    }

    @Deprecated
    public void build(){
        this.configBuilder.build();
    }
}
