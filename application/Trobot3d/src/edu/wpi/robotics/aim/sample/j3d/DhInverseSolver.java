package edu.wpi.robotics.aim.sample.j3d;

import edu.wpi.robotics.aim.core.math.Transform;

public interface DhInverseSolver {
	double[] inverseKinematics(Transform target,double[] jointSpaceVector );
}
