package settings;


import java.util.logging.Logger;

public class PlanningSettings 
{
	
	public static final String STRING_TYPE 	= "s";
	public static final String INTEGER_TYPE = "i";
	public static final String FLOAT_TYPE   = "f";
	public static final String BOOLEAN_TYPE = "b";
	public static final String COLOUR_TYPE  = "c";
	
	
	public static final String BIAS_PROB					=	"biasProb";
	public static final String BDD_FACTORY_CACHE_SIZE		=	"bddFactoryCacheSize";
	public static final String USE_SPOT						= 	"useSpot";
	public static final String MAX_LEVEL_TRANSITION			=	"maxLevelTransition";
	public static final String RANDOM_ENV					=	"randomEnv";
	public static final String TRANSITION_THRESHOLD			= 	"transitionThreshold";
	public static final String ETA							=	"eta";
	public static final String SENSING_RADIUS				=	"sensingRadius";
	public static final String BATCH_SIZE					=  	"batchSize";
	public static final String GRID_SIZE 					=  	"gridSize";
	public static final String USE_ADVICE					=	"useAdvice";
	public static final String FIRST_EXPL_THEN_PLAN			=	"firstExplThenPlan";
	public static final String DEBUG						=	"debug";
	public static final String INPUT_FILE					=	"inputFile";
	public static final String ONLY_OPAQUE_OBSTACLES		=	"onlyOpaqueObstacles";
	public static final String NUMBER_OF_RUNS				=	"numberOfRuns";
	public static final String GENERATE_PLOT				=	"generatePlot";

	public static void outputParameters() {
		System.out.println("Parameters values: ");

		if((boolean) get("randomEnv"))
			System.out.println("Environment: Random");
		else
			System.out.println("Environment: " + (String) get("inputFile"));

		if((boolean) get("useAdvice"))
			System.out.println("Use Advice: Yes");
		else
			System.out.println("Use Advice: No");

		System.out.println("Sensing radius: " + (Float) get("sensingRadius"));
		System.out.println("Batch size: " + (Integer) get("batchSize"));
		System.out.println("Discretization cell size: " + (Float) get("gridSize"));
		System.out.println("Bias prob: " + (Float) get("biasProb") + "\n");
	}

