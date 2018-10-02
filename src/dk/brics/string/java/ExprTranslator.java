
package dk.brics.string.java;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import soot.CharType;
import soot.IntType;
import soot.Local;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AbstractJimpleValueSwitch;
import soot.jimple.ArrayRef;
import soot.jimple.CastExpr;
import soot.jimple.Constant;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.IntConstant;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.LengthExpr;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.NullConstant;
import soot.jimple.ParameterRef;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.StringConstant;
import soot.jimple.VirtualInvokeExpr;
import dk.brics.automaton.Automaton;
import dk.brics.string.Resolver;
import dk.brics.string.intermediate.ArrayAssignment;
import dk.brics.string.intermediate.ArrayCorrupt;
import dk.brics.string.intermediate.ArrayFromArray;
import dk.brics.string.intermediate.ArrayNew;
import dk.brics.string.intermediate.Call;
import dk.brics.string.intermediate.Method;
import dk.brics.string.intermediate.StringAssignment;
import dk.brics.string.intermediate.StringBufferAppend;
import dk.brics.string.intermediate.StringBufferAssignment;
import dk.brics.string.intermediate.StringBufferBinaryOp;
import dk.brics.string.intermediate.StringBufferCorrupt;
import dk.brics.string.intermediate.StringBufferInit;
import dk.brics.string.intermediate.StringBufferPrepend;
import dk.brics.string.intermediate.StringBufferUnaryOp;
import dk.brics.string.intermediate.StringConcat;
import dk.brics.string.intermediate.StringFromArray;
import dk.brics.string.intermediate.StringFromStringBuffer;
import dk.brics.string.intermediate.StringInit;
import dk.brics.string.intermediate.Variable;
import dk.brics.string.mlfa.BinaryOperation;
import dk.brics.string.mlfa.UnaryOperation;
import dk.brics.string.operations.Basic;
import dk.brics.string.operations.Delete;
import dk.brics.string.operations.DeleteCharAt;
import dk.brics.string.operations.Insert;
import dk.brics.string.operations.Postfix;
import dk.brics.string.operations.Prefix;
import dk.brics.string.operations.Replace1;
import dk.brics.string.operations.Replace2;
import dk.brics.string.operations.Replace3;
import dk.brics.string.operations.Replace4;
import dk.brics.string.operations.Replace5;
import dk.brics.string.operations.Reverse;
import dk.brics.string.operations.SetCharAt1;
import dk.brics.string.operations.SetCharAt2;
import dk.brics.string.operations.SetLength;
import dk.brics.string.operations.Substring;
import dk.brics.string.operations.ToLowerCase;
import dk.brics.string.operations.ToUpperCase;
import dk.brics.string.operations.Trim;

/** Translation of Jimple expressions. */
public class ExprTranslator extends AbstractJimpleValueSwitch {
    private JavaTranslator jt;
    private StmtTranslator st;
    private Variable res_var;

    public ExprTranslator(JavaTranslator jt, StmtTranslator st) {
	this.jt = jt;
	this.st = st;
    }

    // Called for any type of value
    public void translateExpr(Variable var, ValueBox box) {
	Value val = box.getValue();
	Variable temp = res_var;
	res_var = var;
	val.apply(this);
	res_var = temp;
	st.addMap(box);
    }

    public void caseLocal(Local expr) {
	assign(res_var, st.getLocalVariable(expr));
    }

    public void caseArrayRef(ArrayRef expr) {
	Variable bvar = jt.makeVariable(expr.getBase());
	assign(bvar, st.getLocalVariable((Local)expr.getBase()));
	st.addMap(expr.getBaseBox());
	switch (res_var.type) {
	case Variable.TYPE_STRING:
	    st.addStatement(new StringFromArray(res_var, bvar));
	    break;
	case Variable.TYPE_STRINGBUFFER:
	    corrupt(res_var);
	    break;
	case Variable.TYPE_ARRAY:
	    st.addStatement(new ArrayFromArray(res_var, bvar));
	    break;
	}
    }

    public void caseInstanceFieldRef(InstanceFieldRef expr) {
	// FUTURE: Treat fields more precisely
	corrupt(res_var);
    }

    public void caseStaticFieldRef(StaticFieldRef expr) {
	// FUTURE: Treat fields more precisely
	corrupt(res_var);
    }

    public void caseParameterRef(ParameterRef expr) {
	assign(res_var, st.getParameter(expr.getIndex()));
    }

    public void caseNullConstant(NullConstant expr) {
	empty(res_var);
    }

