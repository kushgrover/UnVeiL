package abstraction;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import net.sf.javabdd.BDDException;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import planningIO.printing.PrintAcceptingPath;
import planningIO.printing.PrintProductAutomaton;
import settings.PlanningException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import modules.emptinessCheck.EmptinessCheck;

/**
 * <p> This file stores and manipulate the product automaton.</p>
 * @author Kush Grover
 *
 */

public class ProductAutomaton
{
	/**
	 * <p>propertyBDD stores the property automaton
	 * numVars[0] = number of vars used in property automaton
	 * numVars[1] = number of vars used in transition level
	 * numVars[2] = number of vars used in label of transitions in property automaton = number of atomic props used in property automaton
	 * numVars[3] = number of vars used in system automaton = 2 * numAPSystem
	 * threshold and counter are to add level 1 transitions
	 * apListProperty and apListSystem are lists of atomic props used in property and automaton resp.</p>
	 */
	BDD productAutomatonBDD;
	BDD initStates = null;
	static BDD propertyBDD;
	public BDD sampledTransitions;
	public BDD sampledProductTransitions;
	public static BDDFactory factory;
	public static int[] numVars;
	public static int numAPSystem;
	public static int threshold;
	public static int maxLevelOfTransitions;
	public static int varsBeforeSystemVars;
	public static int numAllVars;
	public static ArrayList<String> apListProperty;
	public static ArrayList<String> apListSystem;
	static BDDPairing oldToNewPairing;
	static BDDPairing newToOldPairing;
	static BDDPairing statesToLabelPairing;
	static BDDPairing newVarToOldVarSystemPairing;
	static BDDPairing oldVarToNewVarSystemPairing;
	int[] counter;
	int numStatesSystem;
	static int numStates;
	
	
	/**
	 * <p>Initializes Product Automaton to an empty automaton</p>
	 * @param propertyBDD BDD for the property automaton
	 * @throws PlanningException If something goes wrong in initializing
	 */
	public ProductAutomaton(BDD propertyBDD) throws PlanningException
	{
		ProductAutomaton.propertyBDD	= propertyBDD;
		this.productAutomatonBDD		= factory.zero();
		this.sampledTransitions			= factory.zero();
		this.sampledProductTransitions	= factory.zero();
		
		varsBeforeSystemVars			= numVars[0] + numVars[1] + numVars[2];
		numAllVars						= varsBeforeSystemVars + 2*numAPSystem;
		numStates 						= numberOfStates();
		numStatesSystem					= (int) StrictMath.pow(2, numAPSystem);
		
		initializeCounter();
		
		oldToNewPairing					= oldVarToNewVarPairing();
		newToOldPairing					= newVarToOldVarPairing();
		statesToLabelPairing 			= statesToLabelPairing();
		newVarToOldVarSystemPairing		= newVarToOldVarSystemPairing();
		oldVarToNewVarSystemPairing		= oldVarToNewVarSystemPairing();
	
	}

	



//-----------------------------------------------------------------------------------------------------------------

//-----------------------------------------------------------------------------------------------------------------

	/**
	 * <p>Creating the counter array and initializing all values to 0</p>
	 */
	private void initializeCounter()
	{
		counter	= new int[(int) numStatesSystem];
		Arrays.fill(counter, 0);
	}
	
	/**
	 * <p>Creates BDDPairing for changing pre vars to post vars</p>
	 * @return BDDPairing for changing pre vars to post vars
	 * @see BDDPairing
	 */
	public static BDDPairing oldVarToNewVarPairing() 
	{
		BDDPairing newPairing 	= factory.makePair();
		newPairing.set(factory.getDomain(0), factory.getDomain(1));
		for(int i=0; i<numAPSystem; i++) {
			newPairing.set(varsBeforeSystemVars + i, 		varsBeforeSystemVars + numAPSystem + i);
		}
		return newPairing;
	}
	
	/**
	 * <p>Creates BDDPairing for changing post vars to pre vars</p>
	 * @return BDDPairing for changing post vars to pre vars
	 * @see BDDPairing
	 */
	public static BDDPairing newVarToOldVarPairing() 
	{
		BDDPairing newPairing 	= factory.makePair();
		newPairing.set(factory.getDomain(1), factory.getDomain(0));
		for(int i=0; i<numAPSystem; i++) 
		{
			newPairing.set(varsBeforeSystemVars + numAPSystem + i,		varsBeforeSystemVars + i);
		}
		return newPairing;
	}
	
	/**
	 * <p>Creates BDDPairing for changing pre system vars to label vars</p>
	 * @return BDDPairing for changing pre system vars to label vars
	 * @see BDDPairing
	 */
	public static BDDPairing statesToLabelPairing() throws PlanningException 
	{
		BDDPairing newPairing 	= factory.makePair();
		for(int i=0; i<numAPSystem; i++) 
		{
			if(findIthSystemVarInLabel(i) != -1) 
			{
				newPairing.set(varsBeforeSystemVars + i, 	numVars[0] + numVars[1] + findIthSystemVarInLabel(i));
			}
		}
		return newPairing;
	}
	
	
	/**
	 * <p>Creates BDDPairing for changing pre system vars to post system vars</p>
	 * @return BDDPairing for changing pre system vars to post system vars
	 * @see BDDPairing
	 */
	public static BDDPairing oldVarToNewVarSystemPairing() 
	{
		BDDPairing newPairing 	= factory.makePair();
		for(int i=0; i<numAPSystem; i++) 
		{
			newPairing.set(varsBeforeSystemVars + i,	varsBeforeSystemVars + numAPSystem + i);
		}
		return newPairing;
	}
	
