package modules;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import environment.Environment;

public class Discretization 
{ 
	float x1, x2, y1, y2, size;
	int numX, numY;
	int[][] discretization; // 0: don't know    1: free     2: visited     3:Obstacle
	
	boolean flag[][];
	
	ArrayList<ArrayList<int[]>> frontiers;
	
	public Discretization(Environment env, float size) 
	{
		this.x1 = env.getBoundsX()[0];
		this.y1 = env.getBoundsY()[0];
		this.x2 = env.getBoundsX()[1];
		this.y2 = env.getBoundsY()[1];
		this.numX = (int) ((x2 - x1)/size);
		this.numY = (int) ((y2 - y1)/size);
		this.size = size;
		this.flag = new boolean[numX][numY];
		discretization = new int[numX][numY];
		for(int i=0;i<numX;i++) {
			for(int j=0;j<numY;j++) {
				discretization[i][j] = 0;
			}
		}
		
		frontiers = new ArrayList<ArrayList<int[]>>();
	}
	
//	public Point2D findFreeCell() 
//	{
//		int k = 0, i, j;
//		int count = 0;
//
//		// First explore everything
//		for(i=0;i<numX;i++) {
//			for(j=0;j<numY;j++) {
//				if(discretization[i][j] == 1) 
//				{
//					if(j+1<numY) {
//						if(discretization[i][j+1] == 0) {
//							return findCenter(i,j);
//						}
//					}
//					else if(i+1<numX) {
//						if (discretization[i+1][j] == 0) {
//							return findCenter(i, j);
//						}
//					}
//					else if(i>0) {
//						if (discretization[i-1][j] == 0) {
//							return findCenter(i, j);
//						}
//					}
//					else if(j>0) {
//						if (discretization[i][j-1] == 0) {
//							return findCenter(i, j);
//						}
//					}
//				}
//			}
//		}
//		
////		if explored everywhere, move randomly
//		Random r = new Random();
//		while(k<500) {
//			count = 0;
//			i = r.nextInt(numX-1);
//			j = r.nextInt(numY-1);
//			if(discretization[i][j] <= 1) 
//			{
//				if (j+1<numY) {
//					if (discretization[i][j+1] <= 1) {
//						count++;
//					}
//				}
//				if(i+1<numX) {
//					if (discretization[i+1][j] <= 1) {
//						count++;
//					}
//				}
//				if(i>0) {
//					if (discretization[i-1][j] <= 1) {
//						count++;
//					}
//				}
//				if(j>0) {
//					if (discretization[i][j-1] <= 1) {
//						count++;
//					}
//				}
//				if (i+1<numX && j+1<numY) {
//					if (discretization[i+1][j+1] <= 1) {
//						count++;
//					}
//				}
//				if (i>0 && j+1<numY) {
//					if (discretization[i-1][j+1] <= 1) {
//						count++;
//					}
//				}
//				if (i+1<numX && j>0) {
//					if (discretization[i+1][j-1] <= 1) {
//						count++;
//					}
//				}
//				if (i>0 && j>0) {
//					if (discretization[i-1][j-1] <= 1) {
//						count++;
//					}
//				}
//				
//				if(count>3) {
//					return findCenter(i,j);
//				}
//			}
//			k++;
//		}
//		
//		
////		System.out.println("random Finished");
//		for(i=0;i<numX;i++) {
//			for(j=0;j<numY;j++) {
//				if(discretization[i][j] == 1) 
//				{
//					if(j+1<numY) {
//						if(discretization[i][j+1] <= 1) {
//							return findCenter(i,j);
//						}
//					}
//					else if(i+1<numX) {
//						if (discretization[i+1][j] <= 1) {
//							return findCenter(i, j);
//						}
//					}
//					else if(i>0) {
//						if (discretization[i-1][j] <= 1) {
//							return findCenter(i, j);
//						}
//					}
//					else if(j>0) {
//						if (discretization[i][j-1] <= 1) {
//							return findCenter(i, j);
//						}
//					}
//				}
//			}
//		}
//		printdiscretization();
//		return null;
//	}
	
	
	
	
	
	private boolean checkFrontierCell(int i, int j, int level) 
	{
		if(discretization[i][j] == level) {
			if (j+1<numY) {
				if (discretization[i][j+1] < level) {
					return true;
				}
			}
			if(i+1<numX) {
				if (discretization[i+1][j] < level) {
					return true;
				}
			}
			if(i>0) {
				if (discretization[i-1][j] < level) {
					return true;
				}
			}
			if(j>0) {
				if (discretization[i][j-1] < level) {
					return true;
				}
			}
		}
		return false;
	}

