/*
 * Copyright 2005
 * Georgia Tech Research Corporation
 * Atlanta, GA  30332-0415
 * All Rights Reserved
 * 
 */
package edu.usc.sql.amnesia.monitors;

import edu.usc.sql.models.HotspotModel;
import edu.usc.sql.amnesia.exceptions.SQLIAException;

public class OptimizedMonitor extends Monitor {

    public static void report(String queryString, String id) throws Exception {
        HotspotModel aut = getAut(id);

        if (!aut.accepts(queryString, dbLexer)) {
            throw(new SQLIAException(queryString));
        }
    }
}
