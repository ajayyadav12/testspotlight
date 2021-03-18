package com.ge.finance.spotlight.controllers;

import java.security.Principal;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ge.finance.spotlight.models.SubmissionRequest;
import com.ge.finance.spotlight.repositories.SubmissionRequestRepository;
import com.ge.finance.spotlight.requests.SubmissionStepRequest;
import com.ge.finance.spotlight.security.Constants;
import com.ge.finance.spotlight.services.SubmissionStepService;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/submissions/steps")
@EnableScheduling
public class SubmissionRequestController {

    private static final long MINUTES_1 = 60_000;
    private SubmissionRequestRepository submissionRequestRepository;
    private SubmissionStepService submissionStepService;

    public SubmissionRequestController(SubmissionRequestRepository submissionRequestRepository,
            SubmissionStepService submissionStepService) {
        this.submissionRequestRepository = submissionRequestRepository;
        this.submissionStepService = submissionStepService;
    }

    @PostMapping("/")
    SubmissionRequest create(Principal principal, @Valid @RequestBody SubmissionStepRequest submissionStepRequest)
            throws JsonMappingException, JsonProcessingException {
        
        System.out.println("Submission Created on 001 Server");
        Long processId = Long.parseLong(principal.getName());
        SubmissionRequest submissionRequest = new SubmissionRequest();
        submissionRequest.setRequest(getAsJSONString(submissionStepRequest));
        submissionRequest.setStartTime(new Date());
        submissionRequest.setState(Constants.QUEUED);
        submissionRequest.setProcessId(processId);
        return submissionRequestRepository.save(submissionRequest);
    }

    @Scheduled(initialDelay = MINUTES_1, fixedRate = MINUTES_1) // 1 minute
    public void executeSubmissionRequest() throws JsonMappingException, JsonProcessingException {

        List<SubmissionRequest> submissionRequests = submissionRequestRepository
                .findByStateOrderByStartTimeAsc(Constants.QUEUED);
        if (!submissionRequests.isEmpty()) {
            for (SubmissionRequest stepRequest : submissionRequests) {
                stepRequest.setState(Constants.PROCESSING);
                submissionRequestRepository.save(stepRequest);
                SubmissionStepRequest submissionStepRequest = new ObjectMapper().readValue(stepRequest.getRequest(),
                        SubmissionStepRequest.class);
                if (submissionStepRequest != null) {
                    try {
                        submissionStepService.create(stepRequest.getProcessId(), submissionStepRequest);
                        stepRequest.setState(Constants.COMPLETED);
                    } catch (Exception e) {
                        e.printStackTrace();
                        stepRequest.setState(Constants.FAILED);
                    } finally {
                        submissionRequestRepository.save(stepRequest);
                    }
                }
            }
        }
    }

    String getAsJSONString(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}
