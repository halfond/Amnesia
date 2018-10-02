package dk.brics.string.runtime;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import dk.brics.automaton.Automaton;

/** 
 * Runtime system. 
 * @author Anders M&oslash;ller &lt;<a href="mailto:amoeller@brics.dk">amoeller@brics.dk</a>&gt;
 */
public class RegExp
{
    static Map automata = new HashMap();

    private RegExp() {}

    /**
     * Casts string to regular language specified by regular expression.
     * @param s string
     * @param regexp regular expression written in the full syntax of 
     *               {@link dk.brics.automaton.RegExp dk.brics.automaton.RegExp}.
     * @return the string <tt>s</tt>
     * @exception ClassCastException if the string is not in the given regular language
     * @exception IllegalArgumentException if the regular expression uses an unbound identifier
     */
    public static String cast(String s, String regexp)
	throws IllegalArgumentException
    {
	if (!check(s, regexp))
	    throw new ClassCastException("string does not match regular expression");
	return s;
    }

    /**
     * Casts string to regular language specified by serialized <code>Automaton</code> located by URL.
     * @param s string
     * @param url URL of serialized automaton
     * @return the string <tt>s</tt>
     * @exception IOException if unserialization errors occurred
     * @exception ClassCastException if the string is not in the given regular language
     */
    public static String cast(String s, URL url)
	throws IOException
    {
	if (!check(s, url))
	    throw new ClassCastException("string does not match regular expression");
	return s;
    }

    /**
     * Instructs the string analyzer to verify that the string is in the regular language
     * specified by the regular expression.
     * @param s string
     * @param regexp regular expression written in the full syntax of 
     *               {@link dk.brics.automaton.RegExp dk.brics.automaton.RegExp}.
     * @return the string <tt>s</tt>
     * @exception IllegalArgumentException if the regular expression uses an unbound identifier
     */
    public static String analyze(String s, String regexp)
	throws IllegalArgumentException
    {
	return s;
    }

    /**
     * Instructs the string analyzer to verify that the string is in the regular language
     * specified by a serialized <code>Automaton</code> located by a URL.
     * @param s string
     * @param url URL of serialized automaton
     * @return the string <tt>s</tt>
     * @exception IOException if unserialization errors occurred
     */
    public static String analyze(String s, URL url)
    {
	return s;
    }

    /**
     * Checks that string is in regular language specified by regular expression.
     * @param s string
     * @param regexp regular expression written in the full syntax of 
     *               {@link dk.brics.automaton.RegExp dk.brics.automaton.RegExp}.
     * @return true if the string is in the given regular language
     * @exception IllegalArgumentException if the regular expression uses an unbound identifier
     */
    public static boolean check(String s, String regexp)
	throws IllegalArgumentException
    {
	return (new dk.brics.automaton.RegExp(regexp)).toAutomaton(automata).run(s);
    }

    /**
     * Checks that string is in regular language specified by 
     * serialized <code>Automaton</code> located by URL.
     * @param s string
     * @param url URL of serialized automaton
     * @return true if the string is in the given regular language
     * @exception IOException if unserialization errors occurred
     */
    public static boolean check(String s, URL url)
	throws IOException
    {
	try {
	    return Automaton.load(url).run(s);
	} catch (Exception e) {
	    throw new IOException("automaton load failed");
	}
    }

    /**
     * Binds regular language to identifier. 
     * Subsequent uses of regular expressions may then use the identifier.
     * @param id identifier
     * @param regexp regular expression written in the full syntax of 
     *               {@link dk.brics.automaton.RegExp dk.brics.automaton.RegExp}.
     * @exception IllegalArgumentException if the identifier already has been bound
     *            or the regular expression uses an unbound identifier
     */
    public static void bind(String id, String regexp)
	throws IllegalArgumentException
    {
	if (automata.containsKey(id))
	    throw new IllegalArgumentException("identifier '"+id+"' already bound");
	automata.put(id, (new dk.brics.automaton.RegExp(regexp)).toAutomaton(automata));
    }

    /**
     * Binds regular language to identifier. 
     * Subsequent uses of regular expressions may then use the identifier.
     * @param id identifier
     * @param url URL of serialized automaton
     * @exception IOException if unserialization errors occurred
     * @exception IllegalArgumentException if the identifier already has been bound
     *            or the regular expression uses an unbound identifier
     */
    public static void bind(String id, URL url)
	throws IOException, IllegalArgumentException
    {
	if (automata.containsKey(id))
	    throw new IllegalArgumentException("identifier '"+id+"' already bound");
	try {
	    automata.put(id, Automaton.load(url));
	} catch (Exception e) {
	    throw new IOException("automaton load failed");
	}
    }
}
