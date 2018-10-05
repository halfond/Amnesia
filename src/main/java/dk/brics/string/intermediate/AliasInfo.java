
package dk.brics.string.intermediate;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/** Alias information for a specific program point. */
public class AliasInfo {
    private Map/*<Variable,Set<Variable>>*/ aliases;
    private Map/*<Variable,Set<Variable>>*/ maybe;
    private Set/*<Variable>*/ corrupted;
    private Set/*<Variable>*/ live;

    AliasInfo(Set/*<Variable>*/ live) {
	this.live = live;
	aliases = new HashMap();
	maybe = new HashMap();
	corrupted = new HashSet();
    }

    /** Merge this element with the given. */
    boolean mergeIdentity(AliasInfo other) {
	boolean changed = false;
	Iterator vi1 = other.aliases.keySet().iterator();
	while (vi1.hasNext()) {
	    Variable v1 = (Variable)vi1.next();
	    if (other.live.contains(v1)) {
		// Join v1
		Set al = new HashSet((Set)other.aliases.get(v1));
		Set ml = new HashSet((Set)other.maybe.get(v1));
		al.retainAll(other.live);
		ml.retainAll(other.live);
		if (!aliases.containsKey(v1)) {
		    // Merge with bottom element
		    aliases.put(v1, al);
		    maybe.put(v1, ml);
		    changed = true;
		} else {
		    // Merge two elements
		    Set ar = (Set)aliases.get(v1);
		    Set mr = (Set)maybe.get(v1);
		    Set diff1 = new HashSet(al);
		    Set diff2 = new HashSet(ar);
		    diff1.removeAll(ar);
		    diff2.removeAll(al);
		    diff1.retainAll(other.live);
		    diff2.retainAll(other.live);
		    changed |= ar.addAll(al);
		    changed |= mr.addAll(diff1);
		    changed |= mr.addAll(diff2);
		    changed |= mr.addAll(ml);
		}
	    }
	}
	return changed;
    }

    /** Merge this element with the given, except for
     *  all aliases with the given variable.
     *  Alias the given variable with itself.
     */
    boolean mergeFilter(AliasInfo other, Variable a) {
	boolean changed = false;
	Iterator vi1 = other.aliases.keySet().iterator();
	while (vi1.hasNext()) {
	    Variable v1 = (Variable)vi1.next();
	    if (other.live.contains(v1) && a != v1) {
		Set al = new HashSet((Set)other.aliases.get(v1));
		Set ml = new HashSet((Set)other.maybe.get(v1));
		al.retainAll(other.live);
		ml.retainAll(other.live);
		al.remove(a);
		ml.remove(a);
		// Join v1
		if (!aliases.containsKey(v1)) {
		    // Merge with bottom element
		    aliases.put(v1, al);
		    maybe.put(v1, ml);
		    changed = true;
		} else {
		    // Merge two elements
		    Set ar = (Set)aliases.get(v1);
		    Set mr = (Set)maybe.get(v1);
		    Set diff1 = new HashSet(al);
		    Set diff2 = new HashSet(ar);
		    diff1.removeAll(ar);
		    diff2.removeAll(al);
		    diff1.retainAll(other.live);
		    diff2.retainAll(other.live);
		    changed |= ar.addAll(al);
		    changed |= mr.addAll(diff1);
		    changed |= mr.addAll(diff2);
		    changed |= mr.addAll(ml);
		}
	    }
	}
	// Alias the filter var with itself
	if (other.live.contains(a)) {
	    if (!aliases.containsKey(a)) {
		aliases.put(a, new HashSet());
		maybe.put(a, new HashSet());
	    }
	    changed |= ((Set)aliases.get(a)).add(a);
	}
	return changed;
    }

    /** Alias a with all variables aliased with b */
    boolean mergeAssign(AliasInfo other, Variable a, Variable b) {
	boolean changed = false;
	if (other.live.contains(a) && other.aliases.containsKey(b)) {
	    Set ba = (Set)other.aliases.get(b);
	    Set bm = (Set)other.maybe.get(b);
	    ba.retainAll(other.live);
	    bm.retainAll(other.live);
	    if (!aliases.containsKey(a)) {
		aliases.put(a, new HashSet());
		maybe.put(a, new HashSet());
	    }
	    changed |= ((Set)aliases.get(a)).addAll(ba);
	    changed |= ((Set)maybe.get(a)).addAll(bm);
	    Iterator bai = ba.iterator();
	    while (bai.hasNext()) {
		Variable bav = (Variable)bai.next();
		changed |= ((Set)aliases.get(bav)).add(a);
		if (bm.contains(bav)) {
		    changed |= ((Set)maybe.get(bav)).add(a);
		}
	    }
	}
	return changed;
    }

    boolean mergeCorrupt(AliasInfo other, Variable a) {
	boolean changed = false;
	if (other.aliases.containsKey(a)) {
	    Set aa = (Set)other.aliases.get(a);
	    Iterator aai = aa.iterator();
	    while (aai.hasNext()) {
		Variable aav = (Variable)aai.next();
		if (other.live.contains(aav)) {
		    changed |= corrupted.add(aav);
		}
	    }
	}
	return changed;
    }

    boolean addAlias(Variable v1, Variable v2) {
	boolean changed = false;

	if (!aliases.containsKey(v1)) {
	    aliases.put(v1, new HashSet());
	    maybe.put(v1, new HashSet());
	}
	changed |= ((Set)aliases.get(v1)).add(v2);

	if (!aliases.containsKey(v2)) {
	    aliases.put(v2, new HashSet());
	    maybe.put(v2, new HashSet());
	}
	changed |= ((Set)aliases.get(v2)).add(v1);

	return changed;
    }

    boolean addMaybe(Variable v1, Variable v2) {
	boolean changed = false;
	changed |= ((Set)maybe.get(v1)).add(v2);
	changed |= ((Set)maybe.get(v2)).add(v1);
	return changed;
    }

    boolean corrupt(Variable v) {
	return corrupted.add(v);
    }

    /** Returns the set of variables possibly aliased to some
     *  other variables.
     *  @return a set of {@link dk.brics.string.intermediate.Variable} objects.
     */
    public Set/*<Variable>*/ getAliasedVars() {
	return aliases.keySet();
    }

    /** Returns the set of variables possibly aliased to the given variable.
     *  @return a set of {@link dk.brics.string.intermediate.Variable} objects.
     */
    public Set/*<Variable>*/ getAliasesFor(Variable v) {
	if (aliases.containsKey(v)) {
	    return (Set)aliases.get(v);
	} else {
	    return Collections.EMPTY_SET;
	}
    }

    /** Returns the set of variables possibly aliased and possibly not aliased
     *  to the given variable.
     *  @return a set of {@link dk.brics.string.intermediate.Variable} objects.
     */
    public Set/*<Variable>*/ getMaybeFor(Variable v) {
	if (maybe.containsKey(v)) {
	    return (Set)maybe.get(v);
	} else {
	    return Collections.EMPTY_SET;
	}
    }

    /** Returns whether or not this variable is corrupt.
     *  @param v the variable.
     *  @return <code>true</code> if the variable is corrupt,
     *          <code>false</code> otherwise.
     */
    public boolean isCorrupt(Variable v) {
	return corrupted.contains(v);
    }

}
