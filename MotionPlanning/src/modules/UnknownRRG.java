package modules;


import java.awt.geom.Point2D;
import java.io.IOException;
import gnu.trove.TIntProcedure;
import net.sf.javabdd.BDD;
import planningIO.StoreGraph;
import planningIO.printing.ShowGraph;
import settings.PlanningSettings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import com.infomatiq.jsi.Point;
import com.infomatiq.jsi.Rectangle;
import abstraction.ProductAutomaton;
import environment.EdgeSupplier;
import environment.Environment;
import environment.Vertex;
import environment.VertexSupplier;

public class UnknownRRG extends RRG
{
	public Discretization discretization;
	public ArrayList<Point2D> movement = new ArrayList<Point2D>();

	float sensingRadius;
	public int numOfFrontierUpdates = 0;
	public float moveTime = 0;
	Point2D currentRobotPosition;

	// used for exporting graph
	boolean flagBin = false;
	boolean flagRoom = false;
	boolean flagFirstMove = false;
	
	boolean explorationComplete = false;
	
	
	/**
	 * Initialise the RRG object with the constructor
	 * @param env
	 */
	public UnknownRRG(Environment env) 
	{
		this.env 				= env;
		this.maximumRRGStepSize = (float) PlanningSettings.get("eta");
		float[] sub 			= new float[] {env.getBoundsX()[1]-env.getBoundsX()[0], env.getBoundsY()[1]-env.getBoundsY()[0]};
		this.sensingRadius		= (float) PlanningSettings.get("sensingRadius");
		this.gamma 				= 2.0 * Math.pow(1.5,0.5) * Math.pow(sub[0]*sub[1]/Math.PI,0.5);
		this.graph 				= new SimpleGraph<Vertex, DefaultEdge>(new VertexSupplier(), new EdgeSupplier(), true);
		this.discretization 	= new Discretization(env, (float) PlanningSettings.get("discretizationSize"));
		this.forwardSampledTransitions = ProductAutomaton.factory.zero();
		this.tree.init(null);
	}
	
	
	/**
	 * Set initial point
	 * @param p2D
	 * @throws Exception 
	 */
	@Override
	public void setStartingPoint(Point2D p2D) throws Exception 
	{
		this.initVertex = new Vertex(p2D);
		Rectangle rect 	= new Rectangle((float) p2D.getX(), (float) p2D.getY(), (float) p2D.getX(), (float) p2D.getY());
		Point p 		= new Point((float) p2D.getX(), (float) p2D.getY());
		graph.addVertex(initVertex);
		treePoints.add(p);
		currentRobotPosition = p2D;
		movement.add(p2D);
		tree.add(rect, totalPoints);
		discretization.knowDiscretization(env, productAutomaton, p2D, sensingRadius);
		totalPoints++;
	}
	
