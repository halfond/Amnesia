/*
 * Copyright 2005
 * Georgia Tech Research Corporation
 * Atlanta, GA  30332-0415
 * All Rights Reserved
 * 
 */
package amnesia.monitors;

import java.util.Calendar;

import sql.models.HotspotModel;
import amnesia.exceptions.AmnesiaException;
import amnesia.exceptions.SQLIAException;
import amnesia.exceptions.UndetectedSQLIAException;

public class GatherStatsMonitor extends Monitor {

    private static int truePositive=0, falsePositive=0, trueNegative=0, falseNegative=0;
    private static int queryCount=0;

    
    
    public static void report(String queryString, String id) throws Exception {
        
        boolean status=false;
        boolean isIdentifiedAttack=false;
        queryCount++;
        outputStats();
        
        if (queryString.indexOf("~~~") != -1) { isIdentifiedAttack=true;}
        String[] queryArray = queryString.split("~~~");
        if (queryArray.length > 1) {
            //isIdentifiedAttack=true;
            queryString="";
            for (int i=0; i<queryArray.length; i++) {
                queryString+=queryArray[i];
            }
        }
        HotspotModel aut = getAut(id);
        
        Calendar now= Calendar.getInstance();
        if(aut==null) {
            status= false;
            System.err.println(now.getTime() + "Could not locate automata: " + id);
            throw(new AmnesiaException());
        }
        else {
            status=aut.accepts(queryString, dbLexer);
            if (status) {
                if (isIdentifiedAttack) {
                    falseNegative++;
                    sqliaLog.warning(now.getTime() + ":" + id + " false negative: " + queryString);
                    
                    throw(new UndetectedSQLIAException());
                } else {
                    trueNegative++;
                }
            } else {
                if (isIdentifiedAttack) {
                    truePositive++;                
                } else {
                    //false positive
                    falsePositive++;
                    sqliaLog.warning(now.getTime() + ":" + id + " false positive: " + queryString);
                  
                }
                throw(new SQLIAException(queryString));
            }
        }
    }
    
    public static void outputStats() {
        Calendar now= Calendar.getInstance();
        sqliaLog.warning(now.getTime().toString());
        sqliaLog.warning("Total Queries: " + queryCount);
        sqliaLog.warning("False Postives: " + falsePositive);
        sqliaLog.warning("False Negatives: " + falseNegative);
        sqliaLog.warning("True Positives: " + truePositive);
        sqliaLog.warning("True Negatives: " + trueNegative);
    }

    
    
}
