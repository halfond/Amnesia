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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sql.models.QueryToken;
import sql.models.TokenTransition;

public class GenericLexer implements Lexer {
    QueryToken initialState;
    private static Set ops, keywords;
    protected String queryText="";
    
    private List tokens = new ArrayList();
    
    static {
        String text;
        ops = new HashSet();
        keywords = new HashSet();
        try {
            BufferedReader keywordsFile = new BufferedReader(new FileReader("/net/hc283/whalfond/Research/Code/amnesia/SQL.keywords"));
            BufferedReader opsFile = new BufferedReader(new FileReader("/net/hc283/whalfond/Research/Code/amnesia/SQL.ops"));

            while ((text = keywordsFile.readLine()) != null) {
                keywords.add(text);
            }
            while ((text = opsFile.readLine()) != null) {
                ops.add(text);
            }
            keywordsFile.close();
            opsFile.close();
        } catch (FileNotFoundException fnfe) {
        	String[] k = {"SELECT", "FROM", "WHERE", "OR", "AND", "DROP", "UPDATE", "LIKE", "UNION", "INNER_JOIN", "TABLE"};
            keywords.addAll(Arrays.asList(k));
            String[] o = {"=", "<",	">", "<=", ">=", "!=", "!", ")", "(", ",", "\"", "*", "+", "-",	"%", "'", ";", ".", " "};
            ops.addAll(Arrays.asList(o));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    public String getLexedQueryString() {
    	return queryText;
    }
    
    public List lexQuery(String queryString) throws SQLLexerException {
        String currLabel="";
        queryText = queryString;
        int currTokenType=TokenTransition.NONE;
        boolean foundSlash=false;
        boolean inQuote=false;
        tokens.clear();
        
        for (int i=0; i<queryString.length(); i++) {
            char c = queryString.charAt(i);
            
                        
            if ((c == '\\') && (inQuote) && !foundSlash) {
                foundSlash=true;
                continue;
            } 
            if ((c=='\'') && !foundSlash) {
                if (inQuote) {
                	if (!currLabel.equals("")) {
	                    LexicalToken ltText= new LexicalToken(currLabel, TokenTransition.SCONSTANT, i-1);
	                    tokens.add(ltText);
	                    currLabel="";
                	}
                    LexicalToken ltQuote = new LexicalToken("'", TokenTransition.QUOTE, i);
                    tokens.add(ltQuote);
                    inQuote=false;
                    currTokenType=TokenTransition.QUOTE;
                } else {
                    LexicalToken ltQuote = new LexicalToken("'", TokenTransition.QUOTE, i);
                    tokens.add(ltQuote);
                    inQuote=true;
                    currTokenType=TokenTransition.TEXTFIELD;
                }
            } else {
                if (inQuote && c != '%') {
                    currLabel+=String.valueOf(c);
                    currTokenType=TokenTransition.TEXTFIELD;
                    if (c=='\'') {foundSlash=false;}
                } else if (ops.contains(String.valueOf(c))) {
                    if (currTokenType==TokenTransition.TEXTFIELD) {
                    	if (!currLabel.equals("")) {
	                    	LexicalToken ltText= new LexicalToken(currLabel, TokenTransition.SCONSTANT, i-1);
	                        tokens.add(ltText);
	                        currLabel="";
                    	}
                    }
                    LexicalToken lt = new LexicalToken(String.valueOf(c), TokenTransition.OP, i);
                    tokens.add(lt);
                    currTokenType=TokenTransition.OP;
                } else {
                    currLabel+=String.valueOf(c);
                    currTokenType=TokenTransition.TEXTFIELD;
                }
            }
        }
        if (currLabel.length()>0 && currTokenType==TokenTransition.TEXTFIELD) {
        	LexicalToken lt = new LexicalToken(currLabel, TokenTransition.SCONSTANT, queryString.length()-1);
        	tokens.add(lt);
        }
        return tokens;
    }

}
