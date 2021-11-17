package modules;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import abstraction.ProductAutomaton;
import environment.EdgeSupplier;
import environment.Environment;
import environment.Vertex;
import environment.VertexSupplier;
import net.sf.javabdd.BDD;
import settings.PlanningSettings;

public class KnownGrid extends Grid {

	Graph<Vertex, DefaultEdge> graph;
	private final ArrayList<DefaultEdge> movement;
	Environment env;

	public KnownGrid(Environment env, float size) 
	{
		super(env, size);
		this.env = env;
		this.graph = new SimpleDirectedGraph<>(new VertexSupplier(), new EdgeSupplier(), true);
		this.movement = new ArrayList<>();
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
		Point2D down   = new Point2D.Float((float) centre.getX(), (float) centre.getY()-size);
		Point2D right   = new Point2D.Float((float) centre.getX()+size, (float) centre.getY());
		Point2D up   = new Point2D.Float((float) centre.getX(), (float) centre.getY()+size);

		Vertex leftV   = findTheVertex(left);
		Vertex downV   = findTheVertex(down);
		Vertex rightV   = findTheVertex(right);
		Vertex upV   = findTheVertex(up);
		Vertex centreV = findTheVertex(centre);
		int i;
		int j;
		try {
			i = clampX(findCell(left)[0]);
			j = clampY(findCell(left)[1]);
			if (grid[i][j] == 1) {
				DefaultEdge edge = graph.addEdge(leftV, centreV);
				DefaultEdge edgeR = graph.addEdge(centreV, leftV);
				if (edge != null) {
					graph.setEdgeWeight(edge, size);
					graph.setEdgeWeight(edgeR, size);
				}
			}
		}
		catch (NullPointerException ignored){ }

		try {
			i = clampX(findCell(down)[0]);
			j = clampY(findCell(down)[1]);
			if (grid[i][j] == 1) {
				DefaultEdge edge = graph.addEdge(downV, centreV);
				DefaultEdge edgeR = graph.addEdge(centreV, downV);
				if (edge != null) {
					graph.setEdgeWeight(edge, size);
					graph.setEdgeWeight(edgeR, size);
				}
			}
		}
		catch (NullPointerException ignored){ }

		try {
			i = clampX(findCell(right)[0]);
			j = clampY(findCell(right)[1]);
			if (grid[i][j] == 1) {
				DefaultEdge edge = graph.addEdge(rightV, centreV);
				DefaultEdge edgeR = graph.addEdge(centreV, rightV);
				if (edge != null) {
					graph.setEdgeWeight(edge, size);
					graph.setEdgeWeight(edgeR, size);
				}
			}
		}
		catch (NullPointerException ignored){ }

		try {
			i = clampX(findCell(up)[0]);
			j = clampY(findCell(up)[1]);
			if (grid[i][j] == 1) {
				DefaultEdge edge = graph.addEdge(upV, centreV);
				DefaultEdge edgeR = graph.addEdge(centreV, upV);
				if (edge != null) {
					graph.setEdgeWeight(edge, size);
					graph.setEdgeWeight(edgeR, size);
				}
			}
		}
		catch (NullPointerException ignored){ }

	}
	
	/*
	 * know the grid in the sensing radius
	 */
	@Override
	public void knowDiscretization(Environment env, 
			ProductAutomaton productAutomaton, 
			Point2D currentPosition, 
			float sensingRadius) throws settings.PlanningException {
		for(int i = 0; i<numX; i++) {
			for(int j=0; j<numY; j++) {
				if(cellInsideSensingRadius(i, j, currentPosition, sensingRadius)) {
					Point2D tempPoint = findCentre(i,j);
					if(env.collisionFreeFromOpaqueObstacles(currentPosition, tempPoint) && isObstacleFreeCell(env, i, j)) {
						BDD label = Environment.getLabelling().getLabel(tempPoint);
						updateDiscretization(tempPoint, 1, label);
					}
					else if(! isObstacleFreeCell(env, i, j)) {
						updateDiscretization(tempPoint, 3, null);
					}
				}
			}
		}
		if((boolean) PlanningSettings.get("firstExplThenPlan")) {
			for(int i=0; i<numX; i++) {
				for(int j=0; j<numY; j++) {
					if(cellInsideSensingRadius(i, j, currentPosition, sensingRadius)) {
						Point2D tempPoint = findCentre(i,j);
						if(grid[i][j] == 1) {
							updateGraph(tempPoint);
						}
					}
				}
			}
		}
	}

