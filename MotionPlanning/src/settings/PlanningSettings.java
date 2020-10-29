package settings;


import java.io.File;
import java.util.logging.Logger;

public class PlanningSettings 
{
	
	//Default Constants
	public static final String DEFAULT_STRING = "";
	public static final int DEFAULT_INT = 0;
	public static final double DEFAULT_DOUBLE = 0.0;
	public static final float DEFAULT_FLOAT = 0.0f;
	public static final long DEFAULT_LONG = 0l;
	public static final boolean DEFAULT_BOOLEAN = false;
	public static final File DEFAULT_FILE = null;
	
	public static final String STRING_TYPE = "s";
	public static final String INTEGER_TYPE = "i";
	public static final String FLOAT_TYPE = "f";
	public static final String BOOLEAN_TYPE = "b";
	public static final String COLOUR_TYPE = "c";
	
	
	public static final String VERBOSITY					=	"planning.verbosity";
	public static final String BDD_FACTORY_CACHE_SIZE		=	"planning.bddFactoryCacheSize";
	public static final String USE_SPOT						= 	"planning.useSpot";
	public static final String MAX_LEVEL_TRANSITION			=	"planning.maxLevelTransition";
	public static final String SAMPLING_THRESHOLD			=	"planning.samplingThreshold";
	public static final String TRANSITION_THRESHOLD			= 	"planning.transitionThreshold";
	public static final String ETA							=	"planning.eta";
	public static final String SENSING_RADIUS				=	"planning.sensingRadius";
	public static final String MOVING_THRESHOLD				=  	"planning.movingThreshold";
	
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
			{ FLOAT_TYPE,		ETA,									"Maximum radius for RRG",				"1.0",			new Float(0.5),																"",
																					"Maximum radius to find the neighbours to which current point can have an edge"},
			{ FLOAT_TYPE,		SENSING_RADIUS,							"Sensing radius around the robot",		"1.0",			new Float(1),																"",
																					"The radius of the sensing area arond the robot"},
			{ INTEGER_TYPE,		MOVING_THRESHOLD,						"Threshold for moving the robot",		"1.0",			new Integer(50),															"",
																					"After how many iterations without moving, the robot should move"}

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
		} else if (VARIABLE.equals(MOVING_THRESHOLD))
		{
			propertyData[8][4]	= value;
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
		} else if (VARIABLE.equals(MOVING_THRESHOLD))
		{
			return propertyData[8][4];
		}
		return null;
	}
	

}
