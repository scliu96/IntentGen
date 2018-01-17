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
	private List<Value> intent;
	private List<Value> bundle;
	private Map<String,Type> property;
	
	public MyIntent(){
		this.method = new ArrayList<SootMethod>();
		this.intent = new ArrayList<Value>();
		this.bundle = new ArrayList<Value>();
		this.property = new HashMap<String,Type>();
	}
	
	public MyIntent(SootMethod m){
		this.method = new ArrayList<SootMethod>();
		this.method.add(m);
		this.intent = new ArrayList<Value>();
		this.bundle = new ArrayList<Value>();
		this.property = new HashMap<String,Type>();
	}
	
	public MyIntent(List<SootMethod> m){
		this.method = m;
		this.intent = new ArrayList<Value>();
		this.bundle = new ArrayList<Value>();
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
	
	public List<SootMethod> getMethods(){
		return this.method;
	}
	
	public void setMethods(List<SootMethod> m){
		this.method = m;
	}
	
	public SootMethod getMethod(){
		int size = this.method.size();
		return this.method.get(size-1);
	}
	
	public List<Value> getIntent(){
		return this.intent;
	}
	
	public void setIntent(List<Value> in){
		this.intent = in;
	}
	
	public boolean containIntent(Value v){
		if( this.intent.isEmpty())
			return false;
		if( this.intent.contains(v) )
			return true;
		else return false;
	}
	
	public boolean addIntent(Value v){
		if(!this.intent.contains(v))
			this.intent.add(v);
		return true;
	}
	
	public void cleanIntent(){
		this.intent.clear();
	}
	
	public void printIntent(){
		for(Value v : this.intent){
			System.out.println(v.toString());
		}
	}
	
	public List<Value> getBundle(){
		return this.bundle;
	}
	
	public void setBundle(List<Value> bun){
		this.bundle = bun;
	}
	
	public boolean containBundle(Value v){
		if( this.bundle.isEmpty())
			return false;
		if( this.bundle.contains(v) )
			return true;
		else return false;
	}
	
	public boolean addBundle(Value v){
		if(!this.bundle.contains(v))
			this.bundle.add(v);
		return true;
	}
	
	public void cleanBundle(){
		this.bundle.clear();
	}
	
	public void printBundle(){
		for(Value v : this.bundle){
			System.out.println(v.toString());
		}
	}
	
	public Map<String,Type> getProperty(){
		return this.property;
	}
	
	public boolean containProperty(String s){
		if( this.property.isEmpty())
			return false;
		if( this.property.containsKey(s))
			return true;
		return false;
	}
	
	public boolean addProperty(String s, Type t){
		if(!this.property.containsKey(s))
			this.property.put(s, t);
		return true;
	}
	
	public void printProperty(){
		for(String s : this.property.keySet())
			System.out.println(s + "," + this.property.get(s));
	}
}
