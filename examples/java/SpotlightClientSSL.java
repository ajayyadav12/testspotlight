import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.time.Instant;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class SpotlightClientSSL {

    private static final String KEYSTORE_PATH = "./spotlight.jks";
    private static final String KEYSTORE_PASSWORD = "changeit";
    private static final String API_URL = "<spotlight_https_url>";
    private static final String JWT = "<your_application_token>";
    private static final String BODY_START = "{\"processStepName\": \"start\", \"time\": \"%s\"}";
    private static final String BODY_STEP = "{\"processStepName\": \"%s\", \"time\": \"%s\"}";

    private SSLSocketFactory getSSLSocketFactory() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        InputStream inputStream = new FileInputStream(new File(KEYSTORE_PATH));
        keyStore.load(inputStream, KEYSTORE_PASSWORD.toCharArray());
        inputStream.close();
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
        return sslContext.getSocketFactory();
    }

    private int request(String payload) throws Exception {
        System.out.println("Sending " + payload);
        URL url = new URL(API_URL);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setSSLSocketFactory(getSSLSocketFactory());
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + JWT);
        conn.setDoOutput(true);
        DataOutputStream out = new DataOutputStream(conn.getOutputStream());
        out.writeBytes(payload);
        out.flush();
        out.close();
        return conn.getResponseCode();
    }

    public int startSubmission() throws Exception {
        Instant now = Instant.now();
        String payload = String.format(BODY_START, now.toString());
        return request(payload);
    }

    public int addStepToSubmission(String stepName) throws Exception {
        Instant now = Instant.now();
        String payload = String.format(BODY_STEP, stepName, now.toString());
        return request(payload);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Spotlight Client Custom");
        SpotlightClientSSL client = new SpotlightClientSSL();
        System.out.println(String.format("Start %d", client.startSubmission()));
        Thread.sleep(5000);
        System.out.println(String.format("End %d", client.addStepToSubmission("end")));
    }

}