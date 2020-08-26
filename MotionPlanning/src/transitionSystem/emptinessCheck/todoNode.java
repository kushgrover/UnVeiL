package transitionSystem.emptinessCheck;

import net.sf.javabdd.BDD;
import transitionSystem.ProductAutomaton;
import transitionSystem.exceptions.StateException;

public class todoNode {
	BDD state, transitions;
	todoNode previous=null;
	
	todoNode(BDD state, BDD transitions) throws Exception{
		checkValidity(transitions);
		this.state=state;
		this.transitions=transitions;
	}

	private void checkValidity(BDD transitions) throws Exception {
		if(transitions.satCount(ProductAutomaton.allPreVars())>1) {
			throw new StateException("More than one state");
		}
	}
	
	public BDD getTransitions() {
		return transitions;
	}
	
	public BDD getState() {
		return state;
	}
	
	public BDD getOneTransition() {
		BDD oneSucc=transitions.satOne(ProductAutomaton.allVars(),true);
		transitions=transitions.and(oneSucc.not());
		return oneSucc;
	}
	
	public void setPrevious(todoNode previous) {
		this.previous=previous;
	}
}
