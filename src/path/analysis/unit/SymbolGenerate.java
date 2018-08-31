package path.analysis.unit;

import java.util.Set;

import path.analysis.assist.Database;
import path.analysis.main.Init;
import path.analysis.type.UnitPath;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.jimple.DoubleConstant;
import soot.jimple.FloatConstant;
import soot.jimple.IntConstant;
import soot.jimple.LongConstant;
import soot.jimple.NullConstant;
import soot.jimple.StringConstant;
import soot.jimple.internal.JimpleLocal;
import soot.tagkit.BytecodeOffsetTag;
import soot.tagkit.Tag;

public class SymbolGenerate {
	
	protected static String createSymbol(Value opVal, SootMethod method, Unit unit) {
		String valNameNoDollar = opVal.toString();
		BytecodeOffsetTag bcoTag = null;
		for (Tag tag : unit.getTags()) {
			if (tag instanceof BytecodeOffsetTag) {
				bcoTag = (BytecodeOffsetTag)tag;
			}
		}
		String symbol = null;
		if (bcoTag != null)
			symbol = valNameNoDollar + "_" + convertTypeNameForZ3(opVal.getType()) + "_" + method.getName() + "_" + method.getDeclaringClass().getName() + "_" + bcoTag.toString();
		else
			symbol = valNameNoDollar + "_" + convertTypeNameForZ3(opVal.getType()) + "_" + method.getName() + "_" + method.getDeclaringClass().getName();
		return symbol;
	}
	
	protected static String createParamRefSymbol(Value opVal, int index, SootMethod method, Unit unit) {
		BytecodeOffsetTag bcoTag = null;
		for (Tag tag : unit.getTags()) {
			if (tag instanceof BytecodeOffsetTag) {
				bcoTag = (BytecodeOffsetTag)tag;
			}
		}
		String symbol = null;
		if (bcoTag != null)
			symbol = "pr" + index + "_" + convertTypeNameForZ3(opVal.getType()) + "_" + method.getName() + "_" + method.getDeclaringClass().getName() + "_" + bcoTag.toString();
		else symbol = "pr" + index + "_" + convertTypeNameForZ3(opVal.getType()) + "_" + method.getName() + "_" + method.getDeclaringClass().getName();
		return symbol;
	}
	
	private static String convertTypeNameForZ3(Type type) {
		String returnStr = type.toString();
		returnStr = returnStr.replace("[]","-Arr");
		return returnStr;
	}
	
	public static String negateSymbol(String symbol) {
		switch (symbol.trim()) {
		case "==":
			return "!=";
		case "!=":
			return "==";
		case ">":
			return "<=";
		case "<":
			return ">=";
		case ">=":
			return "<";
		case "<=":
			return ">";
		default:
			throw new RuntimeException("invalid symbol passed to negateSymbol(): " + symbol);
		}
	}
	