    public void caseStringConstant(StringConstant expr) {
	st.addStatement(new StringInit(res_var, Basic.makeConstString(expr.value)));
    }

    public void caseCastExpr(CastExpr expr) {
	translateExpr(res_var, expr.getOpBox());
	if (!jt.isSType(expr.getOp())) {
	    corrupt(res_var);
	}
    }

    public void caseLengthExpr(LengthExpr expr) {
	Variable bvar = jt.makeVariable(expr.getOp());
	assign(bvar, st.getLocalVariable((Local)expr.getOp()));
	st.addMap(expr.getOpBox());
    }

    public void caseNewExpr(NewExpr expr) {
	empty(res_var);
    }

    public void caseNewArrayExpr(NewArrayExpr expr) {
	if (jt.isSType(expr)) {
	    st.addStatement(new ArrayNew(res_var));
	}
    }

    public void caseNewMultiArrayExpr(NewMultiArrayExpr expr) {
	if (jt.isSType(expr)) {
	    st.addStatement(new ArrayNew(res_var));
	}
    }

    public void caseSpecialInvokeExpr(SpecialInvokeExpr expr) {
	// Constructor calls, maybe
	Variable bvar = st.getLocalVariable((Local)expr.getBase());
	Variable dummy = jt.makeVariable(expr.getBase());
	assign(dummy, bvar);
	st.addMap(expr.getBaseBox());
	SootMethod m = expr.getMethod();
	if (m.getName().equals("<init>")) {
	    SootClass dc = m.getDeclaringClass();
	    if (isString(dc)) {
		switch (m.getParameterCount()) {
		case 0:
		    st.addStatement(new StringInit(bvar, Basic.makeEmptyString()));
		    break;
		case 1:
		    if (isString(m.getParameterType(0))) {
			translateExpr(bvar, expr.getArgBox(0));
		    } else if (isStringBuffer(m.getParameterType(0))) {
			Variable rvar = new Variable(Variable.TYPE_STRINGBUFFER);
			translateExpr(rvar, expr.getArgBox(0));
			st.addStatement(new StringFromStringBuffer(bvar, rvar));
		    } else {
			st.addStatement(new StringInit(bvar, Basic.makeAnyString()));
		    }
		    break;
		}
		assign(res_var, bvar);
		return;
	    } else if (isStringBuffer(dc)) {	
		Variable temp = new Variable(Variable.TYPE_STRING);
		int np = m.getParameterCount();
		if (np == 0 || np == 1 && isInt(m.getParameterType(0))) {
		    st.addStatement(new StringInit(temp, Basic.makeEmptyString()));
		} else if (np == 1 && isString(m.getParameterType(0))) {
		    translateExpr(temp, expr.getArgBox(0));
		} else {
		    // Unknown StringBuffer constructor
		    st.addStatement(new StringInit(temp, Basic.makeAnyString()));
		}
		st.addStatement(new StringBufferInit(bvar, temp));
		assign(res_var, bvar);
		return;
	    }
	}
	handleCall(expr, expr.getMethod());
    }

    public void caseStaticInvokeExpr(StaticInvokeExpr expr) {
	SootMethod m = expr.getMethod();
	String mn = m.getName();
	int np = m.getParameterCount();
	SootClass dc = m.getDeclaringClass();
	boolean is_string = isString(dc);
	boolean is_wrapper = isWrapperClass(dc);
	if (is_wrapper || is_string) {
	    Variable temp = null;
	    if (is_wrapper) {
		if (mn.equals("toString")) {
		    if (np == 1) {
			temp = valueOf(expr.getArgBox(0), 10);
		    } else if (np == 2 && isInt(m.getParameterType(1))) {
			Integer radix = trackInteger(expr.getArg(1));
			if (radix != null) {
			    temp = valueOf(expr.getArgBox(0), radix.intValue());
			}
		    }
		} else if (mn.equals("toBinaryString") && np == 1) {
		    temp = valueOf(expr.getArgBox(0), 2);
		} else if (mn.equals("toHexString") && np == 1) {
		    temp = valueOf(expr.getArgBox(0), 16);
		} else if (mn.equals("toOctalString") && np == 1) {
		    temp = valueOf(expr.getArgBox(0), 8);
		}
	    } else {
		// String
		if (mn.equals("valueOf") && np == 1) {
		    temp = valueOf(expr.getArgBox(0), 10);
		}
	    }
	    assign(res_var, temp);
	} else {
	    // Not wrapper or string
	    handleCall(expr, expr.getMethod());
	}
    }

