import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.SVGPath;

public class Engine {
	GraphicsContext gc;
	boolean a;
	boolean d;
	boolean s;
	boolean w;
	ArrayList<double[]> lines = new ArrayList<double[]>();
	ArrayList<GameObject> list = new ArrayList<GameObject>();
	ArrayList<Double> polyMat = new ArrayList<Double>();
	ArrayList<GameObject> delList = new ArrayList<GameObject>();
	ArrayList<GameObject> addList = new ArrayList<GameObject>();
	private Color dirt = Color.web("0x947f7b");
	//private ImagePattern dirt = new ImagePattern(new Image("/res/dirt.jpg"),0,0,200,200,false);
	//private ImagePattern brick = new ImagePattern(new Image("/res/brick.jpg"),0,0,100,100,false);
	private Color brick = Color.web("0xffd943");
	//private ImagePattern wood = new ImagePattern(new Image("/res/wood.jpg"),0,0,100,100,false);
	//private ImagePattern metal = new ImagePattern(new Image("/res/metal.jpg"),0,0,100,100,false);
	private Color metal = Color.web("0x584682");
	private Color wood = Color.web("0xdc4d53");
	private Color shade = Color.web("0x000000",0.3);
	double hWidth;
	double hHeight;
	GameObject cam = new GameObject(0,0);
	GameObject resp;
	Image[] backImg = new Image[]{null,new Image("/res/metalStatic.png")};
	Image[] pBackImg = new Image[]{null,new Image("/res/metalParral.png")};
	GameObject staticBack;
	GameObject paralaxBack;
	private long lastTime;
	private double avgFps;
	private int frameCounter = 0;
	private double totalFps;
	
	public Engine() {
	}

	public void moveCam(double x, double y){
		cam.x = x-hWidth;
		cam.y = y-hHeight;//*1.5;
	}

	public void update() {
		try{
		for(GameObject obj : addList)
			list.add(obj);
		addList.clear();
		for(GameObject obj : list)
			obj.update();
		for(GameObject obj : delList)
			list.remove(obj);
		delList.clear();
		}catch(InternalError e){
			System.out.println("test error");
			e.printStackTrace();
		}
	} 
	
	public void draw(long now){
		gc.setFill(Color.WHITE);
		gc.fillRect(0,0,hWidth*2,hHeight*2);
		gc.setFill(Color.BLACK);
		gc.save();
		gc.translate(-cam.x, -cam.y);
		for(GameObject obj : list)
			obj.draw();
		delList.clear();
		int polyCount = 0;
		String[] content = new String[lines.size()];
		for(int k=0; k<lines.size(); k++){
			double[] poly = lines.get(k);
			polyCount += poly.length/2;
			//SVGPath svg = new SVGPath();
			content[k] = "M"+poly[0]+" " + poly[1];
			//svg.setContent("M"+poly[0]+"," + poly[1]);
			
			//gc.beginPath();
			//gc.moveTo(poly[0]+10, poly[1]+10);
			for(int i=2; i < poly.length -1; i+=2){
				int i2 = i+1;
				//gc.lineTo(poly[i]+10, poly[i2]+10);
				content[k] += " L " + poly[i] + " " + poly[i2];
			}
			content[k] += " Z";
			//gc.closePath();
			//svg.setContent(content)
			content[k] += polyMat.get(k);
		}
		/*
		
		
		//#shadow code!!!!!!!!!!!!!!!!#///
		 * 
		 */
//		gc.save();
//		gc.translate(10, 10);
//		for(String s: content){
//			s = s.substring(0, s.length()-3);
//			gc.beginPath();
//			gc.appendSVGPath(s);
//			gc.closePath();
//			gc.setFill(Color.BLACK);
//			gc.setGlobalAlpha(0.2);
//			gc.fill();
//		}
//		gc.restore();
		for(String s: content){
			double c = Double.valueOf(s.substring(s.length()-3));
			if(c>3)
				gc.setFill(brick);
			else if(c>2)
				gc.setFill(wood);
			else if(c>1)
				gc.setFill(dirt);
			else
				gc.setFill(metal);
			s = s.substring(0, s.length()-3);
			gc.beginPath();
			gc.appendSVGPath(s);
			gc.closePath();
			gc.fill();
		}
		gc.restore();
		double fps = 1000000000.0 / (now - lastTime);
		lastTime = now;
		totalFps += fps;
		frameCounter++;
		if(frameCounter > 7){
			avgFps = totalFps/8;
			frameCounter = 0;
			totalFps = 0;
		}
		gc.strokeText("Edge Count: " + polyCount, 30, 30);
		gc.strokeText("FPS: " + avgFps, 30, 50);
	}

