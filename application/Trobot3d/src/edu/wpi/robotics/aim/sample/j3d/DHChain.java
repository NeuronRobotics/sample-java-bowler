package edu.wpi.robotics.aim.sample.j3d;

import java.util.ArrayList;

import javax.swing.JFrame;

import com.neuronrobotics.sdk.util.ThreadUtil;

import Jama.Matrix;

import edu.wpi.robotics.aim.core.math.Rotation;
import edu.wpi.robotics.aim.core.math.Transform;

public  class DHChain {
	private ArrayList<DHLink> links = new ArrayList<DHLink>();
	private ArrayList<Transform> chain = new ArrayList<Transform>();
	private final double[] upperLimits;
	private final double[] lowerLimits;
	private boolean debug=false;
	private DHViewer viewer=null;
	JFrame frame; 
	public DHChain(double [] upperLimits,double [] lowerLimits, boolean debugViewer ) {
		this(upperLimits, lowerLimits);
		if(debugViewer){
			this.debug=true;
			viewer=new DHViewer(this, new double[]{0,0,0,0,0,0});
			frame = new JFrame();
			frame.getContentPane().add(viewer);
			frame.setSize(1024, 768);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		}
	}
	
	public DHChain(double [] upperLimits,double [] lowerLimits ) {
		
		this.upperLimits = upperLimits;
		this.lowerLimits = lowerLimits;
		links.add(new DHLink(	13, 	Math.toRadians(180), 	32, 	Math.toRadians(-90)));//0->1
		links.add(new DHLink(	25, 	Math.toRadians(-90), 	93, 	Math.toRadians(180)));//1->2
		links.add(new DHLink(	11, 	Math.toRadians(90), 	24, 	Math.toRadians(90)));//2->3 
		links.add(new DHLink(	128, 	Math.toRadians(-90), 	0, 		Math.toRadians(90)));//3->4
		
		links.add(new DHLink(	0, 		Math.toRadians(0), 		0, 		Math.toRadians(-90)));//4->5
		links.add(new DHLink(	45, 	Math.toRadians(90), 		0, 		Math.toRadians(0)));//5->tool
		
		forwardKinematics(new  double [] {0,0,0,0,0,0});
	}

	public double[] inverseKinematics(Transform target,double[] jointSpaceVector )throws Exception {
		if(links == null)
			return null;
		double [] inv = new double[links.size()];	
		
		Estimation [] increments = new Estimation[links.size()];	
		for(int i=0;i<links.size();i++){
			increments[i] = new Estimation(i,jointSpaceVector, target, upperLimits[i],lowerLimits[i] );
		}
		int iter=0;
		double vect=0;
		double orent = 0;
		do{
			int len  = increments.length-1;
			for(int i=len;i>=0;i--){
				increments[i].step();
			}
			if(debug){
				viewer.updatePoseDisplay(getChain(jointSpaceVector));
				ThreadUtil.wait(20);
			}
			vect = forwardKinematics(jointSpaceVector).getOffsetVectorMagnitude(target);
			orent = forwardKinematics(jointSpaceVector).getOffsetOrentationMagnitude(target);
		}while(++iter<1500 && (vect >2 || orent > .001));//preincrement and check
		if(debug)
			System.out.println("Finished iteration, numer of iterations #"+iter+" final offset= "+vect+" final orent= "+orent);
		
		for(int i=0;i<inv.length;i++){
			inv[i]=jointSpaceVector[i];
		}
		
//		//Dump from cartesian to joint space, used as an example
//		inv[0]= cartesianSpaceVector.getX();
//		inv[1]= cartesianSpaceVector.getY();
//		inv[2]= cartesianSpaceVector.getZ();
//		
//		Matrix rotationMatrixArray = new Matrix(cartesianSpaceVector.getRotation().getRotationMatrix()); 
//		
//		//X rotation
//		inv[3]=Math.atan2(-rotationMatrixArray.get(1, 2), rotationMatrixArray.get(2, 2))*180/Math.PI;
//		//Y rotation
//		inv[4]=Math.asin(rotationMatrixArray.get(0, 2))*180/Math.PI;		
//		//Z rotation
//		inv[5]=Math.atan2(-rotationMatrixArray.get(0, 1), rotationMatrixArray.get(0, 0))*180/Math.PI;
		return inv;
	}

	public Transform forwardKinematics(double[] jointSpaceVector) {
		if(links == null)
			return new Transform();
		if (jointSpaceVector.length!=links.size())
			throw new IndexOutOfBoundsException("DH links do not match defined links");
		Transform current = new Transform();
		setChain(new ArrayList<Transform>());
		for(int i=0;i<links.size();i++) {
			Transform step = links.get(i).DhStepRotory(Math.toRadians(jointSpaceVector[i]));
			//System.out.println("Current:\n"+current+"Step:\n"+step);
			current = current.times(step);
			chain.add(current);
		}
		//System.out.println("Final:\n"+current);
		return current;
	}
	

