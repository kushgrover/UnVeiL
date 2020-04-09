package modules;


import java.util.ArrayList;
import java.util.List;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import transitionSystem.ProductAutomaton;

/**
 * <p> Original algo for learn and ask procedures</p>
 * @author kush
 *
 */


public class DefaultExperiment implements Laboratory{
	
	ProductAutomaton productAutomaton;
	BDDFactory factory;
	BDD productAutomatonBDD, propertyBDD;
	public DefaultExperiment(BDD propertyBDD, 
			int[] numVars, 
			ArrayList<String> apListProperty,
			ArrayList<String> apListSystem)
	{
		this.productAutomaton=new ProductAutomaton(propertyBDD);
		this.factory=productAutomaton.getBDD().getFactory();
		this.productAutomatonBDD=productAutomaton.getBDD();
	}
	
	public DefaultExperiment(ProductAutomaton productAutomaton) {
		this.productAutomaton=productAutomaton;
		this.factory=productAutomaton.getBDD().getFactory();
		this.productAutomatonBDD=productAutomaton.getBDD();
	}

	@Override
	public void learn(BDD fromState, BDD toState) throws Exception {
		if(productAutomaton.getLevel(fromState, toState)==-1) {
			productAutomaton.addTransition(fromState, toState, 3);	
		}
		else if(productAutomaton.getLevel(fromState, toState)!=3) {
			productAutomaton.setLevel(fromState, toState, 3);
		}
		
//		debug
//		productAutomaton.createDot(0);
		BDD complementDomainOfChanges=allExceptDomainOfChanges(fromState,toState);
		
		BDD fromStateSimilar=fromState.exist(complementDomainOfChanges);
		BDD toStateSimilar=factory.one();
		for(int i=0;i<ProductAutomaton.numAPSystem;i++) {
			if(fromStateSimilar.and(ProductAutomaton.ithVarSystemPre(i)).isZero()) {
				toStateSimilar.andWith((ProductAutomaton.ithVarSystemPre(i)));
			}
			else if(fromStateSimilar.and(ProductAutomaton.ithVarSystemPre(i).not()).isZero()) {
				toStateSimilar.andWith(ProductAutomaton.ithVarSystemPre(i).not());
			}
		}
		BDD filters=addFilters();
		productAutomaton.addTransitions(fromStateSimilar, toStateSimilar, 2, filters);
		
//		debug
//		productAutomaton.createDot(1);
		int counter=productAutomaton.increaseCounter(fromState);
		if(counter>ProductAutomaton.threshold) {
			productAutomaton.setLevel(fromState,toState,1);
		}
	}

	private BDD allExceptDomainOfChanges(BDD fromState, BDD toState){
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

	@Override
	public BDD ask(BDD currentStates) {
		ArrayList<BDD> setOfStates=new ArrayList<BDD>();
		setOfStates.add(productAutomaton.finalStates());
		if(setOfStates.get(0).isZero()) {
			return setOfStates.get(0);
		}
		setOfStates.add(productAutomaton.preImageOfFinalStates());
		if(setOfStates.get(1).isZero()) {
			return setOfStates.get(0);
		}
		
		int i=1;
		BDD backwardReachableStates=productAutomaton.preImageOfFinalStates();
		while(!productAutomaton.preImage(backwardReachableStates).and(backwardReachableStates.not()).isZero()) {
			System.out.println("Yay!, something happens");
			setOfStates.add(productAutomaton.preImage(setOfStates.get(i)));
			i++;
			backwardReachableStates=backwardReachableStates.or(setOfStates.get(i));
		}
		BDD newCurrentStates=backwardReachableStates.and(currentStates);
		BDD nextStates;
		BDD transitions=factory.zero();
		if(i==1) {
			nextStates=productAutomaton.finalStates().and(productAutomaton.postImage(newCurrentStates));
			transitions=productAutomaton.getTransitions(newCurrentStates,nextStates);
		}
		else {
			nextStates=setOfStates.get(i-1).and(productAutomaton.postImage(newCurrentStates));
			transitions=productAutomaton.getTransitions(newCurrentStates, nextStates);
		}
		return transitions;
	}
	
	public BDD addFilters() {
		BDD h=ProductAutomaton.ithVarSystemPre(0);
		BDD r1=ProductAutomaton.ithVarSystemPre(1);
		BDD r2=ProductAutomaton.ithVarSystemPre(2);
		BDD r3=ProductAutomaton.ithVarSystemPre(3);
		BDD r4=ProductAutomaton.ithVarSystemPre(4);
		BDD filter1=h.imp((r1.or(r2).or(r3).or(r4)).not());
		BDD filter2=r1.imp((h.or(r2).or(r3).or(r4)).not());
		BDD filter3=r2.imp((h.or(r1).or(r3).or(r4)).not());
		BDD filter4=r3.imp((h.or(r1).or(r2).or(r4)).not());
		BDD filter5=r4.imp((h.or(r1).or(r2).or(r3)).not());
		BDD filter=filter1.and(filter2).and(filter3).and(filter4).and(filter5);
		BDD filterPrime=productAutomaton.changePreVarsToPostVars(filter);
//		filter.printDot();
		return filter.and(filterPrime);
	}
	
}
