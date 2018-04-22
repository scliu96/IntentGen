package path.analysis.solver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.javatuples.Pair;
import org.javatuples.Triplet;

import com.microsoft.z3.*;

import global.Database;
import global.Init;
import soot.Local;
import soot.SootMethod;
import soot.Unit;
import type.Intent;
import type.UnitPath;

public class Solve {
	private final static String Z3_RUNTIME_SPECS_DIR = "z3_runtime_specs";
	
	protected static boolean runSolvingPhase(SootMethod method, String currClassName, Unit startingUnit, UnitPath currPath) {
		Init.logger.trace("Current z3 specification to solve: ");
		for(String decl : currPath.decls)
			Init.logger.trace(decl);
		for(String expr : currPath.conds)
			Init.logger.trace(expr);
		
		Pair<Intent,Boolean> results = findSolutionForPath(method,currPath,startingUnit);
		boolean feasible = results.getValue1();
		Intent genIntent = results.getValue0();
		return feasible;
	}
	
	private static Pair<Intent,Boolean> findSolutionForPath(SootMethod method, UnitPath currPath, Unit startingUnit) {
		String targetComponent = null;
		String action = null;
		Set<String> categories = new LinkedHashSet<String>();
		Set<Triplet<String,String,String>> extrasData = new LinkedHashSet<Triplet<String,String,String>>();
		boolean isPathFeasible = false;
		
		try {
			Pair<Map<String,String>,Boolean> returnSATModel = returnSatisfyingModelForZ3(currPath);
			Map<String,String> model = returnSATModel.getValue0();
			Boolean isSAT = returnSATModel.getValue1();
			if(!isSAT) {
				Init.logger.trace("path is infeasible");
				isPathFeasible = false;
			}
			else {
				Init.logger.trace("path is feasible");
				isPathFeasible = true;
				
				Map<String,String> intentSymbol_actionSymbol_map = new LinkedHashMap<String,String>();
				Map<String,String> extraLocalSymbol_key_map = new LinkedHashMap<String,String>();
				
				for(String cond : currPath.conds) {
					Pattern pAction = Pattern.compile("\\(assert \\(= \\(getAction (.+)\\) (.+)\\)\\)");
					Matcher mAction = pAction.matcher(cond);
					while (mAction.find()) {
						String intentSymbol = mAction.group(1);
						//logger.info("intent symbol for action: " + intentSymbol);
						String actionStrSymbol = mAction.group(2);
						//logger.info("action symbol: " + actionStrSymbol);
						intentSymbol_actionSymbol_map.put(intentSymbol,actionStrSymbol);
					}
					
					Pattern pCategories = Pattern.compile("\\(assert \\(exists \\(\\(index Int\\)\\) \\(= \\(select cats index\\) \\\"(.+)\\\"\\)\\)\\)");
					Matcher mCategories = pCategories.matcher(cond);
					while(mCategories.find()) {
						String category = mCategories.group(1);
						categories.add(category);
					}
						
					Pattern pExtra = Pattern.compile("\\(assert \\(= \\(containsKey (.+) \\\"(.+)\\\"\\) true\\)\\)");
					Matcher mExtra = pExtra.matcher(cond);
					while (mExtra.find()) {
						String extraLocalSymbol = mExtra.group(1);
						//logger.info("Found extra local symbol: " + extraLocalSymbol);
						String key = mExtra.group(2);
						//logger.info("Found key for extra: " + key);
						extraLocalSymbol_key_map.put(extraLocalSymbol,key);
					}
				}
				
				for(Map.Entry<String, String> entry : model.entrySet()) {
					String symbol = entry.getKey();
					String value = entry.getValue();
					
					Local local = Database.symbolLocalMap.get(symbol);
					String key = extraLocalSymbol_key_map.get(symbol);
					if(local != null && key != null) {
						Triplet<String,String,String> extraData = new Triplet<String, String, String>(local.getType().toString(), key, value.toString().replaceAll("^\"|\"$", ""));
						extrasData.add(extraData);
					}	
					
					for(String actionSymbol : intentSymbol_actionSymbol_map.values())
						if(actionSymbol.equals(symbol))
							action = value.replaceAll("^\"|\"$", "");
				}
			}
		} catch (Z3Exception e) {
			e.printStackTrace();
		}
		
		Intent newIntent = new Intent(method.getDeclaringClass().getName(),action,categories,extrasData);
		
	}
	
