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

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Base functionality for classes processing property data.
 *
 * @author <a href="mailto:hutdevelopment@gmail.com">hutdev</a>
 */
abstract class PropertyProcessor implements Closeable {

    /**
     * Path delimiter in property names.
     */
    static final String PROPERTY_PATH_DELIMITER = ".";
    /**
     * Regular expression to determine whether a {@link String} is empty or
     * consists of whitespace only.
     */
    private static final String REGEX_BLANK_STRING = "^\\s*$";
    /*
     * Instances of the converters used by this processor.
     */
    private final Map<Class<? extends FieldTypeConverter>, FieldTypeConverter> converters = new HashMap<>();

    /**
     * Computes the prefix for properties pertaining to the specified class.
     *
     * @param clazz The class containing the fields the properties are mapped
     * to.
     * @return The common prefix of properties mapped to the specified class.
     */
    static String getPropertyPrefix(Class<?> clazz) {
        final PropertyRoot propertyRoot
                = clazz.getDeclaredAnnotation(PropertyRoot.class);
        return (propertyRoot != null && !propertyRoot.name().matches(REGEX_BLANK_STRING))
                ? propertyRoot.name()
                : clazz.getSimpleName();
    }

    /**
     * Finds the fields relevant for processing and determines the field names
     * used in the property definitions.
     *
     * @param clazz The class defining the property fields.
     * @return A mapping of the field names to the fields.
     * @throws MultiplePropertyDefinitionException A field name was defined more
     * than once.
     */
    static Map<String, PropertyField> getPropertyFields(Class<?> clazz)
            throws MultiplePropertyDefinitionException {
        try {
            return Stream.of(clazz.getDeclaredFields())
                    .collect(HashMap::new,
                            (map, decField) -> {
                                final Property pdef = decField.getAnnotation(Property.class);
                                if (pdef != null) {
                                    final String pname
                                    = pdef.name().matches(REGEX_BLANK_STRING)
                                    ? decField.getName()
                                    : pdef.name();
                                    if (map.containsKey(pname)) {
                                        throw new MultiplePropertyDefinitionException(pname, clazz)
                                        .forLambda();
                                    }
                                    map.put(pname,
                                            new PropertyField(decField, pdef.converter()));
                                }
                            },
                            (map1, map2) -> {
                                if (!Collections.disjoint(map1.keySet(), map2.keySet())) {
                                    for (final Iterator<String> i = map2.keySet().iterator();
                                    i.hasNext();) {
                                        final String pname = i.next();
                                        if (map1.containsKey(pname)) {
                                            throw new MultiplePropertyDefinitionException(pname, clazz)
                                            .forLambda();
                                        }
                                    }
                                }
                                map1.putAll(map2);
                            });
        } catch (LambdaException ex) {
            throw (MultiplePropertyDefinitionException) ex.getCause();
        }
    }

    /**
     * Produces an instance of the passed subtype of {@link FieldTypeConverter}.
     * Since the converter mechanism is stateless, the instances can be cached.
     * If the cache already contains an instance of the class passed to this
     * method, the cached instance will be returned. Otherwise a new intance
     * will be created and added to the cache.
     *
     * @param converterClass Class of the required converter instance.
     * @return The converter instance.
     * @throws ReflectiveOperationException Could not create a new instance of
     * the converter class.
     */
    FieldTypeConverter getConverterInstance(Class<? extends FieldTypeConverter> converterClass)
            throws ReflectiveOperationException {
        try {
            final FieldTypeConverter converter = converters.computeIfAbsent(
                    converterClass, (convClass) -> {
                        try {
                            return convClass.newInstance();
                        } catch (ReflectiveOperationException ex) {
                            throw new ReflectionException(ex).forLambda();
                        }
                    });
            return converter;
        } catch (LambdaException ex) {
            throw ((ReflectiveOperationException) ex.getCause().getCause());
        }
    }

    /**
     * Clears the cached instances of {@link FieldTypeConverter} managed by
     * {@link #getConverterInstance(java.lang.Class)}.
     *
     * @throws IOException Never thrown in this implementation.
     */
    @Override
    public void close() throws IOException {
        converters.clear();
    }

}
