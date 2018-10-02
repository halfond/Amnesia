package amnesia.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import amnesia.exceptions.ApplicationPropertiesException;

public class ApplicationProperties {

	private String appName;
	private String baseDirectory;
	//private String classPath;
	private Collection<String> requiredJars = new HashSet<String>();

	private Map<String, String> classes = new HashMap<String, String>();
	private Map<String, String> targets = new HashMap<String, String>();

	private String primerTarget;
	private String primerMethod;
	private Map<String, String> primerParams = new HashMap<String, String>();

	public ApplicationProperties(String filename) throws ApplicationPropertiesException {

		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = builder.parse(filename);

			Element rootnode = document.getDocumentElement();
			appName = rootnode.getAttribute("name");
			baseDirectory = rootnode.getAttribute("basedir");
			//classPath = rootnode.getAttribute("classpath");

			Element classlistroot = (Element) rootnode.getElementsByTagName("classlist").item(0);
			NodeList classlist = classlistroot.getElementsByTagName("class");
			for (int i=0; i<classlist.getLength(); i++) {
				Element cls = (Element) classlist.item(i);
				String name = cls.getAttribute("name");
				String target = cls.getAttribute("target");
				classes.put(name, target);
				targets.put(target, name);
			}

			Element jarlistroot = (Element) rootnode.getElementsByTagName("jarlist").item(0);
			if (jarlistroot != null) {
				NodeList jarlist = jarlistroot.getElementsByTagName("jar");
				for (int i=0; i<jarlist.getLength(); i++) {
					Element cls = (Element) jarlist.item(i);
					String path = cls.getAttribute("path");
					requiredJars.add(path);
				}
			}

			Element primerRoot = (Element) rootnode.getElementsByTagName("priming-script").item(0);
			if (primerRoot != null) {
				primerMethod = primerRoot.getAttribute("method");
				primerTarget = primerRoot.getAttribute("target");
				NodeList paramList = primerRoot.getElementsByTagName("param");
				for (int i=0; i<paramList.getLength(); i++) {
					Element param = (Element) paramList.item(i);
					String name = param.getAttribute("name");
					String value = param.getAttribute("value");
					primerParams.put(name, value);
				}
			}
		} catch (Exception e) {
			ApplicationPropertiesException ape = new ApplicationPropertiesException();
			ape.initCause(e);
			throw ape;
		} 

	}

	public String getApplicationName() {
		return appName;
	}

	public Collection<String> getClassNames() {
		return classes.keySet();
	}

	public Collection<String> getTargetNames() {
		return targets.keySet();
	}

	public String lookupCorrespondingTarget(String classname) {
		return classes.get(classname);
	}

	public String lookupCorrespondingClassname(String target) {
		return targets.get(target);
	}

//	public String getClassPath() {
//		return classPath;
//	}

	public Collection<String> getRequiredJars() {
		return requiredJars;
	}

	public void getPrimerScript() {
		throw new RuntimeException("Implement the primer script functionality.");
	}

	public void showApplicationValues() {
		System.out.println("Application Name: " + appName);
		System.out.println("Base Directory: " + baseDirectory);
//		System.out.println("Class Path: " + classPath);

		System.out.println("Target/Class list:");
		for (String cls:classes.keySet()) {
			System.out.println("\t" + classes.get(cls) + " => " + cls);
		}

		System.out.println("Jar list:");
		for (String jar:requiredJars) {
			System.out.println("\t" + jar);
		}

		System.out.println("Priming Script:");
		System.out.println("\tPrimer Method: " + primerMethod);
		System.out.println("\tPrimer Target: " + primerTarget);
		System.out.println("\tPrimer Parameters: ");
		for (String name:primerParams.keySet()) {
			System.out.println("\t\t" + name +  " => " + primerParams.get(name));
		}
	}



	public static void main(String[] args) {
		try {
			String filename = "/home/whalfond/temp/bkstr.xml";
			ApplicationProperties a = new ApplicationProperties(filename);
			a.showApplicationValues();
		} catch (ApplicationPropertiesException e) {
			e.printStackTrace();
		} 
	}
}
