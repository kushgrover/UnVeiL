/**
 * 
 */
package transitionSystem.emptinessCheck;

import java.util.ArrayList;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDD.BDDIterator;
import net.sf.javabdd.BDDFactory;
import transitionSystem.ProductAutomaton;
import transitionSystem.extraExceptions.StateException;

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
	
	public boolean findAcceptingPath() throws Exception{
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
						return true;
//						return findPath();
					}
				}
			}
		}
		return false;
//		return null;		
	}
	
	private ArrayList<BDD> findPath() throws Exception {
		BDD statesSCC=factory.zero();
		ArrayList<BDD> path=new ArrayList<BDD>();
		int topRank=SCC.getTop().getRoot();
		int[] visitedAcceptingSets=new int[totalAcceptingSets];
		for(int i=0;i<totalAcceptingSets;i++) {
			visitedAcceptingSets[i]=0;
		}
		for(int i=0;i<ProductAutomaton.getNumStates();i++){
			if(rank[i]>=topRank) {
				statesSCC=statesSCC.or(productAutomaton.getStateFromID(i));
			}
		}
		ArrayList<BDD> milestones=new ArrayList<BDD>();
		milestones.add(statesSCC.satOne(ProductAutomaton.allPreVars(),true));
		bfs(productAutomaton.getInitStates(),milestones.get(0),path);
		for(int i=0;i<totalAcceptingSets;i++) {
			milestones.add(bfs(milestones.get(i),visitedAcceptingSets,path));
		}
		bfs(milestones.get(totalAcceptingSets),milestones.get(0),path);
		return path;
	}

	private BDD bfs(BDD fromState, int[] visitedAcceptingSets, ArrayList<BDD> path) throws StateException, Exception {
		ArrayList<BDD> setStates=new ArrayList<BDD>();
		setStates.add(fromState);
		int i=0;
		BDD toStates=factory.zero();
		for(int j=0;j<totalAcceptingSets;j++) {
			if(visitedAcceptingSets[j]==0) {
				toStates=toStates.or(productAutomaton.getAcceptingStates(j+1));
			}
		}
		while(setStates.get(i).and(toStates).isZero()) {
			setStates.add(productAutomaton.postImage(setStates.get(i)));
			i++;
		}
		BDD toState=toStates.and(setStates.get(i)).satOne();
		setStates.add(i, toState);
		while(i>0) {
			setStates.add(i, setStates.get(i).and(productAutomaton.preImage(setStates.get(i+1))));
			i--;
		}
		BDD nextState;
		for(int j=0;j<setStates.size();j++) {
			nextState=setStates.get(j).satOne();
			setStates.add(j+1, productAutomaton.postImage(nextState));
			path.add(nextState);
		}
		return toState;
	}

	private void bfs(BDD fromState, BDD toState, ArrayList<BDD> path) throws StateException, Exception {
		ArrayList<BDD> setStates=new ArrayList<BDD>();
		setStates.add(fromState);
		int i=0;
		while(setStates.get(i).and(toState).isZero()) {
			setStates.add(productAutomaton.postImage(setStates.get(i)));
			i++;
		}
		setStates.add(i, toState);
		while(i>0) {
			setStates.add(i, setStates.get(i).and(productAutomaton.preImage(setStates.get(i+1))));
			i--;
		}
		BDD nextState;
		for(int j=0;j<setStates.size();j++) {
			nextState=setStates.get(j).satOne();
			setStates.add(j+1, productAutomaton.postImage(nextState));
			path.add(nextState);
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
		todo.push(state, state.and(productAutomaton.getBDD()));
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
