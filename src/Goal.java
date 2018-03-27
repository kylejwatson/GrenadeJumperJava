import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Goal extends GameObject {

	public Goal(double x, double y) {
		super(new Image("/res/goal.png"), x, y);
		// TODO Auto-generated constructor stub
	}
	
	public void update(GraphicsContext gc){
		//gc.strokeOval(x-radius,y-radius,radius*2,radius*2);
		super.update(gc);
	}

}
