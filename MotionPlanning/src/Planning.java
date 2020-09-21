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
        int[] advice 						= new int[50];
        for(int k=0 ;k<50; k++) 
        {
        	advice[k]						= 0;
        }
        
        // for calculating time of different parts
        double samplingTime = 0, pathTime = 0, learnStartTime = 0, learnTime = 0, processStartTime = 0, adviceTime = 0;
        
        int iterationNumber					= 0;
    	BDD transition = null, fromStates = null, toStates = null;
        boolean computePath 				= false;
        List<DefaultEdge> finalPath 		= new ArrayList<DefaultEdge>();
        boolean needNewAdvice = true;
        ArrayList<BDD> reachableStates = null;
        while(true)  //until the property is satisfied
        {

        	// Don't output random things
//        	System.setOut(new PrintStream(OutputStream.nullOutputStream()));
        	
        	// Sample transitions <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        	processStartTime = System.nanoTime();
        	if(needNewAdvice) 
        	{
        		reachableStates	= exper.advice(currentStates);
        		needNewAdvice = false;
        	}
        	adviceTime += System.nanoTime() - processStartTime;
        	
        	processStartTime = System.nanoTime();
        	int currentPathLength			= 1;
        	while(currentPathLength < reachableStates.size()) //First try to sample from the advice
        	{
        		
        		fromStates					= currentStates.and(reachableStates.get(currentPathLength));
        		toStates 					= reachableStates.get(currentPathLength-1);

        		transition					= rrg.sample(productAutomaton.removeAllExceptPreSystemVars(fromStates),productAutomaton.removeAllExceptPreSystemVars(toStates), productAutomaton);
        		if(transition == null) {
        			currentPathLength++;
        			continue;
        		}
        		else {
        			advice[currentPathLength - 1]++;
        			break;
        		}
        	}
        	
        	if(transition == null) // sample anywhere
        	{
        		transition					= rrg.sampleRandomly(productAutomaton);
        	}
        	
        	if(transition == null) // if transition is still null
        	{
        		System.out.println("Something wrong happened O.o");
        		break;
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
        		if(! transition.and(productAutomaton.sampledTransitions).isZero() || productAutomaton.removeAllExceptPreSystemVars(transition).equals(productAutomaton.changePostSystemVarsToPreSystemVars(productAutomaton.removeAllExceptPostSystemVars(transition))))
            	{
            		continue;
            	}
        		
        		//learning happens here
        		exper.learn(productAutomaton.getFirstStateSystem(transition), productAutomaton.getSecondStateSystem(transition));
        		currentStates 				= currentStates.or(productAutomaton.getSecondStateSystem(transition));	//update the set of current states
        		
        		needNewAdvice = true; 
        	}
        	learnTime 					+= System.nanoTime() - processStartTime;
        	//        	>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        	
        	// Don't output random things
//        	System.setOut(System.out);
        	
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
        	
        	computePath = false;
        	iterationNumber++;
        }
        
//    	productAutomaton.createDot(iterationNumber);
        Initialize.getFactory().done();
        double totalTime = System.nanoTime() - startTime;

        rrg.plotGraph(finalPath);

        // Output
		System.out.println("Total sampled points = " + rrg.totalSampledPoints);	
		System.out.println("\nTotal useful sampled points = " + iterationNumber);
		
		int adviceSamples = 0;
		for(int k=0; k<50; k++) 
		{
			adviceSamples += advice[k];
		}
		
		System.out.println("Sampled from advice = " + adviceSamples);
		System.out.println("\nAdvice samples: " + Arrays.toString(advice));
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
        System.out.print("Time taken other than these things (in ms):");
        System.out.println((totalTime - initializationTime - samplingTime - adviceTime - pathTime - learnTime) / 1000000);    
	}
}
