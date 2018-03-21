import java.util.ArrayList;

import javafx.event.Event;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class Player extends PhysicsObject {
	private double px = 0;
	private double py = 0 ;
	private double mx = 0;
	private double my = 0 ;
	private double speed = 3;
	private double jump = 5;
	public Player(double x, double y,ArrayList<GameObject> list,ArrayList<GameObject> delList,ArrayList<double[]> lines) {
		super(new Image("/res/char.png"),x,y,list,delList,lines);
		// TODO Auto-generated constructor stub
	}
	
	public void update(GraphicsContext gc){
		double vecx = x - mx;
		double vecy = y - my;
		double dist = Math.sqrt(vecx*vecx+vecy*vecy);
		vecx = vecx/dist;
		vecy = vecy/dist;
		vecx = -vecx;
		vecy = -vecy;
		px = x+vecx*50;
		py = y+vecy*50;
		gc.strokeLine(x, y, px, py);
		super.update(gc);
	}
	
	public void keyInput(boolean a,boolean d,boolean s,boolean w, int wHeld){
		if(a && canGoLeft())
			x -= speed;
		if(d && canGoRight())
			x += speed;
		if(w && feetOnGround() && wHeld == 1 && -vely < jump){
			System.out.println(wHeld);
			vely -= jump;
		}
		//if(w)
			//y -= speed;
	}
	public boolean canGoRight(){
		for(GameObject go : list){
			if(go != this){
				double vecx = x-go.x+radius;
				double vecy = y-go.y;
				double dist = Math.abs(Math.sqrt(Math.pow(vecx, 2)+Math.pow(vecy, 2)));
				if(dist<go.radius + radius*0.5){
					return false;
				}
			}
		}
		return true;
	}
	public boolean canGoLeft(){
		for(GameObject go : list){
			if(go != this){
				double vecx = x-go.x-radius;
				double vecy = y-go.y;
				double dist = Math.abs(Math.sqrt(Math.pow(vecx, 2)+Math.pow(vecy, 2)));
				if(dist<go.radius + radius*0.5){
					return false;
				}
			}
		}
		return true;
	}

	public void mouseMove(MouseEvent me){
		mx = me.getX();
		my = me.getY();		
	}
	
	public void mouseDown(MouseEvent me){
		//CHECK IF SPAWN LINE INTERSECTS DIRT BLOCK
		double vecx = x - me.getX();
		double vecy = y - me.getY();
		double dist = Math.sqrt(vecx*vecx+vecy*vecy);
		vecx = vecx/dist;
		vecy = vecy/dist;
		vecx = -vecx;
		vecy = -vecy;
		for(GameObject go : list){
			
		}
		Grenade g = new Grenade(x+vecx*(radius+8), y+vecy*(radius+8),list,delList,lines);
		if(me.getButton() == MouseButton.PRIMARY)
			g.addVelocity(vecx*10, vecy*10);
		else
			g.addVelocity(vecx*5, vecy*5);
		list.add(g);
	}
	
	public void input(Event e){
	}

}
