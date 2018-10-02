
package dk.brics.string.java;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import soot.Local;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceFileTag;
import soot.tagkit.Tag;
import dk.brics.string.intermediate.ArrayCorrupt;
import dk.brics.string.intermediate.ArrayWriteArray;
import dk.brics.string.intermediate.ArrayWriteString;
import dk.brics.string.intermediate.Method;
import dk.brics.string.intermediate.Nop;
import dk.brics.string.intermediate.Return;
import dk.brics.string.intermediate.Statement;
import dk.brics.string.intermediate.StringBufferCorrupt;
import dk.brics.string.intermediate.StringInit;
import dk.brics.string.intermediate.Variable;
import dk.brics.string.operations.Basic;

/** Translation of Jimple statements. */
public class StmtTranslator extends AbstractStmtSwitch {
    private ExprTranslator et;
    private JavaTranslator jt;

    Map/*<Local,Variable>*/ local_var = new HashMap();

    String current_sourcefile;
    SootClass current_class;
    SootMethod current_method;
    int current_line;

    Map/*<ValueBox,Statement>*/ trans_map = new HashMap();
    Map/*<ValueBox,String>*/ sourcefile_map = new HashMap();
    Map/*<ValueBox,String>*/ class_map = new HashMap();
    Map/*<ValueBox,String>*/ method_map = new HashMap();
    Map/*<ValueBox,Integer>*/ line_map = new HashMap();

    private Statement first_statement;
    private Statement last_statement;
    private Statement branch_start;
    private Statement branch_end;
    private Map/*<Stmt,Statement>*/ stmt_first = new HashMap();
    private Map/*<Stmt,Statement>*/ stmt_last = new HashMap();

    public StmtTranslator(JavaTranslator jt) {
	this.jt = jt;
	et = new ExprTranslator(jt, this);
    }

    public void setCurrentClass(SootClass current_class) {
	current_sourcefile = "";
	Iterator ti = current_class.getTags().iterator();
	while (ti.hasNext()) {
	    Tag tag = (Tag)ti.next();
	    if (tag instanceof SourceFileTag) {
		current_sourcefile = ((SourceFileTag)tag).getSourceFile();
	    }
	}
	this.current_class = current_class;
    }

    public void setCurrentMethod(SootMethod current_method) {
	this.current_method = current_method;
    }

    public void translateStmt(Stmt stmt) {
	Iterator ti = stmt.getTags().iterator();
	while (ti.hasNext()) {
	    Tag tag = (Tag)ti.next();
	    if (tag instanceof LineNumberTag) {
		current_line = Integer.parseInt(tag.toString());
	    }
	}

	first_statement = null;
	last_statement = null;
	stmt.apply(this);
	if (first_statement == null) {
	    addStatement(new Nop());
	}
	stmt_first.put(stmt, first_statement);
	stmt_last.put(stmt, last_statement);
    }

    public Statement getFirst(Stmt stmt) {
	return (Statement)stmt_first.get(stmt);
    }

    public Statement getLast(Stmt stmt) {
	return (Statement)stmt_last.get(stmt);
    }

    void addStatement(Statement s) {
	((Method)jt.sms_m.get(current_method.getSignature())).addStatement(s);
	if (first_statement == null) {
	    first_statement = s;
	} else {
	    last_statement.addSucc(s);
	}
	last_statement = s;
    }

    /** Called before the branches */
    void setBranch() {
	if (first_statement == null) {
	    addStatement(new Nop());
	}
	branch_start = last_statement;
	branch_end = new Nop();
    }

    /** Called after each branch */
    void useBranch() {
	last_statement.addSucc(branch_end);
	last_statement = branch_start;
    }

    void endBranch() {
	((Method)jt.sms_m.get(current_method.getSignature())).addStatement(branch_end);
	last_statement = branch_end;
    }


    public void caseInvokeStmt(InvokeStmt stmt) {
	InvokeExpr expr = (InvokeExpr)stmt.getInvokeExpr();
	Variable lvar = jt.makeVariable(expr);
	et.translateExpr(lvar, stmt.getInvokeExprBox());
    }

    public void caseAssignStmt(AssignStmt stmt) {
	handleAssign(stmt);
    }

    public void caseIdentityStmt(IdentityStmt stmt) {
	handleAssign(stmt);
    }

    public void caseReturnStmt(ReturnStmt stmt) {
	Variable rvar = jt.makeVariable(stmt.getOp());
	et.translateExpr(rvar, stmt.getOpBox());
	addStatement(new Return(rvar));
    }

    public void caseReturnVoidStmt(ReturnVoidStmt stmt) {
	addStatement(new Return(new Variable(Variable.TYPE_NONE)));
    }


    public void defaultCase(Stmt stmt) {
	// Do nothing
    }

    void handleAssign(DefinitionStmt stmt) {
	Value lval = stmt.getLeftOp();
	Value rval = stmt.getRightOp();
	Variable rvar;
	if (lval instanceof Local) {
	    rvar = getLocalVariable((Local)lval);
	} else {
	    rvar = jt.makeVariable(rval);
	}
	et.translateExpr(rvar, stmt.getRightOpBox());
	if (lval instanceof ArrayRef) {
	    Variable lvar = getLocalVariable((Local)((ArrayRef)lval).getBase());

	    switch (rvar.type) {
	    case Variable.TYPE_STRING:
		addStatement(new ArrayWriteString(lvar, rvar));
		// Map from base to resulting value
		addMap(((ArrayRef)lval).getBaseBox());
		break;
	    case Variable.TYPE_STRINGBUFFER:
		addStatement(new StringBufferCorrupt(rvar));
		// Base not of s-type - so no map
		break;
	    case Variable.TYPE_ARRAY:
		addStatement(new ArrayWriteArray(lvar, rvar));
		// Map from base to resulting value
		addMap(((ArrayRef)lval).getBaseBox());
		break;
	    }

	} else if (lval instanceof FieldRef) {
	    // FUTURE: Treat fields more precisely

	    // No extra boxes to map from
	    switch (rvar.type) {
	    case Variable.TYPE_STRINGBUFFER:
		addStatement(new StringBufferCorrupt(rvar));
		break;
	    case Variable.TYPE_ARRAY:
		addStatement(new ArrayCorrupt(rvar));
		break;
	    }
	    addStatement(new StringInit(new Variable(Variable.TYPE_STRING), Basic.makeAnyString()));
	}
	addMap(stmt.getLeftOpBox());
    }

    void addMap(ValueBox box) {
	if (jt.isSType(box.getValue())) {
	    if (last_statement != null) {
		trans_map.put(box, last_statement);
		sourcefile_map.put(box, current_sourcefile);
		class_map.put(box, current_class.getName());
		method_map.put(box, current_method.getName());
		line_map.put(box, new Integer(current_line));
	    } else {
		// Internal error
		throw new Error("Internal error: No statement for "+box.getValue());
	    }
	}
    }

    Variable getLocalVariable(Local l) {
	if (local_var.containsKey(l)) {
	    return (Variable)local_var.get(l);
	}
	Variable var = jt.makeVariable(l);
	local_var.put(l, var);
	return var;
    }

    Variable getParameter(int index) {
	int ma = ((int[])jt.sms_sa_ma.get(current_method.getSignature()))[index];
	if (ma != -1) {
	    return ((Method)jt.sms_m.get(current_method.getSignature())).getEntry().params[ma];
	} else {
	    return new Variable(Variable.TYPE_NONE);
	}
    }


}
