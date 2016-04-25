package com.ciandt.sample.detection.video.background.backgroundprocessors;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractorKNN;
import org.opencv.video.BackgroundSubtractorMOG2;

import com.ciandt.sample.detection.video.background.utils.VideoProcessor;

public class CustomTransformationBackground implements VideoProcessor {

	private BackgroundSubtractorMOG2 mog =  org.opencv.video.Video.createBackgroundSubtractorMOG2();
	private Mat foreground = new Mat();
	private double learningRate = 0.01;


	public Mat process(Mat inputImage) {

		mog.apply(inputImage, foreground, learningRate);
		
		

		Mat structuringElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
		for (int i = 0; i < 1; i++) {
		//	Imgproc.dilate(foreground, foreground, structuringElement);
			//Imgproc.dilate(foreground, foreground, structuringElement);
			Imgproc.erode(foreground, foreground, structuringElement);
		}

		return foreground;
	}
	
	public Mat process(Mat frame1, Mat frame2) {
			return null;
	}
	
}
