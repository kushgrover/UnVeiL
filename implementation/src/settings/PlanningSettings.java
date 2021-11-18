package settings;


public final class PlanningSettings
{
	
	public static final String STRING_TYPE 	= "s";
	public static final String INTEGER_TYPE = "i";
	public static final String FLOAT_TYPE   = "f";
	public static final String BOOLEAN_TYPE = "b";
	public static final String DOUBLE_TYPE = "b";

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
	public static final String PROPERTY_FILE				=	"propertyFile";
	public static final String OUTPUT_DIRECTORY				=	"outputDirectory";
	public static final String TIMEOUT						=	"timeout";
	public static final String EXPORT_PLOT_DATA				=	"exportPlotData";
	public static final String EXPORT_VIDEO_DATA			=	"exportVideoData";


	public static void outputParameters() {
		System.out.println("Parameters values: ");

		if((boolean) get("randomEnv")) {
			System.out.println("Environment: Random");
		} else {
			System.out.println("Environment: " + get("inputFile"));
		}

		if((boolean) get("useAdvice")) {
			System.out.println("Use Advice: Yes");
		} else {
			System.out.println("Use Advice: No");
		}

		System.out.println("Sensing radius: " + get("sensingRadius"));
		System.out.println("Batch size: " + get("batchSize"));
		System.out.println("Discretization cell size: " + get("gridSize"));
		System.out.println("Bias prob: " + get("biasProb") + '\n');
	}

//	public static final Logger RTREELOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	
	public static final Object[][] propertyData =
		{
			//Datatype:			Key:									Display name:							Version:		Default:																	Constraints:																				Comment:
			//====================================================================================================================================================================================================================================================================================================================================
				
			{ FLOAT_TYPE,		BIAS_PROB,								"Biasing probability",					"1.0", 			0.1F,															"",
																					"Probability of accepting non-advice samples" },
			
			{ INTEGER_TYPE, 	BDD_FACTORY_CACHE_SIZE,					"Cache size for BDD factory",			"1.0", 			200000,															"",
																					"Cache size for BDD operations."},
			
			{ BOOLEAN_TYPE, 	USE_SPOT,								"Use SPOT or OWL",						"1.0", 			Boolean.FALSE,															"",
																					"Use SPOT or OWL for generating the automaton."},
			
			{ INTEGER_TYPE,		MAX_LEVEL_TRANSITION,					"Levels of transitions",				"1.0", 			4,																"",
																					"Possible levels of transitions possible"},
			
			{ BOOLEAN_TYPE,		RANDOM_ENV,								"Generate a random env",				"1.0", 			Boolean.FALSE,															"",
																					"Generate a random environment and run the algorithm on that"},	
			
			{ INTEGER_TYPE,		TRANSITION_THRESHOLD,					"Threshold for transitions",			"1.0", 			20,															"",
																					"Threshold after how many copies of a transition would decrease its level by 1."},
			
			{ FLOAT_TYPE,		ETA,									"Maximum radius for RRG",				"1.0", 			1.0F,																"",
																					"Maximum radius to find the neighbours to which current point can have an edge"},
			
			{ FLOAT_TYPE,		SENSING_RADIUS,							"Sensing radius around the robot",		"1.0", 			1.0F,																"",
																					"The radius of the sensing area around the robot"},
			
			{ INTEGER_TYPE,		BATCH_SIZE,								"Size of one batch",					"1.0", 			50,															"",
																					"Maximum size of each batch"},
			
			{ FLOAT_TYPE, 		GRID_SIZE,								"Size of grid for frontiers",			"1.0", 			0.05F,															"",
																					"The size of each cell for computation of frontiers"},
			
			{ BOOLEAN_TYPE,		USE_ADVICE,								"Use advice or not",					"1.0", 			Boolean.TRUE,															"",
																					"Bias exploration using advice"},
			
			{ BOOLEAN_TYPE,		FIRST_EXPL_THEN_PLAN,					"Exploration first, then planning",		"1.0", 			Boolean.FALSE,															"",
																					"First explore the environment completely, then do planning with known environment"},
			
			{ BOOLEAN_TYPE,		DEBUG,									"Debug mode",							"1.0", 			Boolean.FALSE,															"",
																					"Output a lot of things"},
			
			{ STRING_TYPE,		INPUT_FILE,								"Input file name",						"1.0", 			"temp/Env/random",												"",
																					"Name of the input files without the extension"},

			{ BOOLEAN_TYPE,		ONLY_OPAQUE_OBSTACLES,					"Consider only Opaque obstacle",		"1.0", 			Boolean.TRUE,															"",
																					"Flag for not having see through obstacles"},

			{ INTEGER_TYPE,		NUMBER_OF_RUNS,							"number of runs to take average",		"1.0", 			1,															"",
																					"Run the current configuration for these many times and take average of the performance"},

			{ BOOLEAN_TYPE,		GENERATE_PLOT,							"Show a plot of output",				"1.0", 			Boolean.FALSE,															"",
																					"Generate plot showing the RRG graph and the trajectory"},

			{ STRING_TYPE,		PROPERTY_FILE,							"Property file in HOA format",			"1.0", "",															"",
																					"Specifying property as an automaton in HOA format"},

			{ STRING_TYPE,		OUTPUT_DIRECTORY,						"Output directory",						"1.0", "temp/",															"",
																					"Directory to store the output"},

			{ DOUBLE_TYPE,		TIMEOUT,								"Timeout for the algorithm",			"1.0", 			Double.MAX_VALUE,															"",
																					"After this much time, stop the run and output the results"},

			{ BOOLEAN_TYPE,		EXPORT_PLOT_DATA,						"Export the data for plotting",			"1.0", 			Boolean.FALSE,															"",
																					"Export data for plotting after some intervals"},

			{ BOOLEAN_TYPE,		EXPORT_PLOT_DATA,						"Export the data for plotting",			"1.0", 			Boolean.FALSE,															"",
																					"Export data after each batch"},

		};
																			
	
	public PlanningSettings(String[] args) throws PlanningException {
		boolean inputFile = true;
		boolean[] usingFlag = new boolean[args.length];
		try {
			if(! args[0].startsWith("--")) {
				set(INPUT_FILE, args[0]);
				usingFlag[0] = true;
			}
		}
		catch(ArrayIndexOutOfBoundsException e) {
			inputFile = false;
		}
		for(int i=0; i<args.length; i++) {
			if(args[i].equals("--first-expl-then-plan")) {
				set(FIRST_EXPL_THEN_PLAN, Boolean.TRUE);
				usingFlag[i] = true;
			}
			if(args[i].equals("--debug")) {
				set(DEBUG, Boolean.TRUE);
				usingFlag[i] = true;
			}
			if(args[i].equals("--no-advice")) {
				set(USE_ADVICE, Boolean.FALSE);
				usingFlag[i] = true;
			}
			if(args[i].equals("--random-env")) {
				set(RANDOM_ENV, Boolean.TRUE);
				inputFile = true;
				usingFlag[i] = true;
			}
			if(args[i].equals("--see-through-obstacles")) {
				set(ONLY_OPAQUE_OBSTACLES, Boolean.FALSE);
				usingFlag[i] = true;
			}
			if(args[i].equals("--plot")) {
				set(GENERATE_PLOT, Boolean.TRUE);
				usingFlag[i] = true;
			}
			if(args[i].equals("--cell-size")) {
				try {
					set(GRID_SIZE, Float.valueOf(args[i+1]));
					usingFlag[i] = true;
					usingFlag[i+1] = true;
				} catch (ArrayIndexOutOfBoundsException e){
					throw new PlanningException("Grid size not specified");
				}
			}
			if(args[i].equals("--sensing-radius")) {
				try {
					set(SENSING_RADIUS, Float.valueOf(args[i+1]));
					usingFlag[i] = true;
					usingFlag[i+1] = true;
				} catch (ArrayIndexOutOfBoundsException e){
					throw new PlanningException("Sensing radius not specified");
				}
			}
			if(args[i].equals("--batch-size")) {
				try {
					set(BATCH_SIZE, Integer.valueOf(args[i + 1]));
					usingFlag[i] = true;
					usingFlag[i+1] = true;
				} catch (ArrayIndexOutOfBoundsException e){
					throw new PlanningException("Batch size not specified");
				}
			}
			if(args[i].equals("--bias-prob")) {
				try {
					set(BIAS_PROB, Float.valueOf(args[i+1]));
					usingFlag[i] = true;
					usingFlag[i+1] = true;
				} catch (ArrayIndexOutOfBoundsException e){
					throw new PlanningException("Biasing probability not specified");
				}
			}
			if(args[i].equals("--num-of-runs")) {
				try {
					set(NUMBER_OF_RUNS, Integer.valueOf(args[i + 1]));
					usingFlag[i] = true;
					usingFlag[i+1] = true;
				} catch (ArrayIndexOutOfBoundsException e){
					throw new PlanningException("Number of runs not specified");
				}
				if(Integer.parseInt(args[i + 1]) > 1) {
					set(GENERATE_PLOT, Boolean.FALSE);
				}
			}
			if(args[i].equals("--property-file")){
				try {
					set(PROPERTY_FILE, args[i + 1]);
					usingFlag[i] = true;
					usingFlag[i+1] = true;
				} catch (ArrayIndexOutOfBoundsException e){
					throw new PlanningException("Property file not specified");
				}
			}
			if(args[i].equals("--output-dir")){
				try {
					set(OUTPUT_DIRECTORY, args[i + 1]);
					usingFlag[i] = true;
					usingFlag[i+1] = true;
				} catch (ArrayIndexOutOfBoundsException e){
					throw new PlanningException("Output directory not specified");
				}
			}
			if(args[i].equals("--timeout")){
				try {
					set(TIMEOUT, Double.valueOf(args[i+1]));
					usingFlag[i] = true;
					usingFlag[i+1] = true;
				} catch (ArrayIndexOutOfBoundsException e){
					throw new PlanningException("Timeout not specified");
				}
			}
			if(args[i].equals("--export-plot-data")){
				set(EXPORT_PLOT_DATA, Boolean.TRUE);
				usingFlag[i] = true;
			}
			if(args[i].equals("--export-video-data")){
				set(EXPORT_VIDEO_DATA, Boolean.TRUE);
				usingFlag[i] = true;
			}
		}
		if(! inputFile){
			throw new PlanningException("No input files given");
		}
		for(int i=0; i < usingFlag.length; i++){
			if(! usingFlag[i]) {
				throw new PlanningException("Not a valid argument: " + args[i]);
			}
		}
	}



