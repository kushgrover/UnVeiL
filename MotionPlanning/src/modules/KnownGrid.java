package modules;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import abstraction.ProductAutomaton;
import environment.EdgeSupplier;
import environment.Environment;
import environment.Vertex;
import environment.VertexSupplier;
import net.sf.javabdd.BDD;
import settings.PlanningSettings;

public class KnownGrid extends Grid {

	Graph<Vertex, DefaultEdge> graph;
	private ArrayList<DefaultEdge> movement;

	public KnownGrid(Environment env, float size) 
	{
		super(env, size);
		this.graph = new SimpleGraph<Vertex, DefaultEdge>(new VertexSupplier(), new EdgeSupplier(), true);
		this.movement = new ArrayList<DefaultEdge>();
	}
	
	public void initializeGraph() {
		for(int i=0; i<numX; i++) {
			for(int j=0; j<numY; j++) {
				Vertex v = new Vertex(findCentre(i,j));
				graph.addVertex(v);
			}
		}
	}
	
	private void updateGraph(Point2D centre) {
		Point2D left   = new Point2D.Float((float) centre.getX()-size, (float) centre.getY());
		Point2D down   = new Point2D.Float((float) centre.getX(), (float) centre.getY()+size);
		Vertex leftV   = findTheVertex(left);
		Vertex downV   = findTheVertex(down);
		Vertex centreV = findTheVertex(centre);
		
		int i = clampX(findCell(left)[0]);
		int j = clampY(findCell(left)[1]);
		if(grid[i][j] == 1){
			DefaultEdge edge = graph.addEdge(leftV, centreV);
			if(edge != null) {
				graph.setEdgeWeight(edge, size);
			}
		}
		i = clampX(findCell(down)[0]);
		j = clampY(findCell(down)[1]);
		if(grid[i][j] == 1){
			DefaultEdge edge = graph.addEdge(downV, centreV);
			if(edge != null) {
				graph.setEdgeWeight(edge, size);
			}		
		}
	}
	
	/*
	 * know the grid in the sensing radius
	 */
	@Override
	public void knowDiscretization(Environment env, 
			ProductAutomaton productAutomaton, 
			Point2D currentPosition, 
			float sensingRadius) throws Exception 
	{	
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
		if((boolean) PlanningSettings.get("firstExplThenPlan")) {
			for(int i=1; i<numX; i++) {
				for(int j=0; j<numY-1; j++) {
					if(cellInsideSensingRadius(i, j, currentPosition, sensingRadius)) {
						Point2D tempPoint = findCentre(i,j);
						if(env.collisionFreeFromOpaqueObstacles(currentPosition, tempPoint) && env.obstacleFreeAll(tempPoint)) {
							updateGraph(tempPoint);
						}
					}
				}
			}
		}
	}

	/**
	 * Find a vertex in the graphRRG whose point is p
	 * @param p
	 * @return
	 */
	protected Vertex findTheVertex(Point2D p) 
	{
		Set<Vertex> vertexSet 	= graph.vertexSet();
		Iterator<Vertex> it 	= vertexSet.iterator();
		Vertex temp;
		while(it.hasNext()) {
			temp 				= it.next();
			if(Math.abs(temp.getPoint().getX() - p.getX()) < 0.0001  &&  Math.abs(temp.getPoint().getY() - p.getY()) < 0.0001) {
				return temp;
			}
		}
//		Vertex v = new Vertex(p);
//		graph.addVertex(v);
		return null;
	}

	Pair<Point2D, Pair<Integer, Float>> findBestFrontier(ArrayList<ArrayList<int[]>> frontiers, Point2D xRobot){
		Point2D currentPoint;
		Point2D bestPoint = findFrontierCenter(frontiers.get(0));
		float currentIG;
		float bestIG = 0;
		int bestIndex = 0;
		for(int i=0;i<frontiers.size();i++) {
			currentPoint = findFrontierCenter(frontiers.get(i));
			currentIG = (float) frontiers.get(i).size() / computeDistance(graph, currentPoint, xRobot);
//			if(distance(centers.get(i), xRobot) < distance(closest, xRobot)) {
			if(currentIG > bestIG) {
				bestPoint = currentPoint;
				bestIG = currentIG;
				bestIndex = i;
			}
		}
		return new Pair<Point2D, Pair<Integer, Float>>(bestPoint, new Pair<Integer, Float>(bestIndex, bestIG));
	}



	public float findAPath(Point2D currentPosition, Point2D newPosition) {
		float pathLength = findAPath(findCell(currentPosition), findCell(newPosition));
		return pathLength;
	}


	private float findAPath(int[] cell1, int[] cell2) {
		Point2D source = findCentre(cell1[0], cell1[1]);
		Point2D target = findCentre(cell2[0], cell2[1]);
		Vertex sourceV = findTheVertex(source);
		Vertex targetV = findTheVertex(target);

		GraphPath<Vertex, DefaultEdge> path = DijkstraShortestPath.findPathBetween(graph, sourceV, targetV);
		ArrayList<DefaultEdge> pathList = new ArrayList<DefaultEdge>();
		pathList.addAll(path.getEdgeList());
		Iterator<DefaultEdge> i = pathList.iterator();
		float pathLength = 0;
		DefaultEdge nextEdge;
		while(i.hasNext()) {
			nextEdge = i.next();
			pathLength += graph.getEdgeWeight(nextEdge);
		}
		return pathLength;
	}

	/*
	 * finds the best move according to the frontiers
	 */
	public Pair<Point2D, Integer> findAMove(Point2D xRobot)
	{
		ArrayList<ArrayList<int[]>> frontiers = findFrontiers(5);
		if(frontiers.size() == 0) {
			frontiers = findFrontiers(1);
		}
		if(frontiers.size() == 0) {
			return null;
		}
		Pair<Point2D, Pair<Integer, Float>> bestFrontier = findBestFrontier(frontiers, xRobot); // point, index, IG
		
		System.out.println("Used frontier: " + bestFrontier.getFirst() + " with IG = " + bestFrontier.getSecond().getSecond());
		return new Pair<Point2D, Integer>(bestFrontier.getFirst(), -1);
	}
	
	public void updateMovement(Point2D currentPosition, Point2D newPosition) {
		Vertex source = findTheVertex(currentPosition);
		Vertex target = findTheVertex(newPosition);
		GraphPath<Vertex, DefaultEdge> path = DijkstraShortestPath.findPathBetween(graph, source, target);
		movement.addAll(path.getEdgeList());
	}

	public void updateInitPoint(Point2D xRobot) {
		Vertex sourceV = new Vertex(xRobot);
		graph.addVertex(sourceV);
		Point2D target = findCellCenter(xRobot);
		Vertex targetV = findTheVertex(target);
		DefaultEdge e = graph.addEdge(sourceV, targetV);
		graph.setEdgeWeight(e, size);
	}

	public ArrayList<DefaultEdge> getMovement() {
		return movement;
	}

	public Graph<Vertex, DefaultEdge> getGraph() {
		return graph;
	}

	public float computeDistance(Graph<Vertex, DefaultEdge> graph, Point2D source, Point2D target) {
		Vertex sourceV = findTheVertex(source);
		Vertex targetV = findTheVertex(target);
		GraphPath<Vertex, DefaultEdge> path = DijkstraShortestPath.findPathBetween(graph, sourceV, targetV);
		ArrayList<DefaultEdge> tempPath = new ArrayList<DefaultEdge>();
		tempPath.addAll(path.getEdgeList());
		Iterator<DefaultEdge> it = tempPath.iterator();
		DefaultEdge edge;
		float length = 0;
		while(it.hasNext()) {
			edge = it.next();
			length += graph.getEdgeWeight(edge);
		}
		return length;
	}
}