	/**
	 * <p>Creates BDDPairing for changing post system vars to pre system vars</p>
	 * @return BDDPairing for changing post system vars to pre system vars
	 * @see BDDPairing
	 */
	public static BDDPairing newVarToOldVarSystemPairing() 
	{
		BDDPairing newPairing 	= factory.makePair();
		for(int i=0; i<numAPSystem; i++) {
			newPairing.set(varsBeforeSystemVars + numAPSystem + i, 		varsBeforeSystemVars + i);
		}
		return newPairing;
	}
	

//------------------------------------------------------------------------------------------------------------------	
	
	
//------------------------------------------------------------------------------------------------------------------	

// *** For getters and setters
	
	/**
	 * set the initial state
	 * @param initStates initial states represented as a BDD
	 */
	public void setInitState(BDD initStates) 
	{
		this.initStates = initStates;
	}
	
	/**
	 * 
	 * @return BDD representing the product automaton
	 */
	public BDD getBDD() 
	{
		return productAutomatonBDD;
	}
	
	/**
	 * 
	 * @return BDD representing the property automaton
	 */
	public static BDD getPropertyBDD() 
	{
		return propertyBDD;
	}
	
	/**
	 * 
	 * @return set of initial states
	 */
	public BDD getInitStates() 
	{
		return initStates;
	}
	
	/**
	 * 
	 * @return total number of states in the product automaton
	 */
	public static int getNumStates() 
	{
		return numStates;
	}
	
	/**
	 * 
	 * @return number of states in the product automaton
	 */
	private static int numberOfStates()
	{
		return (int) StrictMath.pow( 2 , numAPSystem + propertyDomainPre().varNum());
	}
	
	/**
	 * 
	 * @return all the sampled transitions in the product automaton
	 */
	public BDD getSampledProductTransitions() 
	{
		return productAutomatonBDD.and(transitionLevelDomain().ithVar(3));
	}
	
	/**
	 * 
	 * @return BDDDomain for pre vars of property
	 * @see BDDDomain
	 */
	public static BDDDomain propertyDomainPre()
	{
		return factory.getDomain(0);
	}

	/**
	 * 
	 * @return BDDDomain for post vars of property
	 * @see BDDDomain
	 */
	public static BDDDomain propertyDomainPost()
	{
		return factory.getDomain(1);
	}
	
	/**
	 * 
	 * @return BDDDomain for accepting sets
	 * @see BDDDomain
	 */
	public static BDDDomain acceptingSetDomain()
	{
		return factory.getDomain(2);
	}
	
	/**
	 * 
	 * @return BDDDomain for transition level
	 * @see BDDDomain
	 */
	public static BDDDomain transitionLevelDomain()
	{
		return factory.getDomain(3);
	}
	
	/**
	 * 
	 * @return ith system var in pre vars
	 */
	public static BDD ithVarSystemPre(int i) throws PlanningException 
	{
		if(i > numAPSystem) {
			throw new PlanningException("Index out of bounds");
		}
		return factory.ithVar(varsBeforeSystemVars + i);
	}
	
	/**
	 * 
	 * @return ith system var in post vars
	 */
	public static BDD ithVarSystemPost(int i) throws PlanningException 
	{
		if(i > numAPSystem) {
			throw new PlanningException("Index out of bounds");
		}
		return factory.ithVar(varsBeforeSystemVars + numAPSystem + i);
	}
	
	/**
	 * 
	 * @return ith var used for labels
	 */
	public static BDD ithVarLabel(int i) throws PlanningException 
	{
		if(i >= numVars[2]) 
		{
			throw new PlanningException("Index out of bounds");
		}
		return factory.ithVar(i + numVars[0] + numVars[1]);
	}
	
	
	
	/**
	 * 
	 * @return set of all pre system vars
	 */
	public static BDD allPreSystemVars() throws PlanningException 
	{
		BDD allPreVars = factory.one();
		for(int i=0; i<numAPSystem; i++) 
		{
			allPreVars.andWith(ithVarSystemPre(i));
		}
		return allPreVars;
	}
	
	/**
	 * 
	 * @return set of all post system vars
	 */
	public static BDD allPostSystemVars() throws PlanningException 
	{
		BDD allPostVars	= factory.one();
		for(int i=0; i<numAPSystem; i++) 
		{
			allPostVars.andWith(ithVarSystemPost(i));
		}
		return allPostVars;
	}
	
	public static BDD allSystemVars() throws PlanningException
	{
		return allPostSystemVars().and(allPreSystemVars());
		
	}
	
	/**
	 * 
	 * @return set of all pre vars
	 */
	public static BDD allPreVars() throws PlanningException 
	{
		return allPreSystemVars().andWith(propertyDomainPre().set());
	}
	
