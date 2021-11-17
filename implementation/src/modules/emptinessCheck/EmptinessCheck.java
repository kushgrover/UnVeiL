package modules.emptinessCheck;

import java.util.ArrayList;
import java.util.List;

import abstraction.ProductAutomaton;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDD.BDDIterator;
import net.sf.javabdd.BDDFactory;
import settings.PlanningException;

/**
 * @author kush
 *
 */
public class EmptinessCheck {

	ProductAutomaton productAutomaton;
	BDDFactory factory;
	stackTODO todo;
	stackSCC SCC;
	int max=0;
	int totalAcceptingSets;
	int[] rank;
	BDD statesSCC = null;

	public EmptinessCheck(ProductAutomaton productAutomaton) {
		this.productAutomaton	= productAutomaton;
		factory					= ProductAutomaton.factory;
		todo					= new stackTODO();
		SCC						= new stackSCC();
		totalAcceptingSets		= 1;
		rank					= new int[ProductAutomaton.getNumStates()];
		for(int i=0; i<ProductAutomaton.getNumStates(); i++)
		{
			rank[i]				=- 1;
		}
	}

	public ArrayList<BDD> findAcceptingPath(BDD initState) throws PlanningException
	{
		push(null, initState);
		while(! todo.isEmpty()) 
		{	
			if(todo.getTop().getTransitions().isZero())
			{
				pop();
			}
			else 
			{
				BDD transition	= todo.getOneSuccFromTop();
				BDD toState		= productAutomaton.getSecondState(transition);
//				BDD h	= ProductAutomaton.ithVarSystemPre(0).not();
//				BDD r1	= ProductAutomaton.ithVarSystemPre(1).not();
//				BDD r2	= ProductAutomaton.ithVarSystemPre(2).not();
//				BDD r3	= ProductAutomaton.ithVarSystemPre(3).not();
//				BDD r4	= ProductAutomaton.ithVarSystemPre(4).not();
//				BDD r5	= ProductAutomaton.ithVarSystemPre(5).not();
//				BDD r6	= ProductAutomaton.ithVarSystemPre(6).not();
//				BDD b	= ProductAutomaton.ithVarSystemPre(7);
//				BDD t	= ProductAutomaton.ithVarSystemPre(8).not();
//				BDD onlyBin = h.and(r1).and(r2).and(r3).and(r4).and(r5).and(r6).and(b).and(t);
//				if(! toState.and(onlyBin).isZero()){
//					System.out.print(" ");
//				}
				if(toState.isZero()) 
				{
					throw new PlanningException("State does not exist");
				}
				ArrayList<Integer> accSet = productAutomaton.findAcceptingSets(transition);
				if(getRank(toState) == -1) 
				{
					push(accSet,toState);
				}
				else if(getRank(toState)>0) 
				{
					if(containAllAcc(merge(accSet,getRank(toState)))) 
					{
						return findPath(initState);
					}
				}
			}
		}
		return null;		
	}
	
	private ArrayList<BDD> findPath(BDD initState) throws PlanningException
	{
		BDD statesSCC				= initializeStatesSCC();
		ArrayList<BDD> path			= new ArrayList<>();
		int[] visitedAcceptingSets	= new int[totalAcceptingSets + 1];
		

		if(initState.and(statesSCC).isZero()) {
			bfs(initState, statesSCC, path, visitedAcceptingSets);
		}
		else{
			path.add(initState);
		}
		for(int i=0; i<totalAcceptingSets; i++)
		{
 			bfs(path.get(path.size() - 1), visitedAcceptingSets, path);
		}
		return path;
	}

	private BDD initializeStatesSCC() throws PlanningException 
	{
		statesSCC			= factory.zero();
		int topRank			= SCC.getTop().getRoot();
		for(int i=0; i<ProductAutomaton.getNumStates(); i++)
		{
			if(rank[i] >= topRank) 
			{
				statesSCC	= statesSCC.or(productAutomaton.getStateFromID(i));
			}
		}
		return statesSCC;
	}

