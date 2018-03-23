
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
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
	private boolean drawing = false; 
	private GraphicsContext gc;
	private boolean a,d,s,w;
	private int wHeld = 0;
	private double[] newPoint;
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
			gc.save();
			gc.translate(-player.x+gc.getCanvas().getWidth()/2, -player.y+gc.getCanvas().getHeight()/2);
			if(!drawing){
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
			}
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
			gc.restore();
		} 
	};
	
	
	private EventHandler<MouseEvent> clickHandler = new EventHandler<MouseEvent>(){
		@Override
		public void handle(MouseEvent mouseEvent){
			double mx = mouseEvent.getX() +player.x-gc.getCanvas().getWidth()/2;
			double my = mouseEvent.getY() +player.y-gc.getCanvas().getHeight()/2;
			if(drawing){
				if(mouseEvent.getButton() == MouseButton.PRIMARY){
					if(newPoint == null){
						newPoint = new double[4];
						newPoint[0] = mx;
						newPoint[1] = my;
					}
					else{
						newPoint[2] = mx;
						newPoint[3] = my;
						lines.add(newPoint);
						newPoint = new double[4];
						newPoint[0] = mx;
						newPoint[1] = my;
					}
				}else if(mouseEvent.getButton() == MouseButton.SECONDARY){
					drawing = false;
					newPoint = null;
				}
			}else{
				player.mouseDown(mouseEvent);
				if(mouseEvent.getButton() == MouseButton.SECONDARY){
					drawing = true;
				}
			}
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
	
	public void writeMapData(String path){
		if(lines.size() > 0){//Check if customers have been stored
			PrintWriter pWriter = null;
			try{
				pWriter = new PrintWriter(path);//try to open the file for writing
			}catch(FileNotFoundException e){
				System.out.println("Err: FileNotFoundException");
			}
			if(pWriter != null){
				for(double[]  line: lines){
					pWriter.println();
					for(int i =0; i < 4; i++){
						pWriter.print(line[i] + ",");
					}
				}
				pWriter.close();
			}else{
				System.out.println("Err: Could not open file: " + path);
			}
		}else{
			System.out.println("Alert: Please store some customers before attempting to write them");
		}
	}
	public void readMapData(String path){
		//Create a frame for the file dialog box to get a path for the file
		
		File myFile = new File(path);
		System.out.println("Diag: File exists?: " + myFile.exists());
		Scanner fileScanner;
		
		//Try and load the file into a scanner
		try {
			fileScanner = new Scanner(myFile);
			System.out.println("Diag: Data read start");
		} catch (FileNotFoundException e) {
			System.out.println("Err: No file found");
			fileScanner = null;
		}
		
		if(fileScanner != null){
			while(fileScanner.hasNextLine()){
				//If the scanner loaded correctly read all the lines
				String myLine = fileScanner.nextLine();
				Scanner lineScanner = new Scanner(myLine);
				lineScanner.useDelimiter(",");
				double[] newLine = new double[4];
				for(int i = 0; lineScanner.hasNext(); i++){
					newLine[i] = lineScanner.nextDouble();
				}
				lineScanner.close();
				lines.add(newLine);
			}
			lines.remove(0);
			fileScanner.close();
		}
	}

	@Override
	public void stop(){
		writeMapData("myMap.txt");
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
		readMapData("myMap.txt");
		/*lines.add(new double[]{10,10,100,100});
		lines.add(new double[]{200,200,300,200});
		lines.add(new double[]{200,200,150,300});
		lines.add(new double[]{0,400,400,500});
		lines.add(new double[]{400,500,800,500});
		lines.add(new double[]{800,400,800,200});
		lines.add(new double[]{820,200,820,100});*/
		canvas.setOnMouseClicked(clickHandler);
		canvas.setOnMouseMoved(moveHandler);
		scene.setOnKeyPressed(keyDownHandler);
		scene.setOnKeyReleased(keyUpHandler);
		
		player = new TestPlayer(100,100,list,delList,lines);
		list.add(player);
		timer.start();

	}
}