	/**
	 * 
	 * @return set of all post vars
	 */
	public static BDD allPostVars() throws PlanningException 
	{
		return allPostSystemVars().andWith(propertyDomainPost().set());
	}

	
	/**
	 * 
	 * @return conjunction of all the vars
	 */

	public static BDD allVars()
	{
		BDD allVars	= factory.one();
		for(int i=0; i<numVars[0]+numVars[1]+numVars[2]+numVars[3]; i++) 
		{
			allVars.andWith(factory.ithVar(i));
		}
		return allVars;
	}
	
	/**
	 * 
	 * @return true if bdd has only pre system vars
	 */
	public static boolean hasOnlyPreSystemVars(BDD bdd) throws PlanningException
	{
		return ! (bdd.support().imp(allPreSystemVars()).isZero());
	}
	
	/**
	 * 
	 * @return True if bdd has only post system vars
	 */
	public static boolean hasOnlyPostSystemVars(BDD bdd) throws PlanningException
	{
		return ! (bdd.support().imp(allPostSystemVars()).isZero());
	}
	
	/**
	 * 
	 * @return true if bdd has only post system vars
	 */
	public static boolean hasOnlyPreVars(BDD bdd) throws PlanningException
	{
		return ! (bdd.support().imp(allPreVars()).isZero());
	}
	
	/**
	 * 
	 * @return true if bdd has only post system vars
	 */
	public static boolean hasOnlyPostVars(BDD bdd) throws PlanningException
	{
		return ! (bdd.support().imp(allPostVars()).isZero());
	}

	
	/**
	 * <p>Compute final States</p>
	 * @return BDD representing the set of final states
	 */
	public BDD finalStatesSystem() throws PlanningException
	{
		BDD temp	= removeAllExceptPostSystemVars(productAutomatonBDD.and(acceptingSetDomain().ithVar(1)));
		return changePostVarsToPreVars(temp);
	}
	
	/**
	 * <p>Compute the set of states from which there is an outgoing accepting transition</p>
	 */
	public BDD preImageOfFinalStatesSystem() throws PlanningException 
	{
		return removeAllExceptPreSystemVars(productAutomatonBDD.and(acceptingSetDomain().ithVar(1)));
	}
	
	
//----------------------------------------------------------------------------------------------------------------
	
//----------------------------------------------------------------------------------------------------------------
// *** states
	
	
	/**
	 * <p>Transform a BDD of pre vars changed to a BDD of post vars. This is used for converting a pre state to a post state.</p>
	 * @param bdd Input BDD consisting only of pre vars
	 * @return BDD of post vars
	 */
	public static BDD changePreVarsToPostVars(BDD bdd) throws PlanningException
	{
		if(! hasOnlyPreVars(bdd)) 
		{
			throw new PlanningException("BDD has extra vars");
		}
		return bdd.replace(oldToNewPairing);
	}
	
	/**
	 * <p>Transform a BDD of post vars changed to a BDD of pre vars. This is used for converting a post state to a pre state.</p>
	 * @param bdd Input BDD consisting only of post vars
	 * @return BDD of pre vars
	 */
	public static BDD changePostVarsToPreVars(BDD bdd) throws PlanningException
	{
		if(! hasOnlyPostVars(bdd)) 
		{
			throw new PlanningException("BDD has extra vars");
		}
		return bdd.replace(newToOldPairing);
	}
	
	/**
	 * <p>Does what changePostVarsToPreVars does except that they are restricted only to System vars</p>
	 * @param bdd Input BDD consisting only of post system vars
	 * @return BDD of pre system vars
	 */
	public static BDD changePostSystemVarsToPreSystemVars(BDD bdd) throws PlanningException
	{
		if(! hasOnlyPostSystemVars(bdd)) 
		{
			throw new PlanningException("BDD has extra vars");
		}
		return bdd.replace(newVarToOldVarSystemPairing);
	}

	/**
	 * <p>Does what changePreVarsToPostVars does except that they are restricted only to System vars</p>
	 * @param bdd Input BDD consisting only of pre system vars
	 * @return BDD of post system vars
	 */
	public static BDD changePreSystemVarsToPostSystemVars(BDD bdd) throws PlanningException
	{
		if(! hasOnlyPreSystemVars(bdd)) 
		{
			throw new PlanningException("BDD has extra vars");
		}
		return bdd.replace(oldVarToNewVarSystemPairing);
	}
	
	
	
	
	/**
	 * <p> Return a bdd after existentially quantifying all the variables except pre vars</p>
	 * @return BDD with only pre vars
	 */
	public static BDD removeAllExceptPreVars(BDD bdd) throws PlanningException
	{
		BDD temp = bdd;
		try{
			temp = temp.exist(propertyDomainPost().set());
		} catch(BDDException E)
		{
			E.printStackTrace();
		}
		try{
			temp	= temp.exist(acceptingSetDomain().set());
		} catch(BDDException E)
		{
			E.printStackTrace();
		}
		try{
			temp	= temp.exist(transitionLevelDomain().set());
		} catch(BDDException E)
		{
			E.printStackTrace();
		}
		for(int i=0; i<numAPSystem; i++) 
		{
			try{
				temp = temp.exist(ithVarSystemPost(i));
			} catch(BDDException E)
			{
				E.printStackTrace();
			}
		}
		for(int i=0; i<numVars[2]; i++) 
		{
			try{
				temp	= temp.exist(ithVarLabel(i));
			} catch(BDDException E)
			{
				E.printStackTrace();
			}
		}
		return temp;
	}
	
