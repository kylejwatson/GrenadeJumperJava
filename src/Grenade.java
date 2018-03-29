import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Grenade extends PhysicsObject {
	private static final float DETONATE_TIME = 1F;
	private static final float BLAST_RADIUS = 90F;
	private static final double EXPLOSION_FORCE = 10F;
	private static final float GRAPHICS_DELAY = 0.1F;
	private float timer = 0f;
	private float gfxTimer = 0f;
	private Clip expl;
	public Grenade(double x, double y, Engine engine) {
		super(new Image("/res/gren.png"),x,y, engine);
		try {
			expl = AudioSystem.getClip();
			URL url = Grenade.class.getResource("/res/expl.wav");
	        AudioInputStream inputStream = AudioSystem.getAudioInputStream(url);
	        expl.open(inputStream);
		} catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
		}
		try {
			clip = AudioSystem.getClip();
			URL url = Grenade.class.getResource("/res/grenhit.wav");
	        AudioInputStream inputStream = AudioSystem.getAudioInputStream(url);
	        clip.open(inputStream);
		} catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public void update(){
		
		super.update();
		timer+=0.02;
		if(timer>DETONATE_TIME){
			explode();
			if(gfxTimer != 0){
				gc.setFill(Color.YELLOW);
				gc.fillOval(x-BLAST_RADIUS, y-BLAST_RADIUS, BLAST_RADIUS*2, BLAST_RADIUS*2);
			}
		}
	}
	
	private void explode(){
		if(gfxTimer == 0f){
			if(!expl.isRunning()){
				expl.setFramePosition(0);
				expl.start();
			}
			gfxTimer += 0.02;
			for(GameObject obj:engine.list){
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
			for(double[] poly : engine.lines){
				int polyI = engine.lines.indexOf(poly);
				double material = engine.polyMat.get(polyI);
				if(material > 0){
					boolean[] movedVert = new boolean[poly.length/2];
					for(int i=0; i < poly.length -1; i+=2){
						double distx = poly[i] - x;
						double disty = poly[i+1] -y;
						double length = Math.sqrt(distx*distx+disty*disty);
						movedVert[i/2] = false;
						if(length<BLAST_RADIUS){
							distx /= length;
							disty /= length;
							poly[i] += distx*EXPLOSION_FORCE*10/material;
							poly[i+1] += disty*EXPLOSION_FORCE*10/material;
							//movedVert[i/2] = true;
							System.out.println(i);
							System.out.println(i/2);
						}
					}
					double oRadius = radius;
					radius = BLAST_RADIUS;
					if(poly.length < 500){
						boolean addedVert = false;
						ArrayList<Double> newPoly = new ArrayList<Double>();
						for(int i=0; i < poly.length -1; i+=2){
							int i2 = i+2;
							if(i2 == poly.length)
								i2 = 0;
							
							newPoly.add(poly[i]);
							newPoly.add(poly[i+1]);
							if(!movedVert[i/2] && !movedVert[i2/2]){
								System.out.println(i);
								System.out.println(i/2);
								double distx = poly[i]-poly[i2];
								double disty = poly[i+1]-poly[i2+1];
								double length = Math.sqrt(distx*distx+disty*disty);
								if(length > BLAST_RADIUS){
									double[] point = detectCircle(new double[]{poly[i],poly[i+1],poly[i2],poly[i2+1]});
									if(point != null){
										double vecx = point[0] - x;
										double vecy = point[1] -y;
										double len = Math.sqrt(vecx*vecx+vecy*vecy);
										vecx /= len;
										vecy /= len;
										point[0] += vecx*EXPLOSION_FORCE*10/material;
										point[1] += vecy*EXPLOSION_FORCE*10/material;
										newPoly.add(point[0]);
										newPoly.add(point[1]);
										
										addedVert = true;
									}
								}
							}
						}
						radius = oRadius;
						if(addedVert){
							double[] newPolyArr = new double[newPoly.size()];
							for(int i = 0; i<newPolyArr.length; i++){
								newPolyArr[i] = newPoly.get(i).doubleValue();
							}
							engine.lines.set(polyI, newPolyArr);
						}
					}
				}
			}	
		}else{
			gfxTimer += 0.02;
			if(gfxTimer>GRAPHICS_DELAY)
				engine.delList.add(this);
		}
	}
}
