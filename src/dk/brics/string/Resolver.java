
package dk.brics.string;

import soot.SootMethod;
import soot.jimple.FieldRef;
import soot.jimple.InvokeExpr;

/** Callback interface to allow the string analysis to model some
 *  external methods and fields more precisely.
 *  <p>
 *  A resolver is added to the current list of active resolvers by
 *  the {@link dk.brics.string.StringAnalysis#addResolver StringAnalysis.addResolver} method.
 *  Every time the analysis encounters a reference to a field or method
 *  which it does not know about, it will ask all active resolvers (in the
 *  order in which they were added) for information about the field or method.
 */
public interface Resolver {

    /** Called by the string analysis when it encounters a call of an
     *  unknown method in a non-application class.
     *  <p>
     *  Either an {@link dk.brics.automaton.Automaton} or a {@link soot.ValueBox}
     *  is returned.
     *  The language of the returned automaton must contain all possible return values of
     *  the given target method called with the given arguments.<br>
     *  A value box will indicate that the return value of the method is equivalent to
     *  the value in the value box.<br>
     *  If nothing is known about the method, <code>null</code> should be returned.
     *  <p>
     *  If the return type is an array (of any dimension) of {@link java.lang.String},
     *  &quot;all possible return values&quot; means all possible values of any strings
     *  contained in the array.
     *  <p>
     *  If the return type is {@link java.lang.StringBuffer} or array (of any dimension)
     *  of {@link java.lang.String}, the object returned must never again be
     *  modified or returned by the unknown part of the program. If such modification or
     *  reuse could take place, <code>null</code> should be returned.
     *  @param expr the invoke expression that calls the unknown method.
     *  @param target the unknown method.
     *  @return a description of the possible return values, or <code>null</code>.
     */
    public Object resolveMethod(InvokeExpr expr, SootMethod target);

    /** Called by the string analysis when it encounters a reference of an
     *  unknown field in a non-application class.
     *  <p>
     *  Either an {@link dk.brics.automaton.Automaton} or a {@link soot.ValueBox}
     *  is returned.
     *  The language of the returned automaton must contain all possible return values of
     *  the given field.<br>
     *  A value box will indicate that the value of the field is equivalent to
     *  the value in the value box.<br>
     *  If nothing is known about the field, <code>null</code> should be returned.
     *  <p>
     *  If the type of the field is an array (of any dimension) of {@link java.lang.String},
     *  &quot;all possible values&quot; means all possible values of any strings
     *  contained in the array.
     *  <p>
     *  If the type of the field is {@link java.lang.StringBuffer} or array (of any dimension)
     *  of {@link java.lang.String}, the object contained in the field must be effectively constant.
     *  It must never be modified or returned by the unknown part of the program. If such modification or
     *  reuse could take place, <code>null</code> should be returned.
     *  @param expr the field reference.
     *  @return a description of the possible values, or <code>null</code>.
     */
    public Object resolveField(FieldRef expr);
}
