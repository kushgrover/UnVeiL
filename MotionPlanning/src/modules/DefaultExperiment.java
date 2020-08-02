package modules;


import java.util.ArrayList;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import transitionSystem.ProductAutomaton;
import transitionSystem.extraExceptions.learningException;

/**
 * <p> Original algo for learn and ask procedures</p>
 * @author kush
 *
 */


public class DefaultExperiment implements Experiments{
	
	ProductAutomaton productAutomaton;
	BDDFactory factory;
	BDD productAutomatonBDD;
	

//	public DefaultExperiment(BDD propertyBDD, 
//			int[] numVars, 
//			ArrayList<String> apListProperty,
//			ArrayList<String> apListSystem) throws Exception
//	{
//		this.productAutomaton=new ProductAutomaton(propertyBDD);
//		this.factory=productAutomaton.getBDD().getFactory();
//		this.productAutomatonBDD=productAutomaton.getBDD();
//	}
	
	/**
	 * constructor
	 * @param productAutomaton
	 */
	public DefaultExperiment(ProductAutomaton productAutomaton) {
		this.productAutomaton=productAutomaton;
		this.factory=productAutomaton.getBDD().getFactory();
		this.productAutomatonBDD=productAutomaton.getBDD();
	}

	/**
	 * LEARN procedure
	 */
	@Override
	public BDD learn(BDD fromState, BDD toState) throws Exception {
		BDD transition=fromState.and(productAutomaton.changePreSystemVarsToPostSystemVars(toState));
		if(transition.and(productAutomaton.sampledTransitions).isZero()) {
			productAutomaton.sampledTransitions=productAutomaton.sampledTransitions.or(transition);
			productAutomaton.removeTransition(fromState, toState);
			transition=productAutomaton.addTransition(fromState, toState, 3);
			productAutomaton.sampledProductTransitions=productAutomaton.sampledProductTransitions.or(transition);
		}
		else {
			throw new learningException("Transition already learned");
		}
		
		learnSimilarTransitions(fromState, toState);
		  
		
		int counter=productAutomaton.increaseCounter(productAutomaton.getFirstState(fromState));
		
		
		
		// never tested this
		if(counter>ProductAutomaton.threshold) {
			productAutomaton.setLevel(fromState,1);
		}
		return transition;
	}

