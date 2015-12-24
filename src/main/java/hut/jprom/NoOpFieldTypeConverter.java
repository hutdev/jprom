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
 * The default value for {@link Property#converter()} indicating that no
 * converter shall be used for processing a field. Calling
 * {@link #marshal(java.lang.Object)} or {@link #unmarshal(java.lang.String) }
 * on instances of this class will always result in
 * {@link UnsupportedOperationException}s, therefore this class is not supposed
 * to be used to actually convert objects.
 *
 * @author <a href="mailto:hutdevelopment@gmail.com">hutdev</a>
 */
class NoOpFieldTypeConverter implements FieldTypeConverter<Object> {

    private static final String EXCEPTION_MESSAGE = "This implementation is a marker class and is not supposed to be used for object conversion.";

    @Override
    public String marshal(Object obj) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
    }

    @Override
    public Object unmarshal(String str) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
    }

}
