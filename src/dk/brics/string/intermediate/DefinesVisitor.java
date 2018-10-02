
package dk.brics.string.intermediate;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

// TODO: description of 'defined'

/** A statement visitor for querying the set of variables
 *  defined by a statement.
 */
public class DefinesVisitor implements StatementVisitor {
    private AliasAnalysis aa;
    private boolean strict;

    private Statement s;

    private Set/*<Variable>*/ vars;

    public DefinesVisitor() {
	this(null);
    }

    public DefinesVisitor(AliasAnalysis aa) {
	this(aa, false);
    }

    public DefinesVisitor(AliasAnalysis aa, boolean strict) {
	this.aa = aa;
	this.strict = strict;
    }

    public boolean defines(Statement s, Variable var) {
	return definedVars(s).contains(var);
    }

    public Set/*<Variable>*/ definedVars(Statement s) {
	this.s = s;
	s.visitBy(this);
	return vars;
    }

    private void aliasOp(Variable to) {
	vars = new HashSet();
	vars.add(to);
	if (aa != null) {
	    vars.addAll(aa.getInfoBefore(s).getAliasesFor(to));
	    if (strict) {
		vars.removeAll(aa.getInfoBefore(s).getMaybeFor(to));
	    }
	}
    }

    private void singletonOp(Variable to) {
	vars = Collections.singleton(to);
    }

    private void noneOp() {
	vars = Collections.EMPTY_SET;
    }

    public void visitArrayAssignment(ArrayAssignment s)
    { singletonOp(s.to); }
    public void visitArrayCorrupt(ArrayCorrupt s)
    { aliasOp(s.to); }
    public void visitArrayFromArray(ArrayFromArray s)
    { singletonOp(s.to); }
    public void visitArrayNew(ArrayNew s)
    { singletonOp(s.to); }
    public void visitArrayWriteArray(ArrayWriteArray s)
    { if (strict) noneOp(); else aliasOp(s.to); }
    public void visitArrayWriteString(ArrayWriteString s)
    { if (strict) noneOp(); else aliasOp(s.to); }
    public void visitCall(Call s) {
	vars = new HashSet();
	vars.add(s.retvar);
	if (aa != null) {
	    for (int i = 0 ; i < s.args.length ; i++) {
		Set vars_add = new HashSet(aa.getInfoBefore(s).getAliasesFor(s.args[i]));
		if (strict) {
		    vars_add.removeAll(aa.getInfoBefore(s).getMaybeFor(s.args[i]));
		}
		vars.addAll(vars_add);
	    }
	}
    }
    public void visitMethodHead(MethodHead s) {
	vars = new HashSet();
	Variable[] pa = s.getMethod().getParamAlias();
	for (int i = 0 ; i < s.params.length ; i++) {
	    vars.add(s.params[i]);
	    if (aa != null && pa[i] != null) vars.add(pa[i]);
	}
    }
    public void visitNop(Nop s)
    { noneOp(); }
    public void visitReturn(Return s)
    { noneOp(); }
    public void visitStringAssignment(StringAssignment s)
    { singletonOp(s.to); }
    public void visitStringBufferAppend(StringBufferAppend s)
    { aliasOp(s.to); }
    public void visitStringBufferAssignment(StringBufferAssignment s)
    { singletonOp(s.to); }
    public void visitStringBufferBinaryOp(StringBufferBinaryOp s)
    { aliasOp(s.to); }
    public void visitStringBufferCorrupt(StringBufferCorrupt s)
    { aliasOp(s.to); }
    public void visitStringBufferInit(StringBufferInit s)
    { singletonOp(s.to); }
    public void visitStringBufferPrepend(StringBufferPrepend s)
    { aliasOp(s.to); }
    public void visitStringBufferUnaryOp(StringBufferUnaryOp s)
    { aliasOp(s.to); }
    public void visitStringConcat(StringConcat s)
    { singletonOp(s.to); }
    public void visitStringFromArray(StringFromArray s)
    { singletonOp(s.to); }
    public void visitStringFromStringBuffer(StringFromStringBuffer s)
    { singletonOp(s.to); }
    public void visitStringInit(StringInit s)
    { singletonOp(s.to); }

}
