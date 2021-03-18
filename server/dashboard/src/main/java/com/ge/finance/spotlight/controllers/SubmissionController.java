package com.ge.finance.spotlight.controllers;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxFile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.ge.finance.spotlight.dto.AcknowledgeDTO;
import com.ge.finance.spotlight.dto.EmailModel;
import com.ge.finance.spotlight.exceptions.ConflictException;
import com.ge.finance.spotlight.exceptions.NotFoundException;
import com.ge.finance.spotlight.filters.FilterBuilder;
import com.ge.finance.spotlight.libs.AcknowledgeLib;
import com.ge.finance.spotlight.libs.BoxAPI;
import com.ge.finance.spotlight.models.FileSubmission;
import com.ge.finance.spotlight.models.NotificationTemplate;
import com.ge.finance.spotlight.models.Process;
import com.ge.finance.spotlight.models.ProcessUser;
import com.ge.finance.spotlight.models.Submission;
import com.ge.finance.spotlight.models.SubmissionStep;
import com.ge.finance.spotlight.models.User;
import com.ge.finance.spotlight.models.UserPermission;
import com.ge.finance.spotlight.repositories.FileSubmissionRepository;
import com.ge.finance.spotlight.repositories.ProcessRepository;
import com.ge.finance.spotlight.repositories.ProcessStepRepository;
import com.ge.finance.spotlight.repositories.ProcessUserRepository;
import com.ge.finance.spotlight.repositories.SubmissionRepository;
import com.ge.finance.spotlight.repositories.SubmissionStepRepository;
import com.ge.finance.spotlight.repositories.UserPermissionRepository;
import com.ge.finance.spotlight.repositories.UserRepository;
import com.ge.finance.spotlight.requests.FileSubmissionRequest;
import com.ge.finance.spotlight.requests.ManualSubmissionStepRequest;
import com.ge.finance.spotlight.requests.SubmissionStepRequest;
import com.ge.finance.spotlight.security.Constants;
import com.ge.finance.spotlight.services.SpotlightEmailService;
import com.ge.finance.spotlight.services.SubmissionStepService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/submissions")
public class SubmissionController {

    @Value("${app_secret}")
    private String app_secret;
    @Value("${uploaded_file_path}")
    private String uploadedFilePath;
    private SubmissionRepository submissionRepository;
    private SubmissionStepRepository submissionStepRepository;
    private ProcessRepository processRepository;
    private UserRepository userRepository;
    private ProcessUserRepository processUserRepository;
    private SpotlightEmailService spotlightEmailService;
    private SubmissionStepService submissionStepService;
    private ProcessStepRepository processStepRepository;
    private UserPermissionRepository userPermissionRepository;
    private FileSubmissionRepository fileSubmissionRepository;

    private List<String> allowedProcessFilters = Arrays.asList("sender", "receiver", "processType", "childId");
    private List<String> allowedSubmissionFilters = Arrays.asList("id", "from", "to", "status", "bu", "altId", "notes",
            "adHoc");
    private List<String> allowedPaginationFilters = Arrays.asList("size", "page", "sortField", "sortOrder");

    public SubmissionController(SubmissionRepository submissionRepository,
            SubmissionStepRepository submissionStepRepository, ProcessRepository processRepository,
            UserRepository userRepository, ProcessUserRepository processUserRepository,
            SpotlightEmailService spotlightEmailService, SubmissionStepService submissionStepService,
            ProcessStepRepository processStepRepository, UserPermissionRepository userPermissionRepository,
            FileSubmissionRepository fileSubmissionRepository) {
        this.submissionRepository = submissionRepository;
        this.submissionStepRepository = submissionStepRepository;
        this.processRepository = processRepository;
        this.userRepository = userRepository;
        this.processUserRepository = processUserRepository;
        this.spotlightEmailService = spotlightEmailService;
        this.submissionStepService = submissionStepService;
        this.processStepRepository = processStepRepository;
        this.userPermissionRepository = userPermissionRepository;
        this.fileSubmissionRepository = fileSubmissionRepository;
    }

