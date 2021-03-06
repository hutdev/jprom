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
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

/**
 * Converts Java objects into property file output.
 *
 * @author <a href="mailto:hutdevelopment@gmail.com">hutdev</a>
 */
public class PropertyMarshaller extends PropertyProcessor {

    /**
     * Default comment written to the properties output.
     */
    private static final String DEFAULT_COMMENT = "Properties serialized by jprom (https://github.com/hutdev/jprom)";
    /**
     * Prefix for getter methods.
     */
    private static final String GETTER_PREFIX = "get";
    /**
     * The output where the properties will be written to.
     */
    private final OutputStream output;
    /**
     * ID generator used when instance names are not provided.
     */
    private final AtomicInteger idGenerator = new AtomicInteger();

    /**
     * Creates a new instance of <code>PropertyMarshaller</code>.
     *
     * @param output The output where the marshalled properties will be written
     * to.
     */
    public PropertyMarshaller(OutputStream output) {
        this.output = output;
    }

    /**
     * Uses the getter method for a field to retrieve its value in an object and
     * converts it to its {@link String} representation using the annotated
     * {@link FieldTypeConverter} implementation. This method uses the
     * <code>toString()</code> method of the object to get its {@link String}
     * representation if no converter has been defined.
     *
     * @param instance The object where the value will be obtained.
     * @param field The field containing the value.
     * @return The <code>String</code> representation of the field value.
     * @throws ReflectiveOperationException Could not perform the operation.
     */
    private String getFieldValue(Object instance, PropertyField field)
            throws ReflectiveOperationException {
        final String fieldName = field.getField().getName();
        final StringBuffer getterNameBuilder = new StringBuffer(GETTER_PREFIX)
                .append(fieldName.substring(0, 1).toUpperCase());
        if (fieldName.length() > 1) {
            getterNameBuilder.append(fieldName.substring(1));
        }
        final Method getter = instance.getClass()
                .getDeclaredMethod(getterNameBuilder.toString());
        final Object rawValue = getter.invoke(instance);
        final Class<? extends FieldTypeConverter> converterClass
                = field.getConverter();
        return converterClass.equals(NoOpFieldTypeConverter.class)
                ? rawValue.toString()
                : getConverterInstance(converterClass).marshal(rawValue);
    }

    /**
     * Extracts property data from the provided objects and writes it to the
     * output.
     *
     * @param <T> Type of the serialized objects.
     * @param objects The objects which will be converted to property data.
     * @param comment Comment which will be written to the property output.
     * @return This instance of <code>PropertyMarshaller</code>.
     * @throws MultiplePropertyDefinitionException A field name was defined more
     * than once.
     * @throws ReflectionException Could not perform reflective operations
     * required to serialize the provided objects to property data.
     * @throws java.io.IOException Could not write properties to the output.
     * @see Properties#store(java.io.OutputStream, java.lang.String)
     */
    public <T> PropertyMarshaller marshal(Map<String, T> objects, String comment)
            throws MultiplePropertyDefinitionException, ReflectionException, IOException {
        if (!objects.isEmpty()) {
            final Class<T> clazz = (Class<T>) objects.values()
                    .iterator().next().getClass();
            final Map<String, PropertyField> propertyFields = getPropertyFields(clazz);
            final String rootName = getPropertyPrefix(clazz);
            //Transform objects into Properties.
            final BiConsumer<Properties, Entry<String, T>> accumulator
                    = (properties, objectEntry) -> {
                        final String instanceNameBase = rootName
                        + PROPERTY_PATH_DELIMITER + objectEntry.getKey();
                        propertyFields.entrySet().stream()
                        .forEach(fieldEntry -> {
                            final String propertyName = instanceNameBase
                                    + PROPERTY_PATH_DELIMITER
                                    + fieldEntry.getKey();
                            try {
                                final String propertyValue
                                        = getFieldValue(objectEntry.getValue(),
                                                fieldEntry.getValue());
                                properties.setProperty(propertyName, propertyValue);
                            } catch (ReflectiveOperationException ex) {
                                throw new ReflectionException(ex).forLambda();
                            }
                        });
                    };

            try {
                objects.entrySet().stream()
                        .collect(Properties::new,
                                accumulator,
                                Map::putAll)
                        .store(output, comment);
            } catch (LambdaException ex) {
                throw (ReflectionException) ex.getCause();
            }
        }
        return this;
    }

    /**
     * Extracts property data from the provided objects and writes it to the
     * output using a default property comment.
     *
     * @param <T> Type of the serialized objects.
     * @param objects The objects which will be converted to property data.
     * @return This instance of <code>PropertyMarshaller</code>.
     * @throws MultiplePropertyDefinitionException A field name was defined more
     * than once.
     * @throws ReflectionException Could not perform reflective operations
     * required to serialize the provided objects to property data.
     * @throws java.io.IOException Could not write properties to the output.
     * @see Properties#store(java.io.OutputStream, java.lang.String)
     */
    public <T> PropertyMarshaller marshal(Map<String, T> objects)
            throws MultiplePropertyDefinitionException, ReflectionException, IOException {
        return marshal(objects, DEFAULT_COMMENT);        
    }

