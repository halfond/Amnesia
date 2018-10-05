package dk.brics.string.java;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import soot.ArrayType;
import soot.BooleanType;
import soot.ByteType;
import soot.CharType;
import soot.DoubleType;
import soot.FloatType;
import soot.Hierarchy;
import soot.IntType;
import soot.Local;
import soot.LongType;
import soot.NullType;
import soot.PrimType;
import soot.RefType;
import soot.Scene;
import soot.ShortType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.ValueBox;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.toolkits.annotation.nullcheck.LocalRefVarsAnalysisWrapper;
import soot.toolkits.graph.CompleteUnitGraph;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import dk.brics.string.IsExternallyVisible;
import dk.brics.string.intermediate.ArrayCorrupt;
import dk.brics.string.intermediate.Call;
import dk.brics.string.intermediate.Method;
import dk.brics.string.intermediate.Nop;
import dk.brics.string.intermediate.Return;
import dk.brics.string.intermediate.Statement;
import dk.brics.string.intermediate.StringAssignment;
import dk.brics.string.intermediate.StringBufferCorrupt;
import dk.brics.string.intermediate.StringInit;
import dk.brics.string.intermediate.StringStatement;
import dk.brics.string.intermediate.Variable;
import dk.brics.string.operations.Basic;

/**
 * Translation of all application classes into a set of intermediate
 * representation methods.
 * 
 * @see dk.brics.string.StringAnalysis
 */
public class JavaTranslator {
	List/* <Method> */methods = new LinkedList();

	Map/* <String,Method> */sms_m = new HashMap();

	Map/* <String,int[]> */sms_sa_ma = new HashMap();

	Map/* <String,int[]> */sms_ma_sa = new HashMap();

	IsExternallyVisible ext;

	Collection/* <Resolver> */resolvers;

	Collection/* <ValueBox> */hotspots;

	Collection/* <ValueBox> */extra_hotspots = new HashSet();

	Map/* <SootClass,Method> */tostring_targets = new HashMap();

	Map/* <String,Method> */tostring_methods = new HashMap();

	Map/* <SootClass,StringStatement> */tostring_hotspots = new HashMap();

	Map/* <Type,Automaton> */type_automaton = new HashMap();

	StmtTranslator st;

	LocalRefVarsAnalysisWrapper null_an;

	List/* <Local> */null_vars;

	Hierarchy h;

	int skipped;

	public JavaTranslator(IsExternallyVisible ext) {
		this(ext, Collections.EMPTY_SET);
	}

	public JavaTranslator(IsExternallyVisible ext,
			Collection/* <Resolver> */resolvers) {
		this.ext = ext;
		this.resolvers = resolvers;
	}

	public Method[] translateApplicationClasses() {
		return translateApplicationClasses(null);
	}

	public Method[] translateApplicationClasses(
			Collection/* <ValueBox> */hotspots) {
		this.hotspots = hotspots;
		h = new Hierarchy();
		skipped = 0;
		makeMethods();
		makeWrapperMethod();
		makeToStringMethods();
		translate();
		removeNops();

		return (Method[]) methods.toArray(new Method[0]);
	}

	public boolean isHotspot(ValueBox expr) {
		return hotspots == null || hotspots.contains(expr);
	}

	public Map/* <ValueBox,Statement> */getTranslationMap() {
		return st.trans_map;
	}

	public int getNumberOfExpsSkipped() {
		return skipped;
	}

	public Collection/* <ValueBox> */getExtraHotspots() {
		return extra_hotspots;
	}

	public Map/* <SootClass,StringStatement> */getToStringHotspotMap() {
		return tostring_hotspots;
	}

	public Map/* <ValueBox,String> */getSourceFileMap() {
		return st.sourcefile_map;
	}

	public Map/* <ValueBox,String> */getClassNameMap() {
		return st.class_map;
	}

	public Map/* <ValueBox,String> */getMethodNameMap() {
		return st.method_map;
	}

	public Map/* <ValueBox,Integer> */getLineNumberMap() {
		return st.line_map;
	}

	void makeMethods() {
		Collection/* <SootClass> */app = Scene.v().getApplicationClasses();
		Iterator aci = app.iterator();
		while (aci.hasNext()) {
			SootClass ac = (SootClass) aci.next();
			Iterator mi = ac.getMethods().iterator();
			while (mi.hasNext()) {
				SootMethod sm = (SootMethod) mi.next();
				List/* <Variable> */vars = new LinkedList();
				List params = sm.getParameterTypes();
				int[] sa_ma = new int[params.size()];
				int[] ma_sa = new int[params.size()];
				int ma = 0;
				int sa = 0;
				Iterator ai = params.iterator();
				while (ai.hasNext()) {
					Type pt = (Type) ai.next();
					if (isSType(pt)) {
						vars.add(makeVariable(pt));
						sa_ma[sa] = ma;
						ma_sa[ma] = sa;
						ma++;
					} else {
						sa_ma[sa] = -1;
					}
					sa++;
				}

				Variable[] var_array = (Variable[]) vars
						.toArray(new Variable[0]);
				Method m = new Method(sm.getName(), var_array);
				methods.add(m);
				sms_m.put(sm.getSignature(), m);
				sms_sa_ma.put(sm.getSignature(), sa_ma);
				sms_ma_sa.put(sm.getSignature(), ma_sa);
			}

		}
	}