	public void setChain(ArrayList<Transform> chain) {
		this.chain = chain;
	}

	public ArrayList<Transform> getChain(double[] jointSpaceVector) {
		forwardKinematics(jointSpaceVector);
		return chain;
	}
	
	
	
	private class Estimation{
		Transform target;
		int index;
		double offset;
		double increment=.2;
		double myStart=0;
		double[] jointSpaceVector;
		double upper;
		double lower;
		public Estimation(int index,double[] jointSpaceVector,Transform cartesianSpace, double u, double l){
			this.offset=0;
			this.index=index;
			this.jointSpaceVector=jointSpaceVector;
			target = cartesianSpace;
			myStart =  jointSpaceVector[index];
			upper = u;
			lower = l;
		}
		public void step(){
			stepIncrementWithOrent(true);
		}
		public boolean stepIncrementWithOrent(boolean withOrent) {
			double none =  myStart+offset;
			
			jointSpaceVector[index]= bound (none);
			Transform tmp =forwardKinematics(jointSpaceVector);
			tmp =forwardKinematics(jointSpaceVector);
			double nonevect = tmp.getOffsetVectorMagnitude(target);
			double noneOrent = tmp.getOffsetOrentationMagnitude(target);
			
			increment = nonevect/1000+noneOrent;
			
			double up = myStart+offset+increment;
			double down =myStart+offset-increment;
			
			jointSpaceVector[index]= bound (up);
			tmp =forwardKinematics(jointSpaceVector);
			double upvect = tmp.getOffsetVectorMagnitude(target);
			double upOrent = tmp.getOffsetOrentationMagnitude(target);
			
			jointSpaceVector[index]= bound (down);
			tmp =forwardKinematics(jointSpaceVector);
			double downvect = tmp.getOffsetVectorMagnitude(target);
			double downOrent = tmp.getOffsetOrentationMagnitude(target);
			
			
			if((upvect>nonevect && downvect>nonevect)  || (upOrent>noneOrent && downOrent>noneOrent && withOrent)){
				jointSpaceVector[index]=none;
			}
			if((nonevect>upvect && downvect>upvect )  || ( noneOrent>upOrent && downOrent>upOrent && withOrent)){
				jointSpaceVector[index]=up;
				offset+=increment;
				return false;
			}
			if((upvect>downvect && nonevect>downvect)  || (upOrent>downOrent && noneOrent>downOrent && withOrent )){
				jointSpaceVector[index]=down;
				offset-=increment;
				return false;
			}
			return true;
			
		}
		double bound(double in){
			if(in>upper){
				return upper;
			}
			if(in<lower){
				return lower;
			}
			return in;
		}
	}
	
	public static void main(String [] args){

		DHChain tk = new DHChain(new double[]{90,90,90,90,90,90}, new double[]{-90,-90,-90,-90,-90,-90}, true);

		ThreadUtil.wait(2000);
//		Transform target = new Transform(new Matrix(new double [][] {
//				{ -000.46,	0000.63,	-000.63,	-252.29	 },
//				{ 0000.21,	0000.76,	0000.61,	0051.07	 },
//				{ 0000.86,	0000.15,	-000.48,	0083.43	 },
//				{ 0000.00,	0000.00,	0000.00,	0001.00	 }
//				}));
		// the expected Joint space vector is { -10,45,-45,45,45,45}
		//double [] targetVect = new double [] { -85,10,-90,0,90,90};
		double [] targetVect = new double [] { -10,45,-45,45,45,45};
		Transform target = tk.forwardKinematics(targetVect);
		Transform home = tk.forwardKinematics(new  double [] {0,0,0,0,0,0});
		try {
			double [] back = tk.inverseKinematics(target, new  double [] {0,0,0,0,0,0});
			System.out.print("\nJoint angles targeted: {");
			for(int i=0;i<6;i++){
				System.out.print(" "+targetVect[i]);
			}
			System.out.print("} \n");
			System.out.print("\nJoint angles difference: {");
			for(int i=0;i<6;i++){
				System.out.print(" "+(back[i]-targetVect[i]));
			}
			System.out.print("} \n");
			System.out.println("Attempted\n"+target+"\nArrived at \n"+tk.forwardKinematics(back));
			
			back = tk.inverseKinematics( home,back);
			System.out.print("\nJoint angles targeted: {");
			for(int i=0;i<6;i++){
				System.out.print(" "+targetVect[i]);
			}
			System.out.print("} \n");
			System.out.print("\nJoint angles difference: {");
			for(int i=0;i<6;i++){
				System.out.print(" "+(back[i]-targetVect[i]));
			}
			System.out.print("} \n");
			System.out.println("Attempted\n"+target+"\nArrived at \n"+tk.forwardKinematics(back));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
