import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.PatchingChain;
import soot.SootMethod;
import soot.Unit;
import soot.ValueBox;

public class RunTransformer extends BodyTransformer{
	protected static List<SootMethod> entryPoints;
	
	public void setEntryPoints(List<SootMethod> entry){
		RunTransformer.entryPoints = entry;
	}
	
	@Override
	protected void internalTransform(final Body b,String phaseName,final Map<String,String> options) {
		// TODO Auto-generated method stub
		
		if(entryPoints.contains(b.getMethod())){
			System.out.println(b.getMethod().toString());
			List<ValueBox> intent = new ArrayList<ValueBox>();
			List<ValueBox> bundle = new ArrayList<ValueBox>();
			List<ValueBox> rel = new ArrayList<ValueBox>();
			for(ValueBox v : b.getDefBoxes()){
				if(v.getValue().getType().toString().equals("android.content.Intent")){
					intent.add(v);
					break;
				}
			}
			
			PatchingChain<Unit> units = b.getUnits();
			for(Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();){
				final Unit u = iter.next();
				for(ValueBox v1 : u.getUseBoxes()){
					for(ValueBox v2 : intent)
						if(v2.getValue().equals(v1.getValue())){
							for(ValueBox v : u.getDefBoxes())
								if(v.getValue().getType().toString().equals("android.os.Bundle"))
									bundle.add(v);
							break;
						}
					for(ValueBox v2 : bundle)
						if(v2.getValue().equals(v1.getValue())){
							for(ValueBox v : u.getDefBoxes())
								if(v.getValue().getType().toString().equals("android.os.Bundle"))
									bundle.add(v);
								else rel.add(v);
							break;
						}
				}
			}
			System.out.println("intent:");
			for(ValueBox v : intent){
				System.out.print(v.getValue()+" ");
				System.out.println(v.getValue().getType());
			}
			System.out.println("bundle:");
			for(ValueBox v : bundle){
				System.out.print(v.getValue()+" ");
				System.out.println(v.getValue().getType());
			}
			System.out.println("rel:");
			for(ValueBox v : rel){
				System.out.print(v.getValue()+" ");
				System.out.println(v.getValue().getType());
			}
			System.out.println("zzz");
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