	/**
	 * <p> Return a bdd after existentially quantifying all the variables except post vars</p>
	 * @return BDD with only post vars
	 */
	public static BDD removeAllExceptPostVars(BDD bdd) throws PlanningException
	{
		BDD temp	= bdd;
		try{
			temp 	= temp.exist(propertyDomainPre().set());
		} catch(BDDException E)
		{
			E.printStackTrace();
		}
		try {
			temp	= temp.exist(acceptingSetDomain().set());
		} catch(BDDException E)
		{
			E.printStackTrace();
		}
		try {
			temp	= temp.exist(transitionLevelDomain().set());
		} catch(BDDException E)
		{
			E.printStackTrace();
		}
		for(int i=0; i<numVars[2]; i++) 
		{
			try {
				temp	= temp.exist(ithVarLabel(i));
			} catch(BDDException E) 
			{
				E.printStackTrace();
			}
		}
		for(int i=0; i<numAPSystem; i++) 
		{
			try {
				temp	= temp.exist(ithVarSystemPre(i));
			} catch(BDDException E)
			{
				E.printStackTrace();
			}
		}
		return temp;
	}
	
	/**
	 * <p> Return a bdd after existentially quantifying all the variables except pre system vars</p>
	 * @return BDD with only pre system vars
	 */
	public static BDD removeAllExceptPreSystemVars(BDD bdd) throws PlanningException
	{
		BDD temp	= bdd;
		try
		{
			temp	= temp.exist(propertyDomainPre().set());
		} catch(BDDException E)
		{
			E.printStackTrace();
		}
		try
		{
			temp	= temp.exist(propertyDomainPost().set());
		} catch(BDDException E)
		{
			E.printStackTrace();
		}
		try
		{
			temp	= temp.exist(acceptingSetDomain().set());
		} catch(BDDException E)
		{
			E.printStackTrace();
		}
		try
		{
			temp	= temp.exist(transitionLevelDomain().set());
		} catch(BDDException E)
		{
			E.printStackTrace();
		}
		for(int i=0; i<numAPSystem; i++) 
		{
			try
			{
				temp	= temp.exist(ithVarSystemPost(i));
			} catch(BDDException E)
			{
				E.printStackTrace();
			}
		}
		for(int i=0; i<numVars[2]; i++) 
		{
			try
			{
				temp	= temp.exist(ithVarLabel(i));
			} catch(BDDException E)
			{
				E.printStackTrace();
			}
		}
		return temp;
	}
	
	/**
	 * <p> Return a bdd after existentially quantifying all the variables except post system vars</p>
	 * @return BDD with only post system vars
	 */
	public static BDD removeAllExceptPostSystemVars(BDD bdd) throws PlanningException
	{
		BDD temp	= bdd;
		try
		{
			temp	= temp.exist(propertyDomainPre().set());
		} catch(BDDException E)
		{
			E.printStackTrace();
		}
		try
		{
			temp	= temp.exist(propertyDomainPost().set());
		} catch(BDDException E)
		{
			E.printStackTrace();
		}
		try
		{
			temp	= temp.exist(acceptingSetDomain().set());
		} catch(BDDException E)
		{
			E.printStackTrace();
		}
		try
		{
			temp	= temp.exist(transitionLevelDomain().set());
		} catch(BDDException E)
		{
			E.printStackTrace();
		}
		for(int i=0; i<numAPSystem; i++) 
		{
			try
			{
				temp	= temp.exist(ithVarSystemPre(i));
			} catch(BDDException E)
			{
				E.printStackTrace();
			}
		}
		for(int i=0; i<numVars[2]; i++) 
		{
			try
			{
				temp	= temp.exist(ithVarLabel(i));
			} catch(BDDException E)
			{
				E.printStackTrace();
			}
		}
		return temp;
	}
	
	public static BDD removeAllExceptSystemVars(BDD bdd) throws PlanningException {
		BDD temp	= bdd;
		try
		{
			temp	= temp.exist(propertyDomainPre().set());
		} catch(BDDException E)
		{
			E.printStackTrace();
		}
		try
		{
			temp	= temp.exist(propertyDomainPost().set());
		} catch(BDDException E)
		{
			E.printStackTrace();
		}
		try
		{
			temp	= temp.exist(acceptingSetDomain().set());
		} catch(BDDException E)
		{
			E.printStackTrace();
		}
		try
		{
			temp	= temp.exist(transitionLevelDomain().set());
		} catch(BDDException E)
		{
			E.printStackTrace();
		}
		for(int i=0; i<numVars[2]; i++) 
		{
			try
			{
				temp	= temp.exist(ithVarLabel(i));
			} catch(BDDException E)
			{
				E.printStackTrace();
			}
		}
		return temp;
	}
	
	
	