	public static final Logger RTREELOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	
	@SuppressWarnings("deprecation")
	public static final Object[][] propertyData =
		{
			//Datatype:			Key:									Display name:							Version:		Default:																	Constraints:																				Comment:
			//====================================================================================================================================================================================================================================================================================================================================
				
			{ FLOAT_TYPE,		BIAS_PROB,								"Biasing probability",					"1.0",			new Float(0.1),															"",
																					"Probability of accepting non-advice samples" },
			
			{ INTEGER_TYPE, 	BDD_FACTORY_CACHE_SIZE,					"Cache size for BDD factory",			"1.0",			new Integer(200000),															"",
																					"Cache size for BDD operations."},
			
			{ BOOLEAN_TYPE, 	USE_SPOT,								"Use SPOT or OWL",						"1.0",			new Boolean(false),															"",
																					"Use SPOT or OWL for generating the automaton."},
			
			{ INTEGER_TYPE,		MAX_LEVEL_TRANSITION,					"Levels of transitions",				"1.0",			new Integer(4),																"",
																					"Possible levels of transitions possible"},
			
			{ BOOLEAN_TYPE,		RANDOM_ENV,								"Generate a random env",				"1.0",			new Boolean(false),															"",
																					"Generate a random environment and run the algorithm on that"},	
			
			{ INTEGER_TYPE,		TRANSITION_THRESHOLD,					"Threshold for transitions",			"1.0",			new Integer(20),															"",
																					"Threshold after how many copies of a transition would decrease its level by 1."},
			
			{ FLOAT_TYPE,		ETA,									"Maximum radius for RRG",				"1.0",			new Float(1),																"",
																					"Maximum radius to find the neighbours to which current point can have an edge"},
			
			{ FLOAT_TYPE,		SENSING_RADIUS,							"Sensing radius around the robot",		"1.0",			new Float(1),																"",
																					"The radius of the sensing area arond the robot"},
			
			{ INTEGER_TYPE,		BATCH_SIZE,								"Size of one batch",					"1.0",			new Integer(50),															"",
																					"Maximun size of each batch"},
			
			{ FLOAT_TYPE, 		GRID_SIZE,								"Size of grid for frontiers",			"1.0",			new Float(0.05),															"",
																					"The size of each cell for computation of frontiers"},
			
			{ BOOLEAN_TYPE,		USE_ADVICE,								"Use advice or not",					"1.0",			new Boolean(true),															"",
																					"Bias exploration using advice"},
			
			{ BOOLEAN_TYPE,		FIRST_EXPL_THEN_PLAN,					"Exploration first, then planning",		"1.0",			new Boolean(false),															"",
																					"First explore the environment completely, then do planning with known environment"},
			
			{ BOOLEAN_TYPE,		DEBUG,									"Debug mode",							"1.0",			new Boolean(false),															"",
																					"Output a lot of things"},
			
			{ STRING_TYPE,		INPUT_FILE,								"Input file name",						"1.0",			new String("temp/Env/random"),												"",
																					"Name of the input files without the extension"},

			{ BOOLEAN_TYPE,		ONLY_OPAQUE_OBSTACLES,					"Consider only Opaque obstacle",		"1.0",			new Boolean(false),															"",
																					"Flag for not having see through obstacles"},

			{ INTEGER_TYPE,		NUMBER_OF_RUNS,							"number of runs to take average",		"1.0",			new Integer(1),															"",
																					"Run the current configuration for these many times and take averange of the performance"},

			{ BOOLEAN_TYPE,		GENERATE_PLOT,							"Show a plot of output",				"1.0",			new Boolean(false),															"",
																					"Generate plot showing the RRG graph and the trajectory"},

		};
																			
	
	@SuppressWarnings("deprecation")
	public PlanningSettings(String[] args) throws Exception {
		boolean inputFile = true;
		try {
			if(! args[0].startsWith("--")) {
				set(INPUT_FILE, args[0]);
			}
		}
		catch(ArrayIndexOutOfBoundsException e) {
			inputFile = false;
		}
		for(int i=0; i<args.length; i++) {
			if(args[i].equals("--first-expl-then-plan")) {
				set(FIRST_EXPL_THEN_PLAN, new Boolean(true));
			}
			if(args[i].equals("--debug")) {
				set(DEBUG, new Boolean(true));
			}
			if(args[i].equals("--no-advice")) {
				set(USE_ADVICE, new Boolean(false));
			}
			if(args[i].equals("--random-env")) {
				set(RANDOM_ENV, new Boolean(true));
				inputFile = true;
			}
			if(args[i].equals("--only-opaque-obstacles")) {
				set(ONLY_OPAQUE_OBSTACLES, new Boolean(true));
			}
			if(args[i].equals("--plot")) {
				set(GENERATE_PLOT, new Boolean(true));
			}
			if(args[i].equals("--set-grid-size")) {
				try {
					set(GRID_SIZE, new Float(args[i + 1]));
				} catch (ArrayIndexOutOfBoundsException e){
					throw new Exception("Grid size not specified");
				}
			}
			if(args[i].equals("--set-sensing-radius")) {
				try {
					set(SENSING_RADIUS, new Float(args[i+1]));
				} catch (ArrayIndexOutOfBoundsException e){
					throw new Exception("Sensing radius not specified");
				}
			}
			if(args[i].equals("--set-batch-size")) {
				try {
					set(BATCH_SIZE, new Integer(args[i+1]));
				} catch (ArrayIndexOutOfBoundsException e){
					throw new Exception("Batch size not specified");
				}
			}
			if(args[i].equals("--set-bias-prob")) {
				try {
					set(BIAS_PROB, new Float(args[i+1]));
				} catch (ArrayIndexOutOfBoundsException e){
					throw new Exception("Biasing probability not specified");
				}
			}
			if(args[i].equals("--num-of-runs")) {
				try {
					set(NUMBER_OF_RUNS, new Integer(args[i+1]));
				} catch (ArrayIndexOutOfBoundsException e){
					throw new Exception("Number of runs not specified");
				}
				if(new Integer(args[i+1]) > 1)
					set(GENERATE_PLOT, new Boolean(false));
			}
		}
		if(! inputFile){
			throw new Exception("No input files given");
		}
	}



