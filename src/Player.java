import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import javafx.event.Event;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class Player extends PhysicsObject {
	private double px = 0;
	private double py = 0;
	private double mx = 0;
	private double my = 0;
	private Clip clip;
	private static final float SPEED = 3;
	private static final float JUMP = 5;
	private static final double MAX_SPEED = 4;
	public Player(double x, double y, Engine engine) {
		super(new Image("/res/char.png"),x,y,engine);
		try {
			clip = AudioSystem.getClip();
			URL url = Grenade.class.getResource("/res/walk.wav");
	        AudioInputStream inputStream = AudioSystem.getAudioInputStream(url);
	        clip.open(inputStream);
		} catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
		}
	}

	public void update(){
		keyInput();
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
		super.update();
	}

	public void keyInput(){
		radius /= 2;
		boolean totalMovable = false;
		if(engine.a){
			boolean moveable = true;
			x-=radius+5;
			loop1:
				for(double[] poly : engine.lines){
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
		if(engine.d){
			boolean moveable = true;
			x+=radius+5;
			loop1:
				for(double[] poly : engine.lines){
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
			for(double[] poly : engine.lines){
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
		if(engine.w && canJump){		
			vely -= JUMP;
			if(!clip.isRunning()){
				clip.setFramePosition(0);
				clip.start();
			} 
		}
		
		radius *= 2;
		if((engine.a || engine.d) && canJump && totalMovable){
			//
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
		Grenade g = new Grenade(this.x,this.y,engine);
		g.addVelocity(vecx*10, vecy*10);
		engine.list.add(g);
	}
	
	public void mouseDown(MouseEvent me){
		double vecx = 500 - me.getX();
		double vecy = 315 - me.getY();
		double dist = Math.sqrt(vecx*vecx+vecy*vecy);
		vecx = vecx/dist;
		vecy = vecy/dist;
		vecx = -vecx;
		vecy = -vecy;
		Grenade g = new Grenade(x,y,engine);
		if(me.getButton() == MouseButton.PRIMARY){
			g.addVelocity(vecx*10, vecy*10);
			engine.list.add(g);
		}
	}

	public void input(Event e){
	}

	public boolean reachGoal() {
		for(GameObject g : engine.list){
			if(g instanceof Goal){
				if(getDistance(g) < radius+g.radius)
					return true;
			}
		}
		return false;
	}

}
