package amnesia.models;

import java.io.Serializable;


public class Edge implements Serializable {
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
    
    private Node source, dest;
    private String label;
    private int type;
    private int id;
    private long edgeValue=0;
    
    //constructors
    
    public Edge() {
    	
    }
    
     
    public Edge(Node s, Node d, String l, int t) {
        source=s;
        dest=d;
        label=l;
        type=t;
        id=globalID;
        globalID++; 
    }
    
    
    //utility functions
    
    public boolean matches(SQLToken st) {
    	if ((type == VAR) && st.isLiteral()) {
    		return true;
    	} else if (label.equalsIgnoreCase(st.getLabel())) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    //display related functions
    
    public String toString() {
    	return null;
    }
    
    public String toDot() {
    	return null;
    }
    
    public String toXML() {
    	return null;
    }
    
    
    //Getters and Setters
    
	public Node getDest() {
		return dest;
	}

	public int getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	public Node getSource() {
		return source;
	}

	public int getType() {
		return type;
	}
    
    public long getEdgeValue() {
    	return edgeValue;
    }
    
    public void setEdgeValue(int ev) {
    	edgeValue = ev;
    }

}
