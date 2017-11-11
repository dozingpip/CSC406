package Assignment03;

import java.util.ArrayList;

public class Body {
	ArrayList<Limb> limbs;
	Torso torso;
	float x, y, w, h;
	private static float armOffsetX = 0.5f;
	private static float armOffsetY = 0.5f;
	
	public Body(float x_, float y_, float w_, float h_) {
		x = x_;
		y = y_;
		w = w_;
		h = h_;
		limbs = new ArrayList<Limb>();
		leftArm = new Limb(x-(armOffsetX*w), y - (armOffsetY*h));
	}
}
