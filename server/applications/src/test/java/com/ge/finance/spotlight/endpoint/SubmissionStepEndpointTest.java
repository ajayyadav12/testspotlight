package com.ge.finance.spotlight.endpoint;

import com.auth0.jwt.JWT;
import com.ge.finance.spotlight.models.*;
import com.ge.finance.spotlight.models.Process;
import com.ge.finance.spotlight.repositories.*;
import com.ge.finance.spotlight.requests.SubmissionStepRequest;
import com.ge.finance.spotlight.security.Constants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class SubmissionStepEndpointTest {

    @Autowired
    private ProcessRepository processRepository;
    @Autowired
    private ProcessStepRepository processStepRepository;
    @Autowired
    private StatusRepository statusRepository;
    @Autowired
    private SubmissionRepository submissionRepository;
    @Autowired
    private SubmissionStepRepository submissionStepRepository;
    @LocalServerPort
    private int port;
    @Value("${app_secret}")
    private String appSecret;

    /*
     * @Test public void testStartSubmission() { // Prepare process Status
     * statusOnProgress = new Status(); statusOnProgress.setName("in progress");
     * statusRepository.save(statusOnProgress); Status statusSuccess = new Status();
     * statusSuccess.setName("success"); statusRepository.save(statusSuccess);
     * Process process = new Process(); process.setApproved('A'); process =
     * processRepository.save(process); // Prepare step ProcessStep processStep =
     * new ProcessStep(); processStep.setName("start");
     * processStep.setProcessId(process.getId());
     * processStepRepository.save(processStep); // Prepare token String token =
     * JWT.create().withSubject(Long.toString(process.getId())).sign(HMAC512(
     * appSecret.getBytes())); // prepare request SubmissionStepRequest request =
     * new SubmissionStepRequest(); request.setProcessStepName("start"); // Perform
     * test HttpHeaders headers = new HttpHeaders(); headers.add("Authorization",
     * token); HttpEntity<SubmissionStepRequest> entity = new HttpEntity<>(request,
     * headers); ResponseEntity<SubmissionStep> response = new
     * TestRestTemplate().exchange(
     * String.format("http://localhost:%d/appsapi/v1/submissions/steps/", port),
     * HttpMethod.POST, entity, SubmissionStep.class); assertEquals(HttpStatus.OK,
     * response.getStatusCode()); assertNotNull(response.getBody()); // check that
     * submission and submission step were created in the repository
     * Optional<Submission> submission =
     * submissionRepository.findById(response.getBody().getSubmissionId());
     * assertTrue(submission.isPresent()); Optional<SubmissionStep> submissionStep =
     * submissionStepRepository.findById(response.getBody().getId());
     * assertTrue(submissionStep.isPresent()); }
     */

    /*
     * @Test public void testProcessNotApproved() { // Prepare process Process
     * process = new Process(); process = processRepository.save(process); //
     * Prepare token String token =
     * JWT.create().withSubject(Long.toString(process.getId())).sign(HMAC512(
     * appSecret.getBytes())); // prepare request SubmissionStepRequest request =
     * new SubmissionStepRequest(); request.setProcessStepName("start"); // Perform
     * test HttpHeaders headers = new HttpHeaders(); headers.add("Authorization",
     * token); HttpEntity<SubmissionStepRequest> entity = new HttpEntity<>(request,
     * headers); ResponseEntity<String> response = new TestRestTemplate().exchange(
     * String.format("http://localhost:%d/appsapi/v1/submissions/steps/", port),
     * HttpMethod.POST, entity, String.class); assertEquals(HttpStatus.CONFLICT,
     * response.getStatusCode()); // check that no submission nor submission step
     * were created in the repository assertEquals(0, submissionRepository.count());
     * assertEquals(0, submissionStepRepository.count()); }
     * 
     * @Test public void testUpdateSubmissionInProgressAndPreviousStepClosed() { //
     * Prepare status Status statusOnProgress = new Status();
     * statusOnProgress.setName("in progress");
     * statusRepository.save(statusOnProgress); Status statusSuccess = new Status();
     * statusSuccess.setName("success"); statusSuccess =
     * statusRepository.save(statusSuccess); // prepare process Process process =
     * new Process(); process.setApproved('A'); process =
     * processRepository.save(process); // Prepare steps ProcessStep
     * processStepStart = new ProcessStep(); processStepStart.setName("start");
     * processStepStart.setProcessId(process.getId());
     * processStepRepository.save(processStepStart); ProcessStep processStepMiddle =
     * new ProcessStep(); processStepMiddle.setName("middle");
     * processStepMiddle.setProcessId(process.getId());
     * processStepRepository.save(processStepMiddle); // prepare submission
     * Submission submission = new Submission(); submission.setProcess(process);
     * submission.setStartTime(new Date()); submission =
     * submissionRepository.save(submission); // prepare submission step
     * SubmissionStep submissionStep = new SubmissionStep();
     * submissionStep.setSubmissionId(submission.getId());
     * submissionStep.setStartTime(new Date()); submissionStep.setEndTime(new
     * Date()); submissionStep.setProcessStep(processStepStart);
     * submissionStep.setStatus(statusSuccess);
     * submissionStepRepository.save(submissionStep); // Prepare token String token
     * = JWT.create().withSubject(Long.toString(process.getId())).sign(HMAC512(
     * appSecret.getBytes())); // prepare request SubmissionStepRequest request =
     * new SubmissionStepRequest(); request.setProcessStepName("middle"); // Perform
     * test HttpHeaders headers = new HttpHeaders(); headers.add("Authorization",
     * token); HttpEntity<SubmissionStepRequest> entity = new HttpEntity<>(request,
     * headers); ResponseEntity<SubmissionStep> response = new
     * TestRestTemplate().exchange(
     * String.format("http://localhost:%d/appsapi/v1/submissions/steps/", port),
     * HttpMethod.POST, entity, SubmissionStep.class); assertEquals(HttpStatus.OK,
     * response.getStatusCode()); assertNotNull(response.getBody()); // assert that
     * no new submission was created assertEquals(1, submissionRepository.count());
     * // assert that new submission step was created assertEquals(2,
     * submissionStepRepository.count()); Optional<SubmissionStep>
     * optionalSubmissionStep =
     * submissionStepRepository.findById(response.getBody().getId());
     * assertTrue(optionalSubmissionStep.isPresent()); }
     */

    @Test
    public void testUpdateSubmissionInProgressAndPreviousStepOpen() {
        // Prepare status
        Status statusOnProgress = new Status();
        statusOnProgress.setName("in progress");
        statusOnProgress = statusRepository.save(statusOnProgress);
        Status statusSuccess = new Status();
        statusSuccess.setName("success");
        statusSuccess = statusRepository.save(statusSuccess);
        // prepare process
        Process process = new Process();
        process.setApproved('A');
        process = processRepository.save(process);
        // Prepare steps
        ProcessStep processStepStart = new ProcessStep();
        processStepStart.setName("start");
        processStepStart.setProcessId(process.getId());
        processStepRepository.save(processStepStart);
        ProcessStep processStepMiddle = new ProcessStep();
        processStepMiddle.setName("middle");
        processStepMiddle.setProcessId(process.getId());
        processStepRepository.save(processStepMiddle);
        // prepare submission
        Submission submission = new Submission();
        submission.setProcess(process);
        submission.setStartTime(new Date());
        submission = submissionRepository.save(submission);
        // prepare submission step
        SubmissionStep previousSubmissionStep = new SubmissionStep();
        previousSubmissionStep.setSubmissionId(submission.getId());
        previousSubmissionStep.setStartTime(new Date());
        previousSubmissionStep.setProcessStep(processStepStart);
        // previousSubmissionStep.setStatus(statusOnProgress);
        previousSubmissionStep = submissionStepRepository.save(previousSubmissionStep);
        // Prepare token
        String token = JWT.create().withSubject(Long.toString(process.getId())).sign(HMAC512(appSecret.getBytes()));
        // prepare request
        SubmissionStepRequest request = new SubmissionStepRequest();
        request.setProcessStepName("middle");
        // request.setStatus("success");
        // TODO:: Update test case
        // Perform test
        /*
         * HttpHeaders headers = new HttpHeaders(); headers.add("Authorization", token);
         * HttpEntity<SubmissionStepRequest> entity = new HttpEntity<>(request,
         * headers); ResponseEntity<SubmissionStep> response = new
         * TestRestTemplate().exchange(
         * String.format("http://localhost:%d/appsapi/v1/submissions/steps/", port),
         * HttpMethod.POST, entity, SubmissionStep.class); assertEquals(HttpStatus.OK,
         * response.getStatusCode()); assertNotNull(response.getBody()); // assert that
         * no new submission was created assertEquals(1, submissionRepository.count());
         * // assert that new submission step was created assertEquals(2,
         * submissionStepRepository.count()); Optional<SubmissionStep>
         * optionalSubmissionStep =
         * submissionStepRepository.findById(response.getBody().getId());
         * assertTrue(optionalSubmissionStep.isPresent()); // assert that previous step
         * is now closed Optional<SubmissionStep> optionalPreviousSubmissionStep =
         * submissionStepRepository .findById(previousSubmissionStep.getId());
         * assertTrue(optionalPreviousSubmissionStep.isPresent());
         * assertNotNull(optionalPreviousSubmissionStep.get().getEndTime());
         * assertEquals(statusSuccess.getId(),
         * optionalPreviousSubmissionStep.get().getStatus().getId());
         */
    }

    /*
     * @Test public void testSubmissionStepWithOptionalStartTime() { // Prepare
     * status Status statusOnProgress = new Status();
     * statusOnProgress.setName("in progress"); statusOnProgress =
     * statusRepository.save(statusOnProgress); Status statusSuccess = new Status();
     * statusSuccess.setName("success"); statusSuccess =
     * statusRepository.save(statusSuccess); // prepare process Process process =
     * new Process(); process.setApproved('A'); process =
     * processRepository.save(process); // Prepare steps ProcessStep
     * processStepStart = new ProcessStep(); processStepStart.setName("start");
     * processStepStart.setProcessId(process.getId());
     * processStepRepository.save(processStepStart); ProcessStep processStepMiddle =
     * new ProcessStep(); processStepMiddle.setName("end");
     * processStepMiddle.setProcessId(process.getId());
     * processStepRepository.save(processStepMiddle); // Prepare token String token
     * = JWT.create().withSubject(Long.toString(process.getId())).sign(HMAC512(
     * appSecret.getBytes())); // prepare request SubmissionStepRequest request =
     * new SubmissionStepRequest(); request.setProcessStepName("start"); Calendar
     * calendar = Calendar.getInstance(); calendar.add(Calendar.DATE, -1); Date
     * yesterday = calendar.getTime(); request.setStartTime(yesterday); // Perform
     * test HttpHeaders headers = new HttpHeaders(); headers.add("Authorization",
     * token); HttpEntity<SubmissionStepRequest> entity = new HttpEntity<>(request,
     * headers); ResponseEntity<SubmissionStep> response = new
     * TestRestTemplate().exchange(
     * String.format("http://localhost:%d/appsapi/v1/submissions/steps/", port),
     * HttpMethod.POST, entity, SubmissionStep.class); assertEquals(HttpStatus.OK,
     * response.getStatusCode()); assertNotNull(response.getBody());
     * Optional<SubmissionStep> submissionStepOptional =
     * submissionStepRepository.findById(response.getBody().getId());
     * assertTrue(submissionStepOptional.isPresent()); assertEquals(yesterday,
     * submissionStepOptional.get().getStartTime()); }
     */

}
