import net.sf.javabdd.BDD;
import net.sf.javabdd.BDD.BDDIterator;
import settings.Initialize;
import settings.PlanningSettings;
import modules.RRG;
import modules.learnAskExperiments.DefaultExperiment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jgrapht.graph.DefaultEdge;

import abstraction.ProductAutomaton;
import environment.Environment;
import environment.Label;

/**
 * @author kush
 *
 */
public class Planning
{	
	public static void main(String[] args) throws Exception
	{    	
    	
		// Initialise everything <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
		double startTime					= System.nanoTime();
		
		// use default values for everything
		new PlanningSettings();
		
		// read the files and initialise everything
        Initialize initialize				= new Initialize();

        Environment env 					= initialize.getEnvironment();
        RRG rrg 							= initialize.getRRG();
        Label label							= Environment.getLabelling();
        ProductAutomaton productAutomaton	= initialize.getProductAutomaton();
        
        // new learn/ask experiment
        DefaultExperiment exper				= new DefaultExperiment(productAutomaton);
        
        //add initial point
        BDD initStateSystem					= label.getLabel(env.getInit());
        rrg.setStartingPoint(env.getInit());
        productAutomaton.setInitState(productAutomaton.getInitStates().and(initStateSystem)); // and for initial state in the product automaton
        
        
        BDD currentStates					= initStateSystem.id(); //stores the set of states in the abstraction we have seen till now
        
        double initializationTime 			= System.nanoTime() - startTime;
//      >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        
        double samplingTime = 0, pathTime = 0, learnTime = 0, processStartTime = 0, adviceTime = 0;
        
        int iterationNumber					= 0;
    	BDD transitions 					= null;
        boolean computePath 				= false;
        List<DefaultEdge> finalPath 		= new ArrayList<DefaultEdge>();
        boolean needNewAdvice 				= true;
        ArrayList<BDD> advice 				= null;

        while(true)  //until the property is satisfied
        {
        	if(iterationNumber == 1000) {
        		rrg.discretization.printDiscretization();
        		rrg.discretization.printFrontiers();
        		break;
        	}
        	System.out.println("Iter num: " + iterationNumber);
        	
        	processStartTime = System.nanoTime();
        	if(needNewAdvice) {
        		advice	= exper.getAdvice(currentStates);
        		needNewAdvice = false;
        	}
        	adviceTime += System.nanoTime() - processStartTime;
        	
        	processStartTime = System.nanoTime();
        	transitions = rrg.sample(advice);
//        	transition = null; // no advice
        	if(transitions == null) {
        		System.out.println("Something wrong happened O.o");
        		continue;
        	}
        	samplingTime += System.nanoTime() - processStartTime;
        	
        	// if the sampled transition is accepting, path checking will happen
        	if(productAutomaton.isAcceptingTransition(transitions)) computePath = true;

        	// Learning ==============
        	processStartTime 			= System.nanoTime();
        	BDDIterator ite 			= transitions.iterator(ProductAutomaton.allSystemVars());
        	while(ite.hasNext())
        	{
        		transitions 			= (BDD) ite.next();
        		//if transition has been sampled before or transition is a self loop in the abstraction, skip learning
        		if(transitions.and(productAutomaton.sampledTransitions).isZero() && ! productAutomaton.removeAllExceptPreSystemVars(transitions).equals(productAutomaton.changePostSystemVarsToPreSystemVars(productAutomaton.removeAllExceptPostSystemVars(transitions)))){
        			exper.learn(transitions);
            		currentStates 		= currentStates.or(productAutomaton.getSecondStateSystem(transitions));	//update the set of current states
            		needNewAdvice = true; 
            	}
        	}
        	learnTime 					+= System.nanoTime() - processStartTime;
        	//------------------------
        	
        	processStartTime			= System.nanoTime();
        	if(computePath) {
        		ArrayList<BDD> path		= productAutomaton.findAcceptingPath();
        		if(path	!= null) {
        			System.out.println("\nPath found in the abstraction: ");
            		productAutomaton.printPath(path);
                	finalPath = rrg.liftPath(path); //Lifing the path to RRG graph
            		break;
            	}
        	}
        	pathTime					+= System.nanoTime() - processStartTime;
//        	>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        	
        	iterationNumber++;
        }
        
    	productAutomaton.createDot(iterationNumber);
        Initialize.getFactory().done();
        double totalTime = System.nanoTime() - startTime;

        float pathLength = rrg.plotGraph(finalPath);
        
        int adviceSamples = 0;
		for(int k=0; k<10; k++) 
			adviceSamples += rrg.adviceSampled[k];
        
        System.out.println("No of frontiers update: " + rrg.numOfFrontierUpdates);
		System.out.println("\n\nTotal sampled points = " + rrg.totalSampledPoints);	
		System.out.println("\nTotal useful sampled points = " + rrg.totalPoints);
		System.out.println("Path length = " + pathLength);
		System.out.println("Sampled from advice = " + adviceSamples);
		System.out.println("\nAdvice samples: " + Arrays.toString(rrg.adviceSampled));
		System.out.print("\nTotal time taken (in ms):");
        System.out.println(totalTime / 1000000);
        System.out.print("\nInitialization time (in ms):");
        System.out.println((initializationTime) / 1000000);
        System.out.print("Sampling time (in ms):");
        System.out.println(samplingTime / 1000000);
        System.out.print("Advice time (in ms):");
        System.out.println(adviceTime / 1000000);
        System.out.print("Path checking time (in ms):");
        System.out.println(pathTime / 1000000);
        System.out.print("Learning time (in ms):");
        System.out.println(learnTime / 1000000);
        System.out.print("Moving time (in ms):");
        System.out.println(rrg.moveTime / 1000000);
        System.out.print("Time taken other than these things (in ms):");
        System.out.println((totalTime - initializationTime - samplingTime - adviceTime - pathTime - learnTime) / 1000000);    
	}
}
