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
package hut.jprom.example;

import hut.jprom.JPromException;
import hut.jprom.PropertyMarshaller;
import hut.jprom.PropertyObject;
import hut.jprom.PropertyUnmarshaller;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * This class demonstrates two use cases of the jprom library:
 * <ol>
 * <li>Unmarshalling an existing properties file</li>
 * <li>Marshalling two Java beans to the properties file format</li>
 * </ol>
 *
 * @author <a href="mailto:hutdevelopment@gmail.com">hutdev</a>
 */
public class Main {

    public static void main(String[] args) throws IOException, JPromException {
        System.out.println("The following objects have been deserialized from a properties file...");
        showExplicitUnmarshalling();
        System.out.println("The following objects have been deserialized from a properties file...");
        showUnmarshalling();
        System.out.println("\nThe following property output has been generated from Java objects...");
        showMarshalling();
    }

    private static void showExplicitUnmarshalling() throws IOException, JPromException {
        final String propertiesSource = "/example/example.properties";
        try (final PropertyUnmarshaller unmarshaller = new PropertyUnmarshaller(Main.class.getResourceAsStream(propertiesSource))) {
            final Map<String, Customer> customers = unmarshaller.unmarshal(Customer.class);
            System.out.println(customers);
            final Map<String, Configuration> configs = unmarshaller.unmarshal(Configuration.class);
            System.out.println(configs);
        }
    }

    private static void showUnmarshalling() throws IOException, JPromException {
        final String propertiesSource = "/example/example.properties";
        try (final PropertyUnmarshaller unmarshaller = new PropertyUnmarshaller(Main.class.getResourceAsStream(propertiesSource))) {
            final Set<PropertyObject> objects = unmarshaller.unmarshal(Customer.class, Configuration.class);
            objects.stream().filter(obj -> obj.getType() == Customer.class)
                    .forEach(System.out::println);
            objects.stream().filter(obj -> obj.getType() == Configuration.class)
                    .forEach(System.out::println);
        }
    }

    private static void showMarshalling() throws IOException, JPromException {
        final Customer charly = new Customer();
        charly.setName("Charlotte");
        charly.setPhone(6543);
        final Customer danny = new Customer();
        danny.setName("Daniel");
        danny.setPhone(987);
        final Map<String, Customer> customers = new HashMap<>(2);
        customers.put("dan", danny);
        customers.put("charlotte", charly);

        final Configuration config = new Configuration();
        config.setLocale(Locale.GERMANY);

        try (final ByteArrayOutputStream out = new ByteArrayOutputStream();
                final PropertyMarshaller marshaller = new PropertyMarshaller(out)) {
            marshaller
                    .marshal(customers)
                    .marshal("myConfig", config, "My configuration");
            System.out.println(out);
        }
    }
}
