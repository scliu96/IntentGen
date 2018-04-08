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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import IF.Init;
import Type.Path;
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
import soot.jimple.IfStmt;
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
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.SimpleLocalDefs;

public class PathAnalysisOnUnit {
	private static Logger logger = LogManager.getLogger(PathAnalysisOnUnit.class);
	
	public PathAnalysisOnUnit(){
	}
	
	private static boolean unitNeedsAnalysis(SootMethod method, String currClassName, Unit unit) {
		if (unit instanceof InvokeStmt) {
			InvokeStmt stmt = (InvokeStmt) unit;
			if ( stmt.getInvokeExpr().getMethod().getName().equals("d") )  {
				return false;
			}
		}
		return true;
	}
	
	public static boolean doPathAnalysisOnUnit(Path path) {
		SootMethod method = path.entryMethod;
		Body b = method.getActiveBody();
		PatchingChain<Unit> units = b.getUnits();
		BriefUnitGraph ug = new BriefUnitGraph(b);
		if(units.isEmpty())
			return false;
		Unit startingUnit = units.getFirst();
		
		boolean isFeasible = false;
		//boolean enumeratePathsOnly = false;
		
		Set<Unit> discoveredUnits = new LinkedHashSet<Unit>();
		discoveredUnits.add(startingUnit);
		
		Stack<Unit> workUnits = new Stack<Unit>();
		workUnits.push(startingUnit);
		
		Stack<Path> workPaths = new Stack<Path>();
		path.unitPath.add(startingUnit);
		workPaths.push(path);
		
		Set<Path> finalPaths = new LinkedHashSet<Path>();
		boolean hitPathsLimit = false;
		
		while(!workUnits.isEmpty()) {
			if(workPaths.size() != workUnits.size())
				logger.warn("WorkingUnits size is different from workPaths size");
			Unit startUnitOfCurrPath = workUnits.pop();
			Path currPath = workPaths.pop();
			discoveredUnits.add(startUnitOfCurrPath);
			
			if(ug.getSuccsOf(startUnitOfCurrPath).isEmpty()) {
				logger.trace("A final path: "+ currPath);
				if(finalPaths.size() < Init.finalPathsLimit)
					finalPaths.add(currPath);
				else hitPathsLimit = true;
			}
			System.out.println(startUnitOfCurrPath);
			for(Unit succ : ug.getSuccsOf(startUnitOfCurrPath)) {
				if(currPath.containUnit(succ)) {
					logger.trace("Loop detected while analyze unit "+ succ);
					continue;
				}
				System.out.println("succ:"+succ.toString());
				
				logger.trace("Fork the following path on unit " + succ);
				Path newPath = currPath.copy();
				newPath.addUnit(succ);
				workPaths.push(newPath);
				workUnits.push(succ);
				logger.trace("WorkingUnits size now is " + workUnits.size());
			}
			System.out.println();
		}
		/*
		List<Path> intraPaths = new ArrayList<Path>();
		for(Path currPath : finalPaths) {
			analyzeProgramPath(method,currPath);
		}
		*/
		return isFeasible;
	}
	
	public static void analyzeProgramPath(SootMethod method, Path path) {
		List<Unit> currPathList = path.unitPath;
		for(int i = 0; i<currPathList.size();i++) {
			Unit currUnitInPath = currPathList.get(i);
			Unit predUnit = null;
			if(i-1<currPathList.size() && i>=1)
				predUnit = currPathList.get(i-1);
			UnitGraph unitGraph = null;
			SimpleLocalDefs defs = null;
			if(method.hasActiveBody()) {
				unitGraph = new ExceptionalUnitGraph(method.getActiveBody());
				synchronized(method) {
					defs = new SimpleLocalDefs(unitGraph);
				}
			}
			else logger.warn("method " + method.getName() + " has no active body");
			
			try {
				Stmt currStmtInPath = (Stmt) currUnitInPath;
				Set<String> currExprs = new LinkedHashSet<String>();
				if(currStmtInPath instanceof IfStmt)
					StmtHandle.handleIfStmt(method, path, defs, (IfStmt) currStmtInPath);
				else if(currStmtInPath.containsInvokeExpr() && currStmtInPath instanceof DefinitionStmt) {
					StmtHandle.handleIntentGetExtraStmt(method, path, defs, (DefinitionStmt) currStmtInPath);
					StmtHandle.handleIntentGetActionStmt(method, path, defs, (DefinitionStmt) currStmtInPath);
				}
				else logger.warn("Not including condition for " + currUnitInPath + " to path constraint");
			}catch (NullPointerException e) {
                e.printStackTrace();
            }
			
		}
	}
	
}
