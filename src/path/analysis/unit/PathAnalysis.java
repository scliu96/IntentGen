package path.analysis.unit;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import path.analysis.assist.Config;
import path.analysis.assist.Database;
import path.analysis.assist.Timer;
import path.analysis.type.UnitPath;
import soot.Body;
import soot.PatchingChain;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.DefinitionStmt;
import soot.jimple.IfStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.SimpleLocalDefs;

public class PathAnalysis {
	
	public static void analysis() throws Exception{
		long innerPathsNum = 0;
		for(SootMethod m : Database.analyzedMethods)
			innerPathsNum += analyzeInnerPaths(m);
		Timer.tempOut += "Find " + innerPathsNum + " inner-method paths in ";
		Timer.printSystemTime();
		Timer.tempOut += "Average " + (float)innerPathsNum/(float)Database.analyzedMethods.size() + " in one method \n";
		
		for(SootMethod m : Database.analyzedMethods)
			symbolGenInnerPaths(m);
        Timer.tempOut += "Generate Symbols for inner-method paths in ";
        Timer.printSystemTime();
        
		int totalInterMethodPath = 0;
		for(SootMethod m : Database.entryMethods) {
			Set<UnitPath> interPaths = analyzeInterPaths(m);
			totalInterMethodPath += interPaths.size();
			Database.interPathsMap.put(m, interPaths);
		}
		Timer.tempOut += "Find " + totalInterMethodPath + " inter-method paths in ";
		Timer.printSystemTime();
		/*
		for(SootMethod m : Database.entryMethods)
			for(UnitPath currPath : Database.interPathsMap.get(m))
				Solve.runSolvingPhase(m, currPath);
		Timer.tempOut += "Find " + Database.finalPathsMap.size() + " feasible paths in ";
		Timer.printSystemTime();*/
	}
	
	private static int analyzeInnerPaths(SootMethod method) throws Exception {
		Body b = method.getActiveBody();
		PatchingChain<Unit> units = b.getUnits();
		BriefUnitGraph ug = new BriefUnitGraph(b);
		
		if(units.isEmpty())
			return 0;
		Unit startingUnit = units.getFirst();
		Stack<Unit> workUnits = new Stack<Unit>();
		workUnits.push(startingUnit);
		Stack<UnitPath> workPaths = new Stack<UnitPath>();
		UnitPath newUp = new UnitPath(startingUnit);
		workPaths.push(newUp);
		
		Set<UnitPath> paths = new LinkedHashSet<UnitPath>();
		while(!workUnits.isEmpty()) {
			if(workPaths.size() != workUnits.size())
				throw new Exception("workUnits size is different from workPaths size");
			
			Unit currUnit = workUnits.pop();
			UnitPath currPath = workPaths.pop();
			if(ug.getSuccsOf(currUnit).isEmpty()) {
				System.out.println("A inner-method path :\n" + currPath.toUnitString());
				if(paths.size() < Config.pathsLimitInOneMethod)
					paths.add(currPath);
			}
			
			for(Unit succUnit : ug.getSuccsOf(currUnit)) {
				if(currPath.path.contains(succUnit)){
					System.out.println("Loop detected while analyze method : "+ method.getName());
					if(currPath.path.indexOf(succUnit) == currPath.path.size()-1)
						continue;
					workUnits.push(succUnit);
					workPaths.push(currPath);
					continue;
				}
				System.out.println("Fork the following path on unit: " + succUnit);
				UnitPath newPath = new UnitPath(currPath,succUnit);
				workUnits.push(succUnit);
				workPaths.push(newPath);
				System.out.println("WorkingUnits size now is: " + workUnits.size());
			}
		}
		Database.innerPathsMap.put(method, paths);
		return paths.size();
	}
	
