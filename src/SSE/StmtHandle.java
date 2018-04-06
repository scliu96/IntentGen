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

import Type.Path;
import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.jimple.IfStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.ParameterRef;
import soot.jimple.StringConstant;
import soot.toolkits.scalar.SimpleLocalDefs;

public class StmtHandle {
	private static Logger logger = LogManager.getLogger(PathAnalysisOnUnit.class);
	
	/** key: the z3 expression for a Unit, value: the corresponding Unit*/
	private Map<String,Unit> exprUnitMap = new HashMap<String,Unit>();
	
	/** key: a symbol used to represent a Local, value: the Local represented by the symbol */
	private static Map<String,Local> symbolLocalMap = new HashMap<String,Local>();
	
	/** key: a Local that is treated symbolically, value: the symbol used to represent the Local  */
	private Map<Local,String> localSymbolMap = new LinkedHashMap<Local,String>();
	
	/** key: a Value corresponding to an Intent extra, value: the string representing the key of the extra data  */
	private Map<Value,String> valueKeyMap = new LinkedHashMap<Value,String>();
	
	/** key: a symbol representing a string constant, value: the actual string constant value for the symbol */
	private Map<String,StringConstant> stringConstantMap = new LinkedHashMap<String,StringConstant>();
	
	/** symbols that represent Intent actions */
	private Set<String> actionSymbols = new LinkedHashSet<String>();
	
	/** key: action symbol, value: string constant symbols */
	private Map<String,Set<String>> actionStrings = new LinkedHashMap<String,Set<String>>();
	
	protected final static void handleIfStmt(SootMethod method, Path path, SimpleLocalDefs defs, IfStmt defStmt) {
		
	}
	
