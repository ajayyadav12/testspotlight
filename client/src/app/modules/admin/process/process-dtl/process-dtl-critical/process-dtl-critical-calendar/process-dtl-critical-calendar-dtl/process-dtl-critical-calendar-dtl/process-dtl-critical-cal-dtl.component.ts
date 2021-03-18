import { Component, OnInit } from '@angular/core';
import { ScheduleService } from '../../../../../../schedule/schedule.service';
import { FormGroup, FormControl, FormBuilder, Validators, AbstractControl, FormArray } from '@angular/forms';
import { AuditLogService } from 'src/app/core/services/audit-log.service';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import * as moment from 'moment';

@Component({
  selector: 'app-process-dtl-critical-calendar-dtl',
  templateUrl: './process-dtl-critical-cal-dtl.component.html',
  styleUrls: ['./process-dtl-critical-cal-dtl.component.scss']
})
export class ProcessDtlCriticalCalendarDtlComponent implements OnInit {
  duration = 30;
  processId;
  times = [
    { label: '5 minutes', value: 5 },
    { label: '10 minutes', value: 10 },
    { label: '15 minutes', value: 15 },
    { label: '30 minutes', value: 30 },
    { label: '45 minutes', value: 45 },
    { label: '1 Hour', value: 60 }
  ];

  scheduleMenu = [{ label: 'Summary' }];

