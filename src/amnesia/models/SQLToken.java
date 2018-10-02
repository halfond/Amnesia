package amnesia.models;

public class SQLToken {

	public static final int NONE=0;
    public static final int KEYWORD=1;
    public static final int OP=2;
    public static final int VAR=3;
    public static final int STRING=4;
    public static final int TEXTFIELD=5;
    public static final int QUOTE=6;
    public static final int SCONSTANT=7;
    public static final int NCONSTANT=8;
    public static final int IDENTIFIER=9;
    public static final int WHITESPACE=10;
    public static final int COMMENT=11;
    
    String label;
    int type;
    SQLToken next = null;
    
    public SQLToken() {
    	
    }
    
    public boolean isLiteral() {
    	return ((type == TEXTFIELD) || (type==STRING) || (type==SCONSTANT) || (type==NCONSTANT));
    }
    
    public String getLabel() {
    	return label;
    }
    
    public int getType() {
    	return type;
    }
    
    public void setNext(SQLToken st) {
    	next = st;
    }
    
    public SQLToken getNext() {
    	return next;
    }
}
