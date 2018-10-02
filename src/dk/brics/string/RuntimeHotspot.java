
package dk.brics.string;

import soot.ValueBox;
import dk.brics.automaton.Automaton;

/** A representation of a hotspot in the analysis of a program
 *  using the string analysis runtime library.
 *  Each <code>RuntimeHotspot</code> object corresponds to a specific
 *  call of the {@link dk.brics.string.runtime.RegExp#analyze RegExp.analyze} or
 *  {@link dk.brics.string.runtime.RegExp#check RegExp.check} runtime method.
 *  @see dk.brics.string.runtime.RegExp
 */
public class RuntimeHotspot {
    /** The actual string expression to analyze. */
    public ValueBox spot;
    /** The expected analysis result for the expression.
     *  This corresponds to the regular expression or automaton
     *  given in the runtime method call. */
    public Automaton expected;
    /** There are two kinds of hotspots, originating from
     *  two different runtime methods.
     *  <ul>
     *   <li>{@link #KIND_ANALYZE} hotspots originate from
     *       {@link dk.brics.string.runtime.RegExp#analyze RegExp.analyze} calls.</li>
     *   <li>{@link #KIND_CHECK} hotspots originate from
     *       {@link dk.brics.string.runtime.RegExp#check RegExp.check} calls.</li>
     *  </ul>
     */
    public int kind;

    /** A value for the {@link #kind} field indicating that this
     *  hotspot originated from a {@link dk.brics.string.runtime.RegExp#analyze RegExp.analyze} call.
     */
    public static final int KIND_ANALYZE = 1;
    /** A value for the {@link #kind} field indicating that this
     *  hotspot originated from a {@link dk.brics.string.runtime.RegExp#check RegExp.check} call.
     */
    public static final int KIND_CHECK = 2;

    RuntimeHotspot(ValueBox spot, Automaton expected, int kind) {
	this.spot = spot;
	this.expected = expected;
	this.kind = kind;
    }

}
