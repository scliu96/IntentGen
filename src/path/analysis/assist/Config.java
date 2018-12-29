package path.analysis.assist;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Config {
	public final static String androidJarPath = "config/android-platforms/";
	public final static String sourcesAndSinkFile = "config/SourcesAndSinks.txt";
	public final static String androidCallBackFile = "config/AndroidCallbacks.txt";
	
	public final static String Z3BuildPath = "z3-master/build";
	public final static String Z3_RUNTIME_SPECS_DIR = "z3_runtime_specs";
	
	public static List<String> apkNames = new LinkedList<String>();
	public static String apkSourcePath = "apks/";
	public static String apkPath = "";
	public static String outputPath = "outputs/";
	
	public final static boolean parallelEnabled = false;
	public final static boolean pathLimitEnables = true;
	
	public final static int pathsLimitInOneMethod = 1000;
	public final static int analyzedMethodLimit = 1000;
	
	private static String[] androidPkgPrefixes = {"android.","com.android.","dalvik.","java.","javax.","junit.","org.apache.","org.json.","org.w3c.dom.","org.xml.sax","org.xmlpull."};
	public static Set<String> androidPkgPrefixesSet = new LinkedHashSet<String>(Arrays.asList(androidPkgPrefixes));

	private static String[] entryMethods = {"onCreate", "onStart", "onStartCommand", "onBind", "onUnbind"};
	public static Set<String> entryMethodsSet = new LinkedHashSet<String>(Arrays.asList(entryMethods));	
	
	private static String[] bundleExtraDataMethods = {"getString","getShort","getFloat","getBoolean","getDouble","getInt","getLong","getByte","hasExtra","containsKey"};
	public static Set<String> bundleExtraDataMethodsSet = new LinkedHashSet<String>(Arrays.asList(bundleExtraDataMethods));
	
	private static String[] getBundleMethods = {"getExtras","getBundleExtra"};
	public static Set<String> getBundleMethodsSet = new LinkedHashSet<String>(Arrays.asList(getBundleMethods));
	
	private static String[] categoryMethods = {"hasCategory","getCategories"};
	public static Set<String> categoryMethodsSet = new LinkedHashSet<String>(Arrays.asList(categoryMethods));
	
	private static String[] stringReturningIntentMethods = {"getAction","getStringExtra"};
	public static Set<String> stringReturningIntentMethodsSet = new LinkedHashSet<String>(Arrays.asList(stringReturningIntentMethods));
}
