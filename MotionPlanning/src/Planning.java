import net.sf.javabdd.BDD;
import net.sf.javabdd.BDD.BDDIterator;
import modules.DefaultExperiment;
import modules.PlanningSettings;
import modules.motionPlanner.Environment;
import modules.motionPlanner.RRG;
import transitionSystem.Initialize;
import transitionSystem.ProductAutomaton;

import java.io.OutputStream;
import java.io.PrintStream;
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
		// Don't output random things
    	PrintStream out = System.out;
    	
		//initialize everything
		double startTime 					= System.nanoTime();
		
		new PlanningSettings();
        Initialize initialize				= new Initialize();
        
//    	System.setOut(new PrintStream(OutputStream.nullOutputStream()));

        Environment env 					= initialize.getEnvironment();
        RRG rrg 							= initialize.getRRG();
        Label label							= Environment.getLabelling();
        ProductAutomaton productAutomaton	= initialize.getProductAutomaton();
        
        DefaultExperiment exper				= new DefaultExperiment(productAutomaton);
        
        BDD initStateSystem					= label.getLabel(env.getInit());
        rrg.setStartingPoint(env.getInit());
        productAutomaton.setInitState(productAutomaton.getInitStates().and(initStateSystem)); // and for init state in the product automaton
        BDD currentStates					= initStateSystem.id();
        
        double initializationTime 			= System.nanoTime() - startTime;
        //-------------------------------------------------------------------        
        
        // loop of the main algo starts here
        double samplingTime = 0, preTimeSampling = 0, pathStartTime = 0, pathTime = 0, learnStartTime = 0, learnTime = 0;
        int iterationNumber					= 0;
        boolean computePath 				= false;
        
//    	System.setOut(out);


        while(true)  //until the property is satisfied
        {
        	// Don't output random things
        	System.setOut(new PrintStream(OutputStream.nullOutputStream()));
        	
        	
        	ArrayList<BDD> reachableStates	= exper.ask(currentStates);
        	int currentPathLength			= 1;
        	
        	BDD transition = null, fromStates = null, toStates = null;
        	
        	while(currentPathLength < reachableStates.size()) 
        	{
        		fromStates					= currentStates.and(reachableStates.get(currentPathLength));
        		toStates 					= reachableStates.get(currentPathLength-1);

        		preTimeSampling 			= System.nanoTime();
        		transition					= rrg.sample(productAutomaton.removeAllExceptPreSystemVars(fromStates),productAutomaton.removeAllExceptPreSystemVars(toStates), productAutomaton);
        		samplingTime				+= System.nanoTime() - preTimeSampling;
        		if(transition == null) { currentPathLength++; }
        	}	
        	if(transition == null) 
        	{
        		preTimeSampling 			= System.nanoTime();
        		transition					= rrg.sample(currentStates, productAutomaton);
        		samplingTime				+= System.nanoTime() - preTimeSampling;
        	}
        	if(transition == null) 
        	{
        		preTimeSampling 			= System.nanoTime();
        		transition					= rrg.sample(productAutomaton);
        		samplingTime				+= System.nanoTime() - preTimeSampling;
        	}
        	if(transition == null) 
        	{
        		System.out.println("I am SCREWED!");
        		break;
        	}
        	
        	if(productAutomaton.isAcceptingTransition(transition)) {
        		computePath = true;
        	}
        	
        	BDDIterator ite 				= transition.iterator(ProductAutomaton.allSystemVars());

        	while(ite.hasNext())
        	{
            	learnStartTime 				= System.nanoTime();
        		transition 					= (BDD) ite.next();
        		if(! transition.and(productAutomaton.sampledTransitions).isZero()) 
            	{
            		learnTime 					+= System.nanoTime() - learnStartTime;
            		continue;
            	}
        		exper.learn(productAutomaton.getFirstStateSystem(transition), productAutomaton.getSecondStateSystem(transition));
        		currentStates 				= currentStates.or(productAutomaton.getSecondStateSystem(transition));
            	learnTime 					+= System.nanoTime() - learnStartTime;
        	}
        	
        	pathStartTime					= System.nanoTime();
        	
        	// Don't output random things
        	System.setOut(out);

        	if(computePath) 
        	{
        		ArrayList<BDD> path				= productAutomaton.findAcceptingPath();
        		if(path	!= null) 
            	{
        			System.out.println("\nPath found in the abstraction: ");
            		productAutomaton.printPath(path);
            		break;
            	}
        	}
        	pathTime						+= System.nanoTime() - pathStartTime;
        	
        	computePath 					= false;
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
