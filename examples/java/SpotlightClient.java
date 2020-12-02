import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpotlightClient {
	
	private static final String API_URL = "http://localhost:9001/appsapi/v1/submissions/steps/";
	private static final String JWT = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI0In0.EIj6NR5thaI0csLwc2PGEimHnS67tnaw9B9GUAYGdVgytlFUOZnXrwEkEXPjtppn6G-RmlzczlsLRZzbax7cgw";
	private static final String BODY_START = "{\"processStepName\": \"start\", \"time\": \"%s\"}";
	private static final String BODY_STEP = "{\"processStepName\": \"%s\", \"time\": \"%s\"}";
	private static final String BODY_STEP_WITH_STATUS = "{\"processStepName\": \"%s\", \"time\": \"%s\", \"status\": \"%s\"}";
	private static final String BODY_STEP_WITH_ID = "{\"processStepName\": \"%s\", \"submissionId\": %d, \"time\": \"%s\"}";
	private static final Pattern SUBMISSION_ID_REGEX = Pattern.compile("\"submissionId\":(\\d+)");

	private long submissionId;

	private void request(String payload) throws Exception {
		System.out.println("Sending " + payload);
		URL url = new URL(API_URL);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("Authorization", "Bearer " + JWT);
		conn.setDoOutput(true);
		DataOutputStream out = new DataOutputStream(conn.getOutputStream());
		out.writeBytes(payload);
		out.flush();
		out.close();
		int status = conn.getResponseCode();
		if (status == 200) {
			System.out.println("OK");
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuffer content = new StringBuffer();
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			in.close();
			String response = content.toString();
			Matcher matcher = SUBMISSION_ID_REGEX.matcher(response);
			if (matcher.find()) {
				submissionId = Long.parseLong(matcher.group(1));
			} else {
				System.out.println("Submission id not found");
			}
		} else {
			System.out.println("Error " + status);
		}
	}

	public void startSubmission() throws Exception {
		Instant now = Instant.now();
		String payload = String.format(BODY_START, now.toString());
		request(payload);
	}

	public void addStepToSubmissionWithId(String stepName) throws Exception {
		Instant now = Instant.now();
		String payload = String.format(BODY_STEP_WITH_ID, stepName, submissionId, now.toString());
		request(payload);
	}

	public void addStepToSubmission(String stepName) throws Exception {
		Instant now = Instant.now();
		String payload = String.format(BODY_STEP, stepName, now.toString());
		request(payload);
	}

	public void addStepToSubmissionWithStatus(String stepName, String statusName) throws Exception {
		Instant now = Instant.now();
		String payload = String.format(BODY_STEP_WITH_STATUS, stepName, now.toString(), statusName);
		request(payload);
	}

	public static void main(String[] args) throws Exception {
		SpotlightClient client = new SpotlightClient();
		client.startSubmission();
		Thread.sleep(5000);
		// you can use the submission id received
		client.addStepToSubmissionWithId("loading");
		Thread.sleep(5000);
		// or you can add the step to the last open submission
		client.addStepToSubmission("extracting");
		Thread.sleep(5000);
		// a new step will close the previous step, in this case extracting step, using the time as endTime and with status success
		client.addStepToSubmission("processing");
		Thread.sleep(5000);
		// or you can close the previous step providing the same step name, in this case no new step will be created
		client.addStepToSubmission("processing");
		Thread.sleep(5000);
		client.addStepToSubmission("sending");
		Thread.sleep(5000);
		// you can also attach a status to be added to the previous step
		client.addStepToSubmissionWithStatus("sending", "warning");
		Thread.sleep(5000);
		client.addStepToSubmissionWithStatus("end");
	}

}