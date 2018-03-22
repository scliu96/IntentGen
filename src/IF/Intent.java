package IF;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import soot.Value;

public class Intent {
	private Set<String> actions = new LinkedHashSet<String>();
	private Set<String> datas = new LinkedHashSet<String>();
	private Set<String> categories = new LinkedHashSet<String>();
	private Map<String,Value> extras = new LinkedHashMap<String,Value>();
	private String targetComponent;
	
	public Intent() {
        super();
    }
	
    public Intent(Intent intent) {
    		if(intent.actions != null)
    			this.actions = new LinkedHashSet<String>();
    		else this.actions = null;
    		
    		if(intent.datas != null)
    			this.datas = new LinkedHashSet<String>();
    		else this.datas = null;
    		
    		if(intent.categories != null)
    			this.categories = new LinkedHashSet<String>();
    		else this.categories = null;
    	
    	    if(intent.extras != null)
    			this.extras = new LinkedHashMap<String,Value>();
    		else this.extras = null;

    	    if (intent.targetComponent != null)
                this.targetComponent = new String(intent.targetComponent);
        else this.targetComponent = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Intent intent = (Intent) o;
        if (actions != null ? !actions.equals(intent.actions) : intent.actions != null) return false;
        if (datas != null ? !datas.equals(intent.datas) : intent.datas != null) return false;
        if (categories != null ? !categories.equals(intent.categories) : intent.categories != null) return false;
        if (extras != null ? !extras.equals(intent.extras) : intent.extras != null) return false;
        if (targetComponent != null ? !targetComponent.equals(intent.targetComponent) : intent.targetComponent != null)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = extras != null ? extras.hashCode() : 0;
        result = 31 * result + (actions != null ? actions.hashCode() : 0);
        result = 31 * result + (datas != null ? datas.hashCode() : 0);
        result = 31 * result + (categories != null ? categories.hashCode() : 0);
        result = 31 * result + (targetComponent != null ? targetComponent.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Intent{" +
        			"actions='" + actions + '\'' +
        		    ", datas=" + datas + '\'' +
        		    ", categories=" + categories + '\'' +
                ", extras=" + extras + '\'' +
                ", targetComponent='" + targetComponent + '\'' +
                '}';
    }
    
    /*
	private List<SootMethod> method;
	private List<Value> intent;
	private List<Value> bundle;
	private Map<String,Type> property;
	
	public Intent(){
		this.method = new ArrayList<SootMethod>();
		this.intent = new ArrayList<Value>();
		this.bundle = new ArrayList<Value>();
		this.property = new HashMap<String,Type>();
	}
	
	public Intent(SootMethod m){
		this.method = new ArrayList<SootMethod>();
		this.method.add(m);
		this.intent = new ArrayList<Value>();
		this.bundle = new ArrayList<Value>();
		this.property = new HashMap<String,Type>();
	}
	
	public Intent(List<SootMethod> m){
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
	}*/
}