  days = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];
  processes = [];
  //#endregion Dropdown data

  scheduleForm: FormGroup;
  incomingScheduleId;
  incomingEndDate = false;
  incomingRecurrence: any[];

  constructor(private fb: FormBuilder,
    private scheduleSvc: ScheduleService,
    private auditLogSvc: AuditLogService,
    private route: ActivatedRoute,
    private msgSvc: MessageService,
    private router: Router) {
      this.route.parent.params.subscribe(params => {
        this.processId = params['id'] || 0;
        this.incomingScheduleId = params['schedid'] || 0;
        this.setupForm();
      });
      this.auditLogSvc.newAuditLog('Critical Calendar').subscribe(value => { });
  }

  ngOnInit() {
    this.disableEnableMonthControls('day');

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
    const today = new Date();
    this.scheduleForm = this.fb.group({
      processId: [null, Validators.required],
      scheduleStartDate: [new Date(), Validators.required],
      scheduleEndDate: [null, Validators.required],
      recurrenceTime: 0,
      recurrencePattern: null,
      weeklyRecurrence: this.fb.group({
        recurEvery: [null, [Validators.required, Validators.max(6), Validators.min(1)]],
        days: this.fb.array([], [this.checkRequired()])
      }),
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
      }),
      customRecurrence: this.fb.array([], [this.minLengthArray(1), this.noDuplicatesArray()])
    });

    const daysArray = this.scheduleForm['controls'].weeklyRecurrence['controls'].days as FormArray;
    this.days.forEach(d => {
      daysArray.push(new FormControl(false));
    });

    this.setupFormValidation();
  }

  setupFormValidation() {

    this.scheduleForm
      .valueChanges.subscribe(value => {
        if (value) {
          this.scheduleForm
            .get('recurEvery')
            .disable();
        } else {
          this.scheduleForm
            .get('recurEvery')
            .enable();
        }
      });
  }



  /**
   * Cannot submit a custom schedule without date entries
   * @param min
   */
  minLengthArray(min: number) {
    return (c: AbstractControl): { [key: string]: any } => {
      if (c.value.length >= min) {
        return null;
      } else {
        return { minLengthArray: { valid: false } };
      }
    };
  }

  checkRequired() {
    return (c: AbstractControl): { [key: string]: any } => {
      const temp: boolean[] = c.value;
      if (temp.indexOf(true) > -1) {
        return null;
      } else {
        return { checkRequired: { valid: false } };
      }
    };
  }

  noDuplicatesArray() {
    return (c: AbstractControl): { [key: string]: any } => {
      const filtered = c.value.filter(function (x, i, a) {
        return a.indexOf(x) === i;
      });
      if (
        (this.scheduleForm && this.scheduleForm.value.recurrencePattern !== 'C') ||
        c.value.length === filtered.length
      ) {
        return null;
      } else {
        return { noDuplicatesArray: { valid: false } };
      }
    };
  }

  onClickRecurrencePattern() {
    const pattern = this.scheduleForm.value.recurrencePattern;
    const monthlyRecurrence = this.scheduleForm.get('monthlyRecurrence');
    const weeklyRecurrence = this.scheduleForm.get('weeklyRecurrence');
    switch (pattern) {

      case 'C':
        monthlyRecurrence.get('monthsNumber').setValidators(null);
        monthlyRecurrence.get('daysNumber').setValidators(null);
        monthlyRecurrence.get('monthsLCD').setValidators(null);
        monthlyRecurrence.get('every').setValidators(null);
        weeklyRecurrence.get('recurEvery').setValidators(null);
        weeklyRecurrence.get('days').setValidators(null);
        this.scheduleForm.get('customRecurrence').setValidators([this.minLengthArray(1), this.noDuplicatesArray()]);

        break;
      case 'W':
        monthlyRecurrence.get('monthsNumber').setValidators(null);
        monthlyRecurrence.get('daysNumber').setValidators(null);
        monthlyRecurrence.get('monthsLCD').setValidators(null);
        monthlyRecurrence.get('every').setValidators(null);
        weeklyRecurrence.get('recurEvery').setValidators([Validators.required, Validators.max(6), Validators.min(1)]);
        weeklyRecurrence.get('days').setValidators([this.checkRequired()]);
        this.scheduleForm.get('customRecurrence').setValidators(null);

        break;
      case 'M':
        monthlyRecurrence
          .get('monthsNumber')
          .setValidators([Validators.required, Validators.max(12), Validators.min(1)]);
        monthlyRecurrence.get('monthsLCD').setValidators([Validators.required, Validators.max(12), Validators.min(1)]);
        monthlyRecurrence.get('every').setValidators([Validators.required, Validators.max(12), Validators.min(1)]);
        monthlyRecurrence.get('daysNumber').setValidators([Validators.required, Validators.max(31), Validators.min(1)]);
        weeklyRecurrence.get('recurEvery').setValidators(null);
        weeklyRecurrence.get('days').setValidators(null);
        this.scheduleForm.get('customRecurrence').setValidators(null);

        break;
      default:
        break;
    }
  }

  onMonthOptionChanged() {
    const option = this.scheduleForm.get('monthlyRecurrence').get('option').value;
    this.disableEnableMonthControls(option);
  }

  onChangeDuration(event) {
    const startTime: Date = this.scheduleForm.get('startTime').value;
    const newTime = new Date(startTime.getTime() + event.value * 60000);
    this.scheduleForm.get('endTime').setValue(newTime);
  }


  onOneYearAheadEndDate(event) {
    if (event) {
      const oneYearAhead = new Date(this.scheduleForm.get('scheduleStartDate').value);
      oneYearAhead.setFullYear(oneYearAhead.getFullYear() + 1);
      this.scheduleForm.get('scheduleEndDate').setValue(oneYearAhead);
      this.scheduleForm.get('scheduleEndDate').disable();
    } else {
      this.scheduleForm.get('scheduleEndDate').enable();
      this.scheduleForm.get('scheduleEndDate').setValue(null);
    }
  }

  save() {
    const requestPayload = this.getSchedulePayload();
    if (this.incomingScheduleId !== 0) {
      this.updateSchedule(requestPayload);
    } else {
      this.newSchedule(requestPayload);
    }
  }

  newSchedule(requestPayload) {
    this.scheduleSvc.newCriticalSchedule(this.processId, requestPayload).subscribe(value => {
      this.msgSvc.add({
        severity: 'success',
        summary: 'Get Ready! New Critical Period!',
        detail: `New Schedule set`
      });
      this.router.navigate(['/process']);
    });
  }
  updateSchedule(requestPayload) {
    this.scheduleSvc
      .updateSchedule(this.scheduleForm.getRawValue().processId, this.incomingScheduleId, requestPayload)
      .subscribe(value => {
        this.msgSvc.add({
          severity: 'success',
          summary: 'All set!',
          detail: `Schedule was updated`
        });
        this.router.navigate(['/process']);
      });
  }

  getSchedule() {
    this.scheduleSvc.getSchedule(this.incomingScheduleId).subscribe(s => {
      s.processId = s.process.id;
      this.scheduleForm.get('processId').disable();

      s.scheduleStartDate = new Date(s.scheduleStartDate);
      s.scheduleEndDate = new Date(s.scheduleEndDate);

      if (s.scheduleEndDate < new Date(2000)) {
        this.scheduleForm.get('scheduleEndDate').disable();
        this.incomingEndDate = false;
      } else {
        this.incomingEndDate = true;
      }

      switch (s.recurrencePattern) {

        case 'W':
          const weeklySettings = JSON.parse(s.settings);
          s.weeklyRecurrence = {
            recurEvery: weeklySettings.recurEvery,
            days: weeklySettings.days
          };
          break;
        case 'M':
          const monthlySettings = JSON.parse(s.settings);
          s.monthlyRecurrence = {
            option: monthlySettings.option,
            daysNumber: monthlySettings.daysNumber,
            monthsNumber: monthlySettings.monthsNumber,
            ocurrence: monthlySettings.ocurrence,
            day: monthlySettings.day,
            every: monthlySettings.every,
            fromLCD: monthlySettings.fromLCD,
            toLCD: monthlySettings.toLCD,
            monthsLCD: monthlySettings.monthsLCD
          };
          break;
        case 'C':
          this.incomingRecurrence = JSON.parse(s.settings);

          // Convert incoming dates from backend
          this.incomingRecurrence.map(dateRecord => {
            const startTime = new Date(dateRecord.startTime);
            const endTime = new Date(dateRecord.endTime);

            dateRecord.startTime = startTime;
            dateRecord.endTime = endTime;
          });
          break;
      }
      this.scheduleForm.patchValue(s);
      this.onClickRecurrencePattern();
    });
  }


  getSchedulePayload(): any {
    let payload;
    const sched = this.scheduleForm.getRawValue();

    payload = {
      scheduleStartDate: sched.scheduleStartDate,
      scheduleEndDate: sched.scheduleEndDate,
      recurrencePattern: sched.recurrencePattern
    };

    let settings;
    switch (sched.recurrencePattern) {
      case 'W':
        settings = sched.weeklyRecurrence;
        break;
      case 'M':
        settings = sched.monthlyRecurrence;
        break;
      case 'C':
        settings = sched.customRecurrence;
        // Convert dates before sending
        settings.map(x => {
          x.startTime = moment(x.startTime).format();
          x.endTime = moment(x.endTime).format();
        });
        break;
    }
    payload.settings = JSON.stringify(settings);
    return payload;
  }

}
