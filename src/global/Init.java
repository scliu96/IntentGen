package global;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlpull.v1.XmlPullParserException;

import path.analysis.method.MethodAnalysis;
import path.analysis.method.SearchTransformer;
import path.analysis.unit.PathAnalysis;
import soot.PackManager;
import soot.Scene;
import soot.SootMethod;
import soot.Transform;
import soot.Value;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;
import type.Intent;
import type.MethodPoint;
import type.UnitPath;

public class Init{
	private final static String androidJarPath = "/Users/apple/Documents/Eclipse/android-platforms-master";
	private static String APKName = "TurboVPN.apk";
	private static String APKPath = "/Users/apple/Documents/AndroidStudio/release/" + APKName;
	public final static String Z3BuildPath = "/Users/apple/Documents/z3-master/build";
	
	public final static boolean parallelEnabled = false;
	public final static boolean pathLimitEnables = true;
	public static int finalPathsLimit;
	public static int nowh = 0;
	public static int nowmi = 0;
	public static int nows = 0;
	public static int nowms = 0;
	public static String tempOut = "";
	
	public static Logger logger = LogManager.getLogger(Init.class);
	
	private static void sootInit() throws IOException, XmlPullParserException{
		SetupApplication app = new SetupApplication(androidJarPath,APKPath);
		app.calculateSourcesSinksEntrypoints("/Users/apple/Documents/Eclipse/soot-infoflow-android/SourcesAndSinks.txt");
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
		SootMethod entryPoint = app.getEntryPointCreator().createDummyMain();
		Options.v().set_main_class(entryPoint.getSignature());
		Scene.v().setEntryPoints(Collections.singletonList(entryPoint));
	}
	
	private static void pathInit() {
		if(pathLimitEnables)
			finalPathsLimit = 50;
		else finalPathsLimit = Integer.MAX_VALUE;
		Database.entryPoints.clear();
		Database.finalPathsMap.clear();
		Database.localSymbolMap.clear();
		Database.methodPathsMap.clear();
		Database.methodPointsMap.clear();
		Database.symbolLocalMap.clear();
		Database.valueKeyMap.clear();
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
	    tempOut += (h-nowh)+":"+(mi-nowmi)+":"+(s-nows)+":"+(ms-nowms) + "\n";
	    nowh = h;
	    nowmi = mi;
	    nows = s;
	    nowms = ms;
	}
	
	public static void main(String[] args) throws Exception {
		List<String> apkName = new LinkedList<String>();
		//apkName.add("app-release.apk");
		apkName.add("SuperVPN.apk");
		apkName.add("TurboVPN.apk");
		apkName.add("GooglePlayStore.apk");
		apkName.add("WumaVPN.apk");
		apkName.add("ESFile.apk");
		apkName.add("GoogleTranslate.apk");
		apkName.add("Tumblr.apk");
		apkName.add("FreeVPN.apk");
		apkName.add("LanternVPN.apk");
		apkName.add("AccuWeather.apk");
		apkName.add("YouTube.apk");
		apkName.add("Instagram.apk");
		
		for(String sss: apkName) {
			APKName = sss;
			APKPath = "/Users/apple/Documents/AndroidStudio/release/" + APKName;
			String fileName = "/Users/apple/Desktop" + File.separator + APKName + "_results.txt";
			PrintWriter zzzOut = new PrintWriter(fileName);
			try {
				tempOut = "";
				tempOut += APKName + "\n";
				tempOut += "Init\n";
				printSystemTime();
				sootInit();
				pathInit();
				tempOut += "Begin\n";
				printSystemTime();
				SearchTransformer mySearch = new SearchTransformer();
		        PackManager.v().getPack("jtp").add(new Transform("jtp.myInstrumenter",mySearch));
		        PackManager.v().runPacks();
		        
		        tempOut += "Find " + Database.entryPoints.size() + " entryMethods\n";
		        printSystemTime();
		  
		        Database.apkCG = Scene.v().getCallGraph();
		        if(Database.apkCG == null || Database.entryPoints.isEmpty()) {
		        		zzzOut.close();
		        		continue;
		        }
		        
		        MethodAnalysis.analysis();
		        tempOut += "Find " + Database.methodPointsMap.size() + " methods\n";
		        printSystemTime();
		        
		        PathAnalysis.analysis();
		        zzzOut.print(tempOut);
				zzzOut.close();
			}catch (FileNotFoundException e) {
				continue;
			}
		}
        return;
	}
}



