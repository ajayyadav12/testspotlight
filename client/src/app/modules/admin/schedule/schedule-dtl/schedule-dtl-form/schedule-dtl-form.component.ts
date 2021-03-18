import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, FormControl, Validators, FormArray, AbstractControl } from '@angular/forms';
import { ProcessService } from '../../../process/process.service';
import { ScheduleService } from '../../schedule.service';
import { Router, ActivatedRoute } from '@angular/router';
import { MessageService } from 'primeng/api';
import { SidebarService } from 'src/app/core/sidebar/sidebar.service';
import { AuditLogService } from 'src/app/core/services/audit-log.service';
import * as moment from 'moment';

@Component({
	selector: 'app-schedule-dtl-form',
	templateUrl: './schedule-dtl-form.component.html',
	styleUrls: ['./schedule-dtl-form.component.scss'],
	providers: [ProcessService, AuditLogService],
})
export class ScheduleDtlFormComponent implements OnInit {
	//#region Dropdown data
	predecessorEndTime: boolean = false;
	duration = 30;
	recurrencePattern = 'W';
	times = [
		{ label: '5 minutes', value: 5 },
		{ label: '10 minutes', value: 10 },
		{ label: '15 minutes', value: 15 },
		{ label: '30 minutes', value: 30 },
		{ label: '45 minutes', value: 45 },
		{ label: '1 Hour', value: 60 },
	];

	maxDate: Date;

