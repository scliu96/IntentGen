package IF;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import SSE.PathAnalysisOnMethod;
import SSE.PathAnalysisOnUnit;
import Type.Path;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SootMethod;
import soot.Transform;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;

public class Init{
	public final static String androidJarPath="/Users/apple/Documents/Eclipse/android-platforms-master";
	public final static String APKPath="/Users/apple/Documents/AndroidStudio/release/app-release.apk";
	
	public final static boolean parallelEnabled = false;
	public final static boolean pathLimitEnables = true;
	public static int finalPathsLimit;
	
	public static PathAnalysisOnUnit pathAnalysisOnUnit = new PathAnalysisOnUnit();
	
	public static void sootInit(){
		Options.v().set_src_prec(Options.src_prec_apk);
		Options.v().set_android_jars(androidJarPath);
		Options.v().set_process_dir(Collections.singletonList(APKPath));
		Options.v().set_output_format(Options.output_format_none);
		Options.v().set_allow_phantom_refs(true);
		//test11
		Options.v().set_whole_program(true);
		Scene.v().loadNecessaryClasses();
	}
	
	private static void pathInit() {
		if(pathLimitEnables)
			finalPathsLimit = 100;
		else finalPathsLimit = Integer.MAX_VALUE;
	}
	
	public static void main(String[] args) {
		sootInit();
		pathInit();
		
		SearchTransformer mySearch = new SearchTransformer();
        PackManager.v().getPack("jtp").add(new Transform("jtp.myInstrumenter",mySearch));
        PackManager.v().runPacks();
        
        //CallGraph cg = Scene.v().getCallGraph();
        //System.out.println(mySearch.getEntryPoints());
        //debug about unit call graph in many ways
        
        //PathAnalysisOnMethod methodPathAnalysis = new PathAnalysisOnMethod(cg, mySearch.getEntryPoints());
        //methodPathAnalysis.exploreEntryPoints();
        mySearch.printEntryPoints();
        Set<Path> paths = mySearch.generatePaths();
        for(Path path : paths) {
        		PathAnalysisOnUnit.doPathAnalysisOnUnit(path);
        }
        
        return;
	}
}



