package test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;



import net.miginfocom.swing.MigLayout;


//import com.neuronrobotics.sdk.BowlerImaging.ImageProcessingFactory;

import com.neuronrobotics.sdk.bowlercam.device.BowlerCamDevice;
import com.neuronrobotics.sdk.bowlercam.device.IWebcamImageListener;
import com.neuronrobotics.sdk.bowlercam.device.ItemMarker;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.network.BowlerUDPClient;
import com.neuronrobotics.sdk.util.ThreadUtil;

@SuppressWarnings("unused")
public class BowlerCamDeviceTester implements IWebcamImageListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7385372910345405369L;
	private BowlerCamDevice cam = new BowlerCamDevice();

	private JPanel directPanel = new JPanel(new MigLayout());
	private JPanel bcPanel = new JPanel(new MigLayout());
	private JPanel images = new JPanel(new MigLayout());
	private JPanel controls = new JPanel(new MigLayout());
	private JPanel sliders = new JPanel(new MigLayout());
	private JLabel fps	= new JLabel("FPS: ");
	private JLabel thr	= new JLabel("");
	private JFrame frame = new JFrame();
	private RGBSlider target = new RGBSlider("Target Color");
	//private RGBSlider vector = new RGBSlider("Vector Color");
	private JSlider threshhold = new JSlider(SwingConstants.HORIZONTAL, 0, 255, 104);
	private JFormattedTextField min = new JFormattedTextField(NumberFormat.getNumberInstance());
	private JFormattedTextField max = new JFormattedTextField(NumberFormat.getNumberInstance());
	private JFormattedTextField scale = new JFormattedTextField(NumberFormat.getNumberInstance());
	private JCheckBox within = new JCheckBox("Within Threshhold");
	private JButton update = new JButton("Update Processor");
	private boolean packed=false;
	private BufferedImage unaltered=null;
	private BufferedImage processedIm=null;
	private long time;
	double scaleSet = .5;
	public BowlerCamDeviceTester() throws IOException, InterruptedException{
		initGui();
//		if (!ConnectionDialog.getBowlerDevice(cam)){
//			System.exit(1);
//		}
		cam.setConnection(new BowlerUDPClient());
		cam.connect();
		cam.addWebcamImageListener(this);
		cam.startHighSpeedAutoCapture(0,scaleSet,0);
		cam.startHighSpeedAutoCapture(1,scaleSet,0);
		//cam.updateImage(0,imageScale);
		time = System.currentTimeMillis();
		while(true) {
			ThreadUtil.wait(100);
			displayImage();
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Log.enableDebugPrint(true);
			Log.enableSystemPrint(true);
			new BowlerCamDeviceTester();
		}catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	private void initGui() throws IOException, InterruptedException{
		frame.setLayout(new MigLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBackground(Color.black);
		//frame.setLocationRelativeTo(null);
		frame.setSize(new Dimension(640,480));
		frame.setVisible(true);
		
		images.add(directPanel);
		images.add(bcPanel);
		bcPanel.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent arg0) {}
			@Override
			public void mousePressed(MouseEvent arg0) {}
			@Override
			public void mouseExited(MouseEvent arg0) {}
			@Override
			public void mouseEntered(MouseEvent arg0) {}
			@Override
			public void mouseClicked(MouseEvent arg0) {
				Color cl = new Color(unaltered.getRGB(arg0.getX(), arg0.getY()));
				getTargetColor().setColor(cl);
			}
		});
		
		sliders.add(fps,"wrap");
		target.setColor(33,240, 246);
		sliders.add(target);
		controls.add(new JLabel("Threshhold"),"wrap");
		controls.add(threshhold);
		controls.add(thr,"wrap");
		thr.setText(new Integer(threshhold.getValue()).toString());
		controls.add(within,"wrap");
		within.setSelected(true);
		
		controls.add(new JLabel("Image Scale"));
		controls.add(scale,"wrap");
		scale.setText(new Double(scaleSet).toString());
		
		min.setText("5");
		max.setText("100000");
		controls.add(new JLabel("Minimum pixles per blob"));
		controls.add(min,"wrap");
		controls.add(new JLabel("Maximum pixles per blob"));
		controls.add(max,"wrap");
		update.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int minimum= Integer.parseInt(min.getText());
				int maximum= Integer.parseInt(max.getText());
				cam.updateFilter(target.getColor(), threshhold.getValue(), within.isSelected(),minimum, maximum);
			}
		});
		controls.add(update);
		
		
		JPanel tmp = new JPanel(new MigLayout());
		JPanel tmp2 = new JPanel(new MigLayout());
		tmp2.add(sliders);
		tmp2.add(controls);
		tmp.add(tmp2,"wrap");
		tmp.add(images,"wrap");
		frame.add(tmp);
	}
	
//	private void process() {
//		if(unaltered == null)
//			return;
//		try{
//			int minimum= Integer.parseInt(min.getText());
//			int maximum= Integer.parseInt(max.getText());
//			processedIm =ImageProcessingFactory.threshhold(	unaltered,
//													target.getColor(),
//													threshhold.getValue(), 
//													within.isSelected());
//			ArrayList<ItemMarker> blobs = ImageProcessingFactory.getBlobs(processedIm,minimum,maximum,255);
//			processedIm=ImageProcessingFactory.drawMarks(processedIm, blobs, Color.green);
//		}catch(Exception e){
//			e.printStackTrace();
//			System.err.println("#########Image Update failed");
//		}
//	}
	private void displayImage() {
		thr.setText(new Integer(threshhold.getValue()).toString());
		target.getColor();
		if(scaleSet != Double.parseDouble(scale.getText())){
			System.out.println("Resetting scale : "+scale.getText());
			scaleSet = Double.parseDouble(scale.getText());
			cam.startHighSpeedAutoCapture(0,scaleSet,0);
		}
		updateImage(unaltered	,bcPanel);
		updateImage(processedIm,directPanel);
		if(!packed)
			frame.pack();
		if(unaltered != null && processedIm != null)
			packed = true;
	}
	
	protected RGBSlider getTargetColor() {
		return target;
	}
	private void updateImage(BufferedImage imageUpdate, JPanel p){
		if(imageUpdate ==null)
			return;
		p.removeAll();
		JLabel l = new JLabel();
		l.setIcon(new ImageIcon(imageUpdate));
		p.add(l);
		p.invalidate();
	}
	@Override
	public void onNewImage(int camera,BufferedImage image) {
		//System.out.println("Got image: "+camera);
		if(camera == 0){
			double s=((double)(System.currentTimeMillis()-time))/1000.0;
			fps.setText("FPS: "+(int)(1/(s)));
			time = System.currentTimeMillis();
			unaltered=image;
			//process();
		}
		if(camera == 1){
			processedIm=image;
		}
		
	}

}
