package com.ge.finance.spotlight.services;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.ge.finance.spotlight.exceptions.NotFoundException;
import com.ge.finance.spotlight.models.AnalyticsReport;
import com.ge.finance.spotlight.models.NotificationLog;
import com.ge.finance.spotlight.models.NotificationTemplate;
import com.ge.finance.spotlight.models.ParentSubmission;
import com.ge.finance.spotlight.models.Process;
import com.ge.finance.spotlight.models.ProcessStep;
import com.ge.finance.spotlight.models.ScheduleReport;
import com.ge.finance.spotlight.models.ScheduledSubmission;
import com.ge.finance.spotlight.models.Submission;
import com.ge.finance.spotlight.models.SubmissionStep;
import com.ge.finance.spotlight.models.User;
import com.ge.finance.spotlight.repositories.NotificationLogRepository;
import com.ge.finance.spotlight.repositories.NotificationTemplateRepository;
import com.ge.finance.spotlight.repositories.ProcessRepository;
import com.ge.finance.spotlight.repositories.ScheduleReportRepository;
import com.ge.finance.spotlight.repositories.SubmissionRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import freemarker.template.Configuration;
import freemarker.template.Template;

@Service
public class SpotlightEmailServiceImpl implements SpotlightEmailService {

    @Value("${send_emails}")
    private boolean send_emails;

    @Value("${email}")
    private String email;

    private NotificationTemplateRepository notificationTemplateRepository;
    private NotificationLogRepository notificationLogRepository;
    private JavaMailSender javaMailSender;
    private NotificationLog notificationLog;
    private ProcessRepository processRepository;
    private ScheduleReportRepository scheduleReportRepository;
    private SubmissionRepository submissionRepository;

    public SpotlightEmailServiceImpl(NotificationTemplateRepository notificationTemplateRepository,
            JavaMailSender javaMailSender, ScheduleReportRepository scheduleReportRepository,
            ProcessRepository processRepository, NotificationLogRepository notificationLogRepository,
            SubmissionRepository submissionRepository) {
        this.notificationTemplateRepository = notificationTemplateRepository;
        this.notificationLogRepository = notificationLogRepository;
        this.javaMailSender = javaMailSender;
        this.scheduleReportRepository = scheduleReportRepository;
        this.processRepository = processRepository;
        this.submissionRepository = submissionRepository;
    }

    private String keywordReplacement(String body, Submission submission, ScheduledSubmission schedSubmission,
            Process process, User user, SubmissionStep submissionStep, ParentSubmission parentSubmission,
            ProcessStep processStep) {
        DateFormat df = DateFormat.getInstance();
        if (schedSubmission != null) {
            submission = schedSubmission.getSubmission();
            process = schedSubmission.getProcess();
            body = body.replace("${START_TIME}", df.format(schedSubmission.getStartTime()));
            body = body.replace("${END_TIME}", df.format(schedSubmission.getEndTime()));
            body = body.replace("${SUBMISSION_ID}", schedSubmission.getId().toString());
            notificationLog.setScheduledSubmissionId(schedSubmission.getId());
        }
        if (submissionStep != null) {
            body = body.replace("${SUBMISSION_STEP_NAME}", submissionStep.getProcessStep().getName());
            body = body.replace("${SUBMISSION_STEP_STATUS}", submissionStep.getStatus().getName());
            if (submissionStep.getNotes() != null) {
                body = body.replace("${SUBMISSION_STEP_NOTES}", submissionStep.getNotes());
            }
            submission = this.submissionRepository.findById(submissionStep.getSubmissionId()).get();

            notificationLog.setProcessStepId(submissionStep.getProcessStep().getId());
        }
        if (parentSubmission != null) {
            body = body.replace("${PARENT_SUBMISSION_ID}", parentSubmission.getId().toString());
            body = body.replace("${PARENT_SUBMISSION_STATUS}", parentSubmission.getStatus().getName());
        }
        if (submission != null) {
            body = body.replace("${SUBMISSION_ID}", submission.getId().toString());
            body = body.replace("${SUBMISSION_START_TIME}", df.format(submission.getStartTime()));
            if (submission.getEndTime() != null) {
                body = body.replace("${SUBMISSION_END_TIME}", df.format(submission.getEndTime()));
            }
            if (submission.getNotes() != null) {
                body = body.replace("${SUBMISSION_NOTES}", submission.getNotes());
            }

            process = submission.getProcess();
            notificationLog.setSubmissionId(submission.getId());
        }
        if (user != null) {
            body = body.replace("${USER_NAME}", user.getName());
            body = body.replace("${USER_SSO}", user.getSso().toString());
        }
        if (process != null) {
            body = body.replace("${PROCESS_NAME}", process.getName());
            body = body.replace("${PROCESS_ID}", process.getId().toString());
            String approvalResult = (process.getApproved() == 'A') ? "Approved" : "Neglected";
            body = body.replace("${PROCESS_APPROVAL}", approvalResult);
            notificationLog.setProcessId(process.getId());
        }
        if (processStep != null) {
            body = body.replace("${PROCESS_STEP_NAME}", processStep.getName());
            body = body.replace("${PROCESS_ID}", processStep.getProcessId().toString());
            notificationLog.setProcessId(processStep.getProcessId());
            notificationLog.setProcessStepId(processStep.getId());
        }

        // Replace all unkwown ${__} fields
        body = body.replaceAll("\\$\\{\\w*\\}", "");
        return body;
    }

