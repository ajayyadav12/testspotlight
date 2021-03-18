package com.ge.finance.spotlight.services;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.ge.finance.spotlight.dto.EmailModel;
import com.ge.finance.spotlight.dto.ScheduledSubmissionDTO;
import com.ge.finance.spotlight.exceptions.NotFoundException;
import com.ge.finance.spotlight.models.*;
import com.ge.finance.spotlight.models.Process;
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

    protected String keywordReplacement(String body, EmailModel emailModel, ProcessExportRequest processExportRequest) {
        DateFormat df = DateFormat.getInstance();
        if (emailModel.schedSubmission != null) {
            emailModel.submission = emailModel.schedSubmission.getSubmission();
            emailModel.process = emailModel.schedSubmission.getProcess();
            body = body.replace("${START_TIME}", df.format(emailModel.schedSubmission.getStartTime()));
            body = body.replace("${END_TIME}", df.format(emailModel.schedSubmission.getEndTime()));
            body = body.replace("${SUBMISSION_ID}", emailModel.schedSubmission.getId().toString());
            body = body.replace("${SCHED_SUBMISSION_DISABLED_NOTE}", emailModel.schedSubmission.getDisabledNote() != null ? emailModel.schedSubmission.getDisabledNote(): "");
            body = body.replace("${SCHED_SUBMISSION_ACKNOWLEDGEMENT_NOTE}", emailModel.schedSubmission.getAcknowledgementNote() != null ? emailModel.schedSubmission.getAcknowledgementNote(): "");
            notificationLog.setScheduledSubmissionId(emailModel.schedSubmission.getId());
        }
        if (emailModel.submissionStep != null) {
            body = body.replace("${SUBMISSION_STEP_NAME}", emailModel.submissionStep.getProcessStep().getName());
            body = body.replace("${SUBMISSION_STEP_STATUS}", emailModel.submissionStep.getStatus().getName());
            body = body.replace("${SUBMISSION_STEP_NOTES}", emailModel.submissionStep.getNotes() != null ? emailModel.submissionStep.getNotes(): "");
            emailModel.submission = this.submissionRepository.findById(emailModel.submissionStep.getSubmissionId()).get();

            notificationLog.setProcessStepId(emailModel.submissionStep.getProcessStep().getId());
        }
        if (emailModel.parentSubmission != null) {
            body = body.replace("${PARENT_SUBMISSION_ID}", emailModel.parentSubmission.getId().toString());
            body = body.replace("${PARENT_SUBMISSION_STATUS}", emailModel.parentSubmission.getStatus().getName());
        }
        if (emailModel.submission != null) {
            body = body.replace("${SUBMISSION_ID}", emailModel.submission.getId().toString());
            body = body.replace("${SUBMISSION_START_TIME}", df.format(emailModel.submission.getStartTime()));
            body = body.replace("${SUBMISSION_STATUS}", emailModel.submission.getStatus().getName());
            body = body.replace("${SUBMISSION_BU}", emailModel.submission.getBu() == null ? "" : emailModel.submission.getBu());
            body = body.replace("${SUBMISSION_ALTID}", emailModel.submission.getAltId() == null ? "" : emailModel.submission.getAltId());            
            body = body.replace("${SUBMISSION_END_TIME}", emailModel.submission.getEndTime() != null ? df.format(emailModel.submission.getEndTime()): "");
            body = body.replace("${SUBMISSION_NOTES}", emailModel.submission.getNotes() != null ? emailModel.submission.getNotes(): "");
            body = body.replace("${SUBMISSION_ACKNOWLEDGEMENT_NOTE}", emailModel.submission.getAcknowledgementNote() != null ? emailModel.submission.getAcknowledgementNote(): "");
            ScheduledSubmissionDTO schedSubmissionDTO = emailModel.submission.getScheduledSubmission();
            if (schedSubmissionDTO != null) {
                body = body.replace("${SCHED_SUBMISSION_DISABLED_NOTE}", schedSubmissionDTO.getDisabledNote() != null ? schedSubmissionDTO.getDisabledNote(): "");
                body = body.replace("${SCHED_SUBMISSION_ACKNOWLEDGEMENT_NOTE}", schedSubmissionDTO.getAcknowledgementNote() != null ? schedSubmissionDTO.getAcknowledgementNote(): "");
            }            

            emailModel.process = emailModel.submission.getProcess();
            notificationLog.setSubmissionId(emailModel.submission.getId());
        }
        if (emailModel.user != null) {
            body = body.replace("${USER_NAME}", emailModel.user.getName());
            body = body.replace("${USER_SSO}", emailModel.user.getSso().toString());
        }
        if (emailModel.process != null) {
            body = body.replace("${PROCESS_NAME}", emailModel.process.getName());
            body = body.replace("${PROCESS_ID}", emailModel.process.getId().toString());
            String approvalResult = (emailModel.process.getApproved() == 'A') ? "Approved" : "Neglected";
            body = body.replace("${PROCESS_APPROVAL}", approvalResult);
            body = body.replace("${PROCESS_PARENT_NAME}", emailModel.process.getProcessParent() != null ? emailModel.process.getProcessParent().getName(): "");            
            notificationLog.setProcessId(emailModel.process.getId());
        }
        if (emailModel.processStep != null) {
            body = body.replace("${PROCESS_STEP_NAME}", emailModel.processStep.getName());
            body = body.replace("${PROCESS_ID}", emailModel.processStep.getProcessId().toString());
            notificationLog.setProcessId(emailModel.processStep.getProcessId());
            notificationLog.setProcessStepId(emailModel.processStep.getId());
        }
        if (processExportRequest != null) {
            body = body.replace("${PROCESS_EXPORT_STATE}", processExportRequest.getState().toString());
            body = body.replace("${PROCESS_EXPORT_NOTES}", processExportRequest.getNotes());
        }

        // Replace all unkwown ${__} fields
        body = body.replaceAll("\\$\\{\\w*\\}", "");
        return body;
    }

    protected Map<String, String> sendEmail(String body, String subject, String to) {
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
            notificationLog.setBody(body);
            notificationLog.setSubject(subject);
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
    public void genericSend(EmailModel emailModel) {
        NotificationTemplate notificationTemplate = notificationTemplateRepository.findById(emailModel.notificationTemplateId)
                .orElseThrow(NotFoundException::new);
        notificationLog = new NotificationLog();
        notificationLog.setNotificationTemplate(notificationTemplate.getId());
        String body = (emailModel.sms) ? "" : notificationTemplate.getBody();
        String subject = notificationTemplate.getSubject();
        body = (emailModel.sms) ? "" : keywordReplacement(body, emailModel, null);
        subject = keywordReplacement(subject, emailModel, null);

        sendEmail(body, subject, emailModel.to);
    }

    @Override
    public void sendProcessExportDecision(Long notificationTemplateId, String to, Process process, User user, ProcessExportRequest processExportRequest) {
        NotificationTemplate notificationTemplate = notificationTemplateRepository.findById(notificationTemplateId).orElseThrow(NotFoundException::new);
        notificationLog = new NotificationLog();
        notificationLog.setNotificationTemplate(notificationTemplate.getId());
        String body = notificationTemplate.getBody();
        String subject = notificationTemplate.getSubject();
        EmailModel emailModel = new EmailModel(0l, "", false);
        emailModel.process = process;
        emailModel.user = user;
        body = keywordReplacement(body, emailModel, processExportRequest);
        subject = keywordReplacement(subject, emailModel, processExportRequest);
        sendEmail(body, subject, to);
    }

}