	/**
	 * <p>Computes pre-image of a set of states. Pre-image of a set of states S are all the states which have an outgoing to a state in S.</p>
	 * @return pre-image of states in product automaton
	 */
	public BDD preImageConcrete(BDD states) throws PlanningException
	{
		if(! hasOnlyPreVars(states)) 
		{
			throw new PlanningException("BDD has extra vars");
		}
		BDD temp	= factory.zero();
		temp		= temp.or(changePreVarsToPostVars(states));
		return removeAllExceptPreVars(productAutomatonBDD.and(transitionLevelDomain().ithVar(3)).and(temp));
	}

	/**
	 * <p>Computes pre-image of a set of states consisting of only system vars (look at preImageConcrete). It is equivalent to computing pre image of a set of states in the abstraction</p>
	 * @return pre-image of states (with only system vars) in product automaton
	 */
	public BDD preImageSystem(BDD states) throws PlanningException
	{
		if(! hasOnlyPreSystemVars(states)) 
		{
			throw new PlanningException("BDD has extra vars");
		}
		BDD temp		= changePreVarsToPostVars(states);
		return removeAllExceptPreSystemVars(productAutomatonBDD.and(temp));
	}

	/**
	 * <p>Computes post-image of a set of states. Post-image of a set of states S are all the successors of S.</p>
	 * @return post-image of states in product automaton
	 */
	public BDD postImageConcrete(BDD states) throws PlanningException
	{
		if(! hasOnlyPreVars(states))
		{
			throw new PlanningException("BDD has extra vars");
		}
		return changePostVarsToPreVars(removeAllExceptPostVars(productAutomatonBDD.and(transitionLevelDomain().ithVar(3)).and(states)));
	}

	/**
	 * <p>Computes post-image of a set of states consisting of only system vars (look at postImageConcrete). It is equivalent to computing post image of a set of states in the abstraction</p>
	 * @return post-image of states in product automaton
	 */
	public BDD postImageSystem(BDD states) throws PlanningException
	{
		if(! hasOnlyPreVars(states)) 
		{
			throw new PlanningException("BDD has extra vars");
		}
		return changePostVarsToPreVars(removeAllExceptPostSystemVars(productAutomatonBDD.and(states)));
	}

	/**
	 *
	 * @param i For specifying i-th set
	 * @return first states of all accepting transitions of ith accepting set
	 */
	public BDD getAcceptingStates(int i) throws PlanningException 
	{
		return removeAllExceptPreVars(productAutomatonBDD.and(acceptingSetDomain().ithVar(i)));
	}
	
	
//-------------------------------------------------------------------------------------------------------------------

	
//-------------------------------------------------------------------------------------------------------------------
// *** Label

	
	
	/**
	 * <p>Check if ith system var is used in property, returns the index of that var(AP) in apListProperty</p>
	 * @return index of var in apListProperty
	 */
	private static int findIthSystemVarInLabel(int i) throws PlanningException 
	{
		if(i > numAPSystem) 
		{
			throw new PlanningException("Index out of bounds");
		}
		String toSearch		= apListSystem.get(i);
		for(int j=0; j<numVars[2]; j++) 
		{
			if(apListProperty.get(j).equals(toSearch)) 
			{
				return j;
			}
		}
		return -1;
	}
	
	/**
	 * <p>finds the ith label var in system vars</p>
	 * @param i
	 * @return index of ith label var in system
	 * @throws PlanningException 
	 */
	private static int findIthLabelVarInSystem(int i) throws PlanningException
	{
		if(i>numVars[2]) 
		{
			throw new PlanningException("Index out of bounds");
		}
		String toSearch=apListProperty.get(i);
		for(int j=0; j<numAPSystem; j++) 
		{
			if(toSearch.equals(apListSystem.get(j))) 
			{
				return j;
			}
		}
		return -1;
	}

	/**
	 * <p>Restrict to only variables used in property, used only by findLabelBDDFromSystemBDD</p>
	 * @param states BDD representing set of states
	 * @return 
	 * @throws PlanningException 
	 * @throws PlanningException 
	 */
	private static BDD restrictToLabelVarsInPreSystem(BDD states) throws PlanningException
	{
		if(! hasOnlyPreSystemVars(states)) 
		{
			throw new PlanningException("Set of States has other variables than System vars");
		}
		BDD temp	= states.exist(propertyDomainPre().set());
		temp		= temp.exist(acceptingSetDomain().set());
		temp		= temp.exist(transitionLevelDomain().set());
		for(int i=0; i<numAPSystem; i++) 
		{
			temp.exist(ithVarSystemPost(i));
			if(findIthSystemVarInLabel(i)==-1) 
			{
				temp.exist(ithVarSystemPre(i));
			}
		}
		for(int i=0; i<numVars[2]; i++) 
		{
			temp.exist(ithVarLabel(i));
		}
		return temp;
	}
	
