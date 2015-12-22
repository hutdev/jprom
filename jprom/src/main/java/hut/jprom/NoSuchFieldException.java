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
 * Used when no matching field was found in a class for a property name.
 *
 * @author <a href="mailto:hutdevelopment@gmail.com">hutdev</a>
 */
public class NoSuchFieldException extends JPromException {

    /**
     * Error message format.
     */
    private static final String ERROR_NO_SUCH_FIELD = "No field found for property %s in class %s";
    private static final long serialVersionUID = -3213472242228850407L;

    /**
     * Creates a new instance of <code>NoSuchFieldException</code>.
     *
     * @param pname The property name.
     * @param clazz The class which is supposed to be mapped to the property.
     */
    NoSuchFieldException(String pname, Class clazz) {
        super(String.format(ERROR_NO_SUCH_FIELD, pname, clazz));
    }
}
