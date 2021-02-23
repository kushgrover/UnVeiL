package modules;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultEdge;
import com.infomatiq.jsi.Point;
import com.infomatiq.jsi.Rectangle;

import abstraction.ProductAutomaton;
import environment.Environment;
import environment.Vertex;
import gnu.trove.TIntProcedure;
import net.sf.javabdd.BDD;
import planningIO.StoreGraphKnown;
import planningIO.printing.ShowGraphKnown;
import settings.PlanningSettings;

public class KnownRRG extends RRG {
	
	
	
	public KnownRRG(Environment env) {
		super(env);
	}
	
	
	private Point2D sample() {
		Point2D p;
		while(true) {
			p = env.sample();
			if(env.obstacleFreeAll(p)) {
				totalSampledPoints++;
				return p;
			}
		}
	}

	@Override
	void buildGraph(ArrayList<BDD> advice, Point2D xRand2D) {
		Point xRand					= convertPoint2DToPoint(xRand2D);
		
		TIntProcedure procedure		= new TIntProcedure()	// execute this procedure for the nearest neighbour of 'xRand'
		{ 
			public boolean execute(int i) 
			{	
				Point xNearest		= treePoints.get(i);
				Point2D xNearest2D	= convertPointToPoint2D(xNearest);				
				Point2D xNew2D		= steer(xNearest2D, xRand2D);
				Point xNew			= convertPoint2DToPoint(xNew2D);
				
				BDD transition = ProductAutomaton.factory.zero();
				try {
					transition		= Environment.getLabelling().getLabel(xNearest2D);
					transition 		= transition.and(productAutomaton.changePreSystemVarsToPostSystemVars(Environment.getLabelling().getLabel(xNew2D)));
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				
				if(checkValidity(advice, xNearest2D, xNew2D, transition))
				{
					currentBatchSize++;
					updateRrgRadius(); 
					addSymbolicTransitions(xNearest2D, xNew2D);
					addGraphEdge(xNearest2D, xNew2D);
					
//					plotting the first time it sees a bin
					try {
						if(! flagBin && ! Environment.getLabelling().getLabel(xNew2D).and(ProductAutomaton.factory.ithVar(ProductAutomaton.varsBeforeSystemVars+7)).isZero()) {
							plotGraph(null, null);
							flagBin = true;
						}
						if(! flagRoom && ! Environment.getLabelling().getLabel(xNew2D).and(ProductAutomaton.factory.ithVar(ProductAutomaton.varsBeforeSystemVars+4)).isZero()) {
							plotGraph(null, null);
							flagRoom = true;
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				
					tree.nearestN(xNew, 
							new TIntProcedure() // For each neighbour of 'xNew' in the given radius, execute this method
							{
								public boolean execute(int i) 
								{
									Point neighbour			= treePoints.get(i);
									Point2D neighbour2D		= convertPointToPoint2D(neighbour);
									
									if(neighbour2D.equals(xNew2D)) return true;
									
									if(distance(xNew, neighbour) <= rrgRadius		&&		env.collisionFreeAll(xNew2D, neighbour2D)){
										addSymbolicTransitions(neighbour2D, xNew2D);
										addGraphEdge(neighbour2D, xNew2D);
									}
									return true;
								}
							}, 
							100, java.lang.Float.POSITIVE_INFINITY); // a max of 100 neighbours are considered
					
					// add point to the Rtree
					Rectangle rect = new Rectangle(xNew.x, xNew.y, xNew.x, xNew.y);
					tree.add(rect, totalPoints);
					treePoints.add(xNew);
					totalPoints++;
				}
		        return true;
		    }
		};
		tree.nearest(xRand, procedure, java.lang.Float.POSITIVE_INFINITY); // apply 'procedure' to the nearest point of xRand
	}

	@Override
	public BDD sampleBatch(ArrayList<BDD> advice) throws Exception {
		symbolicTransitionsInCurrentBatch = ProductAutomaton.factory.zero();
		Point2D p;
		currentBatchSize = 0;
		
		while(currentBatchSize < (int) PlanningSettings.get("batchSize")) {
			p = sample();
			buildGraph(advice, p);
		}
		return symbolicTransitionsInCurrentBatch;
	}

	public Pair<Float, Float> plotGraph(List<DefaultEdge> finalPath, Graph<Vertex, DefaultEdge> graphMovement) {
		if(finalPath != null) {
			new ShowGraphKnown(graph, graphMovement, env, movement, finalPath).setVisible(true);
			StoreGraphKnown temp = new StoreGraphKnown(env, graph, graphMovement, finalPath, null, "end");
			return new Pair<Float, Float>(temp.movementLength, temp.remainingPathLength);
		} else if(! flagBin) {
			StoreGraphKnown temp = new StoreGraphKnown(graph, graphMovement, movement, "bin");
			return new Pair<Float, Float>(temp.movementLength, temp.remainingPathLength);
		} else if(! flagRoom) {
			StoreGraphKnown temp = new StoreGraphKnown(graph, graphMovement, movement, "room");
			return new Pair<Float, Float>(temp.movementLength, temp.remainingPathLength);
		} else if(! flagFirstMove) {
			StoreGraphKnown temp = new StoreGraphKnown(graph, graphMovement, movement, "firstMove"); 
			return new Pair<Float, Float>(temp.movementLength, temp.remainingPathLength);
		}
		return new Pair<Float, Float>(0f,0f);
	}

}
