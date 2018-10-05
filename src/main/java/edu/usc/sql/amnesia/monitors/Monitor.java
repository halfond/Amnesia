/*
 * Copyright 2005
 * Georgia Tech Research Corporation
 * Atlanta, GA  30332-0415
 * All Rights Reserved
 * 
 */
package edu.usc.sql.amnesia.monitors;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import edu.usc.sql.models.HotspotModel;
import edu.usc.sql.amnesia.config.AnalysisProperties;
import edu.usc.sql.amnesia.exceptions.AnalysisPropertiesException;
import edu.usc.sql.amnesia.lexer.GenericLexer;
import edu.usc.sql.amnesia.lexer.Lexer;
import edu.usc.sql.amnesia.util.AmnesiaConstants;
import edu.usc.sql.amnesia.util.AutomataName;

abstract public class Monitor {
 
	protected static AnalysisProperties properties;
    protected static Map<String, HotspotModel> autMap = new HashMap<String, HotspotModel>();
    protected static Lexer dbLexer=new GenericLexer();
    protected static String DIR_AUT;
    protected static Logger errorLog = Logger.getLogger("edu.usc.sql.amnesia.errors");
    protected static Logger sqliaLog = Logger.getLogger("edu.usc.sql.amnesia.sqlia");

    static { 
        URL propertyFileURL;
    	try {
        	properties = new AnalysisProperties();
        	propertyFileURL = properties.getClass().getClassLoader().getResource(AmnesiaConstants.DEFAULT_PROPERTY_FILE);
        	if (propertyFileURL == null) {
        		throw new AnalysisPropertiesException();
        	}
    		properties.load(propertyFileURL.getPath());
            DIR_AUT = properties.getProperty(AmnesiaConstants.PROP_DIR_AUT);
        } catch(AnalysisPropertiesException ape) {
        	String message = ape.getCause().toString() + " " + ape.getFileLocation();
        	errorLog.severe("Problem loading the edu.usc.sql.amnesia.properties file: " + message);
            throw new RuntimeException(message);
        } 
    }    
    
    public static void report(String queryString, String id) throws Exception {
        throw new RuntimeException("Don't call me!");
    }
    
    
    private static HotspotModel loadAut(String autID) {
    	AutomataName autName = new AutomataName(autID);
    	File autFile = new File(DIR_AUT+File.separator+autName.getAutomataRegularPath());    	
    	
    	if (!autFile.isAbsolute()) {
    		URL autFileURL = properties.getClass().getClassLoader().getResource(autFile.getPath());
    		if (autFileURL == null) {
    			String message = "Could not load aut: " + autFile.getPath();
    			errorLog.severe(message);
        		throw new RuntimeException(message);
        	}
    		autFile = new File(autFileURL.getPath());
    	}
    	
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(autFile));
            HotspotModel aut = (HotspotModel) in.readObject();
            in.close();
            autMap.put(autID, aut);
            return aut;
        } catch (Exception e) {
        	String message = e.getMessage() + " " + autFile.getAbsolutePath();
        	errorLog.severe(message);
        	throw new RuntimeException(message);
        } 
    }
    
    //TODO: autmap can get ridiculously large
    public static HotspotModel getAut(String id) {
        HotspotModel aut = (HotspotModel)autMap.get(id);
        if (aut == null) {   
            aut=loadAut(id);
        }
        return aut;
    }
    
}