    /**
     * Extracts property data from the provided objects and writes it to the
     * output. Instance names are automatically generated as sequential
     * integers.
     *
     * @param <T> Type of the serialized objects.
     * @param objects The objects which will be converted to property data.
     * @param comment Comment which will be written to the property output.
     * @return This instance of <code>PropertyMarshaller</code>.
     * @throws MultiplePropertyDefinitionException A field name was defined more
     * than once.
     * @throws ReflectionException Could not perform reflective operations
     * required to serialize the provided objects to property data.
     * @throws java.io.IOException Could not write properties to the output.
     * @see Properties#store(java.io.OutputStream, java.lang.String)
     */
    public <T> PropertyMarshaller marshal(Collection<T> objects, String comment)
            throws MultiplePropertyDefinitionException, ReflectionException, IOException {
        final HashMap<String, T> mappedObjects = objects.stream()
                .collect(HashMap::new, this::putAnonymousObject, Map::putAll);
        return marshal(mappedObjects, comment);
    }

    /**
     * Extracts property data from the provided objects and writes it to the
     * output using a default property comment. Instance names are automatically
     * generated as sequential integers.
     *
     * @param <T> Type of the serialized objects.
     * @param objects The objects which will be converted to property data.
     * @return This instance of <code>PropertyMarshaller</code>.
     * @throws MultiplePropertyDefinitionException A field name was defined more
     * than once.
     * @throws ReflectionException Could not perform reflective operations
     * required to serialize the provided objects to property data.
     * @throws java.io.IOException Could not write properties to the output.
     * @see Properties#store(java.io.OutputStream, java.lang.String)
     */
    public <T> PropertyMarshaller marshal(Collection<T> objects)
            throws MultiplePropertyDefinitionException, ReflectionException, IOException {
        return marshal(objects, DEFAULT_COMMENT);
    }

    /**
     * Extracts property data from the provided object and writes it to the
     * output using a default property comment. The instance name is
     * automatically generated by an integer sequence.
     *
     * @param <T> Type of the serialized object.
     * @param object The object which will be converted to property data.
     * @return This instance of <code>PropertyMarshaller</code>.
     * @throws MultiplePropertyDefinitionException A field name was defined more
     * than once.
     * @throws ReflectionException Could not perform reflective operations
     * required to serialize the provided object to property data.
     * @throws java.io.IOException Could not write properties to the output.
     * @see Properties#store(java.io.OutputStream, java.lang.String)
     */
    public <T> PropertyMarshaller marshal(T object)
            throws MultiplePropertyDefinitionException, ReflectionException, IOException {
        return marshal(object, DEFAULT_COMMENT);
    }

    /**
     * Extracts property data from the provided object and writes it to the
     * output. The instance name is automatically generated by an integer
     * sequence.
     *
     * @param <T> Type of the serialized object.
     * @param object The object which will be converted to property data.
     * @param comment Comment which will be written to the property output.
     * @return This instance of <code>PropertyMarshaller</code>.
     * @throws MultiplePropertyDefinitionException A field name was defined more
     * than once.
     * @throws ReflectionException Could not perform reflective operations
     * required to serialize the provided object to property data.
     * @throws java.io.IOException Could not write properties to the output.
     * @see Properties#store(java.io.OutputStream, java.lang.String)
     */
    public <T> PropertyMarshaller marshal(T object, String comment)
            throws MultiplePropertyDefinitionException, ReflectionException, IOException {
        final HashMap<String, T> mappedObject = new HashMap<>(1);
        putAnonymousObject(mappedObject, object);
        return marshal(mappedObject, comment);
    }

    /**
     * Extracts property data from the provided object and writes it to the
     * output using a default property comment.
     *
     * @param <T> Type of the serialized object.
     * @param name The instance name for the serialized object.
     * @param object The object which will be converted to property data.
     * @return This instance of <code>PropertyMarshaller</code>.
     * @throws MultiplePropertyDefinitionException A field name was defined more
     * than once.
     * @throws ReflectionException Could not perform reflective operations
     * required to serialize the provided object to property data.
     * @throws java.io.IOException Could not write properties to the output.
     * @see Properties#store(java.io.OutputStream, java.lang.String)
     */
    public <T> PropertyMarshaller marshal(String name, T object)
            throws MultiplePropertyDefinitionException, ReflectionException, IOException {
        return marshal(name, object, DEFAULT_COMMENT);
    }

    /**
     * Extracts property data from the provided object and writes it to the
     * output.
     *
     * @param <T> Type of the serialized object.
     * @param name The instance name for the serialized object.
     * @param object The object which will be converted to property data.
     * @param comment Comment which will be written to the property output.
     * @return This instance of <code>PropertyMarshaller</code>.
     * @throws MultiplePropertyDefinitionException A field name was defined more
     * than once.
     * @throws ReflectionException Could not perform reflective operations
     * required to serialize the provided object to property data.
     * @throws java.io.IOException Could not write properties to the output.
     * @see Properties#store(java.io.OutputStream, java.lang.String)
     */
    public <T> PropertyMarshaller marshal(String name, T object, String comment)
            throws MultiplePropertyDefinitionException, ReflectionException, IOException {
        final HashMap<String, T> mappedObject = new HashMap<>(1);
        mappedObject.put(name, object);
        return marshal(mappedObject, comment);
    }

    /**
     * Closes the {@link OutputStream} provided in the constructor.
     *
     * @throws IOException if an I/O error occurs.
     * @see OutputStream#close()
     */
    @Override
    public void close() throws IOException {
        super.close();
        output.close();
    }

    /**
     * Add an object to an object map using a sequential integer for the
     * instance name.
     *
     * @param <T> Type of the object.
     * @param map The map the object will be added to.
     * @param object The object to add.
     */
    private <T> void putAnonymousObject(Map<String, T> map, T object) {
        //Atomic integer makes sure keys are unique.
        map.put(Integer.toString(idGenerator.get()), object);
    }

}
