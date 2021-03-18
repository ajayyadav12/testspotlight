import { Component, OnInit } from '@angular/core';
import { Validators, FormGroup, FormBuilder } from '@angular/forms';
import { ProcessService } from '../../../admin/process/process.service';
import { AnalyticsService } from '../../analytics/analytics.service';
import { MessageService, MenuItem } from 'primeng/api';
import { DateCommon } from '../../../../shared/DateCommon';
import { SessionService } from 'src/app/core/session/session.service';
import { ActivatedRoute, Router } from '@angular/router';
import { AuditLogService } from 'src/app/core/services/audit-log.service';

@Component({
	selector: 'app-status',
	templateUrl: './status.component.html',
	styleUrls: ['./status.component.scss'],
	providers: [AuditLogService],
})
export class StatusComponent implements OnInit {
	searchBy = 'defaultPeriod';
	queryParams = {};
	reportScheduled = false;
	get isScheduleActive(): boolean {
		const temp = this.router.snapshot.queryParams;
		return this.queryParams !== {} && temp['submission_level'] && temp['process_id'] && temp['range_length']
			? true
			: false;
	}

	displayAllSchedules = false;
	processSelected: any;
	scheduleMenu: MenuItem[] = [
		{
			label: 'View Report',
			command: (event: any) => {
				this.displayAllSchedules = false;
			},
		},
		{
			label: 'See Schedules',
			command: (event: any) => {
				this.displayAllSchedules = true;
			},
		},
	];

	reportForm: FormGroup;

	levels = [
		{ label: 'Child Submissions', value: 'C' },
		{ label: 'Parent Submissions', value: 'P' },
	];
	processesToLoad = [];
	parentProcesses = [];
	childProcesses = [];
	hasProcessAccess;

	isPeriod: boolean = false;
	isDateRange: boolean = false;

	datePattern =
		'(Q[0-9][0-9]-[1-4])|(Y[0-9][0-9][0-9][0-9])|(M[0-9][0-9]-[0-1][0-9])|(((JAN)|(FEB)|(MAR)|(APR)|(MAY)|(JUN)|(JUL)|(AUG)|(SEP)|(OCT)|(NOV)|(DEC))-[2-2][0-9])';

	dayZero = new Date();
	today = new Date();

	get id() {
		return this.reportForm.value.process.id;
	}

	get isParent() {
		return this.info.process.info.isParent;
	}

	get period() {
		return this.reportForm.value.period;
	}

	get stats() {
		return this.info ? this.info.stats : null;
	}

	loading;
	private dataToLoad = [];
	private info;

	displayScheduleDialog = false;

	constructor(
		private fb: FormBuilder,
		private processSvc: ProcessService,
		private analyticsSvc: AnalyticsService,
		private sessionSvc: SessionService,
		private router: ActivatedRoute,
		private route: Router,
		private msgSvc: MessageService,
		private auditLogSvc: AuditLogService
	) {
		this.hasProcessAccess = this.sessionSvc.role === 'admin';
		this.auditLogSvc.newAuditLog('Status Report').subscribe((value) => { });

		this.reportForm = this.fb.group({
			level: ['', Validators.required],
			process: [null, Validators.required],
			bu: [null],
			searchBy: 'defaultPeriod',
			from: [{ value: null, disabled: true }, Validators.pattern(this.datePattern)],
			to: [{ value: null, disabled: true }, Validators.pattern(this.datePattern)],
			period: [{ value: null, disabled: !this.isScheduleActive }],
			//defaultPeriod: [{ value: !this.isScheduleActive }]
		});

		this.resetData();

		this.processSvc.getProcessList().subscribe((value) => {
			value.forEach((p) => {
				if (p.isParent) {
					this.parentProcesses.push({
						label: p.name,
						value: { id: p.id },
						data: p,
					});
					if (this.sessionSvc.processName !== null) {
						this.parentProcesses.forEach((p) => {
							if (p.value.id === this.sessionSvc.processName.id) {
								this.processSelected = p.value;
								this.getProcesses();
								this.reportForm.controls['level'].setValue('P');
							}
						});
					}
				} else {
					this.childProcesses.push({
						label: p.name,
						value: { id: p.id },
						data: p,
					});
					if (this.sessionSvc.processName !== null) {
						this.childProcesses.forEach((p) => {
							if (p.value.id === this.sessionSvc.processName.id) {
								this.processSelected = p.value;
								this.getProcesses();
								this.reportForm.controls['level'].setValue('C');
							}
						});
					}
				}
			});

			this.router.queryParams.subscribe((params) => {
				this.queryParams = params;
				if (this.isScheduleActive) {
					this.reportForm.disable();
					this.generateScheduledReport(params);
				}
			});
		});
	}

