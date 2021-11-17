package planningIO;

import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import environment.Environment;
import environment.Vertex;
import settings.PlanningSettings;

public final class StoreGraphKnown {

	public float movementLength = 0.0F;
	public float remainingPathLength = 0.0F;

	public StoreGraphKnown(Environment env, Graph<Vertex, DefaultEdge> graphRRG, Graph<Vertex, DefaultEdge> graphMovement, List<DefaultEdge> finalPath, List<DefaultEdge> movement, String fileName) {
		try {
			outputGraph(graphRRG, fileName);
			outputObstacles(env);
			outputMovement(graphMovement, movement, fileName);
			outputFinalPath(graphRRG, finalPath, fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public StoreGraphKnown(Graph<Vertex, DefaultEdge> graphRRG, Graph<Vertex, DefaultEdge> graphMovement, List<DefaultEdge> movement, String fileName) {
		try {
			outputGraph(graphRRG, fileName);
			outputMovement(graphMovement, movement, fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void outputGraph(Graph<Vertex, DefaultEdge> graph, String fileName) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileName + ".csv"));
		
		writer.write("x1,y1,x2,y2\n");

		// Whole Graph
		for (DefaultEdge nextEdge : graph.edgeSet()) {
			Vertex source = graph.getEdgeSource(nextEdge);
			Vertex target = graph.getEdgeTarget(nextEdge);
			writer.write(source.getPoint().getX() + "," + source.getPoint().getY() + ',' + target.getPoint().getX() + ',' + target.getPoint().getY() + '\n');
		}
		
		writer.close();
	}
	
	public void outputFinalPath(Graph<Vertex, DefaultEdge> graph, List<DefaultEdge> finalPath, String fileName) throws IOException {
		BufferedWriter writer 		= new BufferedWriter(new FileWriter(fileName+"-finalpath.csv"));
		
		writer.write("x1,y1,x2,y2\n");

//		remainingPathLength = 0;

		for (DefaultEdge nextEdge : finalPath) {
			remainingPathLength += (float) graph.getEdgeWeight(nextEdge);
			Vertex source = graph.getEdgeSource(nextEdge);
			Vertex target = graph.getEdgeTarget(nextEdge);
			writer.write(source.getPoint().getX() + "," + source.getPoint().getY() + ',' + target.getPoint().getX() + ',' + target.getPoint().getY() + '\n');
		}
		writer.close();
	}
	
	public void outputMovement(Graph<Vertex, DefaultEdge> graph, List<DefaultEdge> movement, String fileName) throws IOException {
		
		if(graph == null) {
			return;
		}
		
		BufferedWriter writer 		= new BufferedWriter(new FileWriter(fileName + "-movement.csv"));
		
		writer.write("x1,y1,x2,y2\n");

		movementLength = 0;
		
		if(movement != null) {
			for (DefaultEdge nextEdge : movement) {
				movementLength += (float) graph.getEdgeWeight(nextEdge);
				Vertex source = graph.getEdgeSource(nextEdge);
				Vertex target = graph.getEdgeTarget(nextEdge);
				writer.write(source.getPoint().getX() + "," + source.getPoint().getY() + ',' + target.getPoint().getX() + ',' + target.getPoint().getY() + '\n');
			}
			writer.close();
		}
	}
	
	public static void outputObstacles(Environment env) throws IOException {
		String outputDir = (String) PlanningSettings.get("outputDirectory");
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputDir + "obstacles.csv"));
		writer.write("x1,y1,x2,y2\n");
		writer.write("0.0,0.0,6.0,0.0\n");
		writer.write("6.0,0.0,6.0,3.0\n");
		writer.write("6.0,3.0,0.0,3.0\n");
		writer.write("0.0,3.0,0.0,0.0\n");
		
		//Obstacles
		Iterator<Path2D> i = env.getSeeThroughObstacles().iterator();
		while(i.hasNext())
		{
			Path2D rect = i.next();
			PathIterator it = rect.getPathIterator(null);
			float[] coords = {0.0f, 0.0f};
			float[] from = {0.0f, 0.0f};
			while(! it.isDone()) {
				switch (it.currentSegment(coords)) {
					case (PathIterator.SEG_MOVETO) -> from = coords.clone();
					case (PathIterator.SEG_LINETO) -> {
						float[] to = coords.clone();
						writer.write(from[0] + "," + from[1] + ',' + to[0] + ',' + to[1] + '\n');
//						plot.add("line", new double[] {from[0], to[0]}, new double[] {from[1], to[1]});
						from = to.clone();
					}
					default -> {
					}
				}
				it.next();
			}
		}

		i = env.getObstacles().iterator();
		while(i.hasNext())
		{
			Path2D rect = i.next();
			PathIterator it = rect.getPathIterator(null);
			float[] coords = {0.0f, 0.0f};
			float[] from = {0.0f, 0.0f};
			while(! it.isDone()) {
				switch (it.currentSegment(coords)) {
					case (PathIterator.SEG_MOVETO) -> from = coords.clone();
					case (PathIterator.SEG_LINETO) -> {
						float[] to = coords.clone();
						writer.write(from[0] + "," + from[1] + ',' + to[0] + ',' + to[1] + '\n');
//						plot.add("line", new double[] {from[0], to[0]}, new double[] {from[1], to[1]});
						from = to.clone();
					}
					default -> {
					}
				}
				it.next();
			}
		}
		writer.close();
		
		
		// Label
		writer 		= new BufferedWriter(new FileWriter(outputDir + "labels.csv"));
		writer.write("x1,y1,x2,y2\n");
		i = Environment.getLabelling().getAreas().iterator();
		while(i.hasNext())
		{
			Path2D rect = i.next();
			PathIterator it = rect.getPathIterator(null);
			float[] coords = {0.0f, 0.0f};
			float[] from = {0.0f, 0.0f};
			while(! it.isDone()) {
				switch (it.currentSegment(coords)) {
					case (PathIterator.SEG_MOVETO) -> from = coords.clone();
					case (PathIterator.SEG_LINETO) -> {
						float[] to = coords.clone();
						writer.write(from[0] + "," + from[1] + ',' + to[0] + ',' + to[1] + '\n');
//						plot.add("line", new double[] {from[0], to[0]}, new double[] {from[1], to[1]});
						from = to.clone();
					}
					default -> {
					}
				}
				it.next();
			}
		}
		writer.close();
	}


}
