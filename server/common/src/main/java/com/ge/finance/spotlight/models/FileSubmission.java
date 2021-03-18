package com.ge.finance.spotlight.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
@Table(name = "T_FILE_SUBMISSION")
public class FileSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "S_FILE_SUBMISSION_ID")
    @SequenceGenerator(name = "S_FILE_SUBMISSION_ID", sequenceName = "S_FILE_SUBMISSION_ID", allocationSize = 1)
    private Long id;
    private String name;
    private String comments;
    private Long submittedBy;
    private Long submissionId;
    @JsonIgnore
    private String secret;
    private String fileName;
    @Lob
    @JsonIgnore
    private String fileContent;
    @Column(name = "file_validation_errors")
    private String fileValidationError;

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

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Long getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(Long submittedBy) {
        this.submittedBy = submittedBy;
    }

    public Long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

    public String getFileValidationError() {
        return fileValidationError;
    }

    public void setFileValidationError(String fileValidationError) {
        this.fileValidationError = fileValidationError;
    }

}
