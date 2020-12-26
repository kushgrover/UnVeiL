package planningIO;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import environment.Environment;
import environment.Label;
import settings.PlanningSettings;

public class EnvironmentReader 
{
	public static Environment env;
	
	public EnvironmentReader(String envFile, String labelFile) throws IOException
	{
		
		Point2D.Float p1, p2, p3, p4, init;
		ArrayList<Path2D> obstacles = new ArrayList<Path2D>();
		ArrayList<Path2D> walls		= new ArrayList<Path2D>();
		
		
		BufferedReader reader 		= new BufferedReader(new FileReader(envFile));

		
		String point 				= "\\(\\s*(\\d*|\\d*\\.\\d*)\\s*,\\s*(\\d*|\\d*\\.\\d*)\\s*\\)";
        String rectangle 			= "\\[\\s*" + point + "\\s*,\\s*" + point + "\\s*,\\s*" + point + "\\s*,\\s*" + point + "\\s*\\]";

        
        
		
		// For env boundaries
		String line 	= reader.readLine();
		Pattern p 		= Pattern.compile(rectangle);
		Matcher m 		= p.matcher(line);
		m.find();
		p1 	= new Point2D.Float(Float.parseFloat(m.group(1)), Float.parseFloat(m.group(2)));
		p2 	= new Point2D.Float(Float.parseFloat(m.group(3)), Float.parseFloat(m.group(4)));
		p3 	= new Point2D.Float(Float.parseFloat(m.group(5)), Float.parseFloat(m.group(6)));
		p4 	= new Point2D.Float(Float.parseFloat(m.group(7)), Float.parseFloat(m.group(8)));
		
		if((boolean) PlanningSettings.get("planning.verbosity"))
		{
			System.out.println("Environment boundaries: " + p1.toString() + ", " + p2.toString() + ", " + p3.toString() + ", " + p4.toString());
		}
		
		
		
		
		// For initial point
		line 	= reader.readLine();
        p 		= Pattern.compile(point);
        m 		= p.matcher(line);
        m.find();
		init 	= new Point2D.Float(Float.parseFloat(m.group(1)), Float.parseFloat(m.group(2)));
		
		if ((boolean) PlanningSettings.get("planning.verbosity"))
		{
			System.out.println("\n\nInitial Point: " + init.toString());
		}
		
		
		
		
		// For walls
		p 		= Pattern.compile(rectangle);
		Path2D rect;
		int i 	= 0;
		if((boolean) PlanningSettings.get("planning.verbosity"))
        {
        	System.out.println("\n\nWalls: ");
        }
		
		line = reader.readLine();
		while(line != null)
		{
			if(line.equalsIgnoreCase("obstacles")) {
				break;
			}
			System.out.println(line);
			try
			{
				m = p.matcher(line);
			} catch (Exception E) {
				continue;
			}
			m.find();
			
			rect = new Path2D.Float();
			rect.moveTo(Float.parseFloat(m.group(1)), Float.parseFloat(m.group(2)));
			rect.lineTo(Float.parseFloat(m.group(3)), Float.parseFloat(m.group(4)));
			rect.lineTo(Float.parseFloat(m.group(5)), Float.parseFloat(m.group(6)));
			rect.lineTo(Float.parseFloat(m.group(7)), Float.parseFloat(m.group(8)));
			rect.lineTo(Float.parseFloat(m.group(1)), Float.parseFloat(m.group(2)));
			rect.closePath();
			walls.add(rect);
			
			
			if((boolean) PlanningSettings.get("planning.verbosity"))
            {
            	System.out.println(rect.getBounds2D());
            }
			
			i++;
			line = reader.readLine();
		}
		
		
		
		// For obstacles
		p 		= Pattern.compile(rectangle);
		i 	= 0;
		if((boolean) PlanningSettings.get("planning.verbosity"))
        {
        	System.out.println("\n\nObstacles: ");
        }
		while(line != null)
		{
			line = reader.readLine();
			try
			{
				m = p.matcher(line);
			} catch (Exception E) {
				continue;
			}
			m.find();
			
			rect = new Path2D.Float();
			rect.moveTo(Float.parseFloat(m.group(1)), Float.parseFloat(m.group(2)));
			rect.lineTo(Float.parseFloat(m.group(3)), Float.parseFloat(m.group(4)));
			rect.lineTo(Float.parseFloat(m.group(5)), Float.parseFloat(m.group(6)));
			rect.lineTo(Float.parseFloat(m.group(7)), Float.parseFloat(m.group(8)));
			rect.lineTo(Float.parseFloat(m.group(1)), Float.parseFloat(m.group(2)));
			rect.closePath();
			obstacles.add(rect);
			
			
			if((boolean) PlanningSettings.get("planning.verbosity"))
            {
            	System.out.println(rect.getBounds2D());
            }
			i++;
		}
				        
		reader.close();
        
        
        
        
        // For Label
        reader 				= new BufferedReader(new FileReader(labelFile));
        line 				= reader.readLine();
        
        String apName 		= "\\s*(\\w*)\\s*\\:\\s*";
    	Pattern apNameRegex			= Pattern.compile(apName);
    	Pattern rectRegex 	= Pattern.compile(rectangle);
        
    	
    	ArrayList<Path2D> labelRect = new ArrayList<Path2D>();
    	ArrayList<String> apList 	= new ArrayList<String>();
    	i	= 0;
        if ((boolean) PlanningSettings.get("planning.verbosity"))
        {
        	System.out.println("\n\nLabelling: ");
        }

        while (line != null)
        {
        	m 		= apNameRegex.matcher(line);
        	m.find();
        	apList.add(m.group(1));
        	if((boolean) PlanningSettings.get("planning.verbosity"))
            {
            	System.out.print(apList.get(i) + ": ");
            }
        	
        	
        	m		= rectRegex.matcher(line);

    		rect 	= new Path2D.Float();
        	while(m.find())
        	{
        		rect.moveTo(Float.parseFloat(m.group(1)), Float.parseFloat(m.group(2)));
        		rect.lineTo(Float.parseFloat(m.group(3)), Float.parseFloat(m.group(4)));
        		rect.lineTo(Float.parseFloat(m.group(5)), Float.parseFloat(m.group(6)));
        		rect.lineTo(Float.parseFloat(m.group(7)), Float.parseFloat(m.group(8)));
        		rect.lineTo(Float.parseFloat(m.group(1)), Float.parseFloat(m.group(2)));
        		if((boolean) PlanningSettings.get("planning.verbosity"))
        		{
        			System.out.print(rect.getBounds2D() + "  ");
        		}
        	}
    		rect.closePath();
    		labelRect.add(rect);

        	if((boolean) PlanningSettings.get("planning.verbosity"))
    		{
    			System.out.println("");
    		}
        	line = reader.readLine();
        	
        	
        	i++;
        }
        Label labelling 	= new Label(apList, labelRect);

        env = new Environment(new float[] {p1.x, p3.x}, new float[] {p1.y, p3.y}, obstacles, walls, init, labelling);
	}
	
	
}
