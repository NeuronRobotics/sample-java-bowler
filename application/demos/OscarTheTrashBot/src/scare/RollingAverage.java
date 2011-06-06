package scare;

public class RollingAverage {
	private int avgSize;
	private double [] avgStore;
	private double average=0;
	private int avgIndex=0;
	public RollingAverage(int size){
		avgSize=size;
		avgStore= new double[avgSize];
		average=500*size;
		for(int i=0;i<size;i++){
			avgStore[i]=500;
		}
	}
	
	public void add(double value){
		average+=value;
		average-=avgStore[avgIndex];
		avgStore[avgIndex++]=value;
		if(avgIndex==avgSize)
			avgIndex=0;
		
	}
	public double get(){
		return average/avgSize;
	}
}
