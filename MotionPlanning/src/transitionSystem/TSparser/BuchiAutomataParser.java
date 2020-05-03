package transitionSystem.TSparser;

//package jhoafparser.examples;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import jhoafparser.ast.AtomLabel;
import jhoafparser.ast.BooleanExpression;
import jhoafparser.consumer.HOAConsumerException;
import jhoafparser.consumer.HOAConsumerStore;
import jhoafparser.parser.HOAFParser;
import jhoafparser.parser.generated.ParseException;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import net.sf.javabdd.BDDFactory;



public class BuchiAutomataParser{
	BDD buchiBDD, initStatesProperty;
	BDDFactory factory;
	int numberOfStates=0;
	int acceptingSets=0;
	int numVars[]=new int[4];
	BDDDomain[] bddDomain;
	ArrayList<String> apListSystem, apListProperty;
	int levelOfTransitions;

	public class addEdgeToBDD extends HOAConsumerStore{
		public addEdgeToBDD() {
			super();
		}
		
		@Override
		public void addEdgeWithLabel(int stateId, 
									BooleanExpression<AtomLabel> labelExpr, 
									java.util.List<java.lang.Integer> conjSuccessors, 
									java.util.List<java.lang.Integer> accSignature) {

			BDD newEdge=factory.one();
			for(Integer i : conjSuccessors) {
				newEdge=factory.one();
				newEdge.andWith(bddDomain[0].ithVar(stateId));
				newEdge.andWith(bddDomain[1].ithVar(i));
				
				try {
					newEdge.andWith(bddDomain[2].ithVar(accSignature.get(0)+1));
//					System.out.println("Added edge in buchi from state "+stateId+" ----"+labelExpr.toString()+"----> "+i+" {"+accSignature.get(0)+"}");
				}
				catch(NullPointerException E) {
					newEdge.andWith(bddDomain[2].ithVar(0));
//					System.out.println("Added edge in buchi from state "+stateId+" ----"+labelExpr.toString()+"----> "+i+" {}");
				}
				newEdge.andWith(getBDDFromLabel(labelExpr));
				
				buchiBDD.orWith(newEdge);
			}
			
		}
		
		@Override
		public void addStartStates(List<Integer> stateConjunction) throws HOAConsumerException
		{
			for(int i: stateConjunction) {
				initStatesProperty=initStatesProperty.or(bddDomain[0].ithVar(i));
			}
		}
		
	}
	public class CountStates extends HOAConsumerStore{
		public CountStates() {
			super();
		}
		
		@Override
		public void addState(int id, 
								String info, 
								BooleanExpression<AtomLabel> labelExpr, 
								List<Integer> accSignature) {
			numberOfStates++;
		}
		
		@Override
		public void addEdgeWithLabel(int stateId, 
									BooleanExpression<AtomLabel> labelExpr, 
									java.util.List<java.lang.Integer> conjSuccessors, 
									java.util.List<java.lang.Integer> accSignature) {
			try{
				for(Integer i: accSignature) {
			
					if(i>acceptingSets) {
						acceptingSets=i;
					}
				}
			}
			catch(NullPointerException E) {
				
			}
		}
		
		public void getAPListProperty() {
			apListProperty=(ArrayList<String>) this.getStoredAutomaton().getStoredHeader().getAPs();
		}
	}
	
	public BuchiAutomataParser(BDDFactory factory, 
								InputStream inputStream1, 
								InputStream inputStream2, 
								ArrayList<String> apListSystem,
								int levelOfTransitions) throws ParseException{
		this.apListSystem=apListSystem;
		buchiBDD=factory.zero();
		initStatesProperty=factory.zero();
		this.factory=factory;
		this.levelOfTransitions=levelOfTransitions;
		CountStates countStates=new CountStates();
		HOAFParser.parseHOA(inputStream1, countStates);
		countStates.getAPListProperty();
		initializeBDDVariables();
		addEdgeToBDD newConsumer=new addEdgeToBDD();
		HOAFParser.parseHOA(inputStream2, newConsumer);
	}

	private void initializeBDDVariables(){
		acceptingSets++;
		bddDomain=factory.extDomain(new int[] {numberOfStates, 
				numberOfStates, 
				acceptingSets+1, 
				levelOfTransitions});
		factory.extVarNum(2*apListSystem.size()+apListProperty.size());
		numVars[0]=2*bddDomain[0].varNum()+bddDomain[2].varNum();
		numVars[1]=bddDomain[3].varNum();
		numVars[2]=apListProperty.size();
		numVars[3]=2*apListSystem.size();
	}
	
	public BDD getPropertyBDD() {
		return buchiBDD;
	}
	
	public int[] getNumVars() {
		return numVars;
	}
	
	public ArrayList<String> getAPListProperty(){
		return apListProperty;
	}
	
	public ArrayList<String> getAPListSystem(){
		return apListSystem;
	}
	
	public BDD getBDDFromLabel(BooleanExpression<AtomLabel> labelExpr) {
		BDD label=factory.zero();

		if(labelExpr.isAtom()) {
			label=getIthVarLabel(labelExpr.getAtom().getAPIndex());
		}
		else if(labelExpr.isNOT()) {
			label=getIthVarLabel(labelExpr.getLeft().getAtom().getAPIndex()).not();
		}
		else if(labelExpr.isAND()){
			label=getBDDFromLabel(labelExpr.getLeft()).andWith(getBDDFromLabel(labelExpr.getRight()));
		}
		else if(labelExpr.isOR()) {
			label=getBDDFromLabel(labelExpr.getLeft()).orWith(getBDDFromLabel(labelExpr.getRight()));
		}
		return label;
	}
	
	private BDD getIthVarLabel(int i) {
		return factory.ithVar(i+numVars[0]+numVars[1]);
	}

	public BDD getInitStateProperty() {
		return initStatesProperty;
	}

}
