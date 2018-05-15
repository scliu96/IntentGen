package global;
import java.util.Calendar;
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
	private final static String APKPath="/Users/apple/Documents/AndroidStudio/release/Zhihu_v5.16.2_apkpure.com.apk";
	public final static String Z3BuildPath = "/Users/apple/Documents/z3-master/build";
	
	public final static boolean parallelEnabled = false;
	public final static boolean pathLimitEnables = true;
	public static int finalPathsLimit;
	
	public static Logger logger = LogManager.getLogger(Init.class);
	
	private static void sootInit(){
		soot.G.reset();
		Options.v().set_src_prec(Options.src_prec_apk);
		Options.v().set_android_jars(androidJarPath);
		Options.v().set_process_dir(Collections.singletonList(APKPath));
		Options.v().set_output_format(Options.output_format_none);
		
		Options.v().set_whole_program(true);
		Options.v().set_allow_phantom_refs(true);
		//danger option which will pause many methods
		//Options.v().set_no_bodies_for_excluded(true);
		Scene.v().loadNecessaryClasses();
	}
	
	private static void pathInit() {
		if(pathLimitEnables)
			finalPathsLimit = 100;
		else finalPathsLimit = Integer.MAX_VALUE;
	}
	
	public static void printSystemTime() {
		int y,m,d,h,mi,s,ms;      
	    Calendar cal=Calendar.getInstance();      
	    y = cal.get(Calendar.YEAR);      
	    m = cal.get(Calendar.MONTH);      
	    d = cal.get(Calendar.DATE);      
	    h = cal.get(Calendar.HOUR_OF_DAY);      
	    mi = cal.get(Calendar.MINUTE);      
	    s = cal.get(Calendar.SECOND);
	    ms = cal.get(Calendar.MILLISECOND);
	    System.out.println( h + ":" + mi + ":" + s + ":" + ms);      
	}
	
	public static void main(String[] args) throws Exception {
		sootInit();
		pathInit();
		
		SearchTransformer mySearch = new SearchTransformer();
        PackManager.v().getPack("jtp").add(new Transform("jtp.myInstrumenter",mySearch));
        PackManager.v().runPacks();
        
        Database.apkCG = Scene.v().getCallGraph();
        System.out.println("Find " + Database.entryPoints.size() + " entryMethods");
        printSystemTime();
        
        MethodAnalysis.analysis();
        PathAnalysis.analysis();
        return;
	}
}



