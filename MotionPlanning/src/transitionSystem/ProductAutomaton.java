package transitionSystem;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDD.BDDIterator;
import net.sf.javabdd.BDDDomain;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import printing.PrintProductAutomaton;
import transitionSystem.extraExceptions.stateException;
import transitionSystem.extraExceptions.transitionException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * <p> Stores the product automaton and does all the operations on it</p>
 * @author Kush Grover
 *
 */

public class ProductAutomaton{
	/**
	 * <p>propertyBDD stores the property automaton
	 * numVars[0] = number of vars used in property automaton
	 * numVars[1] = number of vars used in transition level
	 * numVars[2] = number of vars used in label of transitions in property automaton=number of atomic props used in property automaton
	 * numVars[3] = number of vars used in system automaton = 2 * numAPSystem
	 * threshold and counter are to add level 1 transitions
	 * apListProperty and apListSystem are lists of atomic props used in property and automaton resp.</p>
	 */
	BDD productAutomatonBDD, propertyBDD;
	public static BDDFactory factory;
	public static int numVars[];
	public static int numAPSystem, threshold, levelOfTransitions;
	public static ArrayList<String> apListProperty, apListSystem;
	static BDDPairing oldToNewPairing, newToOldPairing, statesToLabelPairing;
	int[] counter;
	
	
	/**
	 * <p>Initializes Product Automaton to an empty automaton</p>
	 * @param propertyBDD BDD for the property automaton
	 */
	public ProductAutomaton(BDD propertyBDD){	
		this.propertyBDD=propertyBDD;
		this.productAutomatonBDD=factory.zero();
		initializeCounter();
		oldToNewPairing=oldVarToNewVarPairing();
		newToOldPairing=newVarToOldVarPairing();
		statesToLabelPairing=statesToLabelPairing();
	}

	/**
	 * <p>Creating the counter array and initializing all values to 0</p>
	 */
	private void initializeCounter(){
		counter=new int[(int) Math.pow(2, numAPSystem+propertyDomainPre().varNum())];
		for(int i=0;i<counter.length;i++) {
			counter[i]=0;
		}
	}

	/**
	 * <p>Restrict the bdd to only contain variables used in property and returns the corresponding bdd in label variables</p>
	 * @param states BDD representing set of states
	 * @return label BDD corresponding to states
	 */
	public BDD findCorrespondingLabelBDD(BDD states){
		BDD temp=restrictToLabelVarsInPreSystem(states);
		temp=removeAllExceptLabelVars(temp.replace(statesToLabelPairing));
		if(temp.isOne()) {
			return factory.zero();
		}
		return temp;
	}
	
	/**
	 * <p>Restrict to only variables used in property, used only by findCorrespondingLabelBDD</p>
	 * @param states BDD representing set of states
	 * @return 
	 */
	private BDD restrictToLabelVarsInPreSystem(BDD states){
		BDD temp=states.exist(propertyDomainPre().set());
		temp=temp.exist(acceptingSetDomain().set());
		temp=temp.exist(transitionLevelDomain().set());
		for(int i=0;i<numAPSystem;i++) {
			temp.exist(ithVarSystemPost(i));
			if(findIthSystemVarInLabel(i)==-1) {
				temp.exist(ithVarSystemPre(i));
			}
		}
		for(int i=0;i<numVars[2];i++) {
			temp.exist(ithVarLabel(i));
		}
		return temp;
	}
	
	/**
	 * <p>Check if ith system var is used in property, returns the index of that var(AP) in apListProperty</p>
	 * @param i
	 * @return index of var in apListProperty
	 */
	private int findIthSystemVarInLabel(int i) {
		String toSearch = apListSystem.get(i);
		for(int j=0;j<numVars[2];j++) {
			if(apListProperty.get(j).equals(toSearch)) {
				return j;
			}
		}
		return -1;
	}

	/**
	 * <p>Compute final States</p>
	 * @return BDD representing the set of final states
	 */
	public BDD finalStates(){
		BDD temp=removeAllExceptPostVars(productAutomatonBDD.and(acceptingSetDomain().ithVar(1)));
		if(temp.exist(propertyDomainPost().set()).isZero()) {
			return factory.zero();
		}
		return changePostVarsToPreVars(temp);
	}
	
	/**
	 * <p>Compute the set of states from which there is an outgoing accepting transition</p>
	 * @return
	 */
	public BDD preImageOfFinalStates() {
		return removeAllExceptPreVars(productAutomatonBDD.and(acceptingSetDomain().ithVar(1)));
	}

