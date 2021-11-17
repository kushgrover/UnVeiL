package environment;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;

public final class Environment {
	Shape bounds;
	
	private float[] boundsX = null;

	private float[] boundsY = null;
	
	List<Path2D> seeThroughObstacles;
	List<Path2D> obstacles;
	int numOfObs;
	int numOfWalls;
	static Point2D init;
	static Label labelling;
//	Grid discretization = null;
	
	public Environment(float[] boundsX, float[] boundsY, List<Path2D> seeThroughObstacles, List<Path2D> obstacles, Point2D init, Label labelling)
	{
		this.bounds				= new Rectangle2D.Float(boundsX[0],boundsY[0],boundsX[1]-boundsX[0],boundsY[1]-boundsY[0]);
		this.setBoundsX(boundsX);
		this.setBoundsY(boundsY);
		this.numOfObs			= seeThroughObstacles.size();
		this.seeThroughObstacles = Collections.unmodifiableList(seeThroughObstacles);
		this.numOfWalls			= obstacles.size();
		this.obstacles 			= Collections.unmodifiableList(obstacles);

		Environment.init		= init;
		Environment.labelling	= labelling;
	}
	
//	public void setDiscretization(Grid discretization)
//	{
//		this.discretization = discretization;
//	}
	
	public static Label getLabelling() 
	{
		return labelling;
	}
	
	public static Point2D getInit()
	{
		return init;
	}
	
	public Boolean obstacleFreeAll(Point2D x) 
	{
		for (Path2D next : seeThroughObstacles) {
			if (next.contains(x)) {
				return false;
			}
		}
		for (Path2D next : obstacles) {
			if (next.contains(x)) {
				return false;
			}
		}
		return true;
	}

//	public Boolean obstacleFreeAll(Rectangle2D x)
//	{
//		for (Path2D next : seeThroughObstacles) {
//			if (next.intersects(x)) {
//				return false;
//			}
//		}
//		for (Path2D next : obstacles) {
//			if (next.intersects(x)) {
//				return false;
//			}
//		}
//		return true;
//	}
	
	public Point2D.Float sample()
	{
		float x		= (float) (getBoundsX()[0]+Math.random()*(getBoundsX()[1]-getBoundsX()[0]));
		float y		= (float) (getBoundsY()[0]+Math.random()*(getBoundsY()[1]-getBoundsY()[0]));
		return new Point2D.Float(x,y);
	}

//	public Point2D.Float sampleFree()
//	{
//		Point2D.Float p;
//		while(true)
//		{
//			p	= sample();
//			if(obstacleFreeAll(p))
//			{
//				return p;
//			}
//		}
//	}
	
	public Boolean collisionFreeFromOpaqueObstacles(Point2D start, Point2D end) 
	{
		Line2D line		= new Line2D.Float(start, end);
		float[] coords1	= new float[6];
		float[] coords2 = new float[6];

		for(int i=0; i<numOfWalls; i++) 
		{
			PathIterator ite	= getObstacles().get(i).getPathIterator(new AffineTransform());
			int segmentType = ite.currentSegment(coords1);
			assert(segmentType == PathIterator.SEG_MOVETO);
			
			while(!ite.isDone()) {
				segmentType		= ite.currentSegment(coords2);
				assert(segmentType == PathIterator.SEG_LINETO);
				Line2D boundary = new Line2D.Float(coords1[0], coords1[1], coords2[0], coords2[1]);

				if(boundary.intersectsLine(line)) {
					return false;
				}
				
				coords1[0]		= coords2[0];
				coords1[1]		= coords2[1];
				ite.next();
			}
		}
		return true;
	}
	
	public Boolean collisionFreeAll(Point2D start, Point2D end) 
	{
		Line2D boundary;
		Line2D line		= new Line2D.Float(start,end);
		float[] coords1	= new float[6];
		float[] coords2 = new float[6];
		int segmentType;
		
		for(int i=0; i<numOfWalls; i++) 
		{
			PathIterator ite	= getObstacles().get(i).getPathIterator(new AffineTransform());
			segmentType			= ite.currentSegment(coords1);
			assert(segmentType == PathIterator.SEG_MOVETO);
			
			while(!ite.isDone()) {
				segmentType		= ite.currentSegment(coords2);
				assert(segmentType == PathIterator.SEG_LINETO);
				boundary		= new Line2D.Float(coords1[0], coords1[1], coords2[0], coords2[1]);

				if(boundary.intersectsLine(line)) {
					return false;
				}
				
				coords1[0]		= coords2[0];
				coords1[1]		= coords2[1];
				ite.next();
			}
		}
		for(int i=0; i<numOfObs; i++) 
		{
			PathIterator ite	= getSeeThroughObstacles().get(i).getPathIterator(new AffineTransform());
			segmentType			= ite.currentSegment(coords1);
			assert(segmentType == PathIterator.SEG_MOVETO);
			
			while(!ite.isDone()) {
				segmentType		= ite.currentSegment(coords2);
				assert(segmentType == PathIterator.SEG_LINETO);
				boundary		= new Line2D.Float(coords1[0], coords1[1], coords2[0], coords2[1]);

				if(boundary.intersectsLine(line)) {
					return false;
				}
				
				coords1[0]		= coords2[0];
				coords1[1]		= coords2[1];
				ite.next();
			}
		}
		return true;
	}
	
//	public static Shape polygon(float[] vertices)
//	{
//		Path2D.Float path		= new Path2D.Float();
//		path.moveTo(vertices[0], vertices[1]);
//
//		for(int i = 2; i<vertices.length; i += 2)
//		{
//			path.lineTo(vertices[i], vertices[i+1]);
//		}
//
//		path.closePath();
//		return path.createTransformedShape(new AffineTransform());
//	}

	public float[] getBoundsX() {
		return boundsX.clone();
	}

	public void setBoundsX(float[] boundsX) {
		this.boundsX = boundsX.clone();
	}

	public float[] getBoundsY() {
		return boundsY.clone();
	}

	public void setBoundsY(float[] boundsY) {
		this.boundsY = boundsY.clone();
	}

	public List<Path2D> getSeeThroughObstacles() {
		return Collections.unmodifiableList(seeThroughObstacles);
	}

	public List<Path2D> getObstacles() {
		return Collections.unmodifiableList(obstacles);
	}
}





