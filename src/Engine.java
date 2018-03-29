import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;

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
	private ImagePattern dirt = new ImagePattern(new Image("/res/dirt.jpg"),0,0,200,200,false);
	private ImagePattern brick = new ImagePattern(new Image("/res/brick.jpg"),0,0,100,100,false);
	private ImagePattern wood = new ImagePattern(new Image("/res/wood.jpg"),0,0,100,100,false);
	private ImagePattern metal = new ImagePattern(new Image("/res/metal.jpg"),0,0,100,100,false);
	GameObject cam = new GameObject(0,0);
	GameObject resp;
	//GameObject[] bgs = new GameObject[maps.length];
	public Engine() {
	}

	public void moveCam(double x, double y){
		cam.x = x-gc.getCanvas().getWidth()/2;
		cam.y = y-gc.getCanvas().getHeight()/2;
	}

	public void update() {
		gc.setFill(Color.WHITE);
		gc.fillRect(0,0,gc.getCanvas().getWidth(),gc.getCanvas().getHeight());
		gc.setFill(Color.BLACK);
		gc.save();
		gc.translate(-cam.x, -cam.y);
		for(GameObject obj : list)
			obj.update();
		for(GameObject obj : delList)
			list.remove(obj);
		delList.clear();
		int polyCount = 0;
		for(int k=0; k<lines.size(); k++){
			double[] poly = lines.get(k);
			polyCount += poly.length/2;
			gc.beginPath();
			gc.moveTo(poly[0], poly[1]);
			for(int i=2; i < poly.length -1; i+=2){
				int i2 = i+1;
				gc.lineTo(poly[i], poly[i2]);
			}
			gc.closePath();
			if(polyMat.get(k)>3)
				gc.setFill(brick);
			else if(polyMat.get(k)>2)
				gc.setFill(wood);
			else if(polyMat.get(k)>1)
				gc.setFill(dirt);
			else
				gc.setFill(metal);
			gc.fill();
			gc.setFill(Color.BLACK);
		}
		gc.restore();
		gc.strokeText("Edge Count: " + polyCount, 30, 30);
	} 

	public void readMapData(String path){
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
					//if(bgs[mapI] != null){
					//bgs[mapI].x = lineScanner.nextDouble();
					//bgs[mapI].y = lineScanner.nextDouble();
					//}
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
		lines.clear();
		list.clear();
		polyMat.clear();
		File file = new File(path);
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
				//if(bgs[mapI] != null){
				//bgs[mapI].x = lineScanner.nextDouble();
				//bgs[mapI].y = lineScanner.nextDouble();
				//}
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
			return path;
		}
		return null;
	}

	void start(Canvas canvas){
		gc = canvas.getGraphicsContext2D();
		resp = new Respawn(0,0,gc);
		moveCam(resp.x, resp.y);
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
