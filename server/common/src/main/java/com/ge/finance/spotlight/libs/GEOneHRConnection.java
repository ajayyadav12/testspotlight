package com.ge.finance.spotlight.libs;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GEOneHRConnection {

    /**
     * Get manager SSO from OneHR server.
     * 
     * @param userSSO
     * @return
     * @throws Exception
     */
    public static String getManagerSSOFromOneHR(Long userSSO, String parameter) throws Exception {
        String managerSSO = null;
        String oneHRURL = "http://search.corporate.ge.com/ldq/Query?serverID=ssoprod&searchBase=ou=geWorker,+o=ge.com&Prebuilt=true&scope=2&filter=(uid="
                + userSSO + ")";
        URL url = new URL(oneHRURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setDoOutput(true);
        DataOutputStream out = new DataOutputStream(conn.getOutputStream());
        out.flush();
        out.close();
        int status = conn.getResponseCode();
        if (status == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuffer content = new StringBuffer();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            String response = content.toString();
            String regex = (parameter.equalsIgnoreCase("name")) ? "(cn.>)(\\s*)(<dsml:value>)(\\w*+,\\s*\\w*)"
                    : "(gessosupervisorid.>)(\\s*)(<dsml:value>)(\\d*)";
            Pattern SUBMISSION_ID_REGEX = Pattern.compile(regex);
            Matcher matcher = SUBMISSION_ID_REGEX.matcher(response);
            if (matcher.find()) {
                managerSSO = (parameter.equalsIgnoreCase("name")) ? matcher.group(4) : matcher.group(4);
            }
        }
        return managerSSO;
    }
}