	ngOnInit() { }

	onChange(event) {
		if (event.value !== null) {
			this.sessionSvc.processName = event.value;
			this.prepareReport();
		}
	}

	getProcesses() {
		if (this.reportForm.controls['level'].value === 'C') {
			this.processesToLoad = this.childProcesses;
			//this.reportForm.controls['bu'].enable();
		} else {
			this.processesToLoad = this.parentProcesses;
			//this.reportForm.controls['bu'].disable();
			//this.reportForm.controls['bu'].setValue(null);
		}
	}

	/**
	 * Disable and re-enable varianceForm field for
	 * time period input when default time checkbox is selected
	 */
	/* togglePeriod() {
	if (this.reportForm.value.defaultPeriod) {
	  this.reportForm.controls['period'].disable();
	  this.reportForm.controls['from'].disable();
	  this.reportForm.controls['to'].disable();
	  this.reportForm.controls['searchBy'].disable();
	  this.reportForm.controls['period'].setValue(null);
	  this.reportForm.controls['from'].setValue(null);
	  this.reportForm.controls['to'].setValue(null);
	  this.reportForm.controls['searchBy'].setValue(null);
	  this.isPeriod = false;
	  this.isDateRange = false;
	} else {
	  this.reportForm.controls['period'].enable();
	  this.reportForm.controls['from'].enable();
	  this.reportForm.controls['to'].enable();
	  this.reportForm.controls['searchBy'].enable();
	}

  } */
	onChangeDateOption(value) {
		switch (value) {
			case 'dateRange':
				this.isDateRange = false;
				this.isPeriod = false;
				this.reportForm.controls['period'].enable();
				this.reportForm.controls['from'].disable();
				this.reportForm.controls['to'].disable();
				this.reportForm.controls['from'].setValue(null);
				this.reportForm.controls['to'].setValue(null);
				break;
			case 'datePeriod':
				this.isPeriod = true;
				this.isDateRange = false;
				this.reportForm.controls['from'].enable();
				this.reportForm.controls['to'].enable();
				this.reportForm.controls['period'].disable();
				this.reportForm.controls['period'].setValue(null);
				break;
			case 'defaultPeriod':
				this.isDateRange = false;
				this.isPeriod = false;
				this.reportForm.controls['period'].disable();
				this.reportForm.controls['from'].disable();
				this.reportForm.controls['to'].disable();
				this.reportForm.controls['from'].setValue(null);
				this.reportForm.controls['to'].setValue(null);
				this.reportForm.controls['period'].setValue(null);
				break;
		}
	}
	/* disablePeriod() {
		if (this.reportForm.controls['searchBy'].value === 'datePeriod') {
			this.isPeriod = true;
			this.isDateRange = false;
			this.reportForm.controls['from'].enable();
			this.reportForm.controls['to'].enable();
			this.reportForm.controls['period'].disable();
			this.reportForm.controls['period'].setValue(null);
		} else if (this.reportForm.controls['searchBy'].value === 'dateRange') {
			this.isDateRange = true;
			this.isPeriod = false;
			this.reportForm.controls['period'].enable();
			this.reportForm.controls['from'].disable();
			this.reportForm.controls['to'].disable();
			this.reportForm.controls['from'].setValue(null);
			this.reportForm.controls['to'].setValue(null);
		} else if (this.reportForm.controls['searchBy'].value === 'defaultPeriod') {
			this.isDateRange = false;
			this.isPeriod = false;
			this.reportForm.controls['period'].disable();
			this.reportForm.controls['from'].disable();
			this.reportForm.controls['to'].disable();
			this.reportForm.controls['from'].setValue(null);
			this.reportForm.controls['to'].setValue(null);
			this.reportForm.controls['period'].setValue(null);
		}
	} */
	/**
	 * Use route parameters to fill form and prepare report
	 */
	generateScheduledReport(params) {
		const level = params['submission_level'];
		const id = parseInt(params['process_id'], 10);
		const rangeLength = parseInt(params['range_length'], 10);
		const start = new Date(new Date(this.today).setDate(this.today.getDate() - rangeLength));
		const end = new Date(this.today);
		const bu = params['searchBy'];

		this.reportForm.patchValue({
			level: level,
		});
		this.getProcesses();
		this.reportForm.patchValue({
			process: { id: id },
			period: [start, end],
			defaultPeriod: 'defaultPeriod',
			bu: bu,
		});

		this.prepareReport();
	}

