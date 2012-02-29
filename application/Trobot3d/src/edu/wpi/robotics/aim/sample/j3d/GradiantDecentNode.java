package edu.wpi.robotics.aim.sample.j3d;

import edu.wpi.robotics.aim.core.math.Transform;

public class GradiantDecentNode{
	Transform target;
	int index;
	double offset;

	double myStart=0;
	double[] jointSpaceVector;
	double upper;
	double lower;
	private final DHChain chain;
	double incVect;
	double incOrent;
	
	//integral
	int integralSize = 100;
	int integralIndex = 0;
	double integralTotalVect = 0;
	double integralTotalOrent = 0;
	double intVect[] = new double[integralSize]; 
	double intOrent[] = new double[integralSize]; 
	
	double Kp = 1;
	double Ki = 1;
	
	public GradiantDecentNode(DHChain chain,int index,double[] jointSpaceVector,Transform cartesianSpace, double u, double l){
		this.chain = chain;
		this.offset=0;
		this.index=index;
		this.jointSpaceVector=jointSpaceVector;
		target = cartesianSpace;
		myStart =  jointSpaceVector[index];
		upper = u;
		lower = l;
		for(int i=0;i<integralSize;i++){
			intVect[i]=0;
			intOrent[i]=0;
		}
	}
	public boolean step() {
		double none =  myStart+offset;
		double start = offset;
		jointSpaceVector[index]= bound (none);
		Transform tmp =chain.forwardKinematics(jointSpaceVector);
		tmp =chain.forwardKinematics(jointSpaceVector);
		double nonevect = tmp.getOffsetVectorMagnitude(target);
		double noneOrent = tmp.getOffsetOrentationMagnitude(target);
		
		double incVectP = (nonevect/1000);// Divide by magic number
		double incOrentP = (noneOrent*10);//Multiply by magic number
		//Remove old values off rolling buffer
		integralTotalVect-=intVect[integralIndex];
		integralTotalOrent-=intOrent[integralIndex];
		//Store current values
		intVect[integralIndex] =incVectP; 
		intOrent[integralIndex] =incOrentP;
		//Add current values to totals
		integralTotalVect+=intVect[integralIndex];
		integralTotalOrent+=intOrent[integralIndex];
		//Reset the index for next iteration
		integralIndex++;
		if(integralIndex==integralSize){
			integralIndex=0;
		}
		
		//The 2 increment numbers
		incVect = incVectP*Kp + (integralTotalVect/integralSize)*Ki;
		incOrent = incOrentP*Kp + (integralTotalOrent/integralSize)*Ki;
		
		double up = myStart+offset+incVect;
		double down =myStart+offset-incVect;
		
		double upO = myStart+offset+incOrent;
		double downO =myStart+offset-incOrent;
		
		jointSpaceVector[index]= bound (up);
		tmp =chain.forwardKinematics(jointSpaceVector);
		double upvect = tmp.getOffsetVectorMagnitude(target);
		jointSpaceVector[index]= bound (upO);
		tmp =chain.forwardKinematics(jointSpaceVector);
		double upOrent = tmp.getOffsetOrentationMagnitude(target);
		
		jointSpaceVector[index]= bound (down);
		tmp =chain.forwardKinematics(jointSpaceVector);
		double downvect = tmp.getOffsetVectorMagnitude(target);
		jointSpaceVector[index]= bound (downO);
		tmp =chain.forwardKinematics(jointSpaceVector);
		double downOrent = tmp.getOffsetOrentationMagnitude(target);
		

		if((upvect>nonevect && downvect>nonevect)  && (upOrent>noneOrent && downOrent>noneOrent)){
			jointSpaceVector[index]=none;
		}
		if((nonevect>upvect && downvect>upvect ) ){
			jointSpaceVector[index]=up;
			offset+=incVect;
		}
		if((upvect>downvect && nonevect>downvect)  ){
			jointSpaceVector[index]=down;
			offset-=incVect;
		}
		if(( noneOrent>upOrent && downOrent>upOrent)){
			jointSpaceVector[index]=up;
			offset+=incOrent;
		}
		if((upOrent>downOrent && noneOrent>downOrent )){
			jointSpaceVector[index]=down;
			offset-=incOrent;
		}
		
		jointSpaceVector[index] = myStart+offset;
		if(start == offset)
			return true;
		return false;
	}
	public void jitter(){
		double jitterAmmount = 10;
		double jitter=(Math.random()*jitterAmmount)-(jitterAmmount /2) ;
		System.out.println("Jittering Link #"+index+" jitter:"+jitter+" current offset:"+offset);
		offset += jitter;
		jointSpaceVector[index] = myStart+offset;
	}
	double bound(double in){
		if(in>upper){
			offset = 0;// Attempt to reset a link on error case
			return upper;
		}
		if(in<lower){
			offset = 0;// Attempt to reset a link on error case
			return lower;
		}
		return in;
	}
}
