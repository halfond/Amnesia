
package dk.brics.string.intermediate;

/** Visitor for producing string representations of statements.
 *  Used by the {@link dk.brics.string.intermediate.Statement#toString toString}
 *  method of {@link dk.brics.string.intermediate.Statement}.
 */
public class ToStringVisitor implements StatementVisitor {
    /** The string representaion, set by the visit methods */
    public String result;

    public void visitArrayAssignment(ArrayAssignment s) {
	result = s.to+" = "+s.from+";";
    }

    public void visitArrayCorrupt(ArrayCorrupt s) {
	result = "corrupt "+s.to+";";
    }

    public void visitArrayFromArray(ArrayFromArray s) {
	result = s.to+" = "+s.from+"[];";
    }

    public void visitArrayNew(ArrayNew s) {
	result = s.to+" = new [];";
    }

    public void visitArrayWriteArray(ArrayWriteArray s) {
	result = s.to+"[] = "+s.from;
    }

    public void visitArrayWriteString(ArrayWriteString s) {
	result = s.to+"[] = "+s.from;
    }

    public void visitCall(Call s) {
	StringBuffer sb = new StringBuffer();
	sb.append(s.retvar);
	sb.append(" = ");
	sb.append(s.target.getName());
	sb.append("(");
	for (int i = 0 ; i < s.args.length ; i++) {
	    sb.append(s.args[i]);
	    if (i < s.args.length-1) {
		sb.append(", ");
	    }
	}
	sb.append(");");
	result = sb.toString();
    }

    public void visitMethodHead(MethodHead s) {
	StringBuffer sb = new StringBuffer();
	sb.append("(");
	for (int i = 0 ; i < s.params.length ; i++) {
	    sb.append(s.params[i]);
	    if (i < s.params.length-1) {
		sb.append(", ");
	    }
	}
	sb.append(")");
	result = sb.toString();
    }

    public void visitNop(Nop s) {
	result = "nop;";
    }

    public void visitReturn(Return s) {
	result = "return "+s.retvar+";";
    }

    public void visitStringAssignment(StringAssignment s) {
	result = s.to+" = "+s.from+";";
    }

    public void visitStringBufferAppend(StringBufferAppend s) {
	result = s.to+".append("+s.from+");";
    }

    public void visitStringBufferAssignment(StringBufferAssignment s) {
	result = s.to+" = "+s.from+";";
    }

    public void visitStringBufferBinaryOp(StringBufferBinaryOp s) {
	result = s.to+"."+s.op+"("+s.from+");";
    }

    public void visitStringBufferCorrupt(StringBufferCorrupt s) {
	result = "corrupt "+s.to+";";
    }

    public void visitStringBufferInit(StringBufferInit s) {
	result = s.to+" = new("+s.from+");";
    }

    public void visitStringBufferPrepend(StringBufferPrepend s) {
	result = s.to+".prepend("+s.from+");";
    }

    public void visitStringBufferUnaryOp(StringBufferUnaryOp s) {
	result = s.to+"."+s.op+"();";
    }

    public void visitStringConcat(StringConcat s) {
	result = s.to+" = "+s.left+" + "+s.right+";";
    }

    public void visitStringFromArray(StringFromArray s) {
	result = s.to+" = "+s.from+"[];";
    }

    public void visitStringFromStringBuffer(StringFromStringBuffer s) {
	result = s.to+" = "+s.from+".s.toString();";
    }

    public void visitStringInit(StringInit s) {
	result = s.to+" = "+s.regexp.getInfo()+";";
    }

}
