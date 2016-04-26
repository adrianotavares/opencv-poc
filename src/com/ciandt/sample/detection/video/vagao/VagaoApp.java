package com.ciandt.sample.detection.video.vagao;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractorKNN;
import org.opencv.videoio.VideoCapture;

import com.ciandt.sample.detection.utils.ImageProcessor;
import com.ciandt.sample.detection.video.background.backgroundprocessors.AbsDifferenceBackground;
import com.ciandt.sample.detection.video.background.backgroundprocessors.CustomTransformationBackground;
import com.ciandt.sample.detection.video.background.backgroundprocessors.ImageGrayDifferenceBackground;
import com.ciandt.sample.detection.video.background.backgroundprocessors.MixtureOfGaussianBackground;
import com.ciandt.sample.detection.video.background.utils.VideoProcessor;
import com.ciandt.sample.detection.video.visionapi.VisionApi;

public class VagaoApp {

	private static final String onFillString = "On";
	private static final String offFillString = "Off";
	private static final String boundingBoxString = "Bounding box";
	private static final String circleString = "Circle";
	private static final String convexHullString = "Convex Hull";
	private JLabel currentImageView;
	private JLabel foregroundImageView;
	private JLabel binaryImageView;
	private String windowName;
	private Mat resizedImage = new Mat();
	private Mat currentImage = new Mat();
	private Mat foregroundImage = new Mat();
	private Mat backgroundImage = new Mat();
	private Mat binaryImage = new Mat();
	private Mat capturedImage = new Mat();
	
	JFrame frame;

	private final ImageProcessor imageProcessor = new ImageProcessor();
	
	private String fillFlag = onFillString;
	private String enclosingType = boundingBoxString;
	private int limiar = 15;
	private int areaThreshold = 13000;

	public VagaoApp(String windowName) {
		super();
		this.windowName = windowName;
		
	}

	public void init() throws Exception {
		setSystemLookAndFeel();
		initGUI();
		runMainLoop();
	}
	
	
	private void initGUI() {
		frame = createJFrame(windowName);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
		
		frame.setSize(600,480);  
	//	frame.pack();
		//frame.setLocationRelativeTo(null);
		frame.setVisible(true);

	}

	private JFrame createJFrame(String windowName) {
		JFrame frame = new JFrame(windowName);
		frame.setLayout(new GridBagLayout());

		setupFillRadio(frame);
		setupShapeRadio(frame);
		
		setupLowerSlider(frame);
		setupUpperSlider(frame);
		
		setupResetButton(frame);
		setupImage(frame);

		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		return frame;
	}


