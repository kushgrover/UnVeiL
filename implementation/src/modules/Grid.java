package modules;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.jgrapht.alg.util.Pair;

import abstraction.ProductAutomaton;
import environment.Environment;
import net.sf.javabdd.BDD;

public abstract class Grid 
{ 
	float x1;
	float x2;
	float y1;
	float y2;
	float size;
	int numX;
	int numY;
	int[][] grid; // 0: don't know    1: free     2: visited     3:Obstacle
	List<ArrayList<BDD>> labels;
	ArrayList<ArrayList<int[]>> frontiers;
	boolean[][] flag; // used to compute frontiers
	
	public Grid(Environment env, float size) 
	{
		this.x1 = env.getBoundsX()[0];
		this.y1 = env.getBoundsY()[0];
		this.x2 = env.getBoundsX()[1];
		this.y2 = env.getBoundsY()[1];
		this.numX = (int) ((x2 - x1)/size);
		this.numY = (int) ((y2 - y1)/size);
		this.size = size;
		this.flag = new boolean[numX][numY];
		grid = new int[numX][numY];
		labels = new ArrayList<>();
		
		for(int i=0;i<numX;i++) {
			labels.add(new ArrayList<>());
			for(int j=0;j<numY;j++) {
				labels.get(i).add(ProductAutomaton.factory.zero());
				grid[i][j] = 0;
			}
		}
		frontiers = new ArrayList<>();
	}
	
	/*
	 * Checks if a cell is a frontier cell by looking at its neighbours
	 */
	private boolean checkFrontierCell(int i, int j, int level) 
	{
		int count = 0;	
		if(grid[i][j] == level) {
			if (j+1<numY) {
				if (grid[i][j+1] < level) {
					count++;
				}
			}
			if(i+1<numX) {
				if (grid[i+1][j] < level) {
					count++;
				}
			}
			if(i>0) {
				if (grid[i-1][j] < level) {
					count++;
				}
			}
			if(j>0) {
				if (grid[i][j-1] < level) {
					count++;
				}
			}
			if (j+1<numY && i>0) {
				if (grid[i-1][j+1] < level) {
					count++;
				}
			}
			if (j+1<numY && i+1<numX) {
				if (grid[i+1][j+1] < level) {
					count++;
				}
			}
			if (j>0 && i>0) {
				if (grid[i-1][j-1] < level) {
					count++;
				}
			}
			if (i+1<numX && j>0) {
				if (grid[i+1][j-1] < level) {
					count++;
				}
			}
		}
		return count > 0;
	}

	/*
	 * updates the grid at point p with value
	 */
	public void updateDiscretization(Point2D p, int value) {
		float x = (float) p.getX();
		float y = (float) p.getY();
		int i 	= (int) ((x-x1)/size);
		if(i == numX) {
			i--;
		}
		int j 	= (int) ((y-y1)/size);
		if(j == numY) {
			j--;
		}
		if(grid[i][j] < value) 
		{
			grid[i][j] = value;
		}
	}
	
	/*
	 * updates the grid at point p with value and label
	 */
	public void updateDiscretization(Point2D p, int value, BDD label)
	{
		float x = (float) p.getX();
		float y = (float) p.getY();
		int i 	= (int) ((x-x1)/size);
		if(i == numX) {
			i--;
		}
		int j 	= (int) ((y-y1)/size);
		if(j == numY) {
			j--;
		}
		if(grid[i][j] < value) 
		{
			grid[i][j] = value;
			labels.get(i).set(j, label);
		}
	}
	

	/*
	 * know the grid in the sensing radius
	 */
	public abstract void knowDiscretization(Environment env,
			ProductAutomaton productAutomaton, 
			Point2D currentPosition, 
			float sensingRadius) throws Exception;


	
	protected int clampX(int i) {
		if(i == numX) {
			return i-1;
		}
		return i;
	}
	
	protected int clampY(int j) {
		if(j == numY) {
			return j-1;
		}
		return j;
	}

	protected boolean cellInsideSensingRadius(int i, int j, Point2D currentPosition, float sensingRadius) {
		if(distance(new float[] {i*size, j*size}, currentPosition) > sensingRadius) {
			return false;
		}
		if(distance(new float[] {i*size, (j+1)*size}, currentPosition) > sensingRadius) {
			return false;
		}
		if(distance(new float[] {(i+1)*size, j*size}, currentPosition) > sensingRadius) {
			return false;
		}
		return !(distance(new float[]{(i + 1) * size, (j + 1) * size}, currentPosition) > sensingRadius);
	}
	
	/*
	 * compute distance
	 */
	protected static float distance(Point2D p, Point2D q) {
		return (float) Math.sqrt(StrictMath.pow(p.getX() - q.getX(), 2) + StrictMath.pow(p.getY() - q.getY(), 2));
	}
	
	/*
	 * compute distance
	 */
	private static float distance(float[] p, Point2D q) {
		return (float) Math.sqrt(StrictMath.pow(p[0] - q.getX(), 2) + StrictMath.pow(p[1] - q.getY(), 2));
	}

	protected float distance(int[] cell, Point2D target) {
		Point2D source = findCentre(cell[0], cell[1]);
		return distance(source, target);
	}
	
	/*
	 * find centre of the cell with indices i,j
	 */
	public Point2D findCentre(int i, int j) {
		float x = i + 0.5F;
		float y = j + 0.5F;
		return new Point2D.Float(x * size, y * size);
	}
	
