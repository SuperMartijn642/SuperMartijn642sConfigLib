package com.supermartijn642.configlib.toml;

import com.supermartijn642.configlib.ConfigFile;
import com.supermartijn642.configlib.ConfigLib;

import java.io.*;
import java.nio.file.*;

/**
 * Created 09/04/2022 by SuperMartijn642
 */
public class TomlConfigFile implements ConfigFile<TomlElement> {

    private final File file;
    private TomlTable table = new TomlTable();
    private boolean tracking = false;

    public TomlConfigFile(File file){
        this.file = file;
    }

    public TomlElement get(String[] path){
        if(path.length == 0)
            return this.table;

        // Find the parent object
        TomlTable object = this.table;
        for(int i = 0; i < path.length - 1; i++){
            TomlElement member = object.get(path[i]);
            // Return null if the parent doesn't exist
            if(member == null || !member.isTable())
                return null;
            object = member.getAsTable();
        }

        // Return the actual value
        return object.get(path[path.length - 1]);
    }

    private void set(String[] path, TomlElement element){
        // Find/create the correct parent object
        TomlTable object = this.table;
        for(int i = 0; i < path.length - 1; i++){
            TomlElement member = object.get(path[i]);
            if(member == null || !member.isTable()){
                TomlTable newObject = new TomlTable();
                if(member != null){
                    newObject.comment = member.comment;
                    newObject.valueHint = member.valueHint;
                }
                object.add(path[i], newObject);
                object = newObject;
            }else
                object = member.getAsTable();
        }

        // Get the comment and hint from the old element
        TomlElement oldElement = object.get(path[path.length - 1]);
        String comment = oldElement == null ? null : oldElement.comment;
        String hint = oldElement == null ? null : oldElement.valueHint;

        // Assign the actual value
        element.comment = comment;
        element.valueHint = hint;
        object.add(path[path.length - 1], element);
    }

    @Override
    public void setValue(String[] path, TomlElement value){
        this.set(path, value);
    }

    @Override
    public TomlElement getValue(String[] path){
        return this.get(path);
    }

    @Override
    public void setComment(String[] path, String comment){
        TomlElement element = this.get(path);
        if(element != null)
            element.comment = comment;
        else{
            TomlElement emptyElement = TomlElement.empty();
            this.set(path, emptyElement);
            emptyElement.comment = comment;
        }
    }

    @Override
    public void setAllowedValuesHint(String[] path, String hint){
        TomlElement element = this.get(path);
        if(element != null)
            element.valueHint = hint;
        else{
            TomlElement emptyElement = TomlElement.empty();
            this.set(path, emptyElement);
            emptyElement.valueHint = hint;
        }
    }

    @Override
    public void clearValues(){
        this.table = new TomlTable();
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
        new Thread(
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
        ).start();
    }

    @Override
    public void readFile(){
        // Use an empty json object if the file doesn't exist
        if(!this.file.exists() || this.file.isDirectory()){
            this.table = new TomlTable();
            return;
        }

        try(BufferedReader reader = new BufferedReader(new FileReader(this.file))){
            this.table = TomlDeserializer.readTomlTable(reader);
        }catch(Exception e){
            ConfigLib.LOGGER.error("Failed to read toml file '" + this.file.getPath() + "'!", e);
            this.table = new TomlTable();
        }
    }

    @Override
    public void writeFile(){
        // Create parent directory
        if(!this.file.getParentFile().exists())
            this.file.getParentFile().mkdirs();

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(this.file))){
            TomlSerializer.writeTomlTable(writer, this.table);
        }catch(Exception e){
            ConfigLib.LOGGER.error("Failed to write toml file '" + this.file.getPath() + "'!", e);
        }
    }
}
