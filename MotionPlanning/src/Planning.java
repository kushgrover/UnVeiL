import net.sf.javabdd.BDD;
import net.sf.javabdd.BDD.BDDIterator;
import settings.Initialize;
import settings.PlanningSettings;
import modules.RRG;
import modules.learnAskExperiments.DefaultExperiment;

import java.io.OutputStream;
import java.io.PrintStream;
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
        
        
        // for output of advice
        int[] adviceSampled 						= new int[50];
        for(int k=0 ;k<50; k++) 
        {
        	adviceSampled[k]						= 0;
        }
        
        // for calculating time of different parts
        double samplingTime = 0, pathTime = 0, learnTime = 0, processStartTime = 0, adviceTime = 0;
        
        int iterationNumber					= 0;
    	BDD transition = null, fromStates = null, toStates = null;
        boolean computePath 				= false;
        List<DefaultEdge> finalPath 		= new ArrayList<DefaultEdge>();
        boolean needNewAdvice = true;
        ArrayList<BDD> advice = null;
        
        

        while(true)  //until the property is satisfied
        {
        	if(iterationNumber==300) {
        		rrg.discretization.printDiscretization();
        		rrg.discretization.printFrontiers();
        		break;
        	}
        	System.out.println("Iter num: " + iterationNumber);
        	
        	// Don't output random things
//        	System.setOut(new PrintStream(OutputStream.nullOutputStream()));
        	
        	// Sample transitions <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        	processStartTime = System.nanoTime();
        	if(needNewAdvice) 
        	{
        		advice	= exper.getAdvice(currentStates);
        		needNewAdvice = false;
        		
        		
//        		System.out.println("Advice length: " + advice.size());
//        		if(iterationNumber >= 250) {
//        			advice	= exper.getAdvice(currentStates);
//            		
//	        		BDD temp;
//	        		for(int i=0; i<advice.size();i++) {
//	        			System.out.println("\nAdvice: " + i + "*******************");
//	        			BDDIterator it = advice.get(i).iterator(ProductAutomaton.allPreSystemVars());
//	        			while(it.hasNext()) {
//	        				temp = (BDD) it.next();
//	        				rrg.printAPList(temp);
//	        			}
//	        		}
//        		}
        	}
        	adviceTime += System.nanoTime() - processStartTime;
        	
        	processStartTime = System.nanoTime();
        	
//        	---------------------------------------------------------------------------------------
//        	---------------------------------------------------------------------------------------
        	
//        	=============== Better use of advice =================
        	transition = rrg.sample(advice, productAutomaton);
        	
//        	---------------------------------------------------------------------------------------
        			
//        	=============== Naive use of advice ================		
//        	int currentPathLength			= 1;
//        	while(currentPathLength < advice.size()) //First try to sample from the advice
//        	{
//        		
//        		fromStates					= currentStates.and(advice.get(currentPathLength));
//        		toStates 					= advice.get(currentPathLength-1);
//
//        		transition					= rrg.sample(productAutomaton.removeAllExceptPreSystemVars(fromStates),productAutomaton.removeAllExceptPreSystemVars(toStates), productAutomaton);
//        		if(transition == null) {
//        			currentPathLength++;
//        			continue;
//        		}
//        		else {
//        			adviceSampled[currentPathLength - 1]++;
//        			break;
//        		}
//        	}
        	
//        	---------------------------------------------------------------------------------------
        	
//        	=============== No advice ================
//        	transition = null;

//        	---------------------------------------------------------------------------------------
//        	---------------------------------------------------------------------------------------
        	
//        	if(transition == null || transition.isZero()) // sample anywhere
//        	{
//        		transition					= rrg.sampleRandomly(productAutomaton);
//        		rrg.countSinceLastMove++;
//        		
//        	}
        	
        	if(transition == null) // if transition is still null
        	{
        		System.out.println("Something wrong happened O.o");
        		continue;
        	}
        	samplingTime += System.nanoTime() - processStartTime;
//        	>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        	
        	
        	// if the sampled transition is accepting, path checking will happen
        	if(productAutomaton.isAcceptingTransition(transition)) {
        		computePath = true;
        	}
        	

        	// Learning <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        	processStartTime 				= System.nanoTime();
        	BDDIterator ite 				= transition.iterator(ProductAutomaton.allSystemVars());
        	while(ite.hasNext())
        	{
        		transition 					= (BDD) ite.next();
        		//if transition has been sampled before or transition is a self loop in the abstraction, skip learning
        		if(transition.and(productAutomaton.sampledTransitions).isZero() && ! productAutomaton.removeAllExceptPreSystemVars(transition).equals(productAutomaton.changePostSystemVarsToPreSystemVars(productAutomaton.removeAllExceptPostSystemVars(transition))))
            	{
        			exper.learn(transition);
            		currentStates 				= currentStates.or(productAutomaton.getSecondStateSystem(transition));	//update the set of current states
            		
            		needNewAdvice = true; 
            	}
        	}
        	learnTime 					+= System.nanoTime() - processStartTime;
        	//        	>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        	
        	// If an accepting transition was sampled, check for path <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        	processStartTime				= System.nanoTime();
        	if(computePath) 
        	{
        		// computing the path
        		ArrayList<BDD> path		= productAutomaton.findAcceptingPath();
        		if(path	!= null) 
            	{
        			System.out.println("\nPath found in the abstraction: ");
            		productAutomaton.printPath(path);
            		
            		// Lifing the path to RRG graph
                	finalPath = rrg.liftPath(path);
            		break;
            	}
        	}
        	pathTime					+= System.nanoTime() - processStartTime;
//        	>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        	
//        	computePath = false;
        	iterationNumber++;
        }
        
    	productAutomaton.createDot(iterationNumber);
        Initialize.getFactory().done();
        double totalTime = System.nanoTime() - startTime;

        float pathLength = rrg.plotGraph(finalPath);
        System.out.println("No of frontiers update: " + rrg.numOfFrontierUpdates);
        // Output
		System.out.println("\n\nTotal sampled points = " + rrg.totalSampledPoints);	
		System.out.println("\nTotal useful sampled points = " + rrg.totalPoints);
		
		int adviceSamples = 0;
		for(int k=0; k<10; k++) 
		{
			adviceSamples += rrg.adviceSampled[k];
		}
		
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
