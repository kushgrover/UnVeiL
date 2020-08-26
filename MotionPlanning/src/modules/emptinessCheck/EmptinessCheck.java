/**
 * 
 */
package modules.emptinessCheck;

import java.util.ArrayList;

import abstraction.ProductAutomaton;
import abstraction.exceptions.StateException;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDD.BDDIterator;
import net.sf.javabdd.BDDFactory;

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
	BDD statesSCC;
	public EmptinessCheck(ProductAutomaton productAutomaton){
		this.productAutomaton=productAutomaton;
		factory=ProductAutomaton.factory;
		todo=new stackTODO();
		SCC=new stackSCC();
		totalAcceptingSets=1;
		rank=new int[ProductAutomaton.getNumStates()];
		for(int i=0;i<ProductAutomaton.getNumStates();i++) {
			rank[i]=-1;
		}
	}
	
//	public boolean findAcceptingPath() throws Exception{
	public ArrayList<BDD> findAcceptingPath() throws Exception{
		push(null, productAutomaton.getInitStates());
		while(! todo.isEmpty()) {
			
			if(todo.getTop().getTransitions().isZero()){
				pop();
			}
			else {
				BDD transition=todo.getOneSuccFromTop();
				BDD toState=productAutomaton.getSecondState(transition);
				if(toState.isZero()) {
					throw new StateException("State does not exist");
				}
				ArrayList<Integer> accSet=productAutomaton.findAcceptingSets(transition);
				if(getRank(toState)==-1) {
					push(accSet,toState);
				}
				else if(getRank(toState)>0) {
					if(containAllAcc(merge(accSet,getRank(toState)))) {
						return findPath();
					}
				}
			}
		}
		return null;		
	}
	
	private ArrayList<BDD> findPath() throws Exception {
		BDD statesSCC=initializeStatesSCC();
		ArrayList<BDD> path=new ArrayList<BDD>();
		ArrayList<BDD> milestones=new ArrayList<BDD>();
		int[] visitedAcceptingSets=new int[totalAcceptingSets+1];
		
		milestones.add(statesSCC.satOne(ProductAutomaton.allPreVars(),false));
		bfs(productAutomaton.getInitStates(),milestones.get(0),path);
		for(int i=0;i<totalAcceptingSets;i++) {
 			milestones.add(bfs(milestones.get(i),visitedAcceptingSets,path));
		}
		bfs(milestones.get(totalAcceptingSets),milestones.get(0),path);
		return path;
	}

	private BDD initializeStatesSCC() throws Exception {
		statesSCC=factory.zero();
		int topRank=SCC.getTop().getRoot();
		for(int i=0;i<ProductAutomaton.getNumStates();i++){
			if(rank[i]>=topRank) {
				statesSCC=statesSCC.or(productAutomaton.getStateFromID(i));
			}
		}
		return statesSCC;
	}

	private BDD bfs(BDD fromState, int[] visitedAcceptingSets, ArrayList<BDD> path) throws StateException, Exception {
		ArrayList<BDD> setStates=new ArrayList<BDD>();
		setStates.add(fromState);
		int i=0;
		BDD accTransitions=factory.zero();
		for(int j=1;j<totalAcceptingSets+1;j++) {
			if(visitedAcceptingSets[j]==0) {
				accTransitions=accTransitions.or(productAutomaton.getAcceptingTransitions(j));
			}
		}
		while(accTransitions.and(setStates.get(i)).and(productAutomaton.changePreVarsToPostVars(statesSCC)).isZero()) {
			setStates.add(productAutomaton.postImage(setStates.get(i)).and(statesSCC));
			i++;
		}
//		productAutomaton.postImage(setStates.get(0)).and(statesSCC).printDot();
//		System.out.println(productAutomaton.getStateID(productAutomaton.postImage(setStates.get(0)).and(statesSCC)));
		BDD usefulTransitions=accTransitions.and(setStates.get(i)).and(productAutomaton.changePreVarsToPostVars(statesSCC));
		setStates.set(i, productAutomaton.getFirstState(usefulTransitions));
		i--;
		while(i>0) {
			setStates.set(i, setStates.get(i).and(productAutomaton.preImage(setStates.get(i+1))));
			i--;
		}
		for(i=1;i<setStates.size();i++) {
			setStates.set(i,setStates.get(i).satOne(ProductAutomaton.allPreVars(),false));
			if(i<setStates.size()-1) {
				setStates.set(i+1, productAutomaton.postImage(setStates.get(i)).and(setStates.get(i+1)));
			}
		}
//		setStates.get(0).printDot();
//		setStates.get(1).and(productAutomaton.changePreVarsToPostVars(setStates.get(2))).and(productAutomaton.getSampledProductTransitions()).printDot();
//		setStates.get(2).printDot();
		path.addAll(setStates);
		BDD transition=(setStates.get(i-1).and(accTransitions));
		BDD toState=productAutomaton.getSecondState(transition);
		int accSet=transition.scanVar(ProductAutomaton.acceptingSetDomain()).intValue();
		visitedAcceptingSets[accSet]=1;
		return toState;
	}

	private void bfs(BDD fromState, BDD toState, ArrayList<BDD> path) throws StateException, Exception {		ArrayList<BDD> setStates=new ArrayList<BDD>();
		setStates.add(fromState);
		int i=0;
		while(setStates.get(i).and(toState).isZero()) {
			setStates.add(productAutomaton.postImage(setStates.get(i)));
			i++;
		}
		setStates.set(i, toState);
		i--;
		while(i>0) {
			setStates.set(i, setStates.get(i).and(productAutomaton.preImage(setStates.get(i+1))));
			i--;
		}
		for(i=0;i<setStates.size();i++) {
			setStates.set(i,setStates.get(i).satOne(ProductAutomaton.allPreVars(),false));
			if(i<setStates.size()-1) {
				setStates.set(i+1, productAutomaton.postImage(setStates.get(i)));
			}
			path.add(setStates.get(i));
		}
		
	}

	private boolean containAllAcc(ArrayList<Integer> accSet) {
		for(int i=1;i<totalAcceptingSets+1;i++) {
			if(! accSet.contains(i)) {
				return false;
			}
		}
		return true;
	}
	
	private ArrayList<Integer> union(ArrayList<Integer> nextSuccAcc, ArrayList<Integer> arrayList) {
		for(int i: arrayList) {
			if(! nextSuccAcc.contains(i)) {
				nextSuccAcc.add(i);
			}
		}
		return nextSuccAcc;
	}

	private int getRank(BDD state) throws Exception {
		int id=productAutomaton.getStateID(state);
		return rank[id];
	}
	
	private void setRank(BDD state, int num) throws Exception {
		int id=productAutomaton.getStateID(state);
		rank[id]=num;
	}

	private void push(ArrayList<Integer> acc, BDD state) throws Exception {
		max++;
		setRank(state,max);
		SCC.push(max, acc, new ArrayList<Integer>(), factory.zero());
		todo.push(state, state.and(productAutomaton.getSampledProductTransitions()));
	}

	private void pop() throws Exception {
		BDD state=productAutomaton.getFirstState(todo.pop().getState());
		SCC.getTop().setRem(SCC.getTop().getRem().or(state));
		if(getRank(state)==SCC.getTop().getRoot()) {
			BDDIterator iterator=SCC.getTop().getRem().iterator(ProductAutomaton.allPreVars());
			while(iterator.hasNext()) {
				BDD st=(BDD) iterator.next();
				setRank(st,0);
			}
			SCC.pop();
		}
	}
	
	private ArrayList<Integer> merge(ArrayList<Integer> nextSuccAcc, int t) {
		BDD r=factory.zero();
		while(t<SCC.getTop().getRoot()) {
			nextSuccAcc=union(nextSuccAcc,SCC.getTop().getAcc());
			nextSuccAcc=union(nextSuccAcc,SCC.getTop().getLast());
			r=r.or(SCC.getTop().getRem());
			SCC.pop();
		}
		SCC.getTop().setAcc(union(SCC.getTop().getAcc(),nextSuccAcc));
		SCC.getTop().setRem(SCC.getTop().getRem().or(r));
		return SCC.getTop().getAcc();
	}
}
