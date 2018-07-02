import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class GrenadeJumperJava extends Application {

	private Clip clip;
	private Clip intro;
	private Clip loop;
	private String[] maps = new String[]{"res/menu map","res/levelselect","res/metalLevel1","res/introToMat","res/myMap1.txt","res/myMap2.txt"};
	private ArrayList<Goal> goals = new ArrayList<Goal>();
	public String devMap;
	private Player player;
	private Engine engine;
	private int mapI = 0;
	private boolean camLock = false;
	private static final float PAN_SPEED = 5000;
	private boolean inMenu = false;
	private Button[] mainButtons = new Button[2];
	private Button[] levelButtons = new Button[4];
	private Button[] buttons = new Button[2];
	public class MyRunnable extends Thread {
		private boolean running = true;
	    public void run() {
	    	while(running){
				update();
				try {
					sleep(20);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//System.out.println("new loop");
			}
	    }
	    public void close(){
	    	running = false;
	    }
	}
	private MyRunnable thread = new MyRunnable();
	private Thread updateThread = new Thread(thread);

	public static void main(String[] args) {
		launch(args);
	}
	protected void update() {
		engine.update();
		if(inMenu){
			for(Button b : buttons){
				b.update();
			}
		}else{
			if(camLock){
				engine.moveCam(player.x, player.y);
				if(player.reachGoal()){
					if(!clip.isRunning()){
						clip.setFramePosition(0);
						clip.start();
					}
					mapI = 0;
					loadMenu(mainButtons);
				}
			}else{
				engine.cam.x += engine.hWidth;
				engine.cam.y += engine.hHeight;
				double dist = player.getDistance(engine.cam);
				engine.cam.x -= engine.hWidth;
				engine.cam.y -= engine.hHeight;
				double xvec = goals.get(0).x - player.x;
				double yvec = goals.get(0).y - player.y;
				//engine.gc.strokeLine(500, 315, 500-xvec/20, 315-yvec/20);
				if(dist<10)
					camLock = true;
				else if(dist < 100){
					engine.cam.x -= xvec*(dist/PAN_SPEED);
					engine.cam.y -= yvec*(dist/PAN_SPEED);
				}else{
					//System.out.println(dist);
					engine.cam.x -= xvec/(PAN_SPEED/100);
					engine.cam.y -= yvec/(PAN_SPEED/100);
				}
			}
		}
		
	}
	void setDevMap(String map){
		devMap = map;
	}
	public AnimationTimer timer = new AnimationTimer() {
		@Override
		public void handle(long now) {
			engine.draw(now);
			if(inMenu){
				for(Button b : buttons){
					b.draw();
				}
			}
			
		}
	};

	private EventHandler<MouseEvent> clickDownHandler = new EventHandler<MouseEvent>(){
		@Override
		public void handle(MouseEvent mouseEvent){
			if(inMenu){
				double mx = mouseEvent.getX();
				double my = mouseEvent.getY();
				for(Button b : buttons)
					b.mouseDown(mx,my);
			}
			if(camLock)
				player.mouseDown(mouseEvent, true);
		}
	};
	private EventHandler<MouseEvent> clickUpHandler = new EventHandler<MouseEvent>(){
		@Override
		public void handle(MouseEvent mouseEvent){
			if(inMenu){
				double mx = mouseEvent.getX();
				double my = mouseEvent.getY();
				for(Button b : buttons)
					b.mouseUp(mx,my);
			}
			if(camLock)
				player.mouseDown(mouseEvent, false);
		}
	};
	private EventHandler<MouseEvent> moveHandler = new EventHandler<MouseEvent>(){
		@Override
		public void handle(MouseEvent event) {
			if(camLock)
				player.mouseMove(event);
		}
	};
	private EventHandler<KeyEvent> keyDownHandler = new EventHandler<KeyEvent>(){
		@Override
		public void handle(KeyEvent arg0) {
			if(camLock)
				engine.keyDown(arg0.getCode());
			switch(arg0.getCode()){
			case SPACE:
				if(camLock)
					engine.w = true;
				break;
			case BACK_QUOTE:
				Random rand = new Random();

				double  x = rand.nextDouble()*engine.hWidth*2-engine.hWidth;
				double  y = rand.nextDouble()*engine.hHeight*2-engine.hHeight;
				if(camLock )
					player.throwNade(x, y);
				break;
			case R:
				reload();
				break;
			case ESCAPE:
				break;
				//case 1 2 and 3 for material
			default:
				//System.out.println(arg0.getCode());
			}
		}

	};
	private EventHandler<KeyEvent> keyUpHandler = new EventHandler<KeyEvent>(){
		@Override
		public void handle(KeyEvent arg0) {
			switch(arg0.getCode()){
			case SPACE:
				engine.w=false;
				break;
			default:
				System.out.println("not player key");
			}
			engine.keyUp(arg0.getCode());
		}

	};
	void reload() {
		System.out.println(devMap);
		if(devMap == null)
			engine.readMapData(maps[mapI]);
		else
			engine.readExternalMapData(devMap);
		goals.clear();
		for(GameObject g : engine.list){
			if(g instanceof Goal)
				goals.add((Goal)g);
		}
		camLock = true;
		if(!goals.isEmpty()){
			camLock = false;
			engine.moveCam(goals.get(0).x, goals.get(0).y);
		}
		player = new Player(100,100,engine);
		player.x = engine.resp.x;
		player.y = engine.resp.y;
		engine.addList.add(player);
		player.addSprites();
	}
	void loadMenu(Button[] mbuttons){
		reload();
		if(devMap == null){
			buttons = mbuttons;
			for(Button b : mbuttons){
				b.x -= engine.hWidth*1.5+b.img.getWidth()/2;
				b.y -= engine.hHeight*1.5+b.img.getHeight()/2;
				engine.lines.add(new double[]{b.x,b.y,b.x+b.img.getWidth(),b.y,b.x+b.img.getWidth(),b.y+b.img.getHeight(),b.x,b.y+b.img.getHeight()});
				engine.polyMat.add(0d);
				b.x += engine.hWidth*1.5+b.img.getWidth()/2;
				b.y += engine.hHeight*1.5+b.img.getHeight()/2;
			}
			engine.moveCam(-engine.hWidth/2, -engine.hHeight/2);
			inMenu = true;
			camLock = true;
		}
	}
	@Override
	public void stop(){
		System.out.println("main exit");
		loop.close();
		thread.close();
		Platform.exit();
	}
	@Override
	public void start(Stage stage) throws Exception {
		Pane root=new Pane();
		Scene scene;
		if(stage.getScene() != null){
			scene = stage.getScene();
			scene.setRoot(root);
		}else{
			scene=new Scene(root,1000,630);
			stage.setScene(scene);
		}
		stage.show();

		Canvas canvas = new Canvas(scene.getWidth(),scene.getHeight());
		root.getChildren().add(canvas);
		canvas.setOnMousePressed(clickDownHandler);
		canvas.setOnMouseReleased(clickUpHandler);
		canvas.setOnMouseMoved(moveHandler);
		scene.setOnKeyPressed(keyDownHandler);
		scene.setOnKeyReleased(keyUpHandler);
		engine = new Engine();
		engine.start(canvas);
		mainButtons[0] = new Button(new Image("/res/play.png"),engine.hWidth,engine.hHeight-100,engine.gc){
			@Override
			public void clickFunc() {
				mapI = 1;
				loadMenu(levelButtons);
			}
		};
		mainButtons[1] = new Button(new Image("/res/quit.png"),engine.hWidth,engine.hHeight+100,engine.gc){
			@Override
			public void clickFunc() {
				stop();
			}
		};
		levelButtons[0] = new Button(new Image("/res/1.png"),engine.hWidth-100,engine.hHeight-100,engine.gc){
			@Override
			public void clickFunc() {
				inMenu = false;
				mapI = 2;
				reload();
			}
		};
		levelButtons[1] = new Button(new Image("/res/2.png"),engine.hWidth+100,engine.hHeight-100,engine.gc){
			@Override
			public void clickFunc() {
				inMenu = false;
				mapI = 3;
				reload();
			}
		};
		levelButtons[2] = new Button(new Image("/res/3.png"),engine.hWidth-100,engine.hHeight+100,engine.gc){
			@Override
			public void clickFunc() {
				inMenu = false;
				mapI = 4;
				reload();
			}
		};
		levelButtons[3] = new Button(new Image("/res/4.png"),engine.hWidth+100,engine.hHeight+100,engine.gc){
			@Override
			public void clickFunc() {
				inMenu = false;
				mapI = 5;
				reload();
			}
		};
		//engine.bgs[0] = new GameObject(new Image("/res/backtometal.png"), 0,0);
		loadMenu(mainButtons);
		try {
			clip = AudioSystem.getClip();
			URL url = this.getClass().getResource("/res/goal.wav");
			AudioInputStream inputStream = AudioSystem.getAudioInputStream(url);
			clip.open(inputStream);
			intro = AudioSystem.getClip();
			url = this.getClass().getResource("/res/intro.wav");
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
			url = this.getClass().getResource("/res/cyberpunk.wav");
			inputStream = AudioSystem.getAudioInputStream(url);
			loop.open(inputStream);
		} catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
		}
		if(!intro.isRunning()){
			intro.setFramePosition(0);
			//intro.start();
		}
		loop.loop(Clip.LOOP_CONTINUOUSLY);
		timer.start();
		updateThread.start();
	}
}