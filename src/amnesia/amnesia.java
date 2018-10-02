/*
 *Copyright 2004 
 *Georgia Tech Research Corporation
 *Atlanta, GA  30332-0415
 *All Rights Reserved
 */

package amnesia;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.Type;
import org.apache.commons.math.stat.descriptive.StatisticalSummary;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;

import soot.Scene;
import soot.ValueBox;
import sql.models.HotspotModel;
import amnesia.config.AnalysisProperties;
import amnesia.config.WAMConfiguration;
import amnesia.exceptions.AnalysisPropertiesException;
import amnesia.exceptions.ApplicationPropertiesException;
import amnesia.util.AutomataName;
import dk.brics.automaton.Automaton;
import dk.brics.string.StringAnalysis;

public class amnesia {

	//The function to be called, must be static
	public static final String reportFunction="report";

	//The hotspot signatures to look for in Soot and bcel format
	public static String[] sootSigs = {
		"<java.sql.Statement: boolean execute(java.lang.String)>",
		"<java.sql.Statement: java.sql.ResultSet executeQuery(java.lang.String)>",
	"<java.sql.Statement: int executeUpdate(java.lang.String)>" };

	public static String[] bcelSigs = {
		"java.sql.Statement.execute(Ljava/lang/String;)Z",
		"java.sql.Statement.executeQuery(Ljava/lang/String;)Ljava/sql/ResultSet;",
	"java.sql.Statement.executeUpdate(Ljava/lang/String;)I" };

	public static final String CONFIG_FILE_NAME="config.xml";

	private static int instrHotspotCount=0, optHotspotCount=0, totalHotspotCount=0;
	private static Map<String, Boolean> userDefined=new HashMap<String, Boolean>();

