package com.supermartijn642.configlib;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Created 1/19/2021 by SuperMartijn642
 */
public abstract class ModConfigValue<T> {

    private final Class<T> type;
    private final String name;
    private final String path;
    private final String comment;
    private final boolean requiresGameRestart;
    private final boolean syncWithClient;
    private final T defaultValue;

    protected CommentedFileConfig config;

    private boolean initial = true;
    private T value;

    private boolean synced = false;
    private T syncedValue;

    protected ModConfigValue(Class<T> type, String path, String comment, boolean requiresGameRestart, boolean syncWithClient, T defaultValue){
        this.type = type;
        int indexOf = path.lastIndexOf('.');
        this.name = indexOf >= 0 ? path.substring(indexOf + 1) : path;
        this.path = indexOf >= 0 ? path.substring(0, indexOf) : "";
        comment = comment == null || comment.isEmpty() ? "" : comment + "\n";
        comment += requiresGameRestart ? "Requires a game restart" : "Requires a world reload";
        this.comment = comment;
        this.requiresGameRestart = requiresGameRestart;
        this.syncWithClient = syncWithClient;
        this.defaultValue = defaultValue;
    }

    protected void build(CommentedFileConfig configuration){
        if(!this.validateValue(this.name, this.path, this.defaultValue, this.comment, configuration))
            this.setValue(this.name, this.path, this.defaultValue, this.comment, configuration);
        this.config = configuration;
    }

    protected abstract boolean validateValue(String name, String path, T defaultValue, String comment, CommentedFileConfig configuration);

    protected abstract void setValue(String name, String path, T defaultValue, String comment, CommentedFileConfig configuration);

    protected abstract T getValue(String name, String path, T defaultValue, String comment, CommentedFileConfig configuration);

    protected void updateValue(){
        if(this.initial || !this.requiresGameRestart){
            this.value = this.getValue(this.name, this.path, this.defaultValue, this.comment, this.config);
            this.initial = false;
        }
    }

    protected String getFullPath(){
        return this.path.isEmpty() ? this.name : this.path + "." + this.name;
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
        if(this.value == null)
            this.updateValue();
        return this.synced ? this.syncedValue : this.value;
    }

    public Class<T> getValueType(){
        return this.type;
    }

    public static class BooleanValue extends ModConfigValue<Boolean> {

        protected BooleanValue(String path, String comment, boolean requiresGameRestart, boolean syncWithClient, Boolean defaultValue){
            super(Boolean.class, path, comment, requiresGameRestart, syncWithClient, defaultValue);
        }

        @Override
        protected boolean validateValue(String name, String path, Boolean defaultValue, String comment, CommentedFileConfig configuration){
            return configuration.get(this.getFullPath()) instanceof Boolean;
        }

        @Override
        protected void setValue(String name, String path, Boolean defaultValue, String comment, CommentedFileConfig configuration){
            comment += "\nAllowed Values: true, false  Default: " + defaultValue;
            configuration.set(this.getFullPath(), defaultValue);
            configuration.setComment(this.getFullPath(), comment);
        }

        @Override
        protected Boolean getValue(String name, String path, Boolean defaultValue, String comment, CommentedFileConfig configuration){
            Object value = configuration.get(this.getFullPath());
            return value instanceof Boolean ? (boolean)value : defaultValue;
        }
    }

    public static class IntegerValue extends ModConfigValue<Integer> {

        private final int min, max;

        protected IntegerValue(String path, String comment, boolean requiresGameRestart, boolean syncWithClient, Integer defaultValue, int minValue, int maxValue){
            super(Integer.class, path, comment, requiresGameRestart, syncWithClient, defaultValue);
            this.min = minValue;
            this.max = maxValue;
        }

        @Override
        protected boolean validateValue(String name, String path, Integer defaultValue, String comment, CommentedFileConfig configuration){
            Object value = configuration.get(this.getFullPath());
            return value instanceof Integer && (int)value >= this.min && (int)value <= this.max;
        }

        @Override
        protected void setValue(String name, String path, Integer defaultValue, String comment, CommentedFileConfig configuration){
            comment += "\nAllowed Range: " + this.min + " ~ " + this.max + "  Default: " + defaultValue;
            configuration.set(this.getFullPath(), defaultValue);
            configuration.setComment(this.getFullPath(), comment);
        }

        @Override
        protected Integer getValue(String name, String path, Integer defaultValue, String comment, CommentedFileConfig configuration){
            Object value = configuration.get(this.getFullPath());
            return value instanceof Integer && (int)value >= this.min && (int)value <= this.max ? (int)value : defaultValue;
        }
    }

    public static class FloatingValue extends ModConfigValue<Double> {

        private final double min, max;

        protected FloatingValue(String path, String comment, boolean requiresGameRestart, boolean syncWithClient, Double defaultValue, double minValue, double maxValue){
            super(Double.class, path, comment, requiresGameRestart, syncWithClient, defaultValue);
            this.min = minValue;
            this.max = maxValue;
        }

        @Override
        protected boolean validateValue(String name, String path, Double defaultValue, String comment, CommentedFileConfig configuration){
            Object value = configuration.get(this.getFullPath());
            return value instanceof Double && (double)value >= this.min && (double)value <= this.max;
        }

        @Override
        protected void setValue(String name, String path, Double defaultValue, String comment, CommentedFileConfig configuration){
            comment += "\nAllowed Range: " + this.min + " ~ " + this.max + "  Default: " + defaultValue;
            configuration.set(this.getFullPath(), defaultValue);
            configuration.setComment(this.getFullPath(), comment);
        }

        @Override
        protected Double getValue(String name, String path, Double defaultValue, String comment, CommentedFileConfig configuration){
            Object value = configuration.get(this.getFullPath());
            return value instanceof Double && (double)value >= this.min && (double)value <= this.max ? (double)value : defaultValue;
        }
    }

    public static class EnumValue<T extends Enum<T>> extends ModConfigValue<T> {

        protected EnumValue(String path, String comment, boolean requiresGameRestart, boolean syncWithClient, T defaultValue){
            super(defaultValue.getDeclaringClass(), path, comment, requiresGameRestart, syncWithClient, defaultValue);
        }

        @Override
        protected boolean validateValue(String name, String path, T defaultValue, String comment, CommentedFileConfig configuration){
            Object value = configuration.get(this.getFullPath());
            if(!(value instanceof String))
                return false;

            try{
                Enum.valueOf(defaultValue.getDeclaringClass(), ((String)value).toUpperCase(Locale.ROOT));
                return true;
            }catch(Exception ignore){
                return false;
            }
        }

        @Override
        protected void setValue(String name, String path, T defaultValue, String comment, CommentedFileConfig configuration){
            String values = Arrays.stream(defaultValue.getDeclaringClass().getEnumConstants()).map(Enum::name).collect(Collectors.joining(", "));
            comment += "\nAllowed Range: " + values + "  Default: " + defaultValue;
            configuration.set(this.getFullPath(), defaultValue);
            configuration.setComment(this.getFullPath(), comment);
        }

        @Override
        protected T getValue(String name, String path, T defaultValue, String comment, CommentedFileConfig configuration){
            Object value = configuration.get(this.getFullPath());
            if(!(value instanceof String))
                return defaultValue;

            try{
                return Enum.valueOf(defaultValue.getDeclaringClass(), ((String)value).toUpperCase(Locale.ROOT));
            }catch(Exception ignore){
                return defaultValue;
            }
        }
    }

}
