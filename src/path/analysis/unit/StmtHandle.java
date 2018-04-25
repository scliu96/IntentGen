package path.analysis.unit;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;

import global.Database;
import global.Init;
import path.analysis.solver.Solve;
import soot.BooleanType;
import soot.ByteType;
import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.ConditionExpr;
import soot.jimple.DefinitionStmt;
import soot.jimple.IfStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.ParameterRef;
import soot.jimple.StringConstant;
import soot.toolkits.scalar.SimpleLocalDefs;
import type.UnitPath;

public class StmtHandle {
	
	protected final static void handleIfStmt(SootMethod method, UnitPath path, SimpleLocalDefs methodDefs, IfStmt currIfStmt) {
		Init.logger.trace("Perform path sensitive analysis for IfStmt: " + currIfStmt.toString());
		ConditionExpr condition = (ConditionExpr) currIfStmt.getCondition();
		Value opVal1 = condition.getOp1();
		Value opVal2 = condition.getOp2();
		Value opVal1Org = opVal1;
		Value opVal2Org = opVal2;
		Unit opVal1DefUnit = null;
		Unit opVal2DefUnit = null;
		boolean generateCondExpr = true;
		
		if(opVal1.getType() instanceof ByteType) {
			Init.logger.trace("opVal1.getType() instanceof ByteType");
			Pair<Pair<Value,Unit>,Pair<Value,Unit>> valueDefPair = ValueFind.findValuesOfByteType(method, path, methodDefs, currIfStmt, opVal1);
			Pair<Value,Unit> left = valueDefPair.getValue0();
			Pair<Value,Unit> right = valueDefPair.getValue1();
			opVal1 = left.getValue0();
			opVal2 = right.getValue0();
			opVal1DefUnit = left.getValue1();
			opVal2DefUnit = right.getValue1();
		}
		else if(opVal1.getType() instanceof BooleanType) {
			Init.logger.trace("opVal1.getType() instanceof BooleanType");
			Pair<Pair<Value,Unit>,Pair<Value,Unit>> valueDefPair = ValueFind.findValuesOfBoolType(method, path, methodDefs, currIfStmt, opVal1);
			Pair<Value,Unit> left = valueDefPair.getValue0();
			Pair<Value,Unit> right = valueDefPair.getValue1();
		
			if(left == null) {
				valueDefPair = ValueFind.findValuesOfBundleType(method, path, methodDefs, currIfStmt, opVal1);
				left = valueDefPair.getValue0();
				right = valueDefPair.getValue1();
				if(left != null || right != null)
					generateCondExpr = false;
				if(left != null) {
					opVal1 = left.getValue0();
					opVal1DefUnit = left.getValue1();
				}
				if(right != null) {
					opVal2 = right.getValue0();
					opVal2DefUnit = right.getValue1();
				}
			}
			
			if(left != null && right != null) {
				if(left.getValue0() == null && right.getValue0() == null)
					ValueFind.findKeysForLRValues(methodDefs,path,currIfStmt,opVal1,opVal2);
				else {
					opVal1 = left.getValue0();
					opVal1DefUnit = left.getValue1();
					opVal2 = right.getValue0();
					opVal2DefUnit = right.getValue1();
				}
			}
			
			if(left == null && right == null) {
				valueDefPair = ValueFind.findCategories(method, path, methodDefs, currIfStmt, opVal1);
				left = valueDefPair.getValue0();
				right = valueDefPair.getValue1();
				if(left != null || right != null)
					generateCondExpr = false;
				if(left != null) {
					opVal1 = left.getValue0();
					opVal1DefUnit = left.getValue1();
				}
				if(right != null) {
					opVal2 = right.getValue0();
					opVal2DefUnit = right.getValue1();
				}
			}
		}
		else {
			Init.logger.trace("else branch, simply invoking findKeysForLeftAndRightValues(...)");
			ValueFind.findKeysForLRValues(methodDefs, path, currIfStmt, opVal1, opVal2);
			opVal1DefUnit = ValueFind.getDefOfValInPath(methodDefs, path, currIfStmt, opVal1);
			opVal2DefUnit = ValueFind.getDefOfValInPath(methodDefs, path, currIfStmt, opVal2);
		}
		
		String opExpr1 = null;
		String opExpr2 = null;
		try {
			if (opVal1 == null) {
				Init.logger.debug("Could not resolve opVal1, so setting it to true");
				opExpr1 = "";
			} else opExpr1 = SymbolGenerate.createZ3Expr(method,path,currIfStmt,opVal1,opVal1DefUnit);
			
			if (opVal2 == null) {
				Init.logger.debug("Could not resolve opVal2, so setting it to true");
				opExpr2 = "";
			} else opExpr2 = SymbolGenerate.createZ3Expr(method,path,currIfStmt,opVal2,opVal2DefUnit);
		} catch (RuntimeException e) {
			Init.logger.warn("caught exception: ", e);
			return;
		}
		
		Unit succUnit = null;
		int index = path.path.indexOf(currIfStmt) + 1;
		if(index < path.path.size())
			succUnit = path.path.get(index);
		
		boolean isFallThrough = ValueFind.isFallThrough(method.getActiveBody(), currIfStmt, succUnit);
		String branchSensitiveSymbol = null;
		if(isFallThrough) {
			if(opVal1Org.getType() instanceof BooleanType)
				branchSensitiveSymbol = condition.getSymbol();
			else branchSensitiveSymbol = SymbolGenerate.negateSymbol(condition.getSymbol());
		}
		else {
			if(opVal1Org.getType() instanceof BooleanType)
				branchSensitiveSymbol = SymbolGenerate.negateSymbol(condition.getSymbol());
			else branchSensitiveSymbol = condition.getSymbol();
		}
		
		if(generateCondExpr)
			path.conds.add(SymbolGenerate.buildZ3CondExpr(opExpr1,opExpr2,branchSensitiveSymbol));
	}	
	
