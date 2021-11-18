package modules;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import abstraction.ProductAutomaton;
import net.sf.javabdd.BDD;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultEdge;

import com.infomatiq.jsi.Point;
import com.infomatiq.jsi.SpatialIndex;
import environment.Environment;
import environment.Vertex;
import settings.PlanningException;
import settings.PlanningSettings;

public class UnknownGrid extends Grid {

	ArrayList<Pair<Point2D, Integer>> adviceFrontiers;
//	public int currentRank = -1;
	Graph<Vertex, DefaultEdge> graph;
	SpatialIndex tree;
	ArrayList<Point> treePoints; // list of all the points in the Rtree/graphRRG
	Environment env;
	
	public UnknownGrid(Environment env, float size, Graph<Vertex, DefaultEdge> graph, SpatialIndex tree, ArrayList<Point> treePoints) 
	{
		super(env, size);
		this.adviceFrontiers = new ArrayList<>();
		this.graph = graph;
		this.tree = tree;
		this.env = env;
		this.treePoints = treePoints;
	}

	private Pair<Point2D, Pair<Integer, Float>> findBestAdviceFrontier(Point2D xRobot) {
		Point2D bestPoint = findFrontierCenter(frontiers.get(0));
		float bestIG = 0;
		int bestRank = -1;
		int bestIndex = 0;
		for(int i=0; i<adviceFrontiers.size(); i++) {
			int currentRank = adviceFrontiers.get(i).getSecond();
			float currentIG = findIGInAdviceFrontier(adviceFrontiers.get(i), bestRank, xRobot);
			if(currentIG > bestIG) {
				bestPoint = adviceFrontiers.get(i).getFirst();
				bestIG = currentIG;
				bestRank = currentRank;
				bestIndex = i;
			}
		}
		return new Pair<>(bestPoint, new Pair<>(bestIndex, bestIG));
	}

	/*
	 * know the grid in the sensing radius
	 */
	@Override
	public void knowDiscretization(Environment env,
								   ProductAutomaton productAutomaton,
								   Point2D currentPosition,
								   float sensingRadius) throws PlanningException {
		for(int i = 0; i<numX; i++) {
			for(int j=0; j<numY; j++) {
				if(cellInsideSensingRadius(i, j, currentPosition, sensingRadius)) {
					Point2D tempPoint = findCentre(i,j);
					if(env.collisionFreeFromOpaqueObstacles(currentPosition, tempPoint) && env.obstacleFreeAll(tempPoint)) {
						BDD label = Environment.getLabelling().getLabel(tempPoint);
						updateDiscretization(tempPoint, 1, label);
					}
					else if(! env.obstacleFreeAll(tempPoint)) {
						updateDiscretization(tempPoint, 3, null);
					}
				}
			}
		}
	}
	
	
	/*
	 * finds the best move according to the frontiers
	 */
	public Pair<Point2D, Integer> findAMove(Point2D xRobot)
	{
		ArrayList<ArrayList<int[]>> frontiers = findFrontiers();
		if(frontiers.isEmpty()) {
			frontiers = findFrontiers();
		}
		if(frontiers.isEmpty()) {
			return null;
		}
		Pair<Point2D, Pair<Integer, Float>> bestFrontier = findBestFrontier(frontiers, xRobot); // point, index, IG
		Pair<Point2D, Pair<Integer, Float>> bestAdviceFrontier = findBestAdviceFrontier(xRobot); // point, rank, IG
		
		if(bestAdviceFrontier.getSecond().getSecond() > bestFrontier.getSecond().getSecond()) {
			int index = bestAdviceFrontier.getSecond().getFirst();
			adviceFrontiers.remove(index);
			if((boolean) PlanningSettings.get("debug")) {
				System.out.println("Used Advice frontier: " + bestAdviceFrontier.getFirst() + " with IG = " + bestAdviceFrontier.getSecond().getSecond());
			}
			return new Pair<>(bestAdviceFrontier.getFirst(), bestAdviceFrontier.getSecond().getFirst());
		}
		else {
			if((boolean) PlanningSettings.get("debug")) {
				System.out.println("Used frontier: " + bestFrontier.getFirst() + " with IG = " + bestFrontier.getSecond().getSecond());
			}
			return new Pair<>(bestFrontier.getFirst(), -1);
		}
	}

	public void addAdviceFrontier(Point2D p, Point2D currentPosition, int rank) {
		Pair<Point2D, Integer> f = new Pair<>(p, rank);
		if((boolean) PlanningSettings.get("debug")) {
			System.out.println("Added advice frontier " + p);
		}
		adviceFrontiers.add(f);
	}
	