	public void updateDiscretization(Point2D p, int value) 
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
		if(discretization[i][j] < value) 
		{
			discretization[i][j] = value;
		}
	}
	
	public void updateDiscretization(Point2D p, Point2D q, int value) 
	{
		float px = (float) p.getX();
		float py = (float) p.getY();
		float qx = (float) q.getX();
		float qy = (float) q.getY();
		int pi 	= (int) ((px-x1)/size);
		if(pi == numX) {
			pi--;
		}
		int pj 	= (int) ((py-y1)/size);
		if(pj == numY) {
			pj--;
		}
		int qi 	= (int) ((qx-x1)/size);
		if(qi == numX) {
			qi--;
		}
		int qj 	= (int) ((qy-y1)/size);
		if(qj == numY) {
			qj--;
		}
		float slope = (qy-py)/(qx-px);
		float m;
		for(int n=pi+1; n<qi+1; n++) {
			m = slope*(n*size-px)+py;
			updateDiscretization(new Point2D.Float(n*size, m), value);
		}
	}

//	private void updateFrontiers(int i, int j, int value) 
//	{
//		if(checkFrontierCell(i,j)) 
//		{
//			for(int k=0; k<frontiers.size(); k++) 
//			{
//				for(int l=0; l<frontiers.get(k).size(); l++) 
//				{
//					if(frontiers.get(k).get(l)[0] == i+1 && frontiers.get(k).get(l)[1] == j) 
//					{
//						addInFrontier(frontiers.get(k),i,j);
//						return;
//					} else if(frontiers.get(k).get(l)[0] == i && frontiers.get(k).get(l)[1] == j+1) 
//					{
//						addInFrontier(frontiers.get(k),i,j);
//						return;
//					} else if(frontiers.get(k).get(l)[0] == i-1 && frontiers.get(k).get(l)[1] == j) 
//					{
//						addInFrontier(frontiers.get(k),i,j);
//						return;
//					} else if(frontiers.get(k).get(l)[0] == i && frontiers.get(k).get(l)[1] == j-1) 
//					{
//						addInFrontier(frontiers.get(k),i,j);
//						return;
//					}
//				}
//			} 
//			ArrayList<int[]> temp = new ArrayList<int[]>();
//			temp.add(new int[] {i,j});
//			frontiers.add(temp);
//		}
//	}
	
