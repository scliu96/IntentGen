import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import soot.Body;
import soot.PatchingChain;
import soot.SootMethod;
import soot.Unit;
import soot.ValueBox;

public class RelateValue {
	public Body body;
	public List<ValueBox> src;
	public List<ValueBox> rel;
	
	public RelateValue(Body b){
		System.out.println(b.toString());
		this.body = b;
		this.src = new ArrayList<ValueBox>();
		this.rel = new ArrayList<ValueBox>();
		
		for(ValueBox v : b.getDefBoxes()){
			System.out.println(v);
			String temp = v.getValue().getType().toString();
			if(temp.equals("android.content.Intent") || temp.equals("android.os.Bundle"))
				this.src.add(v);
		}
		
		PatchingChain<Unit> units = b.getUnits();
		for(Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();){
			final Unit u = iter.next();
			for(ValueBox v1 : u.getUseBoxes())
				for(ValueBox v2 : this.src)
					if(v2.getValue().equals(v1.getValue())){
						for(ValueBox v : u.getDefBoxes()){
							String temp = v.getValue().getType().toString();
							if(temp.equals("android.content.Intent") || temp.equals("android.os.Bundle"))
								this.src.add(v);
							else this.rel.add(v);
						}
						break;
					}
		}
	}
	
	public void print(){
		System.out.println(this.body.getMethod());
		System.out.println("src:");
		for(ValueBox v : this.src){
			System.out.print(v.getValue()+" ");
			System.out.println(v.getValue().getType());
		}
		System.out.println("rel:");
		for(ValueBox v : this.rel){
			System.out.print(v.getValue()+" ");
			System.out.println(v.getValue().getType());
		}
		System.out.println("zzz");
	}
}
