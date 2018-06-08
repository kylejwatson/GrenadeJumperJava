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

public class PlayerAnim extends PhysicsObject {
	private double px = 0;
	private double py = 0;
	private double mx = 0;
	private double my = 0;
	private Clip clip;
	private static final float SPEED = 4;
	private static final float JUMP = 5;
	private static final double MAX_SPEED = 5;
	private Image[] runAnim;
	private Image[] throwAnim;
	private Image jumpImage;
	private Image[] activeAnim;
	private boolean left = false;
	public PlayerAnim(double x, double y, Engine engine) {
		super(new Image("/res/char-anim/char0.png",50,50,true,false),x,y,engine);
		runAnim = new Image[]{new Image("/res/char-anim/char0.png",50,50,true,false),
				new Image("/res/char-anim/char1.png",50,50,true,false),
				new Image("/res/char-anim/char2.png",50,50,true,false),
				new Image("/res/char-anim/char3.png",50,50,true,false),
				new Image("/res/char-anim/char4.png",50,50,true,false),
				new Image("/res/char-anim/char5.png",50,50,true,false),
				new Image("/res/char-anim/char6.png",50,50,true,false),
				new Image("/res/char-anim/char7.png",50,50,true,false),
				new Image("/res/char-anim/char8.png",50,50,true,false)};
		throwAnim = new Image[]{new Image("/res/char-anim/charthrow0.png",50,50,true,false),
				new Image("/res/char-anim/charthrow1.png",50,50,true,false),
				new Image("/res/char-anim/charthrow2.png",50,50,true,false),
				new Image("/res/char-anim/charthrow2.png",50,50,true,false)};
		
		jumpImage = new Image("/res/char-anim/charjump.png",50,50,true,false);
		activeAnim = runAnim;
		//radius /=2;
		//offsety = -radius*2;
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
		//gc.strokeLine(x, y, px, py);
		
		if(left){
			gc.save();
			gc.translate(x, 0);
			gc.scale(-1,1);
			gc.translate(-x, 0);
			super.update();
			gc.restore();
		}else
			super.update();
		
		/*y -= radius*2;
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
		//gc.strokeOval(x-radius, y-radius, radius*2, radius*2);*/
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
		if(!canJump)
			img = jumpImage;
		else
			img = runAnim[0];
		if(engine.w && canJump){	
			//anim = jumpAnim;
			vely -= JUMP;
			if(!clip.isRunning()){
				clip.setFramePosition(0);
				clip.start();
			}
		}
		
		radius *= 2;
		if(anim != throwAnim){
			if((engine.a || engine.d) && totalMovable && canJump){
				if(anim != activeAnim){
					animCounter = 0;
					anim = activeAnim;
				}
			}else 
				anim = null;
		}
		if(anim == throwAnim && animCounter/animSpeed == anim.length-1){
			anim = null;
			animCounter = 0;
		}
		/*if(!engine.a && !engine.d && anim != jumpAnim){
			anim = null;
			animCounter = 0;
		}*/
	}


	public void mouseMove(MouseEvent me){
		mx = me.getX();
		my = me.getY();		
	}

	public void throwNade(double x, double y){
		animCounter = 0;
		anim = throwAnim;
		double vecx = engine.hWidth - x;
		double vecy = engine.hHeight -y;
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
		
		if(me.getButton() == MouseButton.PRIMARY){
			throwNade(me.getX(),me.getY());
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
