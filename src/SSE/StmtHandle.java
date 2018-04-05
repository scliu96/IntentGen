package SSE;

import java.util.Set;
import java.util.regex.Pattern;

import Type.Path;
import soot.SootMethod;
import soot.jimple.DefinitionStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.toolkits.scalar.SimpleLocalDefs;

public class StmtHandle {
	public final static void handleGetExtraOfIntent(SootMethod m, Path p, SimpleLocalDefs defs, DefinitionStmt currStmtInPath) {
		DefinitionStmt defStmt = currStmtInPath;
		if (defStmt.containsInvokeExpr() && defStmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
            InstanceInvokeExpr ie = (InstanceInvokeExpr) defStmt.getInvokeExpr();
            if (Pattern.matches("get.*Extra", ie.getMethod().getName())) {
                if (ie.getMethod().getDeclaringClass().toString().equals("android.content.Intent")) {
                    Pair<Set<String>,Set<String>> exprPair = buildGetExtraData(defStmt, defs, ie, method,currPath);
                    currDecls.addAll(exprPair.getValue0());
                    currPathCond.addAll(exprPair.getValue1());
                }
            }
			if (Pattern.matches("get.*", ie.getMethod().getName())) {
				if (ie.getMethod().getDeclaringClass().toString().equals("android.os.Bundle")) {
					Pair<Set<String>,Set<String>> exprPair = buildGetBundleData(defStmt, defs, ie, method,currPath);
					currDecls.addAll(exprPair.getValue0());
					currPathCond.addAll(exprPair.getValue1());
				}
			}
        }
	}
}
