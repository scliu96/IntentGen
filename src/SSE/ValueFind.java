package SSE;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.javatuples.Pair;
import org.javatuples.Quartet;

import IF.Init;
import Type.UnitPath;
import soot.Body;
import soot.BooleanType;
import soot.ByteType;
import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.Constant;
import soot.jimple.DefinitionStmt;
import soot.jimple.IfStmt;
import soot.jimple.ParameterRef;
import soot.jimple.StringConstant;
import soot.jimple.internal.AbstractJimpleIntBinopExpr;
import soot.jimple.internal.JCastExpr;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.SimpleLocalDefs;

public class ValueFind {
	
	protected final static Pair<Pair<Value,Unit>,Pair<Value,Unit>> findValuesOfByteType(SootMethod method, UnitPath currPath, SimpleLocalDefs methodDefs, Unit currUnit, Value value) {
		Pair<Value,Unit> leftVal = null;
		Pair<Value,Unit> rightVal = null;
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
									leftVal = findOriginalVal(method,currPath,methodDefs,potentialCmpUnit,cmpExpr.getOp1());
									rightVal = findOriginalVal(method,currPath,methodDefs,potentialCmpUnit,cmpExpr.getOp2());
								}
							}
						}
			}
		}
		return new Pair<Pair<Value,Unit>,Pair<Value,Unit>>(leftVal,rightVal);
	}
	
	public final static Pair<Pair<Value,Unit>,Pair<Value,Unit>> findValuesOfBoolType(SootMethod method, UnitPath currPath, SimpleLocalDefs methodDefs, Unit currUnit, Value value){
		Pair<Value,Unit> leftVal = null;
		Pair<Value,Unit> rightVal = null;
		if(value instanceof Local) {
			Local local = (Local) value;
			if(local.getType() instanceof BooleanType) {
				List<Unit> potentialStringEqualsUnits = methodDefs.getDefsOfAt(local,currUnit);
				for(Unit pseUnit: potentialStringEqualsUnits)
					if(StmtHandle.isDefInPathAndLatest(currPath, methodDefs, currUnit, local, pseUnit)) {
						Init.logger.trace("Found potential string equal comparison statement: " + pseUnit);
						if(pseUnit instanceof DefinitionStmt) {
							DefinitionStmt defStmt = (DefinitionStmt) pseUnit;
							if(defStmt.getRightOp() instanceof JVirtualInvokeExpr) {
								JVirtualInvokeExpr jviExpr = (JVirtualInvokeExpr) defStmt.getRightOp();
								if (jviExpr.getMethod().getName().equals("equals") && jviExpr.getMethod().getDeclaringClass().getName().equals("java.lang.String")) {
									Init.logger.debug("Identified actual string equals comparison statement");
									leftVal = findOriginalVal(method,currPath,methodDefs,pseUnit,jviExpr.getBase());
									rightVal = findOriginalVal(method,currPath,methodDefs,pseUnit,jviExpr.getArg(0));
								}
								if (Pattern.matches("hasExtra",jviExpr.getMethod().getName())) {
									Init.logger.debug("Found hasExtra invocation");
									leftVal = findOriginalVal(method,currPath,methodDefs,pseUnit,jviExpr.getBase());
									rightVal = findOriginalVal(method,currPath,methodDefs,pseUnit,jviExpr.getArg(0));

									Body b = method.getActiveBody();
									UnitGraph ug = new BriefUnitGraph(b);

									List<Unit> currPathList = new ArrayList<Unit>(currPath.path);
									int indexOfUnit = currPathList.indexOf(currUnit);
									if (indexOfUnit == -1) 
										throw new RuntimeException(currUnit + " is not in path");
									Unit succ = currPathList.get(indexOfUnit+1);

									boolean isFallThrough = isFallThrough(ug, currUnit, succ);
									String newAssert = null;
									if (isFallThrough) { // intent contains the extra
										newAssert = "(assert (exists ((index Int)) (= (select keys index) " + rightVal.getValue0().toString() + ")))";
										//addIntentExtraForPath(currPath, rightVal.getValue0().toString(), rightVal.getValue0().getType().toString());
									} else { // intent does not contain the extra
										newAssert = "(assert (forall ((index Int)) (not(= (select keys index) " + rightVal.getValue0().toString() + "))))";
									}
									leftVal = new Quartet<Value, String, String, Unit>(jviExpr.getBase(), null, newAssert, leftVal.getValue3());
								}
							}
							}
						}
					}
			}
		}
	}

	private final static Pair<Value,Unit> findOriginalVal(SootMethod method, UnitPath currPath, SimpleLocalDefs methodDefs, Unit potentialCmpUnit, Value cmpOp) {
		Value originVal = null;
		Unit defUnit = null;
		if(cmpOp instanceof Local) {
			Value cmpVal = cmpOp;
			Pair<Value,Unit> pair = findOriginValFromCmpVal(method,currPath,methodDefs,potentialCmpUnit,cmpVal);
			originVal = pair.getValue0();
			defUnit = pair.getValue1();
		}
		else if(cmpOp instanceof Constant) {
			originVal = cmpOp;
		}
		else throw new RuntimeException("Unhandled cmpOp for:" + potentialCmpUnit);
		return new Pair<Value,Unit>(originVal,defUnit);
	}
	
	private final static Pair<Value,Unit> findOriginValFromCmpVal(SootMethod method, UnitPath currPath, SimpleLocalDefs methodDefs, Unit potentialCmpUnit, Value cmpVal) {
		Value originVal = null;
		Unit defUnit = null;
		String key = null;
		
		Local cmpLocal = (Local) cmpVal;
		List<Unit> castOrInvokeUnits = methodDefs.getDefsOfAt(cmpLocal, potentialCmpUnit);
		for(Unit coiUnit : castOrInvokeUnits)
			if(StmtHandle.isDefInPathAndLatest(currPath, methodDefs, potentialCmpUnit, cmpLocal, coiUnit)) {
				Init.logger.trace("Foune potential cast or invoke stmt: " + coiUnit);
				if(coiUnit instanceof DefinitionStmt) {
					DefinitionStmt coiStmt = (DefinitionStmt) coiUnit;
					originVal = coiStmt.getLeftOp();
					defUnit = coiUnit;
					if(!currPath.path.contains(defUnit))
						continue;
					
					if(coiStmt.getRightOp() instanceof JCastExpr) {
						Init.logger.trace("Handling cast expression from potential API invocation");
						JCastExpr expr = (JCastExpr) coiStmt.getRightOp();
						if(expr.getOp() instanceof Local) {
							Local localFromCast = (Local) expr.getOp();
							List<Unit> defsOfLocalFromCast = methodDefs.getDefsOfAt(localFromCast, coiUnit);
							for(Unit defLocalAssignFromCastUnit : defsOfLocalFromCast)
								if(StmtHandle.isDefInPathAndLatest(currPath, methodDefs, coiUnit, localFromCast, defLocalAssignFromCastUnit))
									if(defLocalAssignFromCastUnit instanceof DefinitionStmt) {
										DefinitionStmt defLocalAssignFromCastStmt = (DefinitionStmt) defLocalAssignFromCastUnit;
										originVal = defLocalAssignFromCastStmt.getLeftOp();
										defUnit = defLocalAssignFromCastUnit;
										key = extractKeyFromIntentExtra(defLocalAssignFromCastStmt,methodDefs,currPath);
									}
						}
					}
					else key = extractKeyFromIntentExtra(coiStmt,methodDefs,currPath);
					
					if(coiStmt.getRightOp() instanceof StringConstant) {
						Local local = (Local) coiStmt.getLeftOp();
						String symbol = SymbolGenerate.createSymbol(local, method, coiStmt);
						StringConstant stringConst = (StringConstant)coiStmt.getRightOp();
						currPath.conds.add("(assert (= " + symbol + " " + stringConst + " ))");
						currPath.decls.add("(declare-const " + symbol + " String )");
					}
					
					if(coiStmt.getRightOp() instanceof ParameterRef) {
						Init.logger.trace("Found parameter ref when searching for original value");
						if(coiStmt.getLeftOp() instanceof Local) {
							Local prLocal = (Local) coiStmt.getLeftOp();
							String localSymbol = SymbolGenerate.createSymbol(prLocal, method, coiStmt);
							originVal = coiStmt.getLeftOp();
							ParameterRef pr = (ParameterRef) coiStmt.getRightOp();
							String prSymbol = SymbolGenerate.createParamRefSymbol(prLocal, pr.getIndex(), method, coiStmt);
							currPath.decls.add("(declare-const " + prSymbol + " ParamRef)");
							currPath.conds.add("(assert ( = (index "+prSymbol+") "+pr.getIndex()+"))\n"+
									"(assert ( = (type "+prSymbol+") \""+pr.getType()+"\"))\n"+
									"(assert ( = (method "+prSymbol+") \""+method.getDeclaringClass().getName()+"."+method.getName()+"\"))\n"+
									"(assert (= (hasParamRef "+localSymbol+") "+prSymbol+"))");
							defUnit = coiStmt;
						}
					}
				}
			}	
		Init.valueKeyMap.put(originVal,key);
		return new Pair<Value,Unit>(originVal,defUnit);
	}
	
	private static boolean isFallThrough(UnitGraph ug, Unit inUnit, Unit succ) {
		return (succ == null && inUnit instanceof IfStmt) ? true : icfg.isFallThroughSuccessor(inUnit, succ);
	}
	
	private static String extractKeyFromIntentExtra(DefinitionStmt defStmt, SimpleLocalDefs defs, UnitPath currPath) {
		return "zzz";
	}
}
