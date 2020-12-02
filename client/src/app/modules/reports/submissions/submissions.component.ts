import { Component, OnInit, OnDestroy, Input, ViewChild } from '@angular/core';
import { SelectItem, LazyLoadEvent, MessageService } from 'primeng/api';
import { SubmissionsService } from './submissions.service';
import { ActivatedRoute } from '@angular/router';
import { SidebarService } from 'src/app/core/sidebar/sidebar.service';
import { Subject } from 'rxjs';
import { DateCommon } from 'src/app/shared/DateCommon';
import { Table } from 'primeng/table';
import { GENotes } from 'src/app/shared/Components/GENotes';
import { SubmissionCommon } from 'src/app/shared/SubmissionCommon';
import { AuditLogService } from 'src/app/core/services/audit-log.service';

@Component({
  selector: 'app-submissions',
  templateUrl: './submissions.component.html',
  styleUrls: ['./submissions.component.scss'],
  providers: [AuditLogService]
})
export class SubmissionsComponent implements OnInit, OnDestroy {
  timing: number = localStorage.getItem('autorefresh-timing')
    ? Number.parseInt(localStorage.getItem('autorefresh-timing'))
    : 60;
  get areFiltersActive(): boolean {
    const temp = this.route.snapshot.queryParams;
    if (
      temp['from'] ||
      temp['to'] ||
      temp['childId'] ||
      temp['parentId'] ||
      temp['sender'] ||
      temp['receiver'] ||
      temp['status'] ||
      temp['bu'] ||
      temp['altId'] ||
      temp['adHoc']
    ) {
      return true;
    }
  }

  @Input() isLightVersion: boolean;
  types: SelectItem[] = [
    { label: 'Cards', value: 0, icon: 'pi pi-th-large' },
    { label: 'Table', value: 1, icon: 'pi pi-table' },
    { label: 'Calendar', value: 2, icon: 'pi pi-calendar' }
  ];
  displayType = 0;
  displayFilters = false;
  displayPopup = { value: false, submission: null };
  autoRefreshOn = true;
  autoRefreshHandler;
  clickRefresh = new Subject<any>();

  showIsolation = false;
  isolatedSubmission;

  processSteps = [];
  queryParams;
  lastUpdatedTime;
  submissions = [];

  // Table
  @ViewChild('dt', { static: true }) table: Table;
  totalRecords;
  size = 15;
  sortField;
  sortOrder;
  loading = false;

  displayAcknowledgmentDialog = false;
  displayManualSubmissionClosing = false;
  acknowledgementData: GENotes;

  displayNotesDialog = false;
  notesData: any;

  selectedSubmission: { submissionId: number; processId: number };

  tParams;

  get curPage() {
    return this.table ? this.table.first / this.size : 0;
  }

  constructor(
    private submissionsSvc: SubmissionsService,
    private route: ActivatedRoute,
    private sidebarSvc: SidebarService,
    private msgSvc: MessageService,
    private auditLogSvc: AuditLogService
  ) {
    this.sidebarSvc.title = 'Parent Submissions';

    this.route.queryParams.subscribe(params => {
      if (location.pathname !== '/dashboard' && params === {} && localStorage.getItem('submission-view') !== null) {
        return;
      }
      this.queryParams = params;
      if (this.table) this.table.reset();
      this.getSubmissions(false);
    });
    this.auditLogSvc.newAuditLog('Submission').subscribe(value => {});
  }

  ngOnInit() {
    if (this.isLightVersion) {
      this.displayType = 1;
      this.types[0].disabled = true;
    } else {
      const tab = localStorage.getItem('submission-tab');
      if (tab) {
        this.updateTitle({ value: tab });
      }
    }
    this.onchangeTiming({});
  }

  autoRefreshSetup() {
    this.autoRefreshHandler = setInterval(() => {
      if (this.curPage === 0) {
        this.getSubmissions(true);
      }
    }, 1000 * this.timing);
  }

  onchangeTiming(timing) {
    clearInterval(this.autoRefreshHandler);
    localStorage.setItem('autorefresh-timing', this.timing.toString());
    if (this.timing > 0) {
      this.autoRefreshOn = true;
      this.autoRefreshSetup();
    } else {
      this.autoRefreshOn = false;
    }
  }

