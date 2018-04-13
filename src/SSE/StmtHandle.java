package SSE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import IF.Init;
import Type.UnitPath;
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

public class StmtHandle {
	
	protected final static void handleIfStmt(SootMethod method, UnitPath path, SimpleLocalDefs methodDefs, IfStmt currIfStmt) {
		Init.logger.trace("Perform path sensitive analysis for IfStmt: " + currIfStmt.toString());
		ConditionExpr condition = (ConditionExpr) currIfStmt.getCondition();
		Value opVal1 = condition.getOp1();
		Value opVal2 = condition.getOp2();
		boolean generateCondExpr = true;
		if(opVal1.getType() instanceof ByteType) {
			Init.logger.trace("opVal1.getType() instanceof ByteType");
		}
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
            String extraType = null;
            if(arg2 != null)
            		extraType = arg2.getType().toString();
            else extraType = ie.getMethod().getReturnType().toString();
            String arg2Str = "unknown";
            if(arg2 != null)
            		arg2Str = arg2.toString();
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
                					String intentSymbol = SymbolGenerate.createSymbol(intentLocal,method,intentDefUnit);
        							
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
                            					String intentSymbol = SymbolGenerate.createSymbol(intentLocal,method,intentDefUnit);
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
