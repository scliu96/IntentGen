package IF;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import SSE.PathAnalysisOnMethod;
import SSE.PathAnalysisOnUnit;
import Type.Intent;
import Type.MethodPoint;
import Type.UnitPath;
import soot.PackManager;
import soot.Scene;
import soot.SootMethod;
import soot.Transform;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;

public class Init{
	private final static String androidJarPath="/Users/apple/Documents/Eclipse/android-platforms-master";
	private final static String APKPath="/Users/apple/Documents/AndroidStudio/release/app-release.apk";
	
	public final static boolean parallelEnabled = false;
	public final static boolean pathLimitEnables = true;
	public static int finalPathsLimit;
	
	public static Logger logger = LogManager.getLogger(Init.class);
	public static CallGraph apkCG = null;
	public static Set<SootMethod> entryPoints = new LinkedHashSet<SootMethod>();
	public static Set<MethodPoint> methodPoints = new LinkedHashSet<MethodPoint>();
	public static Map<UnitPath,Intent> finalPaths = new LinkedHashMap<UnitPath,Intent>(); 
	
	public static void sootInit(){
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
		//Options.v().set_no_bodies_for_excluded(true);
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
        
        apkCG = Scene.v().getCallGraph();
        //debug about unit call graph in many ways
        PathAnalysisOnMethod.analysis();
        //for(MethodPoint mp : methodPoints)
        //		System.out.println(mp.toMethodString());
        PathAnalysisOnUnit.analysis();
        return;
	}
}



