
import java.awt.FileDialog;
import java.awt.Frame;
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

public class GrenadeJumperMapEditor extends Application {
	private GraphicsContext gc;
	private boolean ctrl = false;
	private ArrayList<Double> newPoly = new ArrayList<Double>();
	private ArrayList<double[]> lines = new ArrayList<double[]>();
	private ArrayList<GameObject> list = new ArrayList<GameObject>();
	private ArrayList<Double> polyMat = new ArrayList<Double>();
	private ArrayList<GameObject> delList = new ArrayList<GameObject>();
	private double x = 0;
	private double y = 0;
	private Respawn resp = new Respawn(0,0);
	private ImagePattern dirt = new ImagePattern(new Image("/res/dirt.png"),3,8,15,15,false);
	private Double curMaterial = 3D;
	private AnimationTimer timer = new AnimationTimer() {
		@Override
		public void handle(long now) {
			gc.setFill(Color.WHITE);
			gc.fillRect(0,0,gc.getCanvas().getWidth(),gc.getCanvas().getHeight());
			gc.setFill(Color.BLACK);
			gc.save();
			gc.translate(-x+gc.getCanvas().getWidth()/2, -y+gc.getCanvas().getHeight()/2);
			resp.update(gc);
			for(GameObject obj : list)
			{
				obj.update(gc);
			}
			for(GameObject obj : delList)
			{
				list.remove(obj);
			}
			delList.clear();

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
				else if(polyMat.get(k)>1)
					gc.setFill(dirt);
				else
					gc.setFill(Color.BLACK);
				gc.fill();
				gc.setFill(Color.BLACK);
			}
			gc.restore();
			gc.strokeText("Edge Count: " + polyCount+ " Current: " + curMaterial, 30, 30);
		} 
	};

	private EventHandler<MouseEvent> clickHandler = new EventHandler<MouseEvent>(){
		@Override
		public void handle(MouseEvent mouseEvent){
			double mx = mouseEvent.getX() +x-gc.getCanvas().getWidth()/2;
			double my = mouseEvent.getY() +y-gc.getCanvas().getHeight()/2;
			if(mouseEvent.getButton() == MouseButton.PRIMARY){
				if(curMaterial == -1)
					list.add(new Goal(mx,my));
				else if(curMaterial == -2){
					resp.x = mx;
					resp.y = my;
				}else if(newPoly.isEmpty() || newPoly.get(newPoly.size()-1) != my && newPoly.get(newPoly.size()-2) != mx){
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
			}
		}
	};
	private EventHandler<MouseEvent> moveHandler = new EventHandler<MouseEvent>(){
		@Override
		public void handle(MouseEvent event) {
			//
		}
	};
	private EventHandler<KeyEvent> keyDownHandler = new EventHandler<KeyEvent>(){


		@Override
		public void handle(KeyEvent arg0) {
			switch(arg0.getCode()){
			case A:
				x--;
				break;
			case D:
				x++;
				break;
			case W:
				y--;
				break;
			case S:
				if(ctrl)
					writeMapData();
				else
					y++;
				break;
			case O:
				if(ctrl)
					readMapData();
				break;
			case SPACE:
				Random rand = new Random();

				double  x = rand.nextDouble()*1000;
				double  y = rand.nextDouble()*630;
				list.add(new Grenade(x, y, list, delList, lines, polyMat));
				break;
			case DIGIT0:
				curMaterial = 0D;
				break;
			case DIGIT1:
				curMaterial = 1.5D;
				break;
			case DIGIT2:
				curMaterial = 2.5D;
				break;
			case DIGIT3:
				curMaterial = 3.5D;
				break;
			case G:
				curMaterial = -1D;
				break;
			case P:
				curMaterial = -2D;
				break;
				//case 1 2 and 3 for material
			case CONTROL:
				ctrl = true;
			default:
				System.out.println("not player key");
			}
		}

	};
	private EventHandler<KeyEvent> keyUpHandler = new EventHandler<KeyEvent>(){

		@Override
		public void handle(KeyEvent arg0) {
			switch(arg0.getCode()){
			case CONTROL:
				ctrl = false;
				break;
			default:
				System.out.println("not player key");
			}
		}

	};

	public static void main(String[] args) {
		launch(args);
	}	

	public void writeMapData(){
		//Create a frame for the file dialog box to get a path for the file
		Frame myFrame = new Frame();
		FileDialog myDial = new FileDialog(myFrame,"Open",FileDialog.LOAD);
		myDial.setVisible(true);
		String fullPath = myDial.getDirectory() + myDial.getFile();
		System.out.println("Diag: File path: " + fullPath);
		writeMapData(fullPath);
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
				pWriter.println(resp.x + "," + resp.y);
				for(GameObject g : list)
					pWriter.println(g.x + "," + g.y);
				pWriter.println("/");
				for(int i =0; i < lines.size(); i++){
					String s = polyMat.get(i)+",";
					double[] poly = lines.get(i);
					for(double pos:poly){
						s += pos + ",";
					}
					pWriter.println(s);
				}
				pWriter.close();
			}else{
				System.out.println("Err: Could not open file: " + path);
			}
		}else{
			System.out.println("Alert: Please store some customers before attempting to write them");
		}
	}
	
	public void readMapData(){
		Frame myFrame = new Frame();
		FileDialog myDial = new FileDialog(myFrame,"Open",FileDialog.LOAD);
		myDial.setVisible(true);
		String fullPath = myDial.getDirectory() + myDial.getFile();
		System.out.println("Diag: File path: " + fullPath);
		readMapData(fullPath);
	}
	
	public void readMapData(String path){
		lines.clear();
		list.clear();

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
			if(fileScanner.hasNextLine()){
				String myLine = fileScanner.nextLine();
				Scanner lineScanner = new Scanner(myLine);
				lineScanner.useDelimiter(",");
				resp.x = lineScanner.nextDouble();
				resp.y = lineScanner.nextDouble();
				lineScanner.close();
			}
			boolean poly = false;
			while(fileScanner.hasNextLine()){
				//If the scanner loaded correctly read all the lines
				String myLine = fileScanner.nextLine();
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
			fileScanner.close();
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
		canvas.setOnMouseClicked(clickHandler);
		canvas.setOnMouseMoved(moveHandler);
		scene.setOnKeyPressed(keyDownHandler);
		scene.setOnKeyReleased(keyUpHandler);
		timer.start();
	}
}