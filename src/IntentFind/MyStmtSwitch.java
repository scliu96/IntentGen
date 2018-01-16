package IntentFind;
import soot.Body;
import soot.Value;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;

public class MyStmtSwitch extends AbstractStmtSwitch{
	private Body body;
	private MyIntent intent;
	
	public void caseAssignStmt(AssignStmt stmt){
		if(stmt.containsInvokeExpr()){
			InvokeExpr invokeExpr = stmt.getInvokeExpr();
			/*
			System.out.println("1:" + stmt.toString());
			System.out.println("2:" + invokeExpr.toString());
			System.out.println("3:" + invokeExpr.getMethod().toString());
			System.out.println("4:" + invokeExpr.getType().toString());
			System.out.println("5:" + invokeExpr.getArgs().toString());
			*/
			System.out.println(this.body.toString());
			for(Value v : invokeExpr.getArgs()){
				if(v.getType().toString().equals("java.lang.String")){
					this.intent.proAdd(v.toString(),invokeExpr.getType());
					break;
				}
			}
		}
	}
	
	public void inMyIntent(Body b, MyIntent in){
		this.body = b;
		this.intent = in;
	}
	
	public MyIntent outMyIntent(){
		return this.intent;
	}
}