	boolean isObstacleFreeCell(Environment env, int i, int j) {
		float x = (float) i * size;
		float y = (float) j * size;
		if(! env.obstacleFreeAll(new Point2D.Float(x, y))) {
			return false;
		}
		if(! env.obstacleFreeAll(new Point2D.Float(x+size, y))) {
			return false;
		}
		if(! env.obstacleFreeAll(new Point2D.Float(x, y+size))) {
			return false;
		}
		return env.obstacleFreeAll(new Point2D.Float(x + size, y + size));
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
			if (Math.abs(temp.getPoint().getX() - p.getX()) < 0.00001 && Math.abs(temp.getPoint().getY() - p.getY()) < 0.00001) {
				return temp;
			}
		}
//		Vertex v = new Vertex(p);
//		graph.addVertex(v);
		return null;
	}

	@Override
	Pair<Point2D, Pair<Integer, Float>> findBestFrontier(ArrayList<ArrayList<int[]>> frontiers, Point2D xRobot){
		Point2D bestPoint = findFrontierCenter(frontiers.get(0));
		float bestIG = 0;
		int bestIndex = 0;
		for(int i=0;i<frontiers.size();i++) {
			Point2D currentPoint = findFrontierCenter(frontiers.get(i));
			if(currentPoint.equals(xRobot)){
				continue;
			}
			float currentIG = (float) frontiers.get(i).size() / computeDistance(currentPoint, xRobot);
			if(currentIG > bestIG) {
				bestPoint = currentPoint;
				bestIG = currentIG;
				bestIndex = i;
			}
		}
		return new Pair<>(bestPoint, new Pair<>(bestIndex, bestIG));
	}



	public float findAPath(Point2D currentPosition, Point2D newPosition) {
		return findAPath(findCell(currentPosition), findCell(newPosition));
	}


	private float findAPath(int[] cell1, int[] cell2) {
		Point2D source = findCentre(cell1[0], cell1[1]);
		Point2D target = findCentre(cell2[0], cell2[1]);
		Vertex sourceV = findTheVertex(source);
		Vertex targetV = findTheVertex(target);

		GraphPath<Vertex, DefaultEdge> path = DijkstraShortestPath.findPathBetween(graph, sourceV, targetV);
		List<DefaultEdge> pathList = new ArrayList<>(path.getEdgeList());
		Iterator<DefaultEdge> i = pathList.iterator();
		float pathLength = 0;
		while(i.hasNext()) {
			DefaultEdge nextEdge = i.next();
			pathLength += (float) graph.getEdgeWeight(nextEdge);
		}
		return pathLength;
	}

	/*
	 * finds the best move according to the frontiers
	 */
	@Override
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

		if((boolean) PlanningSettings.get("debug")) {
			System.out.println("Used frontier: " + bestFrontier.getFirst() + " with IG = " + bestFrontier.getSecond().getSecond());
		}
		return new Pair<>(bestFrontier.getFirst(), -1);
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
		DefaultEdge eR = graph.addEdge(targetV, sourceV);
		graph.setEdgeWeight(e, size);
		graph.setEdgeWeight(eR, size);
	}

	public ArrayList<DefaultEdge> getMovement() {
		return movement;
	}

	public Graph<Vertex, DefaultEdge> getGraph() {
		return graph;
	}

	public float computeDistance(Point2D source, Point2D target) {
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
}
