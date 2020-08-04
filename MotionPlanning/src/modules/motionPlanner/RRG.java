package modules.motionPlanner;



import java.awt.geom.Point2D;
import gnu.trove.TIntProcedure;
import modules.PlanningSettings;
import modules.printing.ShowGraph;
import net.sf.javabdd.BDD;
import transitionSystem.ProductAutomaton;

import java.util.ArrayList;
import org.jgrapht.Graph;
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
		
//		this.addPoint(init);
	}
	
	
	public BDD buildGraph(BDD fromStates, BDD toStates, Point2D xRand2D, ProductAutomaton productAutomaton) 
	{
		BDD transitions 			= ProductAutomaton.factory.zero();
		
		
		// Point
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
//					System.out.println("Sampled Transition: " + xNearest2D.toString() + " ---> " + xNew2D.toString());
//					try {
//						System.out.println(Environment.getLabelling().getLabel(xNearest2D).toString() + " ---> " + Environment.getLabelling().getLabel(xNew2D).toString());
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
										
										BDD transition;
										try {
											transition 		= productAutomaton.changePreSystemVarsToPostSystemVars(Environment.getLabelling().getLabel(xNew2D));
											transition 		= transition.and(Environment.getLabelling().getLabel(neighbour2D));
											transitions.orWith(transition);
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
					tree.add(rect, numPoints);
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


	void addPoint(Point2D p) 
	{
		graph.addVertex(new Vertex(p));
		Rectangle rect 	= new Rectangle((float) p.getX(), (float) p.getY(), (float) p.getX(), (float) p.getY());
		Point xPoint 	= new Point((float) p.getX(), (float) p.getY());
		treePoints.add(xPoint);
		tree.add(rect, numPoints);
		numPoints++;
	}
	
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


	public void setStartingPoint(Point2D init) {
		addPoint(init);
	}
	
	



	public void plotGraph() {
		
		
		new ShowGraph(graph, env).setVisible(true);;
	}
	  
	
//	public static void main(String[] args)
//	{
//		RTree rand = new RTree();
//		rand.init(null);
//		rand.add(new Rectangle(0.1f, 0.1f, 0.1f, 0.1f), 0);
//		rand.add(new Rectangle(1f, 1f, 1f, 1f), 1);
//		rand.add(new Rectangle(0f, 1f, 0f, 1f), 2);
//		rand.add(new Rectangle(1f, 0f, 1f, 0f), 3);
//		rand.add(new Rectangle(0.5f, 0.5f, 0.5f, 0.5f), 4);
//		rand.nearestN(new Point(0.2f, 0.2f), 
//			new TIntProcedure() 
//			{
//
//				@Override
//				public boolean execute(int i) {
//					System.out.println(i);
//					return true;
//				}
//			}, 100, java.lang.Float.POSITIVE_INFINITY);
//	}
	
	
//	
//	String envFile="/home/kush/Projects/robotmotionplanning/MotionPlanning/Examples/Example1/environment.env";
//	EnvironmentReader r = new EnvironmentReader(envFile);
//	# Define state-space, start and goal'
//    float[] boundsX = new float[]{0.0f, 1.0f};
//    float[] boundsY = new float[]{0.0f, 1.0f};
//    obsList = ([(0, 0), (0, 0.1), (0.1, 0.1), (0.1, 0)],[(0.5, 0.5), (0.7, 0.5), (0.7, 0.8)])
//    Environment env=new Environment(new ArrayList<Path2D>(), boundsX, boundsY, null);
//    Point2D.Float xInit = env.sampleFree();
//    Point2D.Float xGoal = env.sampleFree();
//    # Generate RRG
//    System.out.print(xInit.toString());
//    RRG a = new RRG(env, xInit, 0.5f);	
//	    # Build graph
//	    tic = timeit.default_timer()
//	    a.buildGraph(xGoal);
//	    for(int i=0;i<1;i++) {
//	    	a.buildGraph(env.sampleFree());
//	    }

	    
	    
//	    Iterator<DefaultEdge> ite = a.graph.edgeSet().iterator();
//	    DefaultEdge next;
//	    while(ite.hasNext()) {
//	    	next=ite.next();
//	    	
//	    }
//	    toc = timeit.default_timer()
//	    print(toc-tic, 'sec ellapsed')
//	    # Plan path
//	    cost, path = nx.single_source_dijkstra(a.G,x_init,x_goal)
//	    edges = list(zip(path,path[1:]))
//	    # Plot
//	    fig, axs = plt.subplots()
//	    axs.set_aspect('equal', 'datalim')
//	    nx.draw(a.G, nx.get_node_attributes(a.G, 'pos'), node_size=30)
//	    nx.draw_networkx_edges(a.G, nx.get_node_attributes(a.G, 'pos'), edgelist=edges, edge_color='r')
//	    plt.plot(*workspace.bounds.exterior.xy)
//	    for pol in workspace.obstacles:
//	        xs, ys = pol.exterior.xy
//	        axs.fill(xs, ys)
//	    plt.show()
//	}
}