//	private void addInFrontier(ArrayList<int[]> front, int i, int j) 
//	{
//		for (int k=0;k<front.size();k++) {
//			if(front.get(k)[0]!=i || front.get(k)[1]!=j) {
//				front.add(new int[] {i,j});
//			}
//		}
//	}

	public Point2D findAMove(Point2D xRobot)
	{
		frontiers = new ArrayList<ArrayList<int[]>>();
		for(int i=0;i<numX;i++) {
			for(int j=0;j<numY; j++) {
				flag[i][j] = false;
			}
		}
		
		for(int i=0;i<numX;i++) {
			for(int j=0;j<numY;j++) {
				if(!flag[i][j] && checkFrontierCell(i,j,1)) {
					ArrayList<int[]> frontier = findFrontier(i,j,1);
					if(frontier.size() > 4) {
						frontiers.add(frontier);
					}
				}
			}
		}
		
		if(frontiers.size() == 0) {
			for(int i=0;i<numX;i++) {
				for(int j=0;j<numY;j++) {
					if(!flag[i][j] && checkFrontierCell(i,j,2)) {
						ArrayList<int[]> frontier = findFrontier(i,j,2);
						if(frontier.size() > 4) {
							frontiers.add(frontier);
						}
					}
				}
			}
		}
		
		if(frontiers.size()==0) {
			return null;
		}
		
		ArrayList<float[]> centers = new ArrayList<float[]>(frontiers.size());
		float[] closest = findCenter(frontiers.get(0));
		for(int i=0;i<frontiers.size();i++) {
			centers.add(i, findCenter(frontiers.get(i)));
//			if(distance(centers.get(i), xRobot) < distance(closest, xRobot)) {
			if(frontiers.get(i).size()/distance(centers.get(i), xRobot) > frontiers.get(i).size()/distance(closest, xRobot)) {
				closest = centers.get(i);
			}
		}
		System.out.println("Found move: "+closest[0] + ", " + closest[1]);
		return new Point2D.Float(closest[0], closest[1]);
		
//		ArrayList<int[]> biggest = new ArrayList<int[]>(frontiers.size());
//		for(int i=0; i<frontiers.size(); i++) {
//			if(frontiers.get(i).size() > biggest.size()) {
//				biggest = frontiers.get(i);
//			}
//		}
////		System.out.println("Found move: "+biggest[0] + ", " + biggest[1]);
//		return new Point2D.Float(findCenter(biggest)[0], findCenter(biggest)[1]);
	}
	
	private float distance(float[] p, Point2D q) 
	{
		return (float) Math.sqrt(Math.pow(p[0] - q.getX(), 2)+Math.pow(p[1] - q.getY(), 2));
	}

	private float[] findCenter(ArrayList<int[]> frontier) {
		float x=0, y=0;
		for(int i=0;i<frontier.size();i++) {
			x += frontier.get(i)[0];
			y += frontier.get(i)[1];
		}
		return new float[] {x/frontier.size()*size, y/frontier.size()*size};
	}

	private ArrayList<int[]> findFrontier(int i, int j, int level) {
		ArrayList<int[]> frontier = new ArrayList<int[]>();
		if(flag[i][j] == false) {
			flag[i][j] = true;
			frontier.add(new int[] {i,j});
			if(i+1<numX) {
				if(checkFrontierCell(i+1,j,level)) {
					frontier.addAll(findFrontier(i+1,j,level));
				}
			}
			if(j+1<numY) {
				if(checkFrontierCell(i,j+1,level)) {
					frontier.addAll(findFrontier(i,j+1,level));
				}
			}
			if(j>0) {
				if(checkFrontierCell(i,j-1,level)) {
					frontier.addAll(findFrontier(i,j-1,level));
				}
			}
			if(i>0) {
				if(checkFrontierCell(i-1,j,level)) {
					frontier.addAll(findFrontier(i-1,j,level));
				}
			}
			if(i+1<numX && j+1<numY) {
				if(checkFrontierCell(i+1,j+1,level)) {
					frontier.addAll(findFrontier(i+1,j+1,level));
				}
			}
			if(i>0 && j+1<numY) {
				if(checkFrontierCell(i-1,j+1,level)) {
					frontier.addAll(findFrontier(i-1,j+1,level));
				}
			}
			if(i+1<numX && j>0) {
				if(checkFrontierCell(i+1,j-1,level)) {
					frontier.addAll(findFrontier(i+1,j-1,level));
				}
			}
			if(i>0 && j>0) {
				if(checkFrontierCell(i-1,j-1,level)) {
					frontier.addAll(findFrontier(i-1,j-1,level));
				}
			}
		}
		return frontier;
	}

//	private int[] findBestCell(ArrayList<int[]> front) {
//		Random r = new Random();
////		printDiscretization();
////		printFrontiers();
//		int i = r.nextInt(front.size()-1);
//		return front.get(i);
////		int min_i = front.get(0)[0], max_i = front.get(0)[0];
////		int min_j = front.get(0)[1], max_j = front.get(0)[1];
////		
////		for(int k=0;k<front.size();k++) {
////			if(min_i>front.get(k)[0]) {
////				min_i = front.get(k)[0];
////			}
////			if(max_i<front.get(k)[0]) {
////				max_i = front.get(0)[0];
////			}
////			if(min_j>front.get(0)[1]) {
////				min_j = front.get(k)[1];
////			}
////			if(max_j<front.get(0)[1]) {
////				max_j = front.get(k)[1];
////			}
////		}
//	}

	public void printFrontiers() {
		for(int i=0;i<frontiers.size();i++) {
			for(int j=0;j<frontiers.get(i).size();j++) {
				System.out.print("["+frontiers.get(i).get(j)[0]+", "+frontiers.get(i).get(j)[1]+"]  ");
			}
			System.out.print("\n");
		}
		System.out.println("num of frontiers: " + frontiers.size());
		
	}

//	private ArrayList<int[]> findBestFrontier() {
//		ArrayList<int[]> best = new ArrayList<int[]>();
//		for(int i=0;i<frontiers.size();i++) {
//			if(best.size()<frontiers.get(i).size()) {
//				best = frontiers.get(i);
//			}
//		}
//		return best;
//	}

	public void printDiscretization() 
	{
		for(int i=0;i<numX;i++) {
			for(int j=0;j<numY;j++) {
				if(checkFrontierCell(i,j,1)) {
					System.out.print(8+",");
				} else if(checkFrontierCell(i,j,2)){
					System.out.print(5+",");
				} else {
					System.out.print(discretization[i][j]+",");
				}
			}
			System.out.print("\n");
		}
	}

}
