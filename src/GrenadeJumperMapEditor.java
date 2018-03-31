import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GrenadeJumperMapEditor extends Application {
	private Engine engine = new Engine();
	private boolean ctrl = false;
	private ArrayList<Double> newPoly = new ArrayList<Double>();
	private String curMap;
	private boolean backMoving = false;
	private boolean parMoving = false;
	private Double curMaterial = 0D;
	private int curBack = 0;
	private GameObject g;
	private int p = -1;
	private GrenadeJumperJava gj;
	private boolean moving = false;
	private Boolean drawing = false;
	private Image dirt;
	private Image wood;
	private Image brick;
	private Image metal;
	private Stage stage;
	

	public static void main(String[] args) {
		launch(args);
	}	
	private AnimationTimer timer = new AnimationTimer() {
		@Override
		public void handle(long now) {
			double difx = 0;
			double dify = 0;
			if(engine.a)
				 difx -= 10;
			if(engine.d)
				difx += 10;
			if(engine.w)
				dify -= 10;
			if(engine.s)
				dify += 10;
			
			if(backMoving){
				engine.staticBack.x += difx;
				engine.staticBack.y += dify;
			}else if(parMoving){
				engine.paralaxBack.x += difx;
				engine.paralaxBack.y += dify;
			}else{
				engine.cam.x += difx;
				engine.cam.y += dify;
			}
			
			engine.update();
			engine.gc.strokeText("Current: " + curMap, 30, 10);
			if(g != null)
				engine.gc.drawImage(g.img,30,40);
			else{
				if(curMaterial==3.5)
					engine.gc.drawImage(brick,30,40);
				else if(curMaterial==2.5)
					engine.gc.drawImage(wood,30,40);
				else if(curMaterial==1.5)
					engine.gc.drawImage(dirt,30,40);
				else if(curMaterial==0)
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
			if(!ctrl)
				engine.keyDown(arg0.getCode());
			switch(arg0.getCode()){
			case S:
				if(ctrl)
					writeMapData();
				ctrl = false;
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
				else{
					g = engine.resp;
					moving = true;
				}
				ctrl = false;
				break;
			case B:
				if(ctrl){
					parMoving = !parMoving;
					backMoving = false;
				}else{
					backMoving = !backMoving;
					parMoving = false;
				}
				ctrl = false;
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
					g.x = event.getX() + engine.cam.x;
					g.y = event.getY() + engine.cam.y;
				}else if(p > -1){
					double xvec = engine.lines.get(p)[0] - event.getX() - engine.cam.x;
					double yvec = engine.lines.get(p)[1] - event.getY() - engine.cam.y;
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
		FileChooser fileC = new FileChooser();
		fileC.setTitle("Save Map File");
		if(curMap != null){
			File f = new File(curMap);
			if(f.exists())
				fileC.setInitialDirectory(f.getParentFile());
		}
		curMap = writeMapData(fileC.showSaveDialog(stage));
		
	}
	
	public String writeMapData(File file){
		return writeMapData(file.getAbsolutePath());
	}
	public String writeMapData(String path){
		if(engine.lines.size() > 0){//Check if customers have been stored
			PrintWriter pWriter = null;
			try{
				pWriter = new PrintWriter(path);//try to open the file for writing
			}catch(FileNotFoundException e){
				System.out.println("Err: FileNotFoundException");
			}
			if(pWriter != null){
				pWriter.println(engine.resp.x + "," + engine.resp.y +","+engine.staticBack.x+","+engine.staticBack.y+","+engine.paralaxBack.x+","+engine.paralaxBack.y+","+curBack);
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
				return path;
			}else{
				System.out.println("Err: Could not open file: " + path);
			}
		}else{
			System.out.println("Alert: Please store some customers before attempting to write them");
		}
		return null;
	}
	
	public void readMapData(){
		FileChooser fileC = new FileChooser();
		fileC.setTitle("Open Map File");
		if(curMap != null){
			File f = new File(curMap);
			if(f.exists())
				fileC.setInitialDirectory(f.getParentFile());
		}
		curMap = engine.readExternalMapData(fileC.showOpenDialog(stage));
		p = -1;
		g = null;
		//stage.requestFocus();
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
		File temp = null;
		try {
			temp = File.createTempFile("temp-map", ".tmp");
			temp.deleteOnExit();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		writeMapData(temp);
		
		gj = new GrenadeJumperJava();
		gj.setDevMap(temp.getAbsolutePath());
		Pane root = new Pane();
		Scene scene=new Scene(root,1280,720);
		
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
		this.stage = stage;
		Pane root=new Pane();
		Scene scene=new Scene(root,1000,630);
		stage.setScene(scene);
		stage.show();
		Canvas canvas = new Canvas(800,630);
		canvas.setLayoutX(200);
		root.getChildren().add(canvas);
		VBox sideBar = new VBox();
		sideBar.setSpacing(20);
		sideBar.setPadding(new Insets(30,10,10,10));
		root.getChildren().add(sideBar);
		engine.start(canvas);
		TilePane matPane = new TilePane();
		matPane.setPrefColumns(2);
		//matPane.setPadding(new Insets(10,10,10,10));
		sideBar.getChildren().add(matPane);
		dirt = new Image("/res/dirt.jpg",50,50,false,false);
		ImageView iv = new ImageView(dirt);
		Button dirtButton = new Button("(1)",iv);
		dirtButton.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				changeMat(1.5D);
			}
		});
		matPane.getChildren().add(dirtButton);
		wood = new Image("/res/wood.jpg",50,50,false,false);
		iv = new ImageView(wood);
		Button woodButton = new Button("(2)",iv);
		woodButton.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				changeMat(2.5D);
			}
		});
		matPane.getChildren().add(woodButton);
		brick = new Image("/res/brick.jpg",50,50,false,false);
		iv = new ImageView(brick);
		Button brickButton = new Button("(3)",iv);
		brickButton.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				changeMat(3.5D);
			}
		});
		matPane.getChildren().add(brickButton);
		metal = new Image("/res/metal.jpg",50,50,false,false);
		iv = new ImageView(metal);
		Button metalButton = new Button("(0)",iv);
		metalButton.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				changeMat(0D);
			}
		});
		matPane.getChildren().add(metalButton);
		iv = new ImageView(Goal.graphic);
		Button goalButton = new Button("(G)",iv);
		goalButton.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				newGoal();
			}
		});
		matPane.getChildren().add(goalButton);
		iv = new ImageView(engine.resp.img);
		Button respButton = new Button("(P)",iv);
		respButton.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				g = engine.resp;
				moving = true;
			}
		});
		matPane.getChildren().add(respButton);
		//pane.getChildren().addAll(dirtButton,woodButton,brickButton,metalButton,goalButton,respButton);
		

		GridPane pane = new GridPane();
		//pane.setPadding(new Insets(10,10,10,10));
		pane.setHgap(5);
		pane.setVgap(5);
		sideBar.getChildren().add(pane);
		Button addPoly = new Button("Start (0-3)");
		//addPoly.setDefaultButton(true);
		addPoly.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				newPoly();
			}
		});
		pane.add(addPoly, 0, 3);
		Button endPoly = new Button("End (Enter)");
		endPoly.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				endPoly();
			}
		});
		pane.add(endPoly, 1, 3);
		Button move = new Button("Move (M)");
		move.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				moving = true;
			}
		});
		pane.add(move, 0, 4);
		Button delete = new Button("Delete (Del/Esc)");
		delete.setCancelButton(true);
		delete.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				delete();
			}
		});
		pane.add(delete, 1, 4);
		
		VBox menu = new VBox();
		menu.setSpacing(10);
		sideBar.getChildren().add(menu);
		Button play = new Button("Test Play (Ctrl+P)");
		play.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				play();
			}
		});
		menu.getChildren().add(play);
		Button open = new Button("Open (Ctrl+O)");
		open.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				readMapData();
			}
		});
		menu.getChildren().add(open);
		Button save = new Button("Save (Ctrl+S)");
		save.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				writeMapData();
			}
		});
		menu.getChildren().add(save);
		Label label = new Label("Backgrounds");
		menu.getChildren().add(label);
		ChoiceBox<String> backs = new ChoiceBox<String>();
		backs.getItems().addAll("None","/res/backtometal.png");
		backs.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>(){
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				engine.staticBack.img = engine.backImg[(int) newValue];
				engine.paralaxBack.img = engine.pBackImg[(int) newValue];
				curBack = (int) newValue;
			}
		});
		menu.getChildren().add(backs);
		Button backMove = new Button("Move Background (B)");
		backMove.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				backMoving = !backMoving;
				parMoving = false;
			}
		});
		menu.getChildren().add(backMove);
		Button parMove = new Button("Move Paralax (Ctrl+B)");
		parMove.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				parMoving = !parMoving;
				backMoving = false;
			}
		});
		menu.getChildren().add(parMove);
		Label label2 = new Label("Preset Maps");
		menu.getChildren().add(label2);
		ChoiceBox<String> presetMaps = new ChoiceBox<String>();
		presetMaps.getItems().addAll("res/metalLevel1","res/introToMat","res/myMap1.txt","res/myMap2.txt");
		presetMaps.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>(){
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if(curMap != null)
					writeMapData(curMap);
				else if(!engine.lines.isEmpty()){
					writeMapData();
				}
				engine.readMapData(newValue);			
			}
		});
		menu.getChildren().add(presetMaps);
		//pane.getChildren().addAll(addPoly,endPoly,move,delete,play);

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