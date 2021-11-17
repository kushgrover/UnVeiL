package modules.learnAskExperiments;


import java.util.ArrayList;

import abstraction.ProductAutomaton;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDD.BDDIterator;
import settings.PlanningException;
import settings.PlanningSettings;
import net.sf.javabdd.BDDFactory;

/**
 * <p> Original algo for learn and ask procedures</p>
 * @author kush
 *
 */


public class DefaultExperiment implements Experiments
{
	
	ProductAutomaton productAutomaton;
	BDDFactory factory;
//	BDD productAutomatonBDD = null;
	public double time;
	
	
	/**
	 * constructor
	 * @param productAutomaton
	 */
	public DefaultExperiment(ProductAutomaton productAutomaton) 
	{
		this.productAutomaton		= productAutomaton;
		this.factory				= productAutomaton.getBDD().getFactory();
//		this.productAutomatonBDD	= productAutomaton.getBDD();
		this.time = 0;
	}

	/**
	 * LEARN procedure
	 */
	@Override
	public void learn(BDD transitions) throws PlanningException {
		double startTime = System.nanoTime();
//		BDD transition		= fromState.and(productAutomaton.changePreSystemVarsToPostSystemVars(toState));
		if(transitions.and(productAutomaton.sampledTransitions).isZero()) {
			productAutomaton.sampledTransitions			= productAutomaton.sampledTransitions.or(transitions);
			productAutomaton.removeTransition(transitions);
			BDD productTransitions = productAutomaton.addTransition(transitions, 3);
			productAutomaton.sampledProductTransitions 	= productAutomaton.sampledProductTransitions.or(productTransitions);
		}
		else {
			time += System.nanoTime() - startTime;
			return;
		}

		if((boolean) PlanningSettings.get("useAdvice")) {
			if (!productAutomaton.removeAllExceptPreSystemVars(transitions).equals(productAutomaton.changePostSystemVarsToPreSystemVars(productAutomaton.removeAllExceptPostSystemVars(transitions)))){
				BDDIterator it = transitions.iterator(ProductAutomaton.allSystemVars());
				while (it.hasNext()) {
					BDD nextTransition = (BDD) it.next();
					BDD fromState = nextTransition.exist(ProductAutomaton.allPostSystemVars());
					BDD toState = productAutomaton.changePostSystemVarsToPreSystemVars(nextTransition.exist(ProductAutomaton.allPreSystemVars()));
					learnSimilarTransitions(fromState, toState);
				}
			}
		}
		
//		int counter			= productAutomaton.increaseCounter(productAutomaton.getFirstState(fromState));
//		BDDIterator ite = fromState.iterator(ProductAutomaton.allPreSystemVars());
//		int counter;
//		while(ite.hasNext())
//		{
//			counter			= productAutomaton.increaseCounter((BDD) ite.next());
//			// never tested this
//			if(counter > ProductAutomaton.threshold) 
//			{
//				productAutomaton.setLevel(fromState, 1);
//			}
//		}
		
		time += System.nanoTime() - startTime;
	}

	/**
	 * procedure to add maybe (level 2) transitions give a transition from 'fromState' to 'toState'
	 * @param fromState
	 * @param toState
	 * @throws PlanningException
	 */
	private void learnSimilarTransitions(BDD fromState, BDD toState) throws PlanningException {
		BDD complementDomainOfChanges	= allExceptDomainOfChanges(fromState,toState);
		BDD fromStateSimilar			= fromState.exist(complementDomainOfChanges);
		BDD toStateSimilarPrime			= factory.one();
		for(int i=0; i<ProductAutomaton.numAPSystem; i++)
		{
			if(fromStateSimilar.and(ProductAutomaton.ithVarSystemPre(i)).isZero())
			{
				toStateSimilarPrime.andWith((ProductAutomaton.ithVarSystemPost(i)));
			}
			else if(fromStateSimilar.and(ProductAutomaton.ithVarSystemPre(i).not()).isZero()) 
			{
				toStateSimilarPrime.andWith(ProductAutomaton.ithVarSystemPost(i).not());
			} 
		}
		
		BDD allOtherVariablesWhichRemainEqual	= factory.one();
		BDD support 							= fromStateSimilar.support();
		for(int i=0; i<ProductAutomaton.numAPSystem; i++) 
		{
			if(! support.and(ProductAutomaton.ithVarSystemPre(i)).equals(support)) 
			{
				allOtherVariablesWhichRemainEqual.andWith(ProductAutomaton.ithVarSystemPre(i).biimp(ProductAutomaton.ithVarSystemPost(i)));
			}
		}
		BDD transitions		= fromStateSimilar.and(toStateSimilarPrime).and(ProductAutomaton.transitionLevelDomain().ithVar(2));
		transitions.andWith(allOtherVariablesWhichRemainEqual);
		transitions			= transitions.and(ProductAutomaton.getPropertyBDD()).and(ProductAutomaton.getLabelEquivalence());
		transitions			= transitions.and(productAutomaton.sampledTransitions.not());
		
		//adding filters here
		productAutomaton.addTransitions(transitions.and(addFilters()));
	}

