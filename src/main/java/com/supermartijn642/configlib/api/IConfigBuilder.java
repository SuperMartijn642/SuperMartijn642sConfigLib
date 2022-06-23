package com.supermartijn642.configlib.api;

import java.util.function.Supplier;

/**
 * Created 25/03/2022 by SuperMartijn642
 * <br>
 * Configs can be created using a {@link IConfigBuilder}
 * @author SuperMartijn624
 * @date 25/03/2022
 * @see IConfigBuilder
 */
public interface IConfigBuilder {

    /**
     * Pushes a new category
     * @param category the new category
     * @return a reference to this builder
     * @throws IllegalArgumentException if {@code category} is null or empty or
     *                                  contains a dot '.' or syntax character
     *                                  for the config format
     */
    IConfigBuilder push(String category);

    /**
     * Pops a category
     * @return a reference to this builder
     * @throws IllegalArgumentException if the category stack is empty
     */
    IConfigBuilder pop();

    /**
     * Adds a comment to the current category. If no category is pushed, the
     * comment will serve as a top level comment for the config file.
     * @param comment comment to be added
     * @return a reference to this builder
     * @throws IllegalArgumentException if {@code comment} is null or empty or
     *                                  contains syntax characters for the
     *                                  config format
     */
    IConfigBuilder categoryComment(String comment);

    /**
     * Makes the next defined entry require a game restart before it is reloaded.
     * This function also calls {@link #dontSync()} meaning the next defined
     * entry not be synced between client and server.
     * @return a reference to this builder
     */
    IConfigBuilder gameRestart();

    /**
     * Makes the next defined entry not be synced between client and server.
     * This function is irrelevant if either {@link #onlyOnClient()} or
     * {@link #onlyOnServer()} is set
     * @return a reference to this builder
     */
    IConfigBuilder dontSync();

    /**
     * The next defined entry will only be generated on a client
     * @return a reference to this builder
     * @throws IllegalStateException if {@link #onlyOnServer()} is already set
     */
    IConfigBuilder onlyOnClient();

    /**
     * The next defined entry will only be generated on a dedicated server
     * @return a reference to this builder
     * @throws IllegalStateException if {@link #onlyOnClient()} is already set
     */
    IConfigBuilder onlyOnServer();

    /**
     * Adds a comment to the next defined value
     * @param comment comment to be added
     * @return a reference to this builder
     * @throws IllegalArgumentException if {@code comment} is null or empty or
     *                                  contains syntax characters for the
     *                                  config format
     * @throws IllegalStateException    if a comment is already set
     */
    IConfigBuilder comment(String comment);

    /**
     * Creates a boolean config entry with the given key
     * @param key          key for the entry
     * @param defaultValue default value of the entry
     * @return a {@link Supplier Supplier&lt;Boolean&gt;} from which the entry's
     * value can be obtained
     * @throws IllegalArgumentException if {@code key} is null or empty or
     *                                  contains syntax characters for the
     *                                  config format
     */
    Supplier<Boolean> define(String key, boolean defaultValue);

    /**
     * Creates an integer config entry with the given key
     * @param key          key for the entry
     * @param defaultValue default value of the entry
     * @param minValue     minimum value of the entry
     * @param maxValue     maximum value of the entry
     * @return a {@link Supplier Supplier&lt;Integer&gt;} from which the entry's
     * value can be obtained
     * @throws IllegalArgumentException if {@code key} is null or empty or
     *                                  contains syntax characters for the
     *                                  config format,
     *                                  if {@code defaultValue < minValue} or
     *                                  {@code defaultValue > maxValue}
     */
    Supplier<Integer> define(String key, int defaultValue, int minValue, int maxValue);

    /**
     * Creates a long config entry with the given key
     * @param key          key for the entry
     * @param defaultValue default value of the entry
     * @param minValue     minimum value of the entry
     * @param maxValue     maximum value of the entry
     * @return a {@link Supplier Supplier&lt;Long&gt;} from which the entry's
     * value can be obtained
     * @throws IllegalArgumentException if {@code key} is null or empty or
     *                                  contains syntax characters for the
     *                                  config format,
     *                                  if {@code defaultValue < minValue} or
     *                                  {@code defaultValue > maxValue}
     */
    Supplier<Long> define(String key, long defaultValue, long minValue, long maxValue);

    /**
     * Creates a double config entry with the given key
     * @param key          key for the entry
     * @param defaultValue default value of the entry
     * @param minValue     minimum value of the entry
     * @param maxValue     maximum value of the entry
     * @return a {@link Supplier Supplier&lt;Double&gt;} from which the entry's
     * value can be obtained
     * @throws IllegalArgumentException if {@code key} is null or empty or
     *                                  contains syntax characters for the
     *                                  config format,
     *                                  if {@code defaultValue < minValue} or
     *                                  {@code defaultValue > maxValue}
     */
    Supplier<Double> define(String key, double defaultValue, double minValue, double maxValue);

    /**
     * Creates an enum config entry with the given key
     * @param <T>          the enum type
     * @param key          key for the entry
     * @param defaultValue default value of the entry
     * @return a {@link Supplier Supplier&lt;T&gt;} from which the entry's
     * value can be obtained
     * @throws IllegalArgumentException if {@code key} is null or empty or
     *                                  contains syntax characters for the
     *                                  config format,
     *                                  if {@code defaultValue} is {@code null}
     */
    <T extends Enum<T>> Supplier<T> define(String key, T defaultValue);

    /**
     * Creates a string config entry with the given key
     * @param key          key for the entry
     * @param defaultValue default value of the entry
     * @param minLength    minimum length of the entry's value
     * @param maxLength    maximum length of the entry's value
     * @return a {@link Supplier Supplier&lt;String&gt;} from which the entry's
     * value can be obtained
     * @throws IllegalArgumentException if {@code key} is null or empty or
     *                                  contains syntax characters for the
     *                                  config format,
     *                                  if {@code defaultValue} is {@code null}
     *                                  or contains syntax characters for the
     *                                  config format,
     *                                  if {@code defaultValue.length() < minLength}
     *                                  or {@code defaultValue.length() > maxLength}
     */
    Supplier<String> define(String key, String defaultValue, int minLength, int maxLength);

    /**
     * Completes the config. After this call, no new entries may be defined
     * and entries suppliers can safely be called
     * @throws IllegalStateException if the config was already completed or the
     *                               builder contains no entries
     */
    void build();
}
