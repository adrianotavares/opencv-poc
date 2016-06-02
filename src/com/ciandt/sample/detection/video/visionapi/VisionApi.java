package com.ciandt.sample.detection.video.visionapi;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Base64;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import com.ciandt.sample.detection.video.vagao.CallBack;



public class VisionApi extends Thread {
	
	private Mat img ;
	private CallBack callback;

	public VisionApi(Mat img, CallBack callback) {
		this.img = img;
		this.callback = callback;
	}

	public void run() {
		try {
			String text = this.sendImage(img);
            callback.setOCRResult(text, img);
		} catch (Exception e) {

		}
	}

	  public  String sendImage(Mat img) throws Exception {
		    String json = "";
		    String url = "https://vision.googleapis.com/v1/images:annotate?key=AIzaSyCllsnHYahxHnstOxB37-rYhLOMwyply1I";
		    URL obj = new URL(url);
		    HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
		    
		    
		    MatOfByte buff = new MatOfByte();
			Imgcodecs.imencode(".jpg", img, buff);
			String imgstr = Base64.getEncoder().encodeToString(buff.toArray());
			  
	
		    con.setRequestMethod("POST");
		    con.setRequestProperty("Content-Type", "application/json");

		    String urlParameters = "{"
		            + "  \"requests\":["
		            + "    {"
		            + "      \"image\":{"
		            + "        \"content\":\"" + imgstr + "\""
		            + "      },"
		            + "      \"features\":["
		            + "        {"
		            + "          \"type\":\"TEXT_DETECTION\","
		            + "          \"maxResults\":10"
		            + "        }"
		            + "      ]"
		            + "    }"
		            + "  ]"
		            + "}";

		    // Send post request
		    con.setDoOutput(true);
		    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		    wr.writeBytes(urlParameters);
		    wr.flush();
		    wr.close();

		    BufferedReader in = new BufferedReader(
		            new InputStreamReader(con.getInputStream()));
		    String inputLine;
		    StringBuffer response = new StringBuffer();

		    while ((inputLine = in.readLine()) != null) {
		        response.append(inputLine);
		    }
		    in.close();
		    
		    json = response.toString();
		    
		    JSONObject jsonObject = new JSONObject(json);
		    String text = "";
		    System.out.println(json);
		    JSONArray responses = jsonObject.getJSONArray("responses");
		    if (responses.length() > 0 ){
		    	JSONObject item = responses.getJSONObject(0);
		    	if (json.contains("textAnnotations")){
		    		JSONArray textAnnotations = item.getJSONArray("textAnnotations");
		    		if (textAnnotations.length() > 0 ){
		    			JSONObject subitem = textAnnotations.getJSONObject(0);
		    			text = subitem.getString("description");
		    		}
		    	}
		    }
		    
		    
		    return text;
		    		    
		    

		}
}
