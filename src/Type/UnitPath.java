package Type;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import soot.Unit;

public class UnitPath {
	public List<Unit> unitPath = new LinkedList<Unit>();
	public Set<String> conds = new LinkedHashSet<String>();
	public Set<String> decls = new LinkedHashSet<String>();
	
	public UnitPath() {
	}
	
	public UnitPath(List<Unit> path, Set<String> currConds, Set<String> currDecls) {
		unitPath = path;
		conds = currConds;
		decls = currDecls;
	}
	
	public UnitPath copy(UnitPath up) {
		return new UnitPath(up.unitPath,up.conds,up.decls);
	}
	
	public String toUnitString() {
		String temp = "";
		for(Unit u : unitPath) {
			temp.concat(u.toString());
			temp.concat("->");
		}
		return temp;
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
