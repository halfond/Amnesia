
package dk.brics.string;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.ArrayType;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.ValueBox;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.toolkits.graph.CompleteUnitGraph;
import dk.brics.automaton.Automaton;
import dk.brics.string.flow.Graph;
import dk.brics.string.flow.Node;
import dk.brics.string.grammar.Grammar;
import dk.brics.string.grammar.Nonterminal;
import dk.brics.string.intermediate.AliasAnalysis;
import dk.brics.string.intermediate.FlowGraphConnector;
import dk.brics.string.intermediate.LivenessAnalysis;
import dk.brics.string.intermediate.Method;
import dk.brics.string.intermediate.ReachingDefinitions;
import dk.brics.string.intermediate.StringStatement;
import dk.brics.string.intermediate.Translator;
import dk.brics.string.java.JavaTranslator;
import dk.brics.string.mlfa.MLFA;
import dk.brics.string.mlfa.MLFAStatePair;
import dk.brics.string.operations.Basic;

/** A <code>StringAnalysis</code> object ancapsulates a string analysis performed
 *  on a collection of classes.
 *  The class also contains some convenience methods for loading and traversing
 *  the classes to be analyzed.<p>
 */
public class StringAnalysis implements IsExternallyVisible {
    /** Set this field to <code>true</code> to make the string analysis output
     *  information about its progress to <code>stderr</code>.
     */
    public static boolean DEBUG = false;

    private static List/*<Resolver>*/ resolvers = new ArrayList();

    private MLFA mlfa;
    private JavaTranslator jt;
    private Map/*<ValueBox,MLFAStatePair>*/ map;
    private Map/*<SootClass,MLFAStatePair>*/ tostring_map;
    private Map/*<ValueBox,String>*/ sourcefile_map;
    private Map/*<ValueBox,String>*/ class_map;
    private Map/*<ValueBox,String>*/ method_map;
    private Map/*<ValueBox,Integer>*/ line_map;
    private int num_exps;

    //XXX: added by GJ
    private Graph graph;
    private Grammar grammar;
    
    // Make sure we get line numbers
    static {
    	//soot.Scene.v().loadNecessaryClasses();
    	soot.Scene.v().loadBasicClasses();
    	soot.options.Options.v().parse(new String[] { "-keep-line-number" });
    }

    /** Adds the given resolver to the list of active resolvers used
     *  during the string analysis.
     *  @param r the resolver to add.
     *  @see dk.brics.string.Resolver
     */
    public static void addResolver(Resolver r) {
	resolvers.add(r);
    }

    /** Removes the given resolver from the list of active resolvers used
     *  during the string analysis.
     *  @param r the resolver to remove.
     *  @see dk.brics.string.Resolver
     */
    public static void removeResolver(Resolver r) {
	resolvers.remove(r);
    }

    /** Removes all active resolvers.
     *  @see dk.brics.string.Resolver
     */
    public static void clearResolvers() {
	resolvers = new ArrayList();
    }

    /** Performs a string analysis on the current application classes.
     *  All expressions are considered hot spots.
     */
    public StringAnalysis() {
	this(null, null);
    }

    /** Performs a string analysis on the current application classes.
     *  All expressions are considered hot spots.
     *  @param ext defines which methods are externally visible.
     */
    public StringAnalysis(IsExternallyVisible ext) {
	this(null, ext);
    }

    /** Performs a string analysis on the current application classes.
     *  @param hotspots a set of {@link soot.ValueBox} objects indicating
     *                  the desired hot spots
     */
    public StringAnalysis(Collection/*<ValueBox>*/ hotspots) {
	this(hotspots, null);
    }

