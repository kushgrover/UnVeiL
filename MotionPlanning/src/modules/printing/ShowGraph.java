package modules.printing;

import java.awt.Graphics;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.Iterator;

import javax.swing.JFrame;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import ij.gui.Plot;
import modules.motionPlanner.Environment;
import modules.motionPlanner.Vertex;

public class ShowGraph extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	Graph<Vertex, DefaultEdge> graph;
	Environment env;
	
	
	
	public ShowGraph(Graph<Vertex, DefaultEdge> graph, Environment env)
	{
		this.graph = graph;
		this.env = env;
	}
	
	
	@Override
	public void paint(Graphics l) 
	{
		Plot plot = new Plot("plot", "", "");
		plot.setLimits(env.getBoundsX()[0], env.getBoundsX()[1], env.getBoundsY()[0], env.getBoundsY()[1]);
		
		Iterator<Path2D> i = env.getObstacles().iterator();

		while(i.hasNext())
		{
			Path2D rect = i.next();
			PathIterator it = rect.getPathIterator(null);
			float[] coords= new float[] {0f, 0f}, from = new float[] {0f, 0f}, to;
			while(! it.isDone()) {
				switch(it.currentSegment(coords)) {
					case(PathIterator.SEG_MOVETO):
						from = coords;
						break;
					case(PathIterator.SEG_LINETO):
						to = coords;
						plot.add("line", new double[] {from[0], to[0]}, new double[] {from[1], to[1]});
						break;
					default:
						break;
				}
			}
		}
		
		Iterator<DefaultEdge> ite = graph.edgeSet().iterator();
		
		while(ite.hasNext())
		{
			DefaultEdge nextEdge = ite.next();
			Vertex source = graph.getEdgeSource(nextEdge);
			Vertex target = graph.getEdgeTarget(nextEdge);
			plot.add("line", new double[] {source.getPoint().getX(), target.getPoint().getX()}, new double[] {source.getPoint().getY(), target.getPoint().getY()});
		}
//		plot.makeHighResolution("", 5.0f, true, true);
		plot.show();
	}
	
	
}
