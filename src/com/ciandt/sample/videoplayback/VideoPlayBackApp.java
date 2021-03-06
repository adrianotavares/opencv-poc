package com.ciandt.sample.videoplayback;


import java.awt.Image;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import com.ciandt.sample.videocapture.ImageProcessor;

public class VideoPlayBackApp {
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	private JFrame frame;
	private JLabel imageLabel;
	static final String path = "carros.mp4";

	public static void main(String[] args) throws InterruptedException, IOException {
		VideoPlayBackApp app = new VideoPlayBackApp();
		app.initGUI();
		app.runMainLoop(args);
	}

	private void initGUI() {
		frame = new JFrame("Video Playback Example");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(640, 480);
		imageLabel = new JLabel();
		frame.add(imageLabel);
		frame.setVisible(true);
	}

	private void runMainLoop(String[] args) throws InterruptedException, IOException {
		ImageProcessor imageProcessor = new ImageProcessor();
		Mat webcamMatImage = new Mat();
		Image tempImage;
		VideoCapture capture = new VideoCapture();
		capture.open(path);
		if (capture.isOpened()) {
			while (capture.grab()) {
				capture.read(webcamMatImage);
				if (!webcamMatImage.empty()) {
					tempImage = imageProcessor.toBufferedImage(webcamMatImage);
					ImageIcon imageIcon = new ImageIcon(tempImage, "Video playback");
					imageLabel.setIcon(imageIcon);
					frame.pack(); // this will resize the window to fit the
								  // image
					Thread.sleep(50);
				} else {
					System.out.println(" Frame not captured or video has finished");
					break;
				}
			}
		} else {
			System.out.println("Couldn't open video file.");
		}

	}
}
