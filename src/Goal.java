import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Goal extends GameObject {

	public Goal(double x, double y,GraphicsContext gc) {
		super(new Image("/res/goal.png"), x, y,gc);
		// TODO Auto-generated constructor stub
	}
	
	public void update(){
		super.update();
	}

}
