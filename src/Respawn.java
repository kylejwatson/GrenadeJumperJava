import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Respawn extends GameObject {

	public Respawn(double x, double y,GraphicsContext gc) {
		super(new Image("/res/resp.png"), x, y,gc);
		// TODO Auto-generated constructor stub
	}
	
	public void update(){
		gc.strokeOval(x-radius,y-radius,radius*2,radius*2);
		super.update();
	}

}
