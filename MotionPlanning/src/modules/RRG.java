package modules;


import java.awt.geom.Point2D;
import java.io.IOException;
import gnu.trove.TIntProcedure;
import net.sf.javabdd.BDD;
import planningIO.StoreGraph;
import planningIO.printing.ShowGraph;
import settings.PlanningException;
import settings.PlanningSettings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import com.infomatiq.jsi.Point;
import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.SpatialIndex;
import com.infomatiq.jsi.rtree.RTree;

import abstraction.ProductAutomaton;
import environment.Environment;
import environment.Vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RRG 
{
	@SuppressWarnings("unused")
	private final Logger log 	= LoggerFactory.getLogger(RRG.class);
	float eta; // maximum step size
	Environment env; 
	double gamma;
	SpatialIndex tree;
	Graph<Vertex, DefaultEdge> graph;
	ArrayList<Point> treePoints; // list of all the points in the Rtree/graph
	int numPoints; // num of points in the Rtree/graph
	Vertex initVertex;  //initial vertex in the graph
	public int totalSampledPoints;
//	ProductAutomaton productAutomaton;
	
	Point2D currentRobotPosition;
	float sensingRadius;
	public int countSinceLastMove;
	int movingThreshold;
	public ArrayList<Point2D> movement = new ArrayList<Point2D>();
	
	ArrayList<Pair<BDD, Vertex>> map;
	
	boolean flagBin = false;
	boolean flagRoom = false;
	boolean flagFirstMove = false;
	ArrayList<Point2D> neighbours;
	public Discretization discretization;
	
	public float moveTime = 0;
	
	public int numOfFrontierUpdates=0;
	boolean endIteration;
	public int[] adviceSampled = new int[] {0,0,0,0,0,0,0,0,0,0};
	Vertex source = null;
	
	ProductAutomaton productAutomaton;
	BDD symbolicTransitionsInCurrentBatch;
	float rrgRadius;
	public int totalPoints = 0;
	boolean seeBeyondObstacles = true;
	BDD forwardSampledTransitions;
	
	
	/**
	 * Initialise the RRG object with the constructor
	 * @param env
	 */
	public RRG(Environment env) 
	{
		this.env 				= env;
		
		
		this.eta 				= (float) PlanningSettings.get("planning.eta");
		float[] sub 			= new float[] {env.getBoundsX()[1]-env.getBoundsX()[0], env.getBoundsY()[1]-env.getBoundsY()[0]};
		this.sensingRadius		= (float) PlanningSettings.get("planning.sensingRadius");
		this.gamma 				= 2.0 * Math.pow(1.5,0.5) * Math.pow(sub[0]*sub[1]/Math.PI,0.5);
		this.graph 				= new SimpleGraph<Vertex, DefaultEdge>(DefaultEdge.class);
		this.movingThreshold	= (int) PlanningSettings.get("planning.movingThreshold");
		this.discretization 	= new Discretization(env, (float) PlanningSettings.get("planning.discretizationSize"));
		treePoints 				= new ArrayList<Point>();
		map						= new ArrayList<Pair<BDD, Vertex>>();
		this.countSinceLastMove = 0;
		forwardSampledTransitions = ProductAutomaton.factory.zero();
		numPoints				= 0;
		
		totalSampledPoints 		= 0;
		
		this.tree 				= new RTree();
		tree.init(null);
	}
	
	public void setProductAutomaton(ProductAutomaton productAutomaton) {
		this.productAutomaton = productAutomaton;
	}
	
	/**
	 * 
	 * @return graph
	 */
	public Graph<Vertex, DefaultEdge> getGraph()
	{
		return graph;
	}
	
	/**
	 * After sampling random point 'xRand', check if it is okay to add it and do what is required
	 * @param fromStates
	 * @param toStates
	 * @param xRand2D
	 * @param productAutomaton
	 * @return
	 */
	public BDD buildGraph(BDD fromStates, BDD toStates, Point2D xRand2D, ProductAutomaton productAutomaton, int type) 
	{
		
		BDD transitions 			= ProductAutomaton.factory.zero();
		
		// need 'point2D' for graph and 'point' for Rtree
		Point xRand					= convertPoint2DToPoint(xRand2D);
		
		TIntProcedure procedure		= new TIntProcedure()	// execute this procedure for the nearest neighbour of 'xRand'
		{ 
			public boolean execute(int i) 
			{	
				Point xNearest		= treePoints.get(i);
				Point2D xNearest2D	= convertPointToPoint2D(xNearest);
				
				// steer in the direction of random point, this new point will be added to the graph/Rtree
				Point2D xNew2D		= steer(xNearest2D, xRand2D);
				Point xNew			= convertPoint2DToPoint(xNew2D);
				
				try 
				{
					// check if 'xNearest' is in the set 'fromStates' 					        || check if the new point is in the set 'toStates'
					if(Environment.getLabelling().getLabel(xNearest2D).and(fromStates).isZero() || Environment.getLabelling().getLabel(xNew2D).and(toStates).isZero())	
					{
						return false;
					}
				} catch (Exception e) 
				{
					e.printStackTrace();
				}
				
				
				// If the points are according to the advice
				if(env.collisionFree(xNearest2D, xNew2D))	//check if it is collision free
				{	
//					discretization.updatediscretization(xNew2D, 1);
//					numOfFrontierUpdates++;

					BDD transition = ProductAutomaton.factory.zero();
					try 
					{
						transition		= productAutomaton.changePreSystemVarsToPostSystemVars(Environment.getLabelling().getLabel(xNew2D));
						transition 		= transition.and(Environment.getLabelling().getLabel(xNearest2D));
						if(type == 1 && transition.and(productAutomaton.getBDD().and(ProductAutomaton.transitionLevelDomain().ithVar(2))).and(ProductAutomaton.getPropertyBDD()).and(ProductAutomaton.getLabelEquivalence()).isZero()) {
							return false;
						}
						symbolicTransitionsInCurrentBatch = symbolicTransitionsInCurrentBatch.or(transition);
					}
					catch (Exception e1) {
						e1.printStackTrace();
					}
					
					
//					System.out.println("Sampled Transition: " + xNearest2D.toString() + " ---> " + xNew2D.toString());
//					try {
//						if(! Environment.getLabelling().getLabel(xNearest2D).equals(Environment.getLabelling().getLabel(xNew2D))) {
//							System.out.println(Environment.getLabelling().getLabel(xNearest2D).toString() + " ---> " + Environment.getLabelling().getLabel(xNew2D).toString());
//						}
//					} catch (Exception e1) {
//						e1.printStackTrace();
//					}
					
					
					
					//add the new point to the graph (it will be added later to the Rtree)
					computeRrgRadius();
//					addSymbolicTransitions(xNearest2D, xNew2D);
					addGraphEdge(xNearest2D, xNew2D);
					
//					plotting the first time it sees a bin
					try {
						if(! flagBin && ! Environment.getLabelling().getLabel(xNew2D).and(ProductAutomaton.factory.ithVar(ProductAutomaton.varsBeforeSystemVars+7)).isZero()) {
							plotGraph(null);
							flagBin = true;
						}
						if(! flagRoom && ! Environment.getLabelling().getLabel(xNew2D).and(ProductAutomaton.factory.ithVar(ProductAutomaton.varsBeforeSystemVars+4)).isZero()) {
							plotGraph(null);
							flagRoom = true;
						}
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					// add the vertex in the map from abstraction to concrete
					addToMap(source);
					
					
					
					discretization.updateDiscretization(xNearest2D, xNew2D, 2);
					numOfFrontierUpdates++;
					
					float tempTime = System.nanoTime();
					
					if(type == 1) {
						move(1, xNew2D, transition);
					} else {
						move(-1, xNew2D, transition);
					}
						
					moveTime += System.nanoTime() - tempTime;
					
					
					
					tree.nearestN(xNew, 
							new TIntProcedure() // For each neighbour of 'xNew' in the given radius, execute this method
							{
								public boolean execute(int i) 
								{
									Point neighbour			= treePoints.get(i);
									Point2D neighbour2D		= convertPointToPoint2D(neighbour);
									
									if(neighbour2D.equals(xNew2D)) return true;
									
									if( distance(xNew, neighbour) <= rrgRadius		&&		env.collisionFree(xNew2D, neighbour2D) ) 
									{
										addSymbolicTransitions(neighbour2D, xNew2D);
										addGraphEdge(neighbour2D, xNew2D);
									}
									return true;
								}
							}, 
							100, java.lang.Float.POSITIVE_INFINITY); // a max of 100 neighbours are considered
					
					// add point to the Rtree
					Rectangle rect 			= new Rectangle(xNew.x, xNew.y, xNew.x, xNew.y);
//					System.setOut(new PrintStream(OutputStream.nullOutputStream()));
					tree.add(rect, numPoints);
//					System.setOut(System.out);
					treePoints.add(xNew);
					numPoints++;
				}
		        return true;
		    }
		};
		tree.nearest(xRand, procedure, java.lang.Float.POSITIVE_INFINITY); // apply 'procedure' to the nearest point of xRand
		
		return transitions;
	}
	
	
	private void buildGraph(ArrayList<BDD> advice, Point2D xRand2D, ProductAutomaton productAutomaton) {
		
		// need 'point2D' for graph and 'point' for Rtree
		Point xRand					= convertPoint2DToPoint(xRand2D);
		
		TIntProcedure procedure		= new TIntProcedure()	// execute this procedure for the nearest neighbour of 'xRand'
		{ 
			public boolean execute(int i) 
			{	
				Point xNearest		= treePoints.get(i);
				Point2D xNearest2D	= convertPointToPoint2D(xNearest);
				
				// steer in the direction of random point, this new point will be added to the graph/Rtree
				Point2D xNew2D		= steer(xNearest2D, xRand2D);
				Point xNew			= convertPoint2DToPoint(xNew2D);
				
				BDD transition = ProductAutomaton.factory.zero();
				try {
					transition		= Environment.getLabelling().getLabel(xNearest2D);
					transition 		= transition.and(productAutomaton.changePreSystemVarsToPostSystemVars(Environment.getLabelling().getLabel(xNew2D)));
				} catch (Exception e) {
					e.printStackTrace();
				}
				
//				int rank1 = findRank(advice, xNearest2D, xNew2D, productAutomaton);
//				if(rank1>=0) {
//					System.out.println(rank1);
//					System.out.println("Sampled Transition: " + xNearest2D.toString() + " ---> " + xNew2D.toString());
//					try {
////						if(! Environment.getLabelling().getLabel(xNearest2D).equals(Environment.getLabelling().getLabel(xNew2D))) {
//						printAPList(Environment.getLabelling().getLabel(xNearest2D)); 
//						System.out.print(" ---> ");
//						printAPList(Environment.getLabelling().getLabel(xNew2D));
//						System.out.print("\n");
////						}
//					} catch (Exception e1) {
//						e1.printStackTrace();
//					}
//				}
				
				
				
				if(checkValidity(advice, xNearest2D, xNew2D, transition, productAutomaton))
				{
					computeRrgRadius();
					addSymbolicTransitions(xNearest2D, xNew2D);
					addGraphEdge(xNearest2D, xNew2D);
					totalPoints++;
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
					
					discretization.updateDiscretization(xNearest2D, xNew2D, 2);
					numOfFrontierUpdates++;
					
					
					float tempTime = System.nanoTime();
					int rank = findRank(advice, xNearest2D, xNew2D, transition, productAutomaton);
					move(rank, xNew2D, transition);
					moveTime += System.nanoTime() - tempTime;
					
					
					tree.nearestN(xNew, 
							new TIntProcedure() // For each neighbour of 'xNew' in the given radius, execute this method
							{
								public boolean execute(int i) 
								{
									Point neighbour			= treePoints.get(i);
									Point2D neighbour2D		= convertPointToPoint2D(neighbour);
									
									if(neighbour2D.equals(xNew2D)) return true;
									
									if(! seeBeyondObstacles  &&  ! env.collisionFree(neighbour2D, currentRobotPosition) )
									{
										return true;
									}
									if(distance(xNew, neighbour) <= rrgRadius		&&		env.collisionFree(xNew2D, neighbour2D))
									{
										addSymbolicTransitions(neighbour2D, xNew2D);
										addGraphEdge(neighbour2D, xNew2D);
									}
									return true;
								}
							}, 
							100, java.lang.Float.POSITIVE_INFINITY); // a max of 100 neighbours are considered
					
					// add point to the Rtree
					Rectangle rect 			= new Rectangle(xNew.x, xNew.y, xNew.x, xNew.y);
//					System.setOut(new PrintStream(OutputStream.nullOutputStream()));
					tree.add(rect, numPoints);
//					System.setOut(System.out);
					treePoints.add(xNew);
					numPoints++;
				}
		        return true;
		    }
					};
		tree.nearest(xRand, procedure, java.lang.Float.POSITIVE_INFINITY); // apply 'procedure' to the nearest point of xRand
	}
	
	
	public BDD sample(ArrayList<BDD> advice, ProductAutomaton productAutomaton) 
	{
		symbolicTransitionsInCurrentBatch = ProductAutomaton.factory.zero();
		endIteration = false;
		
		Point2D.Float p;
		int threshold = (int) PlanningSettings.get("planning.samplingThreshold");
		for(int i=0; i<threshold; i++) {
			if(endIteration) {
				break;
			}
			p = sampleInSensingArea();
			buildGraph(advice, p, productAutomaton);
		}
		return symbolicTransitionsInCurrentBatch;
	}

	
	protected void addGraphEdge(Point2D xNearest2D, Point2D xNew2D) {
		source		= findTheVertex(xNearest2D);
		Vertex target 				= null;
		target				= findTheVertex(xNew2D);
		if(target == null) {
			target = new Vertex(xNew2D);
			graph.addVertex(target);
		}
		graph.addEdge(source, target);
		addToMap(target);
	}

	protected void computeRrgRadius() {
		if(numPoints > 1)
		{
			rrgRadius				= (float) Math.min(gamma * Math.pow(Math.log(numPoints)/(numPoints), (0.5)), eta);
		} else 
		{
			rrgRadius				= eta;
		}
	}

	protected void addSymbolicTransitions(Point2D xNearest2D, Point2D xNew2D) {
		try 
		{
			BDD transition	= (Environment.getLabelling().getLabel(xNearest2D));
			transition 		= transition.and(productAutomaton.changePreSystemVarsToPostSystemVars(Environment.getLabelling().getLabel(xNew2D)));
			if(! Environment.getLabelling().getLabel(xNearest2D).equals(Environment.getLabelling().getLabel(xNew2D))) {
				forwardSampledTransitions = forwardSampledTransitions.or(transition);
			}
			symbolicTransitionsInCurrentBatch.orWith(transition);
			
			transition		= Environment.getLabelling().getLabel(xNew2D);
			transition 		= transition.and(productAutomaton.changePreSystemVarsToPostSystemVars(Environment.getLabelling().getLabel(xNearest2D)));
			symbolicTransitionsInCurrentBatch.orWith(transition);
		}
		catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private boolean checkValidity(ArrayList<BDD> advice, Point2D xNearest2D, Point2D xNew2D, BDD transition, ProductAutomaton productAutomaton) {
		if(! env.collisionFree(xNearest2D, xNew2D)) {
			return false;
		}
		if(! seeBeyondObstacles && ! env.collisionFree(xNew2D, currentRobotPosition)) {
			return false;
		}
		if(! forwardSampledTransitions.and(transition).isZero()) {
			return false;
		}
		int rank = findRank(advice, xNearest2D, xNew2D, transition, productAutomaton);
		float prob = getProb(rank);
		float rand = (float) Math.random();
		if(rand < prob) {
			return true;
		}
		return false;
	}
	
	
	private int findRank(ArrayList<BDD> advice, Point2D xNearest2D, Point2D xNew2D, BDD transition, ProductAutomaton productAutomaton) {
		try {
			if(Environment.getLabelling().getLabel(xNearest2D).equals(Environment.getLabelling().getLabel(xNew2D))) {
				return -1;
			}
			for(int i=0;i<advice.size();i++) {
				if(! transition.and(advice.get(i)).isZero()) {
					return i;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	
	
	private float getProb(int rank) 
	{
		switch(rank) 
		{
			case -1: return 0.1f; // not found in advice
//			case 0: return 1;
//			case 1: return 1f;
//			case 2: return 0.95f;
//			case 3: return 0.9f;
//			case 4: return 0.85f;
//			case 5: return 0.8f;
//			case 6: return 0.75f;
//			case 7: return 0.7f;
//			case 8: return 0.6f;
			default: return 1f;
		}
	}
	
	/**
	 * add a vertex to the {@link #Map map}
	 * @param v
	 */
	protected void addToMap(Vertex v) {
		try {
			BDD label = Environment.getLabelling().getLabel(v.getPoint());
			if(findTheVertex(label) == null) 
			{
				map.add(new Pair<BDD, Vertex>(label, v));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Find distance between two points
	 * @param p
	 * @param q
	 * @return
	 */
	private float distance(Point p, Point q) 
	{
		return (float) Math.sqrt(Math.pow(p.x - q.x, 2)+Math.pow(p.y - q.y, 2));
	}
	
	
	
	/**
	 * give a point in the direction of 'dest' from source at a distance <= eta
	 * @param source
	 * @param dest
	 * @return the point
	 */
	public Point2D steer(Point2D source, Point2D dest) 
	{
		float d	= (float) source.distance(dest);
		if(d <= eta) 
		{
			return dest;
		} else 
		{
			Point2D temp = new Point2D.Float((float) (source.getX() + ((eta - 0.00001) * (dest.getX() - source.getX())/d)), (float) (source.getY() + ((eta - 0.00001) * (dest.getY() - source.getY())/d)));
			return temp;
		}
	}
	
	
	public void move(int rank, Point2D xNew2D, BDD transition) 
	{	
		if(rank != -1 && productAutomaton.sampledTransitions.and(transition).isZero()) // if sampled from advice 
		{ 
			adviceSampled[rank]++;
			currentRobotPosition = (Point2D.Float) xNew2D.clone();
			countSinceLastMove   = 0;
			movement.add(currentRobotPosition);
			endIteration = true;
		} 
		else 
		if (countSinceLastMove >= movingThreshold) // hasn't moved in last few iterations
		{ 
			if(! flagFirstMove) 
			{
				// plotting before the first time it moves
				plotGraph(null);
				flagFirstMove = true;
			}			
			
//			----------------------Moving to the min degree neighbour---------------------
//			neighbours = new ArrayList<Point2D>();
//			tree.nearestN(convertPoint2DToPoint(currentRobotPosition), 
//					new TIntProcedure() 
//					{
//						public boolean execute(int i) {
//							Point2D newPoint = convertPointToPoint2D(treePoints.get(i));
//							neighbours.add(newPoint);
//							return true;
//						}
//					},
//					500,
//					java.lang.Float.POSITIVE_INFINITY);
//			Vertex newVertex, vertexWithMinDeg = null;
//			for(int j=0;j<neighbours.size();j++) {
//				newVertex = findTheVertex(neighbours.get(j));
//				if(vertexWithMinDeg == null) {
//					vertexWithMinDeg = newVertex;
//				}
//				else if(graph.degreeOf(newVertex) < graph.degreeOf(vertexWithMinDeg)) {
//					vertexWithMinDeg = newVertex;
//				}
//			}
//			currentRobotPosition = vertexWithMinDeg.getPoint();
//			countSinceLastMove   = 0;
//			movement.add(currentRobotPosition);
//			-----------------------------------------------------------------------------
			
			
//			------------------Moving according to the frontier---------------------------
			Point2D p = discretization.findAMove(currentRobotPosition);
			if(p != null) {
//				System.out.println(p.toString());
				tree.nearest(convertPoint2DToPoint(p),
						new TIntProcedure() {
							public boolean execute(int i) 
							{
								currentRobotPosition = convertPointToPoint2D(treePoints.get(i));
								countSinceLastMove   = 0;
								movement.add(currentRobotPosition);
								return false;
							}
						}, 
						java.lang.Float.POSITIVE_INFINITY);
			}
			else {
				neighbours = new ArrayList<Point2D>();
				tree.nearestN(convertPoint2DToPoint(currentRobotPosition), 
						new TIntProcedure() 
						{
							public boolean execute(int i) {
								Point2D newPoint = convertPointToPoint2D(treePoints.get(i));
								neighbours.add(newPoint);
								return true;
							}
						},
						100,
						java.lang.Float.POSITIVE_INFINITY);
				Vertex newVertex, vertexWithMinDeg = null;
				for(int j=0;j<neighbours.size();j++) {
					newVertex = findTheVertex(neighbours.get(j));
					if(vertexWithMinDeg == null) {
						vertexWithMinDeg = newVertex;
					}
					else if(graph.degreeOf(newVertex) < graph.degreeOf(vertexWithMinDeg)) {
						vertexWithMinDeg = newVertex;
					}
				}
				currentRobotPosition = vertexWithMinDeg.getPoint();
				countSinceLastMove   = 0;
				movement.add(currentRobotPosition);
				
			}
//			-----------------------------------------------------------------------------
		} else {
			countSinceLastMove++;
		}
	}

	
	
	/**
	 * convert point object to a point2D object
	 * @param p
	 * @return
	 */
	private Point2D convertPointToPoint2D(Point p) 
	{
		return new Point2D.Float(p.x, p.y);
	}
	
	/**
	 * convert point2D object to a point object
	 * @param p
	 * @return
	 */
	private Point convertPoint2DToPoint(Point2D p) 
	{
		return new Point((float) p.getX(), (float)p.getY());
	}
	
	/**
	 * Sample a transition with source in 'fromStates' and destination in 'toStates'
	 * @param fromStates
	 * @param toStates
	 * @param productAutomaton
	 * @return sampled transition
	 * @throws Exception
	 */
	public BDD sample(BDD fromStates, BDD toStates, ProductAutomaton productAutomaton) throws Exception 
	{
		Point2D.Float p;
		BDD transition;
		symbolicTransitionsInCurrentBatch = ProductAutomaton.factory.zero();
		int i 		= 0;
		while(i < ProductAutomaton.threshold)
		{
			p 			= sampleInSensingArea();
			transition 	= buildGraph(fromStates, toStates, p, productAutomaton, 1);
			if(! transition.isZero())
			{
				return transition;
			}
			i++;
		}
		return null;
	}
	
	
	

	private Point2D.Float sampleInSensingArea() 
	{
		Point2D.Float p;
		while(true) 
		{
			p = env.sample();
			
			if(distance(p, currentRobotPosition) < sensingRadius) 
			{
				if(env.obstacleFree(p)) 
				{
					discretization.updateDiscretization(p, 1);
					numOfFrontierUpdates++;
					totalSampledPoints++;
					return p;
				}
				else {
					discretization.updateDiscretization(p, 4);
					numOfFrontierUpdates++;
				}
			}
		}
	}

	private float distance(Point2D p, Point2D q) {
		return (float) Math.sqrt(Math.pow(p.getX() - q.getX(), 2)+Math.pow(p.getY() - q.getY(), 2));
	}

	/**
	 * Sample a point from anywhere and add it to the Rtree/graph
	 * @param productAutomaton
	 * @return sampled transition
	 */
	public BDD sampleRandomly(ProductAutomaton productAutomaton) 
	{
		Point2D.Float p;
		BDD transition;
		int i	= 0;
		while(i < ProductAutomaton.threshold)
		{
			p 			= sampleInSensingArea();
			transition 	= buildGraph(ProductAutomaton.factory.one(), ProductAutomaton.factory.one(), p, productAutomaton, 2);
			if(! transition.isZero())
			{
				countSinceLastMove++;
				return transition;
			}
			i++;
		}
		return null;
	}

	/**
	 * Set initial point
	 * @param p2D
	 */
	public void setStartingPoint(Point2D p2D) 
	{
		this.initVertex = new Vertex(p2D);
		graph.addVertex(initVertex); // add to graph
		Rectangle rect 	= new Rectangle((float) p2D.getX(), (float) p2D.getY(), (float) p2D.getX(), (float) p2D.getY());
		Point p 		= new Point((float) p2D.getX(), (float) p2D.getY());
		treePoints.add(p);
		currentRobotPosition = p2D;
		movement.add(p2D);
		
		
		// don't output random things---------
//		System.setOut(new PrintStream(OutputStream.nullOutputStream()));
		tree.add(rect, numPoints);	//add to Rtree
//		System.setOut(System.out); //----------
		numPoints++;
	}

	/**
	 * Lift the path from abstraction to the graph
	 * @param path
	 * @return 
	 */
	public List<DefaultEdge> liftPath(ArrayList<BDD> path) {
		Vertex source 		= initVertex;
		
		Vertex dest;
		Iterator<BDD> it 	= path.iterator();
		BDD nextState;
		List<DefaultEdge> finalPath;
		
		
		//initialize finalPath
		nextState 			= it.next(); // first point is already there
		nextState 			= it.next();
		
		dest = findTheVertex(nextState);
		GraphPath<Vertex, DefaultEdge> nextPath = DijkstraShortestPath.findPathBetween(graph, source, dest);
		finalPath 			= nextPath.getEdgeList();
		
		// iterate over the abstract path
		while(it.hasNext())
		{
			source 			= dest;
			nextState 		= it.next();
			dest 			= findTheVertex(nextState);
			try {
				nextPath 		= DijkstraShortestPath.findPathBetween(graph, source, dest);
			} catch(Exception e) {
				e.printStackTrace();
			}
			finalPath.addAll(nextPath.getEdgeList());
		}
		return finalPath;
	}   
	
	public List<DefaultEdge> findPath(ArrayList<Point2D> movement) {
		Vertex source, dest;
		
		Iterator<Point2D> it 	= movement.iterator();
		Point2D firstPoint, nextPoint;
		List<DefaultEdge> finalPath;
		
		
		//initialize finalPath
		if(it.hasNext()) {
			firstPoint 				= it.next(); // first point is already there
		} else {
			return null;
		}
		if(it.hasNext()) {
			nextPoint 				= it.next();
		} else {
			return null;
		}
		
		source = findTheVertex(firstPoint);
		dest = findTheVertex(nextPoint);
		GraphPath<Vertex, DefaultEdge> nextPath = DijkstraShortestPath.findPathBetween(graph, source, dest);
		finalPath 			= nextPath.getEdgeList();
		
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
	 * Find a vertex in the graph where labelling is
	 * @param nextState
	 * @return
	 */
	private Vertex findTheVertex(BDD label) 
	{
		Iterator<Pair<BDD, Vertex>> it = map.iterator();
		while(it.hasNext())
		{
			Pair<BDD, Vertex> n = (Pair<BDD, Vertex>) it.next();
			if(! n.getFirst().and(label).isZero())
			{
				return n.getSecond();
			}
		}
		return null;
	}
	
	/**
	 * Find a vertex in the graph whose point is p
	 * @param p
	 * @return
	 */
	private Vertex findTheVertex(Point2D p) 
	{
		Set<Vertex> vertexSet 	= graph.vertexSet();
		Iterator<Vertex> it 	= vertexSet.iterator();
		Vertex temp;
		while(it.hasNext()) 
		{
			temp 				= it.next();
			if(Math.abs(temp.getPoint().getX() - p.getX()) < 0.00001  &&  Math.abs(temp.getPoint().getY() - p.getY()) < 0.00001) 
			{
				return temp;
			}
		}
		return null;
	}

	/**
	 * Plot the graph
	 * @throws IOException 
	 */
	public void plotGraph(List<DefaultEdge> finalPath)  
	{
		if(finalPath != null) {
			new ShowGraph(graph, env, findPath(movement), finalPath).setVisible(true);
			new StoreGraph(env, graph, finalPath, findPath(movement), "end");
		} else if(! flagFirstMove) {
			new StoreGraph(graph, findPath(movement), "firstMove"); 
		} else if(! flagBin) {
			new StoreGraph(graph, findPath(movement), "bin");
		} else if(! flagRoom) {
			new StoreGraph(graph, findPath(movement), "room");
		}
	}
	
	
	public void printAPList(BDD state) throws PlanningException 
	{
		ArrayList<String> apList	= findAPList(state);
		System.out.print("[");
		for(int j=0; j<apList.size(); j++) 
		{
			if(j < apList.size() - 1) 
			{
				System.out.print(apList.get(j)+",");
			}
			else 
			{
				System.out.print(apList.get(j));
			}
		}
		System.out.print("]  ");
	}

	private ArrayList<String> findAPList(BDD state) throws PlanningException 
	{
		ArrayList<String> list		= new ArrayList<String>();
		for(int i=0; i<ProductAutomaton.numAPSystem; i++) 
		{
			if(! state.and(ProductAutomaton.ithVarSystemPre(i)).isZero()) 
			{
				list.add(ProductAutomaton.apListSystem.get(i));
			}
		}
		list.add(Integer.toString(state.scanVar(ProductAutomaton.propertyDomainPre()).intValue()));
		return list;
	}
	

}
