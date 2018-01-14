import soot.Value;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;

public class MyStmtSwitch extends AbstractStmtSwitch{
	MyIntent intent;
	
	public void caseAssignStmt(AssignStmt stmt){
		if(stmt.containsInvokeExpr()){
			InvokeExpr invokeExpr = stmt.getInvokeExpr();
			//System.out.println("arg");
			for(Value v : invokeExpr.getArgs()){
				if(v.getType().toString().equals("java.lang.String"))
					this.intent.proAdd(v.toString());
				//System.out.println(v.toString());
			}
		}
	}
	
	public void inMyIntent(MyIntent in){
		this.intent = in;
	}
	
	public MyIntent outMyIntent(){
		return this.intent;
	}
}