	public static void set(String VARIABLE, Object value)
	{
		switch (VARIABLE) {
			case BIAS_PROB -> propertyData[0][4] = value;
			case BDD_FACTORY_CACHE_SIZE -> propertyData[1][4] = value;
			case USE_SPOT -> propertyData[2][4] = value;
			case MAX_LEVEL_TRANSITION -> propertyData[3][4] = value;
			case RANDOM_ENV -> propertyData[4][4] = value;
			case TRANSITION_THRESHOLD -> propertyData[5][4] = value;
			case ETA -> propertyData[6][4] = value;
			case SENSING_RADIUS -> propertyData[7][4] = value;
			case BATCH_SIZE -> propertyData[8][4] = value;
			case GRID_SIZE -> propertyData[9][4] = value;
			case USE_ADVICE -> propertyData[10][4] = value;
			case FIRST_EXPL_THEN_PLAN -> propertyData[11][4] = value;
			case DEBUG -> propertyData[12][4] = value;
			case INPUT_FILE -> propertyData[13][4] = value;
			case ONLY_OPAQUE_OBSTACLES -> propertyData[14][4] = value;
			case NUMBER_OF_RUNS -> propertyData[15][4] = value;
			case GENERATE_PLOT -> propertyData[16][4] = value;
			case PROPERTY_FILE -> propertyData[17][4] = value;
			case OUTPUT_DIRECTORY -> propertyData[18][4] = value;
			case TIMEOUT -> propertyData[19][4] = value;
			case EXPORT_PLOT_DATA -> propertyData[20][4] = value;
			case EXPORT_VIDEO_DATA -> propertyData[21][4] = value;
		}
	}
	