	/**
	 * <p> Return a bdd after existentially quantifying all the variables except label vars</p>
	 * @param bdd
	 * @return BDD with only label vars
	 * @throws PlanningException 
	 */
	public static BDD removeAllExceptLabelVars(BDD bdd) throws PlanningException
	{
		BDD temp	= bdd;
		try 
		{
			temp 	= temp.exist(propertyDomainPre().set());
		} catch(BDDException E)
		{
			E.printStackTrace();
		}
		try
		{
			temp	= temp.exist(propertyDomainPost().set());
		} catch(BDDException E)
		{
			E.printStackTrace();
		}
		try
		{
			temp	= temp.exist(acceptingSetDomain().set());
		} catch(BDDException E)
		{
			E.printStackTrace();
		}
		temp		= temp.exist(transitionLevelDomain().set());
		for(int i=0; i<numAPSystem; i++) 
		{
			try
			{
				temp	= temp.exist(ithVarSystemPost(i));
			} catch(BDDException E)
			{
				E.printStackTrace();
			}
			try
			{
				temp	= temp.exist(ithVarSystemPre(i));
			} catch(BDDException E)
			{
				E.printStackTrace();
			}
		}
		return temp;
	}
	
	/**
	 * <p>Restrict the bdd to only contain variables used in property and returns the corresponding bdd in label variables</p>
	 * @param states BDD representing set of states
	 * @return label BDD corresponding to states
	 * @throws PlanningException 
	 * @throws PlanningException 
	 */
	public static BDD findLabelBDDFromSystemBDD(BDD states) throws PlanningException
	{
		if(! hasOnlyPreSystemVars(states)) 
		{
			throw new PlanningException("Set of States has other variables than System vars");
		}
		BDD temp	= restrictToLabelVarsInPreSystem(states);
		temp		= removeAllExceptLabelVars(temp.replace(statesToLabelPairing));
		if(temp.isOne()) 
		{
			return factory.zero();
		}
		return temp;
	}
	
	/**
	 * <p>maintains an equivalence between Label vars and pre system vars, equate the values of the vars representing same atomic prop </p>
	 * @return
	 * @throws PlanningException
	 */
	public static BDD getLabelEquivalence() throws PlanningException
	{
		BDD equivalence=factory.one();
		for(int i=0; i<numVars[2]; i++) 
		{
			equivalence.andWith(ithVarLabel(i).biimp(ithVarSystemPost(findIthLabelVarInSystem(i))));
		}
		return equivalence;
	}
	
	
	
//----------------------------------------------------------------------------------------------------------------	


//----------------------------------------------------------------------------------------------------------------

// *** Transitions

	
	
	/**
	 * <p>Remove a set of transitions from the productAutomaton</p>
	 * @param transitions
	 */
	public void removeTransitions(BDD transitions) 
	{
		productAutomatonBDD=productAutomatonBDD.and(productAutomatonBDD.and(transitions).not());
	}
	
	/**
	 * <p>Find the level of transition, if the transition doesn't exist, return -1</p>
	 * @param transition
	 * @return level of the transition
	 * @throws PlanningException
	 */
	public static int getLevel(BDD transition) throws PlanningException
	{
		if(transition.pathCount() > 1) 
		{
			throw new PlanningException("More than one transition");
		}
		if(transition.pathCount() < 1)
		{
			throw new PlanningException("No transition exists");
		}
		return transition.scanVar(transitionLevelDomain()).intValue();
	}
	
	

	/**
	 * <p>Find the level of transition from fromState to toState, if the transition doesn't exist yet, return -1</p>
	 * @param fromState
	 * @param toState
	 * @return level of the transition
	 * @throws PlanningException 
	 * @throws PlanningException 
	 */
	public int getLevel(BDD fromState, BDD toState) throws PlanningException
	{
		BDD toStatePrime	= changePreVarsToPostVars(toState);
		BDD transition		= productAutomatonBDD.and(fromState).and(toStatePrime);
		return getLevel(transition);
	}
	
	/**
	 * <p>Change the level of a transition</p>
	 * @param transition
	 * @param level
	 * @throws PlanningException 
	 */
	public void setLevel(BDD transition, int level) throws PlanningException 
	{
		if(transition.and(productAutomatonBDD).isZero()) 
		{
			throw new PlanningException("Transition doesn't exist");
		}
		removeTransitions(transition.and(transitionLevelDomain().ithVar(2)));
		transition	= transition.exist(transitionLevelDomain().set()).and(transitionLevelDomain().ithVar(level));
		productAutomatonBDD.orWith(transition);
	}
	
	/**
	 * <p>Change the level of a transition from fromState to toState</p>
	 * @param fromState
	 * @param toState
	 * @param level
	 * @throws PlanningException 
	 * @throws PlanningException 
	 */
	public void setLevel(BDD fromState, BDD toState, int level) throws PlanningException
	{
		if(! hasOnlyPreVars(fromState) 	||	 ! hasOnlyPreVars(toState)) 
		{
			throw new PlanningException("Extra vars appearing in a state BDD");
		}
		BDD toStatePrime	= changePreVarsToPostVars(toState);
		BDD transition		= productAutomatonBDD.and(fromState).and(toStatePrime);
		setLevel(transition, level);
	}
	
	/**
	 * Returns the list of accepting sets 
	 * @param transition
	 * @return
	 */
	public static ArrayList<Integer> findAcceptingSets(BDD transition)
	{
		int accSet				= transition.scanVar(acceptingSetDomain()).intValue();
		ArrayList<Integer>temp	= new ArrayList<>();
		temp.add(accSet);
		return temp;
	}
	
