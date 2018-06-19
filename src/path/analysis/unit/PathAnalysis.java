package path.analysis.unit;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import global.Init;
import path.analysis.solver.Solve;
import global.Database;
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
import type.MethodPoint;
import type.UnitPath;

public class PathAnalysis {
	
	public static void analysis() throws Exception {
		for(MethodPoint methodPoint : Database.methodPointsMap.values()) {
			SootMethod method = methodPoint.entryMethod;
			Body b = method.getActiveBody();
			PatchingChain<Unit> units = b.getUnits();
			BriefUnitGraph ug = new BriefUnitGraph(b);
			
			if(units.isEmpty())
				continue;
			Unit startingUnit = units.getFirst();
			Stack<Unit> workUnits = new Stack<Unit>();
			workUnits.push(startingUnit);
			Stack<UnitPath> workPaths = new Stack<UnitPath>();
			UnitPath newUp = new UnitPath(startingUnit);
			workPaths.push(newUp);
			Set<UnitPath> finalPaths = new LinkedHashSet<UnitPath>();
			while(!workUnits.isEmpty()) {
				if(workPaths.size() != workUnits.size())
					throw new Exception("workUnits size is different from workPaths size");
				
				Unit currUnit = workUnits.pop();
				UnitPath currPath = workPaths.pop();
				if(ug.getSuccsOf(currUnit).isEmpty()) {
					Init.logger.trace("A final path :" + currPath.toUnitString());
					if(finalPaths.size() < Init.finalPathsLimit)
						finalPaths.add(currPath);
				}
				
				for(Unit succUnit : ug.getSuccsOf(currUnit)) {
					if(currPath.path.contains(succUnit)){
						Init.logger.trace("Loop detected while analyze method : "+ method.getName());
						if(currPath.path.indexOf(succUnit) == currPath.path.size()-1)
							continue;
						workUnits.push(succUnit);
						workPaths.push(currPath);
						continue;
					}
					Init.logger.trace("Fork the following path on unit " + succUnit);
					UnitPath newPath = new UnitPath(currPath,succUnit);
					workUnits.push(succUnit);
					workPaths.push(newPath);
					Init.logger.trace("WorkingUnits size now is " + workUnits.size());
				}
			}
			methodPoint.unitPaths = finalPaths;
			analyzeUnitPathInMethod(methodPoint);
		}
		
		int totalInnerMethodPath = 0;
        for(MethodPoint mp :Database.methodPointsMap.values())
        		totalInnerMethodPath += mp.unitPaths.size();
        Init.tempOut += "Analyze " + Database.methodPointsMap.size() + " methods\n";
        Init.tempOut += "Find " + totalInnerMethodPath +" inner-method paths\n";
        Init.tempOut += "Find " + (float)totalInnerMethodPath/(float)Database.methodPointsMap.size() + " inner-method paths in average in one method\n";
        Init.printSystemTime();
        
		int totalInterMethodPath = 0;
		for(SootMethod m : Database.entryPoints) {
			Set<UnitPath> ups = findFinalPaths(m);
			totalInterMethodPath += ups.size();
			Database.methodPathsMap.put(m, ups);
		}
		Init.tempOut += "Find " + totalInterMethodPath + " inter-method paths\n";
		Init.printSystemTime();
		
		for(SootMethod m : Database.entryPoints)
			for(UnitPath currPath : Database.methodPathsMap.get(m))
				Solve.runSolvingPhase(m, currPath);
		Init.tempOut += "Find " + Database.finalPathsMap.size() + " feasible paths\n";
		Init.printSystemTime();
	}
	
	private static void analyzeUnitPathInMethod(MethodPoint methodPoint) {
		SootMethod method = methodPoint.entryMethod;
		Set<UnitPath> unitPaths = methodPoint.unitPaths;
		for(UnitPath currPath : unitPaths) {
			for(int i = 0; i<currPath.path.size(); i++) {
				Unit currUnitInPath = currPath.path.get(i);
				if(!unitNeedsAnalysis(currUnitInPath))
					continue;
				UnitGraph unitGraph = null;
				SimpleLocalDefs defs = null;
				if(method.hasActiveBody()) {
					unitGraph = new ExceptionalUnitGraph(method.getActiveBody());
					defs = new SimpleLocalDefs(unitGraph);
				}
				else Init.logger.warn("method " + method.getName() + " has no active body");
				
				try {
					Stmt currStmtInPath = (Stmt) currUnitInPath;
					if(currStmtInPath instanceof IfStmt)
						StmtHandle.handleIfStmt(method, currPath, defs, (IfStmt) currStmtInPath);
					else if(currStmtInPath.containsInvokeExpr() && currStmtInPath instanceof DefinitionStmt) {
						StmtHandle.handleIntentGetExtraStmt(method, currPath, defs, (DefinitionStmt) currStmtInPath);
						StmtHandle.handleIntentGetActionStmt(method, currPath, defs, (DefinitionStmt) currStmtInPath);
					}
					else Init.logger.warn("Not including condition for " + currUnitInPath + " to path constraint");
				}catch (NullPointerException e) {
	                e.printStackTrace();
	            }
			}
		}
	}
	
	private static boolean unitNeedsAnalysis(Unit unit) {
		if (unit instanceof InvokeStmt) {
			InvokeStmt stmt = (InvokeStmt) unit;
			if ( stmt.getInvokeExpr().getMethod().getName().equals("d") )
				return false;
		}
		return true;
	}
	
	private static Set<UnitPath> findFinalPaths(SootMethod targetMethod) {
		MethodPoint mp = Database.methodPointsMap.get(targetMethod);
		Set<UnitPath> finalPaths = new LinkedHashSet<UnitPath>();
		for(UnitPath path : mp.unitPaths) {
			//System.out.println(path.toUnitString());
			Map<Unit,Unit> callUnits = new LinkedHashMap<Unit,Unit>();
			for(Unit unit : path.path)
				if(mp.nextMethods.containsKey(unit))
					callUnits.put(unit, path.path.get(path.path.indexOf(unit)+1));
			List<UnitPath> workPaths = new LinkedList<UnitPath>();
			List<UnitPath> analyzedPaths = new LinkedList<UnitPath>();
			workPaths.add(path);
			while(!workPaths.isEmpty()) {
				UnitPath nowPath = workPaths.get(0);
				//System.out.println(nowPath.toUnitString());
				workPaths.remove(nowPath);
				analyzedPaths.add(nowPath);
				int callUnitNum = 0;
				for(Unit callUnit : callUnits.keySet()) {
					if(nowPath.path.contains(callUnit)) {
						if(nowPath.path.get(nowPath.path.indexOf(callUnit)+1).equals(callUnits.get(callUnit))) {
							callUnitNum++;
							Set<UnitPath> conPaths = null;
							if(Database.methodPathsMap.containsKey(mp.nextMethods.get(callUnit)))
								conPaths = Database.methodPathsMap.get(mp.nextMethods.get(callUnit));
							else if(Database.methodPointsMap.containsKey(mp.nextMethods.get(callUnit))){
								conPaths = findFinalPaths(mp.nextMethods.get(callUnit));
								Database.methodPathsMap.put(mp.nextMethods.get(callUnit), conPaths);
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
					//System.out.println(nowPath.toUnitString());
					finalPaths.add(nowPath);
				}
			}
		}
		return finalPaths;
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
