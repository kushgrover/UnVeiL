package modules.emptinessCheck;

import net.sf.javabdd.BDD;

public class stackTODO {
	todoNode top;
	
	public void push(BDD state, BDD transitions) throws Exception{
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
		if(top==null) {
			return true;
		}
		return false;
	}
	
}