	private static Pair<Map<String,String>,Boolean> returnSatisfyingModelForZ3(UnitPath currPath) {
		String pathCondFileName = null;
		try {
			pathCondFileName = Z3_RUNTIME_SPECS_DIR + File.separator + Thread.currentThread().getId() + "_z3_path_cond";
			PrintWriter out = new PrintWriter(pathCondFileName);
			String outSpec = "";
			outSpec +=	"(declare-datatypes () ((Object Null NotNull)))\n" +
						"(declare-fun containsKey (Object String) Bool)\n" +
						"(declare-fun containsKey (String String) Bool)\n" +
						"(declare-fun containsKey (Int String) Bool)\n" +
						"(declare-fun containsKey (Real String) Bool)\n" +

						"(declare-fun getAction (Object) String)\n" +

						"(declare-fun fromIntent (Object) Object)\n" +
						"(declare-fun fromIntent (String) Object)\n" +
						"(declare-fun fromIntent (Int) Object)\n" +
						"(declare-fun fromIntent (Real) Object)\n" +

						"(declare-datatypes () ((ParamRef (mk-paramref (index Int) (type String) (method String)))))\n"+
						"(declare-fun hasParamRef (Object) ParamRef)\n"+
						"(declare-fun hasParamRef (String) ParamRef)\n"+
						"(declare-fun hasParamRef (Int) ParamRef)\n"+
						"(declare-fun hasParamRef (Real) ParamRef)\n"+

						"(declare-fun isNull (String) Bool)\n" +
						"(declare-fun isNull (Object) Bool)\n" +
						"(declare-fun oEquals (String Object) Bool)\n" +
						"(declare-fun oEquals (Object String) Bool)\n" +
						"(declare-const cats (Array Int String))\n" +
						"(declare-const keys (Array Int String))\n";
			for (String d : currPath.decls)
				outSpec += d+"\n";
			for (String c : currPath.conds)
				outSpec += c+"\n";
			outSpec += "(check-sat-using (then qe smt))\n";
			outSpec += "(get-model)\n";
			Init.logger.trace("z3 specification sent to solver:");
			Init.logger.trace(outSpec);
			out.print(outSpec);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		String solverLoc = System.getenv("Z3_EXEC");
		ProcessBuilder pb = new ProcessBuilder(solverLoc,pathCondFileName);
		Init.logger.trace("Running z3 solver");
		Process p = null;
		String returnedOutput = null;
		try {
			p = pb.start();
			int errCode = p.waitFor();
			Init.logger.trace("Returned error code from solver: " + errCode);
			Init.logger.trace("Returned input stream as string:");
			returnedOutput = convertStreamToString(p.getInputStream());
			Init.logger.trace(returnedOutput);
			Init.logger.trace("Returned error stream as string:");
			String errorOut = convertStreamToString(p.getErrorStream());
			Init.logger.trace(errorOut);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Map<String,String> model = new LinkedHashMap<String,String>();
		Pattern pat = Pattern.compile("\\s+\\(define-fun\\s+(\\S+)\\s+\\(\\)\\s+\\w+\\s+(.+)(?=\\))");
		Matcher m = pat.matcher(returnedOutput);
		while (m.find()) {
			String symbol = m.group(1);
			String value = m.group(2);
			model.put(symbol, value);
		}

		String[] outLines = returnedOutput.split("\\n");
		Boolean isSat = false;
		for (String line : outLines) {
			if (line.trim().equals("sat"))
				isSat = true;
		}
		return new Pair<Map<String,String>,Boolean>(model,isSat);
	}
	
	private static String convertStreamToString(java.io.InputStream is) {
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}
}
