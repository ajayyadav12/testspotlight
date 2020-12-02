import { DateCommon } from 'src/app/shared/DateCommon';
import { ScheduleSubmissionsService } from './../../schedule-submissions.service';
import { MessageService } from 'primeng/api';
import { GENotes } from 'src/app/shared/Components/GENotes';
import { ActivatedRoute } from '@angular/router';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-schedule-dtl-submissions',
  templateUrl: './schedule-dtl-submissions.component.html',
  styleUrls: ['./schedule-dtl-submissions.component.scss']
})
export class ScheduleDtlSubmissionsComponent implements OnInit {
  loading = false;
  scheduleDefId;

  displayDisabledDialog = false;
  displayEditDialog = false;
  disableNoteData: GENotes;

  menuItems = [{ label: 'Edit Ocurrence' }, { label: 'Disable submission' }];

  upcomingSubmissions = [];
  columns = [
    { field: 'id', header: 'ID' },
    { field: 'startTime', header: 'Start Time' },
    { field: 'endTime', header: 'End Time' },
    { field: 'acknowledgementFlag', header: 'Acknowledged' },
    { field: 'disabled', header: 'Disabled' }
  ];
  upcomingSubmissionForm: FormGroup;
  schedSubmissionID;
  editScheduleId = 0;

  constructor(
    private scheduleSubmissionsSvc: ScheduleSubmissionsService,
    private route: ActivatedRoute,
    private fb: FormBuilder,
    private msgSvc: MessageService
  ) {
    this.upcomingSubmissionForm = this.fb.group({
      startTime: [null, Validators.required],
      endTime: [null, Validators.required]
    });
  }

  ngOnInit() {
    this.scheduleDefId = Number.parseInt(this.route.snapshot.paramMap.get('id'), 10);
    if (this.scheduleDefId !== 0) {
      this.getUpcomingSubmissions(this.scheduleDefId);
    }
  }

  getUpcomingSubmissions(scheduleDefId: number) {
    this.scheduleSubmissionsSvc.getUpcomingSubmissions(scheduleDefId).subscribe(value => {
      this.upcomingSubmissions = value;
      this.upcomingSubmissions.map(s => {
        this.upcomingSubmissionMapping(s);
      });
    });
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
    } else if (event.item === 'Edit Ocurrence') {
      const startTime = new Date(row.startTime);
      const endTime = new Date(row.endTime);

      DateCommon.convertFromEST(startTime, false);
      DateCommon.convertFromEST(endTime, false);
      this.editScheduleId = row.id;
      this.upcomingSubmissionForm.setValue({ startTime: startTime, endTime: endTime });
      this.displayEditDialog = true;
      this.schedSubmissionID = row.id;
    }
  }

  upcomingSubmissionMapping(scheduledSubmission) {
    scheduledSubmission.startTime = new Date(scheduledSubmission.startTime);
    scheduledSubmission.endTime = new Date(scheduledSubmission.endTime);
    scheduledSubmission.cursor = 'pointer';
  }

  updateUpcomingSubmission() {
    const startTime = this.upcomingSubmissionForm.value.startTime;
    const endTime = this.upcomingSubmissionForm.value.endTime;

    DateCommon.convertToEST(startTime, false);
    DateCommon.convertToEST(endTime, false);
    this.scheduleSubmissionsSvc
      .updateUpcomingSubmission(this.schedSubmissionID, startTime, endTime)
      .subscribe(value => {
        const index = this.upcomingSubmissions.findIndex(x => {
          return x.id === value.id;
        });
        this.upcomingSubmissions[index] = value;
        this.upcomingSubmissionMapping(this.upcomingSubmissions[index]);
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
