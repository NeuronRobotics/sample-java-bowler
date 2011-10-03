package test;

//import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvClearMemStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvLoad;
import static com.googlecode.javacv.cpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;
import static com.googlecode.javacv.cpp.opencv_objdetect.cvHaarDetectObjects;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_objdetect;
import com.googlecode.javacv.cpp.opencv_objdetect.CvHaarClassifierCascade;
import com.neuronrobotics.video.Camera;
import com.neuronrobotics.video.OSUtil;

public class FaceDectector extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3256112311848783879L;
	private JPanel panel = new JPanel();
	OpenCVFrameGrabber grabber;
    private IplImage grayImage;
    private CvHaarClassifierCascade classifierFront;
    private CvHaarClassifierCascade classifierSide;
    private CvMemStorage storage;
    JLabel lab;
    Camera cam;
    IplImage frame=null;
    private boolean HighSpeedImageing=false;
	public FaceDectector(int index) throws Exception{
		HighSpeedImageing = false;
		grabber = new OpenCVFrameGrabber(index);
        grabber.start();
        init();
	}
	public FaceDectector(String device, int hight, int width) throws Exception{
		HighSpeedImageing = true;
		cam = new Camera(device, hight, width);
        init();
	}
	private void init() throws Exception{
		setName("Face Detector");
		add(panel);
		setVisible(true);
		File classifierFileFront = new File("haarcascade_frontalface_alt.xml");
        if (classifierFileFront == null || classifierFileFront.length() <= 0) {
            throw new IOException("Could not extract the classifier file.");
        }
        File classifierFileSide = new File("haarcascade_profileface.xml");
        if (classifierFileSide == null || classifierFileSide.length() <= 0) {
            throw new IOException("Could not extract the classifier file.");
        }

        // Preload the opencv_objdetect module to work around a known bug.
        Loader.load(opencv_objdetect.class);
        classifierFront = new CvHaarClassifierCascade(cvLoad(classifierFileFront.getAbsolutePath()));
        if (classifierFront.isNull()) {
            throw new IOException("Could not load the classifier file.");
        }
        classifierSide= new CvHaarClassifierCascade(cvLoad(classifierFileSide.getAbsolutePath()));
        if ( classifierSide.isNull()) {
            throw new IOException("Could not load the classifier file.");
        }
        storage = CvMemStorage.create();
        panel.removeAll();
		lab=new JLabel();
		panel.add(lab);
        updateFaces();
        pack();
	}
	
	public void stop() throws Exception{
		if(HighSpeedImageing)
			cam.close();
		else
			grabber.stop();
	}
	public FaceLocations updateFaces(int sunSampWidth,int sunSampHeight) throws Exception{
		CvSeq faces;

		if(HighSpeedImageing){
			BufferedImage im = cam.getImage();
			if (im != null)
				frame = IplImage.createFrom(im);
		}
		else
			frame = grabber.grab();
		
		setSize(frame.width(), frame.height());
		BufferedImage smallGrey = toGrayScale(frame.getBufferedImage(), sunSampWidth,sunSampHeight);
		grayImage = IplImage.createFrom(smallGrey);
		
       
        BufferedImage im = frame.getBufferedImage();
        BufferedImage display = new BufferedImage(frame.width(), frame.height(),BufferedImage.TYPE_INT_RGB);
		Graphics2D g =  display.createGraphics();
		g.drawImage(im, 0, 0, im.getWidth(), im.getHeight(), null);
		im = grayImage.getBufferedImage();
		g.drawImage(im, 0, 0, im.getWidth(), im.getHeight(), null);
		g.setColor(Color.red);
		
		
		faces = cvHaarDetectObjects(grayImage, classifierFront, storage, 1.1, 3, CV_HAAR_DO_CANNY_PRUNING);
        cvClearMemStorage(storage);
		FaceLocations loc =new FaceLocations(faces,im.getWidth(),im.getHeight());
        if (faces != null) {
            for(CvRect r:loc.getFaces()){
            	double x = r.x(), y = r.y(), w = r.width(), h = r.height(); 
                g.drawRect((int)x,(int) y,(int)w,(int) h);
                //g.drawRect((int)(x+(w/2)),(int)( y+(h/2)),2, 2);
                //g.setColor(Color.green);
            }
        }
        
        
		lab.setIcon(new ImageIcon(display ));
		lab.setVisible(true);
		panel.setVisible(true);
		panel.repaint();
		repaint();
		return loc;
	}
	public FaceLocations updateFaces() throws Exception {
		return updateFaces(106,80);
	}
	public static void main(String [] args){
		boolean UseHighSpeedImaging = OSUtil.isLinux();
		try{
			FaceDectector f;
			if(UseHighSpeedImaging)
				f=new FaceDectector("/dev/video0",320,240);
			else
				f=new FaceDectector(0);
			long avgIndex=1;
			double avegTotal = 0;
			while (f.isVisible()){
				long start = System.currentTimeMillis();
				f.updateFaces();
				double sec = (double)(System.currentTimeMillis()-start)/1000.0;
				avegTotal+=(1/sec);
				System.out.println("FPS: "+(int)(1/sec)+" average: "+(avegTotal/avgIndex));
				avgIndex++;
			}
			f.stop();
		}catch(Exception e){
			e.printStackTrace();
		}
		System.exit(0);
	}
	private BufferedImage toGrayScale(BufferedImage in, int w, int h){
		BufferedImage bi = new BufferedImage(w,h, BufferedImage.TYPE_BYTE_GRAY);
		Graphics g = bi.createGraphics();
		g.drawImage(in, 0, 0,w,h, null);
		return bi;
	}
	private BufferedImage toGrayScale(BufferedImage in, double scale){
		int w=(int)(in.getWidth()*scale);
		int h=(int) (in.getHeight()*scale);	
		return toGrayScale(in, w, h);
	}
}
