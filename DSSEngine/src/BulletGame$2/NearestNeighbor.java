package BulletGame$2;

import java.awt.geom.Rectangle2D;
import java.util.LinkedList;

public class NearestNeighbor {

	public static void ANN(int closest_num, BulletPattern bp, float t) {
		//1) generate quad tree
		int numPerBin = 3; //
		QuadTree myTree = new QuadTree(numPerBin,new Rectangle2D.Float(-1,-1,2,2), null);
		for(int k = 0; k < bp.states_indexCt.length; k++){
			if (isReady(bp,k,t)){
				int cpos = bp.states_delta_pos[k];
				float x = bp.X2(bp.states_delta_x, k, cpos);
				float y = bp.X2(bp.states_delta_y, k, cpos);
				myTree.addPoint(x, y, k);
			}
		}
		//myTree.print();
		for(int k = 0; k < bp.states_indexCt.length; k++){
			if (isReady(bp,k,t)){
				int cpos = bp.states_delta_pos[k];
				float x = bp.X2(bp.states_delta_x, k, cpos);
				float y = bp.X2(bp.states_delta_y, k, cpos);
				closest2(myTree, bp, k, x, y, closest_num);
			}
		}
		//Now, are some of the links asymmetric?
		if (false){
			for(int k = 0; k < bp.states_indexCt.length; k++){
				if (isReady(bp,k,t)){
					int[] neighbs = bp.states_neighbors[k];
					for(int wE = 0; wE < neighbs.length; wE++){
						int o = neighbs[wE];
						boolean hasMeAsANeighbor = false;
						int[] neighbs2 = bp.states_neighbors[o];
						big: for(int oNeighb : neighbs2){
							if (oNeighb==k){
								hasMeAsANeighbor = true;
								break big;
							}
						}
						if (!hasMeAsANeighbor){
							float[] shareWeight = bp.states_neighbor_EW[k][wE];
							float[][] weights2 = bp.states_neighbor_EW[o];
							float[][] newWeights2 = new float[neighbs2.length+1][];
							System.arraycopy(weights2,0,newWeights2,0,weights2.length);
							newWeights2[newWeights2.length-1] = shareWeight;
							bp.states_neighbor_EW[o] = newWeights2;

							int[] newNeighbs2 = new int[neighbs2.length+1];
							System.arraycopy(neighbs2, 0, newNeighbs2,0,neighbs2.length);
							newNeighbs2[newNeighbs2.length-1] = k;
							bp.states_neighbors[o] = newNeighbs2;
						}
					}
				}
			}
		}
	}

	private static boolean isReady(BulletPattern bp, int k, float t) {
		return bp.states_indexCt[k]>=0 && bp.states_neighbors[k]==null && (t-bp.states_time[k])>bp.linktime;
	}

	private static class ClosestOperator {
		public ClosestOperator(int closestNum){
			cutNum = closestNum;
		}
		private int cutNum;
		public LinkedList<Float> distances = new LinkedList();
		public LinkedList<Integer> neighbs  = new LinkedList();
		public void merge(QuadTree inDir, float x0, float x1, int sourcePval) {
			if (inDir==null){
				return;
			}
			if (inDir.children!=null){
				for(QuadTree q : inDir.children){
					merge(q,x0,x1,sourcePval);
				}
			} else {
				eachPoint: for(int k = 0; k < inDir.numPoints; k++){
					float x = x0-inDir.points[k*2];
					float y = x1-inDir.points[k*2+1];
					int pval = inDir.pointNum[k];
					if (pval == sourcePval){
						continue; //This is me!
					}
					for(int p = 0; p < distances.size(); p++){
						if (neighbs.get(p)==pval){
							continue eachPoint; //Don't duplicate
						}
					}
					float dist = x*x+y*y;
					boolean inserted = false;
					big: for(int p = 0; p < distances.size(); p++){
						if (dist < distances.get(p)){
							distances.add(p,dist);
							neighbs.add(p,pval);
							inserted = true;
							break big;
						}	
					}			
					if (distances.size()<cutNum-1){
						if (!inserted){
							distances.add(dist);
							neighbs.add(pval);
						}
					} else {
						if (distances.size()>cutNum){
							distances.removeLast();
							neighbs.removeLast();
						}
					}
				}
			}
		}
	}

	private static void closest2(QuadTree root, BulletPattern bp, int k, float x, float y, int closest_num) {
		ClosestOperator co = new ClosestOperator(closest_num);

		QuadTree owner = root.getParent(x,y);
		co.merge(owner,x,y,k);
		if (owner==null){
			//System.out.println("@!?");
		} else {
			for(int ri = 0; ri < 4; ri++){
				QuadTree inDir = owner.getNeighbor(ri);
				//L,T,R,B
				//T,R,B,L
				co.merge(inDir,x,y,k);
				if (inDir!=null){
					int coDir = (ri+1)%4;
					inDir = inDir.getNeighbor(coDir);
					co.merge(inDir,x,y,k);
				}
			}
		}

		bp.states_neighbors[k] = new int[co.neighbs.size()];
		float[][] edgWs = bp.states_neighbor_EW[k] = new float[co.neighbs.size()][];
		int count = 0;
		//They both "got it"
		for(int n : co.neighbs){
			bp.states_neighbors[k][count] = n;
			boolean neighbShares = false;
			int[] other = bp.states_neighbors[n];
			if (true){
				edgWs[count] = new float[3];
			} else {
				if (other!=null){
					for(int oCt = 0; oCt < other.length; oCt++){
						if (other[oCt] == k){
							edgWs[count] = bp.states_neighbor_EW[n][oCt];
							neighbShares = true;
						}
					}
				}
				if (!neighbShares){
					edgWs[count] = new float[3];
				}
			}
			///loop
			count++;
		}
	}