	/**
	 * <p>Returns the set of transitions in the product automaton from a state in 'fromState' to a state in 'toState'</p>
	 * @param fromState 
	 * @param toState
	 * @return BDD representing the transition from fromState to toState
	 */
	public BDD getTransitions(BDD fromState, BDD toState) {
		toState=changePreVarsToPostVars(toState);
		fromState.andWith(toState);
		return productAutomatonBDD.and(fromState);
	}

	/**
	 * <p>Computes pre image of a set of states</p>
	 * @param states
	 * @return pre image of states in product automaton
	 */
	public BDD preImage(BDD states) {
		BDD temp=factory.zero();
		temp.orWith(changePreVarsToPostVars(states));
		return removeAllExceptPreVars(productAutomatonBDD.and(temp));
	}
	
	/**
	 * 
	 * @param bdd
	 * @return
	 */
	public BDD changePreVarsToPostVars(BDD bdd) {
		BDD temp=bdd.replace(oldToNewPairing);
		return temp;
	}
	
	/**
	 * <p>Computes post image of a set of states</p>
	 * @param states
	 * @return post image of states in product automaton
	 */
	public BDD postImage(BDD states) {
		return changePostVarsToPreVars(removeAllExceptPostVars(productAutomatonBDD.and(states)));
	}
	
	/**
	 * 
	 * @param bdd
	 * @return
	 */
	public BDD changePostVarsToPreVars(BDD bdd) {
		return bdd.replace(newToOldPairing);
	}
	
	/**
	 * 
	 * @return BDD representing the product automaton
	 */
	public BDD getBDD() {
		return productAutomatonBDD;
	}
	
	/**
	 * 
	 * @param i
	 * @return ith system var in pre vars
	 */
	public static BDD ithVarSystemPre(int i) {
		return factory.ithVar(i+numVars[0]+numVars[1]+numVars[2]);
	}
	
	/**
	 * 
	 * @param i
	 * @return ith system var in post vars
	 */
	public static BDD ithVarSystemPost(int i) {
		return factory.ithVar(i+numVars[0]+numVars[1]+numVars[2]+numAPSystem);
	}
	
	/**
	 * 
	 * @param i
	 * @return ith var used for labels
	 */
	public static BDD ithVarLabel(int i) {
		return factory.ithVar(i+numVars[0]+numVars[1]);
	}
	
	/**
	 * 
	 * @return BDDDoamin for pre vars of property
	 */
	public static BDDDomain propertyDomainPre(){
		return factory.getDomain(0);
	}

	/**
	 * 
	 * @return BDDDoamin for post vars of property
	 */
	public static BDDDomain propertyDomainPost(){
		return factory.getDomain(1);
	}
	
	/**
	 * 
	 * @return BDDDomain for accepting sets
	 */
	public static BDDDomain acceptingSetDomain(){
		return factory.getDomain(2);
	}
	
	/**
	 * 
	 * @return BDDDomain for transition level
	 */
	public static BDDDomain transitionLevelDomain(){
		return factory.getDomain(3);
	}
	
	/**
	 * <p> Return a bdd after existentially quantifying all the variables except pre vars</p>
	 * @param bdd
	 * @return BDD with only pre vars
	 */
	public BDD removeAllExceptPreVars(BDD bdd) {
		BDD temp=bdd.exist(propertyDomainPost().set());
		temp=temp.exist(acceptingSetDomain().set());
		temp=temp.exist(transitionLevelDomain().set());
		for(int i=0;i<numAPSystem;i++) {
			temp=temp.exist(ithVarSystemPost(i));
		}
		for(int i=0;i<numVars[2];i++) {
			temp=temp.exist(ithVarLabel(i));
		}
		return temp;
	}
	
	/**
	 * <p> Return a bdd after existentially quantifying all the variables except post vars</p>
	 * @param bdd
	 * @return BDD with only post vars
	 */
	public BDD removeAllExceptPostVars(BDD bdd) {
		BDD temp=bdd.exist(propertyDomainPre().set());
		temp=temp.exist(acceptingSetDomain().set());
		temp=temp.exist(transitionLevelDomain().set());
		for(int i=0;i<numVars[2];i++) {
			temp=temp.exist(ithVarLabel(i));
		}
		for(int i=0;i<numAPSystem;i++) {
			temp=temp.exist(ithVarSystemPre(i));
		}
		return temp;
	}
	
	/**
	 * <p> Return a bdd after existentially quantifying all the variables except label vars</p>
	 * @param bdd
	 * @return BDD with only label vars
	 */
	public BDD removeAllExceptLabelVars(BDD bdd) {
		BDD temp=bdd.exist(propertyDomainPre().set());
		temp=temp.exist(propertyDomainPost().set());
		temp=temp.exist(acceptingSetDomain().set());
		temp=temp.exist(transitionLevelDomain().set());
		for(int i=0;i<numAPSystem;i++) {
			temp=temp.exist(ithVarSystemPost(i));
			temp=temp.exist(ithVarSystemPre(i));
		}
		return temp;
	}
	
