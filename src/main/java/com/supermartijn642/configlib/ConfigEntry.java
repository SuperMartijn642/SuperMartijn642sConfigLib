package com.supermartijn642.configlib;

import java.nio.ByteBuffer;

/**
 * Created 24/03/2022 by SuperMartijn642
 */
public interface ConfigEntry<T,S> {

    T defaultValue();

    boolean shouldBeSynced();

    boolean requiresGameRestart();

    boolean isClientOnly();

    boolean isServerOnly();

    String getComment();

    String getAllowedValuesHint();

    boolean validateValue(T value);

    S serialize(T value);

    T deserialize(S serialized);

    byte[] write(T value);

    T read(ByteBuffer buffer);
}
