package Type;

import java.util.LinkedList;
import java.util.List;

import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.Edge;

public class Path {
	private SootMethod entryMethod = null;
	private List<SootMethod> methodPath = new LinkedList<SootMethod>();
	private List<Stmt> stmtCall = new LinkedList<Stmt>();
	
	private List<Unit> unitPath = new LinkedList<Unit>();
	private Intent intent = new Intent();
	
	public Path() {
		super();
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
		newPath.intent = intent;
		return newPath;
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
	
	public void generateIntent() {
		
	}
}