	/**
	 * <p>Add a transition in the product automaton</p>
	 * @param level
	 * @return 
	 * @throws PlanningException 
	 */
	public BDD addTransition(BDD transitions, int level) throws PlanningException 
	{
		transitions				= propertyBDD.and(getLabelEquivalence()).and(transitions).and(transitionLevelDomain().ithVar(level));
		productAutomatonBDD		= productAutomatonBDD.or(transitions);
		return transitions;
	}
	
	/**
	 * Remove all transitions from "fromState" to "toState"
	 * @throws PlanningException
	 */
	public void removeTransition(BDD transitions) throws PlanningException 
	{
		productAutomatonBDD		= productAutomatonBDD.and((propertyBDD.and(getLabelEquivalence()).and(transitions.not())));
	}
	
	/**
	 * <p>Returns the set of transitions in the product automaton from a state in 'fromState' to a state in 'toState'</p>
	 * @param fromStates
	 * @param toStates
	 * @return BDD representing the transition from fromState to toState
	 * @throws PlanningException 
	 */
	public BDD getTransitions(BDD fromStates, BDD toStates) throws PlanningException
	{
		if(! hasOnlyPreVars(fromStates) 	||	 ! hasOnlyPreVars(toStates)) 
		{
			throw new PlanningException("Extra vars appearing in a state BDD");
		}
		BDD transitions			= changePreVarsToPostVars(toStates);
		transitions				= transitions.and(fromStates);
		transitions				= productAutomatonBDD.and(transitions);
		return transitions;
	}

	/**
	 * <p>Add a set of transitions to the product automaton</p>
	 * @param transitions
	 */
	public void addTransitions(BDD transitions) {
		productAutomatonBDD=productAutomatonBDD.or(transitions);		
	}
	
	/**
	 * 
	 * @param transition
	 * @return first state in the transition i.e if (p->q) is the transition then return p
	 * @throws PlanningException
	 */
	public static BDD getFirstState(BDD transition) throws PlanningException
	{
		return removeAllExceptPreVars(transition);
	}
	
	/**
	 * 
	 * @param transition
	 * @return second state in the transition i.e if (p->q) is the transition then return q
	 * @throws PlanningException
	 */
	public static BDD getSecondState(BDD transition) throws PlanningException
	{
		return changePostVarsToPreVars(removeAllExceptPostVars(transition));
	}
	
	/**
	 * 
	 * @param transition
	 * @return second state in the transition i.e if (p->q) is the transition then return q
	 * @throws PlanningException
	 */
	public static BDD getFirstStateSystem(BDD transition) throws PlanningException
	{
		return removeAllExceptPreSystemVars(transition);
	}
	
	/**
	 * 
	 * @param transition
	 * @return second state in the transition i.e if (p->q) is the transition then return q
	 * @throws PlanningException
	 */
	public static BDD getSecondStateSystem(BDD transition) throws PlanningException
	{
		return changePostSystemVarsToPreSystemVars(removeAllExceptPostSystemVars(transition));
	}
	
	
	/**
	 * 
	 * @param transition
	 * @return primed first state in the transition i.e if (p->q) is the transition then return p'
	 * @throws PlanningException
	 */
	public static BDD getFirstStatePrime(BDD transition) throws PlanningException
	{
		return changePreVarsToPostVars(removeAllExceptPreVars(transition));
	}
	
	/**
	 * 
	 * @param transition
	 * @return primed second state in the transition i.e if (p->q) is the transition then return q'
	 * @throws PlanningException
	 */
	public static BDD getSecondStatePrime(BDD transition) throws PlanningException
	{
		return removeAllExceptPostVars(transition);
	}
	
	/**
	 * 
	 * @param transition
	 * @return primed second state in the transition i.e if (p->q) is the transition then return q'
	 * @throws PlanningException
	 */
	public static BDD getSecondStateSystemPrime(BDD transition) throws PlanningException
	{
		return removeAllExceptPostSystemVars(transition);
	}
	
	/**
	 * 
	 * @param transition
	 * @return primed second state in the transition i.e if (p->q) is the transition then return q'
	 * @throws PlanningException
	 */
	public static BDD getFirstStateSystemPrime(BDD transition) throws PlanningException
	{
		return changePostSystemVarsToPreSystemVars(removeAllExceptPostSystemVars(transition));
	}
	
	/**
	 * 
	 * @param i
	 * @return all accepting transitions of ith accepting set
	 */
	public BDD getAcceptingTransitions(int i) 
	{
		return productAutomatonBDD.and(acceptingSetDomain().ithVar(i));
	}
	
//-----------------------------------------------------------------------------------------------------------------
	
	
//-----------------------------------------------------------------------------------------------------------------
// *** Counter and stateID

	

	/**
	 * <p>Find the ID of a system state</p>
	 * @param fromState
	 * @return ID of the state
	 * @throws PlanningException 
	 */
	public static int getSystemStateID(BDD fromState) throws PlanningException
	{
		if(fromState.satCount(allPreSystemVars())>1) 
		{
			throw new PlanningException("More than one state");
		}
		if(fromState.satCount(allPreSystemVars())==0)
		{
			throw new PlanningException("State doesn't exist");
		}
		int stateID		= 0;
		for(int i=0; i<numAPSystem; i++) 
		{
			if(! fromState.and(ithVarSystemPre(i)).equals(factory.zero())) 
			{
				stateID	+= (int) StrictMath.pow(2, i);
			}
		}
		return stateID;
	}
	