    public void caseInterfaceInvokeExpr(InterfaceInvokeExpr expr) {
	caseInstanceInvokeExpr(expr);
    }

    public void caseVirtualInvokeExpr(VirtualInvokeExpr expr) {
	caseInstanceInvokeExpr(expr);
    }

    void caseInstanceInvokeExpr(InstanceInvokeExpr expr) {
	Variable bvar = jt.makeVariable(expr.getBase());
	assign(bvar, st.getLocalVariable((Local)expr.getBase()));
	st.addMap(expr.getBaseBox());
	List/*<SootMethod>*/ targets = jt.getTargetsOf(expr);
	if (targets.isEmpty()) {
	    // This is a call to an interface method or abstract method
	    // with no available implementations.
	    // Use instruction target as target.
	    targets = Collections.singletonList(expr.getMethod());
	}
	st.setBranch();
	Iterator ti = targets.iterator();
	while (ti.hasNext()) {
	    SootMethod target = (SootMethod)ti.next();
	    boolean special = handleSpecialCall(expr, target);
	    if (!special) {
		handleCall(expr, target);
	    }
	    st.useBranch();
	}
	st.endBranch();
	assign(res_var, res_var);
    }

    void assign(Variable lvar, Variable rvar) {
	switch (lvar.type) {
	case Variable.TYPE_STRING:
	    st.addStatement(new StringAssignment(lvar, rvar));
	    break;
	case Variable.TYPE_STRINGBUFFER:
	    st.addStatement(new StringBufferAssignment(lvar, rvar));
	    break;
	case Variable.TYPE_ARRAY:
	    st.addStatement(new ArrayAssignment(lvar, rvar));
	    break;
	}
    }

    void empty(Variable lvar) {
	switch (lvar.type) {
	case Variable.TYPE_STRING:
	    st.addStatement(new StringInit(lvar, Basic.makeNoString()));
	    break;
	case Variable.TYPE_STRINGBUFFER:
	    Variable temp = new Variable(Variable.TYPE_STRING);
	    st.addStatement(new StringInit(temp, Basic.makeNoString()));
	    st.addStatement(new StringBufferInit(lvar, temp));
	    break;
	case Variable.TYPE_ARRAY:
	    st.addStatement(new ArrayNew(lvar));
	    break;
	}
    }

    // Non-escaping corrupt
    void corrupt(Variable var) {
	switch (var.type) {
	case Variable.TYPE_STRING:
	    st.addStatement(new StringInit(var, Basic.makeAnyString()));
	    break;
	case Variable.TYPE_STRINGBUFFER:
	    Variable temp = new Variable(Variable.TYPE_STRING);
	    st.addStatement(new StringInit(temp, Basic.makeNoString()));
	    st.addStatement(new StringBufferInit(var, temp));
	    st.addStatement(new StringBufferCorrupt(var));
	    break;
	case Variable.TYPE_ARRAY:
	    st.addStatement(new ArrayNew(var));
	    st.addStatement(new ArrayCorrupt(var));
	    break;
	default:
	    Variable dummy = new Variable(Variable.TYPE_STRING);
	    st.addStatement(new StringInit(dummy, Basic.makeAnyString()));
	    break;
	}
    }

    // Escaping corrupt
    void escape(Variable var) {
	switch (var.type) {
	case Variable.TYPE_STRINGBUFFER:
	    st.addStatement(new StringBufferCorrupt(var));
	    break;
	case Variable.TYPE_ARRAY:
	    st.addStatement(new ArrayCorrupt(var));
	    break;
	}
    }

    boolean isString(Type t) {
	return t.equals(RefType.v("java.lang.String"));
    }

    boolean isString(SootClass c) {
	return c.getName().equals("java.lang.String");
    }

    boolean isStringBuffer(Type t) {
	return t.equals(RefType.v("java.lang.StringBuffer"));
    }

    boolean isStringBuffer(SootClass c) {
	return c.getName().equals("java.lang.StringBuffer");
    }

    boolean isInt(Type t) {
	return t.equals(IntType.v());
    }

    boolean isChar(Type t) {
	return t.equals(CharType.v());
    }

    boolean isWrapperClass(SootClass c) {
	return
	    c.getName().equals("java.lang.Boolean") ||
	    c.getName().equals("java.lang.Byte") ||
	    c.getName().equals("java.lang.Character") ||
	    c.getName().equals("java.lang.Double") ||
	    c.getName().equals("java.lang.Float") ||
	    c.getName().equals("java.lang.Integer") ||
	    c.getName().equals("java.lang.Long") ||
	    c.getName().equals("java.lang.Short");
    }

