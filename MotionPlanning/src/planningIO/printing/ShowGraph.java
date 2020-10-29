package planningIO.printing;

import java.awt.Graphics;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import environment.Environment;
import environment.Vertex;
import ij.gui.Plot;

public class ShowGraph extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	Graph<Vertex, DefaultEdge> graph;
	Environment env;
	List<Point2D> path;
	List<DefaultEdge> finalPath;
	
	
	public ShowGraph(Graph<Vertex, DefaultEdge> graph, Environment env, List<Point2D> path, List<DefaultEdge> finalPath)
	{
		this.graph = graph;
		this.env = env;
		this.path = path;
		this.finalPath = finalPath;
	}
	
	
	@Override
	public void paint(Graphics l) 
	{
		Plot plot = new Plot("plot", "", "");
		plot.setLimits(env.getBoundsX()[0], env.getBoundsX()[1], env.getBoundsY()[0], env.getBoundsY()[1]);
		
		
		DefaultEdge nextEdge;
		Vertex source, target;
		
		// Whole Graph
		plot.setColor("black");
		Iterator<DefaultEdge> ite = graph.edgeSet().iterator();
		while(ite.hasNext())
		{
			nextEdge = ite.next();
			source = graph.getEdgeSource(nextEdge);
			target = graph.getEdgeTarget(nextEdge);
			plot.add("line", new double[] {source.getPoint().getX(), target.getPoint().getX()}, new double[] {source.getPoint().getY(), target.getPoint().getY()});
		}

		// Robot Movement
		plot.setColor("green");
		Iterator<Point2D> iter = path.iterator();
		Point2D previousPoint = null, nextPoint = null;
		while(iter.hasNext())
		{
			if(nextPoint != null) {
				previousPoint = (Point2D) nextPoint.clone();
			}
			nextPoint = iter.next();
			if(previousPoint != null) 
			{
				plot.add("line", new double[] {nextPoint.getX(), previousPoint.getX()}, new double[] {nextPoint.getY(), previousPoint.getY()});
			}
		}
		
		// Final Path
		plot.setColor("pink");
		ite = finalPath.iterator();
		while(ite.hasNext())
		{
			nextEdge = ite.next();
			source = graph.getEdgeSource(nextEdge);
			target = graph.getEdgeTarget(nextEdge);
			plot.add("line", new double[] {source.getPoint().getX(), target.getPoint().getX()}, new double[] {source.getPoint().getY(), target.getPoint().getY()});
		}

		
		
		//Obstacles
		plot.setColor("blue");
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
						plot.add("line", new double[] {from[0], to[0]}, new double[] {from[1], to[1]});
						from = to.clone();
						break;
					default:
						break;
				}
				it.next();
			}
		}
		
		// Label
		plot.setColor("red");
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
						plot.add("line", new double[] {from[0], to[0]}, new double[] {from[1], to[1]});
						from = to.clone();
						break;
					default:
						break;
				}
				it.next();
			}
		}
		
		
//		plot.makeHighResolution("", 5.0f, true, true);
		plot.show();
		
		plot.dispose();
	}
	
	
}
