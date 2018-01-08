import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.PatchingChain;
import soot.SootMethod;
import soot.Unit;
import soot.ValueBox;
import soot.jimple.toolkits.callgraph.CallGraph;

public class RunTransformer extends BodyTransformer{
	protected static List<CallGraph> entryGraphs = new LinkedList<CallGraph>();
	protected static List<SootMethod> Points = new LinkedList<SootMethod>();
	
	public void init(List<CallGraph> call, List<SootMethod> entry){
		RunTransformer.entryGraphs = call;
		RunTransformer.Points = entry;
	}
	
	@Override
	protected void internalTransform(final Body b,String phaseName,final Map<String,String> options) {
		// TODO Auto-generated method stub
		
		if(Points.contains(b.getMethod())){
			//System.out.println(b.getMethod().toString());
			RelateValue rv = new RelateValue(b);
			rv.print();
		}
	}
	
	/*
	public void createCallGraph(Body b,final Map<String,String> options){
		CFGGraphType graphtype;
		graphtype = CFGGraphType.getGraphType("BriefUnitGraph");
		DirectedGraph<Unit> graph = graphtype.buildGraph(b);
		CFGToDotGraph drawer = new CFGToDotGraph();
		
		drawer.setBriefLabels(PhaseOptions.getBoolean(options,"briefLabelOptionName"));
		drawer.setOnePage(!PhaseOptions.getBoolean(options,"multipageOptionName"));
		drawer.setUnexceptionalControlFlowAttr("color", "black");
		drawer.setExceptionalControlFlowAttr("color", "red");
		drawer.setExceptionEdgeAttr("color", "lightgray");
		drawer.setShowExceptions(Options.v().show_exception_dests());
		
		DotGraph canvas = graphtype.drawGraph(drawer, graph, b);
		String methodName = b.getMethod().getSubSignature();
		String className = b.getMethod().getDeclaringClass().getName().replaceAll("\\$", "\\.");
		String fileName = soot.SourceLocator.v().getOutputDir();
		if(fileName.length() > 0)
			fileName += java.io.File.separator;
		fileName += className + " " + methodName.replace(java.io.File.separatorChar, '.') + DotGraph.DOT_EXTENSION;
		G.v().out.println(fileName);
		canvas.plot(fileName);
	}*/
}