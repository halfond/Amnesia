
package dk.brics.string;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.ValueBox;
import soot.jimple.FieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.toolkits.graph.CompleteUnitGraph;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;

/** Encapsulation of the analysis of a program
 *  using the string analysis runtime library.
 *  <p>
 *  This class serves two purposes:
 *  <ul>
 *   <li>It implements the {@link dk.brics.string.Resolver} interface to
 *       identify {@link dk.brics.string.runtime.RegExp#cast RegExp.cast}
 *       calls and tell the string analysis about the assumption.</li>
 *   <li>It locates all hotspots, indicated by
 *       {@link dk.brics.string.runtime.RegExp#analyze RegExp.analyze} and
 *       {@link dk.brics.string.runtime.RegExp#check RegExp.check} calls.
 *  </ul>
 *  <p>
 *  The following code will analyze a program with respect to the runtime methods.
 *  The resolver is added to the list of active resolvers to make the analysis aware
 *  of the results of {@link dk.brics.string.runtime.RegExp#cast RegExp.cast} calls,
 *  and the hotspots used in the analysis are the expressions occurring as the first
 *  argument to {@link dk.brics.string.runtime.RegExp#analyze RegExp.analyze} and
 *  {@link dk.brics.string.runtime.RegExp#check RegExp.check} calls.
 *  <pre>
 *   RuntimeResolver rr = new RuntimeResolver();
 *   StringAnalysis.addResolver(rr);
 *   StringAnalysis sa = new StringAnalysis(rr.getHotspotExps());
 *  </pre>
 *  The {@link dk.brics.string.StringAnalysis} object <code>sa</code> will now contain
 *  the results of the analysis. Run through the set of {@link dk.brics.string.RuntimeHotspot}
 *  objects returned by the {@link dk.brics.string.RuntimeResolver#getHotspots getHotspots}
 *  method and compare the result given by the string analysis to the expected result given
 *  for the hotspot.
 *  @see dk.brics.string.StringAnalysis#addResolver StringAnalysis.addResolver
 *  @see dk.brics.string.RuntimeHotspot 
 *  @see dk.brics.string.AnalyzeRuntime
 *  @see dk.brics.string.InvalidRuntimeUseException
 */
public class RuntimeResolver implements Resolver {
    private Map/*<String,RegExp>*/ regexp_bind = new HashMap();
    private Map/*<String,Automaton>*/ automaton_bind = new HashMap();
    private Map/*<RegExp,Automaton>*/ regexp_cache = new HashMap();
    private Map/*<URL,Automaton>*/ url_cache = new HashMap();
    private Map/*<Local,String>*/ local_url_map = new HashMap();

    private Set/*<RuntimeHotspot>*/ hotspots = new HashSet();

    /** Initializes a <code>RuntimeResolver</code> for the current
     *  application classes.
     *  <p>
     *  First, all {@link dk.brics.string.runtime.RegExp#bind RegExp.bind}
     *  calls in the program are collected, so that the regular expressions
     *  occurring in runtime method calls can be correctly resolved.<br>
     *  Second, all {@link dk.brics.string.runtime.RegExp#analyze RegExp.analyze} and
     *  {@link dk.brics.string.runtime.RegExp#check RegExp.check} calls are
     *  internally marked as hotspots. These can be queried using the
     *  {@link dk.brics.string.RuntimeResolver#getHotspots getHotspots} and
     *  {@link dk.brics.string.RuntimeResolver#getHotspotExps getHotspotExps} methods.
     *  @exception InvalidRuntimeUseException if some invalid use of the runtime
     *             library is encountered.
     */
    public RuntimeResolver() {
	findBinds();
	findHotspots();
    }

    /** Returns the set of runtime method hotspots for the program.
     *  @return a set of {@link dk.brics.string.RuntimeHotspot} objects
     *          describing the runtime method hotspots.
     */
    public Set/*<RuntimeHotspot>*/ getHotspots() {
	return hotspots;
    }

    /** Returns the set of string expressions corresponding to the
     *  runtime method hotspots for the program.
     *  @return a set of {@link soot.ValueBox} objects indicating the
     *          expressions marked as hotspots.
     */
    public Set/*<ValueBox>*/ getHotspotExps() {
	Set/*<ValueBox>*/ exps = new HashSet();
	Iterator rhi = hotspots.iterator();
	while (rhi.hasNext()) {
	    RuntimeHotspot rh = (RuntimeHotspot) rhi.next();
	    exps.add(rh.spot);
	}
	return exps;
    }

