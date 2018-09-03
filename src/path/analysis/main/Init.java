package path.analysis.main;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;

import org.xmlpull.v1.XmlPullParserException;

import path.analysis.assist.*;
import path.analysis.method.MethodAnalysis;
import path.analysis.method.SearchTransformer;
import path.analysis.unit.PathAnalysis;
import soot.PackManager;
import soot.Scene;
import soot.SootMethod;
import soot.Transform;
import soot.jimple.infoflow.android.SetupApplication;
import soot.options.Options;

public class Init{
	
	private static void sootInit() throws IOException, XmlPullParserException{
		SetupApplication app = new SetupApplication(Config.androidJarPath,Config.apkPath);
		app.calculateSourcesSinksEntrypoints(Config.sourcesAndSinkFile);
		soot.G.reset();
		Options.v().set_src_prec(Options.src_prec_apk);
		Options.v().set_android_jars(Config.androidJarPath);
		Options.v().set_process_dir(Collections.singletonList(Config.apkPath));
		Options.v().set_output_format(Options.output_format_none);
		
		Options.v().set_whole_program(true);
		Options.v().set_allow_phantom_refs(true);
		//danger option which will pause many methods
		//Options.v().set_no_bodies_for_excluded(true);
		Scene.v().loadNecessaryClasses();
		SootMethod entryPoint = app.getEntryPointCreator().createDummyMain();
		Options.v().set_main_class(entryPoint.getSignature());
		Scene.v().setEntryPoints(Collections.singletonList(entryPoint));
	}
	
	private static void dataInit() {
		Database.clear();
	}
	
	private static void apkInit() {
		Config.apkNames.add("app-release.apk");
	}
	
	public static void main(String[] args) throws Exception {
		apkInit();
		
		for(String apkName: Config.apkNames) {
			Config.apkPath = Config.apkSourcePath + apkName;
			String fileName = Config.outputPath + apkName + "_results.txt";
			PrintWriter tempOut = new PrintWriter(fileName);
			try {
				Timer.tempOut = "";
				Timer.tempOut += apkName + "\n";
				Timer.tempOut += "Init\n";
				Timer.printSystemTime();
				sootInit();
				dataInit();
				Timer.tempOut += "Begin\n";
				
				Timer.printSystemTime();
				SearchTransformer mySearch = new SearchTransformer();
		        PackManager.v().getPack("jtp").add(new Transform("jtp.myInstrumenter",mySearch));
		        PackManager.v().runPacks();
		        
		        Timer.tempOut += "Find " + Database.entryMethods.size() + " entryMethods in ";
		        Timer.printSystemTime();
		  
		        Database.apkCG = Scene.v().getCallGraph();
		        if(Database.apkCG == null || Database.entryMethods.isEmpty()) {
		        	tempOut.close();
		        	continue;
		        }
		        
		        MethodAnalysis.analysis();
		        Timer.tempOut += "Find " + Database.analyzedMethods.size() + " methods need to be analyzed in ";
		        Timer.printSystemTime();
		        
		        PathAnalysis.analysis();
		        
		        tempOut.print(Timer.tempOut);
				tempOut.close();
			}catch (FileNotFoundException e) {
				continue;
			}
		}
        return;
	}
}



