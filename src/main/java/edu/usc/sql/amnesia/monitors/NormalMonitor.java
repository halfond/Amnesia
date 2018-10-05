/*
 * Copyright 2004
 * Georgia Tech Research Corporation
 * Atlanta, GA  30332-0415
 * All Rights Reserved
 */
package edu.usc.sql.amnesia.monitors;

import edu.usc.sql.models.HotspotModel;
import edu.usc.sql.amnesia.exceptions.SQLIAException;
import edu.usc.sql.amnesia.lexer.SQLLexerException;

public class NormalMonitor extends Monitor {

	public static void report(String queryString, String id) throws SQLIAException, SQLLexerException {         
		HotspotModel aut = getAut(id);

		boolean accepts = aut.accepts(queryString, dbLexer);                    
		if (!accepts) {
			sqliaLog.warning("SQLIA: " + id + " <= " + queryString);
			throw(new SQLIAException(queryString));
		}
	} 

}
