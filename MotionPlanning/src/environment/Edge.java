package environment;

import java.awt.geom.Point2D;

public class Edge {
	float length;

	public Edge(Vertex a, Vertex b) {
		this.length = distance(a.getPoint(), b.getPoint());
	}
		
	private float distance(Point2D p, Point2D q) 
	{
		return (float) Math.sqrt(Math.pow(p.getX() - q.getX(), 2) + Math.pow(p.getY() - q.getY(), 2));
	}
}
