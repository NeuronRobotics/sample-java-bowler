package edu.wpi.robotics.aim.sample.j3d;

import edu.wpi.robotics.aim.core.math.Transform;

public class GradiantDecent {

	private final DHChain dhChain;
	private final boolean debug;

	public GradiantDecent(DHChain dhChain, boolean debug) {
		this.dhChain = dhChain;
		// TODO Auto-generated constructor stub
		this.debug = debug;
	}
	
	public double[] inverseKinematics(Transform target,double[] jointSpaceVector,double [] upperLimits,double [] lowerLimits ){
		int linkNum = jointSpaceVector.length;
		double [] inv = new double[linkNum];	
		
		GradiantDecentNode [] increments = new GradiantDecentNode[linkNum];	
		for(int i=0;i<linkNum;i++){
			increments[i] = new GradiantDecentNode(dhChain,i,jointSpaceVector, target, upperLimits[i],lowerLimits[i] );
		}
		int iter=0;
		double vect=0;
		double orent = 0;
		boolean stopped;
		boolean notArrived = false;
		boolean [] stop = new boolean [increments.length];
		do{
			stopped = true;
			for(int i=increments.length-1;i>=0;i--){
			//for(int i=0;i<increments.length;i++){
				stop[i]=increments[i].step();
				if(!stop[i]){
					stopped = false;
				}
			}
			vect = dhChain.forwardKinematics(jointSpaceVector).getOffsetVectorMagnitude(target);
			orent = dhChain.forwardKinematics(jointSpaceVector).getOffsetOrentationMagnitude(target);
			notArrived = (vect > 10.0|| orent > .001);
			if(stopped == true && notArrived == true){
				stopped = false;
				for(int i=0;i<increments.length;i++){
					increments[i].jitter();
				}
				//ThreadUtil.wait(100);
			}
			if(debug){
//				System.out.print("\nJoint angles current: {");
//				for(int i=0;i<jointSpaceVector.length;i++){
//					System.out.print(" "+jointSpaceVector[i]+ " St="+stop[i]);
//				}
//				System.out.print("} \n");
				dhChain.getViewer().updatePoseDisplay(dhChain.getChain(jointSpaceVector));
				//ThreadUtil.wait(10);
			}
		}while(++iter<1000 && notArrived && stopped == false);//preincrement and check
		if(debug){
			System.out.println("Numer of iterations #"+iter+" \n\tStopped = "+stopped+" \n\tArrived = "+!notArrived+" \n\tFinal offset= "+vect+" \n\tFinal orent= "+orent);
		}
		
		for(int i=0;i<inv.length;i++){
			inv[i]=jointSpaceVector[i];
		}
		return inv;
	}

}