	public void set(String VARIABLE, Object value)
	{
		if(VARIABLE.equals(BIAS_PROB)) {
			propertyData[0][4]	= value;
		}
		else if (VARIABLE.equals(BDD_FACTORY_CACHE_SIZE)) {
			propertyData[1][4]	= value;
		}
		else if (VARIABLE.equals(USE_SPOT)) {
			propertyData[2][4]	= value;
		}
		else if (VARIABLE.equals(MAX_LEVEL_TRANSITION)) {
			propertyData[3][4]	= value;
		}
		else if (VARIABLE.equals(RANDOM_ENV)) {
			propertyData[4][4]	= value;
		}
		else if (VARIABLE.equals(TRANSITION_THRESHOLD)) {
			propertyData[5][4]	= value;
		}
		else if (VARIABLE.equals(ETA)) {
			propertyData[6][4]	= value;
		}
		else if (VARIABLE.equals(SENSING_RADIUS)) {
			propertyData[7][4]	= value;
		}
		else if (VARIABLE.equals(BATCH_SIZE)) {
			propertyData[8][4]	= value;
		}
		else if (VARIABLE.equals(GRID_SIZE)) {
			propertyData[9][4]	= value;
		}
		else if (VARIABLE.equals(USE_ADVICE)) {
			propertyData[10][4]	= value;
		}
		else if (VARIABLE.equals(FIRST_EXPL_THEN_PLAN)) {
			propertyData[11][4]	= value;
		}
		else if (VARIABLE.equals(DEBUG)) {
			propertyData[12][4]	= value;
		}
		else if (VARIABLE.equals(INPUT_FILE)) {
			propertyData[13][4] = value;
		}
		else if (VARIABLE.equals(ONLY_OPAQUE_OBSTACLES)) {
			propertyData[14][4] = value;
		}
		else if (VARIABLE.equals(NUMBER_OF_RUNS)) {
			propertyData[15][4] = value;
		}
		else if (VARIABLE.equals(GENERATE_PLOT)) {
			propertyData[16][4] = value;
		}
	}
	
	public static Object get(String VARIABLE)
	{
		if(VARIABLE.equals(BIAS_PROB)) {
			return propertyData[0][4];
		} 
		else if (VARIABLE.equals(BDD_FACTORY_CACHE_SIZE)) {
			return propertyData[1][4];
		} 
		else if (VARIABLE.equals(USE_SPOT)) {
			return propertyData[2][4];
		} 
		else if (VARIABLE.equals(MAX_LEVEL_TRANSITION)) {
			return propertyData[3][4];
		} 
		else if (VARIABLE.equals(RANDOM_ENV)) {
			return propertyData[4][4];
		} 
		else if (VARIABLE.equals(TRANSITION_THRESHOLD)) {
			return propertyData[5][4];
		}
		else if (VARIABLE.equals(ETA)) {
			return propertyData[6][4];
		}
		else if (VARIABLE.equals(SENSING_RADIUS)) {
			return propertyData[7][4];
		}
		else if (VARIABLE.equals(BATCH_SIZE)) {
			return propertyData[8][4];
		}
		else if (VARIABLE.equals(GRID_SIZE)) {
			return propertyData[9][4];
		}
		else if (VARIABLE.equals(USE_ADVICE)) {
			return propertyData[10][4];
		}
		else if (VARIABLE.equals(FIRST_EXPL_THEN_PLAN)) {
			return propertyData[11][4];
		}
		else if (VARIABLE.equals(DEBUG)) {
			return propertyData[12][4];
		}
		else if (VARIABLE.equals(INPUT_FILE)) {
			return propertyData[13][4];
		}
		else if (VARIABLE.equals(ONLY_OPAQUE_OBSTACLES)) {
			return propertyData[14][4];
		}
		else if (VARIABLE.equals(NUMBER_OF_RUNS)) {
			return propertyData[15][4];
		}
		else if (VARIABLE.equals(GENERATE_PLOT)) {
			return propertyData[16][4];
		}
		return null;
	}



}
