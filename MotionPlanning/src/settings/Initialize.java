package settings;

import java.io.OutputStream;
import java.io.PrintStream;

import abstraction.ProductAutomaton;
import environment.Environment;
import modules.RRG;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BuDDyFactory;
import planningIO.EnvironmentReader;
import planningIO.PropertyReader;

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
		String envFile				= "/home/kush/Projects/robotmotionplanning/MotionPlanning/Examples/6rooms/env.env";
		String labelFile			= "/home/kush/Projects/robotmotionplanning/MotionPlanning/Examples/6rooms/label.lb";
        env = (new EnvironmentReader(envFile, labelFile)).env;
		
        String propertyFile			= "/home/kush/Projects/robotmotionplanning/MotionPlanning/Examples/6rooms/property.pr";
		PropertyReader prop 		= new PropertyReader(Environment.getLabelling().getApListSystem(), propertyFile);

		
		rrg 						= new RRG(env);

		
        ProductAutomaton.apListProperty			= prop.propertyParser.getAPListProperty();
		ProductAutomaton.numVars				= prop.propertyParser.getNumVars();
		ProductAutomaton.apListSystem			= prop.propertyParser.getAPListSystem();
		ProductAutomaton.numAPSystem			= ProductAutomaton.apListSystem.size();
		productAutomaton						= new ProductAutomaton(prop.propertyParser.getPropertyBDD());
		rrg.setProductAutomaton(productAutomaton);
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
	
	/**
	 * 
	 * @return environment
	 */
	public Environment getEnvironment()
	{
		return env;
	}
	
	/**
	 * 
	 * @return RRG object
	 */
	public RRG getRRG()
	{
		return rrg;
	}

	/**
	 * 
	 * @return BDD factory
	 */
	public static BDDFactory getFactory()
	{
		return factory;
	}
}