	/**
	 * Does one iteration of the algo with the point xRand
	 * @param advice
	 * @param xRand2D
	 */
	void buildGraph(ArrayList<BDD> advice, Point2D xRand2D) {
		
		// need 'point2D' for graph and 'point' for Rtree
		Point xRand					= convertPoint2DToPoint(xRand2D);
		
		TIntProcedure procedure		= new TIntProcedure()	// execute this procedure for the nearest neighbour of 'xRand'
		{ 
			public boolean execute(int i) 
			{	
				Point xNearest		= treePoints.get(i);
				Point2D xNearest2D	= convertPointToPoint2D(xNearest);				
				Point2D xNew2D		= steer(xNearest2D, xRand2D);
				Point xNew			= convertPoint2DToPoint(xNew2D);
				
				BDD transition 		= ProductAutomaton.factory.zero();
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
//					try {
//						if(! flagBin && ! Environment.getLabelling().getLabel(xNew2D).and(ProductAutomaton.factory.ithVar(ProductAutomaton.varsBeforeSystemVars+7)).isZero()) {
//							plotGraph(null);
//							flagBin = true;
//						}
//						if(! flagRoom && ! Environment.getLabelling().getLabel(xNew2D).and(ProductAutomaton.factory.ithVar(ProductAutomaton.varsBeforeSystemVars+4)).isZero()) {
//							plotGraph(null);
//							flagRoom = true;
//						}
//					} catch (Exception e1) {
//						e1.printStackTrace();
//					}
					
					tree.nearestN(xNew, 
							new TIntProcedure() // For each neighbour of 'xNew' in the given radius, execute this method
							{
								public boolean execute(int i) 
								{
									Point neighbour			= treePoints.get(i);
									Point2D neighbour2D		= convertPointToPoint2D(neighbour);
									
									if(neighbour2D.equals(xNew2D)) return true;
//									if(! env.collisionFreeFromOpaqueObstacles(neighbour2D, currentRobotPosition)) return true;
									
									if(distance(xNew, neighbour) <= rrgRadius		&&		env.collisionFreeAll(xNew2D, neighbour2D)){
										addSymbolicTransitions(neighbour2D, xNew2D);
										addGraphEdge(neighbour2D, xNew2D);
									}
									return true;
								}
							}, 
							100, java.lang.Float.POSITIVE_INFINITY); // a max of 100 neighbours are considered
					
					// add point to the Rtree
					Rectangle rect 			= new Rectangle(xNew.x, xNew.y, xNew.x, xNew.y);
					tree.add(rect, totalPoints);
					treePoints.add(xNew);
					totalPoints++;
					
					float tempTime = System.nanoTime();
					if(!explorationComplete) {
						int rank = (advice == null)? -1: findRank(advice, xNearest2D, xNew2D, transition); // rank of transition according to advice
						try {
							tryMove(rank, xNew2D, transition, advice); // move if required
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					else if (currentBatchSize >= (int) PlanningSettings.get("batchSize")) {
						endBatch = true;
					}
					moveTime += System.nanoTime() - tempTime;
				}
		        return true;
		    }
		};
		tree.nearest(xRand, procedure, java.lang.Float.POSITIVE_INFINITY); // apply 'procedure' to the nearest point of xRand
	}

	/**
	 * sample one batch and add them in the graphs accordingly
	 * @param advice
	 * @return
	 * @throws Exception
	 */
	public BDD sampleBatch(ArrayList<BDD> advice) throws Exception {
		symbolicTransitionsInCurrentBatch = ProductAutomaton.factory.zero();
		currentBatchSize = 0;
		endBatch = false;
		Point2D p;
		
		while( !endBatch ) {
			p = sampleInExploredArea();
			buildGraph(advice, p);
		}
		return symbolicTransitionsInCurrentBatch;
	}

	/**
	 * Checks if the edge is collision-free, have been sampled before and selects the edge according to its prob
	 * @param advice
	 * @param xNearest2D
	 * @param xNew2D
	 * @param transition
	 * @return
	 */
	@Override 
	boolean checkValidity(ArrayList<BDD> advice, Point2D xNearest2D, Point2D xNew2D, BDD transition) {
		if(! env.collisionFreeAll(xNearest2D, xNew2D)) return false; // new edge is obstacle free
//		if(! env.collisionFreeFromOpaqueObstacles(xNew2D, currentRobotPosition)) return false; // new edge is visible
		if(! forwardSampledTransitions.and(transition).isZero()) return false; // transition already sampled
		if(advice != null) {
			int rank = findRank(advice, xNearest2D, xNew2D, transition);
			float prob = getProb(rank);
			float rand = (float) Math.random();
			if(rand < prob) return true;
			return false;
		} else {
			return true;
		}
	}
	
	private void move(Point2D xNew2D, int rank) throws Exception {
		if(rank != -1)
			adviceSampled[rank]++;
		currentRobotPosition = (Point2D.Float) xNew2D.clone();
		movement.add(currentRobotPosition);
		discretization.knowDiscretization(env, productAutomaton, currentRobotPosition, sensingRadius);
		endBatch = true;
	}

	
	boolean alreadyFound = false; // has to be a global var
	/**
	 * move to a new location if required
	 * @param rank
	 * @param xNew2D
	 * @param transition
	 * @param advice
	 * @throws Exception
	 */
	public void tryMove(int rank, Point2D xNew2D, BDD transition, ArrayList<BDD> advice) throws Exception {

		if(rank != -1 && productAutomaton.sampledTransitions.and(transition).isZero()) // if sampled from advice and have not sampled it before
			move(xNew2D, rank);
		else if (currentBatchSize >= (int) PlanningSettings.get("batchSize")) { // batch is finished
//			if(! flagFirstMove) { // plotting before the first time it moves
//				plotGraph(null);
//				flagFirstMove = true;
//			}
			
			Point2D p = discretization.findAMove(currentRobotPosition);
			if(p != null) {
				alreadyFound = false;
				tree.nearestN(convertPoint2DToPoint(p),
					new TIntProcedure() {
						public boolean execute(int i) {
							if(alreadyFound) {
								return false;
							}
							Point2D newPoint = convertPointToPoint2D(treePoints.get(i));
							if(env.collisionFreeFromOpaqueObstacles(p, newPoint)) {
								alreadyFound = true;
								try {
									move(newPoint, -1);
								}
								catch (Exception e) {
									e.printStackTrace();
								}
							}
							return true;
							
						}
					},
					50,
					java.lang.Float.POSITIVE_INFINITY);
				if(!alreadyFound) {
					tree.nearestN(convertPoint2DToPoint(p),
							new TIntProcedure() {
								public boolean execute(int i) {
									if(alreadyFound) {
										return false;
									}
									Point2D newPoint = convertPointToPoint2D(treePoints.get(i));
									if(env.collisionFreeFromOpaqueObstacles(p, newPoint)) {
										alreadyFound = true;
										try {
											move(newPoint, -1);
										}
										catch (Exception e) {
											e.printStackTrace();
										}
									}
									return true;
									
								}
							},
							100,
							java.lang.Float.POSITIVE_INFINITY);
				}
				
			} 
			else {
				endBatch = true;
				System.out.println("Exploration complete");
				explorationComplete = true;
			}
		} 
	}

	

	/**
	 * sample a point inside sensing area
	 * @return
	 * @throws Exception
	 */
	private Point2D sampleInExploredArea() throws Exception 
	{
		Point2D p;
		while(true) {
			p = env.sample();
			if(discretization.isExplored(p)) {
				if(env.obstacleFreeAll(p)) {
					if(env.collisionFreeFromOpaqueObstacles(currentRobotPosition, p)) {
						discretization.updateDiscretization(p, 1);
						numOfFrontierUpdates++;
					}
					totalSampledPoints++;
					return p;
				} else {
					discretization.updateDiscretization(p, 3);
					numOfFrontierUpdates++;
				}
			}
		}
	}
	
	/**
	 * find the path of the robot movement
	 * @param movement
	 * @return
	 */
	public List<DefaultEdge> findPath(ArrayList<Point2D> movement) {
		Vertex source, dest;
		
		Iterator<Point2D> it = movement.iterator();
		Point2D firstPoint, nextPoint;
		List<DefaultEdge> finalPath;
		
		if(it.hasNext()) {
			firstPoint	= it.next(); // first point is already there
		} else {
			return null;
		}
		if(it.hasNext()) {
			nextPoint	= it.next();
		} else {
			return null;
		}
		
		source = findTheVertex(firstPoint);
		dest = findTheVertex(nextPoint);
		GraphPath<Vertex, DefaultEdge> nextPath = DijkstraShortestPath.findPathBetween(graph, source, dest);
		finalPath 			= new ArrayList<DefaultEdge>();
		finalPath.addAll(nextPath.getEdgeList());
		
		
		// iterate over the abstract path
		while(it.hasNext())
		{
			source 			= dest;
			nextPoint 		= it.next();
			dest 			= findTheVertex(nextPoint);
			nextPath 		= DijkstraShortestPath.findPathBetween(graph, source, dest);
			if(nextPath != null) {
				finalPath.addAll(nextPath.getEdgeList());
			}
		}
		return finalPath;
	}   
	

	
	/**
	 * Plot the graph
	 * @return 
	 * @throws IOException 
	 */
	public float plotGraph(List<DefaultEdge> finalPath)  
	{
		if(finalPath != null) {
			new ShowGraph(graph, env, findPath(movement), finalPath).setVisible(true);
			StoreGraph temp = new StoreGraph(env, graph, finalPath, findPath(movement), "end");
			return temp.length;
		} else if(! flagFirstMove) {
			StoreGraph temp = new StoreGraph(graph, findPath(movement), "firstMove"); 
			return temp.length;
		} else if(! flagBin) {
			StoreGraph temp = new StoreGraph(graph, findPath(movement), "bin");
			return temp.length;
		} else if(! flagRoom) {
			StoreGraph temp = new StoreGraph(graph, findPath(movement), "room");
			return temp.length;
		}
		return 0;
	}
	
	
	

}