    /** Performs a string analysis on the current application classes.
     *  @param hotspots a set of {@link soot.ValueBox} objects indicating
     *                  the desired hot spots
     *  @param ext defines which methods are externally visible.
     */
    public StringAnalysis(Collection/*<ValueBox>*/ hotspots, IsExternallyVisible ext) {
	if (ext == null) ext = this;
	jt = new JavaTranslator(ext, resolvers);
	debug("Translating classes to intermediate form...");
	Method[] methods = jt.translateApplicationClasses(hotspots);
	Map/*<ValueBox,Statement>*/ m1 = jt.getTranslationMap();
	num_exps = m1.size()+jt.getNumberOfExpsSkipped();
	debug("Performing liveness analysis...");
	LivenessAnalysis la = new LivenessAnalysis(methods);
	debug("Performing alias analysis...");
	AliasAnalysis aa = new AliasAnalysis(methods, la);
	debug("Performing reaching definitions analysis...");
	ReachingDefinitions rd = new ReachingDefinitions(methods, la, aa);
	debug("Generating flow graph nodes...");
	Translator tr = new Translator(methods, aa);
	//XXX: Graph g = tr.getGraph();
    graph = tr.getGraph();
	debug("Generating flow graph edges...");
	FlowGraphConnector fgc = new FlowGraphConnector(methods, aa, rd, graph, tr.getMap());
	Map/*<Statement,Node>*/ m2 = tr.getTranslationMap();
	debug("Simplifying flow graph...");
	Map/*<Node,Node>*/ m3 = graph.simplify();

	debug("Transforming into grammar...");
	//XXX: Grammar r = graph.toGrammar();
    grammar = graph.toGrammar();
	debug("Cutting operation cycles...");
	grammar.approximateOperationCycles();

	// Mark all hotspots in grammar
	if (hotspots == null) {
	    hotspots = m1.keySet();
	} else {
	    hotspots.addAll(jt.getExtraHotspots());
	}
	Set/*<Nonterminal>*/ hs_nt = new HashSet();
	Iterator hsi = hotspots.iterator();
	while (hsi.hasNext()) {
	    ValueBox b = (ValueBox) hsi.next();
	    Node n = (Node)m3.get(m2.get(m1.get(b)));
	    if (n != null) {
		hs_nt.add(n.getNonterminal());
	    }
	}
	Iterator tshsi = jt.getToStringHotspotMap().values().iterator();
	while (tshsi.hasNext()) {
	    StringStatement ss = (StringStatement) tshsi.next();
	    Node n = (Node)m3.get(m2.get(ss));
	    if (n != null) {
		hs_nt.add(n.getNonterminal());
	    }
	}

	// Approximate grammar
	debug("Performing regular approximation...");
	grammar.approximateNonRegular(hs_nt);
	debug("Converting to MLFA...");
	mlfa = grammar.toMLFA();

	// Make map
	map = new HashMap();
	Iterator bi = hotspots.iterator();
	while (bi.hasNext()) {
	    ValueBox box = (ValueBox) bi.next();
	    Node n = (Node)m3.get(m2.get(m1.get(box)));
	    if (n == null) {
		// Internal error
		throw new Error("Internal error: No mapping for "+box.getValue().toString());
	    }
	    Nonterminal nt = n.getNonterminal();
	    MLFAStatePair sp = nt.getMLFAStatePair();
	    map.put(box, sp);
	}
	tostring_map = new HashMap();
	Map/*<SootClass,StringStatement>*/ tostring_hotspot_map = jt.getToStringHotspotMap();
	Iterator tsci = tostring_hotspot_map.keySet().iterator();
	while (tsci.hasNext()) {
	    SootClass tsc = (SootClass) tsci.next();
	    StringStatement ss = (StringStatement)tostring_hotspot_map.get(tsc);
	    Node n = (Node)m3.get(m2.get(ss));
	    if (n == null) {
		// Internal error
		throw new Error("Internal error: No mapping for "+tsc.toString());
	    }
	    Nonterminal nt = n.getNonterminal();
	    MLFAStatePair sp = nt.getMLFAStatePair();
	    tostring_map.put(tsc, sp);
	}
	sourcefile_map = jt.getSourceFileMap();
	class_map = jt.getClassNameMap();
	method_map = jt.getMethodNameMap();
	line_map = jt.getLineNumberMap();
    }

    /** Defines which methods are seen by the string analysis as being
     *  accessible from unknown code.
     *  The arguments to these methods can contain any values,
     *  and the return values might escape to unknown code.<p>
     *
     *  The default implementation assumes that public methods are
     *  externally visible and that all others are not. Override
     *  this method to redefine this behavior.
     *
     *  @param sm a method in the analyzed code.
     *  @return whether the method could possibly be called from unknown code.
     */
    public boolean isExternallyVisibleMethod(SootMethod sm) {
	return sm.isPublic();
    }

    private void debug(String s) {
	if (DEBUG) {
	    System.err.println(s);
	}
    }

    /** Returns whether or not the given expression has a type that
     *  the string analysis is able to handle.
     *  @param box the Soot value box containing the expression.
     *  @return <code>true</code> if the expression has type {@link java.lang.String},
     *          {@link java.lang.StringBuffer} or array (of any dimension) of
     *          {@link java.lang.String}; <code>false</code> otherwise;
     */
    public static boolean hasValidType(ValueBox box) {
	Type t = box.getValue().getType();
	if (t instanceof RefType) {
	    if (((RefType)t).getSootClass().getName().equals("java.lang.String")) {
		return true;
	    }
	    if (((RefType)t).getSootClass().getName().equals("java.lang.StringBuffer")) {
		return true;
	    }
	}
	if (t instanceof ArrayType) {
	    Type bt = ((ArrayType)t).baseType;
	    if (bt instanceof RefType && ((RefType)bt).getSootClass().getName().equals("java.lang.String")) {
		return true;
	    }
	}
	return false;
    }

