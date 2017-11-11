package Assignment03;


import processing.core.PApplet;

import java.awt.geom.Point2D;
import java.util.Vector;

import Jama.*;

//NEW	
//	This version supports a manipulator with a 3D workspace and joint space.
//	The inverse kinematics implemented allows us to adjust the orientation of the
//	tip of the manipulator in addition to its position. (check out handleKeyPressed for that).
//
//	A line //NEW
//	indicates a block that has changed from V1

public class Week08NewArmV2 extends PApplet implements ApplicationConstants {

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

	//	workspace is the x-y-ψ space, dimension = 3
	private static final int DIM_WORKSPACE = 3;
	//	I store in these variables the coordinates of the tip of the manipulator
	private Point2D.Float toolPos_; 
	private float toolAngle_; 

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


	
	public void settings() 
	{
		size(WINDOW_WIDTH, WINDOW_HEIGHT);
	}

	public void setup() {
		//	Verify that joint arrays dimensions and indices are ok.
		if (L.length != theta.length)
		{
			println("Dimensions of link length and joint angle arrays don't match");
			System.exit(1);
		}
	}

	/**	Converts pixel coordinates into world coordinates
	 * 
	 * @param ix	x coordinate of a window pixel
	 * @param iy	y coordinate of a window pixel
	 * @return	Corresponding world coordinates stored into a Point2D.Float object
	 */
	public static Point2D.Float pixelToWorld(int ix, int iy) 
	{
		return new Point2D.Float((ix-ORIGIN_X)*PIXELS_TO_WORLD_SCALE, -(iy-ORIGIN_Y)*PIXELS_TO_WORLD_SCALE);
	}
	
	public void draw() {

		background(167);

		translate(ORIGIN_X, ORIGIN_Y);

		//	change to world units
		scale(WORLD_TO_PIXELS_SCALE, -WORLD_TO_PIXELS_SCALE);	

		//	Draw a horizontal line for the "ground"
		stroke(0);
		line(WORLD_X_MIN, 0, WORLD_X_MAX, 0);

		drawArm();
				
		// draw a point at the current location of the mouse pointer
		Point2D.Float target = pixelToWorld(mouseX, mouseY);
		noStroke();
		fill(TARGET_COLOR);
		ellipse(target.x, target.y, TARGET_DIAMETER, TARGET_DIAMETER);		
	
		//	As a general rule, keyPressed and mousePressed are better handled from within the draw
		//	method: if (mousePressed) handleMousePressed();
		//	whereas discrete events *mouse released, keyTyped should be handled by callback functions.
		if (mousePressed) {
			handleMousePressed();
		}
		if (keyPressed) {
			handleKeyPressed();
		}
	}