	private static class QuadTree {
		public QuadTree(int closest_num, Rectangle2D.Float rect, QuadTree parent){
			a = rect;
			neighbs = new QuadTree[]{null,null,null,null};
			this.parent = parent;
			this.numPointsInEach = closest_num;
			points = new float[numPointsInEach*2];
			pointNum = new int[numPointsInEach];
		}
		public QuadTree getNeighbor(int ri) {
			if (neighbs[ri]!=null){
				return neighbs[ri];
			}
			int ups = 1;
			LinkedList<Integer> jumpUpStack = new LinkedList<Integer>();
			jumpUpStack.addLast(quadNum);
			QuadTree p = parent;
			while(true){
				if (p==null || p.neighbs==null){
					return null;
				}
				if (p.neighbs[ri]!=null){
					//Ok! we have something that's in that direction. Go back?
					QuadTree best = p.neighbs[ri];
					for(int k = 0; k < ups; k++){
						if (best.children==null){
							break;
						}
						int godown = jumpUpStack.removeLast();
						godown = complement(godown,ri);
						best = best.children[godown];
					}
					return best;
				}
				jumpUpStack.addLast(p.quadNum);
				p = p.parent;
				ups++;
			}
		}
		private static final int[][] complementArray = new int[][]{
			{1,3,-1,-1},
			{-1,2,0,-1},
			{-1,-1,3,1},
			{2,-1,-1,0}
		};
		public int complement(int source, int direction){
			//NEIGHBORS: 
			//LEFT, TOP, RIGHT, BOTTOM
			//CHILDREN ORDER:
			//TL,TR,BR,BL
			//TL -> (TR,BL,???,???)
			//TR -> (??,BR,TL,??)
			//BR -> (??,??,BL,TR)
			//BL -> (BR,??,??,TL)
			int toRet =  complementArray[source][direction];
			if (toRet==-1){
				throw new RuntimeException("NOT ALLOWED "+" "+source+" "+direction);
			}
			return toRet;
		}
		public void print() {
			System.out.println(a);
			if (children!=null){
				for(QuadTree q : children){
					q.print();
				}
			}
		}
		public void setNeighbs(QuadTree[] neighbs){
			this.neighbs = neighbs;
		}
		private Rectangle2D.Float a;
		public QuadTree[] neighbs;
		public QuadTree[] children;
		public QuadTree parent;
		final int numPointsInEach;
		final static float epsilon = 1e-5f; 
		float[] points;
		int[] pointNum;
		int numPoints = 0;
		int quadNum = -1;
		public QuadTree getParent(float x, float y) {
			if (!a.contains(x,y)){
				return null;
			}
			if (children==null){
				return this;
			}
			for(QuadTree q : children){
				QuadTree got = q.getParent(x,y);
				if (got!=null){
					return got;
				}
			}
			return null;
		}
		public void addPoint(float x, float y, int pval){
			if (!a.contains(x,y)){
				return;
			}
			if(children!=null){
				for(QuadTree q : children) q.addPoint(x,y,pval);
				return;
			}
			if (numPoints>=numPointsInEach){
				subdivide();
				for(int k = 0; k < numPoints; k++){
					addPoint(points[k*2],points[k*2+1],pointNum[k]);
				}
				addPoint(x,y,pval);
			} else {
				//Check if it's already here.
				for(int k = 0; k < numPoints; k++){
					if (Math.abs(points[k*2]-x)<epsilon && Math.abs(points[k*2+1]-y)<epsilon){
						return;
					}
				}
				int myPoint = numPoints++;
				points[myPoint*2]=x;
				points[myPoint*2+1]=y;
				pointNum[myPoint] = pval; 
			}
		}
		public void subdivide(){
			//NEIGHBORS: 
			//LEFT, TOP, RIGHT, BOTTOM
			//CHILDREN ORDER:
			//TL,TR,BR,BL
			float avgx = 0, avgy = 0;
			for(int k = 0; k < numPoints; k++){
				avgx += points[k*2];
				avgy += points[k*2+1];
			}
			avgx /= numPoints; avgy /= numPoints;
			float bifurcx = avgx-a.x;
			float bifurcy = avgy-a.y;
			children = new QuadTree[]{
					new QuadTree(numPointsInEach,
							new Rectangle2D.Float(a.x,a.y,bifurcx,bifurcy),this),
							new QuadTree(numPointsInEach,
									new Rectangle2D.Float(avgx,a.y,a.width-bifurcx,bifurcy),this),
									new QuadTree(numPointsInEach,
											new Rectangle2D.Float(a.x,avgy,bifurcx,a.height-bifurcy),this),	
											new QuadTree(numPointsInEach,
													new Rectangle2D.Float(avgx,avgy,a.width-bifurcx,a.height-bifurcy),this),		
			};
			children[0].setNeighbs(new QuadTree[]{
					null,null,children[1],children[3]
			});
			children[1].setNeighbs(new QuadTree[]{
					children[0],null,null,children[2]
			});
			children[2].setNeighbs(new QuadTree[]{
					children[3],children[1],null,null
			});
			children[3].setNeighbs(new QuadTree[]{
					null,children[0],children[2],null
			});
			for(int k = 0; k < 4; k++){
				children[k].quadNum = k;
			}
		}
	}

}
