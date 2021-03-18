import { Component, Input, OnInit } from '@angular/core';
import { Validators, FormGroup, FormBuilder } from '@angular/forms';
import { ProcessService } from '../../../admin/process/process.service';
import { ScheduleService } from '../../../admin/schedule/schedule.service';
import { AnalyticsService } from '../../analytics/analytics.service';
import { DateCommon } from '../../../../shared/DateCommon';
import { MessageService } from 'primeng/api';
import { AuditLogService } from 'src/app/core/services/audit-log.service';
import { SessionService } from 'src/app/core/session/session.service';

@Component({
	selector: 'app-variance',
	templateUrl: './variance.component.html',
	styleUrls: ['./variance.component.scss'],
	providers: [AuditLogService],
})
export class VarianceComponent implements OnInit {
	reportForm: FormGroup;
	processSelected: any;
	processes = [];
	dayZero = new Date();
	today = new Date();
	searchBy = 'defaultPeriod';
	datePattern =
		'(Q[0-9][0-9]-[1-4])|(Y[0-9][0-9][0-9][0-9])|(M[0-9][0-9]-[0-1][0-9])|(((JAN)|(FEB)|(MAR)|(APR)|(MAY)|(JUN)|(JUL)|(AUG)|(SEP)|(OCT)|(NOV)|(DEC))-[2-2][0-9])';

	isPeriod: boolean = false;
	isDateRange: boolean = false;

	get id() {
		return this.reportForm.value.process.id;
	}
	get period() {
		return this.reportForm.value.period;
	}

	loading;
	private dataToLoad = [];
	private deviationMap;
	@Input() exportData?; // used for PDF export

	get info() {
		return this.deviationMap ? this.deviationMap.info : null;
	}
	get stats() {
		return this.deviationMap ? this.deviationMap.info.stats : null;
	}

	constructor(
		private fb: FormBuilder,
		private processSvc: ProcessService,
		private analyticsSvc: AnalyticsService,
		private scheduleSvc: ScheduleService,
		private msgSvc: MessageService,
		private auditLogSvc: AuditLogService,
		private sessionSvc: SessionService
	) {
		this.auditLogSvc.newAuditLog('Variance Report').subscribe((value) => {});
		this.reportForm = this.fb.group({
			process: [null, Validators.required],
			bu: [null],
			period: [{ value: null, disabled: false }],
			searchBy: 'defaultPeriod',
			from: [{ value: null, disabled: true }, Validators.pattern(this.datePattern)],
			to: [{ value: null, disabled: true }, Validators.pattern(this.datePattern)],
			//defaultPeriod: [{ value: true }]
		});
	}

	ngOnInit() {
		this.resetData();
		if (this.exportData) {
			this.deviationMap = this.exportData;
			this.loading.chart = true;
		} else {
			this.processSvc.getProcessList().subscribe((value) => {
				value.forEach((p) => {
					if (!p.isParent) {
						this.processes.push({ label: p.name, value: { id: p.id }, data: p });
					}
				});
				if (this.sessionSvc.processName !== null) {
					this.processes.forEach((p) => {
						if (p.value.id === this.sessionSvc.processName.id) {
							this.processSelected = p.value;
						}
					});
				}
			});
		}
	}

