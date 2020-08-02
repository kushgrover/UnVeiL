package modules.motionPlanner;

import java.awt.geom.Point2D;

import net.sf.javabdd.BDD;

public class Vertex {
	Point2D point;
	BDD label;
	
	public Vertex(Point2D point) {
		this.point=point;
		try {
			this.label=Environment.getLabelling().getLabel(point);
		} catch (NullPointerException e) {
			this.label=null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
