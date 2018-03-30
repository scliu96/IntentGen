package SSE;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Pattern;

import org.javatuples.Pair;

import IF.Init;
import Type.Path;
import edu.uci.seal.StopWatch;
import soot.Body;
import soot.Local;
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
import soot.toolkits.graph.BriefUnitGraph;

public class PathAnalysis {
	private CallGraph apkCG = new CallGraph();
	private Set<SootMethod> entryPoints = new LinkedHashSet<SootMethod>();
	private Set<Path> paths = new LinkedHashSet<Path>();
	
	//private ExecutorService executor;
	
	public PathAnalysis(){
		super();
		/*
		if (Init.parallelEnabled)
			executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		else executor = Executors.newSingleThreadExecutor();
		if (executor instanceof ThreadPoolExecutor) {
			((ThreadPoolExecutor) executor).setRejectedExecutionHandler(new RejectedExecutionHandler() {
				public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
					try {
						executor.getQueue().put(r);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			});
		}*/
	}
	
	public PathAnalysis(CallGraph cg, Set<SootMethod> points){
		this();
		apkCG = cg;
		entryPoints.addAll(points);
	}
	
	public boolean exploreEntryPoints(){
		if(apkCG.size() == 0)
			return false;
		else if(entryPoints.isEmpty())
			return false;
		for(SootMethod m : entryPoints) {
			Path newPath = new Path(m);
			generateMethodPath(m, newPath);
		}
		System.out.println(paths.size());
		return true;
	}
	
	private void generateMethodPath(SootMethod m, Path p) {
		paths.add(p);
		//Body b = m.retrieveActiveBody();
		Iterator<Edge> outEdges = apkCG.edgesOutOf(m);
		int count = 0;
		if(outEdges != null)
			while(outEdges.hasNext()) {
				Edge e = outEdges.next();
				Path newPath = p.copy();
				if(newPath.addMethod(e)) {
					generateMethodPath((SootMethod) e.getTgt(), newPath);
					count++;
				}
			}
		if(count != 0)
			paths.remove(p);
	}
	
	
	private void doPathAnalysis(SootMethod m) {
		Body b = m.getActiveBody();
		PatchingChain<Unit> units = b.getUnits();
		BriefUnitGraph ug = new BriefUnitGraph(b);
		String currClassName = m.getDeclaringClass().getName();
		
		int totalUnitsToAnalyzeCount=0;
		int currUnitToAnalyzeCount=0;
		for (final Unit unit : units) {
			boolean performPathAnalysis = false;
			synchronized(m) {
				performPathAnalysis = unitNeedsAnalysis(m, currClassName, unit);
			}
			
			if (performPathAnalysis) {
				//doPathAnalysisOnUnitUsingExecutor(method, ug, currClassName, unit);
				totalUnitsToAnalyzeCount++;
				currUnitToAnalyzeCount++;
			}
		}
	}
	
	private boolean doPathAnalysisOnUnit(SootMethod m, BriefUnitGraph ug, String currClassName, Unit startingUnit) {
		boolean isFeasible = false;
		boolean enumeratePathsOnly = false;
		
		Set<Unit> discoveredUnits = new LinkedHashSet<Unit>();
		discoveredUnits.add(startingUnit);
		
		Stack<Unit> workUnits = new Stack<Unit>();
		workUnits.push(startingUnit);
		
		Stack<List<Unit>> workPaths = new Stack<List<Unit>>();
		List<Unit> initialPath = new ArrayList<Unit>();
		initialPath.add(startingUnit);
		workPaths.push(initialPath);
		
		Set<List<Unit>> finalPaths = new LinkedHashSet<List<Unit>>();
		boolean hitPathsLimit = false;
		
		while(!workUnits.isEmpty()) {
			if(workPaths.size() != workUnits.size())
				throw new RuntimeException("workUnits size is different from workPaths size");
			Unit startUnitOfCurrPath = workUnits.pop();
			List<Unit> currPath = workPaths.pop();
			discoveredUnits.add(startUnitOfCurrPath);
			
			if(ug.getSuccsOf(startUnitOfCurrPath).isEmpty())
				System.out.println("End Of Path");
			
			for(Unit succ : ug.getSuccsOf(startUnitOfCurrPath)) {
				if(currPath.contains(succ)) {
					System.out.println("loop finder");
					continue;
				}
			}
		}
	}
	
	private boolean unitNeedsAnalysis(SootMethod m, String currClassName, Unit unit) {
		if (unit instanceof InvokeStmt) {
			InvokeStmt stmt = (InvokeStmt) unit;
			if ( stmt.getInvokeExpr().getMethod().getName().equals("d") )  {
				return true;
			}
		}
		return false;
	}
	
	/*
	private void visit(Intent intent,SootMethod m){
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
	
	private void compareValue(Intent intent, Unit u){
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
