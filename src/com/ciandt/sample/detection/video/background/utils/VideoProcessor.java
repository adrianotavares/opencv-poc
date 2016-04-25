package com.ciandt.sample.detection.video.background.utils;

import org.opencv.core.Mat;

public interface VideoProcessor {
	public Mat process(Mat inputImage);
	public Mat process(Mat frame1, Mat frame2) ;
	

}
