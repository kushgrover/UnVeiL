package modules.learnAskExperiments;

import java.util.ArrayList;

import abstraction.ProductAutomaton;
import net.sf.javabdd.BDD;

public interface Experiments
{
//	ProductAutomaton productAutomaton;

	public BDD learn(BDD fromState, BDD toState) throws Exception;
	
	public ArrayList<BDD> advice(BDD startingStates) throws Exception;
	
	public ProductAutomaton getProductAutomaton();
}