	/**
	 * Handle all compilation needed to
	 *    retrieve/map data, calculate statistics, and generate chart
	 * Start loading prompt
	 * Handle errors: incomplete dates in reportForm
	 */
	prepareReport() {
		if (
			this.reportForm.value.searchBy !== 'defaultPeriod' &&
			(!this.period || !this.period[1]) &&
			this.reportForm.value.from === null &&
			this.reportForm.value.to === null
		) {
			this.msgSvc.add({
				severity: 'info',
				summary: 'Slow down!',
				detail: `We need some dates.`,
			});
		} else {
			this.loading.report = true;

			this.processSvc.getProcessUsers(this.id).subscribe((users) => {
				this.hasProcessAccess = users.some((u) => u.user.sso === this.sessionSvc.sso)
					? true
					: this.hasProcessAccess;

				if (this.reportForm.value.searchBy === 'defaultPeriod') {
					this.info.report.rangeStart = new Date(
						new Date(new Date(this.today).setHours(0, 0, 0)).setDate(this.today.getDate() - 30)
					);
					this.info.report.rangeEnd = new Date(new Date(this.today).setHours(23, 59, 59));
				} else if (this.reportForm.value.searchBy === 'dateRange') {
					this.info.report.rangeStart = new Date(new Date(this.period[0]).setHours(0, 0, 0));
					this.info.report.rangeEnd = new Date(new Date(this.period[1]).setHours(23, 59, 59));
				} else {
					const fromDate = this.reportForm.value.from.toUpperCase();
					const toDate = this.reportForm.value.to.toUpperCase();
					let startDate;
					let endDate;
					let lastDay;

					if (fromDate && toDate !== null) {
						switch (fromDate.substring(0, 1)) {
							case 'Q':
								startDate = new Date(
									2000 + Number(fromDate.substring(1, 3)),
									Number(fromDate.substring(4, 5)) * 3 - 3,
									1
								);
								endDate = new Date(
									2000 + Number(toDate.substring(1, 3)),
									Number(toDate.substring(4, 5)) * 3,
									0
								);
								break;

							case 'Y':
								startDate = '1/1/' + fromDate.substring(1, 5);
								endDate = '12/31/' + toDate.substring(1, 5);

								break;
							case fromDate.substring(0, 1) === 'M' &&
								fromDate.substring(0, 3) !== 'MAR' &&
								fromDate.substring(0, 3) !== 'MAY':
								startDate = new Date(2000 + Number(fromDate.substring(1, 3)), fromDate.substring(4, 6));
								lastDay = new Date(2000 + Number(toDate.substring(1, 3)), toDate.substring(4, 6));
								endDate = new Date(lastDay.getFullYear(), lastDay.getMonth() + 1, 0);

								break;
							default:
								startDate = new Date('1-' + fromDate);
								lastDay = new Date('1-' + toDate);
								endDate = new Date(lastDay.getFullYear(), lastDay.getMonth() + 1, 0);
						}
						this.info.report.rangeStart = new Date(new Date(startDate).setHours(0, 0, 0));
						this.info.report.rangeEnd = new Date(new Date(endDate).setHours(23, 59, 59));
					}
				}

				// get date range difference in days (ignoring DST)
				const msecPerDay = 1000 * 60 * 60 * 24;
				const msecBetween = this.info.report.rangeEnd.getTime() - this.info.report.rangeStart.getTime();
				const days = msecBetween / msecPerDay;
				this.info.report.rangeLength = Math.abs(Math.floor(days));
				this.info.labels.rangeLength = this.info.report.rangeLength + ' days';

				this.info.report.dateGenerated = this.today;

				const found = this.mapProcess();

				if (this.info.report.rangeStart > this.info.report.rangeEnd) {
					this.msgSvc.add({
						severity: 'info',
						summary: 'Slow down!',
						detail: `Invalid Date Range.`,
					});
				} else {
					if (found) {
						this.getData();
					} else {
						this.msgSvc.add({
							severity: 'info',
							summary: 'Whoops! Testing?',
							detail: `That process doesn't exist.`,
						});
						this.loading.report = false;
					}
				}
			});
		}
	}

