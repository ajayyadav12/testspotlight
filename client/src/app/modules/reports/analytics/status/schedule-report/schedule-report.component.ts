import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { Validators, FormGroup, FormBuilder } from '@angular/forms';
import { AnalyticsService } from '../../analytics.service';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-schedule-report',
  templateUrl: './schedule-report.component.html',
  styleUrls: ['./schedule-report.component.scss']
})
export class ScheduleReportComponent implements OnInit {
  @Input() reportInfo;
  @Output() exit = new EventEmitter();

  scheduleForm: FormGroup;
  templates = [];

  today = new Date();
  customEndDate = new Date(this.today.getFullYear(), 11, 31, 23, 59);

  constructor(private fb: FormBuilder, private analyticsSvc: AnalyticsService, private msgSvc: MessageService) {
    this.setupForm();
  }

  ngOnInit() {}

  togglePeriod() {
    if (this.scheduleForm.value.defaultEndDate) {
      this.customEndDate = new Date(new Date(this.customEndDate).setFullYear(this.today.getFullYear()));
      this.scheduleForm.controls['endDate'].disable();
    } else {
      this.customEndDate = new Date(new Date(this.customEndDate).setFullYear(2019));
      this.scheduleForm.controls['endDate'].enable();
    }
  }

  onMonthOptionChanged() {
    const option = this.scheduleForm.get('monthlyRecurrence').get('option').value;
    this.disableEnableMonthControls(option);
  }

  disableEnableMonthControls(option) {
    const monthlyRecurrence = this.scheduleForm.get('monthlyRecurrence');
    switch (option) {
      case 'day':
        monthlyRecurrence.get('ocurrence').setValue(null);
        monthlyRecurrence.get('day').setValue(null);
        monthlyRecurrence.get('every').setValue(null);
        monthlyRecurrence.get('fromLCD').setValue(null);
        monthlyRecurrence.get('toLCD').setValue(null);
        monthlyRecurrence.get('monthsLCD').setValue(null);

        // Disable other option's fields
        monthlyRecurrence.get('ocurrence').disable();
        monthlyRecurrence.get('day').disable();
        monthlyRecurrence.get('every').disable();
        monthlyRecurrence.get('fromLCD').disable();
        monthlyRecurrence.get('toLCD').disable();
        monthlyRecurrence.get('monthsLCD').disable();

        // Enable option fields
        monthlyRecurrence.get('daysNumber').enable();
        monthlyRecurrence.get('monthsNumber').enable();

        // Default Values
        monthlyRecurrence.get('daysNumber').setValue(1);
        monthlyRecurrence.get('monthsNumber').setValue(1);
        break;
      case 'the':
        monthlyRecurrence.get('daysNumber').setValue(null);
        monthlyRecurrence.get('monthsNumber').setValue(null);
        monthlyRecurrence.get('fromLCD').setValue(null);
        monthlyRecurrence.get('toLCD').setValue(null);
        monthlyRecurrence.get('monthsLCD').setValue(null);

        // Disable other option's fields
        monthlyRecurrence.get('ocurrence').disable();
        monthlyRecurrence.get('daysNumber').disable();
        monthlyRecurrence.get('monthsNumber').disable();
        monthlyRecurrence.get('fromLCD').disable();
        monthlyRecurrence.get('toLCD').disable();
        monthlyRecurrence.get('monthsLCD').disable();

        // Enable option fields
        monthlyRecurrence.get('ocurrence').enable();
        monthlyRecurrence.get('day').enable();
        monthlyRecurrence.get('every').enable();

        // Default values
        monthlyRecurrence.get('ocurrence').setValue(1);
        monthlyRecurrence.get('day').setValue('Monday');
        monthlyRecurrence.get('every').setValue(1);
        break;
      case 'lcd':
        monthlyRecurrence.get('ocurrence').setValue(null);
        monthlyRecurrence.get('day').setValue(null);
        monthlyRecurrence.get('every').setValue(null);
        monthlyRecurrence.get('daysNumber').setValue(null);
        monthlyRecurrence.get('monthsNumber').setValue(null);

        // Disable other option's fields
        monthlyRecurrence.get('ocurrence').disable();
        monthlyRecurrence.get('day').disable();
        monthlyRecurrence.get('every').disable();
        monthlyRecurrence.get('ocurrence').disable();
        monthlyRecurrence.get('daysNumber').disable();
        monthlyRecurrence.get('monthsNumber').disable();
        monthlyRecurrence.get('fromLCD').disable();

        // Enable option fields
        monthlyRecurrence.get('fromLCD').enable();
        monthlyRecurrence.get('toLCD').enable();
        monthlyRecurrence.get('monthsLCD').enable();

        // Default values
        monthlyRecurrence.get('fromLCD').setValue(-5);
        monthlyRecurrence.get('toLCD').setValue(5);
        monthlyRecurrence.get('monthsLCD').setValue(1);
        break;
      default:
        break;
    }
    if (option === 'day') {
    } else {
    }
  }

  setupForm() {
    this.scheduleForm = this.fb.group({
      additionalEmails: null,
      startDate: new Date(this.today.getFullYear(), this.today.getMonth(), this.today.getDay(), 0, 0, 0),
      endDate: new Date(this.today.getFullYear() + 1, this.today.getMonth(), this.today.getDay(), 0, 0, 0),
      defaultEndDate: false,
      recurrencePattern: ['M'],
      monthlyRecurrence: this.fb.group({
        option: 'day',
        daysNumber: [null, [Validators.required, Validators.min(1), Validators.max(31)]],
        monthsNumber: [null, [Validators.required, Validators.min(1), Validators.max(12)]],
        ocurrence: null,
        day: null,
        every: [null, [Validators.min(1), Validators.max(12)]],
        fromLCD: null,
        toLCD: null,
        monthsLCD: [null, [Validators.min(1), Validators.max(12)]]
      })
    });
  }

  getSchedulePayload(): any {
    let payload;
    const sched = this.scheduleForm.getRawValue();

    payload = {
      submissionLevel: this.reportInfo.process.info.isParent ? 'P' : 'C', // char
      processId: this.reportInfo.process.info.id, // number
      rangeLength: this.reportInfo.report.rangeLength, // number (days)
      additionalEmails: sched.additionalEmails,
      startDate: new Date(new Date(this.today).setHours(0, 0)), // Date
      endDate: sched.defaultEndDate // Date
        ? new Date(this.today.getFullYear(), 11, 31, 23, 59)
        : new Date(new Date(sched.endDate).setHours(23, 59)),
      recurrencePattern: sched.recurrencePattern // char ('M')
    };

    let settings;
    switch (sched.recurrencePattern) {
      case 'M':
        settings = sched.monthlyRecurrence;
        break;
    }
    payload.settings = JSON.stringify(settings);
    return payload;
  }

  save() {
    const requestPayload = this.getSchedulePayload();
    this.analyticsSvc.newScheduledReport(requestPayload.processId, requestPayload).subscribe(value => {
      this.msgSvc.add({
        severity: 'success',
        summary: 'Oh, how exciting!',
        detail: `Your report has been scheduled.`
      });
      this.exit.emit(true);
    });
  }
}
