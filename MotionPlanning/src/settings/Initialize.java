package settings;

import java.io.OutputStream;
import java.io.PrintStream;

import modules.motionPlanner.Environment;
import modules.motionPlanner.RRG;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BuDDyFactory;
import transitionSystem.ProductAutomaton;
import transitionSystem.reader.EnvironmentReader;
import transitionSystem.reader.PropertyReader;

/**
 * <p>Creates the BDD of the property and Initializes the product automata</p>
 * @see ProductAutomaton
 * @author Kush Grover
 */

public class Initialize
{
	ProductAutomaton productAutomaton;
	static Environment env;
	RRG rrg;
	static BDDFactory factory;
	
	@SuppressWarnings("static-access")
	public Initialize() throws Exception
	{
		
		factory			= BuDDyFactory.init(20, (int) PlanningSettings.get("planning.bddFactoryCacheSize"));
		ProductAutomaton.factory				= factory;
        ProductAutomaton.threshold				= (int) PlanningSettings.get("planning.transitionThreshold");
		
		
		
//		Read environment
//		BufferedReader envReader = new BufferedReader(new FileReader("/home/kush/Projects/robotmotionplanning/MotionPlanning/"+directory+"/environment.env"));
		String envFile				= "/home/kush/Projects/robotmotionplanning/MotionPlanning/Examples/6rooms/env.env";
		String labelFile			= "/home/kush/Projects/robotmotionplanning/MotionPlanning/Examples/6rooms/label.lb";
        env = (new EnvironmentReader(envFile, labelFile)).env;
		
        String propertyFile			= "/home/kush/Projects/robotmotionplanning/MotionPlanning/Examples/6rooms/property.pr";
		PropertyReader prop 		= new PropertyReader(Environment.getLabelling().getApListSystem(), propertyFile);

		
		PrintStream out = System.out;
		System.setOut(new PrintStream(OutputStream.nullOutputStream()));
		rrg 						= new RRG(env);
		System.setOut(out);

		
        ProductAutomaton.apListProperty			= prop.propertyParser.getAPListProperty();
		ProductAutomaton.numVars				= prop.propertyParser.getNumVars();
		ProductAutomaton.apListSystem			= prop.propertyParser.getAPListSystem();
		ProductAutomaton.numAPSystem			= ProductAutomaton.apListSystem.size();
		productAutomaton						= new ProductAutomaton(prop.propertyParser.getPropertyBDD());
        productAutomaton.setInitState(prop.propertyParser.getInitStateProperty());
	}
	

	/**
	 * 
	 * @return Product Automaton
	 */
	public ProductAutomaton getProductAutomaton() 
	{
		return productAutomaton;
	}
	
	public Environment getEnvironment()
	{
		return env;
	}
	
	public RRG getRRG()
	{
		return rrg;
	}

	public static BDDFactory getFactory()
	{
		return factory;
	}
}