	void makeToStringMethods() {
		// Make basic tostring methods
		makeBasicToStringMethod(null, "java.lang.Object", new RegExp(
				".+(\\..+)*\\@[0-9a-f]+").toAutomaton());
		makeBasicToStringMethod(BooleanType.v(), "java.lang.Boolean", Basic
				.makeBooleanString());
		makeBasicToStringMethod(ByteType.v(), "java.lang.Byte", Basic
				.makeByteString());
		makeBasicToStringMethod(CharType.v(), "java.lang.Character", Basic
				.makeCharacterString());
		makeBasicToStringMethod(DoubleType.v(), "java.lang.Double", Basic
				.makeDoubleString());
		makeBasicToStringMethod(FloatType.v(), "java.lang.Float", Basic
				.makeFloatString());
		makeBasicToStringMethod(IntType.v(), "java.lang.Integer", Basic
				.makeIntegerString());
		makeBasicToStringMethod(LongType.v(), "java.lang.Long", Basic
				.makeLongString());
		makeBasicToStringMethod(ShortType.v(), "java.lang.Short", Basic
				.makeShortString());

		// Make tostring methods for application classes
		// Link toString calls to the hotspots for all superclasses of the
		// receiver type
		Collection/* <SootClass> */app = Scene.v().getApplicationClasses();
		Iterator aci = app.iterator();
		while (aci.hasNext()) {
			SootClass ac = (SootClass) aci.next();
			Method m = new Method(ac.getName() + ".toString", new Variable[0]);
			methods.add(m);
			tostring_methods.put(ac.getName(), m);
			Variable var = new Variable(Variable.TYPE_STRING);
			StringStatement spot = new StringAssignment(var, var);
			m.addStatement(spot);
			Return ret = new Return(var);
			m.addStatement(ret);
			spot.addSucc(ret);
			tostring_hotspots.put(ac, spot);
			Iterator aci2 = app.iterator();
			while (aci2.hasNext()) {
				SootClass ac2 = (SootClass) aci2.next();
				if (h.isClassSubclassOfIncluding(ac2, ac)) {
					// Calling tostring with a receiver of type ac,
					// the method in class ac2 might be called.
					// ac2 might not implement it directly,
					// so we search upwards for the implementation
					while (!tostring_targets.containsKey(ac2)) {
						ac2 = ac2.getSuperclass();
					}
					Method target = (Method) tostring_targets.get(ac2);
					Call call = new Call(var, target, new Variable[0]);
					m.addStatement(call);
					m.getEntry().addSucc(call);
					call.addSucc(spot);
				}
			}
		}
	}

	void makeBasicToStringMethod(Type prim, String classname, Automaton a) {
		Method m = new Method(classname + ".toString", new Variable[0]);
		SootClass c = Scene.v().getSootClass(classname);
		Variable var = new Variable(Variable.TYPE_STRING);
		StringStatement ss = new StringInit(var, a);
		m.addStatement(ss);
		m.getEntry().addSucc(ss);
		Return ret = new Return(var);
		m.addStatement(ret);
		ss.addSucc(ret);

		methods.add(m);
		tostring_targets.put(c, m);
		tostring_methods.put(classname, m);

		type_automaton.put(prim, a);
	}

	void translate() {
		st = new StmtTranslator(this);
		Collection/* <SootClass> */app = Scene.v().getApplicationClasses();
		Iterator aci = app.iterator();
		while (aci.hasNext()) {
			SootClass ac = (SootClass) aci.next();
			st.setCurrentClass(ac);
			Iterator mi = ac.getMethods().iterator();
			while (mi.hasNext()) {
				SootMethod sm = (SootMethod) mi.next();
				if (sm.isConcrete()) {
					Method method = (Method) sms_m.get(sm.getSignature());
					st.setCurrentMethod(sm);
					CompleteUnitGraph cug = new CompleteUnitGraph(sm
							.retrieveActiveBody());
					null_an = new LocalRefVarsAnalysisWrapper(cug);
					Iterator si = cug.iterator();
					while (si.hasNext()) {
						Stmt stmt = (Stmt) si.next();
						null_vars = null_an.getVarsNeedCheck(stmt);
						st.translateStmt(stmt);
					}
					si = cug.getHeads().iterator();
					while (si.hasNext()) {
						Stmt stmt = (Stmt) si.next();
						method.getEntry().addSucc(st.getFirst(stmt));
					}
					si = cug.iterator();
					while (si.hasNext()) {
						Stmt stmt = (Stmt) si.next();
						Iterator si2 = cug.getSuccsOf(stmt).iterator();
						while (si2.hasNext()) {
							Stmt stmt2 = (Stmt) si2.next();
							st.getLast(stmt).addSucc(st.getFirst(stmt2));
						}
					}
				}
			}
		}
	}

