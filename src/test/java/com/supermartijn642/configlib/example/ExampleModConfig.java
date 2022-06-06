package com.supermartijn642.configlib.example;

import com.supermartijn642.configlib.api.ConfigBuilders;
import com.supermartijn642.configlib.api.IConfigBuilder;

import java.util.function.Supplier;

/**
 * Created 1/21/2021 by SuperMartijn642
 */
public class ExampleModConfig {

    private enum ExampleEnum {
        CATS, DOGS, BOTH
    }

    public static final Supplier<Boolean> booleanValue;
    public static final Supplier<Integer> integerValue;
    public static final Supplier<Double> doubleValue;
    public static final Supplier<ExampleEnum> enumValue;

    public static final Supplier<Boolean> notReloadedValue;
    public static final Supplier<Boolean> notSynchronizedValue;

    public static final Supplier<Boolean> clientCategoryValue;

    static{
        // construct a new config builder
        IConfigBuilder builder = ConfigBuilders.newTomlConfig("configlibexample", null, false);


        // a boolean value
        booleanValue = builder.comment("this is a boolean value with default true").define("booleanValue", true);
        // an integer value
        integerValue = builder.comment("this is an integer value between 0 and 10").define("integerValue", 5, 0, 10);
        // a double value
        doubleValue = builder.comment("this is a double value between 0.0 and 1.0").define("doubleValue", 0.5, 0, 1);
        // an enum value
        enumValue = builder.comment("this is an enum value of type ExampleEnum").define("enumValue", ExampleEnum.DOGS);


        // values are reloaded between world loads by default, to only load a value at launch use ModConfigBuilder#gameRestart()
        notReloadedValue = builder.gameRestart().comment("this value is only reloaded when Minecraft launches").define("notReloadedValue", true);
        // values in COMMON or SERVER configs are synchronized with clients by default, to prevent this use ModConfigBuilder#dontSync()
        notSynchronizedValue = builder.dontSync().comment("this value is not synchronized with clients").define("notSynchronizedValue", true);


        // values can be put into categories
        builder.push("client").categoryComment("this is a comment for the 'client' category");
        // a value in the 'client' category
        clientCategoryValue = builder.comment("this value is in the 'client' category").define("clientValue", true);
        // end the 'client' category
        builder.pop();


        // build the config
        builder.build();
    }

}
