package SSE;

import java.util.Iterator;
import java.util.Stack;

import IF.Init;
import Type.MethodPoint;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.Edge;

public class MethodAnalysis {
	
	public static void analysis() throws Exception{
		if(Init.apkCG.size() == 0)
			throw new Exception("apkCG is empty");
		else if(Init.entryPoints.isEmpty())
			throw new Exception("entryPoints is empty");
		
		Stack<MethodPoint> workPoints = new Stack<MethodPoint>();
		for(SootMethod m : Init.entryPoints)
			workPoints.push(new MethodPoint(m));
		
		while(!workPoints.isEmpty()) {
			MethodPoint currPoint = workPoints.pop();
			//System.out.println(currPoint.entryMethod);
			
			boolean breakFlag = false;
			for(MethodPoint mp : Init.methodPoints)
				if(mp.entryMethod.equals(currPoint.entryMethod)) {
					breakFlag = true;
					break;
				}
			if(breakFlag)
				continue;
			
			Iterator<Edge> outEdges = Init.apkCG.edgesOutOf(currPoint.entryMethod);
			if(outEdges != null)
				while(outEdges.hasNext()) {
					Edge e = outEdges.next();
					SootMethod tgt = (SootMethod) e.getTgt();
					if(!methodNeedsAnalysis(tgt))
						continue;
					currPoint.nextMethods.put(e.srcUnit(), tgt);
					workPoints.push(new MethodPoint(tgt));
				}
			Init.methodPoints.add(currPoint);
		}
	}
	
	private static boolean methodNeedsAnalysis(SootMethod method) {
		if(method.isJavaLibraryMethod())
			return false;
		if(method.getDeclaringClass().getName().contains("android."))
			return false;
		if(!method.hasActiveBody() || method.getName().equals("d"))
			return false;
		return true;
	}
	
	/*
	private void judgeStmt(Intent intent,Stmt stmt){
		if(stmt.containsInvokeExpr()){
			Value v = stmt.getInvokeExprBox().getValue();
			if(v instanceof JVirtualInvokeExpr){
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
			else if(v instanceof JSpecialInvokeExpr){
				JSpecialInvokeExpr jv = (JSpecialInvokeExpr) v;
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