	/**
	 * procedure to add maybe (level 2) transitions give a transition from 'fromState' to 'toState'
	 * @param fromState
	 * @param toState
	 * @return BDD representing the set of similar transitions
	 * @throws Exception
	 */
	private BDD learnSimilarTransitions(BDD fromState, BDD toState) throws Exception{
		BDD complementDomainOfChanges=allExceptDomainOfChanges(fromState,toState);
		BDD fromStateSimilar=fromState.exist(complementDomainOfChanges);
		BDD toStateSimilarPrime=factory.one();
		for(int i=0;i<ProductAutomaton.numAPSystem;i++) {
			if(fromStateSimilar.and(ProductAutomaton.ithVarSystemPre(i)).isZero()) {
				toStateSimilarPrime.andWith((ProductAutomaton.ithVarSystemPost(i)));
			}
			else if(fromStateSimilar.and(ProductAutomaton.ithVarSystemPre(i).not()).isZero()) {
				toStateSimilarPrime.andWith(ProductAutomaton.ithVarSystemPost(i).not());
			} 
		}
		
		BDD allOtherVariablesWhichRemainEqual=factory.one();
		BDD support = fromStateSimilar.support();
		for(int i=0;i<ProductAutomaton.numAPSystem;i++) {
			if(! support.and(ProductAutomaton.ithVarSystemPre(i)).equals(support)) {
				allOtherVariablesWhichRemainEqual.andWith(ProductAutomaton.ithVarSystemPre(i).biimp(ProductAutomaton.ithVarSystemPost(i)));
			}
		}
		BDD transitions=fromStateSimilar.and(toStateSimilarPrime).and(ProductAutomaton.transitionLevelDomain().ithVar(2));
		transitions.andWith(allOtherVariablesWhichRemainEqual);
		transitions=transitions.and(ProductAutomaton.getPropertyBDD()).and(ProductAutomaton.getLabelEquivalence());
		transitions=transitions.and(productAutomaton.sampledTransitions.not());
		
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
	private BDD allExceptDomainOfChanges(BDD fromState, BDD toState) throws Exception{
		BDD complementDomainOfChanges=factory.one();
		for(int i=0;i<ProductAutomaton.numAPSystem;i++) {
			BDD tempFromState=fromState.simplify(fromState.simplify(ProductAutomaton.ithVarSystemPre(i)));
			BDD tempToState=toState.simplify(toState.simplify(ProductAutomaton.ithVarSystemPre(i)));
			if(tempFromState.equals(tempToState)) {
				complementDomainOfChanges.andWith(ProductAutomaton.ithVarSystemPre(i));
			}
		}
		return complementDomainOfChanges;
	}

	
	/**
	 * ASK procedure
	 */
	@Override
	public ArrayList<BDD> ask(BDD currentStates) throws Exception {
		ArrayList<BDD> reachableStates=new ArrayList<BDD>();
		reachableStates.add(productAutomaton.finalStates());
		reachableStates.add(productAutomaton.preImageOfFinalStates());
		if(reachableStates.get(0).isZero() || reachableStates.get(1).isZero()) {
			return reachableStates;
		}
		
		int i=1;
		BDD backwardReachableStates=productAutomaton.preImageOfFinalStates();
		while(!productAutomaton.preImage(backwardReachableStates).and(backwardReachableStates.not()).isZero()) {
			reachableStates.add(productAutomaton.preImage(reachableStates.get(i)));
			i++;
			backwardReachableStates=backwardReachableStates.or(reachableStates.get(i));
		}
		return reachableStates;
	}
	
	
	
	/**
	 * Some filters for the states like H and r1 can never occur together
	 * User gives it at the beginning
	 * @return a BDD representing the formula for the filters
	 * @throws Exception
	 */
	public BDD addFilters() throws Exception {
//		BDD h=ProductAutomaton.ithVarSystemPre(0);
//		BDD r1=ProductAutomaton.ithVarSystemPre(1);
//		BDD r2=ProductAutomaton.ithVarSystemPre(2);
//		BDD r3=ProductAutomaton.ithVarSystemPre(3);
//		BDD r4=ProductAutomaton.ithVarSystemPre(4);
//		BDD r5=ProductAutomaton.ithVarSystemPre(5);
//		BDD r6=ProductAutomaton.ithVarSystemPre(6);
//		BDD c=ProductAutomaton.ithVarSystemPre(5);
//		BDD t=ProductAutomaton.ithVarSystemPre(3);
//		BDD b=ProductAutomaton.ithVarSystemPre(4);
//		BDD filter1=h.imp((r1.or(r2).or(r3).or(r4).or(r5).or(r6).or(b).or(c).or(t)).not());
//		BDD filter2=r1.imp((h.or(r2).or(r3).or(r4).or(r5).or(r6)).not());
//		BDD filter3=r2.imp((h.or(r1).or(r3).or(r4).or(r5).or(r6)).not());
//		BDD filter4=r3.imp((h.or(r1).or(r2).or(r4).or(r5).or(r6)).not());
//		BDD filter5=r4.imp((h.or(r1).or(r2).or(r3).or(r5).or(r6)).not());
//		BDD filter6=r5.imp((h.or(r1).or(r2).or(r3).or(r4).or(r6)).not());
//		BDD filter7=r6.imp((h.or(r1).or(r2).or(r3).or(r4).or(r5)).not());
//		BDD filter=filter1.and(filter2).and(filter3).and(filter4).and(filter5).and(filter6).and(filter7);
//		BDD filterPrime=productAutomaton.changePreVarsToPostVars(filter);
//		return filter.and(filterPrime);
		return factory.one();
	}
	
	public ProductAutomaton getProductAutomaton() {
		return productAutomaton;
	}
	
}
