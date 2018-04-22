package path.analysis.method;
import java.util.Map;

import global.Database;
import soot.Body;
import soot.BodyTransformer;
import soot.SootMethod;

public class SearchTransformer extends BodyTransformer{
	
	@Override
	protected void internalTransform(final Body b,String phaseName,final Map<String,String> options){
		if(methodIsNeed(b.getMethod()))
			Database.entryPoints.add(b.getMethod());
	}
	
	private boolean methodIsNeed(SootMethod m){
		if(m.getDeclaringClass().hasSuperclass())
			if(m.getDeclaringClass().getSuperclass().toString().equals("android.app.Service")){
				if(m.getDeclaringClass().getName().equals("android.support.v4.app.NotificationCompatSideChannelService"))
					return false;
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
				if(m.getName().equals("onBind"))
					return true;
			}
		return false;
	}
}