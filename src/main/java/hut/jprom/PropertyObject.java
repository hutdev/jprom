/*
  * Copyright (c) 2015, hutdev <hutdevelopment@gmail.com>
  * Permission to use, copy, modify, and/or distribute this software for any
  * purpose with or without fee is hereby granted, provided that the above
  * copyright notice and this permission notice appear in all copies.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH
  * REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY
  * AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
  * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM
  * LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR
  * OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
  * PERFORMANCE OF THIS SOFTWARE.
 */
package hut.jprom;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * This class holds all the information for an object that has been deserialized
 * from property data. This includes the class of the object, the object itself
 * and the instance name that was defined for the object in the property data.
 *
 * @author <a href="mailto:hutdevelopment@gmail.com">hutdev</a>
 * @param <T> The type of the unmarshalled object.
 */
public class PropertyObject<T> {

    /**
     * Format used for the {@link String} representation of this object.
     */
    private static final String TO_STRING_FORMAT = "PropertyObject{rootClass=%s, object=(%s), instanceName=%s}";
    /**
     * The class of the unmarshalled object.
     */
    private final Class<T> type;
    /**
     * The unmarshalled object.
     */
    private final T object;
    /**
     * The name for the unmarshalled object.
     */
    private final String instanceName;

    /**
     * Creates a new instance of <code>PropertyObject</code>.
     *
     * @param type The class of the unmarshalled object.
     * @param object The unmarshalled object.
     * @param instanceName The name for the unmarshalled object.
     */
    PropertyObject(Class<T> type, T object, String instanceName) {
        this.type = type;
        this.object = object;
        this.instanceName = instanceName;
    }

    /**
     * Creates a {@link Set} of <code>PropertyObject</code>s from a {@link Map}
     * defining instance names for objects of a specific type.
     *
     * @param <T> Type of the objects.
     * @param objMap Defines instance names for objects.
     * @return a {@link Set} of <code>PropertyObject</code>s for the passed
     * objects.
     */
    public static <T> Set<PropertyObject<T>> fromMap(Map<String, T> objMap) {
        return objMap.entrySet().stream().collect(HashSet::new,
                (set, entry) -> {
                    final T value = entry.getValue();
                    set.add(new PropertyObject(value.getClass(), value, entry.getKey()));
                },
                Collection::addAll
        );
    }

    /**
     * Creates a {@link Map} defining instance names for unmarshalled objects.
     *
     * @param <T> Type of the objects.
     * @param objs The unmarshalled information.
     * @return a {@link Map} defining instance names for the unmarshalled
     * objects.
     */
    public static <T> Map<String, T> toMap(PropertyObject<T>... objs) {
        return toMap(Stream.of(objs));
    }

    /**
     * Creates a {@link Map} defining instance names for unmarshalled objects.
     *
     * @param <T> Type of the objects.
     * @param objs The unmarshalled information.
     * @return a {@link Map} defining instance names for the unmarshalled
     * objects.
     */
    public static <T> Map<String, T> toMap(Collection<PropertyObject<T>> objs) {
        return toMap(objs.stream());
    }

    /**
     * Creates a {@link Map} defining instance names for unmarshalled objects.
     *
     * @param <T> Type of the objects.
     * @param stream The unmarshalled information.
     * @return a {@link Map} defining instance names for the unmarshalled
     * objects.
     */
    private static <T> Map<String, T> toMap(Stream<PropertyObject<T>> stream) {
        return stream.collect(HashMap::new,
                (map, obj) -> map.put(obj.getInstanceName(), obj.getObject()),
                Map::putAll);
    }

    /**
     *
     * @return The class of the unmarshalled object.
     */
    public Class<T> getType() {
        return type;
    }

    /**
     *
     * @return The unmarshalled object.
     */
    public T getObject() {
        return object;
    }

    /**
     *
     * @return The name for the unmarshalled object.
     */
    public String getInstanceName() {
        return instanceName;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.type);
        hash = 83 * hash + Objects.hashCode(this.instanceName);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PropertyObject<?> other = (PropertyObject<?>) obj;
        if (!Objects.equals(this.instanceName, other.instanceName)) {
            return false;
        }
        return Objects.equals(this.type, other.type);
    }

    @Override
    public String toString() {
        return String.format(TO_STRING_FORMAT, type, object, instanceName);
    }

}
