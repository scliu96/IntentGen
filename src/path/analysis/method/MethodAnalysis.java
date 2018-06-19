package path.analysis.method;

import java.util.Iterator;
import java.util.Stack;

import global.Database;
import global.Init;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.Edge;
import type.MethodPoint;

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
		//System.out.println(method.toString());
		if(Database.methodPointsMap.size() > 1000)
			return false;
		if(method.isJavaLibraryMethod())
			return false;
		if(method.getDeclaringClass().getPackageName().startsWith("android."))
			return false;
		if(!method.hasActiveBody() || method.getName().equals("d"))
			return false;
		return true;
	}
	
	/*
	private void judgeStmt(Intent intent,Stmt stmt){
		if(stmt.containsInvokeExpr()){
			Value v = stmt.getInvokeExprBox().getValue();
			if(v instanceof JVirtualInvokeExpr){JSpecialInvokeExpr
				JVirtualInvokeExpr jv = (JVirtualInvokeExpr) v;
				Value base = jv.getBase();
				if(base.getType().toString().equals("android.content.Intent") &&
						Pattern.matches("get.*Extra", jv.getMethodRef().toString()) ){
					if(jv.getArgCount() > 0){
						Value extraKeyValue = jv.getArg(0);
						if(extraKeyValue instanceof StringConstant)
							intent.addProperty( ((StringConstant)extraKeyValue).value, jv.getType());
						else if(extraKeyValue instanceof Local){
							Local extraKeyLocal = (Local) extraKeyValue;
							intent.addProperty(extraKeyLocal.getName(), jv.getType());
						}
					}
				}
				else if(base.getType().toString().equals("android.os.Bundle") &&
						Pattern.matches("get.*", jv.getMethodRef().toString()) ){
					if(jv.getArgCount() > 0){
						Value extraKeyValue = jv.getArg(0);
						if(extraKeyValue instanceof StringConstant)
							intent.addProperty( ((StringConstant)extraKeyValue).value, jv.getType());
						else if(extraKeyValue instanceof Local){
							Local extraKeyLocal = (Local) extraKeyValue;
							intent.addProperty(extraKeyLocal.getName(), jv.getType());
						}
					}
				}
			}
		}
	}*/
}