	protected final static void handleIntentGetExtraStmt(SootMethod method, UnitPath path, SimpleLocalDefs methodDefs, DefinitionStmt currDefStmt) {
		if (currDefStmt.containsInvokeExpr() && currDefStmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
            InstanceInvokeExpr ie = (InstanceInvokeExpr) currDefStmt.getInvokeExpr();
            int IntentOrBundle = 0; // intent is 1, bundle is 2
            if (Pattern.matches("get.*Extra", ie.getMethod().getName()) && ie.getMethod().getDeclaringClass().toString().equals("android.content.Intent"))
                IntentOrBundle = 1;
            else if (Pattern.matches("get.*", ie.getMethod().getName()) && ie.getMethod().getDeclaringClass().toString().equals("android.os.Bundle"))
            		IntentOrBundle = 2;
            else return;
            
            Init.logger.trace("Perform path sensitive analysis for getExtra: " + currDefStmt.toString());
            Value arg1 = ie.getArg(0);
            Value arg2 = null;
            if(ie.getArgCount()>1)
            		arg2 = ie.getArg(1);
            //String extraType = null;
            //if(arg2 != null)
            //		extraType = arg2.getType().toString();
            //else extraType = ie.getMethod().getReturnType().toString();
            //String arg2Str = "unknown";
            //if(arg2 != null)
            //		arg2Str = arg2.toString();
            if(arg1 instanceof StringConstant) {
            		StringConstant keyStrConst = (StringConstant) arg1;
            		if(ie.getBase() instanceof Local) {
            			if(IntentOrBundle == 1) {
            				Local intentLocal = (Local) ie.getBase();
            				for(Unit intentDefUnit : methodDefs.getDefsOfAt(intentLocal, currDefStmt)) {
                				if(!isDefInPathAndLatest(path, methodDefs, currDefStmt, intentLocal, intentDefUnit))
                					continue;
                				if(currDefStmt.getLeftOp() instanceof Local) {
                					Local extraLocal = (Local) currDefStmt.getLeftOp();
                					String extraLocalSymbol = SymbolGenerate.createSymbol(extraLocal, method, currDefStmt);
                					Database.symbolLocalMap.put(extraLocalSymbol, extraLocal);
                					String intentSymbol = SymbolGenerate.createSymbol(intentLocal,method,intentDefUnit);
                					Database.symbolLocalMap.put(intentSymbol, intentLocal);
                					String newExtraType = SymbolGenerate.getZ3Type(extraLocal.getType());
								String newIntentType = SymbolGenerate.getZ3Type(intentLocal.getType());
								path.decls.add("(declare-const " + extraLocalSymbol + " " + newExtraType + " )");
								path.decls.add("(declare-const " + intentSymbol + " " + newIntentType + " )");
								path.conds.add("(assert (= (containsKey " + extraLocalSymbol + " \"" + keyStrConst.value + "\") true))");
								path.conds.add("(assert (= (fromIntent " + extraLocalSymbol + ") " + intentSymbol + "))");
								buildParamRefExpressions(method, path, intentDefUnit, intentSymbol);
                				}
                			}
            			}
            			else {
            				Local bundleLocal = (Local) ie.getBase();
            				for(Unit bundleDefUnit : methodDefs.getDefsOfAt(bundleLocal, currDefStmt)) {
            					if(!isDefInPathAndLatest(path, methodDefs, currDefStmt, bundleLocal, bundleDefUnit))
                					continue;
            					
            					DefinitionStmt bundleDefStmt = (DefinitionStmt)bundleDefUnit;
            					if(bundleDefStmt.containsInvokeExpr() && bundleDefStmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
            						InstanceInvokeExpr iie = (InstanceInvokeExpr) bundleDefStmt.getInvokeExpr();
            						if(iie.getBase().getType().toString().equals("android.content.Intent") && iie.getBase() instanceof Local) {
            							Local intentLocal = (Local) iie.getBase();
            							for(Unit intentDefUnit : methodDefs.getDefsOfAt(intentLocal, bundleDefStmt)) {
                            				if(!isDefInPathAndLatest(path, methodDefs, bundleDefStmt, intentLocal, intentDefUnit))
                            					continue;
                            				
                            				if(currDefStmt.getLeftOp() instanceof Local) {
                            					Local extraLocal = (Local) currDefStmt.getLeftOp();
                            					String extraLocalSymbol = SymbolGenerate.createSymbol(extraLocal, method, currDefStmt);
                            					Database.symbolLocalMap.put(extraLocalSymbol, extraLocal);
                            					String intentSymbol = SymbolGenerate.createSymbol(intentLocal,method,intentDefUnit);
                            					Database.symbolLocalMap.put(intentSymbol, intentLocal);
                            					String newExtraType = SymbolGenerate.getZ3Type(extraLocal.getType());
                    							String newIntentType = SymbolGenerate.getZ3Type(intentLocal.getType());
                    							path.decls.add("(declare-const " + extraLocalSymbol + " " + newExtraType + " )");
                    							path.decls.add("(declare-const " + intentSymbol + " " + newIntentType + " )");
                    							path.conds.add("(assert (= (containsKey " + extraLocalSymbol + " \"" + keyStrConst.value + "\") true))");
                    							path.conds.add("(assert (= (fromIntent " + extraLocalSymbol + ") " + intentSymbol + "))");
                    							buildParamRefExpressions(method, path, intentDefUnit, intentSymbol);
                            				}
                            			}
            						}
            					}
            				}
            			}
            		}
            }
        }
	}
	