    void findBinds() {
	// Find all bind calls
	Iterator aci = Scene.v().getApplicationClasses().iterator();
	while (aci.hasNext()) {
	    SootClass ac = (SootClass)aci.next();
	    Iterator mi = ac.getMethods().iterator();
	    while (mi.hasNext()) {
		SootMethod sm = (SootMethod)mi.next();
		if (sm.isConcrete()) {
		    CompleteUnitGraph cug = new CompleteUnitGraph(sm.retrieveActiveBody());
		    Iterator si = cug.iterator();
		    while (si.hasNext()) {
			Stmt stmt = (Stmt)si.next();
			if (stmt.containsInvokeExpr()) {
			    InvokeExpr expr = (InvokeExpr)stmt.getInvokeExpr();
			    if (expr instanceof SpecialInvokeExpr &&
				expr.getMethod().getSignature().equals("<java.net.URL: void <init>(java.lang.String)>") &&
				expr.getArg(0) instanceof StringConstant) {
				local_url_map.put(((SpecialInvokeExpr)expr).getBase(), expr.getArg(0).toString());
			    }
			    if (expr.getMethod().getSignature().equals("<dk.brics.string.runtime.RegExp: void bind(java.lang.String,java.lang.String)>")) {
				String name = getName(expr);
				RegExp re = getRegExp(expr);
				regexp_bind.put(name, re);
			    }
			    if (expr.getMethod().getSignature().equals("<dk.brics.string.runtime.RegExp: void bind(java.lang.String,java.net.URL)>")) {
				String name = getName(expr);
				URL url = getURL(expr);
				Automaton automaton = getFromURL(url);
				automaton_bind.put(name, automaton);
			    }
			}
		    }
		}
	    }
	}
    }

    void findHotspots() {
	// Find all hotspots
	Iterator aci = Scene.v().getApplicationClasses().iterator();
	while (aci.hasNext()) {
	    SootClass ac = (SootClass)aci.next();
	    Iterator mi = ac.getMethods().iterator();
	    while (mi.hasNext()) {
		SootMethod sm = (SootMethod)mi.next();
		if (sm.isConcrete()) {
		    CompleteUnitGraph cug = new CompleteUnitGraph(sm.retrieveActiveBody());
		    Iterator si = cug.iterator();
		    while (si.hasNext()) {
			Stmt stmt = (Stmt)si.next();
			if (stmt.containsInvokeExpr()) {
			    InvokeExpr expr = (InvokeExpr)stmt.getInvokeExpr();
			    if (expr.getMethod().getSignature().equals("<dk.brics.string.runtime.RegExp: java.lang.String analyze(java.lang.String,java.lang.String)>")) {
				ValueBox spot = expr.getArgBox(0);
				Automaton expected = getFromRegExp(getRegExp(expr));
				int kind = RuntimeHotspot.KIND_ANALYZE;
				hotspots.add(new RuntimeHotspot(spot, expected, kind));
			    }
			    if (expr.getMethod().getSignature().equals("<dk.brics.string.runtime.RegExp: java.lang.String analyze(java.lang.String,java.net.URL)>")) {
				ValueBox spot = expr.getArgBox(0);
				Automaton expected = getFromURL(getURL(expr));
				int kind = RuntimeHotspot.KIND_ANALYZE;
				hotspots.add(new RuntimeHotspot(spot, expected, kind));
			    }
			    if (expr.getMethod().getSignature().equals("<dk.brics.string.runtime.RegExp: java.lang.String check(java.lang.String,java.lang.String)>")) {
				ValueBox spot = expr.getArgBox(0);
				Automaton expected = getFromRegExp(getRegExp(expr));
				int kind = RuntimeHotspot.KIND_CHECK;
				hotspots.add(new RuntimeHotspot(spot, expected, kind));
			    }
			    if (expr.getMethod().getSignature().equals("<dk.brics.string.runtime.RegExp: java.lang.String check(java.lang.String,java.net.URL)>")) {
				ValueBox spot = expr.getArgBox(0);
				Automaton expected = getFromURL(getURL(expr));
				int kind = RuntimeHotspot.KIND_CHECK;
				hotspots.add(new RuntimeHotspot(spot, expected, kind));
			    }
			}
		    }
		}
	    }
	}
    }

