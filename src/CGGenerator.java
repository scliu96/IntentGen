import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import soot.MethodOrMethodContext;
import soot.SootMethod;
import soot.Type;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

public class CGGenerator {
	public static List<CallGraph> entryGraphs = new LinkedList<CallGraph>();
	public static List<SootMethod> Points = new LinkedList<SootMethod>();
	
	public void getCallGraph(CallGraph cg, List<SootMethod> entryPoints){
		Points = entryPoints;
		for(int i = 0; i < entryPoints.size(); i++)
			entryGraphs.add(visit(cg, entryPoints.get(i)));
	}
	
	private static CallGraph visit(CallGraph cg, SootMethod m){
		CallGraph myCall = new CallGraph();
		//System.out.println(m.getSignature());
		Iterator<Edge> outEdges = cg.edgesOutOf(m);
		if(outEdges != null)
			while(outEdges.hasNext()){
				Edge e = outEdges.next();
				SootMethod c = (SootMethod) e.getTgt();
				List<Type> para = c.getParameterTypes();
                for(int i = 0;i < para.size(); i++){
                	if((para.get(i).toString().equals("android.content.Intent"))||(para.get(i).toString().equals("android.os.Bundle"))){
                		myCall.addEdge(e);
                		Points.add(c);
                		mergeCallGraph(myCall,visit(cg,c));
                	}
                }
			}
		return myCall;
        //Iterator<MethodOrMethodContext> ctargets = new Targets(outEdges);
    }
	
	private static void mergeCallGraph(CallGraph cgMain, CallGraph cgSub){
		if(cgSub.size() == 0)
			return;
		
		Iterator<MethodOrMethodContext> allSource = cgSub.sourceMethods();
		if(allSource != null)
			while(allSource.hasNext()){
				MethodOrMethodContext m = allSource.next();
				Iterator<Edge> outEdges = cgSub.edgesOutOf(m);
				if(outEdges != null)
					while(outEdges.hasNext()){
						Edge e = outEdges.next();
						cgMain.addEdge(e);
					}
			}
	}
}
