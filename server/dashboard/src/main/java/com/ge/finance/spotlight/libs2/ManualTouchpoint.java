package com.ge.finance.spotlight.libs2;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ManualTouchpoint {

     
    public static String DoManualTouchpoint(String processToken, Long submissionId, String sStatus, String notes, String appsApiURL) throws Exception {   
        String outputValue = null;          
        URL url = new URL(appsApiURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
		
        conn.setRequestProperty("Authorization", processToken);
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
		conn.setRequestProperty("Accept", "application/json");
		
		conn.setDoOutput(true);	        

        String jsonInputString = "{\"submissionId\": " + submissionId + ", \"processStepName\": \"end\", \"status\": \"" + sStatus + "\", \"submissionNotes\": \""+notes+"\", \"stepNotes\": \""+notes+"\"}";

        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        
        byte[] input = jsonInputString.getBytes("utf-8");
        wr.write(input, 0, input.length);                     
        wr.flush();             
        wr.close();        

        try(BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))){
			StringBuilder response = new StringBuilder();
			String responseLine = null;
			while ((responseLine = br.readLine()) != null) {
				response.append(responseLine.trim());
			}
			outputValue = response.toString();
		}       
        return outputValue;
    }
}
