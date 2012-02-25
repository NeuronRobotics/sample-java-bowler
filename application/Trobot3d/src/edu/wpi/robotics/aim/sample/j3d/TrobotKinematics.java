package edu.wpi.robotics.aim.sample.j3d;

import javax.xml.parsers.FactoryConfigurationError;

import com.neuronrobotics.sdk.addons.kinematics.LinkFactory;
import com.neuronrobotics.sdk.addons.kinematics.xml.XmlFactory;
import com.neuronrobotics.sdk.dyio.DyIO;

import edu.wpi.robotics.aim.core.math.Transform;
import edu.wpi.robotics.aim.core.robot.AbstractKinematics;

public class TrobotKinematics extends AbstractKinematics {
	
	private DHChain chain=null;
	
	public TrobotKinematics() {
		super(XmlFactory.getDefaultConfigurationStream("TrobotLinks.xml"),new LinkFactory());
		chain = new DHChain(getFactory().getUpperLimits(), getFactory().getLowerLimits());
	}
	
	public TrobotKinematics( DyIO dev) {
		super(XmlFactory.getDefaultConfigurationStream("TrobotLinks.xml"),new LinkFactory( dev));
		chain = new DHChain(getFactory().getUpperLimits(), getFactory().getLowerLimits());
	}

	@Override
	public double[] inverseKinematics(Transform taskSpaceTransform)throws Exception {
		return getDhChain().inverseKinematics(taskSpaceTransform, getCurrentJointSpaceVector());
	}

	@Override
	public Transform forwardKinematics(double[] jointSpaceVector) {
		if(jointSpaceVector == null || getDhChain() == null)
			return new Transform();
		return getDhChain().forwardKinematics(jointSpaceVector);
	}

	public void setDhChain(DHChain chain) {
		this.chain = chain;
	}

	public DHChain getDhChain() {
		return chain;
	}

}
