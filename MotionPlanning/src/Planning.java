import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BuDDyFactory;

import modules.DefaultExperiment;
import transitionSystem.Initialize;
import transitionSystem.MarkovChain;
import transitionSystem.ProductAutomaton;
import transitionSystem.TSparser.MarkovChainParser;

import java.util.ArrayList;

/**
 * @author kush
 *
 */
public class Planning{

	public static void main(String[] args) throws Exception{
		
		final boolean verbose=true;
		int levelOfTransitions=4;
		int threshold=20;
		final int cacheSizeForBDDFactory=10000;
		BDDFactory factory= BuDDyFactory.init(20,cacheSizeForBDDFactory);
		
		MarkovChain mc=MarkovChainParser.markovChainParser();
		mc.checkMarkovChain();
		
		// Creates propertyBDD and initializes productAutomaton
        Initialize initialize= new Initialize(factory, threshold, levelOfTransitions, mc.getApList());
        DefaultExperiment exper1=new DefaultExperiment(initialize.getProductAutomaton());
        
		int numVars[]=ProductAutomaton.numVars;
        
        BDD currentStates=mc.getBDDForState(factory, mc.getInitialState(), numVars[0]+numVars[1]+numVars[2]);

        int fromState=mc.getInitialState();
        int toState=mc.sampleFromState(fromState);
        BDD fromStateBDD=mc.getBDDForState(factory, fromState, numVars[0]+numVars[1]+numVars[2]);
        BDD toStateBDD=mc.getBDDForState(factory, toState, numVars[0]+numVars[1]+numVars[2]);
        
        
        for(int i=0;i<100;i++) {
        	exper1.learn(fromStateBDD, toStateBDD);
        	fromState=toState;
        	toState=mc.sampleFromState(fromState);
        	fromStateBDD=mc.getBDDForState(factory, fromState, numVars[0]+numVars[1]+numVars[2]);
        	toStateBDD=mc.getBDDForState(factory, toState, numVars[0]+numVars[1]+numVars[2]);
        }

        initialize.getProductAutomaton().createDot(0);
        exper1.ask(factory.ithVar(8));
		factory.done();
	}
}