	onChange(event) {
		if (event.value !== null) {
			this.sessionSvc.processName = event.value;
			this.prepareReport();
		}
	}
	/**
	 * Disable and re-enable reportForm field for
	 * time period input when default time checkbox is selected
	 */
	/*  togglePeriod() {
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
	/* disablePeriod() {
		if (this.reportForm.controls['searchBy'].value === 'datePeriod') {
			this.isPeriod = true;
			this.isDateRange = false;
			this.reportForm.controls['from'].enable();
			this.reportForm.controls['to'].enable();
			this.reportForm.controls['period'].disable();
			this.reportForm.controls['period'].setValue(null);
		} else if (this.reportForm.controls['searchBy'].value === 'dateRange') {
			this.isDateRange = false;
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
	/**
	 * Handle all compilation needed to retrieve/map data, calculate statistics, and generate chart
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

			if (this.reportForm.value.searchBy === 'defaultPeriod') {
				this.deviationMap.info.report.rangeStart = new Date(new Date().setDate(this.today.getDate() - 30));
				this.deviationMap.info.report.rangeEnd = this.today;
			} else if (this.reportForm.value.searchBy === 'dateRange') {
				this.deviationMap.info.report.rangeStart = this.period[0];
				this.deviationMap.info.report.rangeEnd = this.period[1];
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
					this.deviationMap.info.report.rangeStart = new Date(new Date(startDate).setHours(0, 0, 0));
					this.deviationMap.info.report.rangeEnd = new Date(new Date(endDate).setHours(23, 59, 59));
				}
			}

			if (this.deviationMap.info.report.rangeStart > this.deviationMap.info.report.rangeEnd) {
				this.msgSvc.add({
					severity: 'info',
					summary: 'Slow down!',
					detail: `Invalid Date Range.`,
				});
			} else {
				this.deviationMap.info.report.dateGenerated = this.today;

				this.getProcessDetails();
				this.getSchedules();
				this.prepareData();
			}
		}
	}

	/**
	 * Retrieve additional information for the process
	 * Including: users, steps
	 * To be used in export visual
	 */
	getProcessDetails() {
		this.processes.forEach((p) => {
			if (p.value.id === this.reportForm.value.process.id) {
				this.deviationMap.info.process.info = p.data;

				this.processSvc.getProcessUsers(p.value.id).subscribe((users) => {
					this.deviationMap.info.process.users = users;
					this.deviationMap.info.process.users.map((u) => {
						u.name = u.user.name;
						u.sso = u.user.sso;
						u.id = u.user.id;
					});

					this.processSvc.getAllProcessSteps(p.value.id, true).subscribe((steps) => {
						this.deviationMap.info.process.steps = steps;
						this.deviationMap.info.process.steps.sort((a, b) => {
							return a.id - b.id;
						});
						// remove steps: start and end
						this.deviationMap.info.process.steps.splice(0, 2);
					});
				});

				return;
			}
		});
	}

	/**
	 * Retrieve all schedules of process.id
	 * Calculate average duration and tolerance (since schedules may differ)
	 */
	getSchedules() {
		this.scheduleSvc.getSchedules(this.id, true).subscribe((value) => {
			if (value.length > 0) {
				let expectedTotal = 0;
				let expectedTolerance = 0;
				value.map((s) => {
					s.startTime = new Date(s.startTime);
					s.endTime = new Date(s.endTime);
					expectedTotal += (s.endTime.getTime() - s.startTime.getTime()) / 1000;
					expectedTolerance += s.tolerance * 60;
					this.deviationMap.info.stats.schedule.data.push(s);
				});

				this.deviationMap.info.stats.schedule.value = expectedTotal / value.length;
				this.deviationMap.info.labels.schedule = DateCommon.dateDifference(
					null,
					null,
					true,
					this.stats.schedule.value
				);

				this.deviationMap.info.stats.schedule.tolerance = expectedTolerance / value.length;
				this.deviationMap.info.labels.tolerance = this.deviationMap.info.stats.schedule.tolerance
					? DateCommon.dateDifference(null, null, true, this.stats.schedule.tolerance)
					: 'N/A';
			} else {
				this.deviationMap.info.labels.schedule = 'N/A';
				this.deviationMap.info.labels.tolerance = 'N/A';
			}
		});
	}

