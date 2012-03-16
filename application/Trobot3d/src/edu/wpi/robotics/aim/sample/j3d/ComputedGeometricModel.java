package edu.wpi.robotics.aim.sample.j3d;

import edu.wpi.robotics.aim.core.math.Transform;

public class ComputedGeometricModel  implements DhInverseSolver{
	private final DHChain dhChain;
	private final boolean debug;
	public ComputedGeometricModel(DHChain dhChain, boolean debug) {
		this.dhChain = dhChain;
		this.debug = debug;
	}
	
	public double[] inverseKinematics(Transform target,double[] jointSpaceVector ) {
		int linkNum = jointSpaceVector.length;
		double [] inv = new double[linkNum];
		
		
		
		
		return inv;
	}
}
