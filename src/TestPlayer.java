import java.util.ArrayList;

import javafx.event.Event;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class TestPlayer extends PhysicsObject {
	private double px = 0;
	private double py = 0 ;
	private double mx = 0;
	private double my = 0 ;
	private static final float SPEED = 4;
	private static final float JUMP = 5;
	private static final double MAX_SPEED = 5;
	public TestPlayer(double x, double y, ArrayList<GameObject>list,ArrayList<GameObject>delList, ArrayList<double[]> lines) {
		super(new Image("/res/char.png"),x,y,list,delList,lines);
		// TODO Auto-generated constructor stub
	}

	public void update(GraphicsContext gc){
		this.gc = gc;
		double vecx = 500 - mx;
		double vecy = 315 - my;
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
		radius /= 2;
		if(a){
			boolean moveable = true;
			x-=radius+1;
			loop1:
				for(double[] poly : lines){
					for(int i=0; i < poly.length -1; i+=2){
						int i2 = i+2; 
						if(i2 == poly.length)
							i2 = 0;
						double[] line = new double[]{poly[i],poly[i+1],poly[i2],poly[i2+1]}; 
						double[] point = detectCircle(line);
						if(point != null){
							moveable = false;
							break loop1;
						}
					}
				}
			x+=radius+1;
			if(moveable){
				if(velx - SPEED > -MAX_SPEED)
					velx -= SPEED;
				else
					velx = -MAX_SPEED;
			}
		}
		if(d){
			boolean moveable = true;
			x+=radius+1;
			loop1:
				for(double[] poly : lines){
					for(int i=0; i < poly.length -1; i+=2){
						int i2 = i+2; 
						if(i2 == poly.length)
							i2 = 0;
						double[] line = new double[]{poly[i],poly[i+1],poly[i2],poly[i2+1]}; 
						double[] point = detectCircle(line);
						if(point != null){
							moveable = false;
							break loop1;
						}
					}
				}
			x-=radius+1;
			if(moveable){
				if(velx + SPEED < MAX_SPEED)
					velx += SPEED;
				else
					velx = MAX_SPEED;
			}
		}
		if(w){
			y+=radius+1;
			loop1:
				for(double[] poly : lines){
					for(int i=0; i < poly.length -1; i+=2){
						int i2 = i+2; 
						if(i2 == poly.length)
							i2 = 0;
						double[] line = new double[]{poly[i],poly[i+1],poly[i2],poly[i2+1]}; 
						double[] point = detectCircle(line);
						if(point != null){
							if(point[1] > y){
								vely -= JUMP;
								break loop1;
							}
						}
					}
				}
			y-=radius+1;
		}
		radius *= 2;
	}


	public void mouseMove(MouseEvent me){
		mx = me.getX();
		my = me.getY();		
	}

	public void mouseDown(MouseEvent me){
		double vecx = 500 - me.getX();
		double vecy = 315 - me.getY();
		double dist = Math.sqrt(vecx*vecx+vecy*vecy);
		vecx = vecx/dist;
		vecy = vecy/dist;
		vecx = -vecx;
		vecy = -vecy;
		Grenade g = new Grenade(x,y,list,delList,lines);
		if(me.getButton() == MouseButton.PRIMARY){
			g.addVelocity(vecx*10, vecy*10);
			list.add(g);
		}
		//else
		//g.addVelocity(vecx*5, vecy*5);
	}

	public void input(Event e){
	}

}
