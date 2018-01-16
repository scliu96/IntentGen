package IntentFind;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import soot.Type;
import soot.SootMethod;
import soot.Value;
import soot.ValueBox;

public class MyIntent {
	private List<SootMethod> method;
	private List<Value> rel;
	private Map<String,Type> property;
	
	public MyIntent(){
		this.method = new ArrayList<SootMethod>();
		this.rel = new ArrayList<Value>();
		this.property = new HashMap<String,Type>();
	}
	
	public MyIntent(SootMethod m){
		this.method = new ArrayList<SootMethod>();
		this.method.add(m);
		this.rel = new ArrayList<Value>();
		this.property = new HashMap<String,Type>();
	}
	
	public MyIntent(List<SootMethod> m){
		this.method = m;
		this.rel = new ArrayList<Value>();
		this.property = new HashMap<String,Type>();
	}
	
	public boolean containMethod(SootMethod m){
		if(this.method.contains(m))
			return true;
		else return false;
	}
	
	public void addMethod(SootMethod m){
		if(!this.method.contains(m))
			this.method.add(m);
	}
	
	public List<SootMethod> getMethodList(){
		return this.method;
	}
	
	public SootMethod getLastMethod(){
		int size = this.method.size();
		return this.method.get(size-1);
	}
	
	public List<Value> getRel(){
		return this.rel;
	}
	
	public boolean relContain(Value v){
		if( this.rel.isEmpty())
			return false;
		if( this.rel.contains(v) )
			return true;
		else return false;
	}
	
	public boolean relAdd(Value v){
		if(!this.rel.contains(v))
			this.rel.add(v);
		return true;
	}
	
	public void relClean(){
		this.rel.clear();
	}
	
	public void relPrint(){
		for(Value v : this.rel){
			System.out.println(v.toString());
		}
	}
	
	public Map<String,Type> getPro(){
		return this.property;
	}
	
	public boolean proContain(String s){
		if( this.property.isEmpty())
			return false;
		if( this.property.containsKey(s))
			return true;
		return false;
	}
	
	public boolean proAdd(String s, Type t){
		if(!this.property.containsKey(s))
			this.property.put(s, t);
		return true;
	}
	
	public void proPrint(){
		for(String s : this.property.keySet())
			System.out.println(s + "," + this.property.get(s));
	}
	
	public void mergeSubIntent(MyIntent sub){
		for(String s : sub.getPro().keySet()){
			this.proAdd(s,sub.getPro().get(s));
		}
	}
	
}