	protected final static void handleIntentGetActionStmt(SootMethod method, UnitPath path, SimpleLocalDefs methodDefs, DefinitionStmt currDefStmt) {
		InvokeExpr ie = currDefStmt.getInvokeExpr();
		if (ie.getMethod().getName().equals("getAction")) {
            if (ie.getMethod().getDeclaringClass().getName().equals("android.content.Intent")) {
                Init.logger.trace("Perform path sensitive analysis for getAction: "+ currDefStmt.toString());
                if (ie instanceof InstanceInvokeExpr) {
                    InstanceInvokeExpr iie = (InstanceInvokeExpr)ie;
                    String actionRefSymbol = null;
                    if (currDefStmt.getLeftOp() instanceof Local) {
                        Local leftLocal = (Local)currDefStmt.getLeftOp();
                        actionRefSymbol = SymbolGenerate.createSymbol(leftLocal, method, currDefStmt);
						if (iie.getBase() instanceof Local) {
							Local intentLocal = (Local) iie.getBase();
							for (Unit intentDefUnit : methodDefs.getDefsOfAt(intentLocal, currDefStmt)) {
								if (!isDefInPathAndLatest(path, methodDefs, currDefStmt, intentLocal, intentDefUnit))
									continue;
								if (path.path.contains(intentDefUnit)) {
									String intentSymbol = SymbolGenerate.createSymbol(intentLocal, method, intentDefUnit);
									Database.symbolLocalMap.put(intentSymbol, intentLocal);
									String intentDecl = "(declare-const " + intentSymbol + " Object )";
									String actionRefDecl = "(declare-const " + actionRefSymbol + " String )";
									path.decls.add(intentDecl);
									path.decls.add(actionRefDecl);
									
									String getActionAssert = "(assert (= (getAction " + intentSymbol + ") " + actionRefSymbol + "))";
									String newFromIntent = "(assert (= (fromIntent " + actionRefSymbol + ") " + intentSymbol + "))";
									path.conds.add(getActionAssert);
									path.conds.add(newFromIntent);
									
									buildParamRefExpressions(method, path, intentDefUnit, intentSymbol);
								}
							}
						}
					}
				}
            }
        }
	}
	