	/**
	 * <p>Find the ID of a state in product automaton</p>
	 * @param fromState
	 * @return ID of the state
	 * @throws PlanningException 
	 */
	public static int getStateID(BDD fromState) throws PlanningException
	{
		if(fromState.satCount(allPreVars())>1) 
		{
			throw new PlanningException("More than one state");
		}
		if(fromState.satCount(allPreVars()) == 0)
		{
			throw new PlanningException("State doesn't exist");
		}
		int stateID		= fromState.scanVar(propertyDomainPre()).intValue();
		for(int i=0; i<numAPSystem; i++) 
		{
			if(! fromState.and(ithVarSystemPre(i)).equals(factory.zero())) 
			{
				stateID	+= (int) StrictMath.pow(2, i+propertyDomainPre().varNum());
			}
		}
		return stateID;
	}

	/**
	 * 
	 * @param id
	 * @return BDD for the state with stateID id.
	 * @throws PlanningException
	 */
	public static BDD getStateFromID(int id) throws PlanningException
	{
		BDD state		= factory.one();
		int i			= numAPSystem-1;
		while(i >= 0) 
		{
			
			if(id/(int) StrictMath.pow(2, i+propertyDomainPre().varNum()) 	>= 	1)
			{
				id = (int) (id % StrictMath.pow(2, i + propertyDomainPre().varNum()));
				state	= state.and(ithVarSystemPre(i));
			} else 
			{
				state	= state.and(ithVarSystemPre(i).not());
			}
			i--;
		}
		state			= state.and(propertyDomainPre().ithVar(id));
		return state;
	}
	
	/**
	 * <p>Increase the counter of a state</p>
	 * @param fromState
	 * @return counter value for that state
	 * @throws PlanningException
	 */
	public int increaseCounter(BDD fromState) throws PlanningException
	{
		int stateID		= getSystemStateID(fromState);
		counter[stateID]++;
		return counter[stateID];
	}
	
	
	
	

//-------------------------------------------------------------------------------------------------------------------

	
//-------------------------------------------------------------------------------------------------------------------	


	/**
 	* Check Accepting condition
 	* @throws PlanningException
 	*/
	public ArrayList<BDD> findAcceptingPath() throws PlanningException 
	{
		EmptinessCheck newCheck	= new EmptinessCheck(this);
		return newCheck.findAcceptingPath(getInitStates());
	}

	public ArrayList<BDD> findAcceptingPath(List<BDD> movementBDD) throws PlanningException {
		EmptinessCheck newCheck = new EmptinessCheck(this);
		BDD st = findCurrentState(movementBDD);
		if(st.isZero()){
			return null;
		}
		return newCheck.findAcceptingPath(st);
	}

	private BDD findCurrentState(List<BDD> movementBDD) throws PlanningException {
		Iterator<BDD> it = movementBDD.iterator();
		BDD currentState = it.next().and(propertyDomainPre().ithVar(0));
		movementBDD.set(0, currentState);
		int i = 1;
		while(it.hasNext()) {
			BDD nextState = it.next();
			currentState = postImageConcrete(currentState).and(nextState);
			movementBDD.set(i, currentState);
			i++;
		}
		return currentState;
	}


//-------------------------------------------------------------------------------------------------------------------

	
//-------------------------------------------------------------------------------------------------------------------	

// *** Printing

	
	
	/**
	 * <p>Create a dot file representing the product automaton</p>
	 * @throws PlanningException 
	 */
	public void createDot() throws java.io.IOException, PlanningException {
		new PrintProductAutomaton(this);
	}

	/**
	 * Print the accepting path
	 * @param path
	 * @throws PlanningException
	 */
	public static void printPath(ArrayList<BDD> path) throws PlanningException
	{
		new PrintAcceptingPath(path);
	}





	public static BDD interchangePrePostVars(BDD bdd) 
	{
		BDDPairing newPairing 	= factory.makePair();
		newPairing.set(factory.getDomain(0), factory.getDomain(1));
		for(int i=0; i<numAPSystem; i++) {
			newPairing.set(varsBeforeSystemVars + i, 	  varsBeforeSystemVars + numAPSystem + i);
			newPairing.set(varsBeforeSystemVars + numAPSystem + i, 		varsBeforeSystemVars + i);
		}
		return bdd.replace(newPairing);
	}





	public boolean isAcceptingTransition(BDD transition) throws PlanningException 
	{
		if(transition == null) {
			return false;
		}
		BDD test = productAutomatonBDD.and(acceptingSetDomain().ithVar(0).not());
		return !productAutomatonBDD.and(propertyBDD.and(getLabelEquivalence()).and(transition).and(transitionLevelDomain().ithVar(3)).and(acceptingSetDomain().ithVar(0).not())).isZero();
	}





	public BDD incomingTransitions(BDD source) throws PlanningException {
		source = changePreSystemVarsToPostSystemVars(source);
		return removeAllExceptSystemVars(productAutomatonBDD.and(source));
	}



//-------------------------------------------------------------------------------------------------------------------

	
//-------------------------------------------------------------------------------------------------------------------	

	
}