  ngOnDestroy() {
    this.showIsolation = false;
    clearInterval(this.autoRefreshHandler);
  }

  updateTitle(event) {
    if (!this.isLightVersion) {
      this.displayType = Number.parseFloat(event.value);
      localStorage.setItem('submission-tab', event.value);
      if (this.displayType !== 0) {
        this.sidebarSvc.title = 'All Submissions';
      } else {
        this.sidebarSvc.title = 'Parent Submissions';
      }
    }
  }

  getSingleSubmission(index) {
    this.submissionsSvc.getSubmissions({ id: this.submissions[index].id }).subscribe((value: any) => {
      value.content.map(s => {
        this.submissions[index].color = 'lightgray';
        if (s.status) {
          this.submissions[index].status = s.status.name;
          this.submissions[index].color = this.submissionStatusColor(s.status);
        }

        this.submissions[index].end = new Date(s.endTime);
        this.submissions[index].elapsedTime = DateCommon.dateDifference(
          s.startTime,
          s.endTime ? s.endTime : new Date(),
          true
        );
        return;
      });
    });
  }

  getLatestStepName(steps: any[]): string {
    if (steps.length > 0) {
      return steps.sort((x, y) => {
        return y.id - x.id;
      })[0].processStep.name;
    } else {
      return '';
    }
  }

  getSubmissions(fromAutoRefresh) {
    this.tParams = this.getUpdatedSubmissionRequestParams();

    this.loading = true;
    this.submissionsSvc.getSubmissions(this.tParams).subscribe(
      (value: any) => {
        if (fromAutoRefresh && this.totalRecords === value.totalElements) {
          this.lastUpdatedTime = new Date();
          this.loading = false;
          return;
        }
        this.submissions = [];
        this.submissions = value.content;
        this.totalRecords = value.totalElements;
        this.submissions.map(s => {
          this.submissionsMapping(s);
        });
        this.displayFilters = false;
        this.displayPopup.value = false;
        this.displayPopup.submission = null;
        this.lastUpdatedTime = new Date();
        this.loading = false;

        if (!this.totalRecords) {
          this.msgSvc.add({
            severity: 'info',
            summary: 'No submissions found!',
            detail: 'Try a different filter or view.'
          });
        }
      },
      err => {
        this.lastUpdatedTime = new Date();
        this.loading = false;
      }
    );
  }

  /**
   * Add table data (size, page and sorting) to submission request query params
   */
  getUpdatedSubmissionRequestParams(): any {
    let tParams: any = {};
    Object.assign(tParams, this.queryParams);
    tParams.size = this.size;
    tParams.page = this.curPage;
    tParams.sortField = this.queryParams.sortField
      ? this.queryParams.sortField
      : this.sortField
      ? this.sortField
      : 'id';
    tParams.sortOrder = this.queryParams.sortOrder ? this.queryParams.sortOrder : this.sortOrder ? this.sortOrder : -1;
    return tParams;
  }

  onSubmissionClose(submission) {
    const index = this.submissions.findIndex(x => {
      return x.id === submission.id;
    });
    this.submissionsMapping(submission);
    this.submissions[index] = submission;
    this.msgSvc.add({
      severity: 'success',
      summary: 'Submission closed!',
      detail: `Good job! Continue keeping things clear`
    });
    this.displayManualSubmissionClosing = false;
  }

  openSubmissionDialog(submission) {
    if (this.isLightVersion) return;
    if (submission.status === 'failed') {
      this.acknowledgementData = {
        id: submission.id,
        note: submission.acknowledgementNote,
        flag: submission.acknowledgementFlag,
        date: submission.acknowledgementDate
      };
      this.displayAcknowledgmentDialog = true;
    } else if (!submission.endTime) {
      this.displayManualSubmissionClosing = true;
      this.selectedSubmission = { submissionId: submission.id, processId: submission.process.id };
    }
  }

