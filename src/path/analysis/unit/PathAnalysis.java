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
			
			//boolean hitPathsLimit = false;
			Set<UnitPath> finalPaths = new LinkedHashSet<UnitPath>();
			while(!workUnits.isEmpty()) {
				if(workPaths.size() != workUnits.size())
					throw new Exception("workUnits size is different from workPaths size");
				
				Unit currUnit = workUnits.pop();
				UnitPath currPath = workPaths.pop();
				if(ug.getSuccsOf(currUnit).isEmpty()) {
					Init.logger.trace("A final path :" + currPath.toUnitString());
					//System.out.println(currPath.toUnitString());
					if(finalPaths.size() < Init.finalPathsLimit)
						finalPaths.add(currPath);
					//else hitPathsLimit = true;
				}
				
				for(Unit succUnit : ug.getSuccsOf(currUnit)) {
					if(currPath.path.contains(succUnit)){
						Init.logger.trace("Loop detected while analyze method : "+ method.getName());
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
			// find all path in one method
			analyzeUnitPathInMethod(methodPoint);
		}
		
		for(SootMethod m : Database.entryPoints) {
			MethodPoint mp = Database.methodPointsMap.get(m);
			Set<UnitPath> workPaths = new LinkedHashSet<UnitPath>();
			for(UnitPath path : mp.unitPaths) {
				workPaths.add(path);
			}
			for(UnitPath path : mp.unitPaths) {
				UnitPath finalPath = new UnitPath(path);
				int size = path.path.size();
				int connectSize = 0;
				for(int i=0;i<size+connectSize;i++) {
					Unit callUnit = path.path.get(i);
					if(mp.nextMethods.containsKey(callUnit))
						 = connectPath(finalPath,callUnit,mp.nextMethods.get(callUnit));
				}
			}
		}
	}
	
	private static Set<UnitPath> connectPath(UnitPath finalPath, Unit callUnit, SootMethod targetMethod) {
		
		/*int index = -1;
		for(Unit unit : finalPath.path)
			if(unit.equals(callUnit))
				index = finalPath.path.indexOf(unit);
		*/
		//Set<UnitPath> tempPaths = new LinkedHashSet<UnitPath>();
		MethodPoint mp = Database.methodPointsMap.get(targetMethod);
		for(UnitPath path : mp.unitPaths) {
			Map<Unit,Unit> callUnits = new LinkedHashMap<Unit,Unit>();
			for(Unit unit : path.path)
				if(mp.nextMethods.containsKey(unit))
					callUnits.put(unit, path.path.get(path.path.indexOf(unit)));
			List<UnitPath> workPaths = new LinkedList<UnitPath>();
			workPaths.add(path);
			for(Unit unit : callUnits.keySet()) {
				while(!workPaths.isEmpty()) {
					UnitPath nowPath = workPaths.get(0);
					workPaths.remove(nowPath);
					if(nowPath.path.contains(unit))
						if(!nowPath.path.get(nowPath.path.indexOf(unit)).equals(callUnits.get(unit))) {
							connectPath(nowPath,unit,mp.nextMethods.get(unit));
							workPaths.addAll();
						}
				}
			}
		}
			
	}
	
	private static void analyzeUnitPathInMethod(MethodPoint methodPoint) {
		System.out.println(methodPoint.entryMethod);
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
			System.out.print(currPath.toUnitString());
			//currPath.print();
			System.out.println("");
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
}
