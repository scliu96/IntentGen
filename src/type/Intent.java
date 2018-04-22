package type;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.javatuples.Triplet;

import soot.Value;

public class Intent {
	public String targetComponent;
	public String action;
	public Set<String> categories = new LinkedHashSet<String>();
	public Set<Triplet<String,String,String>> extras = new LinkedHashSet<Triplet<String,String,String>>();
	
	public Intent() {
        super();
    }
	
    public Intent(Intent intent) {
    		if (intent.targetComponent != null)
            this.targetComponent = new String(intent.targetComponent);
    		else this.targetComponent = null;
    		
    		if(intent.action != null)
    			this.action = new String(intent.action);
    		else this.action = null;
    		
    		if(intent.categories != null)
    			this.categories = new LinkedHashSet<String>();
    		else this.categories = null;
    	
    	    if(intent.extras != null)
    			this.extras = new LinkedHashSet<>(intent.extras);
    		else this.extras = null;

    }

    public Intent(String targetComponent, String action, Set<String> categories, Set<Triplet<String,String,String>> extras) {
    		this.targetComponent = targetComponent;
    		this.action = action;
    		this.categories = categories;
    		this.extras = extras;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Intent intent = (Intent) o;

        if (extras != null ? !extras.equals(intent.extras) : intent.extras != null) return false;
        if (action != null ? !action.equals(intent.action) : intent.action != null) return false;
        if (targetComponent != null ? !targetComponent.equals(intent.targetComponent) : intent.targetComponent != null)
            return false;
        return categories != null ? categories.equals(intent.categories) : intent.categories == null;
    }

    @Override
    public int hashCode() {
    		int result = extras != null ? extras.hashCode() : 0;
        result = 31 * result + (action != null ? action.hashCode() : 0);
        result = 31 * result + (targetComponent != null ? targetComponent.hashCode() : 0);
        result = 31 * result + (categories != null ? categories.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Intent{" +
        			"targetComponent='" + targetComponent + '\'' +
        		    ", action=" + action + '\'' +
        		    ", categories=" + categories + '\'' +
                ", extras=" + extras + '\'' +
                '}';
    }
}
