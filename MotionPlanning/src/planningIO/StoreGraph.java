package planningIO;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import environment.Vertex;

public class StoreGraph 
{

	public StoreGraph(Graph<Vertex, DefaultEdge> graph, String filename) throws IOException {
		BufferedWriter writer 		= new BufferedWriter(new FileWriter("/home/kush/Projects/robotmotionplanning/MotionPlanning/temp/"+filename));
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
}
