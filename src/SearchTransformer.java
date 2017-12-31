import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.SootMethod;

public class SearchTransformer extends BodyTransformer{
	protected List<SootMethod> entryPoints = new LinkedList<SootMethod>();
	
	@Override
	protected void internalTransform(final Body b,String phaseName,final Map<String,String> options){
		if(b.getMethod().getDeclaringClass().hasSuperclass())
			if(b.getMethod().getDeclaringClass().getSuperclass().toString().equals("android.app.Service")){
				if(b.getMethod().getName().equals("onCreate"))
					entryPoints.add(b.getMethod());
				else if(b.getMethod().getName().equals("onStart"))
					entryPoints.add(b.getMethod());
				else if(b.getMethod().getName().equals("onStartCommand"))
					entryPoints.add(b.getMethod());
				else if(b.getMethod().getName().equals("onBind"))
					entryPoints.add(b.getMethod());
				else if(b.getMethod().getName().equals("onUnbind"))
					entryPoints.add(b.getMethod());
			}
	}
	
	public List<SootMethod> getEntryPoints(){
		return entryPoints;
	}
}