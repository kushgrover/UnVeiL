package planningIO.printing;

import java.util.ArrayList;

import abstraction.ProductAutomaton;
import net.sf.javabdd.BDD;
import settings.PlanningException;

public class PrintAcceptingPath 
{
	public PrintAcceptingPath(ArrayList<BDD> path) throws PlanningException 
	{
		if(path == null){
			return;
		}
		BDD next;
		for(int i=0; i<path.size(); i++) 
		{
			next	= path.get(i);
//			next.printDot();
			printAPList(next);
		}
		System.out.print("\n\n");
	}

	public void printAPList(BDD state) throws PlanningException 
	{
		ArrayList<String> apList	= findAPList(state);
		System.out.print("[");
		for(int j=0; j<apList.size(); j++) 
		{
			if(j < apList.size() - 1) 
			{
				System.out.print(apList.get(j)+",");
			}
			else 
			{
				System.out.print(apList.get(j));
			}
		}
		System.out.print("]  ");
	}

	private ArrayList<String> findAPList(BDD state) throws PlanningException 
	{
		ArrayList<String> list		= new ArrayList<String>();
		for(int i=0; i<ProductAutomaton.numAPSystem; i++) 
		{
			if(! state.and(ProductAutomaton.ithVarSystemPre(i)).isZero()) 
			{
				list.add(ProductAutomaton.apListSystem.get(i));
			}
		}
		list.add(Integer.toString(state.scanVar(ProductAutomaton.propertyDomainPre()).intValue()));
		return list;
	}
}
