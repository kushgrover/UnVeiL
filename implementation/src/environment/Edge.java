package environment;

import java.awt.geom.Point2D;

public class Edge {
	float length;

	public Edge(Vertex a, Vertex b) {
		this.length = distance(a.getPoint(), b.getPoint());
	}
		
	private static float distance(Point2D p, Point2D q)
	{
		return (float) Math.sqrt(StrictMath.pow(p.getX() - q.getX(), 2) + StrictMath.pow(p.getY() - q.getY(), 2));
	}
}