	days = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];
	processes = [];
	//#endregion Dropdown data

	scheduleForm: FormGroup;
	incomingScheduleId;
	incomingEndDate = false;
	incomingRecurrence: any[];
	predecessorDuration;
	extendSchedule: boolean = false;
	extendScheduleYear: boolean = false;
	hourlyRecurrance: boolean = false;
	uniqueId;

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
		this.incomingScheduleId = this.incomingScheduleId ? this.incomingScheduleId : 0;
		this.setupForm();
		this.setupCombos();
		this.auditLogSvc.newAuditLog('Schedule Submission').subscribe((value) => {});
	}

	ngOnInit() {
		this.sidebarSvc.title = 'Schedule';
		this.disableEnableMonthControls('day');
		this.disableEnableDailyControls('daily');
		this.setMaxDate(this.scheduleForm.value.scheduleStartDate);
	}

	setMaxDate(startTime) {
		this.maxDate = new Date();
		this.maxDate.setDate(startTime.getDate());
		this.maxDate.setMonth(startTime.getMonth() + 12);
		this.maxDate.setFullYear(startTime.getFullYear() + 1);
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
			dailyRecurrence.get('hourlyStartTime').disable();
			dailyRecurrence.get('hourlyEndTime').disable();
			dailyRecurrence.get('hourlyDuration').disable();
			dailyRecurrence.get('hourlyStartTime').setValue(null);
			dailyRecurrence.get('hourlyEndTime').setValue(null);
			dailyRecurrence.get('hourlyDuration').setValue(null);
			this.hourlyRecurrance = false;
		} else {
			dailyRecurrence.get('recurEvery').setValue(null);
			dailyRecurrence.get('isEveryWeekday').setValue(null);
			dailyRecurrence.get('recurEvery').disable();
			dailyRecurrence.get('isEveryWeekday').disable();
			dailyRecurrence.get('timeRecurrence').enable();
			dailyRecurrence.get('timeRecurrence').setValue(1);
			dailyRecurrence.get('hourlyStartTime').enable();
			dailyRecurrence.get('hourlyEndTime').enable();
			dailyRecurrence.get('hourlyDuration').enable();
			this.hourlyRecurrance = true;
		}
	}

	getSchedule() {
		this.scheduleSvc.getSchedule(this.incomingScheduleId).subscribe((s) => {
			s.processId = s.process.id;
			const settings = JSON.parse(s.settings);
			this.scheduleForm.get('processId').disable();
			if (s.recurrencePattern === 'E') {
				this.predecessorEndTime = true;
				this.predecessorDuration = s.tolerance;
				s.scheduleStartDate = new Date(s.scheduleStartDate);
				s.scheduleEndDate = new Date(s.scheduleEndDate);
				s.startTime = new Date(s.startTime);
				s.endTime = new Date(s.endTime);
				this.scheduleForm.patchValue(s);
			} else {
				s.scheduleStartDate = new Date(s.scheduleStartDate);
				s.scheduleEndDate = new Date(s.scheduleEndDate);
				this.setMaxDate(s.scheduleStartDate);

				var future = new Date();
				future.setDate(future.getDate() - 30);
				const msecPerDay = 1000 * 60 * 60 * 24;
				const msecBetween = s.scheduleEndDate.getTime() - future.getTime();
				const days = msecBetween / msecPerDay;
				const length = Math.abs(Math.floor(days));

				const daysDuration = Math.abs(
					Math.floor((s.scheduleEndDate.getTime() - s.scheduleStartDate.getTime()) / msecPerDay)
				);

				if (s.scheduleEndDate < new Date(2000)) {
					this.scheduleForm.get('scheduleEndDate').disable();
					this.incomingEndDate = false;
				} else {
					this.incomingEndDate = true;
				}
				this.extendSchedule = length <= 30 && (daysDuration === 365 || daysDuration === 366) ? true : false;

				if (settings.option !== 'hourly') {
					s.startTime = new Date(s.startTime);
					s.endTime = new Date(s.endTime);

					s.criticalDate = new Date(s.criticalDate);
					if (s.criticalDate < new Date(2000)) {
						s.criticalDate = null;
					}
				} else {
					s.startTime = null;
					s.endTime = null;
					s.criticalDate = null;
				}

				switch (s.recurrencePattern) {
					case 'D':
						const dailySettings = JSON.parse(s.settings);
						s.dailyRecurrence = {
							option: dailySettings.option,
							timeRecurrence: dailySettings.timeRecurrence,
							recurEvery: dailySettings.recurEvery,
							isEveryWeekday: dailySettings.isEveryWeekday,
							hourlyStartTime: new Date(dailySettings.hourlyStartTime),
							hourlyEndTime: new Date(dailySettings.hourlyEndTime),
							hourlyDuration: dailySettings.hourlyDuration,
						};
						break;
					case 'W':
						const weeklySettings = JSON.parse(s.settings);
						s.weeklyRecurrence = {
							recurEvery: weeklySettings.recurEvery,
							days: weeklySettings.days,
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
							monthsLCD: monthlySettings.monthsLCD,
						};
						break;
					case 'C':
						this.incomingRecurrence = JSON.parse(s.settings);

						// Convert incoming dates from backend
						this.incomingRecurrence.map((dateRecord) => {
							const startTime = new Date(dateRecord.startTime);
							const endTime = new Date(dateRecord.endTime);

							dateRecord.startTime = startTime;
							dateRecord.endTime = endTime;
						});
						break;
				}
				this.scheduleForm.patchValue(s);
				this.recurrencePattern = s.recurrencePattern;
				this.onClickRecurrencePattern();
			}
		});
	}

	onExtendSchedule(event) {
		this.extendScheduleYear = event ? true : false;
	}

	getSchedulePayload(): any {
		let payload;
		const sched = this.scheduleForm.getRawValue();
		var hourlyStart = new Date(sched.dailyRecurrence.hourlyStartTime);
		var hourlyEnd = new Date(sched.dailyRecurrence.hourlyEndTime);

		var scheduleStart = new Date(sched.scheduleStartDate);
		scheduleStart.setHours(hourlyStart.getHours());
		scheduleStart.setMinutes(hourlyStart.getMinutes());
		scheduleStart.setMilliseconds(hourlyStart.getMilliseconds());

		var scheduleEnd = new Date(sched.scheduleEndDate);
		scheduleEnd.setHours(hourlyEnd.getHours());
		scheduleEnd.setMinutes(hourlyEnd.getMinutes());
		scheduleEnd.setMilliseconds(hourlyEnd.getMilliseconds());

		var startTime = sched.dailyRecurrence.hourlyStartTime
			? scheduleStart
			: new Date(year + 1, month, day, hour, min, milisec);
		var endTime = sched.dailyRecurrence.hourlyEndTime
			? scheduleEnd
			: new Date(year + 1, month, day, hour, min, milisec);

		if (this.predecessorEndTime) {
			payload = {
				duration: this.predecessorDuration,
				isPredecessorEndTime: this.predecessorEndTime,
				recurrencePattern: 'E',
			};
		} else {
			if (this.extendScheduleYear) {
				var schedStartDate = sched.scheduleStartDate;
				var year = schedStartDate.getFullYear();
				var month = schedStartDate.getMonth();
				var day = schedStartDate.getDate();
				var startDate = new Date(year + 1, month, day);

				var schedEndDate = sched.scheduleEndDate;
				var year = schedEndDate.getFullYear();
				var month = schedEndDate.getMonth();
				var day = schedEndDate.getDate();
				var endDate = new Date(year + 1, month, day);

				var start = sched.startTime;
				var year = start.getFullYear();
				var month = start.getMonth();
				var day = start.getDate();
				var hour = start.getHours();
				var min = start.getMinutes();
				var milisec = start.getMilliseconds();
				var schedStartTime = new Date(year + 1, month, day, hour, min, milisec);

				var end = sched.endTime;
				var year = end.getFullYear();
				var month = end.getMonth();
				var day = end.getDate();
				var hour = end.getHours();
				var min = end.getMinutes();
				var milisec = end.getMilliseconds();
				var schedEndTime = new Date(year + 1, month, day, hour, min, milisec);
			}

			if (sched.dailyRecurrence.hourlyStartTime) {
				startTime = scheduleStart;
			} else if (this.extendScheduleYear) {
				startTime = schedStartTime;
			} else {
				startTime = sched.startTime;
			}

			if (sched.dailyRecurrence.hourlyEndTime) {
				endTime = scheduleEnd;
			} else if (this.extendScheduleYear) {
				endTime = schedEndTime;
			} else {
				endTime = sched.endTime;
			}

			payload = {
				startTime: moment(startTime).format(),
				endTime: moment(endTime).format(),
				scheduleStartDate: this.extendScheduleYear ? startDate : sched.scheduleStartDate,
				scheduleEndDate: this.extendScheduleYear ? endDate : sched.scheduleEndDate,
				tolerance: sched.tolerance,
				recurrencePattern: sched.recurrencePattern,
				criticalDate: sched.criticalDate,
				recurrenceTime: sched.recurrenceTime,
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
					settings.map((x) => {
						x.startTime = moment(x.startTime).format();
						x.endTime = moment(x.endTime).format();
					});
					break;
			}
			payload.settings = JSON.stringify(settings);
		}
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
		this.scheduleForm.get('recurrencePattern').setValue(this.recurrencePattern);
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
				if (this.scheduleForm.value.dailyRecurrence.option === 'hourly') {
					dailyRecurrence.get('hourlyStartTime').setValidators(Validators.required);
					dailyRecurrence.get('hourlyEndTime').setValidators(Validators.required);
					dailyRecurrence.get('hourlyDuration').setValidators(Validators.required);
					this.hourlyRecurrance = true;
				} else {
					this.hourlyRecurrance = false;
				}
				break;
			case 'C':
				monthlyRecurrence.get('monthsNumber').setValidators(null);
				monthlyRecurrence.get('daysNumber').setValidators(null);
				monthlyRecurrence.get('monthsLCD').setValidators(null);
				monthlyRecurrence.get('every').setValidators(null);
				weeklyRecurrence.get('recurEvery').setValidators(null);
				weeklyRecurrence.get('days').setValidators(null);
				this.scheduleForm
					.get('customRecurrence')
					.setValidators([this.minLengthArray(1), this.noDuplicatesArray()]);
				dailyRecurrence.get('recurEvery').setValidators(null);
				dailyRecurrence.get('hourlyStartTime').setValidators(null);
				dailyRecurrence.get('hourlyEndTime').setValidators(null);
				dailyRecurrence.get('hourlyDuration').setValidators(null);
				this.hourlyRecurrance = false;
				break;
			case 'W':
				monthlyRecurrence.get('monthsNumber').setValidators(null);
				monthlyRecurrence.get('daysNumber').setValidators(null);
				monthlyRecurrence.get('monthsLCD').setValidators(null);
				monthlyRecurrence.get('every').setValidators(null);
				weeklyRecurrence
					.get('recurEvery')
					.setValidators([Validators.required, Validators.max(6), Validators.min(1)]);
				weeklyRecurrence.get('days').setValidators([this.checkRequired()]);
				this.scheduleForm.get('customRecurrence').setValidators(null);
				dailyRecurrence.get('recurEvery').setValidators(null);
				dailyRecurrence.get('hourlyStartTime').setValidators(null);
				dailyRecurrence.get('hourlyEndTime').setValidators(null);
				dailyRecurrence.get('hourlyDuration').setValidators(null);
				this.hourlyRecurrance = false;
				break;
			case 'M':
				monthlyRecurrence
					.get('monthsNumber')
					.setValidators([Validators.required, Validators.max(12), Validators.min(1)]);
				monthlyRecurrence
					.get('monthsLCD')
					.setValidators([Validators.required, Validators.max(12), Validators.min(1)]);
				monthlyRecurrence
					.get('every')
					.setValidators([Validators.required, Validators.max(12), Validators.min(1)]);
				monthlyRecurrence
					.get('daysNumber')
					.setValidators([Validators.required, Validators.max(31), Validators.min(1)]);
				weeklyRecurrence.get('recurEvery').setValidators(null);
				weeklyRecurrence.get('days').setValidators(null);
				this.scheduleForm.get('customRecurrence').setValidators(null);
				dailyRecurrence.get('recurEvery').setValidators(null);
				dailyRecurrence.get('hourlyStartTime').setValidators(null);
				dailyRecurrence.get('hourlyEndTime').setValidators(null);
				dailyRecurrence.get('hourlyDuration').setValidators(null);
				this.hourlyRecurrance = false;
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
		if (event.checked) {
			const oneYearAhead = new Date(this.scheduleForm.get('scheduleStartDate').value);
			oneYearAhead.setFullYear(oneYearAhead.getFullYear() + 1);
			this.scheduleForm.get('scheduleEndDate').setValue(oneYearAhead);
			this.scheduleForm.get('scheduleEndDate').disable();
		} else {
			this.scheduleForm.get('scheduleEndDate').enable();
			this.scheduleForm.get('scheduleEndDate').setValue(null);
		}
	}

	onPredecessorEndTime(event) {
		this.predecessorEndTime = event.checked;
		if (event.checked) {
			this.scheduleForm.get('scheduleEndDate').disable();
			this.scheduleForm.get('scheduleEndDate').setValue(null);
			this.scheduleForm.get('scheduleStartDate').disable();
			this.scheduleForm.get('scheduleStartDate').setValue(null);
			this.scheduleForm.get('tolerance').disable();
			this.scheduleForm.get('criticalDate').disable();
			this.scheduleForm.get('criticalDate').setValue(null);
			this.scheduleForm.get('recurrencePattern').disable();
			this.scheduleForm.get('recurrencePattern').setValue(null);
			this.scheduleForm.get('noEndDate').disable();
			this.scheduleForm.get('noEndDate').setValue(null);
			this.recurrencePattern = null;
		} else {
			this.scheduleForm.get('scheduleEndDate').enable();
			this.scheduleForm.get('scheduleStartDate').enable();
			this.scheduleForm.get('tolerance').enable();
			this.scheduleForm.get('criticalDate').enable();
			this.scheduleForm.get('recurrencePattern').enable();
			this.scheduleForm.get('noEndDate').enable();
		}
	}

	save() {
		const requestPayload = this.getSchedulePayload();
		if (this.incomingScheduleId !== 0 && !this.extendScheduleYear) {
			this.updateSchedule(requestPayload);
		} else if (this.incomingScheduleId !== 0 && this.extendScheduleYear) {
			this.newSchedule(requestPayload);
		} else {
			this.newSchedule(requestPayload);
		}
	}

	newSchedule(requestPayload) {
		this.scheduleSvc.newSchedule(this.scheduleForm.get('processId').value, requestPayload).subscribe((value) => {
			this.msgSvc.add({
				severity: 'success',
				summary: 'Get Ready! New Schedule!',
				detail: `New Schedule set`,
			});
			this.router.navigate(['/schedule', value.id]);
		});
	}

	updateSchedule(requestPayload) {
		this.scheduleSvc
			.updateSchedule(this.scheduleForm.getRawValue().processId, this.incomingScheduleId, requestPayload)
			.subscribe((value) => {
				this.msgSvc.add({
					severity: 'success',
					summary: 'All set!',
					detail: `Schedule was updated`,
				});
				this.router.navigate(['/schedule']);
			});
	}

	setupForm() {
		const today = new Date();
		this.scheduleForm = this.fb.group({
			processId: [null, Validators.required],
			scheduleStartDate: [new Date(), Validators.required],
			scheduleEndDate: [this.maxDate, Validators.required],
			startTime: new Date(today.getFullYear(), today.getMonth(), today.getDate(), 12, 0),
			endTime: new Date(today.getFullYear(), today.getMonth(), today.getDate(), 12, 30),
			duration: null,
			tolerance: [0, Validators.min(0)],
			noEndDate: null,
			criticalDate: null,
			extendSchedule: null,
			predecessorEndTime: null,
			recurrenceTime: 0,
			recurrencePattern: null,
			dailyRecurrence: this.fb.group({
				recurEvery: null,
				isEveryWeekday: false,
				timeRecurrence: 1,
				option: 'daily',
				hourlyStartTime: Validators.required,
				hourlyEndTime: Validators.required,
				hourlyDuration: Validators.required,
			}),
			weeklyRecurrence: this.fb.group({
				recurEvery: [null, [Validators.required, Validators.max(6), Validators.min(1)]],
				days: this.fb.array([], [this.checkRequired()]),
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
				monthsLCD: [null, [Validators.min(1), Validators.max(12)]],
			}),
			customRecurrence: this.fb.array([], [this.minLengthArray(1), this.noDuplicatesArray()]),
		});

		const daysArray = this.scheduleForm['controls'].weeklyRecurrence['controls'].days as FormArray;
		this.days.forEach((d) => {
			daysArray.push(new FormControl(false));
		});
		this.onClickRecurrencePattern();
		this.setupFormValidation();
	}

	setupFormValidation() {
		this.scheduleForm.controls['endTime'].valueChanges.subscribe((value) => {
			const startTime = new Date(this.scheduleForm.get('startTime').value);
			const endTime = new Date(value);
			this.afterChangingEndTime(startTime, endTime);
		});

		this.scheduleForm.controls['startTime'].valueChanges.subscribe((value) => {
			const startTime = new Date(value);
			this.afterChangingStartTime(startTime);
		});

		this.scheduleForm
			.get('dailyRecurrence')
			.get('isEveryWeekday')
			.valueChanges.subscribe((value) => {
				if (this.scheduleForm.get('dailyRecurrence').get('option').value !== 'daily') {
					return;
				}
				if (value) {
					this.scheduleForm.get('dailyRecurrence').get('recurEvery').disable();
				} else {
					this.scheduleForm.get('dailyRecurrence').get('recurEvery').enable();
				}
			});

		// Disable start time and end time when Custom
		this.scheduleForm.controls['recurrencePattern'].valueChanges.subscribe((value) => {
			if (value === 'C') {
				this.scheduleForm.get('startTime').disable();
				this.scheduleForm.get('endTime').disable();
			} else {
				this.scheduleForm.get('startTime').enable();
				this.scheduleForm.get('endTime').enable();
			}
		});
	}

	afterChangingStartTime(startTime) {
		const newTime = new Date(startTime.getTime() + this.duration * 60000);
		this.scheduleForm.get('endTime').setValue(newTime);
	}

	afterChangingEndTime(startTime, endTime) {
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
			value.forEach((p) => {
				if (!p.isParent) {
					this.processes.push({ label: p.name, value: p.id });
				}
			});
			if (this.incomingScheduleId !== 0) {
				this.getSchedule();
			} else {
				this.scheduleForm.patchValue({
					processId: Number.parseInt(localStorage.getItem('scheduleProcessId')),
					recurrencePattern: 'W',
				});
			}
			this.onClickRecurrencePattern();
		});
	}
}
