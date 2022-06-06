package com.supermartijn642.configlib;

/**
 * Created 24/03/2022 by SuperMartijn642
 */
public interface ConfigFile<S> {

    S getValue(String[] path);

    void setValue(String[] path, S value);

    void setComment(String[] path, String comment);

    void setAllowedValuesHint(String[] path, String comment);

    void clearValues();

    void startTrackingFile();

    void readFile();

    void writeFile();
}
