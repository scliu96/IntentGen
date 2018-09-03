package path.analysis.method;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

import path.analysis.assist.Config;
import path.analysis.assist.Database;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.callgraph.Edge;

public class MethodAnalysis {
	
	public static void analysis() throws Exception{
		if(Database.apkCG.size() == 0)
			throw new Exception("apkCG is empty");
		else if(Database.entryMethods.isEmpty())
			throw new Exception("entryPoints is empty");
		
		Stack<SootMethod> workMethods = new Stack<SootMethod>();
		for(SootMethod m : Database.entryMethods)
			workMethods.push(m);
		
		while(!workMethods.isEmpty()) {
			SootMethod currMethod = workMethods.pop();
			if(Database.analyzedMethods.contains(currMethod))
				continue;
			Database.analyzedMethods.add(currMethod);
			
			Map<Unit,SootMethod> nextMethods = new LinkedHashMap<Unit,SootMethod>();
			Iterator<Edge> outEdges = Database.apkCG.edgesOutOf(currMethod);
			if(outEdges != null)
				while(outEdges.hasNext()) {
					Edge e = outEdges.next();
					SootMethod tgt = (SootMethod) e.getTgt();
					if(!methodNeedsAnalysis(tgt))
						continue;
					nextMethods.put(e.srcUnit(), tgt);
					workMethods.push(tgt);
				}
			Database.nextMethodsMap.put(currMethod, nextMethods);
		}
	}
	
	private static boolean methodNeedsAnalysis(SootMethod method) {
		if(Database.analyzedMethods.size() > Config.analyzedMethodLimit)
			return false;
		if(method.isJavaLibraryMethod())
			return false;
		if(SearchTransformer.isApkClassName(method.getDeclaringClass().getName()))
			return false;
		if(!method.hasActiveBody() || method.getName().equals("d"))
			return false;
		return true;
	}
	
}
