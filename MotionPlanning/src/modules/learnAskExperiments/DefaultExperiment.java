package modules.learnAskExperiments;


import java.util.ArrayList;

import abstraction.ProductAutomaton;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDD.BDDIterator;
import settings.PlanningException;
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
	BDD productAutomatonBDD;
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
	public BDD learn(BDD transitions) throws Exception 
	{
		double startTime = System.nanoTime();
//		BDD transition		= fromState.and(productAutomaton.changePreSystemVarsToPostSystemVars(toState));
		BDD productTransitions = ProductAutomaton.factory.zero();
		if(transitions.and(productAutomaton.sampledTransitions).isZero())
		{
			productAutomaton.sampledTransitions			= productAutomaton.sampledTransitions.or(transitions);
			productAutomaton.removeTransition(transitions);
			productTransitions 							= productAutomaton.addTransition(transitions, 3);
			productAutomaton.sampledProductTransitions 	= productAutomaton.sampledProductTransitions.or(productTransitions);
		}
		else
		{
			time += System.nanoTime() - startTime;

			return null;
		}

		BDD nextTransition, fromState, toState;
		BDDIterator it = transitions.iterator(ProductAutomaton.allSystemVars());
		while(it.hasNext()) {
			nextTransition = (BDD) it.next();
			fromState = nextTransition.exist(ProductAutomaton.allPostSystemVars());
			toState = productAutomaton.changePostSystemVarsToPreSystemVars(nextTransition.exist(ProductAutomaton.allPreSystemVars()));
//			fromState.printDot();
//			toState.printDot();
			learnSimilarTransitions(fromState, toState);
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
		
		return productTransitions;
	}

	/**
	 * procedure to add maybe (level 2) transitions give a transition from 'fromState' to 'toState'
	 * @param fromState
	 * @param toState
	 * @return BDD representing the set of similar transitions
	 * @throws Exception
	 */
	private BDD learnSimilarTransitions(BDD fromState, BDD toState) throws Exception
	{
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
		return transitions;
	}

	/**
	 * 
	 * @param fromState
	 * @param toState
	 * @return complement of the set 'domain of changes'
	 * @throws Exception
	 */
	private BDD allExceptDomainOfChanges(BDD fromState, BDD toState) throws Exception
	{
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
	public ArrayList<BDD> getAdvice(BDD currentStates) throws Exception
	{
		ArrayList<BDD> advice	= new ArrayList<BDD>();

//    	---------------------------------------------------------------------------------------
//    	---------------------------------------------------------------------------------------
	
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
			
//			BDDIterator it = advice.get(i-1).iterator(ProductAutomaton.allSystemVars());
//			System.out.println("**************" + i);
//			while(it.hasNext()) {
//				printAPList((BDD) it.next());
//			}
//			System.out.println("\n**  **  **  **");
		}
		
//    	---------------------------------------------------------------------------------------
			
//		reachableStates.add(productAutomaton.finalStatesSystem());
//		reachableStates.add(productAutomaton.preImageOfFinalStatesSystem());
//		
//		if(reachableStates.get(0).isZero() || reachableStates.get(1).isZero()) {
//			return reachableStates;
//		}
//		
//		int i	= 1;
//		BDD backwardReachableStates		= reachableStates.get(1);
//		while(!productAutomaton.preImageSystem(reachableStates.get(i)).and(backwardReachableStates.not()).isZero()) {
//			reachableStates.add(productAutomaton.preImageSystem(reachableStates.get(i)));
//			i++;
//			backwardReachableStates		= backwardReachableStates.or(reachableStates.get(i));
//			BDDIterator it = reachableStates.get(i).iterator(ProductAutomaton.allPreSystemVars());
//			System.out.println("**************" + i);
//			while(it.hasNext()) {
//				printAPList((BDD) it.next());
//			}
//			System.out.println("\n**  **  **  **");
//		}
		
//    	---------------------------------------------------------------------------------------
		
//		reachableStates.add(ProductAutomaton.factory.one());
//		reachableStates.add(ProductAutomaton.factory.one());

//    	---------------------------------------------------------------------------------------
//    	---------------------------------------------------------------------------------------
	
		return advice;
		
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
	
	
	
	/**
	 * Some filters for the states like H and r1 can never occur together
	 * User gives it at the beginning
	 * @return a BDD representing the formula for the filters
	 * @throws Exception
	 */
	public BDD addFilters() throws Exception 
	{
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
	
	public ProductAutomaton getProductAutomaton() 
	{
		return productAutomaton;
	}
	
}
