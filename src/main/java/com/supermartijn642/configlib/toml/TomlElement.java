package com.supermartijn642.configlib.toml;

/**
 * Created 09/04/2022 by SuperMartijn642
 */
public abstract class TomlElement {

    protected String comment;
    protected String valueHint;

    public final boolean isTable(){
        return this instanceof TomlTable;
    }

    public final TomlTable getAsTable(){
        return (TomlTable)this;
    }

    public final boolean isEmpty(){
        return this instanceof Empty;
    }

    public final boolean isInteger(){
        return this instanceof TomlPrimitive.TomlInteger;
    }

    public final int getAsInteger(){
        return ((TomlPrimitive.TomlInteger)this).getValue();
    }

    public final boolean isDouble(){
        return this instanceof TomlPrimitive.TomlDouble;
    }

    public final double getAsDouble(){
        return ((TomlPrimitive.TomlDouble)this).getValue();
    }

    public final boolean isLong(){
        return this instanceof TomlPrimitive.TomlLong;
    }

    public final long getAsLong(){
        return ((TomlPrimitive.TomlLong)this).getValue();
    }

    public final boolean isBoolean(){
        return this instanceof TomlPrimitive.TomlBoolean;
    }

    public final boolean getAsBoolean(){
        return ((TomlPrimitive.TomlBoolean)this).getValue();
    }

    public final boolean isString(){
        return this instanceof TomlPrimitive.TomlString;
    }

    public final String getAsString(){
        return ((TomlPrimitive.TomlString)this).getValue();
    }

    private static class Empty extends TomlElement {}

    public static TomlElement empty(){
        return new Empty();
    }
}
