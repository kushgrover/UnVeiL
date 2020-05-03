package transitionSystem.emptinessCheck;

import java.util.ArrayList;

import net.sf.javabdd.BDD;

public class stackSCC {
	SCCNode top;
		
	public void push(int root, ArrayList<Integer> last, ArrayList<Integer> acc, BDD rem) throws Exception{
		SCCNode newNode=new SCCNode(root,last,acc,rem);
		newNode.setPrevious(top);
		top=newNode;
	}
		
	protected SCCNode pop() {
		SCCNode temp=top;
		top=top.previous;
		return temp;
	}
	
	public SCCNode getTop() {
		return top;
	}
	
	public boolean isEmpty() {
		if(top==null) {
			return true;
		}
		return false;
	}
}