	/**	
	 * Draws the kinematic chain
	 */
	private void drawArm() 
	{
		pushMatrix();

		//--------------------------------------
		//	I draw the base (Link 0) as a line.
		//	I could paint it as a complex shape
		//--------------------------------------
		stroke(LINK_COLOR);
		strokeWeight(LINK_THICKNESS);
		line(0, 0, 0, H);
		noStroke();
		translate(0, H);
		
		//NEW
		//	The tool angle is the sum of all joint angles along the kinematic chain
		toolAngle_ = 0;
		
		for (int k=1; k<=NUM_JOINTS; k++) 
		{
			//--------------------------------------
			//	apply the rotation at Joint k
			//--------------------------------------
			rotate(theta[k]);
			toolAngle_ += theta[k];
					
			//--------------------------------------
			//	I draw Link k as a line.
			//	I could paint it as a complex shape
			//--------------------------------------
			stroke(LINK_COLOR);
			strokeWeight(LINK_THICKNESS);
			line(0, 0, L[k], 0);
			
			//--------------------------------------
			//	I draw Joint k as a disk
			//--------------------------------------
			noStroke();
			fill(JOINT_COLOR);
			ellipse(0, 0, JOINT_DIAMETER, JOINT_DIAMETER);		
			
			//--------------------------------------
			//	move to the end of the link
			//--------------------------------------
			translate(L[k], 0);
		}
		
		// When we leave the loop, and before we pop the matrix, the current
		// reference frame is the one we find ourselves at following the final translate
		//	statement, which put us at the end of the very last link, that is, at
		//	the tip of the manipulator, and with a local x direction aligned with the
		//	last link
		
		// I don't use the PMatrix object for anything other than extracting its array
		//	of float values, so no need to create a variable for that.
		float []toolMatrix = {1, 0, 0, 0, 1, 0};
		getMatrix().get(toolMatrix);
		
		// Explanation of what just happened:  The matrix that we get here is the global transformation
		//	matrix between the window reference frame and the reference frame at the tip of the arm.
		//      ( m11  m12  m13 )
		//	M = ( m21  m22  m23 )
		//      (  0    0    1  )
		// the rightmost column of M gives us the coordinates of the tip of the arm in window (pixel coordinates)
		// the array toolMatrix that we extracted is the "flattened" version of the first 2 rows of M:
		// toolMatrix = {m11, m12, m13, m21, m22, m23}.  The elements we want are at indices 2 and 5
		// We convert the coordinates into world coordinates using the finction we coded earlier.
		toolPos_ = pixelToWorld((int) toolMatrix[2], (int) toolMatrix[5]);
		
		//	So now toolX and toolY store the coordinates of the tip of the arm in world coordinate system.
		
		popMatrix();		
	}
		
	//NEW
	public void handleMousePressed() {
		Point2D.Float target = pixelToWorld(mouseX, mouseY);

		//	Step 1: Compute global displacement in the workspace
		float dx = target.x - toolPos_.x;
		float dy = target.y - toolPos_.y;

		//	Step 2:  We compute the joint displacement that would achieve this workspace displacement.
		//	Note that there is a problem here: the desired workspace displacement could be very large,
		//	while our equation with the Jacobian matrix is the expression of a differential approximation
		//	(that is, for a very small displacement of the joints).  It's simply out of question to
		//	apply directly the joint displacement given by solving the linear system, but we will do
		//	the scaling *after* solving

		//	here I specify that I want no change in the orientation of the tool
		float[] dTheta = jointDisplacement_(dx, dy, 0);

		//	Step 3:  Apply scale to the joint displacement  
		//	The revised algorithm does the following
		//		1. Compute the largest angular displacement
		//		2. if the largest < tolerance
		//			we have reached the goal
		//		3. else if largest < maxStep (small displacement joint)
		//			apply the displacement joint unscaled
		//		4. else // largest ≥ maxStep
		//			apply dq * (maxStep / largest dq)
		float jointDsplctScale = 1.f;
		float jointDsplctAmpl = max(abs(dTheta[1]), abs(dTheta[2]), abs(dTheta[3]));
		if (jointDsplctAmpl < ANGLE_TOLERANCE) {
			jointDsplctScale = 0;
		}
		else if (jointDsplctAmpl > MAX_ANGLE_STEP)
		{
			jointDsplctScale = MAX_ANGLE_STEP / jointDsplctAmpl;
		}
		theta[1] += jointDsplctScale*dTheta[1];
		theta[2] += jointDsplctScale*dTheta[2];
		theta[3] += jointDsplctScale*dTheta[3];
	}

