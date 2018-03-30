import java.awt.FileDialog;
import java.awt.Frame;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GrenadeJumperMapEditor extends Application {
	private Engine engine = new Engine();
	private boolean ctrl = false;
	private ArrayList<Double> newPoly = new ArrayList<Double>();
	private String curMap;
	private GameObject bg = new GameObject(new Image("/res/backtometal.png"), 0,0,engine.gc);
	private Double curMaterial = 3D;
	private GameObject g;
	private Double[] p;
	private GrenadeJumperJava gj;

	public static void main(String[] args) {
		launch(args);
	}	
	private AnimationTimer timer = new AnimationTimer() {
		@Override
		public void handle(long now) {
			if(engine.a)
				 engine.cam.x -= 10;
			if(engine.d)
				 engine.cam.x += 10;
			if(engine.w)
				 engine.cam.y -= 10;
			if(engine.s)
				 engine.cam.y += 10;
			
			engine.update();
			engine.gc.save();
			engine.gc.translate(-engine.cam.x, -engine.cam.y);
			engine.resp.update();
			for(int i =0; i <newPoly.size() -1; i+=2){
				engine.gc.strokeOval(newPoly.get(i)-3, newPoly.get(i+1)-3, 6, 6);
			}
			engine.gc.restore();
			engine.gc.strokeText("Current: " + curMaterial, 30, 10);
		} 
	};

	private EventHandler<MouseEvent> clickHandler = new EventHandler<MouseEvent>(){
		@Override
		public void handle(MouseEvent mouseEvent){
			double mx = mouseEvent.getX() +engine.cam.x;
			double my = mouseEvent.getY() +engine.cam.y;
			if(mouseEvent.getButton() == MouseButton.PRIMARY){
				if(curMaterial == -1){
					engine.list.add(new Goal(mx,my,engine.gc));
				}
				else if(curMaterial == -2){
					engine.resp.x = mx;
					engine.resp.y = my;
				}else if(curMaterial == -3){
					bg.x = mx;
					bg.y = my;
				}else if(newPoly.isEmpty() || newPoly.get(newPoly.size()-1) != my && newPoly.get(newPoly.size()-2) != mx){
					newPoly.add(mx);
					newPoly.add(my);
				}
			}else if(mouseEvent.getButton() == MouseButton.SECONDARY){
				ArrayList<Integer> del = new ArrayList<Integer>();
				for(int k=0; k< engine.lines.size(); k++){
					double[] poly = engine.lines.get(k);
					for(int i=0; i < poly.length -1; i+=2){
						double vecx = poly[i] - mx;
						double vecy = poly[i+1] - my;
						double dist = Math.sqrt(vecx*vecx + vecy*vecy);
						if(dist < 10){
							del.add(k);
						}
					}
				}
				for(int i : del){
					engine.lines.remove(i);
					engine.polyMat.remove(i);
				}
				for(GameObject g:engine.list){
					double vecx = g.x - mx;
					double vecy = g.y - my;
					double dist = Math.sqrt(vecx*vecx + vecy*vecy);
					if(dist < g.radius){
						engine.delList.add(g);
					}
				}
			}
		}
	};
	private EventHandler<KeyEvent> keyDownHandler = new EventHandler<KeyEvent>(){
		@Override
		public void handle(KeyEvent arg0) {
			engine.keyDown(arg0.getCode());
			switch(arg0.getCode()){
			case S:
				if(ctrl)
					writeMapData();
				ctrl = false;
				System.out.println(ctrl);
				break;
			case O:
				if(ctrl)
					readMapData();
				ctrl = false;
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
				if(ctrl){
					gj = new GrenadeJumperJava();
					gj.setDevMap(curMap);
					Pane root = new Pane();
					Scene scene=new Scene(root,500,315);
					
					Stage stage = new Stage();
					stage.setScene(scene);
					try {
						gj.start(stage);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
				curMaterial = -2D;
				break;
			case B:
				curMaterial = -3D;
				break;
				//case 1 2 and 3 for material
			case CONTROL:
				ctrl = true;
			case ENTER:
				if(newPoly.size() > 2){
					double[] arr = new double[newPoly.size()];
					for(int i =0; i<arr.length;i++)
						arr[i] = newPoly.get(i).doubleValue();
					engine.lines.add(arr);
					engine.polyMat.add(curMaterial);
					newPoly.clear();						
				}
			default:
				//System.out.println("not player key");
			}
		}

	};
	private EventHandler<KeyEvent> keyUpHandler = new EventHandler<KeyEvent>(){
		@Override
		public void handle(KeyEvent arg0) {
			engine.keyUp(arg0.getCode());
			switch(arg0.getCode()){
			case CONTROL:
				ctrl = false;
				break;
			default:
				System.out.println("not player key");
			}
		}
	};
	
	public void writeMapData(){
		//Create a frame for the file dialog box to get a path for the file
		Frame myFrame = new Frame();
		FileDialog myDial = new FileDialog(myFrame,"Open",FileDialog.LOAD);
		myDial.setVisible(true);
		String fullPath = myDial.getDirectory() + myDial.getFile();
		myFrame.dispose();
		writeMapData(fullPath);
	}
	public void writeMapData(String path){
		if(engine.lines.size() > 0){//Check if customers have been stored
			PrintWriter pWriter = null;
			try{
				pWriter = new PrintWriter(path);//try to open the file for writing
			}catch(FileNotFoundException e){
				System.out.println("Err: FileNotFoundException");
			}
			if(pWriter != null){
				pWriter.println(engine.resp.x + "," + engine.resp.y +","+bg.x+","+bg.y);
				for(GameObject g : engine.list)
					pWriter.println(g.x + "," + g.y);
				pWriter.println("/");
				for(int i =0; i < engine.lines.size(); i++){
					String s = engine.polyMat.get(i)+",";
					double[] poly = engine.lines.get(i);
					for(double pos:poly){
						s += pos + ",";
					}
					pWriter.println(s);
				}
				pWriter.close();
				curMap = path;
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
		myFrame.dispose();
		curMap = engine.readExternalMapData(fullPath);
	}
	@Override
	public void start(Stage stage) throws Exception {
		Pane root=new Pane();
		Scene scene=new Scene(root,1000,630);
		stage.setScene(scene);
		stage.show();
		Canvas canvas = new Canvas(1000,630);
		root.getChildren().add(canvas);
		canvas.setOnMouseClicked(clickHandler);
		scene.setOnKeyPressed(keyDownHandler);
		scene.setOnKeyReleased(keyUpHandler);
		stage.setOnCloseRequest(new EventHandler<WindowEvent>(){
			@Override
			public void handle(WindowEvent arg0) {
				Platform.exit();
			}
		});
		engine.start(canvas);
		if(curMap != null)
			engine.readExternalMapData(curMap);
		timer.start();
	}
}