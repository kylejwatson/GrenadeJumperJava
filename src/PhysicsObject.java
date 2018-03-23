import java.util.ArrayList;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class PhysicsObject extends GameObject{
	private GraphicsContext gc;
	private static final double GRAV = 0.3;
	private static final double FRIC = 0.3;
	protected double velx = 0;
	protected double vely = 0;
	protected ArrayList<GameObject> list;
	protected ArrayList<GameObject> delList;
	protected ArrayList<double[]> lines = new ArrayList<double[]>();
	public PhysicsObject(Image img, double x, double y,ArrayList<GameObject> list,ArrayList<GameObject> delList, ArrayList<double[]> lines) {
		super(img, x, y);
		this.list = list;
		this.delList = delList;
		this.lines = lines;
		// TODO Auto-generated constructor stub
	}

	public void update(GraphicsContext gc){
		this.gc = gc;
		vely += GRAV;
		if(Math.abs(velx)<0.3)
			velx = 0;
		
		x += velx;
		y += vely;

		collisionResolution();
		for(int i = 0; i< lines.size(); i++)
		{
			gc.setStroke(Color.RED);
			double[] point = closestPoint(lines.get(i));
			//gc.strokeLine(x, y, point[0], point[1]);
		}
		super.update(gc);
		//gc.fillOval(x-radius, y-radius, radius*2, radius*2);
	}

	private void collisionResolution(){
		for(int i = 0; i< lines.size(); i++){
			double[] point = detectCircle(lines.get(i));
			
			if(point != null){
				double xvec = point[0] - x;
				double yvec = point[1] - y;
				double veclen = Math.sqrt(xvec*xvec + yvec*yvec);
				double xfull = xvec * radius/veclen;
				double yfull = yvec*  radius/veclen;
				double newvecx = xfull - xvec;
				double newvecy = yfull - yvec;
				y -= newvecy;
				
				velx -= newvecx;
				vely -= newvecy;	
				if(xfull == 0)
					vely = 0;
				velx -= velx*FRIC;
			}
		}
	}

	protected double[] detectCircle(double[] line){
		double[] point = closestPoint(line);
		double lenx = Math.abs(x-point[0]);
		double leny = Math.abs(y-point[1]);
	
		if(Math.sqrt(lenx*lenx + leny*leny) <= radius)
			return point;
		else
			return null;
	}
	
	protected double[] circleIntersect(double[] line){
		double lx1 = line[0];
		double ly1 = line[1];
		double lx2 = line[2];
		double ly2 = line[3];
		
		double x0 = x;
		double y0 = y;

		
		double dy = ly2 - ly1;
		double dx = lx2 - lx1;
		double A = dy*dy + dx*dx;;
		double B = 2 *(dx *(lx1 - x0) + dy * (ly1-y0));
		double C = (lx1 - x0) * (lx1 -x0) + (ly1 - y0) * (ly1 - y0) - radius*radius;
		double det = Math.sqrt(B*B - 4*A*C);
		gc.setFill(Color.BLUE);
		double t = (-B - det)/(2*A);
		double t2 = (-B + det)/(2*A);
		return new double[]{(lx1 + t * dx),(ly1 + t * dy),(lx1 + t2 * dx), (ly1 + t2 * dy)};
		
	}
	private double[] closestPoint(double[] line){
		double lx1 = line[0];
		double ly1 = line[1];
		double lx2 = line[2];
		double ly2 = line[3];
		
		double x0 = x;
		double y0 = y;
		
		double dy = ly2 - ly1;
		double dx = lx1 - lx2;
		double C1 = (dy)*lx1 + (dx)*ly1;
		double C2 = -dx*x0 + dy*y0;
		double det = dy*dy + dx*dx;
		double cx = 0;
		double cy = 0;
		if (det > 0){
			cx = (dy*C1 - dx*C2)/det;
			cy = (dy*C2 - -dx*C1)/det;
			//gc.fillOval(cx-2, cy-2, 4, 4);
		}else{
			return null;
		}
		if(cx >= Math.max(lx1,lx2))
			cx = Math.max(lx1,lx2);
		else if(cx <= Math.min(lx1,lx2))
			cx = Math.min(lx1,lx2);
			
		if(cy >= Math.max(ly1,ly2))
			cy = Math.max(ly1,ly2);
		else if(cy <= Math.min(ly1,ly2))
			cy = Math.min(ly1,ly2);
		return new double[]{cx,cy};
	}
	


	protected double[] detectLineCollision(double[] line1, double[] line2){
		double x1 = line1[0];
		double y1 = line1[1];
		double x2 = line1[2];
		double y2 = line1[3];
		
		double x3 = line2[0];
		double y3 = line2[1];
		double x4 = line2[2];
		double y4 = line2[3];
		
		double A1 = y2 - y1;
		double B1 = x1 - x2;
		double C1 = A1*x1 + B1*y1;
		double A2 = y4 - y3;
		double B2 = x3 - x4;
		double C2 = A2*x3 + B2*y3;
		double det = A1*B2 - A2*B1;
		
		if(det != 0){
			double xpo = (B2*C1 - B1*C2)/det;
			double ypo = (A1*C2 - A2*C1)/det;
			if(x >= Math.min(x1,x2) && x <= Math.max(x1,x2) &&
				x >= Math.min(x3,x4) && x <= Math.max(x3,x4) &&
				y >= Math.min(y1,y2) && y <= Math.max(y1,y2) &&
				y >= Math.min(y3,y4) && y <= Math.max(y3,y4))
				return new double[]{xpo,ypo};
		}
		return null;
	}
	
	public boolean feetOnGround(){
		for(GameObject go : list){
			if(go != this || go.y > this.y + radius){
				double vecx = x-go.x;
				double vecy = y+radius-go.y;
				double dist = Math.abs(Math.sqrt(Math.pow(vecx, 2)+Math.pow(vecy, 2)));
				if(dist<go.radius + radius){
					return true;
				}
			}
		}
		return false;
	}	
	public boolean headOnCeil(){
		for(GameObject go : list){
			if(go != this || go.y < this.y - radius*1.8){
				double vecx = x-go.x;
				double vecy = y-radius-go.y;
				double dist = Math.abs(Math.sqrt(Math.pow(vecx, 2)+Math.pow(vecy, 2)));
				if(dist<go.radius  + radius*0.9){
					return true;
				}
			}
		}
		return false;
	}
	
	/*public void collisionCorrection(){
		double biggestVecx=0;
		double biggestVecy=0;
		double biggestDist = 3*radius;
		double biggestRadius = 0;
		for(GameObject go : list){
			if(go != this && !(go instanceof Grenade)){
				double vecx = x-go.x;
				double vecy = y-go.y;
				double dist = Math.abs(Math.sqrt(Math.pow(vecx, 2)+Math.pow(vecy, 2)));
				if(dist<biggestDist){
					biggestDist = dist;
					biggestVecx = vecx;
					biggestVecy = vecy;
					biggestRadius = go.radius;
				}
			}
		}
		if(biggestDist < radius+biggestRadius){
			double l =radius+biggestRadius-biggestDist;
			
			velx = l*biggestVecx/Math.abs(biggestVecx);
			double yCor = l*biggestVecy/Math.abs(biggestVecy);
			y += yCor;
			if(feetOnGround() || headOnCeil()){
				vely = yCor/2;
			}
			
			x += velx;
			velx =0;
		}
	}*/

	public void addVelocity(double newVelx, double newVely) {
		velx += newVelx;
		vely += newVely;
	}
	
	
}
