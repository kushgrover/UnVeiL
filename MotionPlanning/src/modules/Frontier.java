package modules;

import java.awt.geom.Point2D;
import java.util.Random;

import environment.Environment;

public class Frontier 
{ 
	float x1, x2, y1, y2, size;
	int numX, numY;
	int[][] frontier; // 0: don't know    1: free     2: visited     3:Obstacle
	public Frontier(Environment env, float size) 
	{
		this.x1 = env.getBoundsX()[0];
		this.y1 = env.getBoundsY()[0];
		this.x2 = env.getBoundsX()[1];
		this.y2 = env.getBoundsY()[1];
		this.numX = (int) ((x2 - x1)/size);
		this.numY = (int) ((y2 - y1)/size);
		this.size = size;
		
		frontier = new int[numX][numY];
		for(int i=0;i<numX;i++) {
			for(int j=0;j<numY;j++) {
				frontier[i][j] = 0;
			}
		}
	}
	
	public Point2D findFreeCell() 
	{
		int k = 0, i, j;
		int count = 0;

		// First explore everything
		for(i=0;i<numX;i++) {
			for(j=0;j<numY;j++) {
				if(frontier[i][j] == 1) 
				{
					if(j+1<numY) {
						if(frontier[i][j+1] == 0) {
							return findCenter(i,j);
						}
					}
					else if(i+1<numX) {
						if (frontier[i+1][j] == 0) {
							return findCenter(i, j);
						}
					}
					else if(i>0) {
						if (frontier[i-1][j] == 0) {
							return findCenter(i, j);
						}
					}
					else if(j>0) {
						if (frontier[i][j-1] == 0) {
							return findCenter(i, j);
						}
					}
				}
			}
		}
		
//		if explored everywhere, move randomly
		Random r = new Random();
		while(k<500) {
			count = 0;
			i = r.nextInt(numX-1);
			j = r.nextInt(numY-1);
			if(frontier[i][j] <= 1) 
			{
				if (j+1<numY) {
					if (frontier[i][j+1] <= 1) {
						count++;
					}
				}
				if(i+1<numX) {
					if (frontier[i+1][j] <= 1) {
						count++;
					}
				}
				if(i>0) {
					if (frontier[i-1][j] <= 1) {
						count++;
					}
				}
				if(j>0) {
					if (frontier[i][j-1] <= 1) {
						count++;
					}
				}
				if (i+1<numX && j+1<numY) {
					if (frontier[i+1][j+1] <= 1) {
						count++;
					}
				}
				if (i>0 && j+1<numY) {
					if (frontier[i-1][j+1] <= 1) {
						count++;
					}
				}
				if (i+1<numX && j>0) {
					if (frontier[i+1][j-1] <= 1) {
						count++;
					}
				}
				if (i>0 && j>0) {
					if (frontier[i-1][j-1] <= 1) {
						count++;
					}
				}
				
				if(count>3) {
					return findCenter(i,j);
				}
			}
			k++;
		}
		
		
//		System.out.println("random Finished");
		for(i=0;i<numX;i++) {
			for(j=0;j<numY;j++) {
				if(frontier[i][j] == 1) 
				{
					if(j+1<numY) {
						if(frontier[i][j+1] <= 1) {
							return findCenter(i,j);
						}
					}
					else if(i+1<numX) {
						if (frontier[i+1][j] <= 1) {
							return findCenter(i, j);
						}
					}
					else if(i>0) {
						if (frontier[i-1][j] <= 1) {
							return findCenter(i, j);
						}
					}
					else if(j>0) {
						if (frontier[i][j-1] <= 1) {
							return findCenter(i, j);
						}
					}
				}
			}
		}
		printFrontier();
		return null;
	}
	
	private Point2D findCenter(int i, int j) {
		float x = x1 + i*size + size/2;
		float y = y1 + j*size + size/2;
		return new Point2D.Float(x,y);
	}

	public void updateFrontier(Point2D p, int value) 
	{
		float x = (float) p.getX();
		float y = (float) p.getY();
		int i = (int) ((x-x1)/size);
		int j = (int) ((y-y1)/size);
		if(frontier[i][j] < value) 
		{
			frontier[i][j] = value;
		}
	}

	public void printFrontier() {
		for(int i=0;i<numX;i++) {
			for(int j=0;j<numY;j++) {
				System.out.print(frontier[i][j]+",");
			}
			System.out.print("\n");
		}
	}

}
