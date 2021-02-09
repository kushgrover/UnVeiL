package settings;


import java.util.logging.Logger;

public class PlanningSettings 
{
	
	//Default Constants
//	public static final String DEFAULT_STRING = "";
//	public static final int DEFAULT_INT = 0;
//	public static final double DEFAULT_DOUBLE = 0.0;
//	public static final float DEFAULT_FLOAT = 0.0f;
//	public static final long DEFAULT_LONG = 0l;
//	public static final boolean DEFAULT_BOOLEAN = false;
//	public static final File DEFAULT_FILE = null;
	
	public static final String STRING_TYPE = "s";
	public static final String INTEGER_TYPE = "i";
	public static final String FLOAT_TYPE = "f";
	public static final String BOOLEAN_TYPE = "b";
	public static final String COLOUR_TYPE = "c";
	
	
	public static final String VERBOSITY					=	"verbosity";
	public static final String BDD_FACTORY_CACHE_SIZE		=	"bddFactoryCacheSize";
	public static final String USE_SPOT						= 	"useSpot";
	public static final String MAX_LEVEL_TRANSITION			=	"maxLevelTransition";
	public static final String SAMPLING_THRESHOLD			=	"samplingThreshold";
	public static final String TRANSITION_THRESHOLD			= 	"transitionThreshold";
	public static final String ETA							=	"eta";
	public static final String SENSING_RADIUS				=	"sensingRadius";
	public static final String BATCH_SIZE					=  	"batchSize";
	public static final String DISCRETIZATION_SIZE			=  	"discretizationSize";
	public static final String USE_ADVICE					=	"useAdvice";
	public static final String FIRST_EXPL_THEN_PLAN			=	"firstExplThenPlan";
	public static final String DEBUG						=	"debug";
	
	
	public static final Logger RTREELOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	
	@SuppressWarnings("deprecation")
	public static final Object[][] propertyData =
		{
			//Datatype:			Key:									Display name:							Version:		Default:																	Constraints:																				Comment:
			//====================================================================================================================================================================================================================================================================================================================================
				
			{ BOOLEAN_TYPE,		VERBOSITY,								"Verbose output",						"1.0",			new Boolean(true),															"",																		
																					"For Verbose output." },
			{ INTEGER_TYPE, 	BDD_FACTORY_CACHE_SIZE,					"Cache size for BDD factory",			"1.0",			new Integer(10000),															"",
																					"Cache size for BDD operations."},
			{ BOOLEAN_TYPE, 	USE_SPOT,								"Use SPOT or OWL",						"1.0",			new Boolean(false),															"",
																					"Use SPOT or OWL for generating the automaton."},
			{ INTEGER_TYPE,		MAX_LEVEL_TRANSITION,					"Levels of transitions",				"1.0",			new Integer(4),																"",
																					"Possible levels of transitions possible"},
			{ INTEGER_TYPE,		SAMPLING_THRESHOLD,						"Threshold for sampling",				"1.0",			new Integer(50),															"",
																					"After how many samples, return null if don't find anything from the advised transitions"},									
			{ INTEGER_TYPE,		TRANSITION_THRESHOLD,					"Threshold for transitions",			"1.0",			new Integer(20),															"",
																					"Threshold after how many copies of a transition would decrease its level by 1."},
			{ FLOAT_TYPE,		ETA,									"Maximum radius for RRG",				"1.0",			new Float(1),																"",
																					"Maximum radius to find the neighbours to which current point can have an edge"},
			{ FLOAT_TYPE,		SENSING_RADIUS,							"Sensing radius around the robot",		"1.0",			new Float(1),																"",
																					"The radius of the sensing area arond the robot"},
			{ INTEGER_TYPE,		BATCH_SIZE,								"Threshold for moving the robot",		"1.0",			new Integer(50),															"",
																					"After how many iterations without moving, the robot should move"},
			{ FLOAT_TYPE,		DISCRETIZATION_SIZE,					"Size of discretization for frontiers",	"1.0",			new Float(0.05),															"",
																					"The size of each cell for computation of frontiers"},
			{ BOOLEAN_TYPE,		USE_ADVICE,								"Use advice or not",	"1.0",							new Boolean(false),															"",
																					"Bias exploration using advice"},
			{ BOOLEAN_TYPE,		FIRST_EXPL_THEN_PLAN,					"Exploration first, then planning",	"1.0",				new Boolean(false),															"",
																					"First explore the environment completely, then do planning with known environment"},
			{ BOOLEAN_TYPE,		DEBUG,									"Debug mode",	"1.0",									new Boolean(true),															"",
																					"Output a lot of things"},

		};
																			
	
	public void set(String VARIABLE, Object value)
	{
		if(VARIABLE.equals(VERBOSITY))
		{
			propertyData[0][4]	= value;
		} else if (VARIABLE.equals(BDD_FACTORY_CACHE_SIZE))
		{
			propertyData[1][4]	= value;
		} else if (VARIABLE.equals(USE_SPOT))
		{
			propertyData[2][4]	= value;
		} else if (VARIABLE.equals(MAX_LEVEL_TRANSITION))
		{
			propertyData[3][4]	= value;
		} else if (VARIABLE.equals(SAMPLING_THRESHOLD))
		{
			propertyData[4][4]	= value;
		} else if (VARIABLE.equals(TRANSITION_THRESHOLD))
		{
			propertyData[5][4]	= value;
		} else if (VARIABLE.equals(ETA))
		{
			propertyData[6][4]	= value;
		} else if (VARIABLE.equals(SENSING_RADIUS))
		{
			propertyData[7][4]	= value;
		} else if (VARIABLE.equals(BATCH_SIZE))
		{
			propertyData[8][4]	= value;
		} else if (VARIABLE.equals(DISCRETIZATION_SIZE))
		{
			propertyData[9][4]	= value;
		} else if (VARIABLE.equals(USE_ADVICE))
		{
			propertyData[10][4]	= value;
		} else if (VARIABLE.equals(FIRST_EXPL_THEN_PLAN))
		{
			propertyData[11][4]	= value;
		} else if (VARIABLE.equals(DEBUG))
		{
			propertyData[12][4]	= value;
		}
	}
	
	public static Object get(String VARIABLE)
	{
		if(VARIABLE.equals(VERBOSITY))
		{
			return propertyData[0][4];
		} else if (VARIABLE.equals(BDD_FACTORY_CACHE_SIZE))
		{
			return propertyData[1][4];
		} else if (VARIABLE.equals(USE_SPOT))
		{
			return propertyData[2][4];
		} else if (VARIABLE.equals(MAX_LEVEL_TRANSITION))
		{
			return propertyData[3][4];
		} else if (VARIABLE.equals(SAMPLING_THRESHOLD))
		{
			return propertyData[4][4];
		} else if (VARIABLE.equals(TRANSITION_THRESHOLD))
		{
			return propertyData[5][4];
		} else if (VARIABLE.equals(ETA))
		{
			return propertyData[6][4];
		} else if (VARIABLE.equals(SENSING_RADIUS))
		{
			return propertyData[7][4];
		} else if (VARIABLE.equals(BATCH_SIZE))
		{
			return propertyData[8][4];
		} else if (VARIABLE.equals(DISCRETIZATION_SIZE))
		{
			return propertyData[9][4];
		} else if (VARIABLE.equals(USE_ADVICE))
		{
			return propertyData[10][4];
		} else if (VARIABLE.equals(FIRST_EXPL_THEN_PLAN))
		{
			return propertyData[11][4];
		} else if (VARIABLE.equals(DEBUG))
		{
			return propertyData[12][4];
		}
		return null;
	}
	

}
