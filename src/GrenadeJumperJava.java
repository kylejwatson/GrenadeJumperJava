
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import com.sun.javafx.scene.paint.GradientUtils.Point;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class GrenadeJumperJava extends Application {
	private GraphicsContext gc;
	private boolean a,d,s,w;
	private int wHeld = 0;
	private ArrayList<double[]> lines = new ArrayList<double[]>();
	private ArrayList<GameObject> list = new ArrayList<GameObject>();
	private ArrayList<PhysicsObject> physList = new ArrayList<PhysicsObject>();
	private ArrayList<GameObject> delList = new ArrayList<GameObject>();
	private TestPlayer player;
	private AnimationTimer timer = new AnimationTimer() {
		@Override
		public void handle(long now) {
			
			gc.setFill(Color.WHITE);
			gc.fillRect(0,0,gc.getCanvas().getWidth(),gc.getCanvas().getHeight());
			gc.setFill(Color.BLACK);

			for(GameObject obj : list)
			{
				obj.update(gc);
			}
			for(GameObject obj : delList)
			{
				list.remove(obj);
			}
			delList.clear();
			for(int i = 0; i< lines.size(); i++)
			{
				gc.setStroke(Color.BLACK);
//				double[] det = detectCircle(lines.get(i),player);
//				if(det[0] != player.x && det[1] != player.y)
//					gc.setStroke(Color.BLUE);
				gc.strokeLine(lines.get(i)[0], lines.get(i)[1], lines.get(i)[2], lines.get(i)[3]);
//				gc.setStroke(Color.RED);
//				double[] point = closestPoint(player,lines.get(i));
//				gc.strokeLine(player.x, player.y, point[0], point[1]);
			}
			if(w)
				wHeld++;
			player.keyInput(a,d,s,w,wHeld);
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
				Random rand = new Random();

				double  x = rand.nextDouble()*1000;
				double  y = rand.nextDouble()*630;
				list.add(new TestPlayer(x, y, list, delList, lines));
			default:
				System.out.println("not player key");
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
				System.out.println(wHeld);
				wHeld = 0;
				w = false;
				break;
			case S:
				s = false;
				break;
			default:
				System.out.println("not player key");
			}
		}
		
	};
	
	public static void main(String[] args) {
		launch(args);
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
		lines.add(new double[]{10,10,100,100});
		lines.add(new double[]{200,200,300,200});
		lines.add(new double[]{200,200,150,300});
		lines.add(new double[]{0,400,400,500});
		lines.add(new double[]{400,500,800,400});
		lines.add(new double[]{800,400,820,200});
		lines.add(new double[]{820,200,820,100});
		canvas.setOnMouseClicked(clickHandler);
		canvas.setOnMouseMoved(moveHandler);
		scene.setOnKeyPressed(keyDownHandler);
		scene.setOnKeyReleased(keyUpHandler);
		
		player = new TestPlayer(100,100,list,delList,lines);
		list.add(player);
		timer.start();

	}
}