	public static Object get(String VARIABLE)
	{
		return switch (VARIABLE) {
			case BIAS_PROB -> propertyData[0][4];
			case BDD_FACTORY_CACHE_SIZE -> propertyData[1][4];
			case USE_SPOT -> propertyData[2][4];
			case MAX_LEVEL_TRANSITION -> propertyData[3][4];
			case RANDOM_ENV -> propertyData[4][4];
			case TRANSITION_THRESHOLD -> propertyData[5][4];
			case ETA -> propertyData[6][4];
			case SENSING_RADIUS -> propertyData[7][4];
			case BATCH_SIZE -> propertyData[8][4];
			case GRID_SIZE -> propertyData[9][4];
			case USE_ADVICE -> propertyData[10][4];
			case FIRST_EXPL_THEN_PLAN -> propertyData[11][4];
			case DEBUG -> propertyData[12][4];
			case INPUT_FILE -> propertyData[13][4];
			case ONLY_OPAQUE_OBSTACLES -> propertyData[14][4];
			case NUMBER_OF_RUNS -> propertyData[15][4];
			case GENERATE_PLOT -> propertyData[16][4];
			case PROPERTY_FILE -> propertyData[17][4];
			case OUTPUT_DIRECTORY -> propertyData[18][4];
			case TIMEOUT -> propertyData[19][4];
			case EXPORT_PLOT_DATA -> propertyData[20][4];
			case EXPORT_VIDEO_DATA -> propertyData[21][4];
			default -> null;
		};
	}



}
