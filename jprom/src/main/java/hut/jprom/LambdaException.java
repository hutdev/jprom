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
 * Auxiliary exception type for handling checked exceptions in lambda
 * expressions. The implementation makes sure that the causing exception will
 * always be of the type {@link JPromException}.
 *
 * @author hutdev <hutdevelopment@gmail.com>
 */
class LambdaException extends RuntimeException {

    private static final long serialVersionUID = -6009150692817958096L;

    /**
     * Creates a new instance of <code>LambdaException</code> with a causing
     * checked exception. If the causing exception which is passed to this
     * constructor is a {@link JPromException}, the exception will be used
     * directly as the causer. Any other exception types will be wrapped in a
     * {@link JPromException} instance.
     *
     * @param cause the cause for the exception.
     */
    LambdaException(Exception cause) {
        super(cause instanceof JPromException
                ? cause
                : new JPromException(cause));
    }

    /**
     * Creates a new instance of <code>LambdaException</code> with an exception
     * message. The message will be wrapped in an instance of
     * {@link JPromException}.
     *
     * @param message The exception message.
     */
    LambdaException(String message) {
        this(new JPromException(message));
    }

    /**
     * Creates a new instance of <code>LambdaException</code> with an exception
     * message. The message will be wrapped in an instance of
     * {@link JPromException}. This constructor makes use of
     * {@link String#format(java.lang.String, java.lang.Object...)} and will
     * pass its arguments to said method to construct the exception message.
     *
     * @param msgFormat Format for the exception message.
     * @param arguments Arguments for the exception message format.
     * @see String#format(java.lang.String, java.lang.Object...)
     */
    LambdaException(String msgFormat, Object... arguments) {
        this(String.format(msgFormat, arguments));
    }

    @Override
    public synchronized JPromException getCause() {
        return (JPromException) super.getCause();
    }

}
