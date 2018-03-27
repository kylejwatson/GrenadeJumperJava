import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import javafx.event.Event;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class Player extends PhysicsObject {
	private double px = 0;
	private double py = 0 ;
	private double mx = 0;
	private double my = 0 ;
	private ArrayList<Double> polyMat;
	private Clip clip;
	private static final float SPEED = 3;
	private static final float JUMP = 5;
	private static final double MAX_SPEED = 4;
	public Player(double x, double y, ArrayList<GameObject>list,ArrayList<GameObject>delList, ArrayList<double[]> lines, ArrayList<Double> polyMat) {
		super(new Image("/res/char.png"),x,y,list,delList,lines);
		this.polyMat = polyMat;
		try {
			clip = AudioSystem.getClip();
			URL url = Grenade.class.getResource("/res/walk.wav");
	        AudioInputStream inputStream = AudioSystem.getAudioInputStream(url);
	        clip.open(inputStream);
		} catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
		}
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
		boolean totalMovable = false;
		if(a){
			boolean moveable = true;
			x-=radius+5;
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
							System.out.println("move a");
							break loop1;
						}
					}
				}
			x+=radius+5;
			if(moveable){
				totalMovable = true;
				if(velx - SPEED > -MAX_SPEED)
					velx -= SPEED;
				else
					velx = -MAX_SPEED;
			}
		}
		if(d){
			boolean moveable = true;
			x+=radius+5;
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
							System.out.println("move d");
							break loop1;
						}
					}
				}
			x-=radius+5;
			if(moveable){
				if(velx + SPEED < MAX_SPEED)
					velx += SPEED;
				else
					velx = MAX_SPEED;
				totalMovable = true;
			}
		}
		boolean canJump = false;
		y+=radius+1;
		radius*=1.2;
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
							canJump = true;
							break loop1;
						}
					}
				}
			}
		radius/=1.2;
		y-=radius+1;
		if(w && canJump)			
			vely -= JUMP;
		
		radius *= 2;
		if((a || d) && canJump && totalMovable){
			if(!clip.isRunning()){
				clip.setFramePosition(0);
				clip.start();
			}
		}
	}


	public void mouseMove(MouseEvent me){
		mx = me.getX();
		my = me.getY();		
	}

	public void throwNade(double x, double y){
		double vecx = x;
		double vecy = y;
		double dist = Math.sqrt(vecx*vecx+vecy*vecy);
		vecx = vecx/dist;
		vecy = vecy/dist;
		vecx = -vecx;
		vecy = -vecy;
		Grenade g = new Grenade(this.x,this.y,list,delList,lines,polyMat);
		g.addVelocity(vecx*10, vecy*10);
		list.add(g);
	}
	
	public void mouseDown(MouseEvent me){
		double vecx = 500 - me.getX();
		double vecy = 315 - me.getY();
		double dist = Math.sqrt(vecx*vecx+vecy*vecy);
		vecx = vecx/dist;
		vecy = vecy/dist;
		vecx = -vecx;
		vecy = -vecy;
		Grenade g = new Grenade(x,y,list,delList,lines,polyMat);
		if(me.getButton() == MouseButton.PRIMARY){
			g.addVelocity(vecx*10, vecy*10);
			list.add(g);
		}
	}

	public void input(Event e){
	}

	public boolean reachGoal() {
		for(GameObject g:list){
			if(g instanceof Goal){
				if(getDistance(g) < radius+g.radius)
					return true;
			}
		}
		return false;
	}

}
