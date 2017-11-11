package Assignment03;

public class Limb {
	public final static float ANGLE_INCR = 0.03f;
	public final static float XY_INCR = 0.01f;
	//
	public final static int JOINT_COLOR = 0xFFFF0000;
	public final static int LINK_COLOR = 0xFF0000FF;
	public final static float JOINT_RADIUS = 0.5f;
	public final static float JOINT_DIAMETER = 2*JOINT_RADIUS;
	public final static float LINK_THICKNESS = 0.6f;
	public final static int TARGET_COLOR = 0xFF00FF00;
	public static final float TARGET_DIAMETER = 0.5f;

	//	any angular displacement < ANGLE_TOLERANCE is ignored
	public static final float ANGLE_TOLERANCE = 0.0001f;
	//	any angular displacement > MAX_ANGLE_STEP gets scaled
	public static final float MAX_ANGLE_STEP = 0.03f;

	public static final int NUM_JOINTS = 3;
	
	//	Because joint indices are typically indexed 1..n rather than 0..n-1,
	//	I "padded" my array.  The first element at index 0 will not be used
	//					              xxxxxx ------------------> not used (padding)
	private static final float[] L = {-10000, 10, 5, 4, 4};
	//												  xx---+--> right now, not used either,
	//													   |    because NUM_JOINTS = 3
	//					        θ1      θ2     xx-----------+
	private float[] theta = {0, 0.1f, -0.3f, 0, 0};
	//					    xx --------------------------------> not used (padding)
	private float H = 10;
	
	public Limb(float x, float y) {
		
	}
	

}
