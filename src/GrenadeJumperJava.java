import java.io.IOException;
import java.net.URL;
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
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class GrenadeJumperJava extends Application {

	private Clip clip;
	private Clip intro;
	private Clip loop;
	private String[] maps = new String[]{"res/metalLevel1","res/introToMat","res/myMap1.txt","res/myMap2.txt"};
	private String devMap;
	private Player player;
	private Engine engine;
	private int mapI = 0;
	private Stage stage;

	public static void main(String[] args) {
		launch(args);
	}	
	void setDevMap(String map){
		devMap = map;
	}
	private AnimationTimer timer = new AnimationTimer() {
		@Override
		public void handle(long now) {
			engine.update();
			engine.moveCam(player.x, player.y);
			if(player.reachGoal()){
				if(!clip.isRunning()){
					clip.setFramePosition(0);
					clip.start();
				}
				mapI++;
				if(mapI == maps.length)
					mapI = 0;
				reload();
			}
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
			engine.keyDown(arg0.getCode());
			switch(arg0.getCode()){
			case SPACE:
				engine.w = true;
				break;
			case BACK_QUOTE:
				Random rand = new Random();

				double  x = rand.nextDouble()*1000-500;
				double  y = rand.nextDouble()*630-315;
				player.throwNade(x, y);
				break;
			case R:
				reload();
				break;
			case ESCAPE:
				if(devMap != null){
					GrenadeJumperMapEditor g = new GrenadeJumperMapEditor(); 
					g.setMap(devMap);
					try {

						timer.stop();
						engine = null;
						g.start(stage);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
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
		player.x = engine.resp.x;
		player.y = engine.resp.y;
		engine.list.add(player);
		//engine.list.add(player);
	}
	@Override
	public void start(Stage stage) throws Exception {
		this.stage = stage;
		Pane root=new Pane();
		Scene scene=new Scene(root,1000,630);
		stage.setScene(scene);
		stage.show();
		Canvas canvas = new Canvas(1000,630);
		root.getChildren().add(canvas);
		canvas.setOnMouseClicked(clickHandler);
		canvas.setOnMouseMoved(moveHandler);
		scene.setOnKeyPressed(keyDownHandler);
		scene.setOnKeyReleased(keyUpHandler);
		engine = new Engine();
		engine.start(canvas);
		//engine.bgs[0] = new GameObject(new Image("/res/backtometal.png"), 0,0);
		
		player = new Player(100,100,engine);
		reload();
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
			url = this.getClass().getResource("/res/loop.wav");
	        inputStream = AudioSystem.getAudioInputStream(url);
	        loop.open(inputStream);
		} catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
		}
		if(!intro.isRunning()){
			intro.setFramePosition(0);
			//intro.start();
		}
		timer.start();
	}
}