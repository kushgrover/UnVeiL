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
import environment.EdgeSupplier;
import environment.Environment;
import environment.Vertex;
import environment.VertexSupplier;

public class RRG 
{
	Environment env;
	ProductAutomaton productAutomaton;

	SpatialIndex tree = new RTree();
	Graph<Vertex, DefaultEdge> graph;
	public Discretization discretization;
	ArrayList<Point> treePoints = new ArrayList<Point>();; // list of all the points in the Rtree/graph
	public ArrayList<Point2D> movement = new ArrayList<Point2D>();
	ArrayList<Pair<BDD, Vertex>> map = new ArrayList<Pair<BDD, Vertex>>(); // map between abstraction and rtree
	Vertex initVertex;  //initial vertex in the graph

	float sensingRadius;
	float rrgRadius;
	float maximumRRGStepSize;
	double gamma; // parameter used in RRG radius
	BDD forwardSampledTransitions; 

	public int totalPoints = 0;
	public int numOfFrontierUpdates = 0;
	public int totalSampledPoints = 0;
	public float moveTime = 0;
	public int[] adviceSampled = new int[] {0,0,0,0,0,0,0,0,0,0};
	Point2D currentRobotPosition;
	public int countSinceLastMove = 0;
	boolean endBatch;// flag for ending the current batch
	BDD symbolicTransitionsInCurrentBatch;

	// used for exporting graph
	boolean flagBin = false;
	boolean flagRoom = false;
	boolean flagFirstMove = false;
	
	
	/**
	 * Initialise the RRG object with the constructor
	 * @param env
	 */
	public RRG(Environment env) 
	{
		this.env 				= env;
		this.maximumRRGStepSize = (float) PlanningSettings.get("planning.eta");
		float[] sub 			= new float[] {env.getBoundsX()[1]-env.getBoundsX()[0], env.getBoundsY()[1]-env.getBoundsY()[0]};
		this.sensingRadius		= (float) PlanningSettings.get("planning.sensingRadius");
		this.gamma 				= 2.0 * Math.pow(1.5,0.5) * Math.pow(sub[0]*sub[1]/Math.PI,0.5);
		this.graph 				= new SimpleGraph<Vertex, DefaultEdge>(new VertexSupplier(), new EdgeSupplier(), true);
		this.discretization 	= new Discretization(env, (float) PlanningSettings.get("planning.discretizationSize"));
		this.forwardSampledTransitions = ProductAutomaton.factory.zero();
		this.tree.init(null);
	}
	
	
	public void setProductAutomaton(ProductAutomaton productAutomaton) {
		this.productAutomaton = productAutomaton;
	}
	
	/**
	 * Set initial point
	 * @param p2D
	 * @throws Exception 
	 */
	public void setStartingPoint(Point2D p2D) throws Exception 
	{
		this.initVertex = new Vertex(p2D);
		graph.addVertex(initVertex);
		Rectangle rect 	= new Rectangle((float) p2D.getX(), (float) p2D.getY(), (float) p2D.getX(), (float) p2D.getY());
		Point p 		= new Point((float) p2D.getX(), (float) p2D.getY());
		treePoints.add(p);
		currentRobotPosition = p2D;
		movement.add(p2D);
		tree.add(rect, totalPoints);
		discretization.knowDiscretization(env, productAutomaton, p2D, sensingRadius);
		totalPoints++;
	}
	