	protected final static void handleIntentGetExtraStmt(SootMethod method, Path path, SimpleLocalDefs defs, DefinitionStmt defStmt) {
		if (defStmt.containsInvokeExpr() && defStmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
            InstanceInvokeExpr ie = (InstanceInvokeExpr) defStmt.getInvokeExpr();
            int IntentOrBundle = 0; // intent is 1, bundle is 2
            if (Pattern.matches("get.*Extra", ie.getMethod().getName()) && ie.getMethod().getDeclaringClass().toString().equals("android.content.Intent"))
                IntentOrBundle = 1;
            else if (Pattern.matches("get.*", ie.getMethod().getName()) && ie.getMethod().getDeclaringClass().toString().equals("android.os.Bundle"))
            		IntentOrBundle = 2;
            else return;
            
            logger.debug("Perform path sensitive analysis for getExtra");
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
            				for(Unit intentDef : defs.getDefsOfAt(intentLocal, defStmt)) {
                				if(!isDefInPathAndLatest(path, intentDef, intentLocal, defStmt, defs))
                					continue;
                				if(defStmt.getLeftOp() instanceof Local) {
                					Local extraLocal = (Local) defStmt.getLeftOp();
                					String extraLocalSymbol = SymbolGenerate.createSymbol(extraLocal, method, defStmt);
                					symbolLocalMap.put(extraLocalSymbol,extraLocal);
                					String intentSymbol = SymbolGenerate.createSymbol(intentLocal,method,intentDef);
        							symbolLocalMap.put(intentSymbol,intentLocal);
        							String newExtraType = SymbolGenerate.getZ3Type(extraLocal.getType());
								String newIntentType = SymbolGenerate.getZ3Type(intentLocal.getType());
								path.decls.add("(declare-const " + extraLocalSymbol + " " + newExtraType + " )");
								path.decls.add("(declare-const " + intentSymbol + " " + newIntentType + " )");
								path.conds.add("(assert (= (containsKey " + extraLocalSymbol + " \"" + keyStrConst.value + "\") true))");
								path.conds.add("(assert (= (fromIntent " + extraLocalSymbol + ") " + intentSymbol + "))");
								buildParamRefExpressions(method, path, intentDef, intentSymbol);
                				}
                			}
            			}
            			else {
            				Local bundleLocal = (Local) ie.getBase();
            				for(Unit bundleDef : defs.getDefsOfAt(bundleLocal, defStmt)) {
            					if(!isDefInPathAndLatest(path, bundleDef, bundleLocal, defStmt, defs))
                					continue;
            					DefinitionStmt bundleDefStmt = (DefinitionStmt)bundleDef;
            					if(bundleDefStmt.containsInvokeExpr() && bundleDefStmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
            						InstanceInvokeExpr iie = (InstanceInvokeExpr) bundleDefStmt.getInvokeExpr();
            						if(iie.getBase().getType().toString().equals("android.content.Intent") && iie.getBase() instanceof Local) {
            							Local intentLocal = (Local) iie.getBase();
            							for(Unit intentDef : defs.getDefsOfAt(intentLocal, defStmt)) {
                            				if(!isDefInPathAndLatest(path, intentDef, intentLocal, defStmt, defs))
                            					continue;
                            				if(defStmt.getLeftOp() instanceof Local) {
                            					Local extraLocal = (Local) defStmt.getLeftOp();
                            					String extraLocalSymbol = SymbolGenerate.createSymbol(extraLocal, method, defStmt);
                            					//symbolLocalMap.put(extraLocalSymbol,extraLocal);
                            					String intentSymbol = SymbolGenerate.createSymbol(intentLocal,method,intentDef);
                    							//symbolLocalMap.put(intentSymbol,intentLocal);
                    							String newExtraType = SymbolGenerate.getZ3Type(extraLocal.getType());
                    							String newIntentType = SymbolGenerate.getZ3Type(intentLocal.getType());
                    							path.decls.add("(declare-const " + extraLocalSymbol + " " + newExtraType + " )");
                    							path.decls.add("(declare-const " + intentSymbol + " " + newIntentType + " )");
                    							path.conds.add("(assert (= (containsKey " + extraLocalSymbol + " \"" + keyStrConst.value + "\") true))");
                    							path.conds.add("(assert (= (fromIntent " + extraLocalSymbol + ") " + intentSymbol + "))");
                    							buildParamRefExpressions(method, path, intentDef, intentSymbol);
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
	
	protected final static void handleIntentGetActionStmt(SootMethod method, Path path, SimpleLocalDefs defs, DefinitionStmt defStmt) {
		InvokeExpr ie = defStmt.getInvokeExpr();
		if (ie.getMethod().getName().equals("getAction")) {
            if (ie.getMethod().getDeclaringClass().getName().equals("android.content.Intent")) {
                logger.debug("Perform path sensitive analysis for getAction");
                if (ie instanceof InstanceInvokeExpr) {
                    InstanceInvokeExpr iie = (InstanceInvokeExpr)ie;
                    String actionRefSymbol = null;
                    if (defStmt.getLeftOp() instanceof Local) {
                        Local leftLocal = (Local)defStmt.getLeftOp();
                        actionRefSymbol = SymbolGenerate.createSymbol(leftLocal, method, defStmt);
                        //symbolLocalMap.put(actionRefSymbol,leftLocal);
						if (iie.getBase() instanceof Local) {
							Local intentLocal = (Local) iie.getBase();
							for (Unit intentDef : defs.getDefsOfAt(intentLocal, defStmt)) {
								if (!isDefInPathAndLatest(path, intentDef, intentLocal, defStmt, defs))
									continue;
								if (path.unitPath.contains(intentDef)) {
									String intentSymbol = SymbolGenerate.createSymbol(intentLocal, method, intentDef);
									String intentDecl = "(declare-const " + intentSymbol + " Object )";
									String actionRefDecl = "(declare-const " + actionRefSymbol + " String )";
									path.decls.add(intentDecl);
									path.decls.add(actionRefDecl);
									//currDecls.add(getActionDecl);
									String getActionAssert = "(assert (= (getAction " + intentSymbol + ") " + actionRefSymbol + "))";
									String newFromIntent = "(assert (= (fromIntent " + actionRefSymbol + ") " + intentSymbol + "))";
									path.conds.add(getActionAssert);
									path.conds.add(newFromIntent);
									buildParamRefExpressions(method, path, intentDef, intentSymbol);
								}
							}
						}
					}
				}
            }
        }
	}
	
	private static void buildParamRefExpressions(SootMethod method, Path currPath, Unit intentDef, String intentSymbol) {
		if (intentDef instanceof DefinitionStmt) {
            DefinitionStmt defStmt = (DefinitionStmt) intentDef;
            if (!currPath.unitPath.contains(defStmt)) {
				return;
            }
            if (defStmt.getRightOp() instanceof ParameterRef) {
                ParameterRef pr = (ParameterRef)defStmt.getRightOp();
                String prSymbol = SymbolGenerate.createParamRefSymbol(defStmt.getLeftOp(),pr.getIndex(),method,defStmt);

                currPath.decls.add("(declare-const " + prSymbol + " ParamRef)");
                currPath.conds.add("(assert ( = (index " + prSymbol + ") " + pr.getIndex() + "))");
                currPath.conds.add("(assert ( = (type " + prSymbol + ") \"" + pr.getType() + "\"))");
                currPath.conds.add("(assert ( = (method " + prSymbol + ") \"" + method.getDeclaringClass().getName() + "." + method.getName() + "\"))");
                currPath.conds.add("(assert (= (hasParamRef " + intentSymbol + ") " + prSymbol + "))");
            }
        }
	}
	
	private static boolean isDefInPathAndLatest(Path path, Unit inDef, Local usedLocal, Unit usedUnit, SimpleLocalDefs defs) {
		if (path.unitPath.contains(inDef)) { // does the path contain the definition
			for (Unit otherDef : defs.getDefsOfAt(usedLocal,usedUnit)) { // check other defs of usedLocal at usedUnit to determine if inDef is the latestDef in path
				if (inDef.equals(otherDef)) // continue if inDef equals otherDef
					continue;
				if (!path.unitPath.contains(otherDef)) // if the otherDef is not in path, then continue
					continue;
				List<Unit> pathList = new ArrayList<Unit>(path.unitPath);
				int inDefPos = pathList.indexOf(inDef);
				int argDefPos = pathList.indexOf(otherDef);
				if (inDefPos > argDefPos)  // if inDef's position in the path is earlier then otherDef's position, then inDef is not the latest definition in the path, so return false
					return false;
			}
			return true; // inDef is in the path and is the latest definition along that path
		}
		else return false;
	}
}
