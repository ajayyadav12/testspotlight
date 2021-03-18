import { ScheduleSubmissionsService } from './../../schedule-submissions.service';
import { MessageService } from 'primeng/api';
import { GENotes } from 'src/app/shared/Components/GENotes';
import { ActivatedRoute, Router } from '@angular/router';
import { Component, OnInit, ViewChild } from '@angular/core';
import * as moment from 'moment-timezone';
import { GeUpdateScheduledSubmissionComponent } from 'src/app/shared/Components/ge-update-scheduled-submission/ge-update-scheduled-submission.component';

@Component({
  selector: 'app-schedule-dtl-submissions',
  templateUrl: './schedule-dtl-submissions.component.html',
  styleUrls: ['./schedule-dtl-submissions.component.scss']
})
export class ScheduleDtlSubmissionsComponent implements OnInit {
  @ViewChild('updateScheduledSubmission', { static: true })
  updateScheduledSubmissionComponent: GeUpdateScheduledSubmissionComponent;
  loading = false;
  scheduleDefId;

  displayDisabledDialog = false;
  disableNoteData: GENotes;
  editNoteData: GENotes;

  menuItems = [{ label: 'Edit Ocurrence' }, { label: 'Disable submission' }];

  upcomingSubmissions = [];
  columns = [
    { field: 'id', header: 'ID' },
    { field: 'startTime', header: 'Start Time(EST)' },
    { field: 'endTime', header: 'End Time(EST)' },
    { field: 'acknowledgementFlag', header: 'Acknowledged' },
    { field: 'disabled', header: 'Disabled' },
    { field: 'delayed', header: 'Delayed' }
  ];
  processType;
  uniqueId;

  constructor(
    private scheduleSubmissionsSvc: ScheduleSubmissionsService,
    private route: ActivatedRoute,
    private router: Router,
    private msgSvc: MessageService
  ) { }

  ngOnInit() {
    this.scheduleDefId = Number.parseInt(this.route.snapshot.paramMap.get('id'), 10);
    if (this.scheduleDefId !== 0) {
      this.getUpcomingSubmissions(this.scheduleDefId);
    }
    this.router.routeReuseStrategy.shouldReuseRoute = () => {
      return false;
    }

    this.route.queryParams.subscribe(params => {
      this.processType = params['delayed'];
      this.uniqueId = params['uniqueId'];
    });

  }

  getUpcomingSubmissions(scheduleDefId: number) {
    this.scheduleSubmissionsSvc.getUpcomingSubmissions(scheduleDefId).subscribe(value => {
      this.upcomingSubmissions = value;
      this.upcomingSubmissions.map(s => {
        this.upcomingSubmissionMapping(s);
      });
    });

  }


  moveToFirst(s, r) {
    r.splice(r.indexOf(s), 1);
    r.unshift(s);

  }

  onClickMenuOption(event) {
    const row = event.value;
    if (event.item === 'Disable submission') {
      this.disableNoteData = {
        id: row.id,
        note: row.disabledNote,
        flag: row.disabled,
        date: null,
        name: null
      };
      this.displayDisabledDialog = true;
    } else if (event.item === 'Edit Ocurrence') {
      const startTime = new Date(row.startTime);
      const endTime = new Date(row.endTime);
      this.editNoteData = {
        id: row.id,
        note: row.editNotes,
        flag: null,
        date: null,
        name: null
      };
      this.updateScheduledSubmissionComponent.setValue({ startTime: startTime, endTime: endTime, editNotes: this.editNoteData.note });
      this.updateScheduledSubmissionComponent.toggle();
      this.updateScheduledSubmissionComponent.schedSubmissionID = row.id;
    }
  }

  upcomingSubmissionMapping(scheduledSubmission) {
    if (this.uniqueId != null && this.uniqueId != 'undefined' && scheduledSubmission.id == this.uniqueId) {
      scheduledSubmission.backgroundColor = '#F080805C';
      this.moveToFirst(scheduledSubmission, this.upcomingSubmissions);

    }
    moment.tz.setDefault('America/New_York');

    let startMoment = moment(new Date(scheduledSubmission.startTime));
    let endMoment = moment(new Date(scheduledSubmission.endTime));

    scheduledSubmission.startTime = startMoment.tz('America/New_York').format('MM/DD/YY hh:mm a');
    scheduledSubmission.endTime = endMoment.tz('America/New_York').format('MM/DD/YY hh:mm a');

    scheduledSubmission.cursor = 'pointer';
  }

  onUpdateSubmission(value) {
    const index = this.upcomingSubmissions.findIndex(x => {
      return x.id === value.id;
    });
    this.upcomingSubmissions[index] = value;
    this.upcomingSubmissionMapping(this.upcomingSubmissions[index]);
    this.updateScheduledSubmissionComponent.toggle();
    this.msgSvc.add({
      severity: 'success',
      summary: 'Single ocurrence updated!',
      detail: `All is set. Good luck!`
    });
  }

  onSubmitDisabledNote(noteValue) {
    this.scheduleSubmissionsSvc.setDisable(this.disableNoteData.id, noteValue).subscribe(
      value => {
        const index = this.upcomingSubmissions.findIndex(x => {
          return x.id === this.disableNoteData.id;
        });
        this.upcomingSubmissions[index] = value;
        this.upcomingSubmissionMapping(this.upcomingSubmissions[index]);
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
}
