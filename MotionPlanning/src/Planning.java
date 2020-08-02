import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BuDDyFactory;

import modules.DefaultExperiment;
import modules.PlanningSettings;
import modules.motionPlanner.Environment;
import modules.motionPlanner.RRG;
import transitionSystem.Initialize;
import transitionSystem.MarkovChain;
import transitionSystem.ProductAutomaton;
import transitionSystem.TSparser.MarkovChainParser;

import java.util.ArrayList;

import labelling.Label;

/**
 * @author kush
 *
 */
public class Planning
{
	
	public static void main(String[] args) throws Exception
	{
		
		double startTime 					= System.nanoTime();
		
//		MarkovChain mc=MarkovChainParser.markovChainParser(args[0]);
//		mc.checkMarkovChain();
		new PlanningSettings();
        Initialize initialize				= new Initialize();
        Environment env 					= initialize.getEnvironment();
        RRG rrg 							= initialize.getRRG();
        Label label							= Environment.getLabelling();
        ProductAutomaton productAutomaton	= initialize.getProductAutomaton();
        
        DefaultExperiment exper				= new DefaultExperiment(productAutomaton);
        
        BDD initStateSystem					= label.getLabel(env.getInit());
        rrg.setStartingPoint(env.getInit());
        productAutomaton.setInitState(initStateSystem);
        BDD currentStates					= initStateSystem.id();
        
        double timeForSampling = 0, preTimeSampling = 0, postTimeSampling = 0, pathTime = 0;
        int iterationNumber					= 0;
        
        
        while(true)  //until the property is satisfied
        {
        	ArrayList<BDD> reachableStates	= exper.ask(currentStates);
        	int currentPathLength			= 1;
        	
        	BDD transition = null, fromStates = null, toStates = null;
        	
        	while(currentPathLength < reachableStates.size()) 
        	{
        		fromStates					= currentStates.and(reachableStates.get(currentPathLength));
        		toStates 					= reachableStates.get(currentPathLength-1);
        		preTimeSampling 			= System.nanoTime();
        		transition					= rrg.sample(productAutomaton.removeAllExceptPreSystemVars(fromStates),productAutomaton.removeAllExceptPreSystemVars(toStates), productAutomaton);
        		postTimeSampling 			= System.nanoTime();
        		timeForSampling				+= postTimeSampling-preTimeSampling;
        		if(transition != null) 
        		{
        			break;
        		} else 
        		{
        			currentPathLength++;
        		}
        	}	
        	if(transition == null) 
        	{
        		preTimeSampling 			= System.nanoTime();
        		transition					= rrg.sample(currentStates, productAutomaton);
        		postTimeSampling 			= System.nanoTime();
        		timeForSampling				+= postTimeSampling-preTimeSampling;
        	}
        	if(transition == null) 
        	{
        		preTimeSampling 			= System.nanoTime();
        		transition					= rrg.sample(productAutomaton);
        		postTimeSampling 			= System.nanoTime();
        		timeForSampling				+= postTimeSampling-preTimeSampling;
//        		System.out.println("I am SCREWED!");
//        		break;
        	}
        	if(transition == null) 
        	{
        		System.out.println("I am SCREWED!");
        		break;
        	}
        	currentStates 					= currentStates.or(productAutomaton.getSecondStateSystem(transition));
        	transition						= exper.learn(productAutomaton.getFirstStateSystem(transition), productAutomaton.getSecondStateSystem(transition));
        	double pathStartTime			= System.nanoTime();
        	ArrayList<BDD> path				= productAutomaton.findAcceptingPath();
        	double pathEndTime				= System.nanoTime();
        	pathTime						+= pathEndTime-pathStartTime;
        	
        	if(path	!= null) 
        	{
        		productAutomaton.printPath(path);
        		System.out.println("Yay!!!!!");
        		break;
        	}
//        	initialize.getProductAutomaton().createDot(iterationNumber);
        	iterationNumber++;
        }
        
        Initialize.getFactory().done();
		
        double endTime = System.nanoTime();
        
		System.out.println("\nTotal sampled transitions = "+iterationNumber);
		System.out.print("\n\nTotal time taken (in ms):");
        System.out.println((endTime-startTime)/1000000);
        System.out.print("Time taken sampling (in ms):");
        System.out.println(timeForSampling/1000000);
        System.out.print("Time taken other than sampling (in ms):");
        System.out.println((endTime-startTime-timeForSampling)/1000000);
        System.out.print("Path checking time (in ms):");
        System.out.println(pathTime/1000000);

	}

	
	
}
