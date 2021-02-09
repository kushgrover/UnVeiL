package planningIO;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import jhoafparser.parser.generated.ParseException;

public class PropertyReader 
{
	public BuchiAutomataParser propertyParser;
	
	
	public PropertyReader(ArrayList<String> apListSystem, String propertyFile) throws IOException 
	{
		// Read property from the file
		BufferedReader propertyReader 	= new BufferedReader(new FileReader(propertyFile));
        String propertyString			= propertyReader.readLine();
        propertyReader.close();
        
        // Output 
        System.out.println("\nProperty to satisfty: " + propertyString + "\n\n");
        
    	
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
        BufferedWriter writer 	= new BufferedWriter(new FileWriter("/home/kush/Projects/robotmotionplanning/MotionPlanning/temp/propertyAutomata.hoa"));
        
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
		} catch (ParseException e) 
        {
			e.printStackTrace();
		} 
	}
}
