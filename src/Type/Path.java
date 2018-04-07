package Type;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.Edge;

public class Path {
	
	public SootMethod entryMethod = null;
	public List<SootMethod> methodPath = new LinkedList<SootMethod>();
	public List<Stmt> stmtCall = new LinkedList<Stmt>();
	
	public List<Unit> unitPath = new LinkedList<Unit>();
	public Set<String> conds = new LinkedHashSet<String>();
	public Set<String> decls = new LinkedHashSet<String>();
	public Intent intent = new Intent();
	
	public Path() {
	}
	
	public Path(SootMethod m) {
		entryMethod = m;
	}
	
	public Path copy() {
		Path newPath = new Path();
		newPath.entryMethod = entryMethod;
		newPath.methodPath = methodPath;
		newPath.stmtCall = stmtCall;
		newPath.unitPath = unitPath;
		newPath.conds = conds;
		newPath.decls = decls;
		newPath.intent = intent;
		return newPath;
	}
	
	public String toUnitString() {
		String temp = "";
		for(Unit u : unitPath) {
			temp.concat(u.toString());
			temp.concat("->");
		}
		return temp;
	}
	
	public boolean addMethod(Edge e) {
		SootMethod nextMethod = (SootMethod) e.getTgt();
		Stmt st = e.srcStmt();
		if(methodPath.contains(nextMethod))
			return false;
		methodPath.add(nextMethod);
		stmtCall.add(st);
		return true;
	}
	
	public boolean containUnit(Unit u) {
		if(unitPath.contains(u))
			return true;
		else return false;
	}
	
	public boolean addUnit(Unit u) {
		if(containUnit(u))
			return false;
		else{
			unitPath.add(u);
			return true;
		}
	}
}
