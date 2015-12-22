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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts property file input to Java objects. TODO: Use asm for faster
 * analysis of annotated types and fields.
 *
 * @author <a href="mailto:hutdevelopment@gmail.com">hutdev</a>
 */
public class PropertyUnmarshaller extends PropertyProcessor {

    /**
     * Regular expression pattern to extract the class instance name from a
     * property name.
     */
    private static final Pattern INSTANCE_NAME = Pattern.compile("^[^.]+\\.([^.]+)\\..+$");
    /**
     * Regular expression pattern to extract the property field name from a
     * property name.
     */
    private static final Pattern PROPERTY_FIELD_NAME = Pattern.compile("^[^.]+\\.[^.]+\\.(.+)$");
    /**
     * Prefix of setter methods.
     */
    private static final String SETTER_PREFIX = "set";
    /**
     * Error message format used when no matching field was found in a class for
     * a property name.
     */
    private static final String ERROR_NO_SUCH_FIELD = "No field found for property %s in class %s";
    /**
     * Error message format used when a property field name could not be
     * extracted from a property name.
     */
    private static final String ERROR_NO_PROPERTY_NAME = "Cannot extract property name from %s";
    /**
     * Error message format used when an instance name could not be extracted
     * from a property name.
     */
    private static final String ERROR_NO_INSTANCE_NAME = "Cannot extract instance name from %s for class %s";
    /**
     * Property input.
     */
    private final InputStream input;
    /**
     * The properties which will be unmarshalled.
     */
    private final Properties properties;

    /**
     * Creates a new instance of <code>PropertyUnmarshaller</code>.
     *
     * @param input The source of the property data.
     * @throws IOException Could not read or parse the property data.
     * @see Properties#load(java.io.InputStream)
     */
    public PropertyUnmarshaller(InputStream input) throws IOException {
        this.input = input;
        properties = new Properties();
        properties.load(input);
    }

    /**
     * Uses the setter method for a field to set its value in an object. If the
     * type of the field which will be set is not {@link String} this method
     * will use the {@link String} constructor of the field type to convert the
     * value.
     *
     * @param <T> The type of the modified object.
     * @param instance The object which will be modified.
     * @param field The field which will be modified.
     * @param value The value which will be set for the field.
     * @return The modified object.
     * @throws ReflectiveOperationException Could not perform the operation.
     */
    private static <T> T setFieldValue(T instance, Field field, String value)
            throws ReflectiveOperationException {
        final String fieldName = field.getName();
        final Class fieldType = field.getType();
        final StringBuffer setterNameBuilder = new StringBuffer(SETTER_PREFIX)
                .append(fieldName.substring(0, 1).toUpperCase());
        if (fieldName.length() > 1) {
            setterNameBuilder.append(fieldName.substring(1));
        }
        final Method setter = instance.getClass()
                .getDeclaredMethod(setterNameBuilder.toString(), fieldType);

        setter.invoke(instance, fieldType.equals(String.class)
                ? value
                : fieldType.getConstructor(String.class).newInstance(value));
        return instance;
    }

    /**
     * Extract objects from the property data.
     *
     * @param <T> Type of the objects.
     * @param clazz Type of the objects.
     * @return A mapping of the instance names to their object instances.
     * @throws MultiplePropertyDefinitionException A field name was defined more
     * than once.
     * @throws JPromException Could not construct objects of the specified type
     * from the input data.
     */
    public <T> Map<String, T> unmarshal(Class<T> clazz)
            throws MultiplePropertyDefinitionException, JPromException {
        final String rootName = getPropertyPrefix(clazz);

        try {
            final Map<String, Field> propertyFields = getPropertyFields(clazz);
            //Transform properties into objects.
            final BiConsumer<Map<String, T>, String> accumulator
                    = (map, pname) -> {
                        final Matcher instNameMatcher = INSTANCE_NAME.matcher(pname);
                        if (!instNameMatcher.matches()) {
                            throw new LambdaException(ERROR_NO_INSTANCE_NAME,
                                    pname, clazz);
                        }
                        final Matcher propNameMatcher = PROPERTY_FIELD_NAME.matcher(pname);
                        if (!propNameMatcher.matches()) {
                            throw new LambdaException(ERROR_NO_PROPERTY_NAME,
                                    pname);
                        }
                        final T instance = map.computeIfAbsent(
                                instNameMatcher.group(1),
                                str -> {
                                    try {
                                        return clazz.newInstance();
                                    } catch (ReflectiveOperationException ex) {
                                        throw new LambdaException(ex);
                                    }
                                });
                        final Field field = propertyFields.get(propNameMatcher.group(1));
                        if (field == null) {
                            throw new LambdaException(ERROR_NO_SUCH_FIELD,
                                    pname, clazz);
                        }
                        try {
                            setFieldValue(instance, field, properties.getProperty(pname));
                        } catch (ReflectiveOperationException ex) {
                            throw new LambdaException(ex);
                        }
                    };
            return properties.stringPropertyNames().stream()
                    .filter(pname
                            -> pname.startsWith(rootName + PROPERTY_PATH_DELIMITER))
                    .collect(HashMap::new,
                            accumulator,
                            NoOpCombiner::combineMaps);
        } catch (LambdaException ex) {
            throw ex.getCause();
        }
    }

    /**
     * Closes the {@link InputStream} provided in the constructor.
     *
     * @throws IOException if an I/O error occurs.
     * @see InputStream#close()
     */
    @Override
    public void close() throws IOException {
        input.close();
    }

}
