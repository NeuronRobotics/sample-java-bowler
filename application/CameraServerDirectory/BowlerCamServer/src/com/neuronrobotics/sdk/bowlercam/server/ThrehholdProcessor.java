package com.neuronrobotics.sdk.bowlercam.server;

import java.awt.Color;

public class ThrehholdProcessor {
	boolean enabled = false;
	private Color target;
	private int treshhold;
	private boolean within;
	public ThrehholdProcessor(){
		enabled = true;
		this.setTarget(new Color(33,240, 246));
		this.setTreshhold(104);
		this.setWithin(true);
	}
	public ThrehholdProcessor(Color target, int threshold,boolean within){
		 enabled = true;
		 this.setTarget(target);
		 this.setTreshhold(threshold);
		 this.setWithin(within);
	}
	public boolean isEnabled(){
		return enabled;
	}
	public void setTarget(Color target) {
		this.target = target;
	}
	public Color getTarget() {
		return target;
	}
	public void setWithin(boolean within) {
		this.within = within;
	}
	public boolean isWithin() {
		return within;
	}
	public void setTreshhold(int treshhold) {
		this.treshhold = treshhold;
	}
	public int getTreshhold() {
		return treshhold;
	}
}
