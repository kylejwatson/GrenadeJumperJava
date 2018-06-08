import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class GameObject {

	protected Image img;
	protected double x,y;
	protected double radius;
	protected GraphicsContext gc;
	protected Image[] anim;
	protected int animCounter = 0;
	protected double offsety;
	protected int animSpeed = 2;

	public GameObject(Image img, double x, double y, GraphicsContext gc) {
		this.x = x;
		this.y = y;
		this.img = img;
		this.gc = gc;
		if(img != null)
			radius =( img.getWidth()+img.getHeight())/4;
	}
	public GameObject(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getDistance(GameObject go){
		return Math.sqrt(Math.pow(x-go.x,2d)+Math.pow(y-go.y,2d));
	}
	public double getDistance(double[] point){
		return Math.sqrt(Math.pow(x-point[0],2d)+Math.pow(y-point[1],2d));
	}
	
	public void update(){
		if(anim == null)
			gc.drawImage(img, x-img.getWidth()/2, y-(img.getHeight()/2)-offsety);
		else{
			gc.drawImage(anim[animCounter/animSpeed], x-img.getWidth()/2, y-(img.getHeight()/2)-offsety);
			animCounter ++;
			if(animCounter/animSpeed == anim.length)
				animCounter = 0;
		}
	}

}
