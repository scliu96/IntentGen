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
	
	private void doPathAnalysis(SootMethod m) {
		Body b = m.getActiveBody();
		PatchingChain<Unit> units = b.getUnits();
		BriefUnitGraph ug = new BriefUnitGraph(b);
		String currClassName = m.getDeclaringClass().getName();
		int totalUnitsToAnalyzeCount = 0;
		int currUnitToAnalyzeCount = 0;
		for (final Unit unit : units) {
			boolean performPathAnalysis = false;
			synchronized(m) {
				performPathAnalysis = unitNeedsAnalysis(m, currClassName, unit);
			}
			
			if (performPathAnalysis) {
				logger.trace("Performing path analysis for unit: " + unit);
				logger.trace("Currently analyzing unit " + currUnitToAnalyzeCount + " of" + totalUnitsToAnalyzeCount);
				doPathAnalysisOnUnit(m, ug, currClassName, unit);
				totalUnitsToAnalyzeCount++;
				currUnitToAnalyzeCount++;
				logger.trace("totalUnitsToAnalyzeCount: " + totalUnitsToAnalyzeCount + " , currUnitToAnalyzeCount: "+ currUnitToAnalyzeCount);
				logger.trace("Finish path analysis on unit: " + unit);
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
	
	private boolean doPathAnalysisOnUnit(SootMethod m, BriefUnitGraph ug, String currClassName, Unit startingUnit) {
		boolean isFeasible = false;
		boolean enumeratePathsOnly = false;
		
		Set<Unit> discoveredUnits = new LinkedHashSet<Unit>();
		discoveredUnits.add(startingUnit);
		
		Stack<Unit> workUnits = new Stack<Unit>();
		workUnits.push(startingUnit);
		
		Stack<Path> workPaths = new Stack<Path>();
		Path initialPath = new Path(startingUnit);
		workPaths.push(initialPath);
		
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
			
			for(Unit succ : ug.getSuccsOf(startUnitOfCurrPath)) {
				if(currPath.containUnit(succ)) {
					logger.trace("Loop detected while analyze unit "+ succ);
					continue;
				}
				
				logger.trace("Fork the following path on unit " + succ);
				Path newPath = currPath.copy();
				newPath.addUnit(succ);
				workPaths.push(newPath);
				workUnits.push(succ);
				logger.trace("WorkingUnits size now is " + workUnits.size());
			}
		}
		
		List<Path> intraPaths = new ArrayList<Path>();
		for(Path currPath : finalPaths) {
			analyzeProgramPath(m,currPath);
		}
		return true;
	}
	
	public void analyzeProgramPath(SootMethod m, Path p) {
		List<Unit> currPathList = p.unitPath;
		for(int i = 0; i<currPathList.size();i++) {
			Unit currUnitInPath = currPathList.get(i);
			Unit predUnit = null;
			if(i-1<currPathList.size() && i>=1)
				predUnit = currPathList.get(i-1);
			UnitGraph unitGraph = null;
			SimpleLocalDefs defs = null;
			if(m.hasActiveBody()) {
				unitGraph = new ExceptionalUnitGraph(m.getActiveBody());
				synchronized(m) {
					defs = new SimpleLocalDefs(unitGraph);
				}
			}
			else logger.warn("method " + m.getName() + " has no active body");
			
			try {
				Stmt currStmtInPath = (Stmt) currUnitInPath;
				Set<String> currExprs = new LinkedHashSet<String>();
			}catch (NullPointerException e) {
                e.printStackTrace();
            }
			
		}
	}
	
}