	/**
	 * Retrieve submissions from database using process.id and timePeriod (default or range)
	 * Call calculateAllStatistics()
	 * End loading prompt
	 * Handle Errors: no submissions, empty submissions
	 */
	prepareData() {
		const timePeriod = {
			default: this.reportForm.value.searchBy === 'defaultPeriod' ? true : false,
			from:
				this.reportForm.value.searchBy === 'dateRange' && this.reportForm.value.searchBy !== 'defaultPeriod'
					? this.period[0].toISOString().split('T')[0]
					: this.deviationMap.info.report.rangeStart.toISOString().split('T')[0],
			to:
				this.reportForm.value.searchBy === 'dateRange' && this.reportForm.value.searchBy !== 'defaultPeriod'
					? new Date(new Date(this.period[1]).setDate(this.period[1].getDate() + 1))
							.toISOString()
							.split('T')[0]
					: this.deviationMap.info.report.rangeEnd.toISOString().split('T')[0],
			bu: this.reportForm.value.bu === null ? '' : this.reportForm.value.bu,
		};

		this.analyticsSvc.getChildProcessSubmissions(this.id, timePeriod).subscribe((value: any[]) => {
			if (value.length !== 0) {
				this.dataToLoad = value;
				this.prepareHistoricalData();
			} else {
				this.deviationMap.info.labels.avgPeriod = 'N/A';
				this.deviationMap.info.stats.avg.period = 0;
				this.deviationMap.info.stats.stddev = 0;
				this.deviationMap.info.stats.count = 0;

				this.msgSvc.add({
					severity: 'info',
					summary: 'Try a different time period!',
					detail: `We couldn't find any submissions.`,
				});
				this.loading.report = false;
			}
		});
	}

	/**
	 * Calculate statistcs needed for comparison to all existing submissions
	 * Call preparePeriodData()
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
		this.analyticsSvc.getChildProcessSubmissions(this.id, timePeriod).subscribe((value: any[]) => {
			let count = 0;
			value.forEach((v) => {
				if (v.duration > 0) {
					count++;
					totalTime += v.duration;
				}
			});
			this.deviationMap.info.stats.avg.hist = totalTime / (count ? count : 1);
			this.deviationMap.info.labels.avgHist = DateCommon.dateDifference(null, null, true, this.stats.avg.hist);
			this.preparePeriodData();
		});
	}

	/**
	 * Calculate statistics needed for chart and summary
	 * Include count, duration average, standard deviation, and deviation ranges
	 * Store "empty" submissions separately
	 * Also creates formatted time labels using DateCommon to display duration statistics
	 * Call prepareChart()
	 */
	preparePeriodData() {
		let totalTime = 0;
		this.dataToLoad.map((v) => {
			v.elapsedTime = DateCommon.dateDifference(null, null, true, v.duration);
			if (v.duration > 0) {
				totalTime += v.duration;
			} else {
				this.stats.empty.push(v);
			}
		});

		this.dataToLoad = this.dataToLoad.filter((v) => {
			return v.duration > 0;
		});
		this.dataToLoad.sort((a, b) => {
			return a.duration - b.duration;
		});

		this.deviationMap.info.stats.count = this.dataToLoad.length;
		this.deviationMap.info.stats.avg.period = this.stats.count ? totalTime / this.stats.count : 0;
		this.deviationMap.info.labels.avgPeriod = DateCommon.dateDifference(null, null, true, this.stats.avg.period);

		if (this.stats.avg.period > this.stats.schedule.value + this.stats.schedule.tolerance) {
			this.deviationMap.info.stats.avg.success = false;
		} else {
			this.deviationMap.info.stats.avg.success = true;
		}

		if (this.stats.count === 1) {
			this.deviationMap.info.stats.median = this.dataToLoad[0].duration;
		} else if (this.stats.count > 1) {
			if (this.stats.count % 2 !== 0) {
				this.deviationMap.info.stats.median = this.dataToLoad[Math.floor(this.stats.count / 2)].duration;
			} else {
				this.deviationMap.info.stats.median =
					(this.dataToLoad[this.stats.count / 2].duration +
						this.dataToLoad[this.stats.count / 2 - 1].duration) /
					2;
			}
		}
		this.deviationMap.info.labels.median = DateCommon.dateDifference(null, null, true, this.stats.median);

		this.calculateDeviationRanges();

		this.deviationMap.info.stats.avg.period = Math.round(this.stats.avg.period * 100) / 100;
		this.deviationMap.info.stats.stddev = Math.round(this.stats.stddev * 100) / 100;

		this.prepareChart();
		this.loading.report = false;
	}