	void makeWrapperMethod() {
		Method wrapper = new Method("<wrapper>", new Variable[0]);
		methods.add(wrapper);
		Return ret = new Return(new Variable(Variable.TYPE_NONE));
		wrapper.addStatement(ret);

		Collection/* <SootClass> */app = Scene.v().getApplicationClasses();
		Iterator aci = app.iterator();
		while (aci.hasNext()) {
			SootClass ac = (SootClass) aci.next();
			Iterator mi = ac.getMethods().iterator();
			while (mi.hasNext()) {
				SootMethod sm = (SootMethod) mi.next();
				if (ext.isExternallyVisibleMethod(sm)) {
					Statement last = wrapper.getEntry();
					Method m = (Method) sms_m.get(sm.getSignature());
					Variable[] params = m.getEntry().params;
					Variable[] args = new Variable[params.length];
					for (int i = 0; i < params.length; i++) {
						Variable arg = new Variable(params[i].type);
						args[i] = arg;
						Statement s;
						switch (arg.type) {
						case Variable.TYPE_STRING:
							s = new StringInit(arg, Basic.makeAnyString());
							break;
						case Variable.TYPE_STRINGBUFFER:
							s = new StringBufferCorrupt(arg);
							break;
						case Variable.TYPE_ARRAY:
							s = new ArrayCorrupt(arg);
							break;
						default:
							s = new Nop();
							break;
						}
						wrapper.addStatement(s);
						last.addSucc(s);
						last = s;
					}
					Variable retvar = makeVariable(sm.getReturnType());
					Call c = new Call(retvar, m, args);
					wrapper.addStatement(c);
					last.addSucc(c);
					c.addSucc(ret);
					// If this is toString, remember the return value
					if (sm.getName().equals("toString")
							&& sm.getParameterCount() == 0
							&& sm.getReturnType().toString().equals(
									"java.lang.String")) {
						tostring_targets.put(ac, m);
					}
				}
			}
		}

	}

	void removeNops() {
		int nops = 0;
		int others = 0;
		Iterator mi = new ArrayList(methods).iterator();
		while (mi.hasNext()) {
			Method m = (Method) mi.next();
			Iterator si = new ArrayList(m.getStatements()).iterator();
			while (si.hasNext()) {
				Statement s = (Statement) si.next();
				if (s instanceof Nop) {
					m.removeNop((Nop) s);
					nops++;
				} else {
					others++;
				}
			}
		}
	}

	boolean canBeNull(Local v) {
		return null_vars.contains(v);
	}

	boolean isSType(Value v) {
		return getValueType(v) != Variable.TYPE_NONE;
	}

	boolean isSType(Type t) {
		return getType(t) != Variable.TYPE_NONE;
	}

	Variable makeVariable(Value v) {
		return makeVariable(v.getType());
	}

	Variable makeVariable(Type t) {
		int type = getType(t);
		return new Variable(type);
	}

	int getValueType(Value v) {
		return getType(v.getType());
	}

	int getType(Type t) {
		if (t instanceof RefType) {
			if (((RefType) t).getSootClass().getName().equals(
					"java.lang.String")) {
				return Variable.TYPE_STRING;
			}
			if (((RefType) t).getSootClass().getName().equals(
					"java.lang.StringBuffer")) {
				return Variable.TYPE_STRINGBUFFER;
			}
		}
		if (t instanceof ArrayType) {
			Type bt = ((ArrayType) t).baseType;
			if (bt instanceof RefType
					&& ((RefType) bt).getSootClass().getName().equals(
							"java.lang.String")) {
				return Variable.TYPE_ARRAY;
			}
		}
		return Variable.TYPE_NONE;
	}

	List/* <SootMethod> */getTargetsOf(InvokeExpr expr) {
		if (expr instanceof InstanceInvokeExpr) {
			return getTargetsOf(((InstanceInvokeExpr) expr).getBase(), expr
					.getMethod());
		} else {
			List/* <SootMethod> */targets = new ArrayList(1);
			targets.add(expr.getMethod());
			return targets;
		}
	}

	List/* <SootMethod> */getTargetsOf(Value v, SootMethod m) {
		SootClass rc;
		Type t = v.getType();
		if (t instanceof ArrayType) {
			rc = Scene.v().getSootClass("java.lang.Object");
		} else {
			rc = ((RefType) v.getType()).getSootClass();
		}
		List/* <SootMethod> */targets = h.resolveAbstractDispatch(rc, m);
		return targets;
	}

	Method getToStringMethod(SootClass c) {
		return (Method) tostring_methods.get(c.getName());
	}

	public Automaton getTypeAutomaton(Type t) {
		if (t instanceof PrimType) {
			return (Automaton) type_automaton.get(t);
		} else if (t instanceof NullType) {
			return Automaton.makeString("null");
		} else {
			return null;
		}
	}

	boolean isApplicationClass(SootClass c) {
		Iterator aci = Scene.v().getApplicationClasses().iterator();
		while (aci.hasNext()) {
			SootClass ac = (SootClass) aci.next();
			if (c.getName().equals(ac.getName())) {
				return true;
			}
		}
		return false;
	}

}
