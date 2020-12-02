import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { SidebarService } from 'src/app/core/sidebar/sidebar.service';
import { DateCommon } from 'src/app/shared/DateCommon';
import { ProcessService } from '../../process/process.service';
import { ScheduleService } from '../schedule.service';
import { AuditLogService } from 'src/app/core/services/audit-log.service';

const TODAY = new Date();
@Component({
  selector: 'app-schedule-dtl',
  templateUrl: './schedule-dtl.component.html',
  styleUrls: ['./schedule-dtl.component.scss'],
  providers: [ProcessService, AuditLogService]
})
export class ScheduleDtlComponent implements OnInit {
  //#region Dropdown data
  duration;
  times = [
    { label: '5 minutes', value: 5 },
    { label: '10 minutes', value: 10 },
    { label: '15 minutes', value: 15 },
    { label: '30 minutes', value: 30 },
    { label: '45 minutes', value: 45 },
    { label: '1 Hour', value: 60 }
  ];

  scheduleMenu = [{ label: 'Summary' }, { label: 'Upcoming submissions' }];

  days = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];
  processes = [];
  //#endregion Dropdown data
  get isDST(): boolean {
    return DateCommon.isDST();
  }

  scheduleForm: FormGroup;
  incomingScheduleId;
  incomingEndDate = false;
  incomingRecurrence: any[];

  constructor(
    private fb: FormBuilder,
    private processSvc: ProcessService,
    private scheduleSvc: ScheduleService,
    private router: Router,
    private route: ActivatedRoute,
    private msgSvc: MessageService,
    private sidebarSvc: SidebarService,
    private auditLogSvc: AuditLogService
  ) {
    this.incomingScheduleId = Number.parseInt(this.route.snapshot.paramMap.get('id'), 10);
    this.setupForm();
    this.setupCombos();
    this.auditLogSvc.newAuditLog('Schedule Submission').subscribe(value => { });
  }

  ngOnInit() {
    this.sidebarSvc.title = 'Schedule';
    this.disableEnableMonthControls('day');
    this.disableEnableDailyControls('daily');
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

  disableEnableDailyControls(option) {
    const dailyRecurrence = this.scheduleForm.get('dailyRecurrence');
    if (option === 'daily') {
      dailyRecurrence.get('timeRecurrence').setValue(null);
      dailyRecurrence.get('timeRecurrence').disable();
      dailyRecurrence.get('recurEvery').enable();
      dailyRecurrence.get('isEveryWeekday').enable();
    } else {
      dailyRecurrence.get('recurEvery').setValue(null);
      dailyRecurrence.get('isEveryWeekday').setValue(null);
      dailyRecurrence.get('recurEvery').disable();
      dailyRecurrence.get('isEveryWeekday').disable();
      dailyRecurrence.get('timeRecurrence').enable();
    }
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

      s.startTime = new Date(s.startTime);
      s.endTime = new Date(s.endTime);
      DateCommon.convertFromEST(s.startTime);
      DateCommon.convertFromEST(s.endTime);
      s.criticalDate = new Date(s.criticalDate);
      if (s.criticalDate < new Date(2000)) {
        s.criticalDate = null;
      }

      switch (s.recurrencePattern) {
        case 'D':
          const dailySettings = JSON.parse(s.settings);
          s.dailyRecurrence = {
            option: dailySettings.option,
            timeRecurrence: dailySettings.timeRecurrence,
            recurEvery: dailySettings.recurEvery,
            isEveryWeekday: dailySettings.isEveryWeekday
          };
          break;
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

            DateCommon.convertFromEST(startTime, false);
            DateCommon.convertFromEST(endTime, false);

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

    DateCommon.convertToEST(sched.startTime);
    DateCommon.convertToEST(sched.endTime);

    payload = {
      startTime: sched.startTime,
      endTime: sched.endTime,
      scheduleStartDate: sched.scheduleStartDate,
      scheduleEndDate: sched.scheduleEndDate,
      tolerance: sched.tolerance,
      recurrencePattern: sched.recurrencePattern,
      criticalDate: sched.criticalDate,
      recurrenceTime: sched.recurrenceTime
    };

    let settings;
    switch (sched.recurrencePattern) {
      case 'D':
        settings = sched.dailyRecurrence;
        break;
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
          const startTime = x.startTime;
          const endTime = x.endTime;

          DateCommon.convertToEST(startTime, false);
          DateCommon.convertToEST(endTime, false);

          x.startTime = startTime;
          x.endTime = endTime;
        });
        break;
    }
    payload.settings = JSON.stringify(settings);
    return payload;
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
      let temp: boolean[] = c.value;
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
    const dailyRecurrence = this.scheduleForm.get('dailyRecurrence');
    switch (pattern) {
      case 'D':
        monthlyRecurrence.get('monthsNumber').setValidators(null);
        monthlyRecurrence.get('daysNumber').setValidators(null);
        monthlyRecurrence.get('monthsLCD').setValidators(null);
        monthlyRecurrence.get('every').setValidators(null);
        weeklyRecurrence.get('recurEvery').setValidators(null);
        weeklyRecurrence.get('days').setValidators(null);
        this.scheduleForm.get('customRecurrence').setValidators(null);
        dailyRecurrence.get('recurEvery').setValidators([Validators.required, Validators.min(1)]);
        break;
      case 'C':
        monthlyRecurrence.get('monthsNumber').setValidators(null);
        monthlyRecurrence.get('daysNumber').setValidators(null);
        monthlyRecurrence.get('monthsLCD').setValidators(null);
        monthlyRecurrence.get('every').setValidators(null);
        weeklyRecurrence.get('recurEvery').setValidators(null);
        weeklyRecurrence.get('days').setValidators(null);
        this.scheduleForm.get('customRecurrence').setValidators([this.minLengthArray(1), this.noDuplicatesArray()]);
        dailyRecurrence.get('recurEvery').setValidators(null);
        break;
      case 'W':
        monthlyRecurrence.get('monthsNumber').setValidators(null);
        monthlyRecurrence.get('daysNumber').setValidators(null);
        monthlyRecurrence.get('monthsLCD').setValidators(null);
        monthlyRecurrence.get('every').setValidators(null);
        weeklyRecurrence.get('recurEvery').setValidators([Validators.required, Validators.max(6), Validators.min(1)]);
        weeklyRecurrence.get('days').setValidators([this.checkRequired()]);
        this.scheduleForm.get('customRecurrence').setValidators(null);
        dailyRecurrence.get('recurEvery').setValidators(null);
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
        dailyRecurrence.get('recurEvery').setValidators(null);
        break;
      default:
        break;
    }
  }

  onDailyOptionChanged() {
    const option = this.scheduleForm.get('dailyRecurrence').get('option').value;
    this.disableEnableDailyControls(option);
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

  onChangeEndDate(event) {
    if (event < this.scheduleForm.get('criticalDate').value) {
      this.scheduleForm.get('criticalDate').setValue(null);
    }
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
    this.scheduleSvc.newSchedule(this.scheduleForm.get('processId').value, requestPayload).subscribe(value => {
      this.msgSvc.add({
        severity: 'success',
        summary: 'Get Ready! New Schedule!',
        detail: `New Schedule set`
      });
      this.router.navigate(['/schedule']);
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
        this.router.navigate(['/schedule']);
      });
  }

  setupForm() {
    const today = new Date();
    this.scheduleForm = this.fb.group({
      processId: [null, Validators.required],
      scheduleStartDate: [new Date(), Validators.required],
      scheduleEndDate: [null, Validators.required],
      startTime: new Date(today.getFullYear(), today.getMonth(), today.getDay(), 12, 0),
      endTime: new Date(today.getFullYear(), today.getMonth(), today.getDay(), 12, 30),
      tolerance: [0, Validators.min(0)],
      criticalDate: null,
      recurrenceTime: 0,
      recurrencePattern: null,
      dailyRecurrence: this.fb.group({
        recurEvery: null,
        isEveryWeekday: false,
        timeRecurrence: 1,
        option: 'daily'
      }),
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
    this.scheduleForm.controls['endTime'].valueChanges.subscribe(value => {
      const startTime = new Date(this.scheduleForm.get('startTime').value);
      const endTime = new Date(value);
      this.afterChangingTime(startTime, endTime);
    });

    this.scheduleForm.controls['startTime'].valueChanges.subscribe(value => {
      const startTime = new Date(value);
      const endTime = new Date(this.scheduleForm.get('endTime').value);
      this.afterChangingTime(startTime, endTime);
    });

    this.scheduleForm
      .get('dailyRecurrence')
      .get('isEveryWeekday')
      .valueChanges.subscribe(value => {
        if (this.scheduleForm.get('dailyRecurrence').get('option').value !== 'daily') return;
        if (value) {
          this.scheduleForm
            .get('dailyRecurrence')
            .get('recurEvery')
            .disable();
        } else {
          this.scheduleForm
            .get('dailyRecurrence')
            .get('recurEvery')
            .enable();
        }
      });

    // Disable start time and end time when Custom
    this.scheduleForm.controls['recurrencePattern'].valueChanges.subscribe(value => {
      if (value === 'C') {
        this.scheduleForm.get('startTime').disable();
        this.scheduleForm.get('endTime').disable();
      } else {
        this.scheduleForm.get('startTime').enable();
        this.scheduleForm.get('endTime').enable();
      }
    });
  }

  afterChangingTime(startTime, endTime) {
    const msec = endTime.getTime() - startTime.getTime();
    const mins = Math.floor(msec / 60000);
    if (this.times.length === 5) {
      this.times.push({ label: `${mins} minutes`, value: mins });
    } else {
      this.times[6] = { label: `${mins} minutes`, value: mins };
    }
    this.duration = mins;
  }

  setupCombos() {
    this.processSvc.getProcessList().subscribe((value: any[]) => {
      value.forEach(p => {
        if (!p.isParent) {
          this.processes.push({ label: p.name, value: p.id });
        }
      });
      if (this.incomingScheduleId !== 0) {
        this.getSchedule();
      } else {
        this.scheduleForm.patchValue({
          processId: Number.parseInt(localStorage.getItem('scheduleProcessId')),
          recurrencePattern: 'W'
        });
      }
      this.onClickRecurrencePattern();
    });
  }
}
