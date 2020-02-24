package transitionSystem.TSparser;

//package jhoafparser.examples;

import java.io.InputStream;
import java.util.List;

import jhoafparser.ast.AtomLabel;
import jhoafparser.ast.BooleanExpression;
import jhoafparser.consumer.HOAConsumer;
import jhoafparser.consumer.HOAConsumerException;
import jhoafparser.consumer.HOAConsumerNull;
import jhoafparser.consumer.HOAConsumerStore;
import jhoafparser.consumer.HOAIntermediate;
import jhoafparser.parser.HOAFParser;
import jhoafparser.parser.generated.ParseException;
import jhoafparser.storage.StoredAutomaton;
import jhoafparser.storage.StoredEdgeWithLabel;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import net.sf.javabdd.BDDFactory;



public class BuchiAutomataParser{
	BDD buchiBDD;
	BDDFactory factory;
	int numberOfStates=0;
	int acceptingSets=0;
	int[] varArray=new int[3];
	int numberOfVarsUsed;
	BDDDomain[] bddDomain;

	public class addEdgeToBDD extends HOAConsumerStore{
		public addEdgeToBDD() {
			super();
		}
		
		@Override
		public void addEdgeWithLabel(int stateId, 
									BooleanExpression<AtomLabel> labelExpr, 
									java.util.List<java.lang.Integer> conjSuccessors, 
									java.util.List<java.lang.Integer> accSignature) {

			BDD temp=factory.one();
			
			for(Integer i : conjSuccessors) {
				temp=factory.one();
				temp.andWith(bddDomain[0].ithVar(stateId));
				temp.andWith(bddDomain[1].ithVar(i));
				
				try {
					temp.andWith(bddDomain[2].ithVar(accSignature.get(i)+1));
					System.out.println("added edge in buchi from state "+stateId+" ----"+labelExpr.toString()+"----> "+i+" {"+accSignature.get(i)+"}");
				}
				catch(NullPointerException E) {
					temp.andWith(bddDomain[2].ithVar(0));
					System.out.println("added edge in buchi from state "+stateId+" ----"+labelExpr.toString()+"----> "+i+" {}");
				}
//				temp.printDot();
				buchiBDD.orWith(temp);
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
	}
	
	public BuchiAutomataParser(BDDFactory factory, 
								InputStream inputStream1, 
								InputStream inputStream2) throws ParseException{
		
		buchiBDD=factory.zero();
		this.factory=factory;
		CountStates countStates=new CountStates();
		HOAFParser.parseHOA(inputStream1, countStates);
		initializeBDDDomain();
		addEdgeToBDD newConsumer=new addEdgeToBDD();
		HOAFParser.parseHOA(inputStream2, newConsumer);
//		buchiBDD.printDot();
	}

	private void initializeBDDDomain(){
		acceptingSets++;
//		numberOfVarsUsed=2*numberOfStates+acceptingSets+1;
		varArray=new int[] {numberOfStates, numberOfStates, acceptingSets+1};
		bddDomain=factory.extDomain(varArray);
		numberOfVarsUsed=2*bddDomain[0].varNum()+bddDomain[2].varNum();
	}
	
	public BDD getBuchiBDD() {
		return buchiBDD;
	}
	
	public int getNumberOfVars() {
		return numberOfVarsUsed;
	}
	
}