	protected static boolean isDefInPathAndLatest(UnitPath path, SimpleLocalDefs methodDefs, Unit currUnit, Local usedLocal, Unit usedDefUnit) {
		if (path.path.contains(usedDefUnit)) { // does path contain definition
			for (Unit otherDefUnit : methodDefs.getDefsOfAt(usedLocal,currUnit)) { // check other defs of usedLocal at usedUnit to determine
				if (usedDefUnit.equals(otherDefUnit)) // continue if usedDefUnit equals otherDef
					continue;
				if (!path.path.contains(otherDefUnit)) // if the otherDef is not in path, then continue
					continue;
				List<Unit> pathList = new ArrayList<Unit>(path.path);
				int usedDefPos = pathList.indexOf(usedDefUnit);
				int otherDefPos = pathList.indexOf(otherDefUnit);
				if (usedDefPos < otherDefPos)  // if inDef's position in the path is earlier then otherDef's position, then inDef is not the latest definition in the path, so return false
					return false;
			}
			return true; // inDef is in the path and is the latest definition along that path
		}
		else return false;
	}
	
	private static void buildParamRefExpressions(SootMethod method, UnitPath currPath, Unit intentDefUnit, String intentSymbol) {
		if (intentDefUnit instanceof DefinitionStmt) {
            DefinitionStmt defStmt = (DefinitionStmt) intentDefUnit;
            if (!currPath.path.contains(defStmt)) {
				return;
            }
            if (defStmt.getRightOp() instanceof ParameterRef) {
                ParameterRef pr = (ParameterRef) defStmt.getRightOp();
                String prSymbol = SymbolGenerate.createParamRefSymbol(defStmt.getLeftOp(), pr.getIndex(), method, defStmt);

                currPath.decls.add("(declare-const " + prSymbol + " ParamRef)");
                currPath.conds.add("(assert ( = (index " + prSymbol + ") " + pr.getIndex() + "))");
                currPath.conds.add("(assert ( = (type " + prSymbol + ") \"" + pr.getType() + "\"))");
                currPath.conds.add("(assert ( = (method " + prSymbol + ") \"" + method.getDeclaringClass().getName() + "." + method.getName() + "\"))");
                currPath.conds.add("(assert (= (hasParamRef " + intentSymbol + ") " + prSymbol + "))");
            }
        }
	}
	
}
