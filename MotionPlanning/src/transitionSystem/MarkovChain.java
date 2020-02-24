package transitionSystem;

import java.util.ArrayList;
import java.util.List;


public class MarkovChain{
	private ArrayList<String> apList;
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
		apList=new ArrayList<String>();
		size=0;
		initial=-1;
		labelling=new ArrayList<ArrayList<String>>();
		edges=new ArrayList<ArrayList<Edge>>();
	}
	
	
	
	public MarkovChain(ArrayList<String> apList){
		this.apList=apList;
	}
	
	
	public ArrayList<String> getApList(){
		return apList;
	}
	
	public int getSize() {
		return size;
	}
	
	public int getInitial() {
		return initial;
	}
	
	public ArrayList<ArrayList<String>> getLabelling(){
		return labelling;
	}
	
	public List<ArrayList<Edge>> getEdges(){
		return edges;
	}
	
	public void setApList(ArrayList<String> apList){
		this.apList=apList;
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
			if(totalProb!=1.0) {
				throw new Exception("Error: MarkovChainNotComplete");
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
			apForStateArray[i]=apList.indexOf(currentAPList.get(i));
		}
		return apForStateArray;
	}

}
