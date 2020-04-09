package modules;

import net.sf.javabdd.BDD;

public interface Laboratory
{
//	ProductAutomaton productAutomaton;

	public void learn(BDD fromState, BDD toState) throws Exception;
	
	public BDD ask(BDD startingStates);
}