    // For a single target, s-type or not, non-special
    void handleCall(InvokeExpr expr, SootMethod target) {
	SootClass dc = target.getDeclaringClass();
	if (jt.isApplicationClass(dc)) {
	    if (dc.isInterface()) {
		// Call to interface method or abstract method as target.
		// Only occurs if no implementions are found.
		// Arguments are evaluated and nothing is returned.
		for (int i = 0 ; i < expr.getArgCount() ; i++) {
		    Value arg = expr.getArg(i);
		    if (jt.isSType(arg)) {
			Variable var = jt.makeVariable(arg);
			translateExpr(var, expr.getArgBox(i));
		    }
		}
		empty(res_var);
	    } else {
		// Target in an application class.
		// Setup call to translated method.
		Method method = (Method)jt.sms_m.get(target.getSignature());
		int[] sa_ma = (int[])jt.sms_sa_ma.get(target.getSignature());
		Variable[] args = new Variable[method.getEntry().params.length];
		for (int i = 0 ; i < expr.getArgCount() ; i++) {
		    Value arg = expr.getArg(i);
		    if (jt.isSType(arg)) {
			Variable var = jt.makeVariable(arg);
			translateExpr(var, expr.getArgBox(i));
			if (sa_ma[i] != -1) {
			    args[sa_ma[i]] = var;
			}
		    }
		}
		// Insert an extra assignment to seperate call from assignment
		Variable retvar = jt.makeVariable(expr);
		st.addStatement(new Call(retvar, method, args));
		assign(res_var, retvar);
	    }
	} else {
	    // Target in non-application class.
	    // Escape all arguments and corrupt result.
	    for (int i = 0 ; i < expr.getArgCount() ; i++) {
		Value arg = expr.getArg(i);
		if (jt.isSType(arg)) {
		    if (expr.getArg(i) instanceof StringConstant && !jt.isHotspot(expr.getArgBox(i))) {
			jt.skipped++;
		    } else {
			Variable var = jt.makeVariable(arg);
			translateExpr(var, expr.getArgBox(i));
			escape(var);
		    }
		}
	    }
	    Object a = resolveCall(expr, target);
	    if (a != null) {
		if (a instanceof Automaton) {
		    st.addStatement(new StringInit(res_var, (Automaton)a));
		} else if (a instanceof ValueBox) {
		    translateExpr(res_var, (ValueBox)a);
		} else {
		    throw new RuntimeException("Invalid type returned from resolver: "+a.getClass().getName());
		}
	    } else {
		corrupt(res_var);
	    }
	}
    }

    Object resolveCall(InvokeExpr expr, SootMethod target) {
	Iterator ri = jt.resolvers.iterator();
	while (ri.hasNext()) {
	    Resolver r = (Resolver)ri.next();
	    Object a = r.resolveMethod(expr, target);
	    if (a != null) {
		return a;
	    }
	}
	return null;
    }

