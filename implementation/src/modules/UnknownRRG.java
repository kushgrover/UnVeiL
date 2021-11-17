package modules;


import abstraction.ProductAutomaton;
import com.infomatiq.jsi.Point;
import com.infomatiq.jsi.Rectangle;
import environment.Environment;
import environment.Vertex;
import gnu.trove.TIntProcedure;
import net.sf.javabdd.BDD;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultEdge;
import planningIO.StoreGraphUnknown;
import planningIO.printing.ShowGraphUnknown;
import settings.PlanningException;
import settings.PlanningSettings;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UnknownRRG extends RRG
{
	public UnknownGrid grid;

	float sensingRadius;
	public int numOfFrontierUpdates = 0;
	public float moveTime = 0;

	// used for exporting graphRRG
	boolean flagBin = false;
	boolean flagRoom = false;
	boolean flagFirstMove = false;
	
	boolean explorationComplete = false;
	
	public ArrayList<BDD> movementBDD;
	
	
	/**
	 * Initialise the RRG object with the constructor
	 * @param env
	 */
	public UnknownRRG(Environment env) 
	{
		super(env);
		this.sensingRadius		= (float) PlanningSettings.get("sensingRadius");
		this.grid 				= new UnknownGrid(env, (float) PlanningSettings.get("gridSize"), graph, tree, treePoints);
		this.movementBDD 		= new ArrayList<>();
	}
	
	/**
	 * Set initial point
	 * @param p2D
	 * @throws PlanningException
	 */
	@Override
	public void setStartingPoint(Point2D p2D) throws PlanningException {
		Point p 		= new Point((float) p2D.getX(), (float) p2D.getY());
		Rectangle rect 	= new Rectangle((float) p2D.getX(), (float) p2D.getY(), (float) p2D.getX(), (float) p2D.getY());
		this.initVertex = new Vertex(p2D);
		graph.addVertex(initVertex);
		tree.add(rect, totalPoints);
		treePoints.add(p);
		grid.knowDiscretization(env, productAutomaton, p2D, sensingRadius);
		this.currentRobotPosition = p2D;
		totalPoints++;
	}
	
	@Override
	public void updateMovement(Point2D newPosition) throws PlanningException {
		Vertex source = findTheVertex(currentRobotPosition);
		Vertex target = findTheVertex(newPosition);
		GraphPath<Vertex, DefaultEdge> path = DijkstraShortestPath.findPathBetween(graph, source, target);
		List<DefaultEdge> edges = new ArrayList<>(path.getEdgeList());
		movement.addAll(edges);
		
		Iterator<DefaultEdge> it = edges.iterator();
		BDD lastMovement = null;
		if(! movementBDD.isEmpty()) {
			lastMovement = movementBDD.get(movementBDD.size() - 1);
		}
		
		while(it.hasNext()) {
			DefaultEdge nextEdge = it.next();
			if(lastMovement == null) {
				BDD sourceBDD = Environment.getLabelling().getLabel(graph.getEdgeSource(nextEdge).getPoint());
				movementBDD.add(sourceBDD);
				lastMovement = sourceBDD;
			}
			BDD targetBDD = Environment.getLabelling().getLabel(graph.getEdgeTarget(nextEdge).getPoint());
			if(! lastMovement.equals(targetBDD)) {
				movementBDD.add(targetBDD);
				lastMovement = targetBDD;
			}
		}
	}
	
	/**
	 * Does one iteration of the algo with the point xRand
	 * @param advice
	 * @param xRand2D
	 */
	@Override
	void buildGraph(ArrayList<BDD> advice, Point2D xRand2D) {
		
		// need 'point2D' for graphRRG and 'point' for Rtree
		Point xRand					= convertPoint2DToPoint(xRand2D);

		// execute this procedure for the nearest neighbour of 'xRand'
		TIntProcedure procedure		= i -> {
			Point xNearest		= treePoints.get(i);
			Point2D xNearest2D	= convertPointToPoint2D(xNearest);
			Point2D xNew2D		= steer(xNearest2D, xRand2D);
			Point xNew			= convertPoint2DToPoint(xNew2D);

			BDD transition 		= ProductAutomaton.factory.zero();
			try {
				transition		= Environment.getLabelling().getLabel(xNearest2D);
				transition 		= transition.and(productAutomaton.changePreSystemVarsToPostSystemVars(Environment.getLabelling().getLabel(xNew2D)));
			} catch (PlanningException e) {
				e.printStackTrace();
			}


			if(checkValidity(advice, xNearest2D, xNew2D, transition))
			{
				currentBatchSize++;
				updateRrgRadius();
				addSymbolicTransitions(xNearest2D, xNew2D);
				addGraphEdge(xNearest2D, xNew2D);

				if((boolean) PlanningSettings.get("exportPlotData")) {
					try {
						if (!flagBin && !Environment.getLabelling().getLabel(xNew2D).and(ProductAutomaton.factory.ithVar(ProductAutomaton.varsBeforeSystemVars + 7)).isZero()) {
							new StoreGraphUnknown(graph, movement, PlanningSettings.get("outputDirectory") + "bin");
							flagBin = true;
						}
						if (!flagRoom && !Environment.getLabelling().getLabel(xNew2D).and(ProductAutomaton.factory.ithVar(ProductAutomaton.varsBeforeSystemVars + 4)).isZero()) {
							new StoreGraphUnknown(graph, movement, PlanningSettings.get("outputDirectory") + "room");
							flagRoom = true;
						}
					} catch (PlanningException e) {
						e.printStackTrace();
					}
				}
				// For each neighbour of 'xNew' in the given radius, execute this method
				tree.nearestN(xNew,
						i1 -> {
							Point neighbour			= treePoints.get(i1);
							Point2D neighbour2D		= convertPointToPoint2D(neighbour);

							if(neighbour2D.equals(xNew2D)) {
								return true;
							}

							if(distance(xNew, neighbour) <= rrgRadius		&&		env.collisionFreeAll(xNew2D, neighbour2D)){
								addSymbolicTransitions(neighbour2D, xNew2D);
								addGraphEdge(neighbour2D, xNew2D);
							}
							return true;
						},
						100, Float.POSITIVE_INFINITY); // a max of 100 neighbours are considered

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
					} catch (PlanningException e) {
						e.printStackTrace();
					}
				}
				else if (currentBatchSize >= (int) PlanningSettings.get("batchSize")) {
					endBatch = true;
				}
				moveTime += System.nanoTime() - tempTime;
			}
			return true;
		};
		tree.nearest(xRand, procedure, java.lang.Float.POSITIVE_INFINITY); // apply 'procedure' to the nearest point of xRand
	}

	/**
	 * sample one batch and add them in the graphs accordingly
	 * @param advice
	 * @return
	 */
	@Override
	public BDD sampleBatch(ArrayList<BDD> advice, int iterNum) {
		symbolicTransitionsInCurrentBatch = ProductAutomaton.factory.zero();
		currentBatchSize = 0;
		endBatch = false;

		while( !endBatch ) {
			Point2D p = sampleInExploredArea();
			buildGraph(advice, p);
		}

		if((boolean) PlanningSettings.get("exportVideoData")) {
			try {
				new StoreGraphUnknown(graph, movement, PlanningSettings.get("outputDirectory") + "video/together/" + iterNum);
			} catch (RuntimeException e1) {
				e1.printStackTrace();
			}
		}

		return symbolicTransitionsInCurrentBatch;
	}


	private void move(Point2D xNew2D, int rank) throws PlanningException {
		if(rank != -1 && rank < 10) {
			adviceSampled[rank]++;
		}
		updateMovement(xNew2D);
		currentRobotPosition = xNew2D;
		grid.knowDiscretization(env, productAutomaton, currentRobotPosition, sensingRadius);
		endBatch = true;
		if(grid.exploredCompletely()) {
			System.out.println("Exploration complete");
			explorationComplete = true;
		}
	}

	
	boolean alreadyFound = false; // has to be a global var
	/**
	 * move to a new location if required
	 * @param rank
	 * @param xNew2D
	 * @param transition
	 * @param advice
	 * @throws PlanningException
	 */
	public void tryMove(int rank, Point2D xNew2D, BDD transition, List<BDD> advice) throws PlanningException {

		if(rank != -1 && productAutomaton.sampledTransitions.and(transition).isZero()) // if sampled from advice and have not sampled it before
		{
			grid.addAdviceFrontier(xNew2D, currentRobotPosition, rank);
		} else if (currentBatchSize >= (int) PlanningSettings.get("batchSize")) { // batch is finished

			if ((boolean) PlanningSettings.get("exportPlotData")) {
				if (!flagFirstMove) { // plotting before the first time it moves
					new StoreGraphUnknown(graph, movement, PlanningSettings.get("outputDirectory") + "firstMove");
					flagFirstMove = true;
				}
			}
			if(explorationComplete) {
				return;
			}
			Pair<Point2D, Integer> foundMove = grid.findAMove(currentRobotPosition);
			if(foundMove == null){
				foundMove = new Pair<>(currentRobotPosition, -1);
			}
			Point2D p = foundMove.getFirst();
			if(p != null) {
				alreadyFound = false;
				Vertex V = findTheVertex(p);
				if(V != null) {
					move(p, foundMove.getSecond());
				}
			} 
			else {
				endBatch = true;
			}
		} 
	}

	/**
	 * sample a point inside sensing area
	 * @return
	 */
	private Point2D sampleInExploredArea() {
		while(true) {
			Point2D p = env.sample();
			if(grid.isExplored(p)) {
				if(env.obstacleFreeAll(p)) {
					if(env.collisionFreeFromOpaqueObstacles(currentRobotPosition, p)) {
//						grid.updateDiscretization(p, 1);
						numOfFrontierUpdates++;
					}
					totalSampledPoints++;
					return p;
				} else {
					grid.updateDiscretization(p, 3);
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
	public List<DefaultEdge> findPath(List<Point2D> movement) {

		Iterator<Point2D> it = movement.iterator();
		Point2D firstPoint;

		if(it.hasNext()) {
			firstPoint	= it.next(); // first point is already there
		} else {
			return null;
		}
		Point2D nextPoint;
		if(it.hasNext()) {
			nextPoint	= it.next();
		} else {
			return null;
		}

		Vertex source = findTheVertex(firstPoint);
		Vertex dest = findTheVertex(nextPoint);
		GraphPath<Vertex, DefaultEdge> nextPath = DijkstraShortestPath.findPathBetween(graph, source, dest);
		List<DefaultEdge> finalPath = new ArrayList<>(nextPath.getEdgeList());
		
		// iterate over the abstract path
		while(it.hasNext())
		{
			source 	  = dest;
			nextPoint = it.next();
			dest 	  = findTheVertex(nextPoint);
			nextPath  = DijkstraShortestPath.findPathBetween(graph, source, dest);
			if(nextPath != null) {
				finalPath.addAll(nextPath.getEdgeList());
			}
		}
		return finalPath;
	}

	public ArrayList<BDD> findRemainingPath() throws PlanningException {
		return productAutomaton.findAcceptingPath(movementBDD);
	}
		
	/**
	 * Plot the graphRRG
	 * @return
	 */
	public Pair<Float, Float> plotGraph(List<DefaultEdge> finalPath)  
	{
		if(finalPath != null) {
			if((boolean) PlanningSettings.get("generatePlot")) {
				new ShowGraphUnknown(graph, env, movement, finalPath).setVisible(true);
			}
			StoreGraphUnknown temp = new StoreGraphUnknown(env, graph, finalPath, movement, PlanningSettings.get("outputDirectory") + "end");
			return new Pair<>(temp.movementLength, temp.remainingPathLength);
		}
		return new Pair<>(0.0f, 0.0f);
	}
	
	
	

}
