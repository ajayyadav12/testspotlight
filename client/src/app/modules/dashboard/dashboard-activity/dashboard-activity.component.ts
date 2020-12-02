import { Component, OnInit, Input, Injectable, EventEmitter, Output } from '@angular/core';
import { ScheduleSubmissionsService } from '../../admin/schedule/schedule-submissions.service';
import { GENotes } from 'src/app/shared/Components/GENotes';
import { SessionService } from 'src/app/core/session/session.service';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { DateCommon } from 'src/app/shared/DateCommon';
import { MessageService, SelectItem } from 'primeng/api';
import { SidebarService } from 'src/app/core/sidebar/sidebar.service';
import { SubmissionsCalendarComponent } from '../../reports/submissions/submissions-calendar/submissions-calendar.component';
import { SubmissionCommon } from 'src/app/shared/SubmissionCommon';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-dashboard-activity',
  templateUrl: './dashboard-activity.component.html',
  styleUrls: ['./dashboard-activity.component.scss']
})
export class DashboardActivityComponent implements OnInit {
  @Input() isLightVersion: boolean;

  scheduledSubmissions;
  dataFound = true;
  displayEditDialog = false;
  displayAcknowledgmentDialog = false;
  acknowledgementData: GENotes;
  displayDisabledDialog = false;
  schedSubmissionID;
  disableNoteData: GENotes;
  loading = false;
  editScheduleId = 0;
  displayType = 0;
  display: boolean = false;
  displayTableDialog = false;
  submissionList: boolean = true;
  params;
  columns = [
    { header: 'ID', field: 'id', width: '4%' },
    { header: 'Process', field: 'processName', width: '12%' },
    { header: 'Planned Start', field: 'start', width: '9%' },
    { header: 'Planned End', field: 'end', width: '9%' }
  ];
  types: SelectItem[] = [
    { label: 'Table', value: 0, icon: 'pi pi-table' },
    { label: 'Calendar', value: 1, icon: 'pi pi-calendar' }
  ];
  upcomingSubmissionForm: FormGroup;
  // submissions = [];

  get menuItems() {
    if (this.sessionSvc.role !== 'user') {
      return [{ label: 'Acknowledge Delay' }, { label: 'Disable submission' }, { label: 'Edit submission' }];
    } else {
      return null;
    }
  }
  constructor(
    private sidebarSvc: SidebarService,
    private msgSvc: MessageService,
    private sessionSvc: SessionService,
    private fb: FormBuilder,
    private scheduleSubmissionsSvc: ScheduleSubmissionsService,
    private activatedRoute: ActivatedRoute
  ) {
    this.upcomingSubmissionForm = this.fb.group({
      startTime: [null, Validators.required],
      endTime: [null, Validators.required]
    });
  }

  ngAfterViewInit(): void {
    this.activatedRoute.queryParams.subscribe(params => {
      if (params.childId === '' || params.parentId === '' || (params.receiver === '' && params.sender === '')) {
        this.getScheduledSubmissions();
      } else if (params.parentId === undefined && params.childId === undefined && params.receiver === undefined) {
        this.getScheduledSubmissions();
      }
      else {
        this.getFilteredScheduleSubmission(params);
      }

    });
    const tab = localStorage.getItem('submission-tab');
    if (tab) {
      this.updateTitle({ value: 1 });
    }
  }

  getFilteredScheduleSubmission(params: any = { scheduledSubmissions: this.scheduledSubmissions }) {
    this.params = {
      childId: params.childId ? params.childId : '-1,',
      parentId: params.parentId ? params.parentId : '-1,',
      receiver: params.receiver ? params.receiver : '-1,',
      sender: params.sender ? params.sender : '-1,',
    };
    this.loading = true;
    this.scheduleSubmissionsSvc.getScheduleSubmissions(this.params).subscribe((value: any[]) => {
      this.dataFound = value.length > 0;
      if (!this.dataFound) {
        this.submissionList = false;
        return;
      }
      this.scheduledSubmissions = value;
      this.scheduledSubmissions.map(s => {
        this.scheduleSubmissionMapping(s);
      });
    });
    this.loading = false;
  }

  ngOnInit() {
    //this.getScheduledSubmissions();
    /*  const tab = localStorage.getItem('submission-tab');
     if (tab) {
       this.updateTitle({ value: 1 });
     } */
  }

  showDialog(event) {
    this.display = false;
  }

  updateTitle(event) {
    if (!this.isLightVersion) {
      this.displayType = Number.parseFloat(event.value);
      localStorage.setItem('submission-tab', event.value);
    }
  }

