package clojure_triple_quote_strings;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import clojure.lang.AFn;
import clojure.lang.IFn;
import clojure.lang.LispReader;
import clojure.lang.Util;


public class TripleQuoteStringReaderWrapper extends AFn {
    IFn wrapped_string_reader;

    TripleQuoteStringReaderWrapper(IFn wrapped_reader) {
        // normally should be an instance of clojrue.lang.LispReader.StringReader
        wrapped_string_reader = wrapped_reader;
    }

    public Object invoke(Object reader, Object doublequote, Object opts, Object pendingForms) {
        PushbackReader r = (PushbackReader) reader;
        int ch;

        /* The clojure.lang.LispReader (the caller code) is written so that a specific reader
         * is called at a point when the first character is already read.
         *
         * That is, we are here:
         *    V
         *   """some string"""
         *
         * Or perhaps here:
         *    V
         *   "some string"
         *
         * So, here we detect this last case with 1-doublequote string.
         * If yes, then we just "undo" our read (as if this wrapper method wasn't called) and pass
         * the control to the original clojure StringReader, letting it read the input as usual.
         */
        ch = read1_throwIfEof(r);
        if (ch != '"') {
            unread(r, ch);
            return wrapped_string_reader.invoke(reader, ch, opts, pendingForms);
        }

        /* Read one more char, now 2 branches are possible:
         *
         * We got the 3rd opening quote, like this:
         *     V
         *   """some string"""
         *
         * Or, we got an empty string.
         *     V
         *   ""
         *
         * (and we stole the character that comes immediately after the trailing quote)
         * So here we detect this last empty-string case, and if this is the case,
         * we put the stolen character (whatever it is) back to the input stream,
         * and return the empty string that we just read.
         */
        ch = read1(r);
        if (ch != '"') {
            unread(r, ch);
            return "";
        }

        /* Ok, at this point we know that we already got 3 opening double-quote characters.
         *
         * That is, we are here:
         *     V
         *  """some string"""
         *
         * So here we may start a loop that captures input until it reaches 3 closing quotes.
         *
         * One little trick is that the wrapped StringReader will stop as soon as it reaches
         * a double quote, like here:
         *            V
         *   """some "example" string"""
         *
         * In such cases, we just continue calling it in a loop, concatenating all inner strings
         * into one bigger string.
         */
        StringBuilder sb = new StringBuilder();

        for(; ;) {
            Object piece_of_string = wrapped_string_reader.invoke(r, (Object)ch, opts, pendingForms);
            sb.append((String)piece_of_string);

            /* Detect if StringReader above stopped after a 1-doublequote character, like this:
             *            V
             *   """some " string"""
             */
            ch = read1_throwIfEof(r);
            if (ch != '"') {
                sb.append('"');
                unread(r, ch);
                continue;
            }

            /* Also, detect this position:
             *             V
             *  """ some "" string"""
             */
            ch = read1_throwIfEof(r);
            if (ch != '"') {
                sb.append('"');
                sb.append('"');
                unread(r, ch);
                continue;
            }

            /* Ok, finally, here we know that we just read 3 subsequent double-quotes:
             *  the 1st comes from the StringReader that terminates after the closing quote
             *  the other 2 double-quote characters come from the read1() calls above
             *
             * So, since we just read 3 closing quotes, we may break out of the loop
             * and return the string concatenate from the all StringReader.invoke() calls above.
             */
            break;
        }

        return sb.toString();
    }


    /* Helpers */

    static void throwEofError() {
        throw Util.runtimeException("EOF while reading triple-quoted string");
    }

    static Boolean isEof(int ch) {
        return (ch < 0);
    }

    static void throwErrorIfEof(int ch) {
        if (isEof(ch)) {
            throwEofError();
        }
    }

    static int read1_throwIfEof(Reader r) {
        int ch = read1(r);
        throwErrorIfEof(ch);
        return ch;
    }


    /* Reading helpers that mimic the original clojure.lang.LispReader code */

    /* reads 1 character from the input stream. */
    static int read1(Reader r) {
        return LispReader.read1(r);
    }

    /* puts 1 character back into the input stream.
     *
     * Unlike read1() above, the unread() method is not public in the original LispReader class
     * So here we have to add some similar code. */
    static void unread(PushbackReader r, int ch) {
        if(ch != -1) {
            try {
                r.unread(ch);
            } catch(IOException e) {
                throw Util.sneakyThrow(e);
            }
        }
    }



}
