package com.ge.finance.spotlight.models;

import javax.persistence.*;

@Entity
@Table(name = "T_NOTIFICATION_TEMPLATE")
@NamedQuery(name = "NotificationTemplate.findAll", query = "select n from NotificationTemplate n order by n.id")
public class NotificationTemplate {

    public final static long BY_STEP_STATUS = 1;
    public final static long BY_SUBMISSION_STATUS = 2;
    public final static long DELAYED_SUBMISSION = 3;
    public final static long ESCALATION_FAILED = 999;
    public final static long ESCALATION_DELAYED = 998;
    public final static long ESCALATION_FAILED_ACK = 994;
    public final static long LONG_RUN_SUBMISSION = 997;
    public final static long LONG_RUN_SUBMISSION_STEP_DELAYED = 996;
    public final static long LONG_RUN_ADHOC_SUBMISSION = 995;
    public final static long PROCESS_APPROVAL = 900;
    public final static long PROCESS_APPROVAL_RESULT = 901;
    public final static long MANUAL_CLOSE = 902;
    public final static long PROCESS_SUMMARY = 888;
    public final static long MISSING_NOTIFICATION = 777;
    public final static long REQUIRED_STEP_NOTIFICATION = 111;
    public final static long SUBMISSION_STEP_INCOMPLETE = 222;
    public final static long DISABLED_SUBMISSION = 801;
    public final static long EDITED_SUBMISSION = 802;
    public final static long ACKNOWLEDGED_SUBMISSION = 803;
    public final static long PROCESS_EXPORT_REQUEST = 1100;
    public final static long PROCESS_EXPORT_DECISION = 1101;
    public final static long PROCESS_IMPORTED = 1102;
    public final static long WARNING_MANUAL_UPLOAD = 1103;

    @Id
    private Long id;
    private String name;
    private String subject;
    private String body;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

}
