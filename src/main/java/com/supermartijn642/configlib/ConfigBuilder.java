package com.supermartijn642.configlib;

import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created 24/03/2022 by SuperMartijn642
 */
public abstract class ConfigBuilder<S> {

    private final String modid, name, extension;
    private final boolean createSubDirectory;

    private final Map<String,ModConfig.Entry<?,S>> entries = new HashMap<>();
    private final Map<List<String>,String> categoryComments = new HashMap<>();
    private boolean hasBeenBuild = false;

    public ConfigBuilder(String modid, String name, String extension, boolean createSubDirectory){
        if(modid == null || modid.isEmpty())
            throw new IllegalArgumentException("Modid must not be null!");

        this.modid = modid;
        this.name = name == null || name.isEmpty() ? null : name;
        this.extension = extension == null ? "" : extension.charAt(0) == '.' ? extension.substring(1) : extension;
        this.createSubDirectory = createSubDirectory;
    }

    /**
     * May be called at any time to create a config file
     */
    protected abstract ConfigFile<S> createConfigFile(File file);

    protected <T> Supplier<T> addEntry(String[] path, ConfigEntry<T,S> configEntry){
        if(this.entries.containsKey(String.join(".", path)))
            throw new IllegalStateException("An entry for '" + String.join(".", path) + "' is already defined!");
        if(configEntry.shouldBeSynced() && String.join(".", path).getBytes(StandardCharsets.UTF_8).length > 1024)
            throw new IllegalArgumentException("Syncable value '" + String.join(".", path) + "''s path must be smaller than 1024 bytes in length!");

        ModConfig.Entry<T,S> entry = new ModConfig.Entry<>(path, configEntry);
        this.entries.put(String.join(".", path), entry);
        return entry::getValue;
    }

    protected void addCategoryComment(String[] path, String comment){
        List<String> pathKey = Arrays.asList(path);
        if(this.categoryComments.containsKey(pathKey))
            throw new IllegalStateException("A comment for category '" + String.join(".", path) + "' is already defined!");

        this.categoryComments.put(pathKey, comment);
    }

    private String getRelativeFileLocation(){
        String cleanModid = this.modid.replaceAll("[\\\\/:*?\"<>|]", "");

        String name = this.name;
        if(name == null || name.isEmpty()){
            boolean isClientOnly = true, isServerOnly = true;
            for(ModConfig.Entry<?,S> entry : this.entries.values()){
                if(!entry.configEntry.isClientOnly())
                    isClientOnly = false;
                if(!entry.configEntry.isServerOnly())
                    isServerOnly = false;
            }
            name = isClientOnly ? "client" : isServerOnly ? "server" : "common";
        }
        return cleanModid + (this.createSubDirectory ? File.separator : '-') + name + '.' + this.extension;
    }

    public void build(){
        if(this.hasBeenBuild)
            throw new IllegalStateException("Config has already been build!");
        this.hasBeenBuild = true;

        String relativeLocation = this.getRelativeFileLocation();
        File configLocation = new File(ConfigLib.getConfigFolder(), relativeLocation);
        ConfigFile<S> configFile = this.createConfigFile(configLocation);

        List<Pair<String[],String>> categoryComments =
            this.categoryComments.entrySet().stream()
            .map(entry -> Pair.of(entry.getKey().toArray(String[]::new), entry.getValue()))
            .collect(Collectors.toList());

        ModConfig<S> config = new ModConfig<>(this.modid, relativeLocation, configFile, this.entries.values().stream().toList(), categoryComments);
        ConfigLib.addConfig(config);
    }
}
