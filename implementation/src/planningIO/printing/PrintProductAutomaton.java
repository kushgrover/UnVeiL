package planningIO.printing;

import java.io.BufferedWriter;
import java.io.FileWriter;

import abstraction.ProductAutomaton;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDD.BDDIterator;
import settings.PlanningException;
import settings.PlanningSettings;

public class PrintProductAutomaton
{
	ProductAutomaton productAutomaton;
	BufferedWriter writer;
	boolean[] visited;
	public PrintProductAutomaton(ProductAutomaton productAutomaton) throws java.io.IOException, PlanningException {
		visited 				= new boolean[ProductAutomaton.getNumStates()];
		writer 					= new BufferedWriter(new FileWriter(PlanningSettings.get("outputDirectory") + "productAutomaton.dot"));
		
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

	private void addTransitionToDotFile(BDD transition) throws PlanningException, java.io.IOException {
		
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
		
		if(level == 3) {
			writer.append(fromStateID + " -> " + toStateID + " [style=filled, color=green, label=\"" + labelString + " (" + acceptingSet + ")\"];\n");
		}
//		if(level == 2)
//			writer.append(fromStateID + " -> " + toStateID + " [style=filled, color=blue, label=\""  + labelString + " (" + acceptingSet + ")\"];\n");
//		if(level == 1)
//			writer.append(fromStateID + " -> " + toStateID + " [style=filled, color=red, label=\""   + labelString + " (" + acceptingSet + ")\"];\n");;
//		if(level == 0)
//			writer.append(fromStateID + " -> " + toStateID + " [style=filled, color=black, label=\"" + labelString + " (" + acceptingSet + ")\"];\n");
	}
	
	private String getTransitionLabelString(BDD transition) throws PlanningException {
		boolean visited		= false;
		StringBuilder labelString	= new StringBuilder();
		BDD label			= productAutomaton.removeAllExceptLabelVars(transition);
		for(int i=0; i<ProductAutomaton.numVars[2]; i++) 
		{
			if(! label.and(ProductAutomaton.ithVarLabel(i)).equals(ProductAutomaton.factory.zero())) 
			{
				if(visited)
				{
					labelString.append(", ").append(ProductAutomaton.apListProperty.get(i));
				}
				else 
				{
					labelString.append(ProductAutomaton.apListProperty.get(i));
					visited			= true;
				}
			}
			else 
			{
				if(visited)
				{
					labelString = new StringBuilder(labelString + ", -" + ProductAutomaton.apListProperty.get(i));
				}
				else 
				{
					labelString = new StringBuilder(labelString.toString() + '-' + ProductAutomaton.apListProperty.get(i));
					visited			= true;
				}
			}
		}
		return labelString.toString();
	}

	private static String stateLabelString(BDD state) throws PlanningException {
		StringBuilder stateLabel	= new StringBuilder(Integer.toString(state.scanVar(ProductAutomaton.propertyDomainPre()).intValue()));
		if(state.pathCount() != 1)
		{
			throw new PlanningException("More than one state");
		}
		for(int i=0; i<ProductAutomaton.numAPSystem; i++) 
		{
			if(! state.and(ProductAutomaton.ithVarSystemPre(i)).equals(ProductAutomaton.factory.zero())) 
			{
				stateLabel.append(", ").append(ProductAutomaton.apListSystem.get(i));
			}
		}
		return stateLabel.toString();
	}
}
