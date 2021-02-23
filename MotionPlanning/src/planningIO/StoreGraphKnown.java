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

public class StoreGraphKnown {

	public float movementLength, remainingPathLength;

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
	
	
	public void outputGraph(Graph<Vertex, DefaultEdge> graph, String fileName) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter("temp/"+fileName+".csv"));
		
		writer.write("x1,y1,x2,y2\n");
		
		DefaultEdge nextEdge;
		Vertex source, target;
		
		// Whole Graph
		Iterator<DefaultEdge> ite = graph.edgeSet().iterator();
		while(ite.hasNext())
		{
			nextEdge = ite.next();
			source = graph.getEdgeSource(nextEdge);
			target = graph.getEdgeTarget(nextEdge);
			writer.write(source.getPoint().getX() + "," +source.getPoint().getY() + "," + target.getPoint().getX() + "," + target.getPoint().getY() + "\n");
		}
		
		writer.close();
	}
	
	public void outputFinalPath(Graph<Vertex, DefaultEdge> graph, List<DefaultEdge> finalPath, String fileName) throws IOException {
		BufferedWriter writer 		= new BufferedWriter(new FileWriter("temp/"+fileName+"-finalpath.csv"));
		
		writer.write("x1,y1,x2,y2\n");
		
		DefaultEdge nextEdge;
		Vertex source, target;
		remainingPathLength = 0;
		
		Iterator<DefaultEdge> i = finalPath.iterator();
		while(i.hasNext())
		{
			nextEdge = i.next();
			remainingPathLength += graph.getEdgeWeight(nextEdge);
			source = graph.getEdgeSource(nextEdge);
			target = graph.getEdgeTarget(nextEdge);
			writer.write(source.getPoint().getX() + "," + source.getPoint().getY() + "," + target.getPoint().getX() + "," + target.getPoint().getY() + "\n");
		}
		writer.close();
	}
	
	public void outputMovement(Graph<Vertex, DefaultEdge> graph, List<DefaultEdge> movement, String fileName) throws IOException {
		
		if(graph == null) return;
		
		BufferedWriter writer 		= new BufferedWriter(new FileWriter("temp/" + fileName + "-movement.csv"));
		
		writer.write("x1,y1,x2,y2\n");
		
		DefaultEdge nextEdge;
		Vertex source, target;
		movementLength = 0;
		
		if(movement != null) {
			Iterator<DefaultEdge> i = movement.iterator();
			while(i.hasNext())
			{
				nextEdge = i.next();
				movementLength += graph.getEdgeWeight(nextEdge);
				source = graph.getEdgeSource(nextEdge);
				target = graph.getEdgeTarget(nextEdge);
				writer.write(source.getPoint().getX() + "," + source.getPoint().getY() + "," + target.getPoint().getX() + "," + target.getPoint().getY() + "\n");
			}
			writer.close();
		}
	}
	
	public void outputObstacles(Environment env) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter("temp/obstacles.csv"));
		writer.write("x1,y1,x2,y2\n");
		writer.write("0.0,0.0,6.0,0.0\n");
		writer.write("6.0,0.0,6.0,3.0\n");
		writer.write("6.0,3.0,0.0,3.0\n");
		writer.write("0.0,3.0,0.0,0.0\n");
		
		//Obstacles
		Iterator<Path2D> i = env.getObstacles().iterator();
		while(i.hasNext())
		{
			Path2D rect = i.next();
			PathIterator it = rect.getPathIterator(null);
			float[] coords = new float[] {0f, 0f}, from = new float[] {0f, 0f}, to;
			while(! it.isDone()) {
				switch(it.currentSegment(coords)) {
					case(PathIterator.SEG_MOVETO):
						from = coords.clone();
						break;
					case(PathIterator.SEG_LINETO):
						to = coords.clone();
						writer.write(from[0] + "," + from[1] + "," + to[0] + "," + to[1] + "\n");
//						plot.add("line", new double[] {from[0], to[0]}, new double[] {from[1], to[1]});
						from = to.clone();
						break;
					default:
						break;
				}
				it.next();
			}
		}
		writer.close();
		
		
		// Label
		writer 		= new BufferedWriter(new FileWriter("/home/kush/Projects/robotmotionplanning/MotionPlanning/temp/labels.csv"));
		writer.write("x1,y1,x2,y2\n");
		i = Environment.getLabelling().getAreas().iterator();
		while(i.hasNext())
		{
			Path2D rect = i.next();
			PathIterator it = rect.getPathIterator(null);
			float[] coords = new float[] {0f, 0f}, from = new float[] {0f, 0f}, to;
			while(! it.isDone()) {
				switch(it.currentSegment(coords)) {
					case(PathIterator.SEG_MOVETO):
						from = coords.clone();
						break;
					case(PathIterator.SEG_LINETO):
						to = coords.clone();
						writer.write(from[0] + "," + from[1] + "," + to[0] + "," + to[1] + "\n");
//						plot.add("line", new double[] {from[0], to[0]}, new double[] {from[1], to[1]});
						from = to.clone();
						break;
					default:
						break;
				}
				it.next();
			}
		}
		writer.close();
	}


}
