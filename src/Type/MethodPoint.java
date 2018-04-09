package Type;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.callgraph.Edge;

public class MethodPoint {
	
	public SootMethod entryMethod = null;
	public Map<Unit,SootMethod> nextMethods = new LinkedHashMap<Unit,SootMethod>();
	
	public MethodPoint() {
	}
	
	public MethodPoint(SootMethod m) {
		entryMethod = m;
	}
	
	public MethodPoint(SootMethod m, Map<Unit,SootMethod> nextMethod) {
		entryMethod = m;
		nextMethods.putAll(nextMethod);
	}
	
	public MethodPoint copy(MethodPoint mp) {
		return new MethodPoint(mp.entryMethod, mp.nextMethods);
	}
	
	public boolean equals(MethodPoint p) {
		if(p.entryMethod.equals(entryMethod))
			return true;
		else return false;
	}
	
	public boolean addMethods(Edge e) {
		SootMethod nextMethod = (SootMethod) e.getTgt();
		Unit u = e.srcUnit();
		if(nextMethods.containsKey(u))
			return false;
		nextMethods.put(u, nextMethod);
		return true;
	}
	
	public String toMethodString() {
		String temp = "";
		temp = temp.concat(entryMethod.toString()).concat("\n");
		
		Set<Unit> UnitSet = nextMethods.keySet();
		for(Unit u : UnitSet)
			temp = temp.concat(u.toString()).concat("->").concat(nextMethods.get(u).toString()).concat("\n");
		return temp;
	}
}
