package planningIO.printing;

import java.io.BufferedWriter;
import java.io.FileWriter;

import abstraction.ProductAutomaton;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDD.BDDIterator;
import settings.PlanningException;

public class PrintProductAutomaton
{
	ProductAutomaton productAutomaton;
	BufferedWriter writer;
	boolean visited[];
	public PrintProductAutomaton(ProductAutomaton productAutomaton, int num) throws Exception
	{
		visited 				= new boolean[ProductAutomaton.getNumStates()];
		writer 					= new BufferedWriter(new FileWriter("/home/kush/Projects/robotmotionplanning/MotionPlanning/temp/productAutomaton"+num+".dot"));
		
		this.productAutomaton	= productAutomaton;
		BDD productAutomatonBDD = productAutomaton.getBDD();
		
		writer.write("digraph G {\n");
		BDDIterator iterator	= productAutomatonBDD.iterator(ProductAutomaton.allVars());
		
		while(iterator.hasNext()) 
		{
			BDD transition		= (BDD) iterator.next();
			addTransitionToDotFile(transition);
		}
		writer.append("}");
		
		writer.close();
	}

	private void addTransitionToDotFile(BDD transition) throws Exception{
		
		BDD fromState		= productAutomaton.removeAllExceptPreVars(transition);
		BDD toState			= productAutomaton.removeAllExceptPostVars(transition);
		toState				= productAutomaton.changePostVarsToPreVars(toState);
		int fromStateID		= productAutomaton.getStateID(fromState);
		int toStateID		= productAutomaton.getStateID(toState);
		int acceptingSet 	= transition.scanVar(ProductAutomaton.acceptingSetDomain()).intValue();

		if(! visited[fromStateID])
		{
			visited[fromStateID]	= true;
			writer.append(fromStateID + " [label=\"" + stateLabelString(fromState) + "\"];\n");
		}
		if(! visited[toStateID]) 
		{
			visited[toStateID]		= true;
			writer.append(toStateID + " [label=\"" + stateLabelString(toState) + "\"];\n");
		}

		int level			= transition.scanVar(ProductAutomaton.transitionLevelDomain()).intValue();
		String labelString	= getTransitionLabelString(transition);
		switch(level)
		{
		case(3):
			writer.append(fromStateID + " -> " + toStateID + " [style=filled, color=green, label=\"" + labelString + " (" + acceptingSet + ")\"];\n");
		case(2):
			writer.append(fromStateID + " -> " + toStateID + " [style=filled, color=blue, label=\""  + labelString + " (" + acceptingSet + ")\"];\n");
		case(1):
//			writer.append(fromStateID + " -> " + toStateID + " [style=filled, color=red, label=\""   + labelString + " (" + acceptingSet + ")\"];\n");;
		case(0):
//			writer.append(fromStateID + " -> " + toStateID + " [style=filled, color=black, label=\"" + labelString + " (" + acceptingSet + ")\"];\n");
		}
	}
	
	private String getTransitionLabelString(BDD transition) throws Exception
	{
		boolean visited		= false;
		String labelString	= "";
		BDD label			= productAutomaton.removeAllExceptLabelVars(transition);
		for(int i=0; i<ProductAutomaton.numVars[2]; i++) 
		{
			if(! label.and(ProductAutomaton.ithVarLabel(i)).equals(ProductAutomaton.factory.zero())) 
			{
				if(visited == true) 
				{
					labelString 	= labelString.concat(", "+ProductAutomaton.apListProperty.get(i));
				}
				else 
				{
					labelString		= labelString.concat(ProductAutomaton.apListProperty.get(i));
					visited			= true;
				}
			}
			else 
			{
				if(visited == true) 
				{
					labelString		= labelString.concat(", -"+ProductAutomaton.apListProperty.get(i));
				}
				else 
				{
					labelString		= labelString.concat("-"+ProductAutomaton.apListProperty.get(i));
					visited			= true;
				}
			}
		}
		return labelString;
	}

	private String stateLabelString(BDD state) throws Exception 
	{
		String stateLabel	= Integer.toString(state.scanVar(ProductAutomaton.propertyDomainPre()).intValue());
		if(state.pathCount() != 1)
		{
			throw new PlanningException("More than one state");
		}
		for(int i=0; i<ProductAutomaton.numAPSystem; i++) 
		{
			if(! state.and(ProductAutomaton.ithVarSystemPre(i)).equals(ProductAutomaton.factory.zero())) 
			{
				stateLabel	= stateLabel.concat(", "+ProductAutomaton.apListSystem.get(i));
			}
		}
		return stateLabel;
	}
}
