package com.ge.finance.spotlight.services;

import java.util.Map;

import com.ge.finance.spotlight.models.AnalyticsReport;
import com.ge.finance.spotlight.models.ParentSubmission;
import com.ge.finance.spotlight.models.Process;
import com.ge.finance.spotlight.models.ProcessStep;
import com.ge.finance.spotlight.models.ScheduledSubmission;
import com.ge.finance.spotlight.models.Submission;
import com.ge.finance.spotlight.models.SubmissionStep;
import com.ge.finance.spotlight.models.User;

public interface SpotlightEmailService {

        public Map<String, String> sendProcessSummaryReport(Long notificationTemplateId,
                        AnalyticsReport analyticsReport, String to);

        public void genericSend(Long notificationTemplateId, String to, ScheduledSubmission schedSubmission,
                        Submission submission, Process process, User user, SubmissionStep submissionStep,
                        ParentSubmission parentSubmission, ProcessStep processStep);

        public void genericSMSSend(Long notificationTemplateId, String to, ScheduledSubmission schedSubmission,
                        Submission submission, Process process, User user, SubmissionStep submissionStep,
                        ParentSubmission parentSubmission);
}
