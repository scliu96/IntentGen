package SSE;

import java.util.List;

import IF.Init;
import Type.UnitPath;
import soot.ByteType;
import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.jimple.internal.AbstractJimpleIntBinopExpr;
import soot.toolkits.scalar.SimpleLocalDefs;

public class ValueFind {
	
	private final static void findOriginalVal(SootMethod method, UnitPath currPath, SimpleLocalDefs methodDefs, Unit potentialCmpUnit, Value cmpOp) {
		if(cmpOp instanceof Local) {
			Value cmpVal = cmpOp;
		}
		//else if
	}
	
	private final static void findOriginValFromCmpVal(SootMethod method, UnitPath currPath, SimpleLocalDefs methodDefs, Unit potentialCmpUnit, Value cmpOp) {
		
	}
	
	protected final static void findLeftAndRightValuesOfByteVal(SootMethod method, UnitPath currPath, SimpleLocalDefs methodDefs, Unit currUnit, Value value) {
		if(value instanceof Local) {
			Local local = (Local) value;
			if(local.getType() instanceof ByteType) {
				List<Unit> potentialCmpUnits = methodDefs.getDefsOfAt(local, currUnit);
				for(Unit potentialCmpUnit : potentialCmpUnits)
					if(StmtHandle.isDefInPathAndLatest(currPath, methodDefs, currUnit, local, potentialCmpUnit))
						if(potentialCmpUnit.toString().contains("cmp")) {
							Init.logger.trace("Found potential cmp* statement: " +potentialCmpUnit.toString());
							if(potentialCmpUnit instanceof DefinitionStmt) {
								DefinitionStmt defStmt = (DefinitionStmt) potentialCmpUnit;
								Value rightOp = defStmt.getRightOp();
								if(rightOp instanceof AbstractJimpleIntBinopExpr) {
									AbstractJimpleIntBinopExpr cmpExpr = (AbstractJimpleIntBinopExpr) rightOp;
									findOriginalVal(method,currPath,methodDefs,potentialCmpUnit,cmpExpr.getOp1());
									findOriginalVal(method,currPath,methodDefs,potentialCmpUnit,cmpExpr.getOp2());
								}
							}
						}
			}
		}
	}
}