	public Graph<Vertex, DefaultEdge> getGraph(){
		return graph;
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
			default: return 1f;
		}
	}
	

	
	private void buildGraph(ArrayList<BDD> advice, Point2D xRand2D) {
		
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
				
				BDD transition = ProductAutomaton.factory.zero();
				try {
					transition		= Environment.getLabelling().getLabel(xNearest2D);
					transition 		= transition.and(productAutomaton.changePreSystemVarsToPostSystemVars(Environment.getLabelling().getLabel(xNew2D)));
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				
				if(checkValidity(advice, xNearest2D, xNew2D, transition))
				{
					computeRrgRadius(); 
					addSymbolicTransitions(xNearest2D, xNew2D);
					addGraphEdge(xNearest2D, xNew2D);
					
					try { // update discretization for visiting point
						discretization.updateDiscretization(xNearest2D, xNew2D, 2);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					numOfFrontierUpdates++;					
					
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
									if(! env.collisionFreeFromOpaqueObstacles(neighbour2D, currentRobotPosition)) return true;
									
									if(distance(xNew, neighbour) <= rrgRadius		&&		env.collisionFreeAll(xNew2D, neighbour2D)){
										addSymbolicTransitions(neighbour2D, xNew2D);
										addGraphEdge(neighbour2D, xNew2D);
										try {
											discretization.updateDiscretization(neighbour2D, xNew2D, 2);
										} catch (Exception e) {
											e.printStackTrace();
										}
										numOfFrontierUpdates++;
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
					int rank = findRank(advice, xNearest2D, xNew2D, transition); // rank of transition according to advice
					try {
						move(rank, xNew2D, transition, advice); // move if required
					} catch (Exception e) {
						e.printStackTrace();
					}
					moveTime += System.nanoTime() - tempTime;
				}
		        return true;
		    }
		};
		tree.nearest(xRand, procedure, java.lang.Float.POSITIVE_INFINITY); // apply 'procedure' to the nearest point of xRand
	}
	
	public BDD sample(ArrayList<BDD> advice) throws Exception {
		symbolicTransitionsInCurrentBatch = ProductAutomaton.factory.zero();
		endBatch = false;
		Point2D p;
		
		p = discretization.sampleFromAdvice(advice, currentRobotPosition, sensingRadius, productAutomaton, env);
		if(p != null) {
			System.out.println("Found sample from advice using frontiers " + p.toString());
			buildGraph(advice, p);
		}
		
		while(!endBatch) {
			p = sampleInSensingArea();
			buildGraph(advice, p);
		}
		return symbolicTransitionsInCurrentBatch;
	}

	private void addGraphEdge(Point2D xNearest2D, Point2D xNew2D) {
		Vertex source	= findTheVertex(xNearest2D);
		Vertex target 	= findTheVertex(xNew2D);
		if(target == null) {
			target = new Vertex(xNew2D);
			graph.addVertex(target);
		}
		try {
			DefaultEdge edge = graph.addEdge(source, target);
			if(edge != null) {
				graph.setEdgeWeight(edge, distance(xNearest2D, xNew2D));
			}
			addToMap(target);
		} catch (Exception IllegalArgumentException) {}		
	}

	private void computeRrgRadius() {
		if(totalPoints > 1)
			rrgRadius = (float) Math.min(gamma * Math.pow(Math.log(totalPoints)/(totalPoints), (0.5)), maximumRRGStepSize);
		else
			rrgRadius = maximumRRGStepSize;
	}

	private void addSymbolicTransitions(Point2D xNearest2D, Point2D xNew2D) {
		try {
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

	private boolean checkValidity(ArrayList<BDD> advice, Point2D xNearest2D, Point2D xNew2D, BDD transition) {
		if(! env.collisionFreeAll(xNearest2D, xNew2D)) return false; // new edge is obstacle free
		if(! env.collisionFreeFromOpaqueObstacles(xNew2D, currentRobotPosition)) return false; // new edge is visible
		if(! forwardSampledTransitions.and(transition).isZero()) return false; // transition already sampled
		int rank = findRank(advice, xNearest2D, xNew2D, transition);
		float prob = getProb(rank);
		float rand = (float) Math.random();
		if(rand < prob) return true;
		return false;
	}
	
	private int findRank(ArrayList<BDD> advice, Point2D xNearest2D, Point2D xNew2D, BDD transition) {
		try {
			if(Environment.getLabelling().getLabel(xNearest2D).equals(Environment.getLabelling().getLabel(xNew2D))) return -1;
			for(int i=0;i<advice.size();i++) {
				if(! transition.and(advice.get(i)).isZero()) return i;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	/**
	 * add a vertex to the {@link #Map map}
	 * @param v
	 */
	protected void addToMap(Vertex v) {
		try {
			BDD label = Environment.getLabelling().getLabel(v.getPoint());
			if(findTheVertex(label) == null) 	map.add(new Pair<BDD, Vertex>(label, v));
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
	private float distance(Point p, Point q) {
		return (float) Math.sqrt(Math.pow(p.x - q.x, 2) + Math.pow(p.y - q.y, 2));
	}
	
	/**
	 * give a point in the direction of 'dest' from source at a distance <= maximumRRGStepSize
	 * @param source
	 * @param dest
	 * @return the point
	 */
	public Point2D steer(Point2D source, Point2D dest) 
	{
		float d	= (float) source.distance(dest);
		if(d <= maximumRRGStepSize) {
			return dest;
		}
		else {
			Point2D temp = new Point2D.Float((float) (source.getX() + ((maximumRRGStepSize - 0.00001) * (dest.getX() - source.getX())/d)), (float) (source.getY() + ((maximumRRGStepSize - 0.00001) * (dest.getY() - source.getY())/d)));
			return temp;
		}
	}
	
	
	
	boolean alreadyFound = false; // has to be a global var
	public void move(int rank, Point2D xNew2D, BDD transition, ArrayList<BDD> advice) throws Exception {
		if(rank != -1 && productAutomaton.sampledTransitions.and(transition).isZero()) // if sampled from advice and have not sampled it before
		{ 
			adviceSampled[rank]++;
			currentRobotPosition = (Point2D.Float) xNew2D.clone();
			countSinceLastMove   = 0;
			movement.add(currentRobotPosition);
			discretization.knowDiscretization(env, productAutomaton, currentRobotPosition, sensingRadius);
			endBatch = true;
		} 
		else if (countSinceLastMove >= (int) PlanningSettings.get("planning.movingThreshold")) { // hasn't moved in last few iterations 
			if(! flagFirstMove) { // plotting before the first time it moves
				plotGraph(null);
				flagFirstMove = true;
			}
			
//			------------------Moving according to the frontier---------------------------
			Point2D p = discretization.findAMove(currentRobotPosition);
			if(p != null) {
				alreadyFound = false;
				if(env.obstacleFreeAll(p)) {
					tree.nearestN(convertPoint2DToPoint(p),
						new TIntProcedure() {
							public boolean execute(int i) 
							{								
								Point2D newPoint = convertPointToPoint2D(treePoints.get(i));
								if(! alreadyFound && env.collisionFreeAll(newPoint, p)) {
									currentRobotPosition = newPoint;
									countSinceLastMove   = 0;
									movement.add(currentRobotPosition);
									alreadyFound = true;
									endBatch = true;
									System.out.println("Moving to: " + currentRobotPosition.toString() + "\n");
									return false;
								}
								return true;
							}
						}, 
						50, java.lang.Float.POSITIVE_INFINITY);
				} else {
					tree.nearest(convertPoint2DToPoint(p),
							new TIntProcedure() {
								public boolean execute(int i) 
								{								
									Point2D newPoint = convertPointToPoint2D(treePoints.get(i));
									currentRobotPosition = newPoint;
									countSinceLastMove   = 0;
									movement.add(currentRobotPosition);
									alreadyFound = true;
									endBatch = true;
									System.out.println("Moving to: " + currentRobotPosition.toString() + "\n");
									return false;
								}
							}, 
							java.lang.Float.POSITIVE_INFINITY);
				}
				if(! alreadyFound) {
					System.out.println("[DEBUG] Did not find any neighbours");;
				}
				
			}
			else {
				ArrayList<Point2D> neighbours = new ArrayList<Point2D>();
				tree.nearestN(convertPoint2DToPoint(currentRobotPosition), 
						new TIntProcedure() 
						{
							public boolean execute(int i) {
								Point2D newPoint = convertPointToPoint2D(treePoints.get(i));
								neighbours.add(newPoint);
								return true;
							}
						},
						50,
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
				discretization.knowDiscretization(env, productAutomaton, currentRobotPosition, sensingRadius);
				endBatch = true;
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
	private Point2D convertPointToPoint2D(Point p) {
		return new Point2D.Float(p.x, p.y);
	}
	
	/**
	 * convert point2D object to a point object
	 * @param p
	 * @return
	 */
	private Point convertPoint2DToPoint(Point2D p) {
		return new Point((float) p.getX(), (float)p.getY());
	}

	

	private Point2D sampleInSensingArea() throws Exception 
	{
		Point2D p;
		while(true) {
			p = env.sample();
			if(distance(p, currentRobotPosition) < sensingRadius) {
				if(env.obstacleFreeAll(p)) {
					if(env.collisionFreeFromOpaqueObstacles(currentRobotPosition, p)) {
						discretization.updateDiscretization(p, 1);
						numOfFrontierUpdates++;
					}
					totalSampledPoints++;
					return p;
				} else {
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
//	public BDD sampleRandomly(ProductAutomaton productAutomaton) 
//	{
//		Point2D.Float p;
//		BDD transition;
//		int i	= 0;
//		while(i < ProductAutomaton.threshold)
//		{
//			p 			= sampleInSensingArea();
//			transition 	= buildGraph(ProductAutomaton.factory.one(), ProductAutomaton.factory.one(), p, productAutomaton, 2);
//			if(! transition.isZero())
//			{
//				countSinceLastMove++;
//				return transition;
//			}
//			i++;
//		}
//		return null;
//	}

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
