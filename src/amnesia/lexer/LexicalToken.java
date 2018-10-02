/*
 * Created on Sep 20, 2005
 *
 * Copyright 2004
 * Georgia Tech Research Corporation
 * Atlanta, GA  30332-0415
 * All Rights Reserved
 * 
 */
package amnesia.lexer;

public class LexicalToken {

    private String label;
    private int type;
    private int strIndex;
    
    public LexicalToken(String l, int t, int sI) {
        label=l;
        type=t;
        strIndex=sI;
    }
    
    public String getLabel() {
        return label;
    }
    
    public int getType() {
        return type;
    }
    
    public int getStringIndex() {
        return strIndex;
    }
    
    public String toString() {
    	return label;
    }
    
    public boolean equals(Object obj) {
    	if (obj instanceof LexicalToken) {
	    	LexicalToken lt = (LexicalToken)obj;
    		boolean result = ((label.equals(lt.label)) && (type==type));
    		return result;
    	} else {
    		return false;
    	}
    }
    
    public int hashCode() {
    	return type;
    }
    
}