	public void readMapData(String path){
		a = false;
		s = false;
		d = false;
		w = false;
		lines.clear();
		list.clear();
		polyMat.clear();
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(path);
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader fileScanner = new BufferedReader(isr);

		if(fileScanner != null){
			String myLine;
			try {
				if((myLine = fileScanner.readLine()) != null){
					Scanner lineScanner = new Scanner(myLine);
					lineScanner.useDelimiter(",");
					resp.x = lineScanner.nextDouble();
					resp.y = lineScanner.nextDouble();
					//staticBack.x = lineScanner.nextDouble();
					//staticBack.y = lineScanner.nextDouble();
					//paralaxBack.x = lineScanner.nextDouble();
					//paralaxBack.y = lineScanner.nextDouble();
					//int i = lineScanner.nextInt();
					//staticBack.img = backImg[i];
					//paralaxBack.img = pBackImg[i];
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
						Goal g = new Goal(0,0, gc);
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
				e.printStackTrace();
			}
		}
	}
	public String readExternalMapData(String path){
		return readExternalMapData(new File(path));
	}
	public String readExternalMapData(File file){
		lines.clear();
		list.clear();
		polyMat.clear();
		Scanner fileScanner = null;
		try {
			fileScanner = new Scanner(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if(fileScanner != null){
			String myLine;
			if(fileScanner.hasNextLine()){
				myLine = fileScanner.nextLine();
				Scanner lineScanner = new Scanner(myLine);
				lineScanner.useDelimiter(",");
				resp.x = lineScanner.nextDouble();
				resp.y = lineScanner.nextDouble();
				staticBack.x = lineScanner.nextDouble();
				staticBack.y = lineScanner.nextDouble();
				paralaxBack.x = lineScanner.nextDouble();
				paralaxBack.y = lineScanner.nextDouble();
				int i = lineScanner.nextInt();
				staticBack.img = backImg[i];
				paralaxBack.img = pBackImg[i];
				lineScanner.close();
			}
			boolean poly = false;
			while(fileScanner.hasNextLine()){
				myLine = fileScanner.nextLine();
				//If the scanner loaded correctly read all the lines
				Scanner lineScanner = new Scanner(myLine);
				lineScanner.useDelimiter(",");
				if(myLine.startsWith("/"))
					poly = true;
				else if(!poly){
					Goal g = new Goal(0,0, gc);
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
			return file.getAbsolutePath();
		}
		return null;
	}

	void start(Canvas canvas){
		gc = canvas.getGraphicsContext2D();
		hWidth = canvas.getWidth()/2;
		hHeight = canvas.getHeight()/2;
		Goal.graphic = new Image("/res/goal.png");
		new Grenade(0,0,this);
		resp = new Respawn(0,0,gc);
		moveCam(resp.x, resp.y);
		//updateThread.start();
	}
	void keyDown(KeyCode code) {
		switch(code){
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
		default:
			//
		}
	}
	void keyUp(KeyCode code) {
		switch(code){
		case A:
			a = false;
			break;
		case D:
			d = false;
			break;
		case W:
			w = false;
			break;
		case S:
			s = false;
			break;
		default:
			//
		}
	}
}
