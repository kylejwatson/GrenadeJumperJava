import java.util.ArrayList;

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
		timer+=0.02;
		if(timer>DETONATE_TIME){
			explode();
			if(gfxTimer != 0){
				gc.setFill(Color.YELLOW);
				gc.fillOval(x-BLAST_RADIUS/2, y-BLAST_RADIUS/2, BLAST_RADIUS, BLAST_RADIUS);
			}
		}
		super.update(gc);
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
			ArrayList<double[]> delLines = new ArrayList<double[]>();
			double oRadius = radius;
			radius = BLAST_RADIUS/2;
			for(double[] line : lines){
				double[] point = detectCircle(line);
				if(point != null){
					//delLines.add(line);
				}
			}
			for(double[] line : delLines){
				lines.remove(line);
			}
			radius = oRadius;
			
		}else{
			gfxTimer += 0.02;
			if(gfxTimer>GRAPHICS_DELAY)
				delList.add(this);
			
		}
	}

}