    // any type
    boolean handleSpecialCall(InstanceInvokeExpr expr, SootMethod target) {
	Value base = expr.getBase();
	Variable bvar = st.getLocalVariable((Local)base);
	String mn = target.getName();
	int np = target.getParameterCount();
	SootClass dc = target.getDeclaringClass();

	if (isString(dc)) {
	    if (mn.equals("toString") && np == 0) {
		assign(res_var, bvar);
		return true;
	    } else if (mn.equals("intern") && np == 0) {
		assign(res_var, bvar);
		return true;
	    } else if (mn.equals("concat") && np == 1 && isString(target.getParameterType(0)) ){
		Variable lvar = new Variable(Variable.TYPE_STRING);
		assign(lvar, bvar);

		Variable rvar = new Variable(Variable.TYPE_STRING);
		translateExpr(rvar, expr.getArgBox(0));

		st.addStatement(new StringConcat(res_var, lvar, rvar));
		return true;
	    } else if (mn.equals("replace") && np == 2 &&
		       isChar(target.getParameterType(0)) &&
		       isChar(target.getParameterType(1))) {
		Integer arg1 = trackInteger(expr.getArg(0));
		Integer arg2 = trackInteger(expr.getArg(1));
		UnaryOperation op;
		if (arg1 != null) {
		    if (arg2 != null) {
			op = new Replace1((char)arg1.intValue(), (char)arg2.intValue());
		    } else {
			op = new Replace2((char)arg1.intValue());
		    }
		} else {
		    if (arg2 != null) {
			op = new Replace3((char)arg2.intValue());
		    } else {
			op = new Replace4();
		    }
		}
		Variable temp = new Variable(Variable.TYPE_STRINGBUFFER);
		st.addStatement(new StringBufferInit(temp, bvar));
		st.addStatement(new StringBufferUnaryOp(temp, op));
		st.addStatement(new StringFromStringBuffer(res_var, temp));
		return true;
	    } else if (mn.equals("trim") && np == 0) {
		UnaryOperation op = new Trim();
		Variable temp = new Variable(Variable.TYPE_STRINGBUFFER);
		st.addStatement(new StringBufferInit(temp, bvar));
		st.addStatement(new StringBufferUnaryOp(temp, op));
		st.addStatement(new StringFromStringBuffer(res_var, temp));
		return true;
	    } else if (mn.equals("substring") && np == 1) {
		UnaryOperation op = new Postfix();
		Variable temp = new Variable(Variable.TYPE_STRINGBUFFER);
		st.addStatement(new StringBufferInit(temp, bvar));
		st.addStatement(new StringBufferUnaryOp(temp, op));
		st.addStatement(new StringFromStringBuffer(res_var, temp));
		return true;
	    } else if (mn.equals("substring") && np == 2) {
		UnaryOperation op;
		Integer arg1 = trackInteger(expr.getArg(0));
		if (arg1 != null && arg1.intValue() == 0) {
		    op = new Prefix();
		} else {
		    op = new Substring();
		}
		Variable temp = new Variable(Variable.TYPE_STRINGBUFFER);
		st.addStatement(new StringBufferInit(temp, bvar));
		st.addStatement(new StringBufferUnaryOp(temp, op));
		st.addStatement(new StringFromStringBuffer(res_var, temp));
		return true;
	    } else if (mn.equals("toLowerCase") && np == 0) {
		UnaryOperation op = new ToLowerCase();
		Variable temp = new Variable(Variable.TYPE_STRINGBUFFER);
		st.addStatement(new StringBufferInit(temp, bvar));
		st.addStatement(new StringBufferUnaryOp(temp, op));
		st.addStatement(new StringFromStringBuffer(res_var, temp));
		return true;
	    } else if (mn.equals("toUpperCase") && np == 0) {
		UnaryOperation op = new ToUpperCase();
		Variable temp = new Variable(Variable.TYPE_STRINGBUFFER);
		st.addStatement(new StringBufferInit(temp, bvar));
		st.addStatement(new StringBufferUnaryOp(temp, op));
		st.addStatement(new StringFromStringBuffer(res_var, temp));
		return true;
	    }
	    // Unknown String method
	    return false;
	} else if (isStringBuffer(dc)) {
	    if (mn.equals("toString") && np == 0) {
		Variable lvar = new Variable(Variable.TYPE_STRINGBUFFER);
		assign(lvar, bvar);
		st.addStatement(new StringFromStringBuffer(res_var, lvar));
		return true;
	    } else if (mn.equals("append") && np == 1) {
		assign(res_var, bvar);
		Variable rvar = valueOf(expr.getArgBox(0), 10);
		st.addStatement(new StringBufferAppend(res_var, rvar));
		return true;
	    } else if (mn.equals("insert") && np == 2 &&
		       isInt(target.getParameterType(0))) {
		assign(res_var, bvar);
		Integer pos = trackInteger(expr.getArg(0));
		Variable rvar = valueOf(expr.getArgBox(1), 10);
		if (pos != null && pos.intValue() == 0) {
		    st.addStatement(new StringBufferPrepend(res_var, rvar));
		} else {
		    st.addStatement(new StringBufferBinaryOp(res_var, new Insert(), rvar));
		}
		return true;
	    } else if (mn.equals("delete") && np == 2) {
		UnaryOperation op = new Delete();
		assign(res_var, bvar);
		st.addStatement(new StringBufferUnaryOp(res_var, op));
		return true;
	    } else if (mn.equals("deleteCharAt") && np == 1) {
		UnaryOperation op = new DeleteCharAt();
		assign(res_var, bvar);
		st.addStatement(new StringBufferUnaryOp(res_var, op));
		return true;
	    } else if (mn.equals("replace") && np == 3) {
		BinaryOperation op = new Replace5();
		assign(res_var, bvar);
		Variable rvar = valueOf(expr.getArgBox(2), 10);
		st.addStatement(new StringBufferBinaryOp(res_var, op, rvar));
		return true;
	    } else if (mn.equals("reverse") && np == 0) {
		UnaryOperation op = new Reverse();
		assign(res_var, bvar);
		st.addStatement(new StringBufferUnaryOp(res_var, op));
		return true;
	    } else if (mn.equals("setCharAt") && np == 2) {
		assign(res_var, bvar);
		Integer c = trackInteger(expr.getArg(1));
		if (c==null) {
		    UnaryOperation op = new SetCharAt2();
		    st.addStatement(new StringBufferUnaryOp(res_var, op));
		} else {
		    UnaryOperation op = new SetCharAt1((char) c.intValue());
		    st.addStatement(new StringBufferUnaryOp(res_var, op));
		}
		return true;
	    } else if (mn.equals("setLength") && np == 1) {
		UnaryOperation op = new SetLength();
		assign(res_var, bvar);
		st.addStatement(new StringBufferUnaryOp(res_var, op));
		return true;
	    } else if (mn.equals("substring") && np == 1) {
		UnaryOperation op = new Postfix();
		assign(res_var, bvar);
		st.addStatement(new StringBufferUnaryOp(res_var, op));
		return true;
	    } else if (mn.equals("substring") && np == 2) {
		UnaryOperation op;
		Integer arg1 = trackInteger(expr.getArg(0));
		if (arg1 != null && arg1.intValue() == 0) {
		    op = new Prefix();
		} else {
		    op = new Substring();
		}
		assign(res_var, bvar);
		st.addStatement(new StringBufferUnaryOp(res_var, op));
		return true;
	    }
	    // Unknown StringBuffer method
	    return false;
	} else if (isWrapperClass(dc)) {
	    if (mn.equals("toString") && np == 0) {
		Variable rvar = valueOf(expr.getBaseBox(), 10, false);
		assign(res_var, rvar);
		return true;
	    }
	    // Unknown wrapper method
	    return false;
	}

	// Not a special call
	return false;
    }

