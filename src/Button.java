import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;


public abstract class Button extends GameObject implements ButtonIF {

	private double mx = 0;
	private double my = 0;
	private boolean isDown = false;
	private float delayTimer = 0f;
	private static final float DELAY_TIME = 1.1F;
	private boolean clicked = false;
	public Button(Image img, double x, double y, GraphicsContext gc) {
		super(img, x, y, gc);
	}
	
	public void update(){
//		gc.setFill(Color.BLACK);
//		gc.setGlobalAlpha(0.4);
//		gc.fillRect(x-img.getWidth()/2+5,y+5-img.getHeight()/2,img.getWidth(),img.getHeight());
//		gc.setGlobalAlpha(1);
		if(isDown){
			x += 10;
			y +=10;
			super.update();
			x -= 10;
			y -= 10;
		}else
			super.update();
		if(clicked == true){
			delayTimer+=0.02;
			if(delayTimer>DELAY_TIME){
				isDown = false;
				delayTimer = 0f;
				clicked = false;
				clickFunc();
			}
		}
		//gc.fillRect(mx,my,10,10);
	}	
	
	private boolean mouseOver(double mx, double my){
		x -= img.getWidth()/2;
		y -= img.getHeight()/2;
		boolean result = mx >= x && mx <= x+img.getWidth() && y <= my && my <= y+img.getHeight();
		x += img.getWidth()/2;
		y += img.getHeight()/2;
		return result;
	}

	public void mouseDown(double mx, double my){
		this.mx = mx;
		this.my = my;
		if(!isDown && mouseOver(mx,my))
			isDown = true;
		
	}
	
	public void mouseUp(double mx, double my){
		if(isDown && mouseOver(mx,my)){
			clicked = true;
		}
	}

}
