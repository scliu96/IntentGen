package flowdroidCG;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.MethodOrMethodContext;
import soot.SootMethod;
import soot.Type;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;

public class CGGenerator {

	private static Map<String,Boolean> visited = new HashMap<String,Boolean>();
    public static CGExporter cge = new CGExporter();
    private static int count = 0;
    
	public static void visit(CallGraph cg,SootMethod m){
		count++;
        String identifier = m.getSignature();
        visited.put(m.getSignature(), true);
        cge.createNode(m.getSignature());
        for(int i = 0;i < count; i++)
        	System.out.print(" ");
        System.out.println(identifier);
        
        /*
        Iterator<MethodOrMethodContext> ptargets = new Targets(cg.edgesInto(m));
        if(ptargets != null){
            while(ptargets.hasNext()){
                SootMethod p = (SootMethod) ptargets.next();
                if(p == null)
                    System.out.println("p is null");
                if(!visited.containsKey(p.getSignature()))
                    visit(cg,p);
            }
        }
        */
        Iterator<MethodOrMethodContext> ctargets = new Targets(cg.edgesOutOf(m));
        if(ctargets != null){
            while(ctargets.hasNext()){
                SootMethod c = (SootMethod) ctargets.next();
                if(c == null)
                    System.out.println("c is null");
                ///List<Type> para = c.getParameterTypes();
                //for(int i = 0;i < para.size(); i++)
                	//if((para.get(i).toString().equals("android.content.Intent"))||(para.get(i).toString().equals("android.os.Bundle")))
                		if(!visited.containsKey(c.getSignature()))
                            visit(cg,c);
                //cge.createNode(c.getSignature());
                //cge.linkNodeByID(identifier, c.getSignature());
            }
        }
        count--;
        
    }
}
