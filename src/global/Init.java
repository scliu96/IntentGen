package global;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import path.analysis.method.MethodAnalysis;
import path.analysis.method.SearchTransformer;
import path.analysis.unit.PathAnalysis;
import soot.PackManager;
import soot.Scene;
import soot.SootMethod;
import soot.Transform;
import soot.Value;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;
import type.Intent;
import type.MethodPoint;
import type.UnitPath;

public class Init{
	private final static String androidJarPath="/Users/apple/Documents/Eclipse/android-platforms-master";
	private final static String APKPath="/Users/apple/Documents/AndroidStudio/release/app-release.apk";
	
	public final static boolean parallelEnabled = false;
	public final static boolean pathLimitEnables = true;
	public static int finalPathsLimit;
	
	public static Logger logger = LogManager.getLogger(Init.class);
	
	private static void sootInit(){
		soot.G.reset();
		//G.v().out.close();
		//;new PrintStream(new File("/Users/apple/Documents/Eclipse/ServiceLeak/log/SootPrint"));
		Options.v().set_src_prec(Options.src_prec_apk);
		Options.v().set_android_jars(androidJarPath);
		Options.v().set_process_dir(Collections.singletonList(APKPath));
		Options.v().set_output_format(Options.output_format_none);
		
		//test11
		Options.v().set_whole_program(true);
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_no_bodies_for_excluded(true);
		Scene.v().loadNecessaryClasses();
	}
	
	private static void pathInit() {
		if(pathLimitEnables)
			finalPathsLimit = 100;
		else finalPathsLimit = Integer.MAX_VALUE;
	}
	
	public static void main(String[] args) throws Exception {
		sootInit();
		pathInit();
		
		SearchTransformer mySearch = new SearchTransformer();
        PackManager.v().getPack("jtp").add(new Transform("jtp.myInstrumenter",mySearch));
        PackManager.v().runPacks();
        
        Database.apkCG = Scene.v().getCallGraph();
        //debug about unit call graph in many ways
        MethodAnalysis.analysis();
        //for(MethodPoint mp : methodPoints)
        //		System.out.println(mp.toMethodString());
        PathAnalysis.analysis();
        return;
	}
}



