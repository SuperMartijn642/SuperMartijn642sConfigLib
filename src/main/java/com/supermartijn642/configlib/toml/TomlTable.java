package com.supermartijn642.configlib.toml;

import com.google.gson.internal.LinkedTreeMap;

import java.util.Map;
import java.util.Set;

/**
 * Created 09/04/2022 by SuperMartijn642
 */
public class TomlTable extends TomlElement {

    private final LinkedTreeMap<String,TomlElement> entries = new LinkedTreeMap<>();

    public TomlTable(){
    }

    public void add(String property, TomlElement value){
        if(value == null)
            throw new IllegalArgumentException("Property value must not be null!");

        this.entries.put(property, value);
    }

    public void add(String property, int value){
        this.add(property, TomlPrimitive.of(value));
    }

    public void add(String property, double value){
        this.add(property, TomlPrimitive.of(value));
    }

    public void add(String property, long value){
        this.add(property, TomlPrimitive.of(value));
    }

    public void add(String property, boolean value){
        this.add(property, TomlPrimitive.of(value));
    }

    public void add(String property, String value){
        this.add(property, TomlPrimitive.of(value));
    }

    public TomlElement remove(String property){
        return this.entries.remove(property);
    }

    public boolean has(String property){
        return this.entries.containsKey(property);
    }

    public TomlElement get(String property){
        return this.entries.get(property);
    }

    public Set<Map.Entry<String,TomlElement>> entrySet(){
        return this.entries.entrySet();
    }

    public Set<String> keySet(){
        return this.entries.keySet();
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || this.getClass() != o.getClass()) return false;

        TomlTable tomlTable = (TomlTable)o;

        return this.entries.equals(tomlTable.entries);
    }

    @Override
    public int hashCode(){
        return this.entries.hashCode();
    }

    @Override
    public String toString(){
        return "TomlTable" + this.entries;
    }
}
