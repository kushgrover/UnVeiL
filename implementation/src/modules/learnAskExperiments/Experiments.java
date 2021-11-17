package modules.learnAskExperiments;

import java.util.ArrayList;

import abstraction.ProductAutomaton;
import net.sf.javabdd.BDD;
import settings.PlanningException;

public interface Experiments
{
//	ProductAutomaton productAutomaton;

	void learn(BDD transition) throws PlanningException;
	
	ArrayList<BDD> getAdvice(BDD startingStates) throws PlanningException;
	
	ProductAutomaton getProductAutomaton();

	
}
