package path.analysis.method;

import java.util.Iterator;
import java.util.Stack;

import path.analysis.assist.Config;
import path.analysis.assist.Database;
import path.analysis.main.Init;
import path.analysis.type.MethodPoint;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.Edge;

public class MethodAnalysis {
	
	public static void analysis() throws Exception{
		if(Database.apkCG.size() == 0)
			throw new Exception("apkCG is empty");
		else if(Database.entryPoints.isEmpty())
			throw new Exception("entryPoints is empty");
		
		Stack<MethodPoint> workPoints = new Stack<MethodPoint>();
		for(SootMethod m : Database.entryPoints)
			workPoints.push(new MethodPoint(m));
		
		while(!workPoints.isEmpty()) {
			MethodPoint currPoint = workPoints.pop();
			boolean breakFlag = false;
			for(SootMethod method : Database.methodPointsMap.keySet())
				if(method.equals(currPoint.entryMethod)) {
					breakFlag = true;
					break;
				}
			if(breakFlag)
				continue;
			
			Iterator<Edge> outEdges = Database.apkCG.edgesOutOf(currPoint.entryMethod);
			if(outEdges != null)
				while(outEdges.hasNext()) {
					Edge e = outEdges.next();
					SootMethod tgt = (SootMethod) e.getTgt();
					if(!methodNeedsAnalysis(tgt))
						continue;
					currPoint.nextMethods.put(e.srcUnit(), tgt);
					workPoints.push(new MethodPoint(tgt));
				}
			Database.methodPointsMap.put(currPoint.entryMethod, currPoint);
		}
	}
	
	private static boolean methodNeedsAnalysis(SootMethod method) {
		if(Database.methodPointsMap.size() > Config.methodPointsLimit)
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