	@Override
	Pair<Point2D, Pair<Integer, Float>> findBestFrontier(ArrayList<ArrayList<int[]>> frontiers, Point2D xRobot){
		Point2D bestPoint = findFrontierBestPoint(frontiers.get(0));
		float bestIG = 0;
		int bestIndex = 0;
		for(int i=0;i<frontiers.size();i++) {
			Point2D tempPoint = findFrontierBestPoint(frontiers.get(i));
			if(tempPoint.equals(xRobot)) {
//				printDiscretization();
				continue;
			}
			float currentIG = (float) frontiers.get(i).size() / computeDistance(graph, xRobot, tempPoint);
//			if(distance(centers.get(i), xRobot) < distance(closest, xRobot)) {
			if(currentIG > bestIG) {
				bestPoint = tempPoint;
				bestIG = currentIG;
				bestIndex = i;
			}
		}
		return new Pair<>(bestPoint, new Pair<>(bestIndex, bestIG));
	}
	
	private Point2D findFrontierBestPoint(ArrayList<int[]> frontier) {
		Point2D p = findFrontierCenter(frontier);
		return findClosestInCurrentGraph(p);
	}

	float findIGInAdviceFrontier(Pair<Point2D, Integer> adviceFrontier, int bestRank, Point2D xRobot) {
		int currentRank = adviceFrontier.getSecond();
		//		if(currentRank < bestRank && bestRank > -1) {
//			currentIG = (currentRank - bestRank) / (size * distance(frontier.getFirst(), xRobot));
//		}
//		else if (currentRank > bestRank && bestRank > -1){
//			currentIG = 1 / ((bestRank - currentRank + 1) * size * distance(frontier.getFirst(), xRobot));
//		}
//		else {
//			currentIG = 1 / ((bestRank + 1) * size * distance(frontier.getFirst(), xRobot));
//		}
		return 5 / ((float) StrictMath.pow(currentRank + 1, 2) * size * computeDistance(graph, xRobot, adviceFrontier.getFirst()));
	}

	/**
	 * Find a vertex in the graphRRG whose point is p
	 * @param p
	 * @return
	 */
	protected Vertex findTheVertex(Point2D p) 
	{
		Set<Vertex> vertexSet 	= graph.vertexSet();
		for (Vertex temp : vertexSet) {
			if (Math.abs(temp.getPoint().getX() - p.getX()) < 0.0001 && Math.abs(temp.getPoint().getY() - p.getY()) < 0.0001) {
				return temp;
			}
		}
		System.out.println("Something wrong O.o " + p);
		return null;
	}
	
	public float computeDistance(Graph<Vertex, DefaultEdge> graph, Point2D source, Point2D target) {
		Vertex sourceV = findTheVertex(source);
		Vertex targetV = findTheVertex(target);
		GraphPath<Vertex, DefaultEdge> path = DijkstraShortestPath.findPathBetween(graph, sourceV, targetV);
		List<DefaultEdge> tempPath = new ArrayList<>(path.getEdgeList());
		Iterator<DefaultEdge> it = tempPath.iterator();
		float length = 0;
		while(it.hasNext()) {
			DefaultEdge edge = it.next();
			length += (float) graph.getEdgeWeight(edge);
		}
		return length;
	}

	Point2D foundPoint = null;
	boolean alreadyFound = false;
	private Point2D findClosestInCurrentGraph(Point2D target) {
		alreadyFound = false;
		tree.nearestN(convertPoint2DToPoint(target),
				i -> {
					if(alreadyFound) {
						return false;
					}
					Point2D newPoint = convertPointToPoint2D(treePoints.get(i));
					if(env.collisionFreeFromOpaqueObstacles(target, newPoint)) {
						foundPoint = newPoint;
						alreadyFound = true;
					}
					return true;
				},
			50,
			java.lang.Float.POSITIVE_INFINITY);
		if(!alreadyFound) {
			tree.nearestN(convertPoint2DToPoint(target),
					i -> {
						if(alreadyFound) {
							return false;
						}
						Point2D newPoint = convertPointToPoint2D(treePoints.get(i));
						if(env.collisionFreeFromOpaqueObstacles(target, newPoint)) {
							foundPoint = newPoint;
							alreadyFound = true;
						}
						return true;

					},
					200,
					java.lang.Float.POSITIVE_INFINITY);
		}
		return foundPoint;
	}

	private static Point2D convertPointToPoint2D(Point point) {
		return new Point2D.Float(point.x, point.y);
	}
	
	private static Point convertPoint2DToPoint(Point2D point2D) {
		return new Point((float) point2D.getX(), (float) point2D.getY());
	}

}
