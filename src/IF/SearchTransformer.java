package IF;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.SootMethod;

public class SearchTransformer extends BodyTransformer{
	private List<SootMethod> entryPoints = new LinkedList<SootMethod>();
	
	@Override
	protected void internalTransform(final Body b,String phaseName,final Map<String,String> options){
		if(this.methodIsNeed(b.getMethod()))
			this.entryPoints.add(b.getMethod());
	}
	
	private boolean methodIsNeed(SootMethod m){
		if(m.getDeclaringClass().hasSuperclass())
			if(m.getDeclaringClass().getSuperclass().toString().equals("android.app.Service")){
				if(m.getName().equals("onCreate"))
					return true;
				else if(m.getName().equals("onStart"))
					return true;
				else if(m.getName().equals("onStartCommand"))
					return true;
				else if(m.getName().equals("onBind"))
					return true;
				else if(m.getName().equals("onUnbind"))
					return true;
			}
		return false;
	}
	
	public List<SootMethod> getEntryPoints(){
		return entryPoints;
	}
}