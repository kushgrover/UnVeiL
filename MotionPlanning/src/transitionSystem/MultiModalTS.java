package transitionSystem;

import java.util.ArrayList;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

import transitionSystem.TS;

public class MultiModalTS extends TS
{
	ArrayList<String> apList;
	int size;
	BDD bdd;
//	public MultiModalTS(ArrayList<String> apList, int cacheSize, int level, BDDFactory factory){
//		super(apList, cacheSize, level, factory);	
//	}
	public MultiModalTS(BDDFactory factory, 
						int cacheSize, 
						ArrayList<String> apList, 
						ArrayList<ArrayList<String>> labelling, 
						int levelOfTransitions){
		
		super(factory, cacheSize, apList, labelling, levelOfTransitions);
	}
}
