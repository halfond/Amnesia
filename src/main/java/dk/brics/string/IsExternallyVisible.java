
package dk.brics.string;

import soot.SootMethod;

/** Defines which methods are seen by the string analysis as being
 *  accessible from unknown code.
 */
public interface IsExternallyVisible {
    /** Defines which methods are seen by the string analysis as being
     *  accessible from unknown code.
     *  The arguments to these methods can contain any values,
     *  and the return values might escape to unknown code.<p>
     *
     *  @param sm a method in the analyzed code.
     *  @return whether the method could possibly be called from unknown code.
     */
    public boolean isExternallyVisibleMethod(SootMethod sm);
}
