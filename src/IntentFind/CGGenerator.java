package IntentFind;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import soot.jimple.DefinitionStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.internal.JInterfaceInvokeExpr;
import soot.jimple.internal.JSpecialInvokeExpr;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.jimple.internal.JimpleLocal;
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
		for(SootMethod m : points)
			this.myIntents.add(new MyIntent(m));
	}
	
	public boolean explorePoints(){
		if(this.myCG.size() == 0)
			return false;
		if(this.myIntents.isEmpty())
			return false;
		for(MyIntent intent : this.myIntents)
			this.visit(intent, intent.getMethod());
		return true;
	}
	
	public List<MyIntent> getIntents(){
		return this.myIntents;
	}
	
	public void printIntents(){
		for(MyIntent intent : this.myIntents){
			System.out.println(intent.getMethod().toString() + ":");
			intent.printProperty();
		}
	}
	
	private void visit(MyIntent intent,SootMethod m){
		intent.addMethod(m);
		Body b = m.retrieveActiveBody();
		for(Value v : b.getParameterRefs()){
			String temp = v.getType().toString();
			if(temp.equals("android.content.Intent"))
				intent.addIntent(v);
			else if(temp.equals("android.os.Bundle"))
				intent.addBundle(v);
		}
		
		PatchingChain<Unit> units = b.getUnits();
		for(Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();){
			Unit u = iter.next();
			this.compareValue(intent, u);
		}
		
		Iterator<Edge> outEdges = this.myCG.edgesOutOf(m);
		if(outEdges != null)
			while(outEdges.hasNext()){
				Edge e = outEdges.next();
				if(e.srcStmt().containsInvokeExpr()){
					int breakFlag = 0;
					InvokeExpr invokeExpr = e.srcStmt().getInvokeExpr();
					for(Value v1:invokeExpr.getArgs()){
						for(Value v2 :intent.getIntent())
							if(v1.equals(v2)){
								if(!intent.containMethod((SootMethod)e.getTgt())){
									List<SootMethod> temp0 = intent.getMethods();
									List<Value> temp1 = intent.getIntent();
									List<Value> temp2 = intent.getBundle();
									this.visit(intent, (SootMethod)e.getTgt());
									intent.setMethods(temp0);
									intent.setIntent(temp1);
									intent.setBundle(temp2);
									breakFlag = 1;
									break;
								}
							}
						if(breakFlag == 1)
							break;
					}
				}
			}
	}
	
	private void compareValue(MyIntent intent, Unit u){
		int breakFlag = 0;
		for(ValueBox v1 : u.getUseBoxes()){
			
			for(Value v2 : intent.getIntent())
				if(v2.equals(v1.getValue())){
					for(ValueBox v : u.getDefBoxes()){
						String temp = v.getValue().getType().toString();
						if(temp.equals("android.content.Intent"))
							intent.addIntent(v.getValue());
						else if(temp.equals("android.os.Bundle"))
							intent.addBundle(v.getValue());
					}
					this.judgeStmt(intent, (Stmt)u );
					breakFlag = 1;
					break;
				}
			if(breakFlag == 1)
				break;
			
			for(Value v2 : intent.getBundle())
				if(v2.equals(v1.getValue())){
					for(ValueBox v : u.getDefBoxes()){
						String temp = v.getValue().getType().toString();
						if(temp.equals("android.content.Intent"))
							intent.addIntent(v.getValue());
						else if(temp.equals("android.os.Bundle"))
							intent.addBundle(v.getValue());
					}
					this.judgeStmt(intent, (Stmt)u );
					breakFlag = 1;
					break;
				}
			if(breakFlag == 1)
				break;
		}
	}
	
	private void judgeStmt(MyIntent intent,Stmt stmt){
		if(stmt.containsInvokeExpr()){
			Value v = stmt.getInvokeExprBox().getValue();
			if(v instanceof JVirtualInvokeExpr){
				JVirtualInvokeExpr jv = (JVirtualInvokeExpr) v;
				Value base = jv.getBase();
				if(base.getType().toString().equals("android.content.Intent")){
					String name = jv.getMethodRef().name();
					if((name.contains("Extra")) && (!name.equals("hasExtra")))
						if(jv.getArgCount() > 0){
							Value arg0 = jv.getArg(0);
							if(arg0.getType().toString().equals("java.lang.String")){
								if(arg0 instanceof StringConstant)
									intent.addProperty(((StringConstant)arg0).value, jv.getType());
								else{
									JimpleLocal local = (JimpleLocal) arg0;
									intent.addProperty(local.getName(), jv.getType());
								}
							}
						}
				}
				else if(base.getType().toString().equals("android.os.Bundle")){
					if(jv.getArgCount() > 0){
						Value arg0 = jv.getArg(0);
						if(arg0.getType().toString().equals("java.lang.String"))
							if(arg0 instanceof StringConstant)
								intent.addProperty(((StringConstant)arg0).value, jv.getType());
							else{
								JimpleLocal local = (JimpleLocal) arg0;
								intent.addProperty(local.getName(), jv.getType());
							}
					}
				}
			}
			else if(v instanceof JSpecialInvokeExpr){
				JSpecialInvokeExpr jv = (JSpecialInvokeExpr) v;
				Value base = jv.getBase();
				if(base.getType().toString().equals("android.content.Intent")){
					String name = jv.getMethodRef().name();
					if((name.contains("Extra")) && (!name.equals("hasExtra")))
						if(jv.getArgCount() > 0){
							Value arg0 = jv.getArg(0);
							if(arg0.getType().toString().equals("java.lang.String"))
								if(arg0 instanceof StringConstant)
									intent.addProperty(((StringConstant)arg0).value, jv.getType());
								else{
									JimpleLocal local = (JimpleLocal) arg0;
									intent.addProperty(local.getName(), jv.getType());
								}
						}
				}
				else if(base.getType().toString().equals("android.os.Bundle")){
					if(jv.getArgCount() > 0){
						Value arg0 = jv.getArg(0);
						if(arg0.getType().toString().equals("java.lang.String"))
							if(arg0 instanceof StringConstant)
								intent.addProperty(((StringConstant)arg0).value, jv.getType());
							else{
								JimpleLocal local = (JimpleLocal) arg0;
								intent.addProperty(local.getName(), jv.getType());
							}
					}
				}
			}
		}
	}
}
