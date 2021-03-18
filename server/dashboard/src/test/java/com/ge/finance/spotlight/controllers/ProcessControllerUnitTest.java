package com.ge.finance.spotlight.controllers;

import com.ge.finance.spotlight.dto.ProcessApprovalDTO;
import com.ge.finance.spotlight.exceptions.ConflictException;
import com.ge.finance.spotlight.exceptions.ForbiddenException;
import com.ge.finance.spotlight.exceptions.NotFoundException;
import com.ge.finance.spotlight.models.*;
import com.ge.finance.spotlight.models.Process;
import com.ge.finance.spotlight.repositories.*;
import com.ge.finance.spotlight.requests.ProcessCopyRequest;
import com.ge.finance.spotlight.requests.ProcessExportNotesRequest;
import com.ge.finance.spotlight.requests.ProcessExportRequestRequest;
import com.ge.finance.spotlight.responses.ProcessExport;
import com.ge.finance.spotlight.services.ProcessImportService;
import com.ge.finance.spotlight.services.ScheduledSubmissionService;
import com.ge.finance.spotlight.services.SpotlightEmailService;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ProcessControllerUnitTest {

    Authentication authAdmin = new UsernamePasswordAuthenticationToken(1L, null,
            List.of(new SimpleGrantedAuthority("admin")));
    Authentication authApp = new UsernamePasswordAuthenticationToken(2L, null,
            List.of(new SimpleGrantedAuthority("application")));
    Authentication authUser = new UsernamePasswordAuthenticationToken(3L, null,
            List.of(new SimpleGrantedAuthority("user")));

    @Captor
    private ArgumentCaptor<ProcessExportRequest> processExportRequestArgumentCaptor;

    @Mock
    private ProcessRepository processRepository;
    @Mock
    private ProcessStepRepository processStepRepository;
    @Mock
    private ProcessUserRepository processUserRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private StatusRepository statusRepository;
    @Mock
    private ScheduleDefinitionRepository scheduleDefinitionRepository;
    @Mock
    private ScheduledSubmissionService scheduledSubmissionService;
    @Mock
    private ScheduledSubmissionRepository scheduledSubmissionRepository;
    @Mock
    private SpotlightEmailService spotlightEmailService;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private ProcessParentChildRepository processParentChildRepository;
    @Mock
    private SubmissionStepRepository submissionStepRepository;
    @Mock
    private ProcessExportRequestRepository processExportRequestRepository;
    @Mock
    private ProcessImportService processImportService;
    @Mock
    private NotificationLogRepository notificationLogRepository;

    @InjectMocks
    private ProcessController processController;

    @Test
    public void testGetProcessesForAdmin() {
        when(processRepository.findAll()).thenReturn(Collections.emptyList());

        assertNotNull(processController.index(authAdmin));
    }

    @Test
    public void testGetProcessesForNonAdmin() {
        User user = new User();
        user.setId(1L);
        ProcessUser pUser = new ProcessUser();
        pUser.setProcessId(1L);

        when(userRepository.findFirstBySso(anyLong())).thenReturn(user);
        when(processUserRepository.findByUserId(anyLong())).thenReturn(List.of(pUser));
        when(processRepository.findByIdIsIn(List.of(1L))).thenReturn(Collections.emptyList());

        assertNotNull(processController.index(authApp));
    }

    @Test
    public void testGetAssignableProcesses() {
        when(processRepository.findByIsParentAndProcessParentIdOrderByName('N', null))
                .thenReturn(Collections.emptyList());
        assertNotNull(processController.assignableProcesses(null));
    }

    @Test
    public void testGetAllProcesses() {
        when(processRepository.findAll()).thenReturn(Collections.emptyList());
        assertNotNull(processController.indexAll());
    }

    @Test(expected = RuntimeException.class)
    public void testCreateProcessExistingName() {
        Process process = new Process();
        process.setName("test");
        when(processRepository.existsByNameIgnoreCase(anyString())).thenReturn(true);
        assertNotNull(processController.create(process, authAdmin));
    }

    @Test
    public void testCreateProcessForAdmin() {
        Process process = new Process();
        process.setName("test");
        process.setIsParent(true);

        when(processRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
        when(processRepository.save(process)).thenReturn(process);

        process = processController.create(process, authAdmin);

        assertEquals('A', process.getApproved().charValue());
    }

    @Test
    public void testCreateProcessForNonAdmin() {
        User user = new User();
        user.setId(1L);
        Role role = new Role();
        role.setId(3L);
        user.setRole(role);

        Process process = new Process();
        process.setId(1L);
        process.setName("test");
        process.setApproved('0');
        process.setIsParent(true);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.findFirstBySso(anyLong())).thenReturn(user);
        when(processRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
        when(processRepository.save(process)).thenReturn(process);
        when(processRepository.existsById(anyLong())).thenReturn(true);
        when(processUserRepository.save(any())).thenReturn(new ProcessUser());

        process = processController.create(process, authApp);

        assertEquals('0', process.getApproved().charValue());
    }

    @Test
    public void testGetProcessForAdmin() {
        Process process = new Process();

        when(processRepository.findById(1L)).thenReturn(Optional.of(process));

        assertNotNull(processController.get(1L));
    }

    @Test(expected = NotFoundException.class)
    public void testGetNotExistingProcess() {
        when(processRepository.findById(1L)).thenReturn(Optional.empty());

        assertNotNull(processController.get(1L));
    }

    @Test
    public void testGetProcessForNonAdmin() {
        Process process = new Process();

        when(processRepository.findById(1L)).thenReturn(Optional.of(process));

        assertNotNull(processController.get(1L));
    }

    @Test()
    public void testGetChildren() {
        when(processRepository.findByProcessParentId(anyLong())).thenReturn(List.of());

        assertNotNull(processController.getChildren(1L));
    }

    @Test(expected = ForbiddenException.class)
    public void testUpdateNonProcessUser() {
        Process process = new Process();
        process.setId(1L);
        User user = new User();
        user.setId(1L);

        when(userRepository.findFirstBySso(anyLong())).thenReturn(user);

        assertNotNull(processController.update(new Process(), 1L, authApp));
    }

    @Test()
    public void testUpdate() {
        Process process = new Process();
        process.setId(1L);
        User user = new User();
        user.setId(1L);

        when(userRepository.findFirstBySso(anyLong())).thenReturn(user);
        when(processRepository.existsById(anyLong())).thenReturn(true);
        when(processUserRepository.findByUserIdAndProcessId(anyLong(), anyLong()))
                .thenReturn(List.of(new ProcessUser()));
        when(processRepository.save(process)).thenReturn(process);
        assertNotNull(processController.update(process, 1L, authApp));
    }

    /*
     * @Test(expected = ConflictException.class) public void
     * testDeleteWithConflict() { Process process = new Process();
     * process.setId(1L);
     * 
     * when(processRepository.findById(anyLong())).thenReturn(Optional.of(process));
     * when(processRepository.existsByProcessParentId(anyLong())).thenReturn(true);
     * 
     * assertNotNull(processController.delete(1L)); }
     */

    /*
     * @Test() public void testDelete() { Process process = new Process();
     * process.setId(1L);
     * 
     * NotificationLog notificationLog = new NotificationLog();
     * notificationLog.setProcessId(1L);
     * 
     * Notification notification = new Notification();
     * notification.setProcessId(1L);
     * 
     * ScheduleDefinition scheduleDefinition = new ScheduleDefinition();
     * scheduleDefinition.setProcess(process);
     * 
     * ScheduledSubmission scheduledSubmission = new ScheduledSubmission();
     * scheduledSubmission.setProcesId(1L);
     * 
     * ProcessParentChild processParentChild = new ProcessParentChild(); //
     * processParentChild.setProcess(new List<Process>);
     * 
     * when(processRepository.findById(anyLong())).thenReturn(Optional.of(process));
     * when(processRepository.existsByProcessParentId(anyLong())).thenReturn(false);
     * when(processStepRepository.existsByProcessId(anyLong())).thenReturn(false);
     * when(processUserRepository.existsByProcessId(anyLong())).thenReturn(false);
     * when(notificationLogRepository.findByProcessId(anyLong())).thenReturn(List.of
     * (notificationLog));
     * when(notificationRepository.findByProcessId(anyLong())).thenReturn(List.of(
     * notification));
     * when(processParentChildRepository.findByProcessIdOrderBySeqAscIdAsc(anyLong()
     * )) .thenReturn(List.of(processParentChild));
     * when(processParentChildRepository.findByChildId(anyLong())).thenReturn(List.
     * of(processParentChild));
     * when(scheduledSubmissionRepository.findByProcessId(anyLong())).thenReturn(
     * List.of(scheduledSubmission));
     * when(scheduleDefinitionRepository.findByProcessId(anyLong())).thenReturn(List
     * .of(scheduleDefinition)); //
     * when(submissionStepRepository.findBySubmissionIdOrderByIdAsc(anyLong())).
     * thenReturn(false); //
     * when(SubmissionRepository.findByProcessId(anyLong())).thenReturn(false);
     * assertNotNull(processController.delete(1L)); }
     */
    @Test
    public void testIndexStepsForAdmin() {
        when(processStepRepository.findByProcessIdAndAssociatedStepIdIsNull(anyLong())).thenReturn(List.of());

        assertNotNull(processController.indexSteps(1L));
    }

    @Test
    public void testIndexStepsForProcessUser() {
        when(processStepRepository.findByProcessIdAndAssociatedStepIdIsNull(anyLong())).thenReturn(List.of());

        assertNotNull(processController.indexSteps(1L));
    }

    @Test
    public void testIndexStepsAll() {
        when(processStepRepository.findByProcessId(anyLong())).thenReturn(List.of());

        assertNotNull(processController.indexStepsAll(1L));
    }

    @Test(expected = ForbiddenException.class)
    public void testApproveProcessNonAdmin() {
        ProcessApprovalDTO approval = new ProcessApprovalDTO();

        assertNotNull(processController.approveProcess(1L, approval, authApp));
    }

    @Test()
    public void testApproveProcessByAdminApproved() {

        User user = new User();
        user.setSso(1L);
        ProcessUser pu = new ProcessUser();
        pu.setUser(user);
        Process process = new Process();
        ProcessApprovalDTO approval = new ProcessApprovalDTO();
        approval.setCheck(true);

        when(processRepository.findById(anyLong())).thenReturn(Optional.of(process));
        when(processUserRepository.findByProcessId(anyLong())).thenReturn(List.of(pu));
        when(processRepository.save(any())).thenReturn(process);

        assertNotNull(processController.approveProcess(1L, approval, authAdmin));
        assertEquals('A', process.getApproved().charValue());
    }

    @Test()
    public void testApproveProcessByAdminRejected() {

        User user = new User();
        user.setSso(1L);
        ProcessUser pu = new ProcessUser();
        pu.setUser(user);
        Process process = new Process();
        ProcessApprovalDTO approval = new ProcessApprovalDTO();
        approval.setCheck(false);

        when(processRepository.findById(anyLong())).thenReturn(Optional.of(process));
        when(processUserRepository.findByProcessId(anyLong())).thenReturn(List.of(pu));
        when(processRepository.save(any())).thenReturn(process);

        assertNotNull(processController.approveProcess(1L, approval, authAdmin));
        assertEquals('N', process.getApproved().charValue());
    }

    @Test
    public void testGetExportRequestsAdmin() {
        when(processExportRequestRepository.findByProcessId(anyLong())).thenReturn(List.of(new ProcessExportRequest()));
        assertNotNull(processController.getExportRequests(1L, authAdmin));
    }

    @Test
    public void testGetExportRequestProcessUser() {
        User user = new User();
        user.setId(1L);
        user.setSso(2L);
        ProcessUser processUser = new ProcessUser();
        processUser.setUser(user);
        when(userRepository.findFirstBySso(anyLong())).thenReturn(user);
        when(processUserRepository.findByUserIdAndProcessId(anyLong(), anyLong())).thenReturn(List.of(processUser));
        when(processExportRequestRepository.findByProcessIdAndUserId(anyLong(), anyLong()))
                .thenReturn(List.of(new ProcessExportRequest()));
        assertNotNull(processController.getExportRequests(1L, authApp));
    }

    @Test(expected = ForbiddenException.class)
    public void testGetExportRequestUser() {
        User user = new User();
        user.setId(1L);
        user.setSso(3L);
        when(userRepository.findFirstBySso(anyLong())).thenReturn(user);
        when(processUserRepository.findByUserIdAndProcessId(anyLong(), anyLong())).thenReturn(Collections.emptyList());
        processController.getExportRequests(1L, authUser);
    }

    @Test
    public void testCreateExportRequestAdminIsAccepted() {
        Long processId = 1L;
        User user = new User();
        user.setId(1L);
        Process process = new Process();
        process.setId(processId);
        when(userRepository.findFirstBySso(anyLong())).thenReturn(user);
        when(processRepository.findById(anyLong())).thenReturn(Optional.of(process));
        ProcessExportRequestRequest exportRequest = new ProcessExportRequestRequest();
        exportRequest.setSettings(new String[] {});
        processController.createExportRequest(processId, exportRequest, authAdmin);
        verify(processExportRequestRepository).save(processExportRequestArgumentCaptor.capture());
        assertEquals(ProcessExportRequest.State.ACCEPTED, processExportRequestArgumentCaptor.getValue().getState());
    }

    @Test
    public void testCreateExportRequestProcessUserIsRequested() {
        Long processId = 1L;
        User user = new User();
        user.setId(1L);
        user.setSso(2L);
        Process process = new Process();
        process.setId(processId);
        ProcessUser processUser = new ProcessUser();
        processUser.setUser(user);
        when(userRepository.findFirstBySso(anyLong())).thenReturn(user);
        when(processRepository.findById(anyLong())).thenReturn(Optional.of(process));
        when(processUserRepository.findByUserIdAndProcessId(anyLong(), anyLong())).thenReturn(List.of(processUser));
        ProcessExportRequestRequest exportRequest = new ProcessExportRequestRequest();
        exportRequest.setSettings(new String[] {});
        processController.createExportRequest(processId, exportRequest, authApp);
        verify(processExportRequestRepository).save(processExportRequestArgumentCaptor.capture());
        assertEquals(ProcessExportRequest.State.REQUESTED, processExportRequestArgumentCaptor.getValue().getState());
    }

    @Test
    public void testAcceptExportRequest() {
        Long userId = 1L;
        Long processId = 1L;
        User user = new User();
        user.setId(userId);
        Process process = new Process();
        process.setId(processId);
        ProcessExportRequest processExportRequest = new ProcessExportRequest();
        processExportRequest.setUserId(userId);
        processExportRequest.setProcessId(processId);
        processExportRequest.setState(ProcessExportRequest.State.REQUESTED);
        when(processExportRequestRepository.findFirsByIdAndProcessId(anyLong(), anyLong()))
                .thenReturn(Optional.of(processExportRequest));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(processRepository.findById(anyLong())).thenReturn(Optional.of(process));
        ProcessExportNotesRequest request = new ProcessExportNotesRequest();
        processController.acceptExportRequest(processId, 1L, request);
        verify(processExportRequestRepository).save(processExportRequestArgumentCaptor.capture());
        verify(spotlightEmailService).sendProcessExportDecision(anyLong(), any(), any(), any(), any());
        assertEquals(ProcessExportRequest.State.ACCEPTED, processExportRequestArgumentCaptor.getValue().getState());
    }

    @Test(expected = ConflictException.class)
    public void testAcceptExportRequestConflict() {
        ProcessExportRequest processExportRequest = new ProcessExportRequest();
        processExportRequest.setState(ProcessExportRequest.State.ACCEPTED);
        when(processExportRequestRepository.findFirsByIdAndProcessId(anyLong(), anyLong()))
                .thenReturn(Optional.of(processExportRequest));
        ProcessExportNotesRequest request = new ProcessExportNotesRequest();
        processController.acceptExportRequest(1L, 1L, request);
    }

    @Test(expected = ForbiddenException.class)
    public void testProcessCopyUserForbidden() {
        User user = new User();
        user.setId(1L);
        user.setSso(3L);
        when(userRepository.findFirstBySso(anyLong())).thenReturn(user);
        when(processUserRepository.findByUserIdAndProcessId(anyLong(), anyLong())).thenReturn(Collections.emptyList());
        ProcessCopyRequest processCopyRequest = new ProcessCopyRequest();
        processController.copyProcess(1L, processCopyRequest, authUser);
    }

    @Test
    public void testProcessCopySummary() {
        Process process = new Process();
        when(processRepository.findById(1L)).thenReturn(Optional.of(process));
        ProcessCopyRequest processCopyRequest = new ProcessCopyRequest();
        processCopyRequest.setName("New process");
        processCopyRequest.setSettings(new String[] {});
        when(processImportService.importProcessWithName(any(Process.class), anyString())).thenReturn(new Process());
        Process newProcess = processController.copyProcess(1L, processCopyRequest, authAdmin);
        assertNotNull(newProcess);
        verify(processImportService).importProcessWithName(process, "New process");
    }

    @Test
    public void testProcessCopySummaryAndSteps() {
        Process process = new Process();
        process.setId(1L);
        when(processRepository.findById(1L)).thenReturn(Optional.of(process));
        when(processStepRepository.findByProcessId(1L)).thenReturn(List.of(new ProcessStep()));
        ProcessCopyRequest processCopyRequest = new ProcessCopyRequest();
        processCopyRequest.setName("New process");
        processCopyRequest.setSettings(new String[] { "steps" });
        Process newProcess = new Process();
        newProcess.setId(2L);
        when(processImportService.importProcessWithName(any(Process.class), anyString())).thenReturn(newProcess);
        when(processImportService.importProcessSteps(anyCollection(), anyLong()))
                .thenReturn(List.of(new ProcessStep()));
        assertNotNull(processController.copyProcess(1L, processCopyRequest, authAdmin));
        verify(processImportService).importProcessWithName(process, "New process");
        verify(processStepRepository).findByProcessId(1L);
        verify(processImportService).importProcessSteps(anyCollection(), anyLong());
    }

}
