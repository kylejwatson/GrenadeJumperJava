
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.stage.Stage;

public class GrenadeJumperJava extends Application {
	private boolean drawing = false; 
	private GraphicsContext gc;
	private boolean a,d,s,w;
	private int wHeld = 0;
	private ArrayList<Double> newPoly = new ArrayList<Double>();
	private ArrayList<double[]> lines = new ArrayList<double[]>();
	private ArrayList<GameObject> list = new ArrayList<GameObject>();
	private ArrayList<Double> polyMat = new ArrayList<Double>();
	private ArrayList<GameObject> delList = new ArrayList<GameObject>();
	private TestPlayer player;
	private ImagePattern dirt = new ImagePattern(new Image("/res/dirt.png"),3,8,15,15,false);
	private Double curMaterial = 3D;
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
			for(int i =0; i <newPoly.size() -1; i+=2){
				gc.strokeOval(newPoly.get(i)-3, newPoly.get(i+1)-3, 6, 6);
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
					gc.setFill(Color.GRAY);
				else if(polyMat.get(k)>2)
					gc.setFill(Color.BROWN);
				else
					gc.setFill(dirt);
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
			double mx = mouseEvent.getX() +player.x-gc.getCanvas().getWidth()/2;
			double my = mouseEvent.getY() +player.y-gc.getCanvas().getHeight()/2;
			if(drawing){
				if(mouseEvent.getButton() == MouseButton.PRIMARY){
					if(newPoly.isEmpty() || newPoly.get(newPoly.size()-1) != my && newPoly.get(newPoly.size()-2) != mx){
						newPoly.add(mx);
						newPoly.add(my);
					}
				}else if(mouseEvent.getButton() == MouseButton.SECONDARY){
					if(!newPoly.isEmpty()){
						double[] arr = new double[newPoly.size()];
						for(int i =0; i<arr.length;i++)
							arr[i] = newPoly.get(i).doubleValue();
						lines.add(arr);
						polyMat.add(curMaterial);
						newPoly.clear();						
					}
					drawing = false;
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
				list.add(new Grenade(x, y, list, delList, lines, polyMat));
			//case 1 2 and 3 for material
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
				for(int i =0; i < lines.size(); i++){
					pWriter.println(polyMat.get(i));
					double[] poly = lines.get(i);
					pWriter.println();
					for(double pos:poly){
						pWriter.print(pos + ",");
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
				if(lines.size() == polyMat.size())
					polyMat.add(lineScanner.nextDouble());
				else{
					lineScanner.useDelimiter(",");
					ArrayList<Double> newLine = new ArrayList<Double>();
					while(lineScanner.hasNext()){
						newLine.add(lineScanner.nextDouble());
					}
					lineScanner.close();
					double[] arr = new double[newLine.size()];
					for(int i =0; i<arr.length;i++)
						arr[i] = newLine.get(i).doubleValue();
					lines.add(arr);
				}
			}
			if(!lines.isEmpty())
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
		
		player = new TestPlayer(100,100,list,delList,lines,polyMat);
		list.add(player);
		timer.start();

	}
}