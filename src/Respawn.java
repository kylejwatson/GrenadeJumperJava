import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Respawn extends GameObject {

	public Respawn(double x, double y,GraphicsContext gc) {
		super(new Image("/res/resp.png"), x, y,gc);
	}
	
	public void draw(){
		gc.strokeOval(x-radius,y-radius,radius*2,radius*2);
		super.draw();
	}

}
