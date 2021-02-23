package environment;

import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;

import settings.PlanningSettings;

public class GenerateRandomEnv {
	public Environment env;

	public GenerateRandomEnv() throws IOException {
		System.out.println("Generating random environment...");
		
		
		Point2D p1, p3, init;
		ArrayList<Path2D> seeThroughObstacles = new ArrayList<Path2D>();
		ArrayList<Path2D> obstacles	= new ArrayList<Path2D>();
		ArrayList<Path2D> labels = new ArrayList<Path2D>();
		ArrayList<String> apList = new ArrayList<String>();
		Path2D tables = new Path2D.Float();
		Path2D bins = new Path2D.Float();

		
		p1 = new Point2D.Float(0f,0f);
		p3 = new Point2D.Float(6f,3f);
		
		init = new Point2D.Float(0.1f,1.5f);
		
		Path2D rect;
		
		rect = new Path2D.Float();
		rect.moveTo(0f, 0.95f);
		rect.lineTo(0.8f, 0.95f);
		rect.lineTo(0.8f, 1.05f);
		rect.lineTo(0f, 1.05f);
		rect.lineTo(0f, 0.95f);
		rect.closePath();
		obstacles.add(rect);
		
		rect = new Path2D.Float();
		rect.moveTo(1.2f, 0.95f);
		rect.lineTo(2f, 0.95f);
		rect.lineTo(2f, 1.05f);
		rect.lineTo(1.2f, 1.05f);
		rect.lineTo(1.2f, 0.95f);
		rect.closePath();
		obstacles.add(rect);
		
		rect = new Path2D.Float();
		rect.moveTo(0f, 1.95f);
		rect.lineTo(0.8f, 1.95f);
		rect.lineTo(0.8f, 2.05f);
		rect.lineTo(0f, 2.05f);
		rect.lineTo(0f, 1.95f);
		rect.closePath();
		obstacles.add(rect);
		
		rect = new Path2D.Float();
		rect.moveTo(1.2f, 1.95f);
		rect.lineTo(2f, 1.95f);
		rect.lineTo(2f, 2.05f);
		rect.lineTo(1.2f, 2.05f);
		rect.lineTo(1.2f, 1.95f);
		rect.closePath();
		obstacles.add(rect);
		
		rect = new Path2D.Float();
		rect.moveTo(2f, 0.95f);
		rect.lineTo(2.8f, 0.95f);
		rect.lineTo(2.8f, 1.05f);
		rect.lineTo(2f, 1.05f);
		rect.lineTo(2f, 0.95f);
		rect.closePath();
		obstacles.add(rect);
		
		rect = new Path2D.Float();
		rect.moveTo(3.2f, 0.95f);
		rect.lineTo(4f, 0.95f);
		rect.lineTo(4f, 1.05f);
		rect.lineTo(3.2f, 1.05f);
		rect.lineTo(3.2f, 0.95f);
		rect.closePath();
		obstacles.add(rect);
		
		rect = new Path2D.Float();
		rect.moveTo(2f, 1.95f);
		rect.lineTo(2.8f, 1.95f);
		rect.lineTo(2.8f, 2.05f);
		rect.lineTo(2f, 2.05f);
		rect.lineTo(2f, 1.95f);
		rect.closePath();
		obstacles.add(rect);
		
		rect = new Path2D.Float();
		rect.moveTo(3.2f, 1.95f);
		rect.lineTo(4f, 1.95f);
		rect.lineTo(4f, 2.05f);
		rect.lineTo(3.2f, 2.05f);
		rect.lineTo(3.2f, 1.95f);
		rect.closePath();
		obstacles.add(rect);
		
		rect = new Path2D.Float();
		rect.moveTo(4f, 0.95f);
		rect.lineTo(4.8f, 0.95f);
		rect.lineTo(4.8f, 1.05f);
		rect.lineTo(4f, 1.05f);
		rect.lineTo(4f, 0.95f);
		rect.closePath();
		obstacles.add(rect);
		
		rect = new Path2D.Float();
		rect.moveTo(5.2f, 0.95f);
		rect.lineTo(6f, 0.95f);
		rect.lineTo(6f, 1.05f);
		rect.lineTo(5.2f, 1.05f);
		rect.lineTo(5.2f, 0.95f);
		rect.closePath();
		obstacles.add(rect);
		
		rect = new Path2D.Float();
		rect.moveTo(4f, 1.95f);
		rect.lineTo(4.8f, 1.95f);
		rect.lineTo(4.8f, 2.05f);
		rect.lineTo(4f, 2.05f);
		rect.lineTo(4f, 1.95f);
		rect.closePath();
		obstacles.add(rect);
		
		rect = new Path2D.Float();
		rect.moveTo(5.2f, 1.95f);
		rect.lineTo(6f, 1.95f);
		rect.lineTo(6f, 2.05f);
		rect.lineTo(5.2f, 2.05f);
		rect.lineTo(5.2f, 1.95f);
		rect.closePath();
		obstacles.add(rect);
		
		rect = new Path2D.Float();
		rect.moveTo(1.95f, 0f);
		rect.lineTo(2.05f, 0f);
		rect.lineTo(2.05f, 0.95f);
		rect.lineTo(1.95f, 0.95f);
		rect.lineTo(1.95f, 0f);
		rect.closePath();
		obstacles.add(rect);
		
		rect = new Path2D.Float();
		rect.moveTo(1.95f, 2.05f);
		rect.lineTo(2.05f, 2.05f);
		rect.lineTo(2.05f, 3f);
		rect.lineTo(1.95f, 3f);
		rect.lineTo(1.95f, 2.05f);
		rect.closePath();
		obstacles.add(rect);

		rect = new Path2D.Float();
		rect.moveTo(3.95f, 0f);
		rect.lineTo(4.05f, 0f);
		rect.lineTo(4.05f, 0.95f);
		rect.lineTo(3.95f, 0.95f);
		rect.lineTo(3.95f, 0f);
		rect.closePath();
		obstacles.add(rect);
		
		rect = new Path2D.Float();
		rect.moveTo(3.95f, 2.05f);
		rect.lineTo(4.05f, 2.05f);
		rect.lineTo(4.05f, 3f);
		rect.lineTo(3.95f, 3f);
		rect.lineTo(3.95f, 2.05f);
		rect.closePath();
		obstacles.add(rect);
		
		// hall
		rect = new Path2D.Float();
		rect.moveTo(0f, 1f);
		rect.lineTo(6f, 1f);
		rect.lineTo(6f, 2f);
		rect.lineTo(0f, 2f);
		rect.lineTo(0f, 1f);
		rect.closePath();
		labels.add(rect);
		apList.add("h");
		
		// room 1
		rect = new Path2D.Float();
		rect.moveTo(0f, 0f);
		rect.lineTo(2f, 0f);
		rect.lineTo(2f, 1f);
		rect.lineTo(0f, 1f);
		rect.lineTo(0f, 0f);
		rect.closePath();
		labels.add(rect);
		apList.add("r1");
		
		// room 2
		rect = new Path2D.Float();
		rect.moveTo(0f, 2f);
		rect.lineTo(2f, 2f);
		rect.lineTo(2f, 3f);
		rect.lineTo(0f, 3f);
		rect.lineTo(0f, 2f);
		rect.closePath();
		labels.add(rect);
		apList.add("r2");
		
		// room 3
		rect = new Path2D.Float();
		rect.moveTo(2f, 0f);
		rect.lineTo(4f, 0f);
		rect.lineTo(4f, 1f);
		rect.lineTo(2f, 1f);
		rect.lineTo(2f, 0f);
		rect.closePath();
		labels.add(rect);
		apList.add("r3");
		
		// room 4
		rect = new Path2D.Float();
		rect.moveTo(2f, 2f);
		rect.lineTo(4f, 2f);
		rect.lineTo(4f, 3f);
		rect.lineTo(2f, 3f);
		rect.lineTo(2f, 2f);
		rect.closePath();
		labels.add(rect);
		apList.add("r4");

		// room 5
		rect = new Path2D.Float();
		rect.moveTo(4f, 0f);
		rect.lineTo(6f, 0f);
		rect.lineTo(6f, 1f);
		rect.lineTo(4f, 1f);
		rect.lineTo(4f, 0f);
		rect.closePath();
		labels.add(rect);
		apList.add("r5");
		
		// room 6
		rect = new Path2D.Float();
		rect.moveTo(4f, 2f);
		rect.lineTo(6f, 2f);
		rect.lineTo(6f, 3f);
		rect.lineTo(4f, 3f);
		rect.lineTo(4f, 2f);
		rect.closePath();
		labels.add(rect);
		apList.add("r6");
		
		// table 1 
		float l = 0.5f;
		float w = 0.3f;
		float x = randomNumber(0, 1.95f-l);
		float y = randomNumber(0, 0.95f-w);
		rect = new Path2D.Float();
		rect.moveTo(x, 		y);
		rect.lineTo(x + l,	y);
		rect.lineTo(x + l, 	y + w);
		rect.lineTo(x, 		y + w);
		rect.lineTo(x, 		y);
		rect.closePath();
		if((boolean) PlanningSettings.get("onlyOpaqueObstacles"))
			obstacles.add(rect);
		else
			seeThroughObstacles.add(rect);
		addTable(tables, x, y, l, w);
		
		// bin 1
		while(true) {
			l = 0.05f;
			w = 0.05f;
			x = randomNumber(0, 1.95f-l);
			y = randomNumber(0, 0.95f-w);
			Path2D temp = new Path2D.Float();
			temp.moveTo(x, 		y);
			temp.lineTo(x + l,	y);
			temp.lineTo(x + l, 	y + w);
			temp.lineTo(x, 		y + w);
			temp.lineTo(x, 		y);
			temp.closePath();
			if(! intersects(temp, rect)) {
				addBin(bins, x, y, l, w);
				break;
			}
		}
		
		
		// table 2
		l = 0.5f;
		w = 0.3f;
		x = randomNumber(0, 1.95f-l);
		y = randomNumber(2.05f, 3f-w);
		rect = new Path2D.Float();
		rect.moveTo(x, 		y);
		rect.lineTo(x + l, 	y);
		rect.lineTo(x + l, 	y+w);
		rect.lineTo(x, 		y+w);
		rect.lineTo(x, 		y);
		rect.closePath();
		if((boolean) PlanningSettings.get("onlyOpaqueObstacles"))
			obstacles.add(rect);
		else
			seeThroughObstacles.add(rect);
		addTable(tables, x, y, l, w);
		
		// bin 2
		while(true) {
			l = 0.05f;
			w = 0.05f;
			x = randomNumber(0, 1.95f-l);
			y = randomNumber(2.05f, 3f-w);
			Path2D temp = new Path2D.Float();
			temp.moveTo(x, 		y);
			temp.lineTo(x + l,	y);
			temp.lineTo(x + l, 	y + w);
			temp.lineTo(x, 		y + w);
			temp.lineTo(x, 		y);
			temp.closePath();
			if(! intersects(temp, rect)) {
				addBin(bins, x, y, l, w);
				break;
			}
		}
		
		// table 3
		l = 0.5f;
		w = 0.3f;
		x = randomNumber(2.05f, 3.95f-l);
		y = randomNumber(0f, 0.95f-w);
		rect = new Path2D.Float();
		rect.moveTo(x, y);
		rect.lineTo(x+l, y);
		rect.lineTo(x+l, y+w);
		rect.lineTo(x, y+w);
		rect.lineTo(x, y);
		rect.closePath();
		if((boolean) PlanningSettings.get("onlyOpaqueObstacles"))
			obstacles.add(rect);
		else
			seeThroughObstacles.add(rect);
		addTable(tables, x, y, l, w);
		
		// bin 3
		while(true) {
			l = 0.05f;
			w = 0.05f;
			x = randomNumber(2.05f, 3.95f-l);
			y = randomNumber(0f, 0.95f-w);
			Path2D temp = new Path2D.Float();
			temp.moveTo(x, 		y);
			temp.lineTo(x + l,	y);
			temp.lineTo(x + l, 	y + w);
			temp.lineTo(x, 		y + w);
			temp.lineTo(x, 		y);
			temp.closePath();
			if(! intersects(temp, rect)) {
				addBin(bins, x, y, l, w);
				break;
			}
		}
		
		
		// table 4
		l = 0.5f;
		w = 0.3f;
		x = randomNumber(2.05f, 3.95f-l);
		y = randomNumber(2.05f, 3f-w);
		rect = new Path2D.Float();
		rect.moveTo(x, y);
		rect.lineTo(x+l, y);
		rect.lineTo(x+l, y+w);
		rect.lineTo(x, y+w);
		rect.lineTo(x, y);
		rect.closePath();
		if((boolean) PlanningSettings.get("onlyOpaqueObstacles"))
			obstacles.add(rect);
		else
			seeThroughObstacles.add(rect);
		addTable(tables, x, y, l, w);
		
		// bin 4
		while(true) {
			l = 0.05f;
			w = 0.05f;
			x = randomNumber(2.05f, 3.95f-l);
			y = randomNumber(2.05f, 3f-w);
			Path2D temp = new Path2D.Float();
			temp.moveTo(x, 		y);
			temp.lineTo(x + l,	y);
			temp.lineTo(x + l, 	y + w);
			temp.lineTo(x, 		y + w);
			temp.lineTo(x, 		y);
			temp.closePath();
			if(! intersects(temp, rect)) {
				addBin(bins, x, y, l, w);
				break;
			}
		}
		
		// table 5
		l = 0.5f;
		w = 0.3f;
		x = randomNumber(4.05f, 6f-l);
		y = randomNumber(0, 0.95f-w);
		rect = new Path2D.Float();
		rect.moveTo(x, y);
		rect.lineTo(x+l, y);
		rect.lineTo(x+l, y+w);
		rect.lineTo(x, y+w);
		rect.lineTo(x, y);
		rect.closePath();
		if((boolean) PlanningSettings.get("onlyOpaqueObstacles"))
			obstacles.add(rect);
		else
			seeThroughObstacles.add(rect);
		addTable(tables, x, y, l, w);
		
		// bin 5
		while(true) {
			l = 0.05f;
			w = 0.05f;
			x = randomNumber(4.05f, 6f-l);
			y = randomNumber(0, 0.95f-w);
			Path2D temp = new Path2D.Float();
			temp.moveTo(x, 		y);
			temp.lineTo(x + l,	y);
			temp.lineTo(x + l, 	y + w);
			temp.lineTo(x, 		y + w);
			temp.lineTo(x, 		y);
			temp.closePath();
			if(! intersects(temp, rect)) {
				addBin(bins, x, y, l, w);
				break;
			}
		}
		
		// table 6
		l = 0.5f;
		w = 0.3f;
		x = randomNumber(4.05f, 6f-l);
		y = randomNumber(2.05f, 3f-w);
		rect = new Path2D.Float();
		rect.moveTo(x, y);
		rect.lineTo(x+l, y);
		rect.lineTo(x+l, y+w);
		rect.lineTo(x, y+w);
		rect.lineTo(x, y);
		rect.closePath();
		if((boolean) PlanningSettings.get("onlyOpaqueObstacles"))
			obstacles.add(rect);
		else
			seeThroughObstacles.add(rect);
		addTable(tables, x, y, l, w);
		tables.closePath();
		
		// bin 6
		while(true) {
			l = 0.05f;
			w = 0.05f;
			x = randomNumber(4.05f, 6f-l);
			y = randomNumber(2.05f, 3f-w);
			Path2D temp = new Path2D.Float();
			temp.moveTo(x, 		y);
			temp.lineTo(x + l,	y);
			temp.lineTo(x + l, 	y + w);
			temp.lineTo(x, 		y + w);
			temp.lineTo(x, 		y);
			temp.closePath();
			if(! intersects(temp, rect)) {
				addBin(bins, x, y, l, w);
				break;
			}
		}
		apList.add("b");
		labels.add(bins);
		
		apList.add("t");
		labels.add(tables);
		
		Label labelling 	= new Label(apList, labels);

        env = new Environment(new float[] {(float) p1.getX(), (float) p3.getX()}, 
        		new float[] {(float) p1.getY(), (float) p3.getY()}, 
        		seeThroughObstacles, 
        		obstacles, 
        		init, 
        		labelling);
        
        System.out.println("done!");
		
	}
	
