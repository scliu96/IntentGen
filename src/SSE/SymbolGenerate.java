package SSE;

import java.util.List;
import java.util.Set;

import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.jimple.ParameterRef;
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
