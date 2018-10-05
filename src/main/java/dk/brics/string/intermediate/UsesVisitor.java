
package dk.brics.string.intermediate;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

// TODO: description of 'used'

/** A statement visitor for querying the set of variables
 *  used by a statement.
 */
public class UsesVisitor implements StatementVisitor {
    private AliasAnalysis aa;

    private Statement s;

    private Set/*<Variable>*/ vars;

    public UsesVisitor() {
	this(null);
    }

    public UsesVisitor(AliasAnalysis aa) {
	this.aa = aa;
    }

    public boolean uses(Statement s, Variable var) {
	return usedVars(s).contains(var);
    }

    public Set/*<Variable>*/ usedVars(Statement s) {
	this.s = s;
	s.visitBy(this);
	return vars;
    }

    private void aliasOp(Variable to) {
	vars = new HashSet();
	vars.add(to);
	if (aa != null) {
	    vars.addAll(aa.getInfoBefore(s).getAliasesFor(to));
	}
    }

    private void aliasOp(Variable to, Variable from) {
	vars = new HashSet();
	vars.add(to);
	vars.add(from);
	if (aa != null) {
	    vars.addAll(aa.getInfoBefore(s).getAliasesFor(to));
	}
    }

    private void singletonOp(Variable from) {
	vars = Collections.singleton(from);
    }

    private void noneOp() {
	vars = Collections.EMPTY_SET;
    }

    public void visitArrayAssignment(ArrayAssignment s)
    { singletonOp(s.from); }
    public void visitArrayCorrupt(ArrayCorrupt s)
    { noneOp(); }
    public void visitArrayFromArray(ArrayFromArray s)
    { singletonOp(s.from); }
    public void visitArrayNew(ArrayNew s)
    { noneOp(); }
    public void visitArrayWriteArray(ArrayWriteArray s)
    { aliasOp(s.to, s.from); }
    public void visitArrayWriteString(ArrayWriteString s)
    { aliasOp(s.to, s.from); }
    public void visitCall(Call s) {
	vars = new HashSet();
	for (int i = 0 ; i < s.args.length ; i++) {
	    vars.add(s.args[i]);
	}
    }
    public void visitMethodHead(MethodHead s)
    { noneOp(); }
    public void visitNop(Nop s)
    { noneOp(); }
    public void visitReturn(Return s) {
	vars = new HashSet();
	vars.add(s.retvar);
	Variable[] pa = s.getMethod().getParamAlias();
	for (int i = 0 ; i < pa.length ; i++) {
	    if (pa[i] != null) vars.add(pa[i]);
	}
    }
    public void visitStringAssignment(StringAssignment s)
    { singletonOp(s.from); }
    public void visitStringBufferAppend(StringBufferAppend s)
    { aliasOp(s.to, s.from); }
    public void visitStringBufferAssignment(StringBufferAssignment s)
    { singletonOp(s.from); }
    public void visitStringBufferBinaryOp(StringBufferBinaryOp s) 
    { aliasOp(s.to, s.from); }
    public void visitStringBufferCorrupt(StringBufferCorrupt s)
    { noneOp(); }
    public void visitStringBufferInit(StringBufferInit s)
    { singletonOp(s.from); }
    public void visitStringBufferPrepend(StringBufferPrepend s)
    { aliasOp(s.to, s.from); }
    public void visitStringBufferUnaryOp(StringBufferUnaryOp s)
    { aliasOp(s.to); }
    public void visitStringConcat(StringConcat s) {
	vars = new HashSet();
	vars.add(s.left);
	vars.add(s.right);
    }
    public void visitStringFromArray(StringFromArray s)
    { singletonOp(s.from); }
    public void visitStringFromStringBuffer(StringFromStringBuffer s)
    { singletonOp(s.from); }
    public void visitStringInit(StringInit s)
    { noneOp(); }

}
