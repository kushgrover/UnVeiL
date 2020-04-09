/**
 * 
 */
package transitionSystem.TSparser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.StringTokenizer;
import transitionSystem.MarkovChain;

/**
 * @author Kush Grover
 *
 */
public class MarkovChainParser
{
	
	public static MarkovChain markovChainParser() throws Exception{
	    
		MarkovChain mc=new MarkovChain();
		
		
		BufferedReader apFileReader = new BufferedReader(new FileReader("/home/kush/Projects/robotmotionplanning/MotionPlanning/Examples/Example 1/atomicPropositions.ap"));
        String line=apFileReader.readLine();
		
		while(line!=null) {
			mc.getApList().add(line);
			line=apFileReader.readLine();
		}
		apFileReader.close();
		
		
        BufferedReader statesFileReader = new BufferedReader(new FileReader("/home/kush/Projects/robotmotionplanning/MotionPlanning/Examples/Example 1/markovChainStates.st"));
        line = statesFileReader.readLine(); //read 1st line, number of states
        mc.setSize(Integer.parseInt(line));
       
        line=statesFileReader.readLine(); // read second line, start state
        mc.setInitial(Integer.parseInt(line));
        
        int i=0;
        String currentToken;
        try {
        	while(i<mc.getSize()) {
        		line=statesFileReader.readLine();
        		StringTokenizer tokens= new StringTokenizer(line," "); //tokenize a line
        		while(tokens.hasMoreTokens()) {
        			currentToken=tokens.nextToken();
        			mc.getLabelling().get(i).add(currentToken);
        		}
        		i++;
        	}
        }
        catch(Exception E) {
        	statesFileReader.close();
        	throw new Exception("Error in mc states file");
        }
        statesFileReader.close();
        
        
        
        BufferedReader transitionsFileReader = new BufferedReader(new FileReader("/home/kush/Projects/robotmotionplanning/MotionPlanning/Examples/Example 1/markovChainTransitions.tr"));
        line = transitionsFileReader.readLine();
        int numOfTransitions=Integer.parseInt(line);
        int firstState,secondState;
        float prob;
        try {
        	i=0;
        	while(i<numOfTransitions) {
        		line=transitionsFileReader.readLine();
        		StringTokenizer tokens=new StringTokenizer(line," ");
        		firstState=Integer.parseInt(tokens.nextToken());
        		secondState=Integer.parseInt(tokens.nextToken());
        		prob=Float.parseFloat(tokens.nextToken());
        		mc.addEdge(firstState, secondState, prob);
        		i++;
        	}
        } catch(Exception E) {
        	transitionsFileReader.close();
        	throw new Exception("Error in mc transitions file");
        }
        transitionsFileReader.close();
        
        mc.checkMarkovChain();
        
        return mc;
    }
}