	public static void main(String[] args)  {

		if (args.length < 3) {
			System.err.println("Correct usage: amnesia <propertyfile> <configFile> <classPath>");
			System.exit(-1);
		} 

		File propertyFile = new File(args[0]);
		File configFile = new File(args[1]);
		File classPath= new File(args[2]);
		
		try {
			AnalysisProperties properties = new AnalysisProperties(propertyFile.getCanonicalPath());
			WAMConfiguration application = WAMConfiguration.load(configFile.getAbsolutePath());

			String appName = application.getAppName();
			String runtimeMonitor=properties.getProperty("runtime.monitor");
			File autDir=new File(properties.getProperty("dir.aut"));
			File imgsDir=new File(properties.getProperty("dir.imgs"));
//			String autDirName=properties.getProperty("dir.aut")+File.separator+appName;
//			String imgsDirName=properties.getProperty("dir.imgs")+File.separator+appName;
			
			if (!autDir.isAbsolute()) {
				autDir=new File(classPath+ File.separator +  autDir.getPath());
			}
			if (!autDir.exists()) {
				autDir.mkdirs();
			}
			
			if (!imgsDir.isAbsolute()) {
				imgsDir=new File(classPath+ File.separator +  imgsDir.getPath());
			}
			if (!imgsDir.exists()) {
				imgsDir.mkdirs();
			}
			
			System.out.println("AMNESIA analysis of " + appName );
			System.out.println("\tAnalysis property file: " + propertyFile.getAbsolutePath());
			System.out.println("\tApplication property file: " + configFile.getAbsolutePath());
			System.out.println("\tClass path: " + classPath);
			System.out.println("\tInstrumented classes: " + classPath);
			System.out.println("\tAutomata output: " + autDir.getAbsolutePath());
			System.out.println("\tImage output: " + imgsDir.getAbsolutePath());
			
			String cp=Scene.v().getSootClassPath()+File.pathSeparator+classPath;
			Scene.v().setSootClassPath(cp);
			
			Collection<String> classesToAnalyze=application.getClasses();
			System.out.print("\tNumber of classes loaded: ");
			for (String classname:classesToAnalyze) {
				StringAnalysis.loadClass(classname);
			}
			System.out.println(classesToAnalyze.size());

			System.out.print("\tHotspot signatures: ");
			Set<ValueBox> hotspots = new HashSet<ValueBox>();
			for (String sig:sootSigs) {
				hotspots.addAll(StringAnalysis.getExps(sig, 0));
			}
			StringAnalysis sa = new StringAnalysis(hotspots);     
			System.out.println(sootSigs.length);

			//Iterate through each hotspot
			System.out.print("\tAnalyzing " + hotspots.size() + " hotspots...");
			SummaryStatistics stats = SummaryStatistics.newInstance();        
			for (ValueBox e:hotspots) {
				AutomataName autName = new AutomataName(appName, sa.getClassName(e), sa.getLineNumber(e));

				try {
					Automaton aut=sa.getAutomaton(e);
					aut.reduce();
					//writeGraph(imgsDir.getPath() + File.separator + filename + ".char", aut.toDot()); //char-level representation
					HotspotModel sqlAut=new HotspotModel(aut);
					writeGraph(imgsDir.getPath() + File.separator + autName.getAutomataDottyPath(), sqlAut.toDot());
					//sqlAut.minimize();
					//writeGraph(imgsDir.getPath() + File.separator + filename + ".mintoken", sqlAut.toDot()); //minimized token representation
					ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(autDir.getPath()+File.separator+autName.getAutomataRegularPath()));
					out.writeObject(sqlAut);
					out.close();              
					userDefined.put(autName.getAutomataID(), sqlAut.hasUserDefinedElements());
					stats.addValue(sqlAut.getNumberOfStates());
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
			System.out.println("done!");

			StatisticalSummary summary = stats.getSummary();
			System.out.println("Automaton size: ");
			System.out.println("\tAverage: " + Math.rint(summary.getMean()));
			System.out.println("\tStd dev: " + Math.rint(summary.getStandardDeviation()));
			System.out.println("\tMax: " + summary.getMax());
			System.out.println("\tMin: " + summary.getMin());


			System.out.print("Instrumenting classes...");
			for (String c:classesToAnalyze) {
				String filePath = classPath+File.separator+c.replace('.', File.separatorChar)+".class";
				try {
					JavaClass origClass = (new ClassParser(filePath)).parse();
					JavaClass newClass = simpleTransform(origClass, appName, runtimeMonitor);
					newClass.dump(filePath);
				} catch (ClassFormatException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} 
			}
			System.out.println("done!");

			System.out.println("\tOptimized points: " + optHotspotCount);
			System.out.println("\tInstrumented points: " + instrHotspotCount);
			System.out.println("\tTotal hotspots: " + totalHotspotCount);
			if (totalHotspotCount != hotspots.size()) {
				System.err.println("BCEL and Soot found a different amount of hotspots! That's really weird...");
			}

			System.out.println("AMNESIA analysis completed.");
			
		} catch (ApplicationPropertiesException ape) {
			System.out.println("Could not load application property file: " + configFile.getAbsolutePath());
			ape.printStackTrace();
		} catch (AnalysisPropertiesException ape) {
			System.out.println("Could not load analysis property file: " + ape.getFileLocation());
			ape.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}


	public static JavaClass simpleTransform(JavaClass jc, String appName, String runtimeMonitor) {
		try {
			ClassGen cg = new ClassGen(jc);
			ConstantPoolGen cpg = cg.getConstantPool();
			InstructionFactory instrFactory = new InstructionFactory(cg);

			for (Method m:cg.getMethods()) {
				MethodGen mg = new MethodGen(m, jc.getClassName(), cpg);
				LineNumberTable lineNumberTable = mg.getLineNumberTable(cpg);
				InstructionList il = mg.getInstructionList();

				if (il != null) {
					for (InstructionHandle currHandle:il.getInstructionHandles()) {
						Instruction currInstr = currHandle.getInstruction();
						if (currInstr instanceof InvokeInstruction) {
							InvokeInstruction instr = (InvokeInstruction) currInstr;
							String signature=instr.getClassName(cpg) + "." + instr.getMethodName(cpg) + instr.getSignature(cpg); 
							ArrayList<String> dbSigList = new ArrayList<String>(Arrays.asList(bcelSigs));
							if (dbSigList.contains(signature)) {
								totalHotspotCount++;
								AutomataName autName = new AutomataName(appName, mg.getClassName(), lineNumberTable.getSourceLine(currHandle.getPosition()));
								if (userDefined.get(autName.getAutomataID())) {
									InstructionList checkerInstructions = new InstructionList();
									checkerInstructions.append(InstructionConstants.DUP);
									checkerInstructions.append(new PUSH(cpg, autName.getAutomataID()));
									checkerInstructions.append(instrFactory.createInvoke(runtimeMonitor, reportFunction, Type.VOID, new Type[] { Type.STRING, Type.STRING }, Constants.INVOKESTATIC));
									il.insert(currHandle, checkerInstructions);
									instrHotspotCount++;
								} else {
									optHotspotCount++;
								}
							}
						}    
					}
					mg.setMaxStack();
					mg.setMaxLocals();

					cg.replaceMethod(m, mg.getMethod());
				}
			}
			return cg.getJavaClass();
		} catch (ClassFormatException e) {
			e.printStackTrace();
		}
		return null;
	}


	public static void writeGraph(String filename, String graph) {
		try {
			FileWriter outputFile=new FileWriter(filename);
			outputFile.write(graph);
			outputFile.close();			
		} catch (IOException ioe) {
			System.err.println("Could not create output file: " + filename);
			ioe.printStackTrace();
		}

	}

}

