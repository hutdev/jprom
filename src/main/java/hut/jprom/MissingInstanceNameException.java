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
 * Used when an instance name could not be extracted from a property name.
 *
 * @author <a href="mailto:hutdevelopment@gmail.com">hutdev</a>
 */
public class MissingInstanceNameException extends JPromException {

    /**
     * Error message format.
     */
    private static final String ERROR_NO_INSTANCE_NAME = "Cannot extract instance name from %s for class %s";
    private static final long serialVersionUID = -6572411577356602963L;

    /**
     * Creates a new instance of <code>MissingInstanceNameException</code>.
     *
     * @param pname Property identifier.
     * @param clazz Class referenced in pname.
     */
    MissingInstanceNameException(String pname, Class clazz) {
        super(String.format(ERROR_NO_INSTANCE_NAME, pname, clazz));
    }

}
