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
	
	public static void visit(CallGraph cg,SootMethod m){
		CallGraph myCall = new CallGraph();
		System.out.println(m.getSignature());
		Iterator<Edge> outEdges = cg.edgesOutOf(m);
		if(outEdges != null)
			while(outEdges.hasNext()){
				
			}
        Iterator<MethodOrMethodContext> ctargets = new Targets(outEdges);
        if(ctargets != null){
            while(ctargets.hasNext()){
                SootMethod c = (SootMethod) ctargets.next();
                if(c == null)
                    System.out.println("c is null");
                List<Type> para = c.getParameterTypes();
                for(int i = 0;i < para.size(); i++){
                	if((para.get(i).toString().equals("android.content.Intent"))||(para.get(i).toString().equals("android.os.Bundle"))){
                		
                		visit(cg,c);
                	}
                }
            }
        }
    }
}
