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
	private static final float SPEED = 5;
	private static final float JUMP = 5;
	private static final double MAX_SPEED = 10;
	private Image[] runAnim;
	private Image[] crouchAnim;
	private Image[] activeAnim;
	private boolean left = false;
	public Player(double x, double y, Engine engine) {
		super(new Image("/res/char-anim/char_animtion0_0.png"),x,y,engine);
		runAnim = new Image[]{new Image("/res/char-anim/char_animtion0_0.png"),
				new Image("/res/char-anim/char_animtion0_1.png"),
				new Image("/res/char-anim/char_animtion0_2.png"),
				new Image("/res/char-anim/char_animtion0_3.png"),
				new Image("/res/char-anim/char_animtion0_4.png"),
				new Image("/res/char-anim/char_animtion0_5.png"),
				new Image("/res/char-anim/char_animtion0_6.png"),
				new Image("/res/char-anim/char_animtion0_7.png"),
				new Image("/res/char-anim/char_animtion0_8.png")};
		crouchAnim = new Image[]{new Image("/res/char-anim/char_crouchwalk_0.png"),
				new Image("/res/char-anim/char_crouchwalk_1.png"),
				new Image("/res/char-anim/char_crouchwalk_2.png"),
				new Image("/res/char-anim/char_crouchwalk_3.png"),
				new Image("/res/char-anim/char_crouchwalk_4.png"),
				new Image("/res/char-anim/char_crouchwalk_5.png"),
				new Image("/res/char-anim/char_crouchwalk_6.png"),
				new Image("/res/char-anim/char_crouchwalk_7.png"),
				new Image("/res/char-anim/char_crouchwalk_8.png"),
				};
		activeAnim = runAnim;
		radius /=2;
		offsety = -radius*2;
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
		double vecx = engine.hWidth - mx;
		double vecy = engine.hHeight - my;
		double dist = Math.sqrt(vecx*vecx+vecy*vecy);
		vecx = vecx/dist;
		vecy = vecy/dist;
		vecx = -vecx;
		vecy = -vecy;
		px = x+vecx*50;
		py = y+vecy*50;
		gc.strokeLine(x, y, px, py);
		if(left){
			gc.save();
			gc.translate(x, 0);
			gc.scale(-1,1);
			gc.translate(-x, 0);
			super.update();
			gc.restore();
		}else
			super.update();
		y -= radius*2;
		boolean crouch = false;
		for(double[] poly : engine.lines){
			for(int i=0; i < poly.length -1; i+=2){
				int i2 = i+2; 
				if(i2 == poly.length)
					i2 = 0;
				double[] line = new double[]{poly[i],poly[i+1],poly[i2],poly[i2+1]}; 
				double[] point = detectCircle(line);
				
				if(point != null){
					crouch = true;
				}
			}
		}
		if(crouch){
			//gc.strokeOval(x-radius, y-radius, radius*2, radius*2);
			activeAnim = crouchAnim;
			img = crouchAnim[0];
			offsety = radius-10;
		}else{
			activeAnim = runAnim;
			img = runAnim[0];
			offsety = radius-20;
		}
		y += radius*2;
		gc.strokeOval(x-radius, y-radius, radius*2, radius*2);
	}

	public void keyInput(){
		radius /= 2;
		boolean totalMovable = false;
		if(engine.a){
			left = true;
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
			left = false;
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
			anim = activeAnim;
		}else{
			anim = null;
			animCounter = 0;
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
		double vecx = engine.hWidth - me.getX();
		double vecy = engine.hHeight - me.getY();
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