    Integer trackInteger(Value val) {
	if (val instanceof IntConstant) {
	    return new Integer(((IntConstant)val).value);
	}
	// TODO: Make some more intelligent tracking
	return null;
    }

    Variable valueOf(ValueBox box, int radix) {
	return valueOf(box, radix, true);
    }

    // Skal selv tilf?je n?dvendige ting til map
    Variable valueOf(ValueBox box, int radix, boolean nullcheck) {
	Value val = box.getValue();
	// If the value is a local that can be null,
	// "null" is also a possibility.
	if (nullcheck && val instanceof Local && jt.canBeNull((Local)val)) {
	    st.setBranch();
	    Variable var = valueOf(box, radix, false);
	    st.useBranch();
	    st.addStatement(new StringInit(var, Basic.makeConstString("null")));
	    st.useBranch();
	    st.endBranch();
	    return var;
	}
	Variable res = new Variable(Variable.TYPE_STRING);
	if (jt.isSType(val)) {
	    Variable temp = jt.makeVariable(val);
	    translateExpr(temp, box);
	    switch (temp.type) {
	    case Variable.TYPE_STRING:
		st.addStatement(new StringAssignment(res, temp));
		break;
	    case Variable.TYPE_STRINGBUFFER:
		st.addStatement(new StringFromStringBuffer(res, temp));
		break;
	    case Variable.TYPE_ARRAY:
		st.addStatement(new StringFromArray(res, temp));
		break;
	    }
	} else if (radix == 10 && val instanceof Constant) {
	    // CLARIFY: is the toString() method of Constant always returning the right thing?
	    String s = val.toString();
	    st.addStatement(new StringInit(res, Basic.makeConstString(s)));
	} else if (radix == 10 && jt.getTypeAutomaton(val.getType()) != null) {
	    st.addStatement(new StringInit(res, jt.getTypeAutomaton(val.getType())));
	} else if (val.getType() instanceof RefType) {
	    // Call the corresponding toString method
	    Method tostring_method = jt.getToStringMethod(((RefType)val.getType()).getSootClass());
	    if (tostring_method != null) {
		st.addStatement(new Call(res, tostring_method, new Variable[0]));
	    } else {
		st.addStatement(new StringInit(res, Basic.makeAnyString()));
	    }
	} else {
	    // If all else fails, give any string
	    st.addStatement(new StringInit(res, Basic.makeAnyString()));
	}
	return res;
    }

}
