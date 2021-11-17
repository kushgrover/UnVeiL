package planningIO.printing;

import java.util.ArrayList;

import abstraction.ProductAutomaton;
import net.sf.javabdd.BDD;
import settings.PlanningException;

public final class PrintAcceptingPath
{
	public PrintAcceptingPath(Iterable<BDD> path) throws PlanningException
	{
		if(path == null){
			return;
		}
		for (BDD next : path) {
			printAPList(next);
		}
		System.out.print("\n\n");
	}

	public static void printAPList(BDD state) throws PlanningException
	{
		ArrayList<String> apList	= findAPList(state);
		System.out.print("[");
		for(int j=0; j<apList.size(); j++) 
		{
			if(j < apList.size() - 1) 
			{
				System.out.print(apList.get(j)+ ',');
			}
			else 
			{
				System.out.print(apList.get(j));
			}
		}
		System.out.print("]  ");
	}

	private static ArrayList<String> findAPList(BDD state) throws PlanningException
	{
		ArrayList<String> list		= new ArrayList<>();
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
