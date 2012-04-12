package com.neuronrobotics.demo.face;

import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;

import java.util.ArrayList;

import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;

public class FaceLocations {
	private ArrayList<CvSeq> faces = new ArrayList<CvSeq>();
	private int width;
	private int hight;
	private ArrayList<CvRect>  list;
	public FaceLocations(CvSeq faces, int imageWidth,int imageHight){
		setFaces(faces);
		setWidth(imageWidth);
		setHight(imageHight);
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getWidth() {
		return width;
	}
	public void setHight(int hight) {
		this.hight = hight;
	}
	public int getHight() {
		return hight;
	}
	private void setFaces(CvSeq faces) {
		if(faces != null)
			this.faces.add(faces);
	}
	public ArrayList<CvRect> getFaces() {
		if(list == null){
			list = new ArrayList<CvRect>();
			if(faces != null){
				int total=0;// = faces.total();
				for(CvSeq sec:faces){
					total=sec.total();
					for (int i = 0; i < total; i++) {
			        	list.add(new CvRect(cvGetSeqElem(sec, i)));
			        }
				} 
			}
		}
		return list;
	}
	public void add(CvSeq faces2) {
		if(faces2 != null)
			this.faces.add(faces2);
	}
}