	/**
	 * Calculate deviation values and labels for each range
	 */
	calculateDeviationRanges() {
		let sum = 0;
		this.dataToLoad.forEach((v) => {
			sum += Math.pow(v.duration - this.stats.avg.period, 2);
		});

		// standard deviation
		this.deviationMap.info.stats.stddev = this.dataToLoad.length
			? Math.sqrt(sum / (this.dataToLoad.length - 1))
			: 0;

		this.deviationMap.minus.value = Math.round((this.stats.avg.period - this.stats.stddev * 3) * 100) / 100;
		this.deviationMap.minus2.value = Math.round((this.stats.avg.period - this.stats.stddev * 2) * 100) / 100;
		this.deviationMap.minus1.value = Math.round((this.stats.avg.period - this.stats.stddev) * 100) / 100;
		this.deviationMap.plus1.value = Math.round((this.stats.avg.period + this.stats.stddev) * 100) / 100;
		this.deviationMap.plus2.value = Math.round((this.stats.avg.period + this.stats.stddev * 2) * 100) / 100;
		this.deviationMap.plus.value = Math.round((this.stats.avg.period + this.stats.stddev * 3) * 100) / 100;

		// range: minus (-)
		this.deviationMap.minus.labels.value = DateCommon.dateDifference(
			null,
			null,
			true,
			this.deviationMap.minus.value
		);
		this.deviationMap.minus.labels.range =
			'under ' + DateCommon.dateDifference(null, null, true, this.deviationMap.minus2.value);

		// range: minus2 (-2)
		this.deviationMap.minus2.labels.value = DateCommon.dateDifference(
			null,
			null,
			true,
			this.deviationMap.minus2.value
		);
		this.deviationMap.minus2.labels.range =
			DateCommon.dateDifference(null, null, true, this.deviationMap.minus2.value) +
			' to ' +
			DateCommon.dateDifference(null, null, true, this.deviationMap.minus1.value);

		// range: minus1 (-1)
		this.deviationMap.minus1.labels.value = DateCommon.dateDifference(
			null,
			null,
			true,
			this.deviationMap.minus1.value
		);
		this.deviationMap.minus1.labels.range =
			DateCommon.dateDifference(null, null, true, this.deviationMap.minus1.value) +
			' to ' +
			DateCommon.dateDifference(null, null, true, this.stats.avg.period);

		// range: plus1 (+1)
		this.deviationMap.plus1.labels.value = DateCommon.dateDifference(
			null,
			null,
			true,
			this.deviationMap.plus1.value
		);
		this.deviationMap.plus1.labels.range =
			DateCommon.dateDifference(null, null, true, this.stats.avg.period) +
			' to ' +
			DateCommon.dateDifference(null, null, true, this.deviationMap.plus1.value);

		// range: plus2 (+2)
		this.deviationMap.plus2.labels.value = DateCommon.dateDifference(
			null,
			null,
			true,
			this.deviationMap.plus2.value
		);
		this.deviationMap.plus2.labels.range =
			DateCommon.dateDifference(null, null, true, this.deviationMap.plus1.value) +
			' to ' +
			DateCommon.dateDifference(null, null, true, this.deviationMap.plus2.value);

		// range: plus (+)
		this.deviationMap.plus.labels.value = DateCommon.dateDifference(null, null, true, this.deviationMap.plus.value);
		this.deviationMap.plus.labels.range =
			'over ' + DateCommon.dateDifference(null, null, true, this.deviationMap.plus2.value);
	}

