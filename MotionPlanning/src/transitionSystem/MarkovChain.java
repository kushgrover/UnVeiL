package transitionSystem;

import java.util.ArrayList;
import java.util.List;

import modules.DefaultExperiment;
import modules.Experiments;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDD.BDDIterator;


public class MarkovChain{
	private ArrayList<String> apListSystem;
	private int size;
	private int initial;
	private ArrayList<ArrayList<String>> labelling;
	private ArrayList<ArrayList<Edge>> edges;
	
	class Edge
	{
		private float prob;
		private int dest;

		public Edge(float prob, int dest)
		{
			this.prob = prob;
			this.dest = dest;
		}
	}
	
	
	public MarkovChain() {
		apListSystem=new ArrayList<String>();
		size=0;
		initial=-1;
		labelling=new ArrayList<ArrayList<String>>();
		edges=new ArrayList<ArrayList<Edge>>();
	}
	
	public MarkovChain(ArrayList<String> apListSystem){
		this.apListSystem=apListSystem;
	}
	
	
	public ArrayList<String> getApList(){
		return apListSystem;
	}
	
	public int getSize() {
		return size;
	}
	
	public int getInitialState() {
		return initial;
	}
	
	public ArrayList<ArrayList<String>> getLabelling(){
		return labelling;
	}
	
	public List<ArrayList<Edge>> getEdges(){
		return edges;
	}
	
	public void setApList(ArrayList<String> apListSystem){
		this.apListSystem=apListSystem;
	}
	
	public void setSize(int size) {
		this.size=size;
		this.labelling=new ArrayList<ArrayList<String>>(size);
		this.edges=new ArrayList<ArrayList<Edge>>(size);
		for(int i=0;i<size;i++) {
			labelling.add(new ArrayList<String>());
			edges.add(new ArrayList<Edge>());
		}
	}
	
	public void setInitial(int initial) {
		this.initial=initial;
	}
	
	public void setLabelling(ArrayList<ArrayList<String>> labelling){
		this.labelling=labelling;
	}
	
	public void setEdges(ArrayList<ArrayList<Edge>> edges){
		this.edges=edges;
	}
	
	public void addEdge(int first, int second, float prob) {
		Edge newEdge= new Edge(prob, second);
		edges.get(first).add(newEdge);
	}
	
	public void checkMarkovChain() throws Exception{
		float totalProb;
		for(int i=0;i<size;i++) {
			totalProb=0;
			ArrayList<Edge> currentEdges=edges.get(i);
			for(int j=0;j<currentEdges.size();j++) {
				totalProb+=currentEdges.get(j).prob;
			}
			if(totalProb-1.0>0.00001 || 1-totalProb>0.00001) {
				System.out.println(totalProb);
				throw new Exception("Error: MarkovChainNotComplete: state "+i);
			}
		}
	}
	
	public int sampleFromState(int state) {
		double x=Math.random();
		ArrayList<Edge> currentEdges=edges.get(state);
		int i=0;
		while(i<currentEdges.size()) {
			x-=currentEdges.get(i).prob;
			if(x<0) {
				return currentEdges.get(i).dest;
			}
			i++;
		}
		return -1;
	}



	public int[] findAPForState(int state){
		ArrayList<String> currentAPList=new ArrayList<String>();
		for(String i: labelling.get(state)) {
			currentAPList.add(i);
		}
		currentAPList.remove(0);
		int[] apForStateArray=new int[currentAPList.size()];
		for(int i=0;i<currentAPList.size();i++) {
			apForStateArray[i]=apListSystem.indexOf(currentAPList.get(i));
		}
		return apForStateArray;
	}

	public BDD getBDDForState(int state) {
		int[] toStateAP=findAPForState(state);
		BDD toStateBDD=ProductAutomaton.factory.one();
        for(int i=0;i<apListSystem.size();i++) {
        	if(arraySearch(toStateAP,i)) {
        		toStateBDD.andWith(ProductAutomaton.factory.ithVar(i+ProductAutomaton.varsBeforeSystemVars));
        	}
        	else {
        		toStateBDD.andWith(ProductAutomaton.factory.ithVar(i+ProductAutomaton.varsBeforeSystemVars).not());
        	}
        }
        return toStateBDD;
	}