	/**
	 * 
	 * @param fromState
	 * @param toState
	 * @return complement of the set 'domain of changes'
	 * @throws PlanningException
	 */
	private BDD allExceptDomainOfChanges(BDD fromState, BDD toState) throws PlanningException {
		BDD complementDomainOfChanges=factory.one();
		for(int i=0; i<ProductAutomaton.numAPSystem; i++) 
		{
			BDD tempFromState	= fromState.simplify(fromState.simplify(ProductAutomaton.ithVarSystemPre(i)));
			BDD tempToState		= toState.simplify(toState.simplify(ProductAutomaton.ithVarSystemPre(i)));
			if(tempFromState.equals(tempToState)) 
			{
				complementDomainOfChanges.andWith(ProductAutomaton.ithVarSystemPre(i));
			}
		}
		return complementDomainOfChanges;
	}

	
	/**
	 * ASK procedure
	 */
	@Override
	public ArrayList<BDD> getAdvice(BDD currentStates) throws PlanningException {
		ArrayList<BDD> advice	= new ArrayList<>();
		BDD target = productAutomaton.finalStatesSystem();
		BDD source = productAutomaton.preImageOfFinalStatesSystem();
		
		advice.add(source.and(productAutomaton.changePreSystemVarsToPostSystemVars(target)));
		BDD totalTransitions = advice.get(0);
		
		int i = 1;
		while(! productAutomaton.incomingTransitions(source).and(totalTransitions.not()).isZero()) {
			advice.add(productAutomaton.incomingTransitions(source));
			source = productAutomaton.removeAllExceptPreSystemVars(advice.get(i));
			totalTransitions = totalTransitions.or(advice.get(i));
			i++;
		}
		return advice;
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
	
	
	
	/**
	 * Some filters for the states like H and r1 can never occur together
	 * User gives it at the beginning
	 * @return a BDD representing the formula for the filters
	 * @throws PlanningException
	 */
	public BDD addFilters() throws PlanningException {
		BDD h	= ProductAutomaton.ithVarSystemPre(0);
		BDD r1	= ProductAutomaton.ithVarSystemPre(1);
		BDD r2	= ProductAutomaton.ithVarSystemPre(2);
		BDD r3	= ProductAutomaton.ithVarSystemPre(3);
		BDD r4	= ProductAutomaton.ithVarSystemPre(4);
		BDD r5	= ProductAutomaton.ithVarSystemPre(5);
		BDD r6	= ProductAutomaton.ithVarSystemPre(6);
//		BDD c	= ProductAutomaton.ithVarSystemPre(5);
//		BDD t	= ProductAutomaton.ithVarSystemPre(3);
//		BDD b	= ProductAutomaton.ithVarSystemPre(7);
//		BDD filter1	= h.imp((r1.or(r2).or(r3).or(r4).or(r5).or(r6).or(b)).not());
		BDD filter2	= r1.imp((h.or(r2).or(r3).or(r4).or(r5).or(r6)).not());
		BDD filter3	= r2.imp((h.or(r1).or(r3).or(r4).or(r5).or(r6)).not());
		BDD filter4	= r3.imp((h.or(r1).or(r2).or(r4).or(r5).or(r6)).not());
		BDD filter5 = r4.imp((h.or(r1).or(r2).or(r3).or(r5).or(r6)).not());
		BDD filter6	= r5.imp((h.or(r1).or(r2).or(r3).or(r4).or(r6)).not());
		BDD filter7	= r6.imp((h.or(r1).or(r2).or(r3).or(r4).or(r5)).not());
		BDD filter	= (filter2).and(filter3).and(filter4).and(filter5).and(filter6).and(filter7);
		BDD filterPrime	= productAutomaton.changePreVarsToPostVars(filter);
		return filter.and(filterPrime);
		
		
		
//		return factory.one();
	}
	
	@Override
	public ProductAutomaton getProductAutomaton()
	{
		return productAutomaton;
	}
	
}
