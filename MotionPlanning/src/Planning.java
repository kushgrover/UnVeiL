import net.sf.javabdd.BDD;
import net.sf.javabdd.BDD.BDDIterator;
import settings.Initialize;
import settings.PlanningException;
import settings.PlanningSettings;
import modules.Discretization;
import modules.RRG;
import modules.UnknownRRG;
import modules.learnAskExperiments.DefaultExperiment;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jgrapht.alg.util.Pair;
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
	RRG rrg;
	ProductAutomaton productAutomaton;
	DefaultExperiment exper;
	Environment env;
	
	BDD currentStates;
	
	int iterationNumber 	= 0;
	double initTime 		= 0;
	double beginTime 		= 0;
	double startTime 		= 0;
	double samplingTime 	= 0;
	double pathTime 		= 0;
	double learnTime 		= 0;
	double explTime 		= 0;
	double adviceTime 		= 0;
    boolean needNewAdvice 	= true;
    ArrayList<BDD> advice 	= null;
    float moveLength, pathLength;
    List<DefaultEdge> finalPath = new ArrayList<DefaultEdge>();

    /**
     * initialize everything
     * @throws Exception
     */
	public Planning() throws Exception 
	{
		beginTime				= System.nanoTime();
		
		Initialize initialize	= new Initialize();// read the files and initialise everything
        rrg 					= initialize.getRRG();
        productAutomaton		= initialize.getProductAutomaton();
        exper					= new DefaultExperiment(productAutomaton); // learn and advce procedures
        env 					= initialize.getEnvironment();
        
        Label label				= Environment.getLabelling();
        BDD initStateSystem		= label.getLabel(env.getInit());
        currentStates			= initStateSystem.id(); //stores the set of states in the abstraction we have seen till now
        
        rrg.setStartingPoint(env.getInit());
        productAutomaton.setInitState(productAutomaton.getInitStates().and(initStateSystem)); // and for initial state in the product automaton
        
        initTime 				= System.nanoTime() - beginTime;
	}
	
	/**
	 * outputs the results and times
	 * @throws Exception
	 */
	void printOutput() throws Exception 
	{
//    	productAutomaton.createDot(iterationNumber);
		Pair<Float, Float> pathLength = rrg.plotGraph(finalPath);
		if((boolean) PlanningSettings.get("firstExplThenPlan")) {
			pathLength.setFirst(moveLength);
		}
		double totalTime = System.nanoTime() - beginTime;
        
        int adviceSamples = 0;
		for( int k=0; k<10; k++ )
			adviceSamples += rrg.adviceSampled[k];
		
		System.out.println("\n\nTotal sampled points = " + rrg.totalSampledPoints);	
		System.out.println("\nTotal useful sampled points = " + rrg.totalPoints);
		System.out.println("Movement Length = " + pathLength.getFirst());
		System.out.println("Remaining path Length = " + pathLength.getSecond());
		System.out.println("Sampled from advice = " + adviceSamples);
		System.out.println("\nAdvice samples: " + Arrays.toString(rrg.adviceSampled));
		System.out.print("\nTotal time taken (in ms):");
        System.out.println(totalTime / 1000000);
        System.out.print("\nInitialization time (in ms):");
        System.out.println((initTime) / 1000000);
        System.out.print("Sampling time (in ms):");
        System.out.println(samplingTime / 1000000);
        System.out.print("Advice time (in ms):");
        System.out.println(adviceTime / 1000000);
        System.out.print("Path checking time (in ms):");
        System.out.println(pathTime / 1000000);
        System.out.print("Learning time (in ms):");
        System.out.println(learnTime / 1000000);
        System.out.print("Exploration time (in ms):");
        System.out.println(explTime / 1000000);
        System.out.print("Time taken other than these things (in ms):");
        System.out.println((totalTime - initTime - samplingTime - adviceTime - pathTime - learnTime) / 1000000);    
	}
	
	/**
	 * check if learning is required for the set of transitions
	 * @param transitions
	 * @return
	 * @throws PlanningException
	 */
	boolean needLearning(BDD transitions) throws PlanningException {
		return transitions.and(productAutomaton.sampledTransitions).isZero() && ! productAutomaton.removeAllExceptPreSystemVars(transitions).equals(productAutomaton.changePostSystemVarsToPreSystemVars(productAutomaton.removeAllExceptPostSystemVars(transitions)));
	}
	
	/**
	 * update advice if required
	 * @throws Exception
	 */
	void updateAdvice() throws Exception {
		startTime 	= System.nanoTime(); // Advice time
    	if ( needNewAdvice ) {
    		advice			= exper.getAdvice(currentStates);
    		needNewAdvice 	= false;
    	}
    	adviceTime 			+= System.nanoTime() - startTime;
	}
	
	/**
	 * sample a batch
	 * @return
	 * @throws Exception
	 */
	BDD sampleBatch(RRG rrg) throws Exception {
		startTime = System.nanoTime(); 
    	BDD transitions = rrg.sampleBatch(advice);
    	samplingTime += System.nanoTime() - startTime;
    	return transitions;
	}
	
	/**
	 * call learn procedure
	 * @param transitions
	 * @throws Exception
	 */
	void learn(BDD transitions) throws Exception {
		startTime = System.nanoTime(); 
    	BDDIterator ite = transitions.iterator(ProductAutomaton.allSystemVars());
    	while( ite.hasNext() ) 
    	{
    		transitions = (BDD) ite.next();
    		if( needLearning(transitions) ) {
    			exper.learn(transitions);
        		currentStates = currentStates.or(productAutomaton.getSecondStateSystem(transitions));
        		needNewAdvice = true;
        	}
    	}
    	learnTime += System.nanoTime() - startTime;
	}
	
	/**
	 * Do exploration and planning simultaneously
	 * @throws Exception
	 */
	public void explAndPlanTogether() throws Exception 
	{    
    	BDD transitions 					= null;
        boolean computePath 				= false;
        boolean debug 						= (boolean) PlanningSettings.get("debug");
        
        UnknownRRG urrg = (UnknownRRG) rrg;
        urrg.discretization.knowDiscretization(env, productAutomaton, env.getInit(), (float) PlanningSettings.get("sensingRadius"));
        while( true )
        {
        	if( debug ) {
	        	if( iterationNumber == 100 ) {
	        		urrg.discretization.printDiscretization();
	        		urrg.discretization.printFrontiers();
	        		break;
	        	}
        	}
        	
        	iterationNumber++;
        	System.out.println("Iter num: " + iterationNumber);
        	
        	if ( (boolean) PlanningSettings.get("useAdvice") )
        		updateAdvice();
        	transitions = sampleBatch(urrg);
        	if ( transitions == null ) 
        		continue;
        	if( productAutomaton.isAcceptingTransition(transitions) )
        		computePath = true;
        	learn(transitions);
        	
        	startTime = System.nanoTime(); 
        	if( computePath ) {
        		ArrayList<BDD> path	= productAutomaton.findAcceptingPath();
        		if( path != null ) {
        			System.out.println("\nPath found in the abstraction: ");
            		productAutomaton.printPath(path);
                	finalPath = urrg.liftPath(path); 
            		break;
            	}
        	}
        	pathTime += System.nanoTime() - startTime;
        }
        printOutput();
        System.out.print("Moving time (in ms):");
        System.out.println(urrg.moveTime / 1000000);
        Initialize.getFactory().done();
    }
	
	/**
	 * Do exploration first, then do the planning
	 * @throws Exception
	 */
	void firstExplThenPlan() throws Exception {
		Discretization discretization = new Discretization(env, (float) PlanningSettings.get("discretizationSize"));
		startTime = System.nanoTime();
		moveLength = exploreDiscretization(discretization);
		explTime = System.nanoTime() - startTime;
		
		BDD transitions = null;
        boolean computePath = true;

		while( true ){
			iterationNumber++;
        	System.out.println("Iter num: " + iterationNumber);
        	
        	if ( (boolean) PlanningSettings.get("useAdvice") )
            	updateAdvice();

        	transitions = sampleBatch(rrg);
        	if ( transitions == null )
        		continue;
        	if( productAutomaton.isAcceptingTransition(transitions) )
        		computePath = true;
        	learn(transitions);
        	
        	startTime = System.nanoTime();
        	if( computePath ) {
        		ArrayList<BDD> path	= productAutomaton.findAcceptingPath();
        		if( path != null ) {
        			System.out.println("\nPath found in the abstraction: ");
            		productAutomaton.printPath(path);
                	finalPath = rrg.liftPath(path);
            		break;
            	}
        	}
        	pathTime += System.nanoTime() - startTime;
		}
		printOutput();
        Initialize.getFactory().done();
	}

	private float exploreDiscretization(Discretization discretization) throws Exception {
		float length = 0;
		Point2D currentPosition = env.getInit();
		Point2D newPosition;
		discretization.initializeGraph();
		discretization.knowDiscretization(env, productAutomaton, currentPosition, (float) PlanningSettings.get("sensingRadius"));
		
		while(! discretization.exploredCompletely()) {
			newPosition = discretization.findAMove(currentPosition);
			length += discretization.findAPath(currentPosition, newPosition);
			currentPosition = newPosition;
			discretization.knowDiscretization(env, productAutomaton, currentPosition, (float) PlanningSettings.get("sensingRadius"));
		}
		System.out.println(length);
		return length;
	}
}









