package Assignment03;

public interface ApplicationConstants {

	//	world that gets mapped into the window
	float WORLD_X_MIN = -20;
	float WORLD_X_MAX = 40; 
	float WORLD_Y_MIN = -20;
	float WORLD_Y_MAX = 40;
	
	float WORLD_WIDTH = WORLD_X_MAX - WORLD_X_MIN;
	float WORLD_HEIGHT = WORLD_Y_MAX - WORLD_Y_MIN;
	
	int WINDOW_HEIGHT = 800;

	float WORLD_TO_PIXELS_SCALE = WINDOW_HEIGHT/WORLD_HEIGHT;
	float PIXELS_TO_WORLD_SCALE = 1.0f/WORLD_TO_PIXELS_SCALE;

	int WINDOW_WIDTH = (int) (WORLD_WIDTH*WORLD_TO_PIXELS_SCALE);

	//	This gives the location of the world's origin in the window,
	//	in pixel coordinates.  Note that this point could well be 
	//	outside of the window.
	float ORIGIN_X = -WORLD_TO_PIXELS_SCALE*WORLD_X_MIN;
	float ORIGIN_Y = WORLD_TO_PIXELS_SCALE*WORLD_Y_MAX;

//	float PI = (float) Math.PI;
	float G = 32.2f; // ft/s^2
}
