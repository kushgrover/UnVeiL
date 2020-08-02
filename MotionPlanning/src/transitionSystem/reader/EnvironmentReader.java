package transitionSystem.reader;

import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import labelling.Label;
import modules.PlanningSettings;
import modules.motionPlanner.Environment;

public class EnvironmentReader 
{
	
	public static Environment env;
	
	public EnvironmentReader(String envFile, String labelFile) throws IOException
	{
		
		Point2D.Float p1, p2, p3, p4, init;
		ArrayList<Path2D> obstacles = new ArrayList<Path2D>();
		
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
			System.out.println("Initial Point: " + init.toString());
		}
		
		
		
		
		// For obstacles
		p 		= Pattern.compile(rectangle);
		Path2D rect;
		int i 	= 0;
		if((boolean) PlanningSettings.get("planning.verbosity"))
        {
        	System.out.println("Obstacles: ");
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
			rect.closePath();
			obstacles.add(rect);
			
			
			if((boolean) PlanningSettings.get("planning.verbosity"))
            {
            	System.out.println(rect.toString());
            }
			i++;
		}
		
        reader.close();
        
        
        
        
        // For Label
        reader 			= new BufferedReader(new FileReader(labelFile));
        line 			= reader.readLine();
        
        String apRegex 	= "\\s*(\\w*)\\s*\\:\\s*"+rectangle;
    	p 				= Pattern.compile(apRegex);
        
    	
    	ArrayList<Path2D> labelRect = new ArrayList<Path2D>();
    	ArrayList<String> apList 	= new ArrayList<String>();
    	i	= 0;
        if ((boolean) PlanningSettings.get("planning.verbosity"))
        {
        	System.out.println("Labelling: ");
        }

        while (line != null)
        {
        	m 		= p.matcher(line);
        	m.find();
        	rect 	= new Path2D.Float();
			rect.moveTo(Float.parseFloat(m.group(2)), Float.parseFloat(m.group(3)));
			rect.lineTo(Float.parseFloat(m.group(4)), Float.parseFloat(m.group(5)));
			rect.lineTo(Float.parseFloat(m.group(6)), Float.parseFloat(m.group(7)));
			rect.lineTo(Float.parseFloat(m.group(8)), Float.parseFloat(m.group(9)));
			rect.closePath();
        	labelRect.add(rect);
        	apList.add(m.group(1));
        	line = reader.readLine();

            if((boolean) PlanningSettings.get("planning.verbosity"))
            {
            	System.out.println(apList.get(i) + ": " + rect.toString());
            }
        	i++;
        }
        Label labelling 	= new Label(apList, labelRect);

        env = new Environment(new float[] {p1.x, p3.x}, new float[] {p1.y, p3.y}, obstacles, init, labelling);
	}
	
	
//	public static void main(String[] args) throws IOException 
//	{
//		String x = "[(0,0.95), (0.3,0.95), (0.3,1.05), (0,1.05)]";
//		String y = "(0.3,0.95)";
//		String point = "\\(\\s*(\\d*|\\d*\\.\\d*)\\s*,\\s*(\\d*|\\d*\\.\\d*)\\s*\\)";
//        String rectangle = "\\[\\s*" + point + "\\s*,\\s*" + point + "\\s*,\\s*" + point + "\\s*,\\s*" + point + "\\s*\\]";
//		Pattern p = Pattern.compile(point);
//		Matcher m = p.matcher(y);
//		m.find();
//		System.out.println(m.group());
//		String envFile="/home/kush/Projects/robotmotionplanning/MotionPlanning/Examples/Example1/environment.env";
//		String labelFile="/home/kush/Projects/robotmotionplanning/MotionPlanning/Examples/Example1/label.lb";
//		EnvironmentReader r = new EnvironmentReader(envFile, labelFile);
//	}
	
}
