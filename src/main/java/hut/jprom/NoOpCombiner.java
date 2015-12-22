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

import java.util.Map;
import java.util.Properties;

/**
 * Sequential streams do not need a combiner for collect operations, therefore
 * this class provides combiner methods that do not do anything.
 * <strong>Do not use the combiner methods provided by this class in collect
 * operations on parallel streams!</strong>
 *
 * @author <a href="mailto:hutdevelopment@gmail.com">hutdev</a>
 */
class NoOpCombiner {

    /**
     * Combiner method that does not do anything.
     *
     * @param t
     * @param u
     */
    static void combineMaps(Map t, Map u) {
    }

    /**
     * Combiner method that does not do anything.
     *
     * @param t
     * @param u
     */
    static void combineProperties(Properties t, Properties u) {
    }
}
