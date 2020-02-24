/**
 * 
 */
package transitionSystem;




import java.util.ArrayList;
//import java.util.List;

import net.sf.javabdd.BDD;
//import net.sf.javabdd.BDDDomain;
import net.sf.javabdd.BDDFactory;
//import net.sf.javabdd.BuDDyFactory;
/**
 * @author kush
 *
 */
public class TS{
	
	private ArrayList<String> apList;
	private int size;
	private BDD bdd;
	private BDDFactory factory;
//	private BDDDomain[] bddDomainForLearnedAutomata;
	private int initial;
	private ArrayList<ArrayList<String>> labelling;
	
	public TS(BDDFactory factory, 
				int cacheSize, 
				ArrayList<String> apList, 
				ArrayList<ArrayList<String>> labelling, 
				int typeOfTransitions) {
		
		this.apList=apList;
		this.size=apList.size();
		this.labelling=labelling;
		this.bdd=factory.zero();
		this.factory=factory;
		
//		bddDomainForLearnedAutomata=factory.extDomain(new int[] {size,size,typeOfTransitions});
		factory.extVarNum(2*size+typeOfTransitions);
	}
	
	public BDD getBDD() {
		return bdd;
	}
	
	public ArrayList<String> getAPList(){
		return apList;
	}

	public int getInitial() {
		return initial;
	}
	
	public ArrayList<ArrayList<String>> getLabelling(){
		return labelling;
	}
	
	public void setLabelling(ArrayList<ArrayList<String>> labelling){
		this.labelling=labelling;
	}
	
	public void addTransitionFromListOfAP(int[] currentState, int[] nextState, int varsBuchi){
		
		BDD currentStateBDD=factory.one();
		BDD nextStateBDD=factory.one();
		System.out.println("vasrBuhci= "+varsBuchi);
		
		for(int i=0;i<currentState.length;i++) {
			currentStateBDD=currentStateBDD.and(factory.ithVar(currentState[i]+varsBuchi));
//			System.out.println("currentState "+i+" "+currentState[i]);
//			currentStateBDD.andWith(bddDomainForLearnedAutomata[0].ithVar(currentState[i]));
		}
		for(int i=0;i<nextState.length;i++) {
			nextStateBDD=nextStateBDD.and(factory.ithVar(size+nextState[i]+varsBuchi));
//			System.out.println("nextState "+i+" "+nextState[i]);
//			nextStateBDD.andWith(bddDomainForLearnedAutomata[1].ithVar(nextState[i]));
		}
		
		bdd=bdd.or(currentStateBDD.and(nextStateBDD));
		
	}
	
	
}
