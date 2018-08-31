package path.analysis.method;
import java.util.Map;

import path.analysis.assist.Config;
import path.analysis.assist.Database;
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
				if(isApkClassName(m.getDeclaringClass().getName()))
					return false;
				else if(Config.entryMethodsSet.contains(m.getName()))
					return true;
			}
		return false;
	}
	
	protected static boolean isApkClassName(String className) {
		for(String apkStartString: Config.androidPkgPrefixesSet)
			if(className.startsWith(apkStartString))
				return true;
		return false;
	}
}