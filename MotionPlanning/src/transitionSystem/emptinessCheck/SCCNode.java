package transitionSystem.emptinessCheck;

import java.util.ArrayList;

import net.sf.javabdd.BDD;




public class SCCNode {

	int root;
	ArrayList<Integer> last,acc;
	BDD rem;
	SCCNode previous=null;
	
	public SCCNode(int root, ArrayList<Integer> last, ArrayList<Integer> acc, BDD rem) throws Exception{
		checkRem(rem);
		this.root=root;
		this.last=last;
		this.acc=acc;
		this.rem=rem;
	}
	
	public int getRoot() {
		return root;
	}
	
	public ArrayList<Integer> getLast() {
		return last;
	}
	
	public ArrayList<Integer> getAcc() {
		return acc;
	}
	
	public BDD getRem() {
		return rem;
	}
	
	public void setRoot(int root) {
		this.root=root;
	}
	
	public void setLast(ArrayList<Integer> last) {
		this.last=last;
	}
	
	public void setAcc(ArrayList<Integer> acc) {
		this.acc=acc;
	}
	
	public void setRem(BDD rem) {
		checkRem(rem);
		this.rem=rem;
	}
	
	private void checkRem(BDD rem) {
		// TODO Auto-generated method stub
		
	}

	public void setPrevious(SCCNode previous) {
		this.previous=previous;
	}
}
