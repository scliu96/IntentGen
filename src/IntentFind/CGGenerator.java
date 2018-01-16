package IntentFind;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import soot.Body;
import soot.MethodOrMethodContext;
import soot.PatchingChain;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

public class CGGenerator {
	private CallGraph myCG;
	private List<MyIntent> myIntents;
	
	public CGGenerator(){
		this.myCG = new CallGraph();
		this.myIntents = new LinkedList<MyIntent>();
	}
	
	public CGGenerator(CallGraph cg, List<SootMethod> points){
		this.myCG = cg;
		this.myIntents = new LinkedList<MyIntent>();
		for(SootMethod m : points){
			MyIntent intent = new MyIntent(m);
			this.myIntents.add(intent);
		}
	}
	
	public boolean explorePoints(){
		if(this.myCG.size() == 0)
			return false;
		if(this.myIntents.isEmpty())
			return false;
		//int size = this.myIntents.size();
		for(MyIntent mi: this.myIntents){
			MyIntent temp = this.visit(new ArrayList<SootMethod>(),mi.getLastMethod());
			System.out.println(temp.getLastMethod().toString());
			temp.proPrint();
		}
		return true;
	}
	
	public void printIntents(){
		for(MyIntent in : this.myIntents){
			//System.out.println(in.getMethod().toString() + ":");
			in.proPrint();
		}
	}
	
	private MyIntent visit(List<SootMethod> li,SootMethod m){
		MyIntent intent = new MyIntent(li);
		intent.addMethod(m);
		Body b = m.retrieveActiveBody();
		for(Value v : b.getParameterRefs()){
			String temp = v.getType().toString();
			if(temp.equals("android.content.Intent") || temp.equals("android.os.Bundle")){
				intent.relAdd(v);
			}
		}
		
		PatchingChain<Unit> units = b.getUnits();
		for(Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();){
			Unit u = iter.next();
			int breakFlag = 0;
			for(ValueBox v1 : u.getUseBoxes()){
				for(Value v2 : intent.getRel())
					if(v2.equals(v1.getValue())){
						for(ValueBox v : u.getDefBoxes()){
							String temp = v.getValue().getType().toString();
							if(temp.equals("android.content.Intent") || temp.equals("android.os.Bundle"))
								intent.relAdd(v.getValue());
						}
						MyStmtSwitch sw = new MyStmtSwitch();
						sw.inMyIntent(b,intent);
						u.apply(sw);
						intent = sw.outMyIntent();
						
						breakFlag = 1;
						break;
					}
				if(breakFlag == 1)
					break;
			}
		}
		
		Iterator<Edge> outEdges = this.myCG.edgesOutOf(m);
		if(outEdges != null)
			while(outEdges.hasNext()){
				Edge e = outEdges.next();
				if(e.srcStmt().containsInvokeExpr()){
					int breakFlag = 0;
					InvokeExpr invokeExpr = e.srcStmt().getInvokeExpr();
					for(Value v1:invokeExpr.getArgs()){
						for(Value v2 :intent.getRel())
							if(v1.equals(v2)){
								if(!intent.containMethod((SootMethod)e.getTgt())){
									MyIntent sub = this.visit(intent.getMethodList(), (SootMethod)e.getTgt());
									intent.mergeSubIntent(sub);
									breakFlag = 1;
									break;
								}
							}
						if(breakFlag == 1)
							break;
					}
				}
			}
		intent.relClean();
		return intent;
	}
}
