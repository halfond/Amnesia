package sql.models;

import java.io.Serializable;

import amnesia.lexer.LexicalToken;

public class QueryTokenTransition extends TokenTransition implements Serializable {

	private static final long serialVersionUID= 1;
	
	public QueryTokenTransition(Token from, Token to, LexicalToken info) {
        super(from, to, info);
    }
	
	public String toDot() {
		      
        String color="black";
        
        switch (type) {

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
        
        String temp="";
        temp+="  " + source.getID();
        temp += " [shape=box, label=\"" + label + "\" color=\"" + color + "\"];\n";
        if (!dest.acceptState) {
        	temp += source.getID() + " -> " + dest.getID() + "\n";
        }
        return temp;
	}
}
