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
 * Allows custom converter implementations to define how fields will be
 * marshalled and unmarshalled. Implementations of this interface must make sure
 * that the following condition is met for an object <code>obj</code> of an
 * arbitrary type:  <code>
 * obj.equals(unmarshal(marshal(obj))) == true;
 * </code>
 *
 * @author <a href="mailto:hutdevelopment@gmail.com">hutdev</a>
 * @param <T> The target type of the field.
 */
public interface FieldTypeConverter<T> {

    /**
     * Implementation of the conversion from a custom type to its {@link String}
     * representation.
     *
     * @param obj an instance of the target type.
     * @return the <code>String</code> representation of the method parameter.
     */
    String marshal(T obj);

    /**
     * Implementation of the conversion from a {@link String} to an instance of
     * a custom type.
     *
     * @param str the <code>String</code> representation of an instance of a
     * custom type.
     * @return An instance of a custom type as defined by the method parameter.
     */
    T unmarshal(String str);
}