	/**
	 * Call mapDeviationData()
	 * Generate chart component
	 * Handle Error: only empty submissions
	 */
	prepareChart() {
		if (this.stats.avg.period === 0) {
			this.deviationMap.info.labels.avgPeriod = 'N/A';
			this.msgSvc.add({
				severity: 'info',
				summary: 'Whoops! Testing?',
				detail: `We only found empty submissions.`,
			});
		} else {
			this.mapDeviationData();
			if (this.stats.count > 1) {
				this.loading.chart = true;
			}
			this.loading.complete = true;
			this.reportForm.controls['period'].disable();
			//this.reportForm.controls['defaultPeriod'].disable();
			this.reportForm.controls['from'].disable();
			this.reportForm.controls['to'].disable();
			this.reportForm.controls['bu'].disable();
			this.reportForm.controls['searchBy'].disable();
		}
	}

	/**
	 * Create labels for each deviation range
	 * Map each submission in dataToLoad[] to
	 * corresponding deviation range in deviationMap[]
	 */
	mapDeviationData() {
		this.dataToLoad.forEach((v) => {
			if (v.duration >= this.stats.avg.period) {
				if (v.duration > this.deviationMap.plus2.value) {
					this.deviationMap.plus.data.push(v);
				} else if (v.duration > this.deviationMap.plus1.value) {
					this.deviationMap.plus2.data.push(v);
				} else {
					this.deviationMap.plus1.data.push(v);
				}
			} else {
				if (v.duration < this.deviationMap.minus2.value) {
					this.deviationMap.minus.data.push(v);
				} else if (v.duration < this.deviationMap.minus1.value) {
					this.deviationMap.minus2.data.push(v);
				} else {
					this.deviationMap.minus1.data.push(v);
				}
			}
		});
	}

	/**
	 * Handle Export Preview
	 */
	closePreview(loading) {
		if (!loading) {
			this.loading.export = false;
		}
	}

	/**
	 * Reset (1) data using resetData() and (2) reportForm input
	 * Maintain processes loaded into reportForm
	 */
	resetForm() {
		if (this.stats.count) {
			this.resetData();
			this.reportForm.reset();
			this.reportForm.controls['searchBy'].setValue('defaultPeriod');
			/*  this.isDateRange = false;
      this.isPeriod = false; */
			this.reportForm.enable();
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

		// raw submissions retrieved
		this.dataToLoad = [];

		// information + stats related to data
		this.deviationMap = {
			info: {
				process: {
					info: null,
					users: null,
					steps: [],
				},
				stats: {
					schedule: {
						value: null,
						tolerance: null,
						data: [],
					},
					count: 0,
					median: null,
					stddev: null,
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
					schedule: '',
					tolerance: '',
					avgPeriod: '',
					avgHist: '',
					median: '',
				},
				report: {
					rangeStart: null,
					rangeEnd: null,
					dateGenerated: null,
				},
			},
			minus: {
				value: null,
				labels: {
					title: 'Deviation Range: -',
					value: '',
					range: '',
				},
				data: [],
			},
			minus2: {
				value: null,
				labels: {
					title: 'Deviation Range: -2',
					value: '',
					range: '',
				},
				data: [],
			},
			minus1: {
				value: null,
				labels: {
					title: 'Deviation Range: -1',
					value: '',
					range: '',
				},
				data: [],
			},
			plus1: {
				value: null,
				labels: {
					title: 'Deviation Range: +1',
					value: '',
					range: '',
				},
				data: [],
			},
			plus2: {
				value: null,
				labels: {
					title: 'Deviation Range: +2',
					value: '',
					range: '',
				},
				data: [],
			},
			plus: {
				value: null,
				labels: {
					title: 'Deviation Range: +',
					value: '',
					range: '',
				},
				data: [],
			},
		};
	}
}
