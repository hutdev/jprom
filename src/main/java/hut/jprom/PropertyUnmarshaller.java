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
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Converts property file input to Java objects.
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
     * Inspects and throws the causing exception of a {@link LamdaException}. If
     * the type of the causing exception is not of one of the declared thrown
     * exceptions, this method will return silently.
     *
     * @param ex The <code>LambdaException</code> to inspect.
     * @throws ReflectionException The cause of the method parameter was a
     * <code>ReflectionException</code>.
     * @throws MissingInstanceNameException The cause of the method parameter
     * was a <code>MissingInstanceNameException</code>.
     * @throws NoSuchFieldException The cause of the method parameter was a
     * <code>NoSuchFieldException</code>.
     * @throws MissingPropertyNameException The cause of the method parameter
     * was a <code>MissingPropertyNameException</code>.
     */
    private static void throwCause(LambdaException ex)
            throws ReflectionException, MissingInstanceNameException,
            NoSuchFieldException, MissingPropertyNameException {
        final JPromException cause = ex.getCause();
        if (cause instanceof ReflectionException) {
            throw (ReflectionException) ex.getCause();
        } else if (cause instanceof MissingInstanceNameException) {
            throw (MissingInstanceNameException) ex.getCause();
        } else if (cause instanceof NoSuchFieldException) {
            throw (NoSuchFieldException) ex.getCause();
        } else if (cause instanceof MissingPropertyNameException) {
            throw (MissingPropertyNameException) ex.getCause();
        }
    }

    /**
     * Uses the setter method for a field to set its value in an object. If the
     * {@link Property} annotation for the field defines a
     * {@link FieldTypeConverter}, the converter will be used to create the
     * field value from its {@link String} representation. If no
     * {@link FieldTypeConverter} has been defined for the field and the type of
     * the field is {@link String}, the value passed to this method will be used
     * without further conversion as the value for the field. If no
     * {@link FieldTypeConverter} has been defined for the field and the type of
     * the field is <strong>not</strong> {@link String}, this method will
     * attempt to use a {@link String} constructor of the field type to create
     * the value.
     *
     * @param <T> The type of the modified object.
     * @param instance The object which will be modified.
     * @param field The field which will be modified.
     * @param value The value which will be set for the field.
     * @return The modified object.
     * @throws ReflectiveOperationException Could not perform the operation.
     */
    private <T> T setFieldValue(T instance, PropertyField field, String value)
            throws ReflectiveOperationException {
        final java.lang.reflect.Field decField = field.getField();
        final String fieldName = decField.getName();
        final Class fieldType = decField.getType();
        final StringBuffer setterNameBuilder = new StringBuffer(SETTER_PREFIX)
                .append(fieldName.substring(0, 1).toUpperCase());
        if (fieldName.length() > 1) {
            setterNameBuilder.append(fieldName.substring(1));
        }
        final Method setter = instance.getClass()
                .getDeclaredMethod(setterNameBuilder.toString(), fieldType);
        final Class<? extends FieldTypeConverter> converterClass
                = field.getConverter();

        final Object fieldValue;
        if (converterClass.equals(NoOpFieldTypeConverter.class)) {
            fieldValue = fieldType.equals(String.class)
                    ? value
                    : fieldType.getConstructor(String.class).newInstance(value);
        } else {
            fieldValue = getConverterInstance(converterClass).unmarshal(value);
        }

        setter.invoke(instance, fieldValue);
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
     * @throws ReflectionException Could not perform reflective operations
     * required to deserialize the provided property data to objects.
     * @throws MissingInstanceNameException instance name could not be extracted
     * from a property name.
     * @throws NoSuchFieldException no matching field was found in a class for a
     * property name.
     * @throws MissingPropertyNameException a property field name could not be
     * extracted from a property name.
     */
    public <T> Map<String, T> unmarshal(Class<T> clazz)
            throws MultiplePropertyDefinitionException, ReflectionException,
            MissingInstanceNameException, NoSuchFieldException, MissingPropertyNameException {
        final String rootName = getPropertyPrefix(clazz);

        try {
            final Map<String, PropertyField> propertyFields = getPropertyFields(clazz);
            //Transform properties into objects.
            final BiConsumer<Map<String, T>, String> accumulator
                    = (map, pname) -> {
                        final Matcher instNameMatcher = INSTANCE_NAME.matcher(pname);
                        if (!instNameMatcher.matches()) {
                            throw new MissingInstanceNameException(pname, clazz)
                            .forLambda();
                        }
                        final Matcher propNameMatcher = PROPERTY_FIELD_NAME.matcher(pname);
                        if (!propNameMatcher.matches()) {
                            throw new MissingPropertyNameException(pname)
                            .forLambda();
                        }
                        final T instance = map.computeIfAbsent(
                                instNameMatcher.group(1),
                                str -> {
                                    try {
                                        return clazz.newInstance();
                                    } catch (ReflectiveOperationException ex) {
                                        throw new ReflectionException(ex).forLambda();
                                    }
                                });
                        final PropertyField field = propertyFields.get(propNameMatcher.group(1));
                        if (field == null) {
                            throw new NoSuchFieldException(pname, clazz)
                            .forLambda();
                        }
                        try {
                            setFieldValue(instance, field, properties.getProperty(pname));
                        } catch (ReflectiveOperationException ex) {
                            throw new ReflectionException(ex).forLambda();
                        }
                    };
            return properties.stringPropertyNames().stream()
                    .filter(pname
                            -> pname.startsWith(rootName + PROPERTY_PATH_DELIMITER))
                    .collect(HashMap::new, accumulator,
                            Map::putAll); //Instances will be overwritten when defined more than once.
        } catch (LambdaException ex) {
            throwCause(ex);
            throw new RuntimeException(ex.getCause());
        }
    }

    /**
     * Extract objects from the property data.
     *
     * @param classes Types of the objects.
     * @return The extracted objects wrapped in {@link PropertyObject}s.
     * @throws MultiplePropertyDefinitionException A field name was defined more
     * than once.
     * @throws ReflectionException Could not perform reflective operations
     * required to deserialize the provided property data to objects.
     * @throws MissingInstanceNameException instance name could not be extracted
     * from a property name.
     * @throws NoSuchFieldException no matching field was found in a class for a
     * property name.
     * @throws MissingPropertyNameException a property field name could not be
     * extracted from a property name.
     */
    public Set<PropertyObject> unmarshal(Class... classes) throws
            MultiplePropertyDefinitionException, ReflectionException,
            MissingInstanceNameException, NoSuchFieldException,
            MissingPropertyNameException {
        try {
            return Stream.of(classes)
                    .flatMap(clazz -> {
                        try {
                            final Map<String, Object> mappedObjects = unmarshal(clazz);
                            return mappedObjects.entrySet().stream().map(entry
                                    -> new PropertyObject(clazz, entry.getValue(), entry.getKey()));
                        } catch (JPromException ex) {
                            throw new LambdaException(ex);
                        }
                    })
                    .collect(Collectors.toSet());
        } catch (LambdaException ex) {
            throwCause(ex);
            throw new RuntimeException(ex.getCause());
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
        super.close();
        input.close();
    }

}
