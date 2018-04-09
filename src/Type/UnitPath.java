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
	
	public UnitPath(List<Unit> path, Set<String> currConds) {
		unitPath.addAll(path);
		conds.addAll(currConds);
	}
	
	public UnitPath copy(UnitPath up) {
		return new UnitPath(up.unitPath,up.conds);
	}
	
	public String toUnitString() {
		String temp = "";
		for(Unit u : unitPath)
			temp = temp.concat(u.toString()).concat("->");
		return temp;
	}
	
}
