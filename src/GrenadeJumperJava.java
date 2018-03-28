
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.stage.Stage;

public class GrenadeJumperJava extends Application {
	private GraphicsContext gc;
	private boolean a,d,s,w;
	private int wHeld = 0;
	private ArrayList<double[]> lines = new ArrayList<double[]>();
	private ArrayList<GameObject> list = new ArrayList<GameObject>();
	private ArrayList<Double> polyMat = new ArrayList<Double>();
	private ArrayList<GameObject> delList = new ArrayList<GameObject>();
	private String[] maps = new String[]{"res/metalLevel1","res/introToMat","res/myMap1.txt","res/myMap2.txt"};
	private Player player;
	private ImagePattern dirt = new ImagePattern(new Image("/res/dirt.jpg"),0,0,200,200,false);
	private ImagePattern brick = new ImagePattern(new Image("/res/brick.jpg"),0,0,100,100,false);
	private ImagePattern wood = new ImagePattern(new Image("/res/wood.jpg"),0,0,100,100,false);
	private ImagePattern metal = new ImagePattern(new Image("/res/metal.jpg"),0,0,100,100,false);
	private GameObject[] bgs = new GameObject[maps.length];
	private int mapI = 0;
	private Clip clip;
	private Clip intro;
	private Clip loop;
	private AnimationTimer timer = new AnimationTimer() {
		@Override
		public void handle(long now) {

			if(!intro.isRunning() && !loop.isRunning()){
		        
			}
			gc.setFill(Color.WHITE);
			gc.fillRect(0,0,gc.getCanvas().getWidth(),gc.getCanvas().getHeight());
			gc.setFill(Color.BLACK);
			gc.save();
			gc.translate(-player.x+gc.getCanvas().getWidth()/2, -player.y+gc.getCanvas().getHeight()/2);
			if(bgs[mapI] != null){
				bgs[mapI].update(gc);
			}
				for(GameObject obj : list)
				{
					obj.update(gc);
				}
				for(GameObject obj : delList)
				{
					list.remove(obj);
				}
				delList.clear();
				
				if(w)
					wHeld++;
				player.keyInput(a,d,s,w,wHeld);
			
			if(player.reachGoal()){
				if(!clip.isRunning()){
					clip.setFramePosition(0);
					clip.start();
				}
				mapI++;
				if(mapI == maps.length)
					mapI = 0;
				readMapData(maps[mapI]);
			}
			int polyCount = 0;
			for(int k=0; k<lines.size(); k++){
				double[] poly = lines.get(k);
				polyCount += poly.length/2;
				gc.beginPath();
				gc.moveTo(poly[0], poly[1]);
				//gc.strokeOval(poly[0]-3, poly[1]-3, 6, 6);
				for(int i=2; i < poly.length -1; i+=2){
					int i2 = i+1;
					gc.lineTo(poly[i], poly[i2]);
					//gc.strokeOval(poly[i]-3, poly[i2]-3, 6, 6);
				}
				gc.closePath();
				if(polyMat.get(k)>3)
					gc.setFill(brick);
				else if(polyMat.get(k)>2)
					gc.setFill(wood);
				else if(polyMat.get(k)>1)
					gc.setFill(dirt);
				else
					gc.setFill(metal);
				gc.fill();
				gc.setFill(Color.BLACK);
			}
			gc.restore();
			gc.strokeText("Edge Count: " + polyCount, 30, 30);
		} 
	};
	
	private EventHandler<MouseEvent> clickHandler = new EventHandler<MouseEvent>(){
		@Override
		public void handle(MouseEvent mouseEvent){
			player.mouseDown(mouseEvent);
		}
	};
	private EventHandler<MouseEvent> moveHandler = new EventHandler<MouseEvent>(){
		@Override
		public void handle(MouseEvent event) {
			player.mouseMove(event);
		}
	};
	private EventHandler<KeyEvent> keyDownHandler = new EventHandler<KeyEvent>(){


		@Override
		public void handle(KeyEvent arg0) {
			switch(arg0.getCode()){
			case A:
				a = true;
				break;
			case D:
				d = true;
				break;
			case W:
				w = true;
				break;
			case S:
				s = true;
				break;
			case SPACE:
				w = true;
				break;
			case BACK_QUOTE:
				Random rand = new Random();

				double  x = rand.nextDouble()*1000-500;
				double  y = rand.nextDouble()*630-315;
				player.throwNade(x, y);
				break;
			case R:
				readMapData(maps[mapI]);
				break;
			//case 1 2 and 3 for material
			default:
				System.out.println(arg0.getCode());
			}
		}
		
	};
	private EventHandler<KeyEvent> keyUpHandler = new EventHandler<KeyEvent>(){

		@Override
		public void handle(KeyEvent arg0) {
			switch(arg0.getCode()){
			case A:
				a = false;
				break;
			case D:
				d = false;
				break;
			case W:
				wHeld = 0;
				w = false;
				break;
			case S:
				s = false;
				break;
			case SPACE:
				w=false;
				break;
			default:
				System.out.println("not player key");
			}
		}
		
	};
	
