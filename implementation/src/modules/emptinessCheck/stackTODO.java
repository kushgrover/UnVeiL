package modules.emptinessCheck;

import net.sf.javabdd.BDD;
import settings.PlanningException;

public class stackTODO {
	todoNode top = null;
	
	public void push(BDD state, BDD transitions) throws PlanningException {
		todoNode newNode=new todoNode(state, transitions); 
		newNode.setPrevious(top);
		top=newNode;
	}
	
	public todoNode pop() {
		todoNode temp=top;
		top=top.previous;
		return temp;
	}
	
	public BDD getOneSuccFromTop() {
		return top.getOneTransition();
	}
	
	public todoNode getTop() {
		return top;
	}
	
	public boolean isEmpty() {
		return top == null;
	}
	
}
