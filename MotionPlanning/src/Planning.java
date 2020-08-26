import net.sf.javabdd.BDD;
import net.sf.javabdd.BDD.BDDIterator;
import settings.Initialize;
import settings.PlanningSettings;
import modules.RRG;
import modules.learnAskExperiments.DefaultExperiment;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

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
    	
		// Initialize everything-------------------------------------------
		double startTime 					= System.nanoTime();
		
		// use default values for everything
		new PlanningSettings();
		
		// read the files and initialize everything
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
        productAutomaton.setInitState(productAutomaton.getInitStates().and(initStateSystem)); // and for init state in the product automaton
        
        
        BDD currentStates					= initStateSystem.id(); //it stores the set of states in the abstraction we have seen till now
        
        double initializationTime 			= System.nanoTime() - startTime;
        //-------------------------------------------------------------------
        
        
        
        
        
        // loop of the main algo starts here
        double samplingTime = 0, preTimeSampling = 0, pathStartTime = 0, pathTime = 0, learnStartTime = 0, learnTime = 0;
        int iterationNumber					= 0;
        boolean computePath 				= false;
        PrintStream out = System.out;		// Don't output random things from libraries
        
        while(true)  //until the property is satisfied
        {
        	// Don't output random things
        	System.setOut(new PrintStream(OutputStream.nullOutputStream()));
        	
        	
        	// Sample transitions
        	ArrayList<BDD> reachableStates	= exper.ask(currentStates);
        	int currentPathLength			= 1;
        	
        	BDD transition = null, fromStates = null, toStates = null;
        	
        	while(currentPathLength < reachableStates.size()) //First try to sample from the advice
        	{
        		fromStates					= currentStates.and(reachableStates.get(currentPathLength));
        		toStates 					= reachableStates.get(currentPathLength-1);

        		preTimeSampling 			= System.nanoTime();
        		transition					= rrg.sample(productAutomaton.removeAllExceptPreSystemVars(fromStates),productAutomaton.removeAllExceptPreSystemVars(toStates), productAutomaton);
        		samplingTime				+= System.nanoTime() - preTimeSampling;
        		if(transition == null) { currentPathLength++; }
        	}	
        	
        	if(transition == null) // sample from the set of current states
        	{
        		preTimeSampling 			= System.nanoTime();
        		transition					= rrg.sample(currentStates, productAutomaton);
        		samplingTime				+= System.nanoTime() - preTimeSampling;
        	}
        	
        	if(transition == null) // sample anywhere
        	{
        		preTimeSampling 			= System.nanoTime();
        		transition					= rrg.sample(productAutomaton);
        		samplingTime				+= System.nanoTime() - preTimeSampling;
        	}
        	
        	if(transition == null) // if transition is still null
        	{
        		System.out.println("Something wrong happened O.o");
        		break;
        	}
        	//-----------------------------------------------------------------------------
        	
        	
        	// if the sampled transition is accepting, path checking will happen
        	if(productAutomaton.isAcceptingTransition(transition)) {
        		computePath = true;
        	}
        	

        	
        	
        	// Learning ------------------------------------------------------
        	BDDIterator ite 				= transition.iterator(ProductAutomaton.allSystemVars());
        	while(ite.hasNext())
        	{
            	learnStartTime 				= System.nanoTime();
        		transition 					= (BDD) ite.next();
        		
        		//if transition has been sampled before, skip learning
        		if(! transition.and(productAutomaton.sampledTransitions).isZero())
            	{
            		learnTime 					+= System.nanoTime() - learnStartTime;
            		continue;
            	}
        		
        		//learning happens here
        		exper.learn(productAutomaton.getFirstStateSystem(transition), productAutomaton.getSecondStateSystem(transition));
        		currentStates 				= currentStates.or(productAutomaton.getSecondStateSystem(transition));	//update the set of current states
            	learnTime 					+= System.nanoTime() - learnStartTime;
        	}
        	//-------------------------------------------------------------------------
        	
        	
        	// Don't output random things
        	System.setOut(out);
        	

        	
        	// If an accepting transition was sampled, check for path
        	pathStartTime				= System.nanoTime();
        	if(computePath) 
        	{
        		// computing the path
        		ArrayList<BDD> path		= productAutomaton.findAcceptingPath();
        		if(path	!= null) 
            	{
        			System.out.println("\nPath found in the abstraction: ");
            		productAutomaton.printPath(path);
            		
            		// Lifing the path to RRG graph
                	rrg.liftPath(path);
            		break;
            	}
        	}
        	pathTime					+= System.nanoTime() - pathStartTime;
        	//------------------------------------------------------------------
        	
        	
        	
        	
        	computePath = false;
        	iterationNumber++;
        }
//    	productAutomaton.createDot(iterationNumber);

        Initialize.getFactory().done();
        double totalTime = System.nanoTime() - startTime;

        rrg.plotGraph();

        // Output time
		System.out.println("\n\nTotal sampled transitions = " + iterationNumber);
		System.out.print("\n\nTotal time taken (in ms):");
        System.out.println(totalTime / 1000000);
        System.out.print("\nInitialization time (in ms):");
        System.out.println((initializationTime) / 1000000);
        System.out.print("Sampling time (in ms):");
        System.out.println(samplingTime / 1000000);
        System.out.print("Path checking time (in ms):");
        System.out.println(pathTime / 1000000);
        System.out.print("Learning time (in ms):");
        System.out.println(learnTime/ 1000000);
        System.out.print("Time taken other than these things (in ms):");
        System.out.println((totalTime - initializationTime - samplingTime - pathTime - learnTime) / 1000000);
        
	}

	
	
}
