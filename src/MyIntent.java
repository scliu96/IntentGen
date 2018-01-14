import java.util.ArrayList;
import java.util.List;

import soot.SootMethod;
import soot.Value;
import soot.ValueBox;

public class MyIntent {
	private SootMethod method;
	private List<Value> rel;
	private List<String> property;
	
	public MyIntent(){
		this.method = null;
		this.rel = new ArrayList<Value>();
		this.property = new ArrayList<String>();
	}
	
	public MyIntent(SootMethod m){
		this.method = m;
		this.rel = new ArrayList<Value>();
		this.property = new ArrayList<String>();
	}
	
	public void setMethod(SootMethod m){
		this.method = m;
	}
	
	public SootMethod getMethod(){
		return this.method;
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
	
	public List<String> getPro(){
		return this.property;
	}
	
	public boolean proContain(String s){
		if( this.property.isEmpty())
			return false;
		if( this.property.contains(s) )
			return true;
		return false;
	}
	
	public boolean proAdd(String s){
		if(!this.property.contains(s))
			this.property.add(s);
		return true;
	}
	
	public void proPrint(){
		for(String s : this.property)
			System.out.println(s);
	}
	
	public void mergeSubIntent(MyIntent sub){
		for(String s : sub.getPro()){
			this.proAdd(s);
		}
	}
	
}
