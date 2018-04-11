package Type;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import soot.Unit;

public class UnitPath {
	public List<Unit> unitPath = new LinkedList<Unit>();
	public Set<String> decls = new LinkedHashSet<String>();
	public Set<String> conds = new LinkedHashSet<String>();
	
	public UnitPath() {
	}
	
	public UnitPath(Unit u) {
		unitPath.add(u);
	}
	
	public UnitPath(List<Unit> path, Set<String> currDecls, Set<String> currConds) {
		unitPath.addAll(path);
		decls.addAll(currDecls);
		conds.addAll(currConds);
	}
	
	public UnitPath(UnitPath up, Unit nextUnit) {
		unitPath.addAll(up.unitPath);
		unitPath.add(nextUnit);
		decls.addAll(up.decls);
		conds.addAll(up.conds);
	}
	
	public UnitPath copy(UnitPath up) {
		return new UnitPath(up.unitPath,up.decls,up.conds);
	}
	
	public String toUnitString() {
		String temp = "";
		for(Unit u : unitPath)
			temp = temp.concat("->").concat(u.toString()).concat("\n");
		temp = temp.concat("\n");
		return temp;
	}
	
}
