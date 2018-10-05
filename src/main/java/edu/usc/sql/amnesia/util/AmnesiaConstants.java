package edu.usc.sql.amnesia.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AmnesiaConstants {
	public static final String DEFAULT_PROPERTY_FILE = "edu.usc.sql.amnesia.properties";
	public static final String PROP_DIR_AUT = "dir.aut";
	public static final String PROP_DIR_LOGS = "dir.logs";
	public static final String PROP_DIR_VIZ_MODELS = "dir.visualization.models";
	public static final String PROP_DIR_VIZ_QUERY = "dir.visualization.queries";
	
	private static final String[] DEFAULT_KEYWORDS_ARRAY = {"SELECT", "FROM", "WHERE", "OR", "AND", "DROP", "UPDATE", "LIKE", "UNION", "INNER_JOIN"};
	private static final String[] DEFAULT_OPERATORS_ARRAY = {"=", "<",	">", "<=", ">=", "!=", "!", ")", "(", ",", "\"", "*", "+", "-",	"%", "'", ";", ".", " "};
	
	public static final Set<String> DEFAULT_KEYWORDS = new HashSet<String>(Arrays.asList(DEFAULT_KEYWORDS_ARRAY));
	public static final Set<String> DEFAULT_OPERATORS = new HashSet<String>(Arrays.asList(DEFAULT_OPERATORS_ARRAY));
	
}