	private void setupResetButton(JFrame frame) {
		JButton resetButton = new JButton("Draw contours");
		resetButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent event) {
				drawContours();
			}

			
		});
		resetButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 2;
		
		frame.add(resetButton,c);
	}
	
	private void runMainLoop() throws Exception{
		
		VideoProcessor videoProcessor;
		VideoCapture capture = new VideoCapture("tremcut.avi");
		Mat frame2 = new Mat();
		int framecount = 1;
		
		if( capture.isOpened()){
			videoProcessor = new ImageGrayDifferenceBackground();		
			capture.read(currentImage);  
			
			binaryImage.create(new Size(currentImage.cols(), currentImage.rows()), CvType.CV_8UC1);
			binaryImage.setTo(new Scalar(0));
			String text = "";
			
			capture.read(capturedImage);
			while (true){  
				capture.read(currentImage);  
				if( !currentImage.empty() ){
					capture.read(frame2);  
					
					//foregroundImage = videoProcessor.process(currentImage);
					foregroundImage = videoProcessor.process(currentImage, frame2);
					
					//Imgproc.cvtColor(foregroundImage, binaryImage,Imgproc.COLOR_BGR2GRAY);
					//Imgproc.threshold(foregroundImage, binaryImage, imageThreshold, 255.0, Imgproc.THRESH_BINARY_INV);
					
					Mat structuringElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(limiar, limiar));
				    Imgproc.morphologyEx(foregroundImage, binaryImage, Imgproc.MORPH_OPEN, structuringElement);
				    Imgproc.morphologyEx(binaryImage, binaryImage, Imgproc.MORPH_CLOSE, structuringElement);
					
					
					
					if (framecount % 20 ==0){
						capturedImage = currentImage.clone();
					//	Image tempCapturedImage = imageProcessor.toBufferedImage(resizeImage(capturedImage));
					//	binaryImageView.setIcon(new ImageIcon(tempCapturedImage));
						
						VisionApi visionApi = new VisionApi();

						text = visionApi.sendImage(resizeImage(capturedImage));
						System.out.println("Text_Detect: " + text);
						Imgproc.putText(capturedImage, text , new Point(10,680),Core.FONT_HERSHEY_COMPLEX, 3, new Scalar(0,255,255),3);
						if (visionApi.getRectangle() != null)
							Imgproc.rectangle(capturedImage, visionApi.getRectangle().tl(), visionApi.getRectangle().br(), new Scalar(255,0,0),5);
						Image tempCapturedImage = imageProcessor.toBufferedImage(resizeImage(capturedImage));
						binaryImageView.setIcon(new ImageIcon(tempCapturedImage));
					}
					
					drawContours();
					
					// update
					updateView();
					
					 
					
					frame.pack();
					framecount ++;
					
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
					}
				}  
			}  
		}
		else{
			System.out.println("Couldn't open video file.");
		}
		
	}
	
	


	protected void drawContours() {
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		
		final Mat contourMat = new Mat(binaryImage.rows(), binaryImage.cols(), binaryImage.type());
		binaryImage.copyTo(contourMat);
		
		
		//Mat contourMat = binaryImage.clone();
		
		
		int thickness = (fillFlag.equals(onFillString))?-1:2;
		
		Imgproc.findContours(contourMat, contours, new Mat(), Imgproc.CHAIN_APPROX_NONE,Imgproc.CHAIN_APPROX_SIMPLE);
		System.out.println("Number of found contours: "+contours.size());
		for(int i=0;i<contours.size();i++){
			MatOfPoint currentContour = contours.get(i);
			double currentArea = Imgproc.contourArea(currentContour);
			
			if(currentArea > areaThreshold){
				System.out.println("Contour points: " +contours.get(i).size().height);
				//Imgproc.drawContours(currentImage, contours, i, new Scalar(0,255,0), thickness);
				System.out.println("Area: "+currentArea);
				
				if(boundingBoxString.equals(enclosingType)){
					drawBoundingBox(currentContour);
				}
				else if (circleString.equals(enclosingType)){
					drawEnclosingCircle(currentContour);					
				}
				else if (convexHullString.equals(enclosingType)){
					drawConvexHull(currentContour);					
				}
				
			}
			else{
			//	Imgproc.drawContours(currentImage, contours, i, new Scalar(0,0,255), thickness);	
			}
			
		}
	}
	
	private void updateView(){
		Image tempCurrent = imageProcessor.toBufferedImage(resizeImage(currentImage));
		Image tempForeground = imageProcessor.toBufferedImage(resizeImage(foregroundImage));
//		Image tempCapturedImage = imageProcessor.toBufferedImage(resizeImage(capturedImage));
		currentImageView.setIcon(new ImageIcon(tempCurrent));
		foregroundImageView.setIcon(new ImageIcon(tempForeground));
///		binaryImageView.setIcon(new ImageIcon(tempCapturedImage));
		
		frame.pack();
	}
	
	private Mat resizeImage(Mat src){
		resizedImage = new Mat();
		Size sz = new Size(600,480);
		Imgproc.resize( src, resizedImage, sz );
		
		return resizedImage; 
	}

	private void drawBoundingBox(MatOfPoint currentContour) {
		
		Rect rectangle = Imgproc.boundingRect(currentContour);
		Imgproc.rectangle(currentImage, rectangle.tl(), rectangle.br(), new Scalar(255,0,0),5);
		

	}

	private void drawEnclosingCircle(MatOfPoint currentContour) {
		float[] radius = new float[1];
		Point center = new Point();
		
		MatOfPoint2f currentContour2f = new MatOfPoint2f();
		currentContour.convertTo(currentContour2f, CvType.CV_32FC2);
		Imgproc.minEnclosingCircle(currentContour2f, center, radius);
		
		Imgproc.circle(currentImage, center, (int) radius[0], new Scalar(255,0,0));
		
	}

	private void drawConvexHull(MatOfPoint currentContour) {
		MatOfInt hull = new MatOfInt();
		Imgproc.convexHull(currentContour, hull);	
		
		List<MatOfPoint> hullContours = new ArrayList<MatOfPoint>();
		MatOfPoint hullMat = new MatOfPoint();
		hullMat.create((int)hull.size().height,1,CvType.CV_32SC2);
		
		for(int j = 0; j < hull.size().height ; j++)
		{
		    int index = (int)hull.get(j, 0)[0];
		    double[] point = new double[] {
		        currentContour.get(index, 0)[0], currentContour.get(index, 0)[1]
		    };
		    hullMat.put(j, 0, point);
		} 
		hullContours.add(hullMat);
		Imgproc.drawContours(currentImage, hullContours, 0, new Scalar(128,0,0), 2);
	}

	private void setupLowerSlider(JFrame frame) {
		
		JLabel sliderLabel = new JLabel("Image threshold:", JLabel.RIGHT);

		int minimum = 0;
		int maximum = 255;
		int initial =limiar;

		JSlider thresholdSlider = new JSlider(JSlider.HORIZONTAL,
				minimum, maximum, initial);

		thresholdSlider.setMajorTickSpacing(25);
		thresholdSlider.setMinorTickSpacing(5);
		thresholdSlider.setPaintTicks(true);
		thresholdSlider.setPaintLabels(true);
		thresholdSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
				limiar = (int)source.getValue();
				processOperation();	
			}
		});

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 2;
		frame.add(sliderLabel,c);
		c.gridx = 1;
		c.gridy = 2;
		frame.add(thresholdSlider,c);
	}
	
