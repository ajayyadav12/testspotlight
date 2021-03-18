package com.ge.finance.spotlight.controllers;

import java.security.Principal;
import java.util.Date;

import javax.validation.Valid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ge.finance.spotlight.exceptions.ForbiddenException;
import com.ge.finance.spotlight.models.SubmissionRequest;
import com.ge.finance.spotlight.models.SubmissionStep;
import com.ge.finance.spotlight.repositories.FileSubmissionRepository;
import com.ge.finance.spotlight.repositories.SubmissionRepository;
import com.ge.finance.spotlight.repositories.SubmissionRequestRepository;
import com.ge.finance.spotlight.requests.SubmissionStepRequest;
import com.ge.finance.spotlight.security.Constants;
import com.ge.finance.spotlight.services.SubmissionStepService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/submissions/steps")
public class SubmissionStepController {

    private FileSubmissionRepository fileSubmissionRepository;
    private SubmissionRepository submissionRepository;
    private SubmissionStepService submissionStepService;
    private SubmissionRequestRepository submissionRequestRepository;

    public SubmissionStepController(FileSubmissionRepository fileSubmissionRepository,
            SubmissionRepository submissionRepository, SubmissionStepService submissionStepService,
            SubmissionRequestRepository submissionRequestRepository) {
        this.fileSubmissionRepository = fileSubmissionRepository;
        this.submissionRepository = submissionRepository;
        this.submissionStepService = submissionStepService;
        this.submissionRequestRepository = submissionRequestRepository;
    }

    @PostMapping("/")
    SubmissionStep create(Principal principal, @Valid @RequestBody SubmissionStepRequest submissionStepRequest)
            throws JsonMappingException, JsonProcessingException {
        Long processId = Long.parseLong(principal.getName());
        return submissionStepService.create(processId, submissionStepRequest);
    }

    @PostMapping("/file")
    SubmissionStep createForFileSubmission(@Valid @RequestBody SubmissionStepRequest submissionStepRequest)
            throws JsonMappingException, JsonProcessingException {
        if (fileSubmissionRepository.existsBySubmissionIdAndSecret(submissionStepRequest.getSubmissionId(),
                submissionStepRequest.getSecret())) {
            Long processId = submissionRepository.findProcessIdForSubmissionId(submissionStepRequest.getSubmissionId())
                    .orElse(0L);
            return submissionStepService.create(processId, submissionStepRequest);
        } else {
            throw new ForbiddenException();
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