	/**
	 * <p>Creates BDDPairing for changing pre vars to post vars</p>
	 * @return BDDPairing for changing pre vars to post vars
	 */
	public BDDPairing oldVarToNewVarPairing() {
		BDDPairing newPairing = factory.makePair();
		newPairing.set(factory.getDomain(0), factory.getDomain(1));
		for(int i=0;i<numAPSystem;i++) {
			newPairing.set(i+numVars[0]+numVars[1]+numVars[2],i+numVars[0]+numVars[1]+numVars[2]+numAPSystem);
		}
		return newPairing;
	}
	
	/**
	 * <p>Creates BDDPairing for changing post vars to pre vars</p>
	 * @return BDDPairing for changing post vars to pre vars
	 */
	public BDDPairing newVarToOldVarPairing() {
		BDDPairing newPairing = factory.makePair();
		newPairing.set(factory.getDomain(1), factory.getDomain(0));
		for(int i=0;i<numAPSystem;i++) {
			newPairing.set(i+numVars[0]+numVars[1]+numVars[2]+numAPSystem,i+numVars[0]+numVars[1]+numVars[2]);
		}
		return newPairing;
	}
	
	/**
	 * <p>Creates BDDPairing for changing pre system vars to label vars</p>
	 * @return BDDPairing for changing pre system vars to label vars
	 */
	public BDDPairing statesToLabelPairing() {
		BDDPairing newPairing = factory.makePair();
		for(int i=0;i<numAPSystem;i++) {
			if(findIthSystemVarInLabel(i)!=-1) {
				newPairing.set(i+numVars[0]+numVars[1]+numVars[2], numVars[0]+numVars[1]+findIthSystemVarInLabel(i));
			}
		}
		return newPairing;
	}
	
	/**
	 * <p>Add a transition in the product automaton</p>
	 * @param fromState
	 * @param toState
	 * @param level
	 */
	public void addTransition(BDD fromState, BDD toState, int level) {
		BDD label=findCorrespondingLabelBDD(toState);
		BDD toStatePrime=changePreVarsToPostVars(toState);
		if(! label.imp(removeAllExceptLabelVars(propertyBDD.and(fromState).and(toStatePrime))).isZero()) {
			productAutomatonBDD.orWith(propertyBDD.and(fromState).and(toStatePrime).and(label).and(transitionLevelDomain().ithVar(level)));
		}
	}
	
	/**
	 * <p>Find the level of transition, if the transition doesn't exist yet, return -1</p>
	 * @param transition
	 * @return level of the transition
	 * @throws transitionException
	 */
	public int getLevel(BDD transition) throws transitionException {
		if(transition.pathCount()>1) {
			int highestLevel=-1;
			int currentLevel=-1;
			BDDIterator iterator= transition.iterator(allVars());
			while(iterator.hasNext()) {
				BDD nextTransition=(BDD) iterator.next();
				currentLevel=nextTransition.scanVar(acceptingSetDomain()).intValue();
				if(currentLevel>highestLevel) {
					highestLevel=currentLevel;
				}
			}
			return highestLevel;
//			throw new transitionException("More than one transition");
		}
		else if(transition.pathCount()<1) {
			return -1;
		}
		return transition.scanVar(transitionLevelDomain()).intValue();
	}
	
	private static BDD allVars()
	{
		BDD allVars=factory.one();
		for(int i=0;i<numVars[0]+numVars[1]+numVars[2]+numVars[3];i++) {
			allVars.andWith(factory.ithVar(i));
		}
		return allVars;
	}

	/**
	 * <p>Find the level of transition from fromState to toState, if the transition doesn't exist yet, return -1</p>
	 * @param fromState
	 * @param toState
	 * @return level of the transition
	 * @throws transitionException
	 */
	public int getLevel(BDD fromState, BDD toState) throws transitionException {
		BDD toStatePrime=changePreVarsToPostVars(toState);
		BDD transition=productAutomatonBDD.and(fromState).and(toStatePrime);
		return getLevel(transition);
	}
	
	/**
	 * <p>Change the level of a transition</p>
	 * @param transition
	 * @param level
	 * @throws transitionException 
	 */
	public void setLevel(BDD transition, int level) throws transitionException {
		if(transition.and(productAutomatonBDD).isZero()) {
			throw new transitionException("Transition doesn't exist");
		}
		removeTransition(transition);
		transition=transition.exist(transitionLevelDomain().set()).and(transitionLevelDomain().ithVar(level));
		productAutomatonBDD.orWith(transition);
	}
	
