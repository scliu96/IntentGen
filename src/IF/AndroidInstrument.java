package IF;
import java.util.Collections;
import java.util.List;

import soot.PackManager;
import soot.Scene;
import soot.SootMethod;
import soot.Transform;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;

public class AndroidInstrument{
	//public final static String sootJarPath="/Users/apple/Documents/Soot/sootclasses-trunk-jar-with-dependencies.jar";
	public final static String androidJarPath="/Users/apple/Documents/Eclipse/android-platforms-master";
	public final static String APKPath="/Users/apple/Documents/AndroidStudio/release/app-release.apk";
	//public final static String rtJarPath="/Library/Java/JavaVirtualMachines/jdk1.7.0_80.jdk/Contents/Home/jre/lib/rt.jar";
	
	public static void initSoot(String[] args){
		Options.v().set_src_prec(Options.src_prec_apk);
		Options.v().set_android_jars(androidJarPath);
		Options.v().set_process_dir(Collections.singletonList(APKPath));
		Options.v().set_output_format(Options.output_format_none);
		Options.v().set_allow_phantom_refs(true);
		//test11
		Options.v().set_whole_program(true);
		Scene.v().loadNecessaryClasses();
	}
	
	public static void main(String[] args) {
		initSoot(args);
		
		SearchTransformer mySearch = new SearchTransformer();
        PackManager.v().getPack("jtp").add(new Transform("jtp.myInstrumenter",mySearch));
        PackManager.v().runPacks();
        
        CallGraph cg = Scene.v().getCallGraph();
        CGGenerator cgg = new CGGenerator(cg, mySearch.getEntryPoints());
        cgg.explorePoints();
        cgg.printIntents();
        List<Intent> result = cgg.getIntents();
        return;
	}
}



