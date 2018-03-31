import java.awt.FileDialog;
import java.awt.Frame;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
	private int p = -1;
	private GrenadeJumperJava gj;
	private boolean moving = false;
	private Boolean drawing = false;
	private Image dirt;
	private Image wood;
	private Image brick;
	private Image metal;
	

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
			engine.gc.strokeText("Current: " + curMaterial, 30, 10);
			if(g != null)
				engine.gc.drawImage(g.img,30,40);
			else{
				if(curMaterial>3)
					engine.gc.drawImage(brick,30,40);
				else if(curMaterial>2)
					engine.gc.drawImage(wood,30,40);
				else if(curMaterial>1)
					engine.gc.drawImage(dirt,30,40);
				else
					engine.gc.drawImage(metal,30,40);
			}
			engine.gc.save();
			engine.gc.translate(-engine.cam.x, -engine.cam.y);
			engine.resp.update();
			if(p>-1){
				engine.gc.beginPath();
				engine.gc.moveTo(engine.lines.get(p)[0], engine.lines.get(p)[1]);
				for(int i=2; i < engine.lines.get(p).length -1; i+=2){
					int i2 = i+1;
					engine.gc.lineTo(engine.lines.get(p)[i], engine.lines.get(p)[i2]);
				}
				engine.gc.closePath();
				engine.gc.setLineWidth(3);
				engine.gc.stroke();
				engine.gc.setLineWidth(1);
			}
			for(int i =0; i <newPoly.size() -1; i+=2){
				engine.gc.strokeOval(newPoly.get(i)-3, newPoly.get(i+1)-3, 6, 6);
			}
			if(g != null){
				engine.gc.setLineWidth(3);
				engine.gc.strokeOval(g.x-g.radius, g.y-g.radius, g.radius*2,g.radius*2);
				engine.gc.setLineWidth(1);
			}
			engine.gc.restore();
		} 
	};

	private int selectLine(double x, double y){
		for(int k=0; k< engine.lines.size(); k++){
			double[] poly = engine.lines.get(k);
			for(int i=0; i < poly.length -1; i+=2){
				double vecx = poly[i] - x;
				double vecy = poly[i+1] - y;
				double dist = Math.sqrt(vecx*vecx + vecy*vecy);
				if(dist < 10){
					return k;
				}
			}
		}
		return -1;
	}
	private GameObject selectObject(double x, double y){
		double vecx = engine.resp.x - x;
		double vecy = engine.resp.y - y;
		double dist = Math.sqrt(vecx*vecx + vecy*vecy);
		if(dist < engine.resp.radius){
			return engine.resp;
		}
		for(GameObject g:engine.list){
			vecx = g.x - x;
			vecy = g.y - y;
			dist = Math.sqrt(vecx*vecx + vecy*vecy);
			if(dist < g.radius){
				return g;
			}
		}
		return null;
	}
	private EventHandler<MouseEvent> clickHandler = new EventHandler<MouseEvent>(){
		@Override
		public void handle(MouseEvent mouseEvent){
			double mx = mouseEvent.getX() +engine.cam.x;
			double my = mouseEvent.getY() +engine.cam.y;
			if(mouseEvent.getButton() == MouseButton.PRIMARY){
				if(drawing){
					newPoly.add(mx);
					newPoly.add(my);
				}else{
					if(moving){
						moving = false;
					}else{
						g = selectObject(mx,my);
						p = selectLine(mx,my);
						if(p > -1)
							curMaterial = engine.polyMat.get(p);
					}
					if(g == null && p == -1){
						if(curMaterial == -3){
							bg.x = mx;
							bg.y = my;
						}
					}
				}
			}else if(mouseEvent.getButton() == MouseButton.SECONDARY){
				moving = false;
				p = -1;
				g = null;
				newPoly.clear();
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
				changeMat(0D);
				break;
			case DIGIT1:
				changeMat(1.5D);
				break;
			case DIGIT2:
				changeMat(2.5D);
				break;
			case DIGIT3:
				changeMat(3.5D);
				break;
			case G:
				newGoal();
				break;
			case P:
				if(ctrl)
					play();
				
				g = engine.resp;
				moving = true;
				break;
			case B:
				curMaterial = -3D;
				break;
				//case 1 2 and 3 for material
			case CONTROL:
				ctrl = true;
				break;
			case ENTER:
				endPoly();
				break;
			case DELETE:
				delete();
				break;
			case M:
				moving = true;
				break;
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
	private EventHandler<MouseEvent> moveHandler = new EventHandler<MouseEvent>(){
		@Override
		public void handle(MouseEvent event) {
			if(moving){
				if(g != null){
					g.x = event.getX() - engine.hWidth;
					g.y = event.getY() - engine.hHeight;
				}else if(p > -1){
					double xvec = engine.lines.get(p)[0] - event.getX() + engine.hWidth;
					double yvec = engine.lines.get(p)[1] - event.getY() + engine.hHeight;
					for(int i=0; i < engine.lines.get(p).length -1; i+=2){
						engine.lines.get(p)[i] -= xvec;
						engine.lines.get(p)[i+1] -= yvec;
					}
				}
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
	public void endPoly(){
		if(newPoly.size() > 2){
			double[] arr = new double[newPoly.size()];
			for(int i =0; i<arr.length;i++)
				arr[i] = newPoly.get(i).doubleValue();
			engine.lines.add(arr);
			engine.polyMat.add(curMaterial);
			p = engine.lines.size()-1;
		}
		newPoly.clear();
		drawing = false;
	}
	public void delete(){
		if(p > -1){
			engine.lines.remove(p);
			p = -1;
		}else if(g != null){
			engine.list.remove(g);
			g = null;
		}
		newPoly.clear();
		drawing = false;
	}
	public void newPoly(){
		endPoly();
		p = -1;
		g = null;
		moving = false;
		drawing = true;
	}
	public void changeMat(double mat){
		if(p > -1)
			engine.polyMat.set(p, mat);
		else if(!drawing)
			newPoly();
		curMaterial = mat;
	}
	public void newGoal(){
		g = new Goal(0,0,engine.gc);
		engine.list.add(g);
		moving = true;
		newPoly.clear();
		drawing = false;
	}
	public void play(){
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
	@Override
	public void start(Stage stage) throws Exception {
		Pane root=new Pane();
		Scene scene=new Scene(root,1000,630);
		stage.setScene(scene);
		stage.show();
		Canvas canvas = new Canvas(800,630);
		canvas.setLayoutX(200);
		root.getChildren().add(canvas);
		engine.start(canvas);
		dirt = new Image("/res/dirt.jpg",50,50,false,false);
		ImageView iv = new ImageView(dirt);
		Button dirtButton = new Button("(1)",iv);
		dirtButton.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				changeMat(1.5D);
			}
		});
		dirtButton.setLayoutX(10);
		dirtButton.setLayoutY(10);
		wood = new Image("/res/wood.jpg",50,50,false,false);
		iv = new ImageView(wood);
		Button woodButton = new Button("(2)",iv);
		woodButton.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				changeMat(2.5D);
			}
		});
		woodButton.setLayoutX(100);
		woodButton.setLayoutY(10);
		brick = new Image("/res/brick.jpg",50,50,false,false);
		iv = new ImageView(brick);
		Button brickButton = new Button("(3)",iv);
		brickButton.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				changeMat(3.5D);
			}
		});
		brickButton.setLayoutX(10);
		brickButton.setLayoutY(90);
		metal = new Image("/res/metal.jpg",50,50,false,false);
		iv = new ImageView(metal);
		Button metalButton = new Button("(0)",iv);
		metalButton.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				changeMat(0D);
			}
		});
		metalButton.setLayoutX(100);
		metalButton.setLayoutY(90);
		iv = new ImageView(Goal.graphic);
		Button goalButton = new Button("(G)",iv);
		goalButton.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				newGoal();
			}
		});
		goalButton.setLayoutX(10);
		goalButton.setLayoutY(170);
		iv = new ImageView(engine.resp.img);
		Button respButton = new Button("(P)",iv);
		respButton.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				g = engine.resp;
				moving = true;
			}
		});
		respButton.setLayoutX(110);
		respButton.setLayoutY(170);
		root.getChildren().addAll(dirtButton,woodButton,brickButton,metalButton,goalButton,respButton);
		
		Button addPoly = new Button("Start (0-3)");
		//addPoly.setDefaultButton(true);
		addPoly.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				newPoly();
			}
		});
		addPoly.setLayoutX(10);
		addPoly.setLayoutY(260);
		Button endPoly = new Button("End (Enter)");
		endPoly.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				endPoly();
			}
		});
		endPoly.setLayoutX(80);
		endPoly.setLayoutY(260);
		Button move = new Button("Move (M)");
		move.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				moving = true;
			}
		});
		move.setLayoutX(10);
		move.setLayoutY(310);
		Button delete = new Button("Delete (Del/Esc)");
		delete.setCancelButton(true);
		delete.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				delete();
			}
		});
		delete.setLayoutX(80);
		delete.setLayoutY(310);
		Button play = new Button("Test Play (Ctrl+P)");
		play.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				play();
			}
		});
		play.setLayoutX(10);
		play.setLayoutY(360);
		root.getChildren().addAll(addPoly,endPoly,move,delete,play);
		
		canvas.setOnMouseClicked(clickHandler);
		canvas.setOnMouseMoved(moveHandler);
		scene.setOnKeyPressed(keyDownHandler);
		scene.setOnKeyReleased(keyUpHandler);
		stage.setOnCloseRequest(new EventHandler<WindowEvent>(){
			@Override
			public void handle(WindowEvent arg0) {
				Platform.exit();
			}
		});
		if(curMap != null)
			engine.readExternalMapData(curMap);
		timer.start();
	}
}