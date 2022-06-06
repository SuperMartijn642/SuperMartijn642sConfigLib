package com.supermartijn642.configlib.api;

import com.supermartijn642.configlib.json.JsonConfigBuilder;
import com.supermartijn642.configlib.toml.TomlConfigBuilder;

/**
 * Provides methods to obtain config builders for various file formats.
 * <p>
 * Currently, the json and toml file formats are supported. Builders for these
 * formats can be obtained from {@link #newJsonConfig(String, String, boolean)}
 * and {@link #newTomlConfig(String, String, boolean)} respectively.
 * @author SuperMartijn624
 * @date 25/03/2022
 * @see IConfigBuilder
 */
public interface ConfigBuilders {

    /**
     * Creates a {@link IConfigBuilder} with the json format
     * @param modid              modid of the mod owning this config
     * @param name               name of the config, may be {@code null}
     * @param createSubDirectory if true, the config will be placed in a
     *                           subdirectory with the modid as name
     * @return a {@link IConfigBuilder}
     * @throws IllegalArgumentException if {@code modid} is null or empty,
     *                                  if {@code name} is null or empty or
     *                                  contains illegal file name characters
     */
    static IConfigBuilder newJsonConfig(String modid, String name, boolean createSubDirectory){
        if(name != null && name.matches("[^\\\\/:*?\"<>|]"))
            throw new IllegalArgumentException("Name may only contain valid file name characters!");
        return new JsonConfigBuilder(modid, name, createSubDirectory);
    }

    /**
     * Creates a {@link IConfigBuilder} with the toml format
     * @param modid              modid of the mod owning this config
     * @param name               name of the config, may be {@code null}
     * @param createSubDirectory if true, the config will be placed in a
     *                           subdirectory with the modid as name
     * @return a {@link IConfigBuilder}
     * @throws IllegalArgumentException if {@code modid} is null or empty,
     *                                  if {@code name} is null or empty or
     *                                  contains illegal file name characters
     */
    static IConfigBuilder newTomlConfig(String modid, String name, boolean createSubDirectory){
        if(name != null && name.matches("[^\\\\/:*?\"<>|]"))
            throw new IllegalArgumentException("Name may only contain valid file name characters!");
        return new TomlConfigBuilder(modid, name, createSubDirectory);
    }
}