    private boolean isProcessUser(Long sso, Long processId) {
        User user = userRepository.findFirstBySso(sso);
        List<ProcessUser> processUsers = processUserRepository.findByUserIdAndProcessId(user.getId(), processId);
        return !processUsers.isEmpty();
    }

    @GetMapping("/")
    public Page<Submission> index(@RequestParam Map<String, String> filters, Authentication authentication) {
        Pageable pageList;
        Page<Submission> submissionPageList = null;
        Map<String, String> processFilters = filters.entrySet().stream()
                .filter(entry -> allowedProcessFilters.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<String, String> submissionFilters = filters.entrySet().stream()
                .filter(entry -> allowedSubmissionFilters.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<String, String> paginationFilters = filters.entrySet().stream()
                .filter(entry -> allowedPaginationFilters.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        int page = Integer.valueOf(paginationFilters.get("page") == null ? "0" : paginationFilters.get("page"));
        int pageSize = Integer.valueOf(paginationFilters.get("size") == null ? "15" : paginationFilters.get("size"));
        String sortField = paginationFilters.get("sortField") == null ? "id" : paginationFilters.get("sortField");
        int sortOrder = Integer
                .valueOf(paginationFilters.get("sortOrder") == null ? "1" : paginationFilters.get("sortOrder"));

        pageList = (sortOrder != 1) ? PageRequest.of(page, pageSize, Sort.by(sortField).descending())
                : PageRequest.of(page, pageSize, Sort.by(sortField).ascending());

        if (processFilters.size() != 0) {
            FilterBuilder<Process> processFilterBuilder = new FilterBuilder<>(processFilters);

            String processIdListFiltered = processRepository.findAll(processFilterBuilder.build()).stream()
                    .map(p -> Long.toString(p.getId())).collect(Collectors.joining(","));

            processIdListFiltered = processIdListFiltered == "" ? "0" : processIdListFiltered;

            submissionFilters.put("process", processIdListFiltered);
        }
        if (filters.get("schedOnly") != null) {
            submissionFilters.put("scheduledSubmission", "true");
        }
        FilterBuilder<Submission> submissionFilterBuilder = new FilterBuilder<>(submissionFilters);
        submissionPageList = submissionRepository.findAll(submissionFilterBuilder.build(), pageList);
        if (submissionPageList.getSize() > 0) {
            for (Submission submissions : submissionPageList.getContent()) {
                Iterator it = submissions.getSteps().iterator();
                for (SubmissionStep allData : submissions.getSteps()) {
                    if (submissions.getStatus() != null
                            && submissions.getStatus().getName().equalsIgnoreCase("in progress")
                            && allData.getStatus().getName().equalsIgnoreCase("in progress")) {
                        submissions.setLatestStepname(allData.getProcessStep().getName());
                    }
                }
            }
        }
        return submissionPageList;
    }

    @PostMapping("/file")
    Collection<FileSubmission> createFileSubmission(@ModelAttribute FileSubmissionRequest fileSubmissionRequest,
            Authentication authentication) {
        Long sso = (Long) authentication.getPrincipal();
        User user = userRepository.findOptionalBySSO(sso).orElseThrow(NotFoundException::new);
        Process process = processRepository.findById(fileSubmissionRequest.getProcessId())
                .orElseThrow(NotFoundException::new);
        List<UserPermission> userPermissions = userPermissionRepository.findByUserIdAndReceiverIdAndSenderId(
                user.getId(), process.getReceiver().getId(), process.getSender().getId());
        Collection<FileSubmission> fileSubmissions = new ArrayList<>();
        for (UserPermission userPermission : userPermissions) {
            if (userPermission.getPermission().getPermission().compareTo("submit") == 0) {
                if (processStepRepository.existsByNameAndProcessId(process.getId(), "Manual upload for file")) {
                    for (MultipartFile file : fileSubmissionRequest.getFiles()) {
                        try {
                            // edit checks validation
                            FileSubmission fileSubmission = new FileSubmission();
                            String fileValidationError = validateFileEditChecks(file);
                            if ("".equals(fileValidationError)) {
                                String content = new String(file.getBytes());
                                // perform validation
                                SubmissionStepRequest submissionStepStartRequest = new SubmissionStepRequest();
                                submissionStepStartRequest.setProcessStepName("start");
                                submissionStepStartRequest.setAltSubmissionId(file.getOriginalFilename());
                                SubmissionStep submissionStepStart = submissionStepService.create(process.getId(),
                                        submissionStepStartRequest);
                                SubmissionStepRequest submissionStepFileRequest = new SubmissionStepRequest();
                                submissionStepFileRequest.setSubmissionId(submissionStepStart.getSubmissionId());
                                submissionStepFileRequest.setProcessStepName("Manual upload for file");
                                submissionStepService.create(process.getId(), submissionStepFileRequest);
                                fileSubmission.setFileContent(content);
                                fileSubmission.setSubmissionId(submissionStepStart.getSubmissionId());
                                // save upload
                            } else {
                                fileSubmission.setFileValidationError(fileValidationError);
                            }
                            fileSubmission.setName(fileSubmissionRequest.getName());
                            fileSubmission.setComments(fileSubmissionRequest.getComments());
                            fileSubmission.setSubmittedBy(user.getId());
                            fileSubmission.setSecret(UUID.randomUUID().toString());
                            fileSubmission.setFileName(file.getOriginalFilename());
                            fileSubmissions.add(fileSubmissionRepository.save(fileSubmission));

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    List<String> adminEmails = userRepository.findUserByRoleName("admin").stream().map(User::getEmail)
                            .collect(Collectors.toList());
                    String to = String.join(" ", adminEmails);
                    EmailModel emailModel = new EmailModel(NotificationTemplate.WARNING_MANUAL_UPLOAD, to, false);
                    emailModel.process = process;
                    spotlightEmailService.genericSend(emailModel);
                }
                break;
            }
        }
        return fileSubmissions;

    }

    String validateFileEditChecks(MultipartFile multipartFile) {
        List<String> fileRowsList = new ArrayList<>();
        BufferedReader bufferReader = null;
        File file = null;
        String[] fields = null;
        String fileValidationError = "";
        try {
            String line = "";
            file = new File(uploadedFilePath + multipartFile.getOriginalFilename());
            if (file != null && file.exists()) {
                file.delete();
            }
            Path path = Paths.get(uploadedFilePath);
            Files.copy(multipartFile.getInputStream(), path.resolve(multipartFile.getOriginalFilename()));
            bufferReader = new BufferedReader(new FileReader(uploadedFilePath + multipartFile.getOriginalFilename()));

            while ((line = bufferReader.readLine()) != null) {
                if (!line.equals("")) {
                    fileRowsList.add(line);
                }
            }

            if (fileRowsList.isEmpty()) {
                fileValidationError = Constants.FILE_EMPTY;
            } else {
                int counter = 0;
                boolean isValidColumns = true;
                for (int i = 0; i < fileRowsList.size() - 1; i++) {
                    fields = fileRowsList.get(i).split(",");
                    if (fields.length < Constants.MIN_COLUMNS_LIMIT || fields.length > Constants.MAX_COLUMNS_LIMIT) {
                        counter = i + 1;
                        if (!"".equals(fileValidationError)) {
                            fileValidationError = fileValidationError + ", " + Constants.FILE_COLUMN_COUNT + counter;
                        } else {
                            fileValidationError = fileValidationError + Constants.FILE_COLUMN_COUNT + counter;
                        }
                        isValidColumns = false;
                        break;
                    }
                }
                if (isValidColumns) {
                    String[] trailerRow = fileRowsList.get(fileRowsList.size() - 1).split(",");
                    if (!Constants.TRAILER_RECORD.equals(trailerRow[Constants.TRAILER_POSITION])) {
                        fileValidationError = fileValidationError + Constants.FILE_TRAILOR_RECORD;
                    }

                    if (trailerRow[Constants.TRAILER_COUNT_POSITION] != null
                            && !trailerRow[Constants.TRAILER_COUNT_POSITION].equals("" + (fileRowsList.size() - 1))) {
                        if (!"".equals(fileValidationError)) {
                            fileValidationError = fileValidationError + Constants.ERROR_SEPERATOR
                                    + Constants.FILE_TRAILOR_COUNT;
                        } else {
                            fileValidationError = fileValidationError + Constants.FILE_TRAILOR_COUNT;
                        }

                    }

                    if (trailerRow[Constants.TRAILER_DATE_POSITION] != null
                            && trailerRow[Constants.TRAILER_DATE_POSITION].length() != 8) {
                        if (!"".equals(fileValidationError)) {
                            fileValidationError = fileValidationError + Constants.ERROR_SEPERATOR
                                    + Constants.FILE_TRAILOR_DATE;
                        } else {
                            fileValidationError = fileValidationError + Constants.FILE_TRAILOR_DATE;
                        }
                    }
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferReader != null) {
                    bufferReader.close();
                }

                if (file != null && file.exists()) {
                    file.delete();
                } else {
                    fileValidationError = Constants.FILE_UPLOAD_ERROR;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileValidationError;

    }

    @GetMapping("/{submissionId}")
    public Submission getSubmission(@PathVariable(value = "submissionId") Long submissionId) {
        return submissionRepository.findById(submissionId).orElseThrow(NotFoundException::new);
    }

    @GetMapping("/{submissionId}/steps")
    public List<SubmissionStep> indexSteps(@PathVariable(value = "submissionId") Long submissionId) {
        return submissionStepRepository.findBySubmissionIdOrderByStartTimeAsc(submissionId);
    }

    @PostMapping("/{submissionId}/acknowledgement")
    public Submission setAcknowledgementFlag(@PathVariable(value = "submissionId") Long submissionId,
            @RequestBody AcknowledgeDTO acknowledgeDTO, Authentication authentication) {
        // Validate user is the app owner or member of team
        Long sso = (Long) authentication.getPrincipal();
        Submission submission = submissionRepository.findById(submissionId).get();

        if (isProcessUser(sso, submission.getProcess().getId())
                || AcknowledgeLib.IsAllowedToAcknowledge(submission.getProcess(), sso)) {
            updateSubmissionAcknowledgementDetails(submission, acknowledgeDTO.getAcknowledgementNote());
            submission.setAcknowledgedBy(acknowledgeDTO.getAcknowledgedBy());
            return submissionRepository.save(submission);
        } else {
            throw new RuntimeException("Only Application Owner and team can acknowledge a failure.");
        }
    }

    private void updateSubmissionAcknowledgementDetails(Submission submission, String notes) {
        submission.setAcknowledgementFlag(true);
        submission.setAcknowledgementDate(new Date());
        submission.setAcknowledgementNote(notes);
    }

    @PostMapping("/{submissionId}/manual-close")
    public Submission manualClose(@PathVariable(value = "submissionId") Long submissionId,
            @RequestBody ManualSubmissionStepRequest manualSubmissionStepRequest, Authentication authentication)
            throws JsonMappingException, JsonProcessingException {
        Long sso = (Long) authentication.getPrincipal();

        if (isProcessUser(sso, manualSubmissionStepRequest.getProcessId())) {
            SubmissionStepRequest submissionStepRequest = new SubmissionStepRequest();
            submissionStepRequest.setSubmissionId(submissionId);
            submissionStepRequest.setStatus(manualSubmissionStepRequest.getStatus());
            submissionStepRequest.setSubmissionNotes(manualSubmissionStepRequest.getNotes());
            submissionStepRequest.setStepNotes(manualSubmissionStepRequest.getNotes());
            submissionStepRequest.setProcessStepName("end");
            this.submissionStepService.create(manualSubmissionStepRequest.getProcessId(), submissionStepRequest);

            Submission submission = submissionRepository.findById(submissionId).orElseThrow(NotFoundException::new);
            if (manualSubmissionStepRequest.getStatus().equalsIgnoreCase("failed")) {
                updateSubmissionAcknowledgementDetails(submission, manualSubmissionStepRequest.getNotes());
                submission = submissionRepository.save(submission);
            }

            // Send email
            User appOwner = submission.getProcess().getAppOwner();
            if (appOwner != null) {
                EmailModel emailModel = new EmailModel(NotificationTemplate.MANUAL_CLOSE, appOwner.getEmail(), false);
                emailModel.submission = submission;
                this.spotlightEmailService.genericSend(emailModel);
            }
            return submission;

        } else {
            throw new RuntimeException("Only Application Owner and team can manually close a submission.");
        }
    }

    @GetMapping("/submission-count")
    List<?> getSubmissionStatusCount(@RequestParam Map<String, String> filters, Authentication authentication) {
        Integer days = Integer.parseInt(filters.get("days"));
        int[] processList = Arrays.stream(filters.get("childId").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] parentList = Arrays.stream(filters.get("parentId").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] senderList = Arrays.stream(filters.get("sender").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] receiverList = Arrays.stream(filters.get("receiver").split(",")).mapToInt(Integer::parseInt).toArray();
        String bu = filters.get("bu");
        String adHoc = filters.get("adHoc");
        return submissionRepository.findSubmissionStatusCount(days, processList, parentList, senderList, receiverList,
                bu, adHoc);
    }

    @GetMapping("/submission-count/{status}")
    List<?> getSubmissionStatusCountByProcess(@RequestParam Map<String, String> filters,
            @PathVariable(value = "status") String status, Authentication authentication) {
        Integer statusId = 0;
        switch (status.toLowerCase()) {
            case "failed":
                statusId = 4;
                break;
            case "warning":
                statusId = 3;
                break;
            case "long":
                statusId = 997;
                break;
            case "delayed":
                statusId = 998;
                break;
            default:
                throw new RuntimeException("Status doesn't exists.");
        }

        Integer days = Integer.parseInt(filters.get("days"));
        int[] processList = Arrays.stream(filters.get("childId").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] parentList = Arrays.stream(filters.get("parentId").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] senderList = Arrays.stream(filters.get("sender").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] receiverList = Arrays.stream(filters.get("receiver").split(",")).mapToInt(Integer::parseInt).toArray();
        String bu = filters.get("bu");
        String adHoc = filters.get("adHoc");

        // Different query depending if from t_submission or t_notification_log
        if (statusId == 4 || statusId == 3) {
            return submissionRepository.findSubmissionStatusPerProcessByDate(statusId, days, processList, parentList,
                    senderList, receiverList, bu, adHoc);
        } else {
            return submissionRepository.findSubmissionStatusPerProcessByDate2(statusId, days, processList, parentList,
                    senderList, receiverList, bu, adHoc);
        }

    }

    @GetMapping("/submission-drill-in-progress")
    List<?> getSubmissionInProgresList(@RequestParam Map<String, String> filters, Authentication authentication) {

        Integer days = Integer.parseInt(filters.get("days") != null ? filters.get("days") : "7");
        int[] processList = Arrays.stream(filters.get("childId").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] parentList = Arrays.stream(filters.get("parentId").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] senderList = Arrays.stream(filters.get("sender").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] receiverList = Arrays.stream(filters.get("receiver").split(",")).mapToInt(Integer::parseInt).toArray();
        String bu = filters.get("bu");
        String adHoc = filters.get("adHoc");

        return submissionRepository.findSubmissionsInProgress(days, processList, parentList, senderList, receiverList,
                bu, adHoc);
    }

    @GetMapping("/submission-drill-failed")
    List<?> getSubmissionFailedList(@RequestParam Map<String, String> filters, Authentication authentication) {

        Integer days = Integer.parseInt(filters.get("days") != null ? filters.get("days") : "7");
        int[] processList = Arrays.stream(filters.get("childId").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] parentList = Arrays.stream(filters.get("parentId").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] senderList = Arrays.stream(filters.get("sender").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] receiverList = Arrays.stream(filters.get("receiver").split(",")).mapToInt(Integer::parseInt).toArray();
        String bu = filters.get("bu");
        String adHoc = filters.get("adHoc");
        return submissionRepository.findSubmissionsFailed(days, processList, parentList, senderList, receiverList, bu,
                adHoc);
    }

    @GetMapping("/submission-drill-delayed")
    List<?> getSubmissionDelayedList(@RequestParam Map<String, String> filters, Authentication authentication) {
        Integer days = Integer.parseInt(filters.get("days") != null ? filters.get("days") : "7");
        int[] processList = Arrays.stream(filters.get("childId").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] parentList = Arrays.stream(filters.get("parentId").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] senderList = Arrays.stream(filters.get("sender").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] receiverList = Arrays.stream(filters.get("receiver").split(",")).mapToInt(Integer::parseInt).toArray();
        // String bu = filters.get("bu");
        // String adHoc = filters.get("adHoc");

        return submissionRepository.findSubmissionsDelayed(days, processList, parentList, senderList, receiverList);
    }

    @GetMapping("/submission-drill-warning")
    List<?> getSubmissionWarningList(@RequestParam Map<String, String> filters, Authentication authentication) {
        Integer days = Integer.parseInt(filters.get("days") != null ? filters.get("days") : "7");
        int[] processList = Arrays.stream(filters.get("childId").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] parentList = Arrays.stream(filters.get("parentId").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] senderList = Arrays.stream(filters.get("sender").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] receiverList = Arrays.stream(filters.get("receiver").split(",")).mapToInt(Integer::parseInt).toArray();
        String bu = filters.get("bu");
        String adHoc = filters.get("adHoc");

        return submissionRepository.findSubmissionsWarning(days, processList, parentList, senderList, receiverList, bu,
                adHoc);
    }

    @GetMapping("/submission-drill-success")
    List<?> getSubmissionSuccessList(@RequestParam Map<String, String> filters, Authentication authentication) {
        Integer days = Integer.parseInt(filters.get("days") != null ? filters.get("days") : "7");
        int[] processList = Arrays.stream(filters.get("childId").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] parentList = Arrays.stream(filters.get("parentId").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] senderList = Arrays.stream(filters.get("sender").split(",")).mapToInt(Integer::parseInt).toArray();
        int[] receiverList = Arrays.stream(filters.get("receiver").split(",")).mapToInt(Integer::parseInt).toArray();
        String bu = filters.get("bu");
        String adHoc = filters.get("adHoc");

        return submissionRepository.findSubmissionsSuccess(days, processList, parentList, senderList, receiverList, bu,
                adHoc);
    }

    @GetMapping("/data-report-file/{submissionId}/{flag}")
    public List<String[]> getSubmissionReports(@PathVariable(value = "submissionId") Long submissionId,
            @PathVariable(value = "flag") String flag, HttpServletRequest request, Authentication authentication)
            throws NoSuchFieldException, SecurityException, ClassNotFoundException, IllegalArgumentException,
            IllegalAccessException, NoSuchAlgorithmException, BoxAPIException, IOException {

        List<String[]> records = null;
        BoxFile boxFile = null;
        long count = 0;
        Long sso = (Long) authentication.getPrincipal();
        Collection<UserPermission> userPermissions = userPermissionRepository.findBySSOAndPermission(sso, "view");
        if (userPermissions.isEmpty()) {
            throw new ConflictException(
                    "Sorry ! But you don't have the permission to view this file.Please contact administrator.");
        } else {
            for (UserPermission userPermission : userPermissions) {
                if (userPermission.getPermission().getPermission().compareTo("view") == 0) {
                    Submission submission = submissionRepository.findById(submissionId)
                            .orElseThrow(NotFoundException::new);
                    BoxAPI api = new BoxAPI();
                     if (submission.getDataFile() != null && flag.equalsIgnoreCase("D")) {
                        boxFile = api.getCSVData(submission.getDataFile(), request);
                    } else if (submission.getReportFile() != null && flag.equalsIgnoreCase("R")) {
                        boxFile = api.getCSVData(submission.getReportFile(), request);
                    }
                    BoxFile.Info info = boxFile.getInfo();
                    if (info.getName().contains(".csv")) {

                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        boxFile.download(byteArrayOutputStream);
                        // File dataFile = new File(info.getName());
                        records = new ArrayList<String[]>();
                        String[] OneRow;

                        BufferedReader brd = new BufferedReader(new StringReader(byteArrayOutputStream.toString()));

                        while (brd.ready()) {
                            String st = brd.readLine();
                            count++;
                            if (st != null) {
                                OneRow = st.split(",", -1);
                                records.add(OneRow);
                                System.out.println(Arrays.toString(OneRow));
                            } else {

                                System.out.println("reading ..." + st);
                                if (count > 10) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return records;
    }

    @GetMapping("/submission-current-status")
    List<?> getSubmissionCurrentStatus(@RequestParam Map<String, String> filters) {
        Long systemId = Long.parseLong(filters.get("systemId") != null ? filters.get("systemId") : "-1");

        return submissionRepository.findSubmissionsCurrentStatus(systemId);
    }

    @GetMapping("/getAverageValue/{submissionId}")
    public List<String> getAverageValue(@PathVariable(value = "submissionId") Long submissionId) {

        double averageRunTime = 0;
        double avgWarning = 0;
        double avgErrors = 0;
        double avgRecords = 0;
        DecimalFormat df = new DecimalFormat("0.00");
        List<String> avgList = new ArrayList();
        Date startTime = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.add(Calendar.MONTH, -6);
        startTime = calendar.getTime();
        long processId = submissionRepository.findProcessIdForSubmissionId(submissionId).orElse(0L);
        long totalSubmissionCount = submissionRepository.countByProcessIdAndStartTimeGreaterThan(processId, startTime)
                .orElse(0L);
        List<Double> totalRunTimeList = submissionRepository.findRunTimeForProcessId(processId, 2L, startTime);

        // startTime);
        averageRunTime = getAvgRunTime(totalRunTimeList);
        if (averageRunTime > 0) {
            avgList.add(df.format(averageRunTime));
        } else {
            avgList.add("" + 0.00);
        }
        long warningSubmissionCount = submissionRepository
                .countByProcessIdAndStatusIdAndStartTimeGreaterThan(processId, 3L, startTime).orElse(0L);
        if (warningSubmissionCount > 0) {
            avgWarning = (double) warningSubmissionCount / totalSubmissionCount;
            if (avgWarning > 0) {
                avgList.add(df.format(avgWarning));
            }
        } else {
            avgList.add("" + 0.00);
        }

        long errorsSubmissionCount = submissionRepository
                .countByProcessIdAndStatusIdAndStartTimeGreaterThan(processId, 4L, startTime).orElse(0L);
        if (errorsSubmissionCount > 0) {
            avgErrors = (double) errorsSubmissionCount / totalSubmissionCount;
            if (avgErrors > 0) {
                avgList.add(df.format(avgErrors));
            }
        } else {
            avgList.add("" + 0.00);
        }

        long totalRecords = submissionRepository.totalRecordsCountForProcessId(processId, startTime).orElse(0L);

        if (totalRecords > 0) {
            avgRecords = (double) totalRecords / totalSubmissionCount;
            if (avgRecords > 0) {
                avgList.add(df.format(avgRecords));
            }
        } else {
            avgList.add("" + 0.00);
        }

        return avgList;
    }

    double getAvgRunTime(List<Double> listSubmission) {
        double totalMinutes = 0;
        double averageRunTime = 0;
        if (!listSubmission.isEmpty()) {
            for (Double runTimeMinutes : listSubmission) {
                if (runTimeMinutes > 0) {
                    totalMinutes = totalMinutes + runTimeMinutes;
                }
            }
            averageRunTime = totalMinutes / listSubmission.size();
        }
        return averageRunTime;
    }
}
