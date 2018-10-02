/*
 * Created on Feb 2, 2006
 *
 * Copyright 2004
 * Georgia Tech Research Corporation
 * Atlanta, GA  30332-0415
 * All Rights Reserved
 * 
 */
package amnesia.monitors;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import sql.models.DBAccess;
import sql.models.HotspotModel;
import sql.models.QueryAnalysis;
import amnesia.exceptions.SQLIAException;
import amnesia.util.AmnesiaConstants;
import amnesia.util.AutomataName;

public class VisualizationMonitor extends Monitor {

	private static File queryDir;
	private static File modelDir;

	static {  
		queryDir=new File(properties.getProperty(AmnesiaConstants.PROP_DIR_VIZ_QUERY));
		if (!queryDir.exists()) {
			queryDir.mkdirs();
		}

		modelDir=new File(properties.getProperty(AmnesiaConstants.PROP_DIR_VIZ_MODELS));
		if (!modelDir.exists()) {
			modelDir.mkdirs();
		}
	}

	public static void report(String queryString, String id) throws Exception {      

		HotspotModel aut = getAut(id);

		QueryAnalysis qa = aut.analyze(queryString, dbLexer);
		int type = DBAccess.NONE;
		if (qa.getAccept()) {
			type = DBAccess.NORMAL;
		} else {
			type = DBAccess.ATTACK;
		}
		DBAccess access = new DBAccess(id, qa.getVisited(), qa.getLongestMatchQuery(), qa.getLongestMatchModel(), type, queryString);

		AutomataName autName = new AutomataName(id);
		File modelFile = new File(modelDir.getAbsolutePath()+File.separator+autName.getAutomataRegularPath());
		if (!modelFile.exists()) {
			ObjectOutputStream out; 
			out = new ObjectOutputStream(new FileOutputStream(modelFile));
			out.writeObject(aut);
			out.close();
		}
		
		ObjectOutputStream out; 
		out = new ObjectOutputStream(new FileOutputStream(queryDir.getPath()+File.separator+access.getUniqueName()+".access"));
		out.writeObject(access);
		out.close();

		if (!qa.getAccept()) {
			sqliaLog.warning("SQLIA: " + id);
			sqliaLog.warning("Query: " + queryString);
			sqliaLog.warning("Match: " + qa.getLongestMatchQuery());
			sqliaLog.warning("\n--------------------------------\n");
			throw(new SQLIAException(queryString));
		}

	}

}
