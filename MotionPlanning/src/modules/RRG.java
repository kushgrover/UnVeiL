package modules;


import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import gnu.trove.TIntProcedure;
import net.sf.javabdd.BDD;
import planningIO.StoreGraph;
import planningIO.printing.ShowGraph;
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
	public Frontier frontier;
	
	public float moveTime = 0;

	
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
		this.frontier 			= new Frontier(env, 0.05f);
		treePoints 				= new ArrayList<Point>();
		map						= new ArrayList<Pair<BDD, Vertex>>();
		this.countSinceLastMove = 0;
		
		numPoints				= 0;
		
		totalSampledPoints 		= 0;
		
		this.tree 				= new RTree();
		tree.init(null);
		
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
//					System.out.println("Sampled Transition: " + xNearest2D.toString() + " ---> " + xNew2D.toString());
//					try {
//						if(! Environment.getLabelling().getLabel(xNearest2D).equals(Environment.getLabelling().getLabel(xNew2D))) {
//							System.out.println(Environment.getLabelling().getLabel(xNearest2D).toString() + " ---> " + Environment.getLabelling().getLabel(xNew2D).toString());
//						}
//					} catch (Exception e1) {
//						e1.printStackTrace();
//					}

					BDD transition;
					try 
					{
						transition		= productAutomaton.changePreSystemVarsToPostSystemVars(Environment.getLabelling().getLabel(xNew2D));
						transition 		= transition.and(Environment.getLabelling().getLabel(xNearest2D));
						if(type == 1 && transition.and(productAutomaton.getBDD().and(ProductAutomaton.transitionLevelDomain().ithVar(2))).and(ProductAutomaton.getPropertyBDD()).and(ProductAutomaton.getLabelEquivalence()).isZero()) {
							return false;
						}
						transitions.orWith(transition);
					}
					catch (Exception e1) {
						e1.printStackTrace();
					}
					
					
					
					final float radius;	// radius for which the neighbours will be considered to add further edges
					if(numPoints > 1) 
					{
						radius				= (float) Math.min(gamma * Math.pow(Math.log(numPoints)/(numPoints), (0.5)), eta);
					} else 
					{
						radius				= eta;
					}
					
					
					
					//add the new point to the graph (it will be added later to the Rtree)
					final Vertex source	= new Vertex(xNew2D);
					graph.addVertex(source);
					Vertex target		= findTheVertex(xNearest2D);
					graph.addEdge(source, target);
					
					
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
					
					
					
					frontier.updateFrontier(xNew2D, 2);
					float tempTime = System.nanoTime();
					move(type, xNew2D);
					moveTime += System.nanoTime() - tempTime;
					
					
					
					tree.nearestN(xNew, 
							new TIntProcedure() // For each neighbour of 'xNew' in the given radius, execute this method
							{
								public boolean execute(int i) 
								{
									Point neighbour			= treePoints.get(i);
									Point2D neighbour2D		= convertPointToPoint2D(neighbour);
									
									if(neighbour2D.equals(xNew2D)) return true;
									
									if( distance(xNew, neighbour) <= radius		&&		env.collisionFree(xNew2D, neighbour2D) ) 
									{
										Vertex target		= findTheVertex(neighbour2D);
//										graph.addVertex(target);
										graph.addEdge(source, target);
										
										BDD transition, transition2;
										try {
											transition 		= productAutomaton.changePreSystemVarsToPostSystemVars(Environment.getLabelling().getLabel(xNew2D));
											transition 		= transition.and(Environment.getLabelling().getLabel(neighbour2D));
											transition2 	= Environment.getLabelling().getLabel(xNew2D);
											transition2 	= transition2.and(productAutomaton.changePreSystemVarsToPostSystemVars(Environment.getLabelling().getLabel(neighbour2D)));
											transitions.orWith(transition);
											transitions.orWith(transition2);
										} catch (Exception e)
										{
											e.printStackTrace();
										}
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
	
	
	public void move(int type, Point2D xNew2D) 
	{
		if(type == 1) // if sampled from advice 
		{ 
			currentRobotPosition = (Point2D.Float) xNew2D.clone();
			countSinceLastMove   = 0;
			movement.add(currentRobotPosition);
		} 
		else 
		if (countSinceLastMove == movingThreshold) // hasn't moved in last few iterations
		{ 
			if(! flagFirstMove) 
			{
				// plotting before the first time it moves
				plotGraph(null);
				flagFirstMove = true;
			}
			
//			-----------------------------Moving Randomly---------------------------------
//			Point random = convertPoint2DToPoint(env.sample());
//			tree.nearest(random,
//					new TIntProcedure() {
//						public boolean execute(int i) {
//							Point2D newPoint = convertPointToPoint2D(treePoints.get(i));
//							currentRobotPosition = newPoint;
//							countSinceLastMove   = 0;
//							movement.add(currentRobotPosition);
//							return false;
//						}
//					}, 
//					java.lang.Float.POSITIVE_INFINITY);
//			-----------------------------------------------------------------------------
			
			
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
			Point2D p = frontier.findFreeCell();
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
		}
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
		int i 		= 0;
		while(i < ProductAutomaton.threshold)
		{
			p 			= sampleInSensingArea();
			totalSampledPoints++;
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
					frontier.updateFrontier(p, 1);
					return p;
				}
				else {
					frontier.updateFrontier(p, 8);
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
			totalSampledPoints++;
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
			nextPath 		= DijkstraShortestPath.findPathBetween(graph, source, dest);
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
			finalPath.addAll(nextPath.getEdgeList());
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
			if(Math.abs(temp.getPoint().getX() - p.getX()) < 0.0001  &&  Math.abs(temp.getPoint().getY() - p.getY()) < 0.0001) 
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

}