	/**
	 * Map selected process data from form
	 * To be used in export
	 */
	mapProcess() {
		let found = false;
		this.processesToLoad.forEach((p) => {
			if (p.value.id === this.reportForm.value.process.id) {
				found = true;
				this.info.process.info = p.data;
			}
		});
		return found;
	}

	/**
	 * Retrieve submissions from database using process.id and timePeriod (default or range)
	 * End loading prompt
	 * Handle Errors: no submissions, empty submissions
	 */
	getData() {
		const timePeriod = {
			default: this.reportForm.value.searchBy === 'defaultPeriod' ? true : false,
			from:
				this.reportForm.value.searchBy === 'dateRange' && this.reportForm.value.searchBy !== 'defaultPeriod'
					? this.period[0].toISOString().split('T')[0]
					: this.info.report.rangeStart.toISOString().split('T')[0],
			to:
				this.reportForm.value.searchBy === 'dateRange' && this.reportForm.value.searchBy !== 'defaultPeriod'
					? new Date(new Date(this.period[1]).setDate(this.period[1].getDate() + 1))
						.toISOString()
						.split('T')[0]
					: this.info.report.rangeEnd.toISOString().split('T')[0],
			bu: this.reportForm.value.bu === null || this.isParent ? '' : this.reportForm.value.bu,
		};

		if (this.isParent) {
			this.analyticsSvc.getParentProcessSubmissions(this.id, timePeriod).subscribe((value: any[]) => {
				if (value.length !== 0) {
					this.dataToLoad = value.filter((v) => {
						return v.duration > 0;
					});
					this.prepareHistoricalData();
				} else {
					this.msgSvc.add({
						severity: 'info',
						summary: 'Try a different time period!',
						detail: `We couldn't find any submissions.`,
					});
					this.loading.report = false;
				}
			});
		} else {
			this.analyticsSvc.getChildProcessSubmissions(this.id, timePeriod).subscribe((value: any[]) => {
				if (value.length !== 0) {
					this.dataToLoad = value.filter((v) => {
						return v.duration > 0;
					});
					this.prepareHistoricalData();
				} else {
					this.msgSvc.add({
						severity: 'info',
						summary: 'Try a different time period!',
						detail: `We couldn't find any submissions.`,
					});
					this.loading.report = false;
				}
			});
		}
	}

	/**
	 * Calculate statistcs needed for comparison to all existing submissions
	 * Call calculatePeriodStatistics()
	 */
	prepareHistoricalData() {
		this.dayZero.setMonth(this.dayZero.getMonth() - 3);
		const timePeriod = {
			default: false,
			from: this.dayZero.toISOString().split('T')[0],
			to: new Date(new Date(this.today).setDate(this.today.getDate() + 1)).toISOString().split('T')[0],
			bu: this.reportForm.value.bu === null ? '' : this.reportForm.value.bu,
		};

		let totalTime = 0;

		if (this.isParent) {
			this.analyticsSvc.getParentProcessSubmissions(this.id, timePeriod).subscribe((value: any[]) => {
				let count = 0;
				value.forEach((v) => {
					if (v.duration > 0) {
						count++;
						totalTime += v.duration;
					}
				});
				this.info.stats.avg.hist = totalTime / (count ? count : 1);
				this.info.labels.avgHist = DateCommon.dateDifference(null, null, true, this.stats.avg.hist);
				this.preparePeriodData();
			});
		} else {
			this.analyticsSvc.getChildProcessSubmissions(this.id, timePeriod).subscribe((value: any[]) => {
				let count = 0;
				value.forEach((v) => {
					if (v.duration > 0) {
						count++;
						totalTime += v.duration;
					}
				});
				this.info.stats.avg.hist = totalTime / (count ? count : 1);
				this.info.labels.avgHist = DateCommon.dateDifference(null, null, true, this.stats.avg.hist);
				this.preparePeriodData();
			});
		}
	}

