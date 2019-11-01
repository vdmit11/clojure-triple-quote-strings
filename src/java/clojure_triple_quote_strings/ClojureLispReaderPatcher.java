package clojure_triple_quote_strings;

import java.lang.reflect.Field;
import clojure.lang.IFn;
import clojure.lang.LispReader;
import clojure.lang.Util;

public class ClojureLispReaderPatcher {


    public static void replaceStringReader() {
        /* Here we basically do:
         *    LispReader['"'] = wrapper(LispReader['"'])
         *
         * where wrapper is our custom class that "strips" triple quotes from string literals,
         * passing the inner string to the original wrapped reader
         */
        IFn[] macros = getReaderMacros();
        IFn wrapped_reader = macros['"'];
        macros['"'] = new TripleQuoteStringReaderWrapper(wrapped_reader);
    }

    /* That basically returns the `LispReader.macros` array.
     *
     * The `LispReader.macros` isn't public, so reflection is used reach it.
     * Actually it could be done by defining a class in the `clojure.lang` package,
     * but I don't want to pollute the `clojure.lang`, so reflection seems the least evil to me.
     */
    public static IFn[] getReaderMacros() {
        try {
            Field macros_field = LispReader.class.getDeclaredField("macros");
            macros_field.setAccessible(true);
            IFn[] macros = (IFn[])macros_field.get(null);
            return macros;
        } catch (NoSuchFieldException e) {
            // may happen if `clojure.lang.LispReader.macros` is re-named
            throw Util.sneakyThrow(e);
        } catch (IllegalAccessException e) {
            // shall never happen, as we set .setAccessible(true) above
            throw Util.sneakyThrow(e);
        }
    }


}
