import net.sf.javabdd.BDD;
import net.sf.javabdd.BDD.BDDIterator;
import net.sf.javabdd.BDDFactory;
import settings.Initialize;
import settings.PlanningException;
import settings.PlanningSettings;
import modules.KnownGrid;
import modules.KnownRRG;
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
	double totalTime		= 0;
    boolean needNewAdvice 	= true;
    ArrayList<BDD> advice 	= null;
    float moveLength, pathLength;
    List<DefaultEdge> finalPath = new ArrayList<DefaultEdge>();

    /**
     * initialize everything
     * @throws Exception
	 * @param factory
     */
	public Planning(BDDFactory factory) throws Exception
	{
		beginTime				= System.nanoTime();
		
		Initialize initialize	= new Initialize(factory);// read the files and initialise everything
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
		if((boolean) PlanningSettings.get("debug")){
			System.out.print("Exporting product automaton ... ");
	    	productAutomaton.createDot(iterationNumber);
			System.out.println("done");
		}
		Pair<Float, Float> length = new Pair<Float, Float>(0f, 0f);
		if((boolean) PlanningSettings.get("firstExplThenPlan")) {
			length.setFirst(moveLength);
			length.setSecond(pathLength);
		}
		else{
			UnknownRRG urrg = (UnknownRRG) rrg;
			System.out.print("Storing output ... ");
			length = urrg.plotGraph(finalPath);
			System.out.println("done");
			moveLength = length.getFirst();
			pathLength = length.getSecond();
		}
		totalTime = System.nanoTime() - beginTime;
        
        int adviceSamples = 0;
		for( int k=0; k<10; k++ )
			adviceSamples += rrg.adviceSampled[k];

		if((Integer) PlanningSettings.get("numberOfRuns") == 1 || (boolean) PlanningSettings.get("debug")) {
			System.out.println("\nNumber of iterations = " + iterationNumber);
			System.out.println("Total sampled points = " + rrg.totalSampledPoints);
			System.out.println("RRG size = " + rrg.totalPoints);
			System.out.println("\nMovement Length = " + length.getFirst());
			System.out.println("Remaining path Length = " + length.getSecond());
			System.out.println("\nAdvice samples: " + Arrays.toString(rrg.adviceSampled) + " (" + adviceSamples + ")");
			System.out.print("\nTotal time taken (in ms):");
			System.out.println(totalTime / 1000000);
		}

		if((boolean) PlanningSettings.get("debug")) {
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
	public Object[] explAndPlanTogether() throws Exception
	{    
    	BDD transitions 					= null;
        boolean computePath 				= false;
        boolean debug 						= (boolean) PlanningSettings.get("debug");

		System.out.println("Starting planning ...");
		UnknownRRG urrg = (UnknownRRG) rrg;
        urrg.grid.knowDiscretization(env, productAutomaton, env.getInit(), (float) PlanningSettings.get("sensingRadius"));
        while( true )
        {
        	if( debug ) {
	        	if( iterationNumber == 100 ) {
//	        		urrg.grid.printDiscretization();
//	        		urrg.grid.printFrontiers();
	        		break;
	        	}
        	}
        	
        	iterationNumber++;
        	if((boolean) PlanningSettings.get("debug"))
	        	System.out.println("Iter num: " + iterationNumber);

        	if ((boolean) PlanningSettings.get("useAdvice") )
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
        			System.out.println("\nPath found :D :D :D");
					if((boolean) PlanningSettings.get("debug"))
	            		productAutomaton.printPath(path);
                	finalPath = urrg.liftPath(path); 
            		break;
            	}
        	}
        	pathTime += System.nanoTime() - startTime;
        }
        printOutput();
		if((boolean) PlanningSettings.get("debug")) {
			System.out.print("Moving time (in ms):");
			System.out.println(urrg.moveTime / 1000000);
		}

//        Initialize.getFactory().done();
//		Initialize.getFactory().reset();

		Object[] data = new Object[] {
				new Integer(iterationNumber),
				new Integer(rrg.totalSampledPoints),
				new Integer(rrg.totalPoints),
				new Float(moveLength),
				new Float(pathLength),
				new Double(totalTime)
		};
		return data;
    }
	
	/**
	 * Do exploration first, then do the planning
	 * @throws Exception
	 */
	public Object[] firstExplThenPlan() throws Exception {
		KnownGrid grid = new KnownGrid(env, (float) PlanningSettings.get("gridSize"));
		startTime = System.nanoTime();
		System.out.print("Starting exploration ...");
		moveLength = exploreDiscretization(grid);
		System.out.println(" finished");
		explTime = System.nanoTime() - startTime;
		
		BDD transitions = null;
        boolean computePath = true;

		System.out.print("Starting planning ...");
		while( true ){
			iterationNumber++;

			if((boolean) PlanningSettings.get("debug"))
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
        			System.out.println("Path found :D");
					if((boolean) PlanningSettings.get("debug"))
						productAutomaton.printPath(path);
                	finalPath = rrg.liftPath(path);
            		break;
            	}
        	}
        	pathTime += System.nanoTime() - startTime;
		}
//        Initialize.getFactory().done();

		KnownRRG krrg = (KnownRRG) rrg;
		System.out.print("Plotting...");
		Pair<Float, Float> length = krrg.plotGraph(finalPath, grid.getGraph());
		System.out.println("done");
		pathLength = length.getSecond();
		printOutput();

		Object[] data = new Object[] {
				new Integer(iterationNumber),
				new Integer(rrg.totalSampledPoints),
				new Integer(rrg.totalPoints),
				new Float(moveLength),
				new Float(pathLength),
				new Double(totalTime)
		};
		return data;
	}

	private float exploreDiscretization(KnownGrid grid) throws Exception {
		float length = 0;
		Point2D currentPosition = env.getInit();
		Point2D newPosition;
		grid.initializeGraph();
		grid.knowDiscretization(env, productAutomaton, currentPosition, (float) PlanningSettings.get("sensingRadius"));
		grid.updateInitPoint(currentPosition);
		while(! grid.exploredCompletely()) {
			newPosition = grid.findAMove(currentPosition).getFirst();
			grid.updateMovement(currentPosition, newPosition);
			length += grid.findAPath(currentPosition, newPosition);
			currentPosition = newPosition;
			grid.knowDiscretization(env, productAutomaton, currentPosition, (float) PlanningSettings.get("sensingRadius"));
		}
		rrg.setMovement(grid.getMovement());
		return length;
	}
}









