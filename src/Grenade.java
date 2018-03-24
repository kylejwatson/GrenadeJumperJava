import java.util.ArrayList;
import java.util.Arrays;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Grenade extends PhysicsObject {
	private static final float DETONATE_TIME = 1F;
	private static final float BLAST_RADIUS = 90F;
	private static final double EXPLOSION_FORCE = 10F;
	private static final float GRAPHICS_DELAY = 0.1F;
	private float timer = 0f;
	private float gfxTimer = 0f;
	public Grenade(double x, double y, ArrayList<GameObject> list, ArrayList<GameObject> delList, ArrayList<double[]> lines) {
		super(new Image("/res/gren.png"), x, y, list,delList,lines);
		
		radius = 10;
		// TODO Auto-generated constructor stub
	}
	
	public void update(GraphicsContext gc){
		super.update(gc);
		timer+=0.02;
		if(timer>DETONATE_TIME){
			explode();
			if(gfxTimer != 0){
				gc.setFill(Color.YELLOW);
				gc.fillOval(x-BLAST_RADIUS/2, y-BLAST_RADIUS/2, BLAST_RADIUS, BLAST_RADIUS);
			}
		}
	}
	
	private void explode(){
		if(gfxTimer == 0f){

			gfxTimer += 0.02;
			for(GameObject obj:list){
				double dist = getDistance(obj);
				if(dist <BLAST_RADIUS && obj != this){
					if(obj instanceof PhysicsObject){
						PhysicsObject physObj = (PhysicsObject) obj;
						double vecx = obj.x - x;
						double vecy = obj.y - y;
						double newVelx = EXPLOSION_FORCE*vecx/dist;
						double newVely = EXPLOSION_FORCE*vecy/dist;
						physObj.addVelocity(newVelx,newVely);
					}
				}
			}
			ArrayList<Integer> delLines = new ArrayList<Integer>();
			double oRadius = radius;
			radius = BLAST_RADIUS/2;
			ArrayList<double[]> addList = new ArrayList<double[]>();
			ArrayList<double[]> remList = new ArrayList<double[]>();
			System.out.println("1: " + lines.size());
			for(double[] poly : lines){
				for(int i=0; i < poly.length -2; i+=2){
					int i2 = i+2;
					double[] line = new double[]{poly[i],poly[i+1],poly[i2],poly[i2+1]}; 
					double[] point = detectCircle(line);
					if(point != null){
						delLines.add(i);
						delLines.add(i+1);
					}
				}
				if(delLines.size() >= poly.length){
					remList.add(poly);
					break;
				}
					
				double[] newPoly = new double[poly.length-delLines.size()];
				System.out.println("l1: " +poly.length + " l2: " + newPoly.length + " l3: " + delLines.size());
				int cnt = 0;
				if(newPoly.length > 0){
					for(int i = 0; i<poly.length && cnt < newPoly.length; i++){
						if(!delLines.contains(i)){
							newPoly[cnt] = poly[i];
							System.out.println("i: " +cnt);
							cnt++;
						}
					}
					addList.add(newPoly);
					remList.add(poly);
				}
			}
			System.out.println("2: " + addList.size());
			System.out.println("3: " + remList.size());
			for(int i = 0; i < addList.size(); i++){
				lines.add(addList.get(i));
			}
			for(int i = 0; i < remList.size(); i++){
				lines.remove(remList.get(i));
			}
			System.out.println("4: " + lines.size());
			//NEED TO FIX THIS FOR POLY
			
			radius = oRadius;
			
		}else{
			gfxTimer += 0.02;
			if(gfxTimer>GRAPHICS_DELAY)
				delList.add(this);
			
		}
	}

}
