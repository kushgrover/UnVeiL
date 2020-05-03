package modules;

import java.util.ArrayList;

import net.sf.javabdd.BDD;
import transitionSystem.ProductAutomaton;

public interface Experiments
{
//	ProductAutomaton productAutomaton;

	public BDD learn(BDD fromState, BDD toState) throws Exception;
	
	public ArrayList<BDD> ask(BDD startingStates) throws Exception;
	
	public ProductAutomaton getProductAutomaton();
}