private void setupUpperSlider(JFrame frame) {
		
		JLabel sliderLabel = new JLabel("Area threshold:", JLabel.RIGHT);

		int minimum = 0;
		int maximum = 10000;
		int initial =500;

		JSlider areaSlider = new JSlider(JSlider.HORIZONTAL,
				minimum, maximum, initial);
		areaSlider.setMajorTickSpacing(1000);
		areaSlider.setMinorTickSpacing(250);
		areaSlider.setPaintTicks(true);
		areaSlider.setPaintLabels(true);
		areaSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
				areaThreshold = (int)source.getValue();
				processOperation();	
			}
		});

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;

		c.gridx = 0;
		c.gridy = 3;
		frame.add(sliderLabel,c);

		c.gridx = 1;
		c.gridy = 3;
		frame.add(areaSlider,c);
	}



	private void setupShapeRadio(JFrame frame) {
		JRadioButton boundingBoxButton = new JRadioButton(boundingBoxString);
		boundingBoxButton.setMnemonic(KeyEvent.VK_B);
		boundingBoxButton.setActionCommand(boundingBoxString);
		boundingBoxButton.setSelected(true);

		JRadioButton circleButton = new JRadioButton(circleString);
		circleButton.setMnemonic(KeyEvent.VK_C);
		circleButton.setActionCommand(circleString);
		circleButton.setSelected(false);
		
		
		JRadioButton convexHullButton = new JRadioButton(convexHullString);
		convexHullButton.setMnemonic(KeyEvent.VK_H);
		convexHullButton.setActionCommand(convexHullString);
		convexHullButton.setSelected(false);


		ButtonGroup group = new ButtonGroup();
		group.add(boundingBoxButton);
		group.add(circleButton);
		group.add(convexHullButton);

		ActionListener operationChangeListener = new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				enclosingType  = event.getActionCommand();
				processOperation();	
			}
		};

		boundingBoxButton.addActionListener(operationChangeListener);
		circleButton.addActionListener(operationChangeListener);
		convexHullButton.addActionListener(operationChangeListener);

		
		GridLayout gridRowLayout = new GridLayout(1,0);
		JPanel radioOperationPanel = new JPanel(gridRowLayout);

		JLabel rangeLabel = new JLabel("Enclosing shape:", JLabel.RIGHT);

		radioOperationPanel.add(boundingBoxButton);
		radioOperationPanel.add(circleButton);
		radioOperationPanel.add(convexHullButton);


		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		
		c.gridx = 0;
		c.gridy = 1;

		frame.add(rangeLabel,c);

		c.gridx = 1;
		c.gridy = 1;
		frame.add(radioOperationPanel,c);
		
	}

	private void setupFillRadio(JFrame frame){
		
		JRadioButton onButton = new JRadioButton(onFillString);
		onButton.setMnemonic(KeyEvent.VK_O);
		onButton.setActionCommand(onFillString);
		onButton.setSelected(true);

		JRadioButton offButton = new JRadioButton(offFillString);
		offButton.setMnemonic(KeyEvent.VK_F);
		offButton.setActionCommand(offFillString);
		offButton.setSelected(false);


		ButtonGroup group = new ButtonGroup();
		group.add(onButton);
		group.add(offButton);

		ActionListener operationChangeListener = new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				fillFlag = event.getActionCommand();
				processOperation();	
			}
		};

		onButton.addActionListener(operationChangeListener);
		offButton.addActionListener(operationChangeListener);

		GridLayout gridRowLayout = new GridLayout(1,0);
		JPanel radioOperationPanel = new JPanel(gridRowLayout);

		JLabel fillLabel = new JLabel("Fill contour:", JLabel.RIGHT);

		radioOperationPanel.add(onButton);
		radioOperationPanel.add(offButton);


		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		
		c.gridx = 0;
		c.gridy = 0;


		frame.add(fillLabel,c);

		c.gridx = 1;
		c.gridy = 0;

		frame.add(radioOperationPanel,c);
		
	}

	
	
	
	
	
	

	private void setupImage(JFrame frame) {
		currentImageView = new JLabel();
		
		foregroundImageView = new JLabel();
		binaryImageView = new JLabel();

		

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;

		c.gridx = 0;
		c.gridy = 5;
		c.gridwidth = 1;

		frame.add(new JLabel("Image", JLabel.CENTER),c);
		
		c.gridx = 1;
		
		frame.add(new JLabel("Background", JLabel.CENTER),c);
		
		c.gridx = 2;
		frame.add(new JLabel("Binary", JLabel.CENTER),c);
		
		c.gridy = 6;
		c.gridx = 0;
		c.gridwidth = 1;
		
		frame.add(currentImageView,c);
		
		c.gridx = 1;
		
		frame.add(foregroundImageView,c);
		
		c.gridx = 2;
		
		frame.add(binaryImageView,c);
	}

	private void setSystemLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}

	/*private void updateView() {
		
		Image outputImage = imageProcessor.toBufferedImage(image);
		Image binaryImage = imageProcessor.toBufferedImage(binary);
		currentImageView.setIcon(new ImageIcon(outputImage));
		foregroundImageView.setIcon(new ImageIcon(binaryImage));
	}*/

	private void processOperation() {
	//	resetImage();
		
	}
	
	static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

	public static void main(String[] args) throws Exception {
			VagaoApp gui = new VagaoApp("Vagão App");
			gui.init();


	}

}
