
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.ValueBox;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.SimpleLiveLocals;
import soot.util.cfgcmd.CFGGraphType;
import soot.util.cfgcmd.CFGToDotGraph;
import soot.util.dot.DotGraph;


public class AndroidInstrument{
	public final static String sootJarPath="/Users/apple/Documents/Soot/FlowDroid/sootclasses-trunk-jar-with-dependencies.jar";
	public final static String androidJarPath="/Users/apple/Documents/Soot/android-platforms-master";
	public final static String APKPath="/Users/apple/Documents/Android/Projects/MyApplication/app/build/outputs/apk/app-release-unaligned.apk";
	//public final static String rtJarPath="/Library/Java/JavaVirtualMachines/jdk1.7.0_80.jdk/Contents/Home/jre/lib/rt.jar";
	
	public static void initSoot(String[] args){
		Options.v().set_src_prec(Options.src_prec_apk);
		Options.v().set_android_jars(androidJarPath);
		Options.v().set_process_dir(Collections.singletonList(APKPath));
		Options.v().set_output_format(Options.output_format_none);
		//Options.v().set_whole_program(true);
		Options.v().set_allow_phantom_refs(true);
		//Options.v().setPhaseOption("cg.spark", "on");
		//Options.v().setPhaseOption("cg.spark", "rta:true");
		//Options.v().setPhaseOption("cg.spark", "on-fly-cg:false");
		Scene.v().loadNecessaryClasses();
	}
	
	public static void main(String[] args) {
		//SetupApplication app = new SetupApplication(androidJarPath, APKPath);
		//soot.G.reset();
		SetupApplication analyzer = new SetupApplication(androidJarPath, APKPath);
		analyzer.constructCallgraph();
		
		initSoot(args);
        PackManager.v().getPack("jtp").add(new Transform("jtp.myInstrumenter",new MyTransform()));
        PackManager.v().runPacks();
        
        //CallGraph cg = Scene.v().getCallGraph();
        //SootMethod entryPoint = app.
        
        return;
	}
}

class MyTransform extends BodyTransformer{
	
	@Override
	protected void internalTransform(final Body b,String phaseName,final Map<String,String> options) {
		// TODO Auto-generated method stub
		/*
		SootClass sClass = b.getMethod().getDeclaringClass();
		if(sClass.getName().equals("com.vogella.android.myapplication.MyService")){
			SootMethod m = b.getMethod();
			if((m.getName().equals("onStartCommand"))||(m.getName().equals("onBind"))){
				System.out.println(b.toString());
				
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
		}*/
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