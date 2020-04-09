package transitionSystem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import jhoafparser.parser.generated.ParseException;
import net.sf.javabdd.BDDFactory;
import transitionSystem.TSparser.BuchiAutomataParser;

/**
 * <p>Creates the BDD of the property and Initializes the product automata</p>
 * @see ProductAutomaton
 * @author Kush Grover
 */

public class Initialize
{
	ProductAutomaton productAutomaton;
	
	public Initialize(BDDFactory factory, 
			int threshold, 
			int levelOfTransitions, 
			ArrayList<String> apListSystem) throws IOException, ParseException{
		
		BufferedReader propertyReader = new BufferedReader(new FileReader("/home/kush/Projects/robotmotionplanning/MotionPlanning/Examples/Example 1/property.pr"));
        String propertyString=propertyReader.readLine();
        propertyReader.close();
        System.out.println("\nProperty to satisfty: "+propertyString);
        
//		String command="/home/kush/Projects/robotmotionplanning/spot-2.8.5/bin/ltl2tgba";
        String command="/home/kush/Projects/robotmotionplanning/owl/build/install/owl/bin/ltl2ldba";
		
		
//      ProcessBuilder builder1 = new ProcessBuilder(command,"--deterministic",propertyString);
        ProcessBuilder builder1 = new ProcessBuilder(command,propertyString);
        builder1.redirectErrorStream(true);
        Process p1 = builder1.start();
        
//      ProcessBuilder builder2 = new ProcessBuilder(command,"--deterministic",propertyString);
        ProcessBuilder builder2 = new ProcessBuilder(command,propertyString);
        builder2.redirectErrorStream(true);
        Process p2 = builder2.start();
        
//      ProcessBuilder builder3 = new ProcessBuilder(command,"--deterministic",propertyString);
        ProcessBuilder builder3 = new ProcessBuilder(command,propertyString);
        builder3.redirectErrorStream(true);
        Process p3 = builder2.start();
        
        BufferedReader r = new BufferedReader(new InputStreamReader(p3.getInputStream()));
        String line;
        BufferedWriter writer = new BufferedWriter(new FileWriter("/home/kush/Projects/robotmotionplanning/MotionPlanning/temp/propertyAutomata.hoa"));
        while (true) {
            line = r.readLine();
            if (line == null) { break; }
            writer.write(line+"\n");
        } 
        writer.close();
        
        BuchiAutomataParser propertyParser=new BuchiAutomataParser(factory, 
        		p1.getInputStream(), 
        		p2.getInputStream(), 
        		apListSystem, 
        		levelOfTransitions); 
		
        ProductAutomaton.factory=factory;
        ProductAutomaton.threshold=threshold;
        ProductAutomaton.levelOfTransitions=levelOfTransitions;
		ProductAutomaton.apListProperty=propertyParser.getAPListProperty();
		ProductAutomaton.numVars=propertyParser.getNumVars();
		ProductAutomaton.apListSystem=propertyParser.getAPListSystem();
		ProductAutomaton.numAPSystem=ProductAutomaton.apListSystem.size();
		productAutomaton=new ProductAutomaton(propertyParser.getPropertyBDD());
	}
	

	/**
	 * 
	 * @return Product Automaton
	 */
	public ProductAutomaton getProductAutomaton() {
		return productAutomaton;
	}

}