	private boolean arraySearch(int[] array, int i)
	{
		for(int j=0;j<array.length;j++) {
			if(array[j]==i) {
				return true;
			}
		}
		return false;
	}



	
//	
	public ArrayList<String> findAPForState(BDD state, Experiments exper) throws Exception {
		ArrayList<String> apListState=new ArrayList<String>();
		for(int i=0;i<ProductAutomaton.numAPSystem;i++) {
			if(! ProductAutomaton.ithVarSystemPre(i).and(state).isZero()) {
				apListState.add(apListSystem.get(i));
			}
		}
		return apListState;
	}
	
	public int getStateNumber(ArrayList<String> apListState) {
		ArrayList<String> temp;
		int num;
		for(int i=0;i<size;i++) {
			temp=labelling.get(i);
			if(temp.size()!=apListState.size()+1) {
				continue;
			}
			num=0;
			for(int j=1;j<temp.size();j++) {
				for(int k=0;k<apListState.size();k++) {
					if(temp.get(j).equals(apListState.get(k))) {
						num++;
					}
				}
			}
			if(num==apListState.size()) {
				return i;
			}
		}
		return -1;
	}


	
	public BDD sample(BDD fromStates, BDD toStates, Experiments exper) throws Exception{
		ProductAutomaton productAutomaton=exper.getProductAutomaton();
		BDD fromStatesCopy=fromStates.id();
		BDDIterator iterator=fromStatesCopy.iterator(ProductAutomaton.allPreSystemVars());
		BDD fromState=null, toState=null, transition=null;
		int thresholdSampling=20;
		int fromStateNum, toStateNum;
		while(iterator.hasNext()) {
			fromState=(BDD) iterator.next();
			ArrayList<String> listOfAP=findAPForState(fromState, exper);
			fromStateNum=getStateNumber(listOfAP);
			if(fromStateNum!=-1) {
				for(int i=0;i<thresholdSampling;i++) {
					toStateNum=sampleFromState(fromStateNum);
					toState=getBDDForState(toStateNum);
					if(toState.and(toStates).isZero()) {
						continue;
					}
					transition=fromState.and(productAutomaton.changePreSystemVarsToPostSystemVars(toState));
					if(transition.and(productAutomaton.sampledTransitions).isZero()) {
						System.out.println("Sampled helpful transition: "+labelling.get(fromStateNum)+"---->"+labelling.get(toStateNum));
						return transition;
					}
				}
			}
		}
		return null;
	}

	public BDD sample(BDD fromStates, DefaultExperiment exper) throws Exception{
		BDD fromStatesCopy=fromStates.id();
		int thresholdSampling=20;
		BDDIterator iterator =fromStatesCopy.iterator(ProductAutomaton.allPreSystemVars());
		while(iterator.hasNext()) {
			BDD fromState=(BDD) iterator.next();
			ArrayList<String> listOfAP=findAPForState(fromState, exper);
			int fromStateNum=getStateNumber(listOfAP);
			if(fromStateNum!=-1) {
				int toStateNum=-1;
				BDD toState=null,transition=null;
				int k=0;
				while(k<=thresholdSampling) {
					toStateNum=sampleFromState(fromStateNum);
					toState=getBDDForState(toStateNum);
					transition=fromState.and(exper.getProductAutomaton().changePreSystemVarsToPostSystemVars(toState));
					if(transition.and(exper.getProductAutomaton().sampledTransitions).isZero()) {
						System.out.println("Sampled transition: "+labelling.get(fromStateNum)+"---->"+labelling.get(toStateNum));
						return transition;
					}
					k++;
				}
			}
		}
		return null;
	}
}
