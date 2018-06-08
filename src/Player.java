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
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.StrokeLineCap;

public class Player extends PhysicsObject {
	private double px = 0;
	private double py = 0;
	private double mx = 0;
	private double my = 0;
	private double[] l0 = {0,0};
	private double[] l1 = {0,0};
	private Clip clip;
	private static final float SPEED = 4;
	private static final float JUMP = 5;
	private static final double MAX_SPEED = 5;
	private static final double LEG_SPEED = 1;
	private boolean left = false;
	private Image mouthClosed;
	private Image mouthOpen;
	private GameObject mouth;
	private double legMove = 0;
	public Player(double x, double y, Engine engine) {
		super(new Image("/res/char-anim/char.png",40,40,true,false),x,y,engine);
		try {
			clip = AudioSystem.getClip();
			URL url = Grenade.class.getResource("/res/walk.wav");
	        AudioInputStream inputStream = AudioSystem.getAudioInputStream(url);
	        clip.open(inputStream);
		} catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public void addSprites(){
		if(mouth == null){
			mouthOpen = new Image("/res/char-anim/mouth.png",20,20,true,false);
			mouthClosed = new Image("/res/char-anim/mouth.png");
			mouth = new GameObject(mouthClosed,px,py,engine.gc);
			engine.list.add(mouth);
		}
	}

	public void update(){
		keyInput();
		gc.setStroke(Color.web("0x3cb878"));
		gc.setLineWidth(5);
		gc.setLineCap(StrokeLineCap.ROUND);
		gc.beginPath();
		gc.moveTo(x, y);
		gc.lineTo(l0[0], l0[1]);
		gc.moveTo(x, y);
		gc.lineTo(l1[0], l1[1]);
		gc.stroke();
		
		super.update();
		
		
		double vecx = engine.hWidth - mx;
		double vecy = engine.hHeight - my;
		double dist = Math.sqrt(vecx*vecx+vecy*vecy);
		vecx = vecx/dist;
		vecy = vecy/dist;
		vecx = -vecx;
		vecy = -vecy;
		px = x+vecx*10;
		py = y+vecy*10;
		mouth.x = px;
		mouth.y = py;
		
		
		//gc.strokeLine(x, y, px, py);
		
//		if(left){
//			gc.save();
//			gc.translate(x, 0);
//			gc.scale(-1,1);
//			gc.translate(-x, 0);
//			super.update();
//			gc.restore();
//		}else
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
				legMove += LEG_SPEED;
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
				legMove -= LEG_SPEED;
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
		double[] lleg = {x - radius - (legMove % 20)-10,y + radius*1.5, x,y};
		double[] rleg = {x + radius + (legMove % 20)+10,y + radius*1.5, x,y};
		l0[0] = lleg[0];
		l0[1] = lleg[1];
		l1[0] = rleg[0];
		l1[1] = rleg[1];
		loop1:
			for(double[] poly : engine.lines){
				for(int i=0; i < poly.length -1; i+=2){
					int i2 = i+2; 
					if(i2 == poly.length)
						i2 = 0;
					double[] line = new double[]{poly[i],poly[i+1],poly[i2],poly[i2+1]};
					double[] point = detectCircle(line);
					double[] legPoint = detectLineCollision(lleg,line);
					if(legPoint != null){
						l0 = legPoint;
					}
					legPoint = detectLineCollision(rleg,line);
					if(legPoint != null){
						l1 = legPoint;
					}
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
			legMove = -19;
			if(!clip.isRunning()){
				clip.setFramePosition(0);
				clip.start();
			}
		}
		
		radius *= 2;
		//System.out.println(legMove);
	}


	public void mouseMove(MouseEvent me){
		mx = me.getX();
		my = me.getY();
	}

	public void throwNade(double x, double y){
		double vecx = engine.hWidth - x;
		double vecy = engine.hHeight -y;
		double dist = Math.sqrt(vecx*vecx+vecy*vecy);
		vecx = vecx/dist;
		vecy = vecy/dist;
		vecx = -vecx;
		vecy = -vecy;
		Grenade g = new Grenade(this.x,this.y,engine);
		g.addVelocity(vecx*10, vecy*10);
//		double[] aimline = {x,y,x+g.velx,y+g.vely};
//		loop1:
//			for(double[] poly : engine.lines){
//				for(int i=0; i < poly.length -1; i+=2){
//					int i2 = i+2; 
//					if(i2 == poly.length)
//						i2 = 0;
//					double[] line = new double[]{poly[i],poly[i+1],poly[i2],poly[i2+1]};
//					double[] colPoint = detectLineCollision(aimline,line);
//					if(colPoint != null){
//						g.vely = 0;
//						break loop1;
//					}
//				}
//			}
		engine.list.add(g);
	}
	
	public void mouseDown(MouseEvent me, boolean open){
		if(me.getButton() == MouseButton.PRIMARY && open){
			throwNade(me.getX(),me.getY());
			mouth.img = mouthOpen;
		}else{
			mouth.img = mouthClosed;
		}
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