	private void bfs(BDD fromState, int[] visitedAcceptingSets, List<BDD> path) throws PlanningException
	{
		ArrayList<BDD> setStates	= new ArrayList<>();
		setStates.add(fromState);
		BDD accTransitions			= factory.zero();
		for(int j=1; j<totalAcceptingSets+1; j++) 
		{
			if(visitedAcceptingSets[j] == 0) 
			{
				accTransitions		= accTransitions.or(productAutomaton.getAcceptingTransitions(j));
			}
		}
		if(accTransitions.isZero()){
			return;
		}
		int i = 0;
		while(accTransitions.and(setStates.get(i)).and(productAutomaton.changePreVarsToPostVars(statesSCC)).isZero())
		{
			setStates.add(productAutomaton.postImageConcrete(setStates.get(i)).and(statesSCC));
			i++;
		}
		BDD usefulTransitions	= accTransitions.and(setStates.get(i)).and(productAutomaton.changePreVarsToPostVars(statesSCC));
		setStates.set(i, productAutomaton.getFirstState(usefulTransitions));
		i--;
		while(i > 0) 
		{
			setStates.set(i, setStates.get(i).and(productAutomaton.preImageConcrete(setStates.get(i+1))));
			i--;
		}
		for(i=1; i<setStates.size(); i++) 
		{
			setStates.set(i,setStates.get(i).satOne(ProductAutomaton.allPreVars(),false));
			if(i < setStates.size()-1) 
			{
				setStates.set(i+1, productAutomaton.postImageConcrete(setStates.get(i)).and(setStates.get(i+1)));
			}
		}
		path.addAll(setStates);
		BDD transition					= (setStates.get(i-1).and(accTransitions));
//		BDD toState						= productAutomaton.getSecondState(transition);
		int accSet						= transition.scanVar(ProductAutomaton.acceptingSetDomain()).intValue();
		visitedAcceptingSets[accSet]	= 1;
	}

	private void bfs(BDD fromState, BDD toState, List<BDD> path, int[] visitedAcceptingSets) throws PlanningException
	{		
		ArrayList<BDD> setStates		= new ArrayList<>();
		setStates.add(fromState);
		int i							= 0;
		while(setStates.get(i).and(toState).isZero()) 
		{
			setStates.add(productAutomaton.postImageConcrete(setStates.get(i)));
			i++;
		}
		setStates.set(i, setStates.get(i).and(toState));
		i--;
		while(i > 0) 
		{
			setStates.set(i, setStates.get(i).and(productAutomaton.preImageConcrete(setStates.get(i+1))));
			i--;
		}
		for(i=0; i<setStates.size(); i++) 
		{
			setStates.set(i,setStates.get(i).satOne(ProductAutomaton.allPreVars(),false));
			if(i < setStates.size()-1) {
				setStates.set(i+1, productAutomaton.postImageConcrete(setStates.get(i)).and(setStates.get(i+1)));
				BDD transition = setStates.get(i).and(productAutomaton.changePreVarsToPostVars(setStates.get(i+1)));
				transition = transition.and(productAutomaton.getBDD()).and(ProductAutomaton.transitionLevelDomain().ithVar(3));
				List<Integer> k = productAutomaton.findAcceptingSets(transition);
				for (int k_n : k) {
					if (k_n > 0) {
						visitedAcceptingSets[k_n] = 1;
						path.add(setStates.get(i));
						return;
					}
				}
			}
			path.add(setStates.get(i));
		}
		
	}

	private boolean containAllAcc(List<Integer> accSet)
	{
		for(int i=1; i<totalAcceptingSets+1; i++) 
		{
			if(! accSet.contains(i)) 
			{
				return false;
			}
		}
		return true;
	}
	
	private static ArrayList<Integer> union(ArrayList<Integer> nextSuccAcc, Iterable<Integer> arrayList)
	{
		for(int i: arrayList) 
		{
			if(! nextSuccAcc.contains(i)) 
			{
				nextSuccAcc.add(i);
			}
		}
		return nextSuccAcc;
	}

	private int getRank(BDD state) throws PlanningException 
	{
		int id		= productAutomaton.getStateID(state);
		return rank[id];
	}
	
	private void setRank(BDD state, int num) throws PlanningException 
	{
		int id		= productAutomaton.getStateID(state);
		rank[id]	= num;
	}

	private void push(ArrayList<Integer> acc, BDD state) throws PlanningException 
	{
		max++;
		setRank(state,max);
		SCC.push(max, acc, new ArrayList<>(), factory.zero());
		todo.push(state, state.and(productAutomaton.getSampledProductTransitions()));
	}

	private void pop() throws PlanningException 
	{
		BDD state	= productAutomaton.getFirstState(todo.pop().getState());
		SCC.getTop().setRem(SCC.getTop().getRem().or(state));
		if(getRank(state) == SCC.getTop().getRoot()) 
		{
			BDDIterator iterator	= SCC.getTop().getRem().iterator(ProductAutomaton.allPreVars());
			while(iterator.hasNext()) 
			{
				BDD st				= (BDD) iterator.next();
				setRank(st,0);
			}
			SCC.pop();
		}
	}
	
	private List<Integer> merge(ArrayList<Integer> nextSuccAcc, int t)
	{
		BDD r	= factory.zero();
		while(t<SCC.getTop().getRoot()) 
		{
			nextSuccAcc	= union(nextSuccAcc,SCC.getTop().getAcc());
			nextSuccAcc	= union(nextSuccAcc,SCC.getTop().getLast());
			r			= r.or(SCC.getTop().getRem());
			SCC.pop();
		}
		SCC.getTop().setAcc(union(SCC.getTop().getAcc(),nextSuccAcc));
		SCC.getTop().setRem(SCC.getTop().getRem().or(r));
		return SCC.getTop().getAcc();
	}
}