  setAcknowledgementFlag(noteValue) {
    this.loading = true;
    this.submissionsSvc.setAcknowledgementFlag(this.acknowledgementData.id, noteValue).subscribe(value => {
      // Update submission
      const index = this.submissions.findIndex(x => {
        return x.id === value.id;
      });
      this.submissions[index].acknowledgementFlag = value.acknowledgementFlag;
      this.submissions[index].acknowledgementNote = value.acknowledgementNote;
      this.displayAcknowledgmentDialog = false;
      this.acknowledgementData = null;
      this.msgSvc.add({
        severity: 'success',
        summary: 'Acknowledge flag set!',
        detail: `Now everybody knows you took care of the issue. Good job!`
      });
      this.loading = false;
      this.displayAcknowledgmentDialog = false;
    });
  }

  openNotesDialog(submission) {
    this.notesData = submission.notes;
    this.displayNotesDialog = true;
  }

  openIsolationMode(submission) {
    this.isolatedSubmission = submission;
    this.showIsolation = true;
  }

  submissionStatusColor(status) {
    return SubmissionCommon.submissionStatusColor(status);
  }

  /**
   * submissionsMapping() maps retreived data for All Submissions table,
   * Calendar, and parent component dialog box
   */
  submissionsMapping(s) {
    s['process.name'] = s.process.name;
    s.title = s.process.name;
    s.color = 'lightgray';
    if (s.status) {
      s.status = s.status.name;
      s.color = this.submissionStatusColor(s.status);
    }
    s.start = new Date(s.startTime);
    s.end = new Date(s.endTime);
    s.elapsedTime = DateCommon.dateDifference(s.startTime, s.endTime ? s.endTime : new Date(), true);
  }

  /**
   * changeView is called to manage the dialog boxes between parent/child views
   * uses submission/value to manage which component to view/hide
   * in Card View: changeView opens dialog box to show step component of child submission
   * in Table View: changeView opens dialog box to show parent component of child submission
   **/
  changeView(submission?) {
    if (!submission) {
      this.displayPopup.value = false;
      this.displayPopup.submission = null;
    } else if (this.displayType === 0) {
      this.displayPopup.value = true;
      this.displayPopup.submission = submission.element;
    } else {
      this.submissionsSvc.getSubmissionParent(submission.parentId).subscribe(parent => {
        this.mapParentSubmission(parent);
        this.displayPopup.value = true;
        this.displayPopup.submission = parent;
      });
    }
  }

  mapParentSubmission(value) {
    value.title = value.process.name;
    value.status = value.status.name;
    value.start = new Date(value.startTime);
    value.end = new Date(value.endTime);
    value.elapsedTime = DateCommon.dateDifference(value.startTime, value.endTime ? value.endTime : new Date(), true);
    value.children.map(c => {
      c.title = c.process.name;
      c.status = c.status.name;
      c.color = this.submissionStatusColor(c.status);
      c.parentId = value.id;
      c.start = new Date(c.startTime);
      c.end = new Date(c.endTime);
      c.elapsedTime = DateCommon.dateDifference(c.startTime, c.endTime ? c.endTime : new Date(), true);
    });
    // future: sort by predecessor/successor
    if (value.children.length > 1) {
      value.children.sort((a, b) => {
        return a.id - b.id;
      });
    }
  }

  /**
   * LoadSubmissions is called when the table starts,
   * because we are doing the same in constructor we avoid it when sortField is undefined
   **/
  loadSubmissions(event: LazyLoadEvent) {
    if (this.sortField) {
      this.sortField = event.sortField ? event.sortField : 'id';
      this.sortOrder = event.sortField ? event.sortOrder : -1;
      this.getSubmissions(false);
    } else {
      this.sortField = 'id';
    }
  }

  onClickRefresh() {
    this.getSubmissions(false);
    this.clickRefresh.next(this.queryParams);
  }

  /**
   * refreshStatus() is called when a submission dropdown is opened
   * retrieves updated information for that submissions so that data matches
   **/
  refreshStatus(submission) {
    const updateIndex = this.submissions.findIndex(s => s.id.toString() === submission.id);
    this.getSingleSubmission(updateIndex);
  }
}
