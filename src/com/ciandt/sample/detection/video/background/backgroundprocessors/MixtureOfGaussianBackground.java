package com.ciandt.sample.detection.video.background.backgroundprocessors;

import org.opencv.core.Mat;
import org.opencv.video.BackgroundSubtractorMOG2;

import com.ciandt.sample.detection.video.background.utils.VideoProcessor;




public class MixtureOfGaussianBackground implements VideoProcessor {
	
	private BackgroundSubtractorMOG2 mog =  org.opencv.video.Video.createBackgroundSubtractorMOG2();
	private Mat foreground = new Mat();
	private double learningRate = 0.01;
	

	public Mat process(Mat inputImage) {
		
		mog.apply(inputImage, foreground, learningRate);
		
		return foreground;
	}
	
	public Mat process(Mat frame1, Mat frame2) {
		return null;
	}
	


}