	private boolean intersects(Path2D rect1, Path2D rect2) {
		PathIterator it1 = rect1.getPathIterator(new AffineTransform());
		float[] point = new float[6];
		int segType1 = it1.currentSegment(point);
		assert(segType1 == PathIterator.SEG_MOVETO);
		
		while(!it1.isDone()) {
			if(rect2.contains(new Point2D.Float(point[0], point[1]))) {
				return true;
			}
			
			segType1 = it1.currentSegment(point);
			assert(segType1 == PathIterator.SEG_LINETO);
			it1.next();
		}
		return false;
	}

	void addTable(Path2D tables, float x, float y, float l, float w) {
		tables.moveTo(x - 0.1f, 		y - 0.1f);
		tables.lineTo(x + l + 0.1f, 	y - 0.1f);
		tables.lineTo(x + l + 0.1f, 	y + w + 0.1f);
		tables.lineTo(x - 0.1f, 		y + w + 0.1f);
		tables.lineTo(x - 0.1f, 		y - 0.1f);
	}
	
	void addBin(Path2D bins, float x, float y, float l, float w) {
		bins.moveTo(x - 0.1f, 		y - 0.1f);
		bins.lineTo(x + l + 0.1f, 	y - 0.1f);
		bins.lineTo(x + l + 0.1f, 	y + w + 0.1f);
		bins.lineTo(x - 0.1f, 		y + w + 0.1f);
		bins.lineTo(x - 0.1f, 		y - 0.1f);
	}
	
	public float randomNumber(float min, float max) {
		return (float) (Math.random() * (max - min) + min);
	}

}
