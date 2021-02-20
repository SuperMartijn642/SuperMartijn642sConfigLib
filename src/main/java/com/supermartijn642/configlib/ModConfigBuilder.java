package com.supermartijn642.configlib;

import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.util.*;
import java.util.function.Supplier;

/**
 * Created 1/19/2021 by SuperMartijn642
 */
public class ModConfigBuilder {

    private final List<ModConfigValue<?>> allValues = new ArrayList<>();
    private final Map<String,String> categoryComments = new HashMap<>();
    private String category = "";
    private String comment = "";
    private boolean requiresGameRestart = false;
    private boolean syncWithClient = true;

    private final String modid;
    private final ModConfig.Type type;

    public ModConfigBuilder(String modid, ModConfig.Type type){
        this.modid = modid;
        this.type = type;
    }

    public ModConfigBuilder(String modid){
        this(modid, ModConfig.Type.COMMON);
    }

    /**
     * Pushes a new category
     * @param category the new category
     */
    public ModConfigBuilder push(String category){
        if(category == null)
            throw new IllegalArgumentException("category must not be null");
        if(category.isEmpty())
            throw new IllegalArgumentException("category must not be empty");

        if(this.category.isEmpty())
            this.category = category;
        this.category += "." + category;

        return this;
    }

    /**
     * Pops a category
     */
    public ModConfigBuilder pop(){
        if(this.category.isEmpty())
            throw new IllegalStateException("no more categories to pop");

        int index = this.category.lastIndexOf(".");
        if(index == -1)
            this.category = "";
        else
            this.category = this.category.substring(0, index);

        return this;
    }

    /**
     * Adds a comment to the current category
     * @param comment comment to be added
     */
    public ModConfigBuilder categoryComment(String comment){
        if(comment == null)
            throw new IllegalArgumentException("comment must not be null");
        if(comment.isEmpty())
            throw new IllegalArgumentException("comment must not be empty");
        if(this.category.isEmpty())
            throw new IllegalStateException("no category pushed");
        if(this.categoryComments.containsKey(this.category))
            throw new IllegalStateException("category " + this.category + " already has a comment");

        this.categoryComments.put(this.category, comment);

        return this;
    }

    /**
     * Makes the next defined value require a world game before being changed
     */
    public ModConfigBuilder gameRestart(){
        this.requiresGameRestart = true;
        return this;
    }

    /**
     * Makes the next defined value not be synced with client
     */
    public ModConfigBuilder dontSync(){
        this.syncWithClient = false;
        return this;
    }

    /**
     * Adds a comment to the next defined value
     * @param comment comment to be added
     */
    public ModConfigBuilder comment(String comment){
        if(comment == null)
            throw new IllegalArgumentException("comment must not be null");
        if(comment.isEmpty())
            throw new IllegalArgumentException("comment must not be empty");
        if(!this.comment.isEmpty())
            throw new IllegalStateException("a comment is already specified");

        this.comment = comment;

        return this;
    }

    public Supplier<Boolean> define(String name, boolean defaultValue){
        ModConfigValue<Boolean> value = new ModConfigValue.BooleanValue(this.getPath(name), this.comment, this.requiresGameRestart, this.syncWithClient, defaultValue);
        this.allValues.add(value);
        this.resetValues();
        return value::get;
    }

    public Supplier<Integer> define(String name, int defaultValue, int minValue, int maxValue){
        ModConfigValue<Integer> value = new ModConfigValue.IntegerValue(this.getPath(name), this.comment, this.requiresGameRestart, this.syncWithClient, defaultValue, minValue, maxValue);
        this.allValues.add(value);
        this.resetValues();
        return value::get;
    }

    public Supplier<Double> define(String name, double defaultValue, double minValue, double maxValue){
        ModConfigValue<Double> value = new ModConfigValue.FloatingValue(this.getPath(name), this.comment, this.requiresGameRestart, this.syncWithClient, defaultValue, minValue, maxValue);
        this.allValues.add(value);
        this.resetValues();
        return value::get;
    }

    public <T extends Enum<T>> Supplier<T> define(String name, T defaultValue){
        ModConfigValue<T> value = new ModConfigValue.EnumValue<>(this.getPath(name), this.comment, this.requiresGameRestart, this.syncWithClient, defaultValue);
        this.allValues.add(value);
        this.resetValues();
        return value::get;
    }

    private String getPath(String name){
        if(name == null)
            throw new IllegalArgumentException("name must not be null");
        if(name.isEmpty())
            throw new IllegalArgumentException("name must not be empty");

        return (this.category.isEmpty() ? "" : this.category + '.') + name;
    }

    private void resetValues(){
        this.requiresGameRestart = false;
        this.syncWithClient = true;
        this.comment = "";
    }

    public void build(){
        File file = new File(new File("."), "config/" + this.modid + '-' + this.type.name().toLowerCase(Locale.ROOT) + ".cfg");
        Configuration configuration = new Configuration(file, true);

        this.build(configuration);

        ModConfig config = new ModConfig(configuration, this.modid, this.type, this.allValues);

        ConfigLib.addConfig(config);
    }

    private void build(Configuration configuration){
        for(ModConfigValue<?> value : this.allValues)
            value.build(configuration);

        for(Map.Entry<String,String> category : this.categoryComments.entrySet())
            configuration.addCustomCategoryComment(category.getKey(), category.getValue());
    }

}
