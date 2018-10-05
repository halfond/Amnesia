package edu.usc.sql.amnesia.util;


public class AutomataName {

	String appName;
	String className;
	int lineNumber;
	final String separator = ":";
	
	public AutomataName(String appName, String className, int lineNumber) {
		this.appName = appName;
		this.className = className;
		this.lineNumber = lineNumber;
	}
	
	public AutomataName(String automataID) {
		String[] fields = automataID.split(separator);
		this.appName=fields[0];
		this.className=fields[1];
		this.lineNumber=Integer.parseInt(fields[2]);
	}
	
	public String getAutomataID() {
		String name =appName+separator+className+separator+lineNumber;
		return name;
	}
	
	private String getFilename() {
		String filename = appName + "-" + className + lineNumber;
		return filename;
	}
	
	public String getAutomataRegularPath() {
		String path = getFilename() + ".aut";
		return path;
	}
	
	public String getAutomataDottyPath() {
		String path = getFilename() + ".dotty";
		return path;
	}
}