	ArrayList<ArrayList<int[]>> findFrontiers(){
		frontiers = new ArrayList<>();
		for(int i=0; i<numX; i++) {
			for(int j=0; j<numY; j++) {
				flag[i][j] = false;
			}
		}
		
		for(int i=0; i<numX; i++) {
			for(int j=0; j<numY; j++) {
				if(!flag[i][j] && checkFrontierCell(i,j,1)) {
					ArrayList<int[]> frontier = findFrontier(i,j,1);
					if(frontier.size() >= size) {
						frontiers.add(frontier);
					}
				}
			}
		}
		return frontiers;
	}
	
	
	

	protected Point2D findCellCenter(Point2D p) {
		int[] index = findCell(p);
		
		return findCentre(index[0], index[1]);
	}

	/*
	 * finds centre of a frontier
	 */
	protected Point2D findFrontierCenter(ArrayList<int[]> frontier) {
		float x=size/2;
		float y=size/2;
		for (int[] ints : frontier) {
			x += ints[0];
			y += ints[1];
		}
		
		Point2D bestFrontier = new Point2D.Float(x/frontier.size()*size, y/frontier.size()*size); 
		int bestCell = 0;
		float bestD = distance(frontier.get(0), bestFrontier);
		for(int i=0; i<frontier.size(); i++) {
			if(distance(frontier.get(i), bestFrontier) < bestD) {
				bestCell = i;
				bestD = distance(frontier.get(i), bestFrontier);
			}
		}
		return findCentre(frontier.get(bestCell)[0], frontier.get(bestCell)[1]);
	}

	/*
	 * find frontier containing the cell with indices i,j
	 */
	private ArrayList<int[]> findFrontier(int i, int j, int level) {
		ArrayList<int[]> frontier = new ArrayList<>();
		if(!flag[i][j]) {
			flag[i][j] = true;
			frontier.add(new int[] {i,j});
			if(i+1 < numX) {
				if(checkFrontierCell(i+1,j,level)) {
					frontier.addAll(findFrontier(i+1,j,level));
				}
			}
			if(j+1 < numY) {
				if(checkFrontierCell(i,j+1,level)) {
					frontier.addAll(findFrontier(i,j+1,level));
				}
			}
			if(j > 0) {
				if(checkFrontierCell(i,j-1,level)) {
					frontier.addAll(findFrontier(i,j-1,level));
				}
			}
			if(i > 0) {
				if(checkFrontierCell(i-1,j,level)) {
					frontier.addAll(findFrontier(i-1,j,level));
				}
			}
			if(i+1 < numX && j+1 < numY) {
				if(checkFrontierCell(i+1,j+1,level)) {
					frontier.addAll(findFrontier(i+1,j+1,level));
				}
			}
			if(i > 0 && j+1 < numY) {
				if(checkFrontierCell(i-1,j+1,level)) {
					frontier.addAll(findFrontier(i-1,j+1,level));
				}
			}
			if(i+1 < numX && j > 0) {
				if(checkFrontierCell(i+1,j-1,level)) {
					frontier.addAll(findFrontier(i+1,j-1,level));
				}
			}
			if(i > 0 && j > 0) {
				if(checkFrontierCell(i-1,j-1,level)) {
					frontier.addAll(findFrontier(i-1,j-1,level));
				}
			}
		}
		return frontier;
	}
	
	/*
	 * print all frontiers
	 */
	public void printFrontiers() {
		for (Iterable<int[]> frontier : frontiers) {
			for (int[] ints : frontier) {
				System.out.print("[" + ints[0] + ", " + ints[1] + "]  ");
			}
			System.out.print("\n");
		}
		System.out.println("num of frontiers: " + frontiers.size());
	}

	/**
	 * prints the grid
	 */
	public void printDiscretization() 
	{
//		for(int i=0; i<numX; i++) {
//			for(int j=0; j<numY; j++) {
//				if(checkFrontierCell(i,j,1)) {
//					System.out.print("F,");
//				} else if(checkFrontierCell(i,j,2)){
//					System.out.print("X,");
//				} else {
//					System.out.print(grid[i][j] + ",");
//				}
//			}
//			System.out.print("\n");
//		}
//		System.out.println("\n\n\n\n");
		
		for(int i=0; i<numX; i++) {
			for(int j=0; j<numY; j++) {
					System.out.print(grid[i][j] + ",");
			}
			System.out.print("\n");
		}
	}
	
	protected int[] findCell(Point2D p){
		int i = (int) (p.getX()/size);
		int j = (int) (p.getY()/size);
		return new int[] {i, j};
	}
	
	public boolean isExplored(Point2D p) {
		int[] c = findCell(p);
		return grid[c[0]][c[1]] > 0;
	}
	
	public boolean exploredCompletely() {
		for(int i=0; i<numX; i++) {
			for(int j=0; j<numY; j++) {
				if(grid[i][j] == 0) {
					return false;
				}
			}
		}
		return true;
	}
	
//	public abstract Pair<Point2D, Integer> findAMove(Point2D xRobot);
	
	abstract Pair<Point2D, Pair<Integer, Float>> findBestFrontier(ArrayList<ArrayList<int[]>> frontiers, Point2D xRobot);

//	public abstract float computeDistance(Graph<Vertex, DefaultEdge> graph, Point2D currentPoint, Point2D xRobot);

}