  onClickMenuOption(event) {
    const row = event.value;
    if (event.item === 'Disable submission') {
      this.disableNoteData = {
        id: row.id,
        note: row.disabledNote,
        flag: row.disabled,
        date: null
      };
      this.displayDisabledDialog = true;
    } else if (event.item === 'Acknowledge Delay') {
      this.displayAcknowledgmentDialog = true;
      this.acknowledgementData = {
        id: row.id,
        note: row.acknowledgementNote,
        flag: row.acknowledgementFlag,
        date: row.acknowledgementDate
      };
    } else if (event.item === 'Edit submission') {
      this.editScheduleId = row.id;
      const startTime = new Date(row.startTime);
      const endTime = new Date(row.endTime);
      DateCommon.convertFromEST(startTime);
      DateCommon.convertFromEST(endTime);
      this.upcomingSubmissionForm.setValue({ startTime: startTime, endTime: endTime });
      this.displayEditDialog = true;
      this.schedSubmissionID = row.id;
    }
  }

  getScheduledSubmissions() {
    const date1 = new Date(new Date().setDate(new Date().getDate())).toISOString().split('T')[0];
    const date2 = new Date(new Date().setDate(new Date().getDate() + 90)).toISOString().split('T')[0];
    this.loading = true;
    this.scheduleSubmissionsSvc.getExpectedSubmissions(date1, date2).subscribe((value: any[]) => {
      this.scheduledSubmissions = value;
      this.scheduledSubmissions.map(s => {
        this.scheduleSubmissionMapping(s);
      });
      this.submissionList = true;
      this.loading = false;
    });
  }

  scheduleSubmissionMapping(s) {
    const now = new Date();
    s.processName = s.process.name;
    s.start = new Date(s.startTime).toLocaleString();
    const delayedTime = new Date(s.startTime);
    delayedTime.setMinutes(delayedTime.getMinutes() + 15 + s.tolerance);
    s.end = new Date(s.endTime).toLocaleString();
    s.cursor = 'pointer';
    if (!s.disabled) {
      const isDelayed = !s.acknowledgementFlag && now.getTime() > delayedTime.getTime();
      s.backgroundColor = isDelayed ? '#f080805c' : null;
      s.tooltipValue = isDelayed ? 'Delayed' : null;
    }
    s.id = s.acknowledgementFlag ? '*' + s.id : s.id;
    s.tooltipValue = s.disabled ? 'Disabled' : s.tooltipValue;
    s.color = s.disabled ? 'lightsteelblue' : null;
  }

  setAcknowledgementFlag(noteValue) {
    this.scheduleSubmissionsSvc.setAcknowledgementFlag(this.acknowledgementData.id, noteValue).subscribe(value => {
      // Update submission
      const index = this.scheduledSubmissions.findIndex(x => {
        return x.id === this.acknowledgementData.id;
      });
      this.scheduledSubmissions[index] = value;
      this.scheduleSubmissionMapping(this.scheduledSubmissions[index]);
      this.displayAcknowledgmentDialog = false;
      this.acknowledgementData = null;
      this.msgSvc.add({
        severity: 'success',
        summary: 'Acknowledge flag set!',
        detail: `Now everybody knows you took care of the issue. Good job!`
      });
    });
  }

  updateUpcomingSubmission() {
    let startTime: Date = this.upcomingSubmissionForm.value.startTime;
    let endTime: Date = this.upcomingSubmissionForm.value.endTime;

    DateCommon.convertToEST(startTime);
    DateCommon.convertToEST(endTime);
    this.scheduleSubmissionsSvc
      .updateUpcomingSubmission(this.schedSubmissionID, startTime, endTime)
      .subscribe(value => {
        const index = this.scheduledSubmissions.findIndex(x => {
          return x.id === value.id;
        });
        this.scheduleSubmissionMapping(value);
        this.scheduledSubmissions[index] = value;
        this.displayEditDialog = false;
        this.msgSvc.add({
          severity: 'success',
          summary: 'Single ocurrence updated!',
          detail: `All is set. Good luck!`
        });
      });
  }

  onSubmitDisabledNote(noteValue) {
    this.scheduleSubmissionsSvc.setDisable(this.disableNoteData.id, noteValue).subscribe(
      value => {
        const index = this.scheduledSubmissions.findIndex(x => {
          return x.id === this.disableNoteData.id;
        });
        this.scheduledSubmissions[index] = value;
        this.scheduleSubmissionMapping(this.scheduledSubmissions[index]);
        this.disableNoteData = null;
        this.displayDisabledDialog = false;
        this.msgSvc.add({
          severity: 'success',
          summary: 'Disabled flag set!',
          detail: `You make the world a better place. Good job!`
        });
      },
      err => {
        this.displayDisabledDialog = false;
      }
    );
  }

  openTableDialog(scheduledSubmissions) {
    this.displayTableDialog = true;
  }




}
