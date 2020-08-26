package modules.motionPlanner;



import java.awt.geom.Point2D;
import java.io.OutputStream;
import java.io.PrintStream;

import gnu.trove.TIntProcedure;
import modules.printing.ShowGraph;
import net.sf.javabdd.BDD;
import transitionSystem.ProductAutomaton;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import com.infomatiq.jsi.Point;
import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.SpatialIndex;
import com.infomatiq.jsi.rtree.RTree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RRG 
{
	@SuppressWarnings("unused")
	private final Logger log 	= LoggerFactory.getLogger(RRG.class);
	float eta;
	Environment env;
	double gamma;
	SpatialIndex tree;
	Graph<Vertex, DefaultEdge> graph;
	ArrayList<Point> treePoints;
	int numPoints;
	Vertex initVertex;
	
	public RRG(Environment env) 
	{
		
		this.env 				= env;
		this.eta 				= (float) PlanningSettings.get("planning.eta");
		float[] sub 			= new float[] {env.getBoundsX()[1]-env.getBoundsX()[0], env.getBoundsY()[1]-env.getBoundsY()[0]};
		this.gamma 				= 2.0 * Math.pow(1.5,0.5) * Math.pow(sub[0]*sub[1]/Math.PI,0.5);
		this.graph 				= new SimpleGraph<Vertex, DefaultEdge>(DefaultEdge.class);
		treePoints 				= new ArrayList<Point>();
		numPoints				= 0;
		
		this.tree 				= new RTree();
		tree.init(null);

	}
	
	
	public BDD buildGraph(BDD fromStates, BDD toStates, Point2D xRand2D, ProductAutomaton productAutomaton) 
	{
		BDD transitions 			= ProductAutomaton.factory.zero();
		
		Point xRand					= convertPoint2DToPoint(xRand2D);
		
		TIntProcedure kush			= new TIntProcedure()
		{ 
			public boolean execute(int i) 
			{
				Point xNearest		= treePoints.get(i);
				Point2D xNearest2D	= convertPointToPoint2D(xNearest);
				try 
				{
					if(Environment.getLabelling().getLabel(xNearest2D).and(fromStates).isZero())
					{
						return false;
					}
				} catch (Exception e) 
				{
					e.printStackTrace();
				}
				
				Point2D xNew2D		= steer(xNearest2D, xRand2D);
				Point xNew			= convertPoint2DToPoint(xNew2D);
				
				try 
				{
					if(Environment.getLabelling().getLabel(xNew2D).and(toStates).isZero())
					{
						return false;
					}
				} catch (Exception e1) 
				{
					e1.printStackTrace();
				}
				
				if(env.collisionFree(xNearest2D, xNew2D))
				{	
//					tempCount++;
//					System.out.println("Sampled Transition: " + xNearest2D.toString() + " ---> " + xNew2D.toString());
//					try {
//						if(! Environment.getLabelling().getLabel(xNearest2D).equals(Environment.getLabelling().getLabel(xNew2D))) {
//							System.out.println(Environment.getLabelling().getLabel(xNearest2D).toString() + " ---> " + Environment.getLabelling().getLabel(xNew2D).toString());
//						}
//					} catch (Exception e1) {
//						e1.printStackTrace();
//					}
					
					final float radius;
					if(numPoints > 1) 
					{
						radius				= (float) Math.min(gamma * Math.pow(Math.log(numPoints)/(numPoints), (0.5)), eta);
					} else 
					{
						radius				= 0.1f;
					}
					final Vertex source	= new Vertex(xNew2D);
					graph.addVertex(source);
					
					tree.nearestN(xNew, 
							new TIntProcedure() 
							{
								public boolean execute(int i) 
								{
									Point neighbour			= treePoints.get(i);
									Point2D neighbour2D		= convertPointToPoint2D(neighbour);

									if(neighbour2D.equals(xNew2D)) return true;
									
									if( distance(xNew, neighbour) <= radius		&&		env.collisionFree(xNew2D, neighbour2D) ) 
									{
										Vertex target		= new Vertex(neighbour2D);
										graph.addVertex(target);
										graph.addEdge(source, target);
										
//										System.out.println("Added Transition: " + xNew.toString() + " ---> " + neighbour.toString());
										
										BDD transition, transition2;
										try {
											transition 		= productAutomaton.changePreSystemVarsToPostSystemVars(Environment.getLabelling().getLabel(xNew2D));
											transition 		= transition.and(Environment.getLabelling().getLabel(neighbour2D));
											transition2 	= Environment.getLabelling().getLabel(xNew2D);
											transition2 	= transition2.and(productAutomaton.changePreSystemVarsToPostSystemVars(Environment.getLabelling().getLabel(neighbour2D)));
											transitions.orWith(transition);
											transitions.orWith(transition2);
										} catch (Exception e)
										{
											e.printStackTrace();
										}
									}
									return true;
								}
							}, 
							100, java.lang.Float.POSITIVE_INFINITY);
					Rectangle rect 			= new Rectangle(xNew.x, xNew.y, xNew.x, xNew.y);
					PrintStream out = System.out;
					System.setOut(new PrintStream(OutputStream.nullOutputStream()));
					tree.add(rect, numPoints);
					System.setOut(out);
					treePoints.add(xNew);
					numPoints++;
				}
		        return true;
		    }
		};
		tree.nearest(xRand, kush, java.lang.Float.POSITIVE_INFINITY);
		
		return transitions;
	}
	

	private float distance(Point p, Point q) 
	{
		return (float) Math.sqrt(Math.pow(p.x - q.x, 2)+Math.pow(p.y - q.y, 2));
	}


//	void addPoint(Point2D p) 
//	{
		
//	}
	
	Point2D.Float arrayToPoint(double[][] x) 
	{
		return new Point2D.Float((float) x[0][0], (float) x[0][1]);
	}
	
	private Point2D convertPointToPoint2D(Point p) 
	{
		return new Point2D.Float(p.x,p.y);
	}
	
	private Point convertPoint2DToPoint(Point2D p) 
	{
		return new Point((float) p.getX(), (float)p.getY());
	}
	
	public Point2D steer(Point2D from, Point2D to) 
	{
		float d	= (float) from.distance(to);
		if(d <= eta) 
		{
			return to;
		} else 
		{
			Point2D temp = new Point2D.Float((float) (from.getX()+((eta-0.00001)*(to.getX()-from.getX())/d)), (float) (from.getY()+((eta-0.00001)*(to.getY()-from.getY())/d)));
			if((float) from.distance(temp)>0.051f) {
				System.out.println("I am fucked");
			}
			return temp;
		}
	}

	public BDD sample(BDD fromStates, BDD toStates, ProductAutomaton productAutomaton) throws Exception {
		Point2D.Float p;
		BDD transition;
		int i 		= 0;
		while(i < ProductAutomaton.threshold)
		{
			i++;
			p 			= env.sample();
			transition 	= buildGraph(fromStates, toStates, p, productAutomaton);
			if(! transition.isZero())
			{
				return transition;
			}
		}
		return null;
	}


	public BDD sample(BDD currentStates, ProductAutomaton productAutomaton) {
		Point2D.Float p;
		BDD transition;
		int i 		= 0;
		while(i < ProductAutomaton.threshold)
		{
			i++;
			p 			= env.sample();
			transition 	= buildGraph(currentStates, ProductAutomaton.factory.one(), p, productAutomaton);
			if(! transition.isZero())
			{
				return transition;
			}
		}
		return null;
	}


	public BDD sample(ProductAutomaton productAutomaton) {
		Point2D.Float p;
		BDD transition;
		while(true)
		{
			p 			= env.sample();
			transition 	= buildGraph(ProductAutomaton.factory.one(), ProductAutomaton.factory.one(), p, productAutomaton);
			if(! transition.isZero())
			{
				return transition;
			}
		}
//		return null;
	}


	public void setStartingPoint(Point2D p) {
		this.initVertex = new Vertex(p);
		graph.addVertex(initVertex);
		Rectangle rect 	= new Rectangle((float) p.getX(), (float) p.getY(), (float) p.getX(), (float) p.getY());
		Point xPoint 	= new Point((float) p.getX(), (float) p.getY());
		treePoints.add(xPoint);
		PrintStream out = System.out;
		System.setOut(new PrintStream(OutputStream.nullOutputStream()));
		tree.add(rect, numPoints);
		System.setOut(out);
		numPoints++;
	}
	
	



	public void plotGraph() 
	{
		new ShowGraph(graph, env).setVisible(true);;
	}
	  
	
	public Graph<Vertex, DefaultEdge> getGraph()
	{
		return graph;
	}


	public void liftPath(ArrayList<BDD> path) {
//		Vertex source = initVertex;
//		Vertex dest;
//		Iterator<BDD> it = path.iterator();
//		BDD nextState;
//		while(it.hasNext())
//		{
//			nextState = it.next();
//			dest = findAVertex(nextState);
//			GraphPath<Vertex, DefaultEdge> rrgPath = DijkstraShortestPath.findPathBetween(graph, source, dest); 
//
//		}
		
	}


//	private Vertex findAVertex(BDD nextState) {
//		graph.vertexSet()
//	}
	    
	    
}
