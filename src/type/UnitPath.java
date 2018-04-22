package type;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import soot.Unit;

public class UnitPath {
	public List<Unit> path = new LinkedList<Unit>();
	public Set<String> decls = new LinkedHashSet<String>();
	public Set<String> conds = new LinkedHashSet<String>();
	
	public UnitPath() {
	}
	
	public UnitPath(Unit u) {
		path.add(u);
	}
	
	public UnitPath(List<Unit> path, Set<String> currDecls, Set<String> currConds) {
		path.addAll(path);
		decls.addAll(currDecls);
		conds.addAll(currConds);
	}
	
	public UnitPath(UnitPath up, Unit nextUnit) {
		path.addAll(up.path);
		path.add(nextUnit);
		decls.addAll(up.decls);
		conds.addAll(up.conds);
	}
	
	public UnitPath copy(UnitPath up) {
		return new UnitPath(up.path,up.decls,up.conds);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		UnitPath unitPath = (UnitPath) o;

		if (!conds.equals(unitPath.conds)) return false;
		if (!decls.equals(unitPath.decls)) return false;
		return path.equals(unitPath.path);

	}
	
	public String toUnitString() {
		String temp = "";
		for(Unit u : path)
			temp = temp.concat("->").concat(u.toString()).concat("\n");
		temp = temp.concat("\n");
		return temp;
	}
	
}
