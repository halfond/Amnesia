/*
 *Copyright 2005 
 *Georgia Tech Research Corporation
 *Atlanta, GA  30332-0415
 *All Rights Reserved
 */

package edu.usc.sql.amnesia.exceptions;


/**
 * @author William Halfond, whalfond@cc.gatech.edu
 *
 */
public class SQLIAException extends Exception {

    private static final long serialVersionUID=1L;
	private String query;
	
    
	/**
	 * @param q: Contains the query string that caused the SQLIA
	 */
	public SQLIAException (String q) {
		query=q;
	}
	
	public String getQuery() {
		return query;
	}
}
