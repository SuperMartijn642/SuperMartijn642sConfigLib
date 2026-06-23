package com.supermartijn642.configlib;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created 24/03/2022 by SuperMartijn642
 */
public class ModConfig<S> {

    private final String modid;
    private final String identifier;
    private final ConfigFile<S> configFile;
    private final List<Entry<?,S>> entries;
    private final Map<String,Entry<?,S>> entriesByPath = new HashMap<>();
    private final List<Entry<?,S>> correctSideEntries = new ArrayList<>();
    private final List<Entry<?,S>> updatableEntries = new ArrayList<>();
    private final List<Entry<?,S>> syncableEntries = new ArrayList<>();
    private final List<Pair<String[],String>> categoryComments;
    private final boolean shouldBeSynced;

    public ModConfig(String modid, String identifier, ConfigFile<S> configFile, List<Entry<?,S>> configEntries, List<Pair<String[],String>> categoryComments){
        this.modid = modid;
        this.identifier = identifier;
        this.configFile = configFile;
        this.entries = Collections.unmodifiableList(configEntries);
        this.categoryComments = Collections.unmodifiableList(categoryComments);

        boolean shouldBeSynced = false;
        for(Entry<?,S> entry : this.entries){
            if(entry.configEntry.shouldBeSynced())
                shouldBeSynced = true;
        }
        this.shouldBeSynced = shouldBeSynced;
    }

    public void initialize(){
        // Sort all entries
        boolean isClientSide = ConfigLib.isClientEnvironment();
        for(Entry<?,S> entry : this.entries){
            if(isClientSide ? entry.configEntry.isServerOnly() : entry.configEntry.isClientOnly()){
                // Wrong side
                entry.wrongSide = true;
            }else{
                // Correct side
                this.correctSideEntries.add(entry);
                this.entriesByPath.put(entry.combinedPath, entry);
                if(!entry.configEntry.requiresGameRestart())
                    this.updatableEntries.add(entry);
                if(entry.configEntry.shouldBeSynced())
                    this.syncableEntries.add(entry);
            }
        }

        // Now initialize all entries
        this.configFile.readFile();

        this.correctSideEntries.forEach(this::readEntryValue);

        this.configFile.clearValues();

        for(Map.Entry<String[],String> comment : this.categoryComments)
            this.configFile.setComment(comment.getKey(), comment.getValue());

        for(Entry<?,S> entry : this.correctSideEntries){
            this.configFile.setComment(entry.path, entry.configEntry.getComment());
            this.configFile.setAllowedValuesHint(entry.path, entry.configEntry.getAllowedValuesHint());
            this.writeEntryValue(entry);
        }

        this.configFile.writeFile();
        this.configFile.startTrackingFile();

        this.correctSideEntries.forEach(entry -> entry.hasBeenInitialized = true);
    }

    private void updateValues(){
        this.updatableEntries.forEach(this::readEntryValue);
    }

    private <T> void writeEntryValue(Entry<T,S> entry){
        S serialized = entry.configEntry.serialize(entry.value);
        if(serialized == null)
            ConfigLib.LOGGER.error("Failed to serialize config value '" + entry.value + "' for '" + String.join(",", entry.path) + "' in config from " + this.modid + "!");
        else
            this.configFile.setValue(entry.path, serialized);
    }

    private <T> void readEntryValue(Entry<T,S> entry){
        S serialized = this.configFile.getValue(entry.path);
        if(serialized == null)
            entry.value = entry.configEntry.defaultValue();
        else{
            T value = entry.configEntry.deserialize(serialized);
            if(value == null || !entry.configEntry.validateValue(value))
                entry.value = entry.configEntry.defaultValue();
            else
                entry.value = value;
        }
    }

    public String getModid(){
        return this.modid;
    }

    public String getIdentifier(){
        return this.identifier;
    }

    public void onJoinGame(){
        this.updateValues();
    }

    public void onLeaveGame(){
        this.clearSyncedValues();
    }

    public boolean hasSyncableEntries(){
        return this.shouldBeSynced;
    }

    public void writeSyncableEntries(ByteBuf buffer){
        buffer.writeInt(this.syncableEntries.size());

        for(Entry<?,S> entry : this.syncableEntries)
            this.writeSyncableEntry(buffer, entry);
    }

