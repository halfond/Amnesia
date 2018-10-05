
package dk.brics.string;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import soot.ValueBox;
import dk.brics.automaton.Automaton;

/** An executable frontend for analyzing programs using the string analysis
 *  runtime library.
 *  <p>
 *  The given classes are analyzed using the string analysis, and the results
 *  are compared to the expected results as given in all
 *  {@link dk.brics.string.runtime.RegExp#analyze RegExp.analyze} and
 *  {@link dk.brics.string.runtime.RegExp#check RegExp.check} calls.
 *  <p>
 *  For all {@link dk.brics.string.runtime.RegExp#analyze RegExp.analyze} calls,
 *  there are three cases:
 *  <ul>
 *   <li>The inferred language is identical to the expected language.<br>
 *       The message &quot;<tt>Exact match!</tt>&quot; is printed.</li>
 *   <li>The inferred language is a subset of the expected language.<br>
 *       The message &quot;<tt>Always satified!</tt>&quot; is printed.</li>
 *   <li>There exist one or more strings in the inferred language that
 *       are not in the expected language.<br>
 *       One shortest example of such a string is printed.</li>
 *  </ul>
 *  <p>
 *  For all {@link dk.brics.string.runtime.RegExp#check RegExp.check} calls,
 *  there are also three cases:
 *  <ul>
 *   <li>The inferred language is a subset of the expected language, indicating
 *       that the check always succeeds.<br>
 *       The message &quot;<tt>Always satified!</tt>&quot; is printed.</li>
 *   <li>The inferred language is disjoint from the expected language, indicating
 *       that the check always fails.<br>
 *       The message &quot;<tt>Never satified!</tt>&quot; is printed.</li>
 *   <li>Otherwise, nothing is printed.</li>
 *  </ul>
 */
public class AnalyzeRuntime {

    // Not instantiable
    private AnalyzeRuntime() {}

    /** Main method for the program.
     *  @param args a list of class file names for the classes to be analyzed.
     *              The class names must be given fully qualified, with
     *              &quot;<tt>.</tt>&quot; or &quot;<tt>/</tt>&quot; to
     *              seperate the path components, and with
     *              or without the &quot;<tt>.class</tt>&quot; extension, respectively.
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException {
	String program_name = null;

	long time0 = System.currentTimeMillis();

	System.err.println("Loading classes...");
	for (int i = 0 ; i < args.length ; i++) {
	    String classname = args[i];
	    if (classname.endsWith(".class")) {
		classname = classname.substring(0,classname.length()-6).replace('/','.');
	    }
	    StringAnalysis.loadClass(classname);
	    if (program_name == null) {
		program_name = classname;
	    }
	}

	long time1 = System.currentTimeMillis();

	System.err.println("Finding runtime support methods...");
	RuntimeResolver rr = new RuntimeResolver();
	Set/*<RuntimeHotspot>*/ runtime_hotspots = rr.getHotspots();
	int places = runtime_hotspots.size();
	System.err.println("Number of hotspots: "+places);

	System.err.println("Analyzing...");
	StringAnalysis.addResolver(rr);
	StringAnalysis sa = new StringAnalysis(rr.getHotspotExps());

	long time2 = System.currentTimeMillis();

	int errors = 0;

	Iterator rhi = runtime_hotspots.iterator();
	while (rhi.hasNext()) {
	    RuntimeHotspot rh = (RuntimeHotspot) rhi.next();
	    ValueBox e = rh.spot;
	    String sf = sa.getSourceFile(e);
	    int line = sa.getLineNumber(e);
	    String kind = "";
	    switch (rh.kind) {
	    case RuntimeHotspot.KIND_ANALYZE:
		kind = "RegExp.analyze()";
		break;
	    case RuntimeHotspot.KIND_CHECK:
		kind = "RegExp.check()";
		break;
	    default:
		System.err.println("unrecognized runtime method!");
		System.exit(1);
		break;
	    }
	    System.err.println("Checking "+kind+" at line "+line+" in "+sf+"...");
	    Automaton a = sa.getAutomaton(e);
	    Automaton expected_neg = rh.expected.complement();
	    switch (rh.kind) {
	    case RuntimeHotspot.KIND_ANALYZE:
		Automaton diff = a.intersection(expected_neg);
		if (diff.isEmpty()) {
		    if (a.equals(rh.expected)) {
			System.err.println("Exact match!");
		    } else {
			System.err.println("Always satified!");
		    }
		} else {
		    System.err.println("Dissatisfied by:");
		    System.err.println(quoteString(diff.getShortestExample(true)));
		}
		break;
	    case RuntimeHotspot.KIND_CHECK:
		if (a.intersection(expected_neg).isEmpty()) {
		    System.err.println("Always satified!");
		} else if (a.intersection(rh.expected).isEmpty()) {
		    System.err.println("Never satified!");
 		}
		break;
	    }
	}

	long time3 = System.currentTimeMillis();

	System.err.println("Loading time: "+time(time1-time0));
	System.err.println("Analysis time: "+time(time2-time1));
	System.err.println("Extraction time: "+time(time3-time2));
    }

    private static String quoteString(String s) {
	StringBuffer sb = new StringBuffer();
	sb.append("\"");
	for (int i = 0 ; i < s.length() ; i++) {
	    char c = s.charAt(i);
	    switch (c) {
	    case '\t': sb.append("\\t"); break;
	    case '\n': sb.append("\\n"); break;
	    case '\r': sb.append("\\r"); break;
	    case '\"': sb.append("\\\""); break;
	    case '\\': sb.append("\\\\"); break;
	    default:
		if (c >= 32 && c <= 126) {
		    sb.append(c);
		} else {
		    String digits = Integer.toHexString(0x10000+c);
		    sb.append("\\u").append(digits.substring(1));
		}
	    }
	}
	sb.append("\"");
	return sb.toString();
    }

    private static String time(long t) {
	return t/1000 + "." + String.valueOf(1000+(t%1000)).substring(1);
    }
}
