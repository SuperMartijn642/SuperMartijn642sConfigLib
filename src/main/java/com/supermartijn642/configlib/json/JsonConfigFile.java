package com.supermartijn642.configlib.json;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.supermartijn642.configlib.ConfigFile;
import com.supermartijn642.configlib.ConfigLib;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.*;

/**
 * Created 23/03/2022 by SuperMartijn642
 */
public class JsonConfigFile implements ConfigFile<JsonElement> {

    private static final Gson GSON = new GsonBuilder().setLenient().setPrettyPrinting().create();

    private final File file;
    private JsonObject json = new JsonObject();
    private boolean tracking = false;

    public JsonConfigFile(File file){
        this.file = file;
    }

    public JsonElement get(String[] path, String key){
        if(path.length == 0)
            return this.json;

        // Find the parent object
        JsonObject object = this.json;
        for(int i = 0; i < path.length; i++){
            JsonElement member = object.get(path[i]);
            // Return null if the parent doesn't exist
            if(member == null || !member.isJsonObject())
                return null;
            object = member.getAsJsonObject();
        }

        // Return the actual value
        return object.get(key);
    }

    private void set(String[] path, String key, JsonElement element){
        // Find/create the correct parent object
        JsonObject object = this.json;
        for(String s : path){
            JsonElement member = object.get(s);
            if(member == null || !member.isJsonObject()){
                JsonObject newObject = new JsonObject();
                object.add(s, newObject);
                object = newObject;
            }else
                object = member.getAsJsonObject();
        }

        // Assign the actual value
        object.add(key, element);
    }

    @Override
    public void setValue(String[] path, JsonElement value){
        this.set(path, "value", value);
    }

    @Override
    public JsonElement getValue(String[] path){
        return this.get(path, "value");
    }

    @Override
    public void setComment(String[] path, String comment){
        this.set(path, "comment", new JsonPrimitive(comment));
    }

    @Override
    public void setAllowedValuesHint(String[] path, String hint){
        this.set(path, "hint", new JsonPrimitive(hint));
    }

    @Override
    public void clearValues(){
        this.json = new JsonObject();
    }

    @Override
    public void startTrackingFile(){
        if(this.tracking)
            throw new IllegalStateException("Config file is already being tracked!");

        Path parentPath = this.file.getParentFile().toPath();

        // Create a new watch service for the file
        WatchService watchService;
        try{
            watchService = parentPath.getFileSystem().newWatchService();
            parentPath.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
        }catch(Exception e){
            ConfigLib.LOGGER.error("Failed to create watch service for config file!", e);
            return;
        }

        this.tracking = true;

        // Create a new thread to wait for watch service events
        Thread watchThread = new Thread(
            () -> {
                while(true){
                    WatchKey watchKey;

                    // Wait for a new event
                    try{
                        watchKey = watchService.take();
                    }catch(Exception e){
                        // Watch service got closed
                        this.tracking = false;
                        break;
                    }

                    // Read the file again
                    for(WatchEvent<?> pollEvent : watchKey.pollEvents()){
                        Path path = (Path)pollEvent.context();
                        if(this.file.getName().equals(path.toString())){
                            this.readFile();
                            break;
                        }
                    }

                    // Reset the watch key
                    watchKey.reset();
                }
            },
            "Config Lib config file watcher"
        );
        // Make sure the thread doesn't prevent the program from exiting
        watchThread.setDaemon(true);
        // Start the file watcher thread
        watchThread.start();
    }

    @Override
    public void readFile(){
        // Use an empty json object if the file doesn't exist
        if(!this.file.exists() || this.file.isDirectory()){
            this.json = new JsonObject();
            return;
        }

        try(JsonReader reader = GSON.newJsonReader(new FileReader(this.file))){
            this.json = GSON.fromJson(reader, JsonObject.class);
        }catch(Exception e){
            ConfigLib.LOGGER.error("Failed to read json file '" + this.file.getPath() + "'!", e);
            this.json = new JsonObject();
        }
    }

    @Override
    public void writeFile(){
        // Create parent directory
        if(!this.file.getParentFile().exists())
            this.file.getParentFile().mkdirs();

        try(JsonWriter writer = GSON.newJsonWriter(new FileWriter(this.file))){
            GSON.toJson(this.json, writer);
        }catch(Exception e){
            ConfigLib.LOGGER.error("Failed to write json file '" + this.file.getPath() + "'!", e);
        }
    }
}