    private Map<String, String> sendEmail(String body, String subject, String to) {
        Map<String, String> result = new HashMap<>();
        try {
            Template template = new Template("email", new StringReader(body),
                    new Configuration(Configuration.VERSION_2_3_28));
            String emailBody = FreeMarkerTemplateUtils.processTemplateIntoString(template, null);
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED,
                    StandardCharsets.UTF_8.name());
            helper.setTo(InternetAddress.parse(to, false));
            helper.setText(emailBody, true);
            helper.setSubject(subject);
            helper.setFrom(this.email);
            notificationLog.setSendTime(new Date());
            notificationLog.setToEmails(to);
            notificationLogRepository.save(notificationLog);
            if (this.send_emails)
                javaMailSender.send(message);
            result.put("status", "ok");

        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "error");
            result.put("reason", e.getMessage());
        }
        return result;
    }

    @Override
    public Map<String, String> sendProcessSummaryReport(Long notificationTemplateId, AnalyticsReport analyticsReport,
            String to) {

        NotificationTemplate notificationTemplate = notificationTemplateRepository.findById(notificationTemplateId)
                .orElseThrow(NotFoundException::new);

        Process process = processRepository.findById(analyticsReport.getProcess().getId())
                .orElseThrow(NotFoundException::new);

        ScheduleReport scheduleReport = scheduleReportRepository.findById(analyticsReport.getScheduleReportId())
                .orElseThrow(NotFoundException::new);

        notificationLog = new NotificationLog();
        notificationLog.setNotificationTemplate(notificationTemplate.getId());
        notificationLog.setProcessId(process.getId());
        String body = notificationTemplate.getBody();
        String subject = notificationTemplate.getSubject();

        DateFormat df = DateFormat.getInstance();
        String submission = (analyticsReport.getSubmissionLevel().equals("C")) ? "Child Submission"
                : "Parent Submission";
        // Replace keywords
        body = body.replace("${PROCESS_NAME}", process.getName());
        body = body.replace("${PROCESS_ID}", analyticsReport.getProcess().getId().toString());
        body = body.replace("${START_TIME}", df.format(analyticsReport.getStartTime()));
        body = body.replace("${END_TIME}", df.format(analyticsReport.getEndTime()));
        body = body.replace("${SUBMISSION}", submission);
        body = body.replace("${GET_SUBMISSION}", analyticsReport.getSubmissionLevel());
        body = body.replace("${RANGE_LENGTH}", scheduleReport.getRangeLength().toString());

        return sendEmail(body, subject, to);
    }

    @Override
    public void genericSend(Long notificationTemplateId, String to, ScheduledSubmission schedSubmission,
            Submission submission, Process process, User user, SubmissionStep submissionStep,
            ParentSubmission parentSubmission, ProcessStep processStep) {
        NotificationTemplate notificationTemplate = notificationTemplateRepository.findById(notificationTemplateId)
                .orElseThrow(NotFoundException::new);
        if (schedSubmission != null && schedSubmission.getDisabled()) {
            return;
        }
        notificationLog = new NotificationLog();
        notificationLog.setNotificationTemplate(notificationTemplate.getId());
        String body = notificationTemplate.getBody();
        String subject = notificationTemplate.getSubject();

        body = keywordReplacement(body, submission, schedSubmission, process, user, submissionStep, parentSubmission,
                processStep);
        subject = keywordReplacement(subject, submission, schedSubmission, process, user, submissionStep,
                parentSubmission, processStep);

        sendEmail(body, subject, to);
    }

    @Override
    public void genericSMSSend(Long notificationTemplateId, String to, ScheduledSubmission schedSubmission,
            Submission submission, Process process, User user, SubmissionStep submissionStep,
            ParentSubmission parentSubmission) {
        NotificationTemplate notificationTemplate = notificationTemplateRepository.findById(notificationTemplateId)
                .orElseThrow(NotFoundException::new);
        if (schedSubmission != null && schedSubmission.getDisabled()) {
            return;
        }
        notificationLog = new NotificationLog();
        notificationLog.setNotificationTemplate(notificationTemplate.getId());
        String body = "";
        String subject = notificationTemplate.getSubject();

        subject = keywordReplacement(subject, submission, schedSubmission, process, user, submissionStep,
                parentSubmission, null);

        sendEmail(body, subject, to);
    }

}
