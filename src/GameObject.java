import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class GameObject {

	protected Image img;
	protected double x,y;
	protected double radius;
	
	public GameObject(Image img, double x, double y) {
		// TODO Auto-generated constructor stub
		this.x = x;
		this.y = y;
		this.img = img;
		radius =( img.getWidth()+img.getHeight())/4;
		
	}
	
	public double getDistance(GameObject go){
		return Math.sqrt(Math.pow(x-go.x,2d)+Math.pow(y-go.y,2d));
	}
	
	public void update(GraphicsContext gc){
		gc.drawImage(img, x-img.getWidth()/2, y-img.getHeight()/2);
		//gc.strokeOval(x-radius, y-radius, radius*2, radius*2);
		//gc.strokeOval(x-radius, y+radius/2, radius*1.8, radius*1.8);
	}

}