	private static void symbolGenInnerPaths(SootMethod method) {
		Set<UnitPath> innerPaths = Database.innerPathsMap.get(method);
		for(UnitPath currPath : innerPaths) {
			for(int i = 0; i<currPath.path.size(); i++) {
				Unit currUnitInPath = currPath.path.get(i);
				if(!unitNeedsGen(currUnitInPath))
					continue;
				UnitGraph unitGraph = null;
				SimpleLocalDefs defs = null;
				if(method.hasActiveBody()) {
					unitGraph = new ExceptionalUnitGraph(method.getActiveBody());
					defs = new SimpleLocalDefs(unitGraph);
				}
				else System.out.println("method " + method.getName() + " has no active body");
				
				try {
					Stmt currStmtInPath = (Stmt) currUnitInPath;
					if(currStmtInPath instanceof IfStmt)
						StmtHandle.handleIfStmt(method, currPath, defs, (IfStmt) currStmtInPath);
					else if(currStmtInPath.containsInvokeExpr() && currStmtInPath instanceof DefinitionStmt) {
						StmtHandle.handleIntentGetExtraStmt(method, currPath, defs, (DefinitionStmt) currStmtInPath);
						StmtHandle.handleIntentGetActionStmt(method, currPath, defs, (DefinitionStmt) currStmtInPath);
					}
					else System.out.println("Not including condition for " + currUnitInPath + " to path constraint");
				}catch (NullPointerException e) {
	                e.printStackTrace();
	            }
			}
		}
	}
	
	private static boolean unitNeedsGen(Unit unit) {
		if (unit instanceof InvokeStmt) {
			InvokeStmt stmt = (InvokeStmt) unit;
			if ( stmt.getInvokeExpr().getMethod().getName().equals("d") )
				return false;
		}
		return true;
	}
	
	private static Set<UnitPath> analyzeInterPaths(SootMethod m) {
		Map<Unit,SootMethod> nextMethods = Database.nextMethodsMap.get(m);
		Set<UnitPath> innerPaths = Database.innerPathsMap.get(m);
		
		Set<UnitPath> interPaths = new LinkedHashSet<UnitPath>();
		for(UnitPath path : innerPaths) {
			//use callUnits map to restore for if stmt conditions
			Map<Unit,Unit> callUnits = new LinkedHashMap<Unit,Unit>();
			for(Unit unit : path.path)
				if(Database.nextMethodsMap.get(m).containsKey(unit))
					callUnits.put(unit, path.path.get(path.path.indexOf(unit)+1));
			List<UnitPath> workPaths = new LinkedList<UnitPath>();
			List<UnitPath> analyzedPaths = new LinkedList<UnitPath>();
			
			workPaths.add(path);
			while(!workPaths.isEmpty()) {
				UnitPath nowPath = workPaths.get(0);
				workPaths.remove(nowPath);
				analyzedPaths.add(nowPath);
				int callUnitNum = 0;
				for(Unit callUnit : callUnits.keySet()) {
					if(nowPath.path.contains(callUnit)) {
						if(nowPath.path.get(nowPath.path.indexOf(callUnit)+1).equals(callUnits.get(callUnit))) {
							callUnitNum++;
							Set<UnitPath> conPaths = null;
							if(Database.interPathsMap.containsKey(nextMethods.get(callUnit)))
								conPaths = Database.interPathsMap.get(nextMethods.get(callUnit));
							else if(Database.innerPathsMap.containsKey(nextMethods.get(callUnit))){
								conPaths = analyzeInterPaths(nextMethods.get(callUnit));
								Database.interPathsMap.put(nextMethods.get(callUnit), conPaths);
							}
							else break;
							
							for(UnitPath conPath : conPaths) {
								UnitPath newPath = connectPath(nowPath,conPath,callUnit);
								if(!analyzedPaths.contains(newPath))
									workPaths.add(newPath);
							}
							break;
						}
					}
				}
				if(callUnitNum == 0) {
					interPaths.add(nowPath);
				}
			}
		}
		return interPaths;
	}
	
	private static UnitPath connectPath(UnitPath callPath, UnitPath calledPath, Unit callUnit) {
		UnitPath newPath = new UnitPath(callPath);
		if(newPath.path.contains(callUnit)) {
			int index = newPath.path.indexOf(callUnit);
			newPath.path.addAll(index+1, calledPath.path);
		}
		else newPath.path.addAll(calledPath.path);
		
		newPath.conds.addAll(calledPath.conds);
		newPath.decls.addAll(calledPath.decls);
		return newPath;
	}
}
