package transitionSystem;

import java.util.ArrayList;

import net.sf.javabdd.BDDFactory;

public class BuchiAutomaton extends TS
{

	public BuchiAutomaton(BDDFactory factory, 
							int cacheSize, 
							ArrayList<String> apList, 
							ArrayList<ArrayList<String>> labelling){

		super(factory, cacheSize, apList, labelling, 2);

	}

}