    private <T> void writeSyncableEntry(ByteBuf buffer, Entry<T,S> entry){
        // Write entry's path
        byte[] pathBytes = entry.combinedPath.getBytes(StandardCharsets.UTF_8);
        buffer.writeInt(pathBytes.length);
        buffer.writeBytes(pathBytes);

        // Write entry's value
        byte[] bytes = null;
        try{
            bytes = entry.configEntry.write(entry.value);
        }catch(Exception e){
            ConfigLib.LOGGER.error("Failed to write synced config value '" + entry.value + "' for '" + String.join(",", entry.path) + "' in config from " + this.modid + "!", e);
        }
        if(bytes == null)
            buffer.writeInt(-1);
        else{
            buffer.writeInt(bytes.length);
            buffer.writeBytes(bytes);
        }
    }

    public void readSyncableValues(ByteBuf buffer){
        int entryCount = buffer.readInt();

        for(int i = 0; i < entryCount; i++){
            // Read entry's path
            byte[] pathBytes = new byte[Math.max(buffer.readInt(), 0)];
            buffer.readBytes(pathBytes);
            String combinedPath = new String(pathBytes, StandardCharsets.UTF_8);

            // Read entry value's bytes
            int valueByteCount = buffer.readInt();
            byte[] valueBytes = new byte[Math.max(valueByteCount, 0)];
            buffer.readBytes(valueBytes);

            // Get the correct entry
            Entry<?,S> entry = this.entriesByPath.get(combinedPath);
            if(entry == null){
                ConfigLib.LOGGER.error("Received synced config value for unknown entry '" + combinedPath + "' in config from " + this.modid + "!");
                continue;
            }
            if(!entry.configEntry.shouldBeSynced()){
                ConfigLib.LOGGER.error("Received synced config value for entry which should not be synced '" + combinedPath + "' in config from " + this.modid + "!");
                continue;
            }

            // Read the entry's value
            this.readSyncableEntry(valueByteCount < 0 ? null : ByteBuffer.wrap(valueBytes), entry);
        }
    }

    private <T> void readSyncableEntry(ByteBuffer buffer, Entry<T,S> entry){
        // Set value to default if it failed to write correctly
        if(buffer == null){
            entry.syncedValue = entry.configEntry.defaultValue();
            return;
        }

        // Try to read the entry's value
        T value;
        try{
            value = entry.configEntry.read(buffer);
        }catch(Exception e){
            ConfigLib.LOGGER.error("Failed to read synced config value for entry '" + String.join(",", entry.path) + "' in config from " + this.modid + "!", e);
            entry.syncedValue = entry.configEntry.defaultValue();
            return;
        }
        if(value == null){
            ConfigLib.LOGGER.error("Failed to read synced config value for entry '" + String.join(",", entry.path) + "' in config from " + this.modid + "!");
            entry.syncedValue = entry.configEntry.defaultValue();
            return;
        }

        // Validate read value
        if(!entry.configEntry.validateValue(value)){
            ConfigLib.LOGGER.error("Received invalid synced config value '" + entry.value + "' for entry '" + String.join(",", entry.path) + "' from network in config from " + this.modid + "!");
            entry.syncedValue = entry.configEntry.defaultValue();
            return;
        }

        entry.syncedValue = value;
    }

    private void clearSyncedValues(){
        this.syncableEntries.forEach(entry -> entry.syncedValue = null);
    }

    protected static class Entry<T, S> {

        protected final String[] path;
        protected final String combinedPath;
        protected final ConfigEntry<T,S> configEntry;
        private boolean hasBeenInitialized;
        private boolean wrongSide;
        private T value, syncedValue;

        Entry(String[] path, ConfigEntry<T,S> configEntry){
            this.path = path;
            this.combinedPath = String.join(".", path);
            this.configEntry = configEntry;
        }

        public T getValue(){
            if(!this.hasBeenInitialized)
                throw new IllegalStateException("Config has not yet been initialized!");
            if(this.wrongSide)
                throw new IllegalStateException("Entry " + String.join(".", this.path) + " is " + (this.configEntry.isClientOnly() ? "client" : "server") + " side only!");

            return this.syncedValue != null ? this.syncedValue : this.value;
        }
    }
}
