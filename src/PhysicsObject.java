import java.util.ArrayList;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class PhysicsObject extends GameObject{
	private static final double GRAV = 0.3;
	private static final double FRIC = 0.1;
	protected double velx;
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
		vely += GRAV;
		if(Math.abs(velx)<0.2)
			velx = 0;
		if(Math.abs(vely)<0.2)
			vely = 0;
		
		collisionResolution();
		
		x += velx;
		y += vely;
		//collisionCorrection();
		//gc.strokeOval(x-radius, y-0.2,radius*1.8, radius*1.8);
		super.update(gc);
	}

	private void collisionResolution(){
		for(int i = 0; i< lines.size(); i++){
			double[] point = detectCircle(lines.get(i));
			if(point[0] != x && point[1] != y){
				double xvec = point[0] - x;
				double yvec = point[1] - y;
				double veclen = Math.sqrt(xvec*xvec + yvec*yvec);
				double xfull = xvec * radius/veclen;
				double yfull = yvec*  radius/veclen;
				double newvecx = xfull - xvec;
				double newvecy = yfull - yvec;

				x -= newvecx;
				y -= newvecy;
				
				velx -= newvecx;
				vely -= newvecy;	
				if(xfull == 0)
					vely = 0;
				if(yfull != 0 && velx != 0)
					velx -= (velx/Math.abs(velx))*FRIC;
				
			}
		}
	}

	private double[] detectCircle(double[] line){
		double[] point = closestPoint(line);
		double lenx = Math.abs(x-point[0]);
		double leny = Math.abs(y-point[1]);
	
		if(Math.sqrt(lenx*lenx + leny*leny) <= radius)
			return point;
		else
			return new double[]{x,y};
	}
	private double[] closestPoint(double[] line){
		double lx1 = line[0];
		double ly1 = line[1];
		double lx2 = line[2];
		double ly2 = line[3];
		
		double x0 = x;
		double y0 = y;
		
		double A1 = ly2 - ly1;
		double B1 = lx1 - lx2;
		double C1 = (ly2 - ly1)*lx1 + (lx1 - lx2)*ly1;
		double C2 = -B1*x0 + A1*y0;
		double det = A1*A1 - -B1*B1;
		double cx = 0;
		double cy = 0;
		if (det != 0){
			cx = (A1*C1 - B1*C2)/det;
			cy = (A1*C2 - -B1*C1)/det;
		}else{
			cx = x0;
			cy = y0;
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