    String getName(InvokeExpr expr) {
	if (expr.getArg(0) instanceof StringConstant) {
	    return expr.getArg(0).toString();
	} else {
	    throw new InvalidRuntimeUseException("Non-constant name");
	}
    }

    RegExp getRegExp(InvokeExpr expr) {
	if (expr.getArg(1) instanceof StringConstant) {
	    return new RegExp(((StringConstant)expr.getArg(1)).value);
	} else {
	    throw new InvalidRuntimeUseException("Non-constant regexp");
	}
    }

    URL getURL(InvokeExpr expr) {
	if (expr.getArg(1) instanceof Local && local_url_map.containsKey(expr.getArg(1))) {
	    String url = (String)local_url_map.get(expr.getArg(1));
	    try {
		return new URL(url);
	    } catch (MalformedURLException e) {
		throw new InvalidRuntimeUseException("Malformed URL: "+url);
	    }
	} else {
	    throw new InvalidRuntimeUseException("Non-constant URL");
	}
    }

    Automaton getFromURL(URL url) {
	if (url_cache.containsKey(url)) {
	    return (Automaton)url_cache.get(url);
	}
	try {
	    Automaton a = Automaton.load(url);
	    url_cache.put(url, a);
	    return a;
	} catch (Exception e) {
	    throw new InvalidRuntimeUseException("Invalid automaton URL: "+url);
	}
    }

    Automaton getFromRegExp(RegExp re) {
	if (regexp_cache.containsKey(re)) {
	    return (Automaton)regexp_cache.get(re);
	}
	Iterator ni = re.getIdentifiers().iterator();
	while (ni.hasNext()) {
	    String name = (String)ni.next();
	    getFromName(name);
	}
	Automaton a = re.toAutomaton(automaton_bind);
	regexp_cache.put(re, a);
	return a;
    }

    Automaton getFromName(String name) {
	if (automaton_bind.containsKey(name)) {
	    return (Automaton)automaton_bind.get(name);
	}
	if (regexp_bind.containsKey(name)) {
	    RegExp re = (RegExp)regexp_bind.get(name);
	    regexp_bind.remove(re);
	    Automaton a = getFromRegExp(re);
	    automaton_bind.put(name, a);
	    return a;
	}
	throw new InvalidRuntimeUseException("Unable to resolve binding for name `"+name+"'");
    }

    /** If the given target method is {@link dk.brics.string.runtime.RegExp#cast RegExp.cast},
     *  returns the automaton given as a regular expression or automaton URL in the cast.
     *  <p>
     *  If the given target method is {@link dk.brics.string.runtime.RegExp#cast RegExp.analyze},
     *  returns the value box for the first argument.
     *  @param expr the invocation to be resolved.
     *  @param target the target method.
     *  @return the automaton given in the cast, or <code>null</code>.
     *  @exception InvalidRuntimeUseException if some invalid use of the runtime
     *             library is encountered.
     */
    public Object resolveMethod(InvokeExpr expr, SootMethod target) {
	if (target.getSignature().equals("<dk.brics.string.runtime.RegExp: java.lang.String cast(java.lang.String,java.lang.String)>")) {
	    return getFromRegExp(getRegExp(expr));
	}
	if (target.getSignature().equals("<dk.brics.string.runtime.RegExp: java.lang.String cast(java.lang.String,java.net.URL)>")) {
	    return getFromURL(getURL(expr));
	}
	if (target.getSignature().equals("<dk.brics.string.runtime.RegExp: java.lang.String analyze(java.lang.String,java.lang.String)>")) {
	    return expr.getArgBox(0);
	}
	if (target.getSignature().equals("<dk.brics.string.runtime.RegExp: java.lang.String analyze(java.lang.String,java.net.URL)>")) {
	    return expr.getArgBox(0);
	}
	return null;
    }

    /** No special fields are resolved.
     *  @param expr the field to be resolved.
     *  @return <code>null</code>.
     */
    public Object resolveField(FieldRef expr) {
	return null;
    }
}
