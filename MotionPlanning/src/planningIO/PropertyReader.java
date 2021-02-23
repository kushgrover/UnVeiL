package planningIO;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import jhoafparser.parser.generated.ParseException;
import settings.PlanningSettings;

public class PropertyReader 
{
	public BuchiAutomataParser propertyParser;
	
	
	public PropertyReader(ArrayList<String> apListSystem, String propertyFile) throws IOException 
	{
		String propertyString;
		if((boolean) PlanningSettings.get("randomEnv")) {
			propertyString = "GF (r1 & b) & GF (r2 & b) & GF (r3 & b) & GF (r4 & b) & GF (r5 & b) & GF (r6 & b)";
		}
		else {
			// Read property from the file
			BufferedReader propertyReader 	= new BufferedReader(new FileReader(propertyFile));
	        propertyString			= propertyReader.readLine();
	        propertyReader.close();
		}
        // Output 
        System.out.println("\nConstructing automaton for property: " + propertyString + " ...\n\n");
        
    	
        // Using SPOT
//		String command="/home/kush/Projects/robotmotionplanning/spot-2.8.5/bin/ltl2tgba";
//      ProcessBuilder builder1 = new ProcessBuilder(command,"--deterministic",propertyString);
//      ProcessBuilder builder2 = new ProcessBuilder(command,"--deterministic",propertyString);
//      ProcessBuilder builder3 = new ProcessBuilder(command,"--deterministic",propertyString);
        
        // Using OWL
        String command					= "/home/kush/Projects/robotmotionplanning/owl/build/install/owl/bin/ltl2ldba";
//        String command 					= "/home/kush/Projects/robotmotionplanning/MotionPlanning/lib/ltl2ldba";
        ProcessBuilder builder1 		= new ProcessBuilder(command, propertyString);
        ProcessBuilder builder2 		= new ProcessBuilder(command, propertyString);
        ProcessBuilder builder3 		= new ProcessBuilder(command, propertyString);


        builder1.redirectErrorStream(true);
        Process p1 				= builder1.start();

        builder2.redirectErrorStream(true);
        Process p2 				= builder2.start();
        
        builder3.redirectErrorStream(true);
        Process p3 				= builder3.start();
        
        BufferedReader r 		= new BufferedReader(new InputStreamReader(p3.getInputStream()));
        String line;
        BufferedWriter writer 	= new BufferedWriter(new FileWriter("temp/propertyAutomata.hoa"));
        
        while (true) 
        {
            line 	= r.readLine();
            if (line == null) 
            	break;
            writer.write(line+"\n");
        } 
        writer.close();
        
        try 
        {
			propertyParser		= new BuchiAutomataParser( p1.getInputStream(), 
					p2.getInputStream(), 
					apListSystem);
			System.out.println("done!");
		} catch (ParseException e) 
        {
			e.printStackTrace();
		} 
	}
}
