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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field which will be represented in a properties file.
 *
 * @author <a href="mailto:hutdevelopment@gmail.com">hutdev</a>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Property {

    /**
     * Optional name of the field as it will be represented in a properties
     * file. If left to default, the declared name of the annotated field will
     * be used.
     *
     * @return The property name of the field.
     */
    String name() default "";

    /**
     * Optional definition of a {@link FieldTypeConverter} allowing a custom
     * definition of how a field value is marshalled and unmarshalled. If left
     * to default, the <code>toString()</code> method will be used for
     * marshalling and the <code>String</code> constructor will be used for
     * unmarshalling.
     *
     * @return The class of the converter implementation.
     */
    Class<? extends FieldTypeConverter> converter() default NoOpFieldTypeConverter.class;
}