    /** Computes the automaton describing the possible string
     *  values at the given expression.
     *  The expression given can have any type. If the type is
     *  {@link java.lang.String}, {@link java.lang.StringBuffer} or
     *  array (of any dimension) of {@link java.lang.String},
     *  the inferred result is returned directly. If it is of a simple type
     *  or a wrapper class, the corresponding type automaton is returned.
     *  Otherwise, the inferred result for the return values of the
     *  relevant <code>toString</code> methods is returned.
     *  If a specific set of hotspots has been supplied to the analysis,
     *  and the expression is of one of the string types metioned above,
     *  the expression given must be one of these hotspots.
     *  @param box the Soot value box containing the expression.
     *  @return an automaton whose language contains all possible run-time values
     *          of the given expression.
     *  @exception IllegalArgumentException if the expression is not a marked hotspot.
     */
    public final Automaton getAutomaton(ValueBox box) {
	if (!hasValidType(box)) {
	    return getTypeAutomaton(box.getValue().getType());
	}
	if (!map.containsKey(box)) {
	    throw new IllegalArgumentException("Expression is not a marked hotspot");
	}
	MLFAStatePair sp = (MLFAStatePair)map.get(box);
	return mlfa.extract(sp);
    }

    /** Computes the automaton describing the possible string
     *  values that can occur as a result of converting the given
     *  type into a string.
     *  If the type is a simple type or a wrapper class,
     *  the corresponding type automaton is returned.
     *  Otherwise, the result is the union of the inferred results
     *  for the return values of the <code>toString</code> methods of
     *  the type and all its subclasses.
     *  @param t the Soot type.
     *  @return an automaton whose language contains all possible values
     *          of the result of converting this type into a string.
     */
    public final Automaton getTypeAutomaton(Type t) {
	Automaton ta = jt.getTypeAutomaton(t);
	if (ta != null) {
	    return ta;
	}
	if (t instanceof RefType) {
	    SootClass c = ((RefType)t).getSootClass();
	    if (tostring_map.containsKey(c)) {
		MLFAStatePair sp = (MLFAStatePair)tostring_map.get(c);
		return mlfa.extract(sp);
	    }
	}
	return Basic.makeAnyString();
    }

    /** Returns the name of the source file containing the given expression.
     *  @param box the expression.
     *  @return the source file name.
     */
    public final String getSourceFile(ValueBox box) {
	return (String)sourcefile_map.get(box);
    }

    /** Returns the name of the class containing the given expression.
     *  @param box the expression.
     *  @return the fully qualified class name.
     */
    public final String getClassName(ValueBox box) {
	return (String)class_map.get(box);
    }

    /** Returns the name of the method containing the given expression.
     *  @param box the expression.
     *  @return the method name.
     */
    public final String getMethodName(ValueBox box) {
	return (String)method_map.get(box);
    }

    /** Returns the source line number of the given expression.
     *  @param box the expression.
     *  @return the line number.
     */
    public final int getLineNumber(ValueBox box) {
	return ((Integer)line_map.get(box)).intValue();
    }

    /** Loads the named class into the Soot scene,
     *  marks it as an application class, and generates bodies
     *  for all of its concrete methods.
     *  @param name the fully qualified name of the class to be loaded.
     */
    public static void loadClass(String name) {
	SootClass c = Scene.v().loadClassAndSupport(name);
	c.setApplicationClass();
	Iterator mi = c.getMethods().iterator();
	while (mi.hasNext()) {
	    SootMethod sm = (SootMethod)mi.next();
	    if (sm.isConcrete()) {
		sm.retrieveActiveBody();
	    }
	}
    }

    /** Returns the total number of analyzable expressions in the program.
     *  @return the number of expressions in the analyzed program that has
     *  a type that the string analysis is able to handle.
     */
    public final int getNumExps() {
	return num_exps;
    }

    /** Returns a list containing all expressions occurring as
     *  argument to the specified method.
     *  @param sig the signature of the method to collect arguments to, e.g.
     *             <code>"&lt;java.io.PrintStream: void println(java.lang.String)&gt"</code>.
     *  @param argnum the index of the argument to the call
     *  @return a {@link java.util.List} of {@link soot.ValueBox} objects.
     *          It is not checked that these have valid types.
     */
    public static List/*<ValueBox>*/ getExps(String sig, int argnum) {
	ArrayList list = new ArrayList();
	Iterator aci = Scene.v().getApplicationClasses().iterator();
	while (aci.hasNext()) {
	    SootClass ac = (SootClass)aci.next();
	    Iterator mi = ac.getMethods().iterator();
	    while (mi.hasNext()) {
		SootMethod sm = (SootMethod)mi.next();
		if (sm.isConcrete()) {
		    CompleteUnitGraph cug = new CompleteUnitGraph(sm.retrieveActiveBody());
//			BriefUnitGraph cug = new BriefUnitGraph(sm.retrieveActiveBody());
		    Iterator si = cug.iterator();
		    while (si.hasNext()) {
			Stmt stmt = (Stmt)si.next();
			if (stmt.containsInvokeExpr()) {
			    InvokeExpr expr = (InvokeExpr)stmt.getInvokeExpr();
			    if (expr.getMethod().getSignature().equals(sig)) {
				ValueBox box = expr.getArgBox(argnum);
				list.add(box);
			    }
			}
		    }
		}
	    }
	}
	return list;
    }

    public Grammar getGrammar() {
        return grammar;
    }

    public Graph getGraph() {
        return graph;
    }

    public MLFA getMlfa() {
        return mlfa;
    }

}
