import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BuDDyFactory;

import jhoafparser.consumer.HOAConsumer;
import jhoafparser.parser.HOAFParser;
import jhoafparser.consumer.HOAIntermediate;

import transitionSystem.TS;
import transitionSystem.MarkovChain;
import transitionSystem.MultiModalTS;
import transitionSystem.TSparser.MarkovChainParser;
import transitionSystem.TSparser.BuchiAutomataParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;

import java.util.ArrayList;

/**
 * @author kush
 *
 */
public class Planning{

	public static void main(String[] args) throws Exception{
		
		boolean verbose=true;
		int cacheSizeForBDDFactory=1000;
		int levelOfTransitions=2;
		
		MarkovChain mc=MarkovChainParser.markovChainParser();
		mc.checkMarkovChain();
		ArrayList<String> apList=mc.getApList();

		BDDFactory factory= BuDDyFactory.init(20,1000);
		
		
		

		
		
		
		BufferedReader propertyReader = new BufferedReader(new FileReader("/home/kush/Projects/robotmotionplanning/MotionPlanning/property.pr"));
        String propertyString=propertyReader.readLine();
        propertyReader.close();
        System.out.println("\nProperty to satisfty: "+propertyString);
        
//		String command="/home/kush/Projects/robotmotionplanning/spot-2.8.5/bin/ltl2tgba";
        String command="/home/kush/Projects/robotmotionplanning/owl/build/install/owl/bin/ltl2ldba";
		
		
//        ProcessBuilder builder = new ProcessBuilder(command,"--deterministic",propertyString);
        ProcessBuilder builder1 = new ProcessBuilder(command,propertyString);
        builder1.redirectErrorStream(true);
        Process p1 = builder1.start();
        
        ProcessBuilder builder2 = new ProcessBuilder(command,propertyString);
        builder2.redirectErrorStream(true);
        Process p2 = builder2.start();
        
        
        BuchiAutomataParser buchiParser=new BuchiAutomataParser(factory,p1.getInputStream(),p2.getInputStream()); 
        BDD buchiBDD=buchiParser.getBuchiBDD();
            
//        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
//        String line;
//        BufferedWriter writer = new BufferedWriter(new FileWriter("/home/kush/Projects/robotmotionplanning/MotionPlanning/buchiAutomata.hoa"));
//        while (true) {
//            line = r.readLine();
//            if (line == null) { break; }
//            System.out.println(line);
//            writer.write(line+"\n");
//        } 
//        writer.close();
        
        
        
        

        
        
        
        
        
        TS learnedAutomata=new MultiModalTS(factory,
				cacheSizeForBDDFactory,
				apList,
				mc.getLabelling(),
				levelOfTransitions);


        int currentState=mc.getInitial();
        int i=0,nextState,currentStateArray[],nextStateArray[];  	//currentStateArray stores the list of atomic propositions true in that state
        currentStateArray=mc.findAPForState(currentState);

        while(i<10) {

        	nextState=mc.sampleFromState(currentState);
        	nextStateArray=mc.findAPForState(nextState);

        	if(verbose) {
        		System.out.print("Adding transition to learned automata: ");
        		System.out.print(mc.getLabelling().get(currentState));
        		System.out.print(" ---> ");
        		System.out.print(mc.getLabelling().get(nextState));
        		System.out.print("\n");
        	}

        	learnedAutomata.addTransitionFromListOfAP(currentStateArray,nextStateArray, buchiParser.getNumberOfVars());

        	i++;
        	currentState=nextState;
        	currentStateArray=nextStateArray;
        }

//	learnedAutomata.getBDD().printDot();


        
        
        
        
        BDD productAutomata=buchiBDD.and(learnedAutomata.getBDD());
//        learnedAutomata.getBDD().printDot();
//        buchiBDD.printDot();
        productAutomata.printDot();
        
		factory.done();
	}

}
