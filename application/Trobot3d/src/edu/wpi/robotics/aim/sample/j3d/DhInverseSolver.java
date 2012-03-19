package edu.wpi.robotics.aim.sample.j3d;

import com.neuronrobotics.sdk.addons.kinematics.math.Transform;

public interface DhInverseSolver {
	double[] inverseKinematics(Transform target,double[] jointSpaceVector );
}