	public static void main(String[] args) {
		launch(args);
	}	
	
	
	public void readMapData(String path){
		lines.clear();
		list.clear();
		polyMat.clear();
		player = new Player(100,100,list,delList,lines,polyMat);
		list.add(player);
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(path);
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader fileScanner = new BufferedReader(isr);
		
		if(fileScanner != null){
			String myLine;
			try {
				if((myLine = fileScanner.readLine()) != null){
					Scanner lineScanner = new Scanner(myLine);
					lineScanner.useDelimiter(",");
					player.x = lineScanner.nextDouble();
					player.y = lineScanner.nextDouble();
					if(bgs[mapI] != null){
						bgs[mapI].x = lineScanner.nextDouble();
						bgs[mapI].y = lineScanner.nextDouble();
					}
					lineScanner.close();
				}
				boolean poly = false;
				while((myLine = fileScanner.readLine()) != null){
					//If the scanner loaded correctly read all the lines
					Scanner lineScanner = new Scanner(myLine);
					lineScanner.useDelimiter(",");
					if(myLine.startsWith("/"))
						poly = true;
					else if(!poly){
						Goal g = new Goal(0,0);
						g.x = lineScanner.nextDouble();
						g.y = lineScanner.nextDouble();
						list.add(g);
					}else{
						if(lineScanner.hasNextDouble())
							polyMat.add(lineScanner.nextDouble());
						ArrayList<Double> newLine = new ArrayList<Double>();
						while(lineScanner.hasNextDouble()){
							newLine.add(lineScanner.nextDouble());
						}
						double[] arr = new double[newLine.size()];
						for(int i =0; i<arr.length;i++)
							arr[i] = newLine.get(i).doubleValue();
						lines.add(arr);
					}
					lineScanner.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void stop(){
		//writeMapData("myMap.txt");
	}
	@Override
	public void start(Stage stage) throws Exception {
		Pane root=new Pane();
		Scene scene=new Scene(root,1000,630);
		stage.setScene(scene);
		stage.show();
		root.getChildren().addAll();
		Canvas canvas = new Canvas(1000,630);
		gc = canvas.getGraphicsContext2D();
		gc.setFill(Color.WHITE);
		gc.fillRect(0,0,canvas.getWidth(),canvas.getHeight());
		root.getChildren().add(canvas);
		bgs[0] = new GameObject(new Image("/res/backtometal.png"), 0,0);
		readMapData(maps[mapI]);
		canvas.setOnMouseClicked(clickHandler);
		canvas.setOnMouseMoved(moveHandler);
		scene.setOnKeyPressed(keyDownHandler);
		scene.setOnKeyReleased(keyUpHandler);
		timer.start();
		try {
			clip = AudioSystem.getClip();
			URL url = Grenade.class.getResource("/res/goal.wav");
	        AudioInputStream inputStream = AudioSystem.getAudioInputStream(url);
	        clip.open(inputStream);
			intro = AudioSystem.getClip();
			url = Grenade.class.getResource("/res/intro.wav");
	        inputStream = AudioSystem.getAudioInputStream(url);
	        intro.open(inputStream);
	        intro.addLineListener(new LineListener(){
				@Override
				public void update(LineEvent arg0) {
					if(arg0.getType() == LineEvent.Type.STOP){
						loop.loop(Clip.LOOP_CONTINUOUSLY);
				        System.out.println("llop");
					}
				}
	        });
			loop = AudioSystem.getClip();
			url = Grenade.class.getResource("/res/loop.wav");
	        inputStream = AudioSystem.getAudioInputStream(url);
	        loop.open(inputStream);
		} catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
		}
		if(!intro.isRunning()){
			intro.setFramePosition(0);
			intro.start();
		}
	}
}