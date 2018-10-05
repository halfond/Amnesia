/*
 * Created on Aug 1, 2005
 *
 * Copyright 2004
 * Georgia Tech Research Corporation
 * Atlanta, GA  30332-0415
 * All Rights Reserved
 * 
 */
package edu.usc.sql.amnesia.lexer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.usc.sql.models.QueryToken;
import edu.usc.sql.models.TokenTransition;



public class PostgreSQLLexer implements Lexer {
    private static int  POSTGRESQL_SYNTAX_ERROR=-2;
    
    protected String queryString;
    protected String lexedQueryString="";
    private List tokens=new ArrayList();
    
    private String currLabel=""; //set by lex
    private int currType=-1; //set by lex
    private int strIndex=0;

    
    static {
        System.loadLibrary("PostgreSQLLexer");
    }

    
//    public PostgreSQLLexer(String name, String queryString) {
//        this.name=name;
//        this.queryString=queryString;
//        states = new ArrayList<LexicalToken>();
//        currLabel="";
//        currType=-1;
//    }
    
    public List lexQuery(String queryString) throws SQLLexerException {
        this.queryString=queryString;
        lexedQueryString="";
        tokens.clear();
        currLabel="";
        currType=-1;
        strIndex=0;
        
        init_lex(queryString);
        
        int returnVal=lex();
        
        while (returnVal > 0) {
            if (currType == TokenTransition.SCONSTANT) {
                
                String text=currLabel.substring(1,currLabel.length()-1);                
                int tempIndex=strIndex;
                
                LexicalToken openQuote = new LexicalToken("'", TokenTransition.QUOTE, tempIndex);
                tokens.add(openQuote);
                tempIndex++;
                
                if (text.indexOf("\u0001") == -1 ) {
                    LexicalToken textString = new LexicalToken(text, TokenTransition.TEXTFIELD, tempIndex);
                    tokens.add(textString);
                } else {
                    //TODO:THis has incorrect indexes which will cause metastrings to have problems
                    String[] textParts = text.split("\u0001");
                    for (int i=0; i<textParts.length; i++) {
                        if (textParts[i].length() > 0) {
                            LexicalToken textString = new LexicalToken(textParts[i], TokenTransition.TEXTFIELD, tempIndex);
                            tokens.add(textString);
                        }
                        LexicalToken percentOp = new LexicalToken("%", TokenTransition.OP, tempIndex);
                        tokens.add(percentOp);
                    }
                }
                tempIndex+=returnVal-2;
                
                
                LexicalToken closeQuote = new LexicalToken("'", TokenTransition.QUOTE, tempIndex);
                tokens.add(closeQuote);
            } else if (currType == TokenTransition.WHITESPACE) { 
                int tempIndex=strIndex;
                for (int i=0; i<currLabel.length(); i++) {
                    LexicalToken whitespace= new LexicalToken(currLabel.substring(i, i+1), TokenTransition.WHITESPACE, tempIndex);
                    tokens.add(whitespace);
                    tempIndex++;
                }
            } else {
                LexicalToken nextToken=new LexicalToken(currLabel, currType, strIndex);
                tokens.add(nextToken);
            }
            strIndex+=returnVal;
            returnVal=lex();
        }
        
        finish_lex();
        if (returnVal == POSTGRESQL_SYNTAX_ERROR) {
            throw new SQLLexerException();
        } 
        return tokens;
    }
        
//    public QueryToken parseQuery() {
//        init_lex(queryString);
//        int returnVal=lex();
//        initialState=new QueryToken();
//        initialState.setAccept(returnVal == 0);
//        states.add(initialState);
//        
//        TokenTransition nextTransition;
//        QueryToken nextState=null, prevState=initialState;
//        while (returnVal > 0) {
//            nextState= new QueryToken();
//            states.add(nextState);
//            if (currType == TokenTransition.SCONSTANT) {
//                QueryToken inter1 = new QueryToken(), inter2=new QueryToken();
//                states.add(inter1);
//                states.add(inter2);
//                String text=currLabel.substring(1,currLabel.length()-1);                
//                int tempIndex=strIndex;
//                
//                TokenTransition openQuote = new TokenTransition(prevState, inter1, "'", TokenTransition.QUOTE, tempIndex);
//                prevState.addTransition(openQuote);
//                tempIndex++;
//                TokenTransition textString = new TokenTransition(inter1, inter2, text, TokenTransition.TEXTFIELD, tempIndex);
//                inter1.addTransition(textString);
//                tempIndex+=returnVal-2;
//                TokenTransition closeQuote = new TokenTransition(inter2, nextState, "'", TokenTransition.QUOTE, tempIndex);
//                inter2.addTransition(closeQuote);
//            } else {
//                nextTransition=new TokenTransition(prevState, nextState, currLabel, currType, strIndex);
//                prevState.addTransition(nextTransition);
//            }
//            prevState=nextState;
//            strIndex+=returnVal;
//            returnVal=lex();
//        }
//        if (nextState != null) {
//            nextState.setAccept(true);
//        }
//        finish_lex();
//        if (returnVal == -2) {
//            return null;
//        } else {
//            return initialState;
//        }
//    }
    
//  This function copied from the JSA implementation
    public String toDot() {
        StringBuffer b = new StringBuffer("digraph Automaton {\n");
        b.append("  rankdir = LR;\n");
        Iterator i = tokens.iterator();
        while (i.hasNext()) {
            QueryToken s = (QueryToken) i.next();
            b.append(s);
            Iterator j = s.getTransitions().iterator();
            while (j.hasNext()) {
                TokenTransition t = (TokenTransition) j.next();
                b.append(t);
            }
        }
        return b.append("}\n").toString();
    }
    
    private void setState(String label, int type) {
        currLabel=label;
        currType=type;
        lexedQueryString+=currLabel;
    }
    
    public String getLexedQueryString() {
        return lexedQueryString;
    }
    
    private native void init_lex(String queryString);
    private native int lex();
    private native void finish_lex();
}
