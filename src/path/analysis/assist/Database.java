package path.analysis.assist;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import path.analysis.type.Intent;
import path.analysis.type.MethodPoint;
import path.analysis.type.UnitPath;
import soot.Local;
import soot.SootMethod;
import soot.Value;
import soot.jimple.toolkits.callgraph.CallGraph;

public class Database {
	// call graph of methods
	public static CallGraph apkCG = null;
	
	// set of entry methods
	public static Set<SootMethod> entryPoints = new LinkedHashSet<SootMethod>();
	
	// key: method, value: methodPoint
	public static Map<SootMethod,MethodPoint> methodPointsMap = new LinkedHashMap<SootMethod,MethodPoint>();
	
	// key: method, value: methodPaths
	public static Map<SootMethod,Set<UnitPath>> methodPathsMap = new LinkedHashMap<SootMethod,Set<UnitPath>>();
	
	// key: path of inter-method units, value: intent corresponding to the path
	public static Map<UnitPath,Intent> finalPathsMap = new LinkedHashMap<UnitPath,Intent>();
	
	// key: a symbol used to represent a Local, value: the Local represented by the symbol
	public static Map<String,Local> symbolLocalMap = new HashMap<String,Local>();
	
	// key: a Local that is treated symbolically, value: the symbol used to represent the Local
	public static Map<Local,String> localSymbolMap = new LinkedHashMap<Local,String>();
	
	//
	public static Map<Value,String> valueKeyMap = new LinkedHashMap<Value,String>();
	
	public static void printEntryPoints() {
		String temp = "EntryPoints: \n";
		for(SootMethod m: entryPoints)
			temp += m.getDeclaringClass().getName() + "," + m.getName() + "\n";
		System.out.println(temp + "\n");
	}
	
	public static void printMethodPoints() {
		String temp = "MethodPoints: \n";
		for(SootMethod m: methodPointsMap.keySet())
			temp += m.getDeclaringClass().getName() + "," + m.getName() + "\n";
		System.out.println(temp + "\n");
	}
	
	public static void printMethodPaths() {
		String temp = "MethodPaths: ";
		
		System.out.println(temp + "\n");
	}
	
	public static void printFinalPaths() {
		String temp = "FinalPaths: ";
		
		System.out.println(temp + "\n");
	}
	
	public static void clear() {
		entryPoints.clear();
		methodPointsMap.clear();
		methodPathsMap.clear();
		finalPathsMap.clear();
		symbolLocalMap.clear();
		localSymbolMap.clear();
		valueKeyMap.clear();
	}
	
}