	/**
	 * <p>Change the level of a transition from fromState to toState</p>
	 * @param fromState
	 * @param toState
	 * @param level
	 * @throws transitionException 
	 */
	public void setLevel(BDD fromState, BDD toState, int level) throws transitionException {
		BDD toStatePrime=changePreVarsToPostVars(toState);
		BDD transition=productAutomatonBDD.and(fromState).and(toStatePrime);
		setLevel(transition, level);
	}
	
	/**
	 * <p>Remove a set of transitions from the productAutomaton</p>
	 * @param transition
	 */
	public void removeTransition(BDD transitions) {
		productAutomatonBDD.andWith(transitions.not());
	}

	/**
	 * <p>Increase the counter of a state</p>
	 * @param fromState
	 * @return counter value for that state
	 * @throws stateException
	 */
	public int increaseCounter(BDD fromState) throws stateException{
		int stateID=getStateID(fromState);
		counter[stateID]++;
		return counter[stateID];
	}

	/**
	 * <p>Find the ID of a state</p>
	 * @param fromState
	 * @return ID of the state
	 * @throws stateException
	 */
	public int getStateID(BDD fromState) throws stateException{
		if(fromState.pathCount()>1) {
			throw new stateException("More than one state");
		}
		else if(fromState.pathCount()==0) {
			throw new stateException("State desn't exist");
		}
		int stateID=fromState.scanVar(propertyDomainPre()).intValue();
		for(int i=0;i<numAPSystem;i++) {
			if(! fromState.and(ithVarSystemPre(i)).equals(factory.zero())) {
				stateID+=(int)Math.pow(2, i+propertyDomainPre().varNum());
			}
		}
		return stateID;
	}

	/**
	 * <p>Add a set of transitions to the product automaton</p>
	 * @param fromStates from set of states
	 * @param toStates to set of states
	 * @param level level of all transitions
	 * @param filters 
	 * @throws Exception 
	 */
	public void addTransitions(BDD fromStates, BDD toStates, int level, BDD filters) throws Exception{
		BDD toStatesPrime=changePreVarsToPostVars(toStates);
		BDD transitions=fromStates.and(toStatesPrime).and(transitionLevelDomain().ithVar(level));
		BDD allOtherVariablesWhichRemainEqual=factory.one();
		BDD support = fromStates.support();
		for(int i=0;i<numAPSystem;i++) {
			if(! support.and(ithVarSystemPre(i)).equals(support)) {
				allOtherVariablesWhichRemainEqual.andWith(ithVarSystemPre(i).biimp(ithVarSystemPost(i)));
			}
		}
		transitions.andWith(allOtherVariablesWhichRemainEqual);
		BDD label=factory.one();
		for(int i=0;i<numVars[2];i++) {
			label.andWith(ithVarLabel(i).biimp(ithVarSystemPost(findIthVarLabelInSystem(i))));
		}
		transitions.andWith(label);
		productAutomatonBDD.orWith(transitions.and(propertyBDD).and(filters));		
	}
	
	private int findIthVarLabelInSystem(int i) throws Exception
	{
		String toSearch=apListProperty.get(i);
		for(int j=0;j<numAPSystem;j++) {
			if(toSearch.equals(apListSystem.get(j))) {
				return j;
			}
		}
		throw new Exception("You are fucked!");
	}

	/**
	 * 
	 * @return set of all pre system vars
	 */
	public BDD allPreSystemVars() {
		BDD allPreVars=factory.one();
		for(int i=0;i<numAPSystem;i++) {
			allPreVars.andWith(ithVarSystemPre(i));
		}
		return allPreVars;
	}
	
	/**
	 * 
	 * @return set of all post system vars
	 */
	public BDD allPostSystemVars() {
		BDD allPostVars=factory.one();
		for(int i=0;i<numAPSystem;i++) {
			allPostVars.andWith(ithVarSystemPost(i));
		}
		return allPostVars;
	}
	
	/**
	 * 
	 * @return set of all pre vars
	 */
	public BDD allPreVars() {
		return allPreSystemVars().andWith(propertyDomainPre().set());
	}
	
	/**
	 * 
	 * @return set of all post vars
	 */
	public BDD allPostVars() {
		return allPostSystemVars().andWith(propertyDomainPost().set());
	}

	/**
	 * 
	 * @return number of states in the product automaton
	 */
	public int numberOfStates(){
		return (int) Math.pow(2, numAPSystem+propertyDomainPre().varNum());
	}
	
	/**
	 * <p>Create a dot file representing the product automaton</p>
	 * @throws IOException
	 * @throws stateException
	 */
	public void createDot(int i) throws IOException, stateException{
		new PrintProductAutomaton(this, i);
	}
	
}


