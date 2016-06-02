package com.ciandt.sample.detection.video.vagao;

import org.opencv.core.Mat;

public interface CallBack {
	
	public void setOCRResult(String text, Mat img); 

}