	protected static String createZ3Expr(SootMethod method, UnitPath currPath, Unit currUnit, Value opVal, Unit defUnit) {
		String opExpr = null;
		String newDecl = null;

		if (opVal instanceof IntConstant) {
			IntConstant intConst = (IntConstant) opVal;
			opExpr = Integer.toString(intConst.value);
		} else if (opVal instanceof LongConstant) {
			LongConstant longConst = (LongConstant) opVal;
			opExpr = Long.toString(longConst.value);
		} else if (opVal instanceof FloatConstant) {
			FloatConstant floatConst = (FloatConstant) opVal;
			opExpr = Float.toString(floatConst.value);
		} else if (opVal instanceof DoubleConstant) {
			DoubleConstant doubleConst = (DoubleConstant) opVal;
			opExpr = Double.toString(doubleConst.value);
		} else if (opVal instanceof NullConstant) {
			opExpr = "Null";
		} else if (opVal instanceof StringConstant) {
			StringConstant strConst = (StringConstant) opVal;
			opExpr = "\"" + strConst.value + "\"";
		} else if (opVal instanceof JimpleLocal) {
			JimpleLocal opLocal = (JimpleLocal) opVal;
			Init.logger.trace("opLocal type: " + opLocal.getType());

			String symbol = null;
			DefinitionStmt defStmt = (DefinitionStmt) defUnit;
			if (defStmt.getLeftOp() == opVal) {
				symbol = createSymbol(opVal, method, defStmt);
				Database.symbolLocalMap.put(symbol, opLocal);
				Database.localSymbolMap.put(opLocal, symbol);
			}

			symbol = Database.localSymbolMap.get(opLocal);
			if (symbol == null) {
				symbol = createSymbol(opVal, method, defUnit);
				Database.symbolLocalMap.put(symbol, opLocal);
				Database.localSymbolMap.put(opLocal, symbol);
			}

			switch (opLocal.getType().toString().trim()) {
				case "short":
					newDecl = "(declare-const " + symbol + " Int )";
					opExpr = symbol;
					break;
				case "int":
					newDecl = "(declare-const " + symbol + " Int )";
					opExpr = symbol;
					break;
				case "long":
					newDecl = "(declare-const " + symbol + " Int )";
					opExpr = symbol;
					break;
				case "float":
					newDecl = "(declare-const " + symbol + " Real )";
					opExpr = symbol;
					break;
				case "double":
					newDecl = "(declare-const " + symbol + " Real )";
					opExpr = symbol;
					break;
				case "boolean":
					newDecl = "(declare-const " + symbol + " Int )";
					opExpr = symbol;
					break;
				case "byte":
					newDecl = "(declare-const " + symbol + " Int )";
					opExpr = symbol;
					break;
				case "java.lang.String":
					newDecl = "(declare-const " + symbol + " String )";
					opExpr = symbol;
					break;
				default:
					// object is an arbitrary type so we'll mark it as null or not null
					Init.logger.debug("Creating object with symbol: " + symbol + " for Local " + opLocal + " in " + method);
					newDecl = "(declare-const " + symbol + " Object )";
					opExpr = symbol;
			}
			currPath.decls.add(newDecl);
		} else throw new RuntimeException("I don't know what to do with this Value's type: " + opVal.getType());
		return opExpr;
	}
	
	protected static String buildZ3CondExpr(String opExpr1, String opExpr2, String branchSensitiveSymbol) {
		String returnExpr;
		String condExpr = null;

		switch (branchSensitiveSymbol.trim()) {
			case "==":
				if (opExpr2.equals("Null"))
					condExpr = "(assert (= (isNull " + opExpr1 + ") true))";
				else if (isObjectEquals(opExpr1,opExpr2))
					condExpr = "(assert (= (oEquals " + opExpr1 + " "  + opExpr2 + ") true))";
				else
					condExpr = "(assert (= " + opExpr1 + " " + opExpr2 + "))";
				break;
			case "!=":
				if (opExpr2.equals("Null"))
					condExpr = "(assert (= (isNull " + opExpr1 + ") false))";
				else if (isObjectEquals(opExpr1,opExpr2))
					condExpr = "(assert (= (oEquals " + opExpr1 + " "  + opExpr2 + ") false))";
				else
					condExpr = "(assert (not (= " + opExpr1 + " " + opExpr2 + ")))";
				break;
			case ">":
				condExpr = "(assert (> " + opExpr1 + " " + opExpr2 + "))";
				break;
			case ">=":
				condExpr = "(assert (>= " + opExpr1 + " " + opExpr2 + "))";
				break;
			case "<":
				condExpr = "(assert (< " + opExpr1 + " " + opExpr2 + "))";
				break;
			case "<=":
				condExpr = "(assert (<= " + opExpr1 + " " + opExpr2 + "))";
				break;
		}
		Init.logger.trace("z3 conditional expr: " + condExpr);

		if (condExpr == null) {
            Init.logger.debug("currExpr should not be null");
            Init.logger.debug("opExpr1: " + opExpr1);
            Init.logger.debug("opExpr2: " + opExpr2);
            throw new RuntimeException("currExpr should not be null");
        }
		returnExpr = condExpr;
		return returnExpr;
	}
	
	private static boolean isObjectEquals(String opExpr1, String opExpr2) {
		if (opExpr1.contains("_java.lang.String_") && !opExpr2.contains("_java.lang.String_") && !opExpr2.contains("\""))
			return true;
		else if (!opExpr1.contains("_java.lang.String_") && opExpr2.contains("_java.lang.String_") && !opExpr2.contains("\""))
			return true;
		else
			return false;
	}
	
	protected static String getZ3Type(Type type) {
		switch (type.toString()) {
			case "short":
				return "Int";
			case "int":
				return "Int";
			case "long":
				return "Int";
			case "float":
				return "Real";
			case "double":
				return "Real";
			case "boolean":
				return "Int";
			case "byte":
				return "Int";
			case "java.lang.String":
				return "String";
			default:
				return "Object";
		}
	}
}
