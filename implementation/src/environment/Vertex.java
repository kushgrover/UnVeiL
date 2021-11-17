package environment;

import java.awt.geom.Point2D;

import net.sf.javabdd.BDD;
import settings.PlanningException;

public class Vertex {
	Point2D point;
	BDD label = null;
	
	public Vertex(Point2D point) {
		this.point = point;
		try {
			this.label = Environment.getLabelling().getLabel(point);
		} catch (NullPointerException ignored) {
		} catch (PlanningException e) {
			e.printStackTrace();
		}
	}
	
	public Point2D getPoint() {
		return point;
	}
	
	public BDD getLabel() {
		return label;
	}
}
