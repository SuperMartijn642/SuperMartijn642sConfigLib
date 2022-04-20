package com.supermartijn642.configlib;

import com.supermartijn642.configlib.api.IConfigBuilder;

import java.util.Stack;

/**
 * Created 25/03/2022 by SuperMartijn642
 */
public abstract class BaseConfigBuilder<S> extends ConfigBuilder<S> implements IConfigBuilder {

    protected Stack<String> category = new Stack<>();
    protected String comment;
    protected boolean requiresGameRestart;
    protected boolean shouldBeSynced = true;
    protected boolean isClientOnly, isServerOnly;

    public BaseConfigBuilder(String modid, String name, String extension, boolean createSubDirectory){
        super(modid, name, extension, createSubDirectory);
    }

    protected abstract String[] getIllegalCharacters();

    protected String[] getPath(String key){
        String[] path = this.category.toArray(new String[this.category.size() + 1]);
        path[path.length - 1] = key;
        return path;
    }

    protected void resetState(){
        this.comment = null;
        this.requiresGameRestart = false;
        this.shouldBeSynced = true;
        this.isClientOnly = false;
        this.isServerOnly = false;
    }

    @Override
    public IConfigBuilder push(String category){
        if(category == null)
            throw new IllegalArgumentException("Category must not be null!");
        if(category.isEmpty())
            throw new IllegalArgumentException("Category must not be empty!");
        if(category.contains("."))
            throw new IllegalArgumentException("Category must not contain dots '.'!");
        for(String characters : this.getIllegalCharacters())
            if(category.contains(characters))
                throw new IllegalArgumentException("Category must not contain character '" + characters + "'!");

        this.category.push(category);
        return this;
    }

    @Override
    public IConfigBuilder pop(){
        if(this.category.isEmpty())
            throw new IllegalStateException("Category stack is empty!");

        this.category.pop();
        return this;
    }

    @Override
    public IConfigBuilder categoryComment(String comment){
        if(comment == null)
            throw new IllegalArgumentException("Comment must not be null!");
        if(comment.isEmpty())
            throw new IllegalArgumentException("Comment must not be empty!");
        for(String characters : this.getIllegalCharacters())
            if(comment.contains(characters))
                throw new IllegalArgumentException("Comment must not contain character '" + characters + "'!");

        this.addCategoryComment(this.category.toArray(String[]::new), comment);
        return this;
    }

    @Override
    public IConfigBuilder gameRestart(){
        this.requiresGameRestart = true;
        return this.dontSync();
    }

    @Override
    public IConfigBuilder dontSync(){
        this.shouldBeSynced = false;
        return this;
    }

    @Override
    public IConfigBuilder onlyOnClient(){
        if(this.isServerOnly)
            throw new IllegalStateException("OnlyOnServer has already been set!");

        this.isClientOnly = true;
        return this.dontSync();
    }

    @Override
    public IConfigBuilder onlyOnServer(){
        if(this.isClientOnly)
            throw new IllegalStateException("OnlyOnClient has already been set!");

        this.isServerOnly = true;
        return this.dontSync();
    }

    @Override
    public IConfigBuilder comment(String comment){
        if(comment == null)
            throw new IllegalArgumentException("Comment must not be null!");
        if(comment.isEmpty())
            throw new IllegalArgumentException("Comment must not be empty!");
        for(String characters : this.getIllegalCharacters())
            if(comment.contains(characters))
                throw new IllegalArgumentException("Comment must not contain character '" + characters + "'!");

        this.comment = comment;
        return this;
    }
}