	/**
	 * Calculate statistics needed for chart and summary
	 * Include count, duration average
	 * Store "empty" submissions separately
	 * Also creates formatted time labels using DateCommon to display duration statistics
	 * Call prepareChart()
	 */
	preparePeriodData() {
		let totalTime = 0;

		this.dataToLoad.forEach((v) => {
			if (v.duration > 0) {
				totalTime += v.duration;
			} else {
				this.info.stats.empty.push(v);
			}
		});

		this.dataToLoad = this.dataToLoad.filter((v) => {
			return v.duration > 0;
		});
		this.dataToLoad.sort((a, b) => {
			return a.submission.id - b.submission.id;
		});

		this.info.stats.count = this.dataToLoad.length;
		this.info.stats.avg.period = this.stats.count ? totalTime / this.stats.count : 0;

		this.prepareChart();
		this.loading.report = false;
	}

	/**
	 * Generate chart component
	 * Handle Error: only empty submissions
	 */
	prepareChart() {
		if (this.stats.avg.period === 0) {
			this.info.labels.avgPeriod = 'N/A';
			this.msgSvc.add({
				severity: 'info',
				summary: 'Whoops! Testing?',
				detail: `We only found empty submissions.`,
			});
		} else {
			this.info.labels.avgPeriod = DateCommon.dateDifference(null, null, true, this.stats.avg.period);

			this.loading.chart = true;

			this.loading.complete = true;
			this.reportForm.controls['level'].disable();
			this.reportForm.controls['process'].enable();
			this.reportForm.controls['period'].disable();
			//this.reportForm.controls['defaultPeriod'].disable();
			this.reportForm.controls['from'].disable();
			this.reportForm.controls['to'].disable();
			this.reportForm.controls['bu'].disable();
			this.reportForm.controls['searchBy'].disable();
		}
	}

	/**
	 * Reset (1) data using resetData() and (2) reportForm input
	 * Maintain processes loaded into reportForm
	 */
	resetForm() {
		if (this.stats.count) {
			const level = this.reportForm.controls['level'].value;

			this.resetData();
			this.reportForm.reset();
			this.reportForm.controls['level'].setValue(level);
			this.getProcesses();
			this.reportForm.controls['searchBy'].setValue('defaultPeriod');
			this.isDateRange = false;
			this.isPeriod = false;
			//this.reportForm.controls['defaultPeriod'].setValue(true);
			this.reportForm.enable();

			if (this.isScheduleActive || this.reportScheduled) {
				this.queryParams = {};
				this.reportScheduled = false;
				this.route.navigate(['/analytics/status']);
			}
		}
	}

	/**
	 * Reset all data stored for chart, incoming submissions and mapped calculations
	 */
	resetData() {
		this.loading = {
			complete: false, // manages form disable
			report: false, // generates loading icon
			chart: false, // triggers chart component
			export: false, // triggers export component
		};

		// processes available in form (based on selected level)
		this.processesToLoad = [];

		// raw submissions retrieved
		this.dataToLoad = [];

		// used for deviationMap of each status sub-component
		this.info = {
			process: {
				info: null,
			},
			stats: {
				count: 0,
				median: null,
				avg: {
					hist: null,
					period: null,
					success: null,
				},
				// captures faulty submissions (often from testing)
				// e.g. no/negative duration, incomplete (no endTime or 'in progress' status)
				empty: [],
			},
			labels: {
				rangeLength: '',
				avgPeriod: '',
				avgHist: '',
			},
			report: {
				rangeStart: null,
				rangeEnd: null,
				rangeLength: null,
				dateGenerated: null,
			},
		};
	}
}
