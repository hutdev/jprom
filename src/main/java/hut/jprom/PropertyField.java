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

/**
 * The definition of a field as needed of marshaller and unmarshaller
 * operations. This includes the {@link java.lang.reflect.Field} as well as the
 * {@link FieldTypeConverter} used for marshalling and unmarshalling the field.
 *
 * @author <a href="mailto:hutdevelopment@gmail.com">hutdev</a>
 */
class PropertyField {

    private final java.lang.reflect.Field field;
    private final Class<? extends FieldTypeConverter> converter;

    PropertyField(java.lang.reflect.Field field,
            Class<? extends FieldTypeConverter> converter) {
        this.field = field;
        this.converter = converter;
    }

    java.lang.reflect.Field getField() {
        return field;
    }

    Class<? extends FieldTypeConverter> getConverter() {
        return converter;
    }

}
