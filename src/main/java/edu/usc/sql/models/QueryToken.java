/*
 *Copyright 2005 
 *Georgia Tech Research Corporation
 *Atlanta, GA  30332-0415
 *All Rights Reserved
 */

package edu.usc.sql.models;


import java.util.Iterator;

import dk.brics.automaton.State;

public class QueryToken extends Token {
    private static final long serialVersionUID = 1L;

    public QueryToken() {
        super();
    }
    
    public QueryToken(State s) {
        super(s);
    }
    
    public TokenTransition next() throws NonLinearQueryException{
		if (outTransitions.size() > 1) {
			throw new NonLinearQueryException();
		} else {
			Iterator it = outTransitions.iterator();
            if (it.hasNext()) {
                return (TokenTransition)it.next();
            } else {
                return null;
            }
		}		
	}
}
