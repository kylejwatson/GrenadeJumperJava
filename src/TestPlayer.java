import java.util.ArrayList;

import javafx.event.Event;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class TestPlayer extends PhysicsObject {
	private double px = 0;
	private double py = 0 ;
	private double mx = 0;
	private double my = 0 ;
	private double speed = 3;
	private double jump = 5;
	public TestPlayer(double x, double y, ArrayList<GameObject>list,ArrayList<GameObject>delList, ArrayList<double[]> lines) {
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
		if(a)
			x -= speed;
		if(d)
			x += speed;
		if(w)
			vely -= speed/10;
		if(s)
			y += speed;
		
		//if(w)
			//y -= speed;
	}
	

	public void mouseMove(MouseEvent me){
		mx = me.getX();
		my = me.getY();		
	}
	
	public void mouseDown(MouseEvent me){
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