	//NEW
	//	In this revised version of the method, some keys now control the 
	//	position and orientation of the tool
	public void handleKeyPressed() {

		float[] dTheta = new float[NUM_JOINTS+1];

		switch (key) {
		case 'q':
			if (NUM_JOINTS >= 1)
				theta[1] += ANGLE_INCR;
			break;
		case 'a':
			if (NUM_JOINTS >= 1)
				theta[1] -= ANGLE_INCR;
			break;
		case 'w':
			if (NUM_JOINTS >= 2)
				theta[2] += ANGLE_INCR;
			break;
		case 's':
			if (NUM_JOINTS >= 2)
				theta[2] -= ANGLE_INCR;
			break;
		case 'e':
			if (NUM_JOINTS >= 3)			
				theta[3] += ANGLE_INCR;
			break;
		case 'd':
			if (NUM_JOINTS >= 3)
				theta[3] -= ANGLE_INCR;
			break;
		case 'r':
			if (NUM_JOINTS >= 4)
				theta[4] += ANGLE_INCR;
			break;
		case 'f':
			if (NUM_JOINTS >= 4)
				theta[4] -= ANGLE_INCR;
			break;

		//---------------------------------------------------
		//	Inverse Kinematics
		//---------------------------------------------------
		// x--
		case '[':
			dTheta = jointDisplacement_(-XY_INCR, 0, 0);
			break;
		// x++
		case ']':
			dTheta = jointDisplacement_(+XY_INCR, 0, 0);
			break;
		// y--
		case ';':
			dTheta = jointDisplacement_(0, -XY_INCR, 0);
			break;
		// y++'
		case '\'':
			dTheta = jointDisplacement_(0, +XY_INCR, 0);
			break;
		// ψ--
		case ',':
			dTheta = jointDisplacement_(0, 0, -ANGLE_INCR);
			break;
		// ψ++
		case '.':
			dTheta = jointDisplacement_(0, 0, +ANGLE_INCR);
			break;

	}

	//		//	apply the joint displacement. 
	for (int k=1; k<=NUM_JOINTS; k++) {
		theta[k] += dTheta[k];	
	}
}

	//NEW
	//	I moved the Jacobian matrix's computation code into a separate method, 
	//	as it is becoming more complex.  
	//	If we really wanted to optimize, we could pre-allocate the 2D array and
	//	the Jama Matrix object and reuse them instead of allocating a new array and
	//	matrix each time.
	private double[][] getJacobianMatrix_() {
		
		//	optimize somewhat the computation of the Jacobian matrix
		double	q1 = theta[1], q2 = theta[2], q3 = theta[3],
				q1p2 = q1+q2,  q1p2p3 = q1 + q2 + q3;
		double	cq1 = Math.cos(q1), sq1 = Math.sin(q1),
				cq1p2 = Math.cos(q1p2), sq1p2 = Math.sin(q1p2),
				cq1p2p3 = Math.cos(q1p2p3), sq1p2p3 = Math.sin(q1p2p3);
		//NEW
		//	Looking at the expressions below, you should be able to see the general
		//	pattern emerge, and guess how we are going to compute the J matrix in the
		//	general case of n joints.
		double [][]J = {
						{
							-L[1]*sq1 - L[2]*sq1p2 - L[3]*sq1p2p3,
							-L[2]*sq1p2 - L[3]*sq1p2p3,
							-L[3]*sq1p2p3
						},
						{
							L[1]*cq1 + L[2]*cq1p2 + L[3]*cq1p2p3,
							L[2]*cq1p2 + L[3]*cq1p2p3,
							L[3]*cq1p2p3
						},
						//NEW	
						//	3-dimensional workspace.  The third coordinate is the angle of the tip,
						//	which is the cummulative angle.  It's partial derivative relative to 
						//	each of the separate joint angles is therefore 1
						{1, 1, 1}
						};
		
		return J;
	}

	//NEW
	//	This method performs the inverse kinematics in the case of a joint space and
	//	workspace that are both 3-dimensional
	private float[] jointDisplacement_(float dx, float dy, float dTheta) 
	{
		double [][]dw = {{dx}, {dy}, {dTheta}}; 
		Matrix dwVect = new Matrix(dw);

		//	We are going to optimize this later by doing the calculations directly into
		//	the Matrix object's own array
		Matrix Jmat = new Matrix(getJacobianMatrix_());
		
		Matrix dqVect = Jmat.solve(dwVect);	
				
		float[] jointDisplct = new float[NUM_JOINTS+1];
		for (int i=0; i<NUM_JOINTS; i++)
			jointDisplct[i+1] = (float) (dqVect.get(i,  0));
			
		return jointDisplct;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PApplet.main("Assignment03.Week08NewArmV2");
	}
}
