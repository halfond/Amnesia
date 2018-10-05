/*
 *Copyright 2005 
 *Georgia Tech Research Corporation
 *Atlanta, GA  30332-0415
 *All Rights Reserved
 */

package edu.usc.sql.models;

import java.io.Serializable;

import edu.usc.sql.amnesia.lexer.LexicalToken;


public class TokenTransition implements Serializable {
    private static final long serialVersionUID = 1L;
    
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
	
    private static int globalID=0;
	
	protected String label;
	protected int type;
	protected int id;
	protected int stringIndex=-1;
	protected Token source;
	protected Token dest;
	protected long val=0;

	
    public TokenTransition() {
    	
    }
    
	public TokenTransition(Token from, Token to, String label, int type) {
		this(from, to, label, type, -1);
	}
	
    public TokenTransition(Token from, Token to, LexicalToken info) {
            this(from, to, info.getLabel(), info.getType(), info.getStringIndex());
    }    
     
    public TokenTransition(Token from, Token to, String label, int type, int stringIndex) {
        this.source=from;
        this.dest=to;
        this.label=label;
        this.type=type;
        this.stringIndex=stringIndex;
        id=globalID;
        globalID++; 
        if ((source==null) || (dest==null) || (label==null)) {throw new RuntimeException();}
    }
    
	public boolean isTextfield() {
		return ((type==VAR) || (type == TEXTFIELD) || (type==STRING) || (type==SCONSTANT) || (type==NCONSTANT));
	}
	
	public Token getDest() {
		return dest;
	}
	
	public Token getSource() {
		return source;
	}
	
    public void setSource(Token s) {
        source=s;
    }
    
    public void setDest(Token d) {
        dest=d;
    }
    
	public int getType() {
		return type;
	}
    
    public int getIndex() {
        return stringIndex;
    }
	
    public String getLabel() {
        return label;
    }
    
    public int getID() {
        return id;
    }
    
	public boolean matches(TokenTransition t) {
		boolean varMatch = (((type==VAR) && (t.isTextfield())) || ((t.getType()==VAR) && (isTextfield())));
        if (varMatch) {
            return true;
        } else {
            //boolean typeMatch = (type == t.getType());
            boolean typeMatch=true;
            boolean labelMatch = label.equalsIgnoreCase(t.getLabel());
            if (typeMatch && labelMatch) {
                return true;
            } else {
                return false;
            }
        }
	}
	
	public boolean equals (Object o) {
		try {
		if (o instanceof TokenTransition) {
			TokenTransition t = (TokenTransition)o;
			if ((t!=null) && (dest != null) && (source != null) && (label!=null)) {
				if ((dest.id == t.dest.id) && (label.equalsIgnoreCase(t.label)) && (source.id == t.source.id) ) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else {
			return super.equals(o);
		}} catch (NullPointerException npe) {
			return false;
		}
	}
	
	public int hashCode() {
		if ((dest==null) || (label==null)) {
			return 0;
		} else {
			return (dest.id + label.hashCode());
		}
	}
	
//    public boolean equals (Object o) {
//        if (o instanceof TokenTransition) {
//            TokenTransition t = (TokenTransition)o;
//            if ((dest.id == t.dest.id) && (label.equalsIgnoreCase(t.label))) {
//                return true;
//            } else {
//                return false;
//            }
//        } else {
//            return super.equals(o);
//        }
//    }
//    
//    public int hashCode() {
//        if ((dest==null) || (label==null)) {
//            return 0;
//        } else {
//            return (dest.id + label.hashCode());
//        }
//    }
    
	public String toString() {
		StringBuffer temp=new StringBuffer();
        /*
        String typeName="black";
		
		switch (type) {
			case NONE:
				typeName="NONE";
				break;
			case KEYWORD:
                typeName="KEYWORD";
				break;
			case OP:
                typeName="OP";
				break;
			case VAR:
                typeName="VAR";
				break;
			case STRING:
                typeName="STRING";
				break;
			case TEXTFIELD:
                typeName="TEXTFIELD";
				break;
			case QUOTE:
                typeName="QUOTE";
				break;
            case WHITESPACE:
                typeName="WHITESPACE";
                break;
		}
        */
		temp.append("\t"+id + " : " + source.getID() + " -> " + dest.getID() + " : \"" + label + "\"\n");
        //temp.append("\t" + "Type: " + typeName + "\n");
        //temp.append("\t" + "Index: " + stringIndex + "\n");
        //temp.append("\t" + "Lbl Length: " + label.length() + "\n");
        //temp.append("\n" + "\n");
		return temp.toString();
	}
    
    public String toDot() {
        String temp="", color="black";
        
        switch (type) {
            case NONE:
                color="black";
                break;
            case KEYWORD:
                color="blue";
                break;
            case OP:
                color="green";
                break;
            case VAR:
                color="darkorange";
                break;
            case STRING:
                color="darksalmon";
                break;
            case TEXTFIELD:
                color="red";
                break;
            case QUOTE:
                color="yellow";
                break;
            case WHITESPACE:
                color="purple";
                break;
        }
        temp+=source.getID() + " -> " + dest.getID() + " [label=\"" + label + "(" + id + ")\" color=\"" + color + "\"]\n";
        return temp;
    }

    public static String toGraphMLInit() {
    	StringBuffer tokenInit = new StringBuffer();
    	//tokenInit.append("<key id=\"token\" for=\"node\" attr.name=\"token\" attr.type=\"string\"/>\n");
    	//tokenInit.append("<key id=\"type\" for=\"node\" attr.name=\"type\" attr.type=\"string\"/>\n");
    	return tokenInit.toString();
    }
    
    public String toGraphML() {
    	StringBuffer node = new StringBuffer();
    	node.append("<node id=\""+id+"\">\n");
    	//node.append(" <data key=\"token\">"+label+"</data>\n");
    	//node.append(" <data key=\"type\">"+type+"</data>\n");
    	node.append("</node>\n");
    	return node.toString();
    }
    
    public long getVal() {
        return val;
    }

    public void setVal(long val) {
        this.val = val;
    }
}
