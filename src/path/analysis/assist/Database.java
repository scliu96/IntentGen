package path.analysis.assist;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import path.analysis.type.Intent;
import path.analysis.type.UnitPath;
import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.toolkits.callgraph.CallGraph;

public class Database {
	public static CallGraph apkCG = null;
	public static Set<SootMethod> entryMethods = new LinkedHashSet<SootMethod>();
	public static Set<SootMethod> analyzedMethods = new LinkedHashSet<SootMethod>();
	
	public static Map<SootMethod,Map<Unit,SootMethod>> nextMethodsMap = new LinkedHashMap<SootMethod,Map<Unit,SootMethod>>();
	public static Map<SootMethod,Set<UnitPath>> innerPathsMap = new LinkedHashMap<SootMethod,Set<UnitPath>>();
	public static Map<SootMethod,Set<UnitPath>> interPathsMap = new LinkedHashMap<SootMethod,Set<UnitPath>>();
	public static Map<UnitPath,Intent> feasiblePathsMap = new LinkedHashMap<UnitPath,Intent>();
	
	public static Map<String,Local> symbolLocalMap = new HashMap<String,Local>();
	public static Map<Local,String> localSymbolMap = new LinkedHashMap<Local,String>();
	public static Map<Value,String> valueKeyMap = new LinkedHashMap<Value,String>();
	
	public static void printEntryMethods() {
		String temp = "EntryMethods: \n";
		for(SootMethod m: entryMethods)
			temp += m.getDeclaringClass().getName() + "," + m.getName() + "\n";
		System.out.println(temp + "\n");
	}
	
	public static void printAnalyzedMethods() {
		String temp = "AnalyzedMethods: \n";
		for(SootMethod m: analyzedMethods)
			temp += m.getDeclaringClass().getName() + "," + m.getName() + "\n";
		System.out.println(temp + "\n");
	}
	
	public static void printNextMethods() {
		String temp = "NextMethods: ";
		
		System.out.println(temp + "\n");
	}
	
	public static void printInnerPaths() {
		String temp = "InnerPaths: ";
		
		System.out.println(temp + "\n");
	}
	
	public static void printInterPaths() {
		String temp = "InterPaths: ";
		
		System.out.println(temp + "\n");
	}
	
	public static void printFeasiblePaths() {
		String temp = "FeasiblePaths: ";
		
		System.out.println(temp + "\n");
	}
	
	public static void clear() {
		entryMethods.clear();
		analyzedMethods.clear();
		nextMethodsMap.clear();
		innerPathsMap.clear();
		interPathsMap.clear();
		feasiblePathsMap.clear();
		symbolLocalMap.clear();
		localSymbolMap.clear();
		valueKeyMap.clear();
	}
	
}
