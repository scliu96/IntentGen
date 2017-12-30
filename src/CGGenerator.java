import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.MethodOrMethodContext;
import soot.SootMethod;
import soot.Type;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.Targets;

public class CGGenerator {
	
	public static CallGraph visit(CallGraph cg,SootMethod m){
		CallGraph myCall = new CallGraph();
		System.out.println(m.getSignature());
		Iterator<Edge> outEdges = cg.edgesOutOf(m);
		if(outEdges != null)
			while(outEdges.hasNext()){
				Edge e = outEdges.next();
				SootMethod c = (SootMethod) e.getTgt();
				List<Type> para = c.getParameterTypes();
                for(int i = 0;i < para.size(); i++){
                	if((para.get(i).toString().equals("android.content.Intent"))||(para.get(i).toString().equals("android.os.Bundle"))){
                		myCall.addEdge(e);
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
