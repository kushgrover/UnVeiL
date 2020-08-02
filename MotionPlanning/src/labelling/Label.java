package labelling;

import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import transitionSystem.ProductAutomaton;

public class Label 
{
	int numOfAP;
	ArrayList<Path2D> areas;
	ArrayList<String> apListSystem;
	BDDFactory factory;
	
	public Label(ArrayList<String> apListSystem, ArrayList<Path2D> areas) 
	{
		this.numOfAP 		= areas.size();
		this.apListSystem 	= apListSystem;
		this.areas 			= areas;
		this.factory 		= ProductAutomaton.factory;
	}
	
	public BDD getLabel(Point2D x) throws Exception 
	{
		BDD label 	= factory.one();
		
		for(int i=0; i<numOfAP; i++) 
		{
			if(checkIthAP(x, i)) 
			{
				label.andWith(ProductAutomaton.ithVarSystemPre(i));
			}
			else
			{
				label.andWith(ProductAutomaton.ithVarSystemPre(i).not());
			}
		}
		return label;
	}
	
	private Boolean checkIthAP(Point2D x, int i)
	{
		
		return areas.get(i).contains(x);
	}
	
	public ArrayList<String> getApListSystem()
	{
		return apListSystem;
	}
}
