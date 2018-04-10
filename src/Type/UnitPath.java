package Type;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import soot.Unit;

public class UnitPath {
	public List<Unit> unitPath = new LinkedList<Unit>();
	public Set<String> conds = new LinkedHashSet<String>();
	
	public UnitPath() {
	}
	
	public UnitPath(Unit u) {
		unitPath.add(u);
	}
	
	public UnitPath(List<Unit> path, Set<String> currConds) {
		unitPath.addAll(path);
		conds.addAll(currConds);
	}
	
	public UnitPath(UnitPath up, Unit nextUnit) {
		unitPath.addAll(up.unitPath);
		unitPath.add(nextUnit);
		conds.addAll(up.conds);
	}
	
	public UnitPath copy(UnitPath up) {
		return new UnitPath(up.unitPath,up.conds);
	}
	
	public String toUnitString() {
		String temp = "";
		for(Unit u : unitPath)
			temp = temp.concat("->").concat(u.toString()).concat("\n");
		temp = temp.concat("\n");
		return temp;
	}
	
}
