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
  providers: [AuditLogService]
})
export class StatusComponent implements OnInit {
  queryParams = {};
  reportScheduled = false;
  get isScheduleActive(): boolean {
    const temp = this.router.snapshot.queryParams;
    return this.queryParams !== {} && temp['submission_level'] && temp['process_id'] && temp['range_length']
      ? true
      : false;
  }

  displayAllSchedules = false;
  scheduleMenu: MenuItem[] = [
    {
      label: 'View Report',
      command: (event: any) => {
        this.displayAllSchedules = false;
      }
    },
    {
      label: 'See Schedules',
      command: (event: any) => {
        this.displayAllSchedules = true;
      }
    }
  ];

  reportForm: FormGroup;

  levels = [{ label: 'Child Submissions', value: 'C' }, { label: 'Parent Submissions', value: 'P' }];
  processesToLoad = [];
  parentProcesses = [];
  childProcesses = [];
  hasProcessAccess;

  dayZero = new Date(2019, 1, 7, 0, 0, 0);
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
    this.auditLogSvc.newAuditLog('Status Report').subscribe(value => { });;

    this.reportForm = this.fb.group({
      level: ['', Validators.required],
      process: [null, Validators.required],
      period: [{ value: null, disabled: !this.isScheduleActive }],
      defaultPeriod: [{ value: !this.isScheduleActive }]
    });

    this.resetData();

    this.processSvc.getAllProcesses(true).subscribe(value => {
      value.forEach(p => {
        if (p.isParent) {
          this.parentProcesses.push({ label: p.name, value: { id: p.id }, data: p });
        } else {
          this.childProcesses.push({ label: p.name, value: { id: p.id }, data: p });
        }
      });

      this.router.queryParams.subscribe(params => {
        this.queryParams = params;
        if (this.isScheduleActive) {
          this.reportForm.disable();
          this.generateScheduledReport(params);
        }
      });
    });
  }

  ngOnInit() { }

  getProcesses() {
    if (this.reportForm.controls['level'].value === 'C') {
      this.processesToLoad = this.childProcesses;
    } else {
      this.processesToLoad = this.parentProcesses;
    }
  }

  /**
   * Disable and re-enable varianceForm field for
   * time period input when default time checkbox is selected
   */
  togglePeriod() {
    if (this.reportForm.value.defaultPeriod) {
      this.reportForm.controls['period'].disable();
    } else {
      this.reportForm.controls['period'].enable();
    }
  }

  /**
   * Use route parameters to fill form and prepare report
   */
  generateScheduledReport(params) {
    const level = params['submission_level'];
    const id = parseInt(params['process_id'], 10);
    const rangeLength = parseInt(params['range_length'], 10);
    const start = new Date(new Date(this.today).setDate(this.today.getDate() - rangeLength));
    const end = new Date(this.today);

    this.reportForm.patchValue({
      level: level
    });
    this.getProcesses();
    this.reportForm.patchValue({
      process: { id: id },
      period: [start, end],
      defaultPeriod: false
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
    if (!this.reportForm.value.defaultPeriod && (!this.period || !this.period[1])) {
      this.msgSvc.add({
        severity: 'info',
        summary: 'Slow down!',
        detail: `We need some dates.`
      });
    } else {
      this.loading.report = true;

      this.processSvc.getProcessUsers(this.id).subscribe(users => {
        this.hasProcessAccess = users.some(u => u.user.sso === this.sessionSvc.sso) ? true : this.hasProcessAccess;

        if (this.reportForm.value.defaultPeriod) {
          this.info.report.rangeStart = new Date(
            new Date(new Date(this.today).setHours(0, 0, 0)).setDate(this.today.getDate() - 30)
          );
          this.info.report.rangeEnd = new Date(new Date(this.today).setHours(23, 59, 59));
        } else {
          this.info.report.rangeStart = new Date(new Date(this.period[0]).setHours(0, 0, 0));
          this.info.report.rangeEnd = new Date(new Date(this.period[1]).setHours(23, 59, 59));
        }

        // get date range difference in days (ignoring DST)
        const msecPerDay = 1000 * 60 * 60 * 24;
        const msecBetween = this.info.report.rangeEnd.getTime() - this.info.report.rangeStart.getTime();
        const days = msecBetween / msecPerDay;
        this.info.report.rangeLength = Math.abs(Math.floor(days));
        this.info.labels.rangeLength = this.info.report.rangeLength + ' days';

        this.info.report.dateGenerated = this.today;

        const found = this.mapProcess();

        if (found) {
          this.getData();
        } else {
          this.msgSvc.add({
            severity: 'info',
            summary: 'Whoops! Testing?',
            detail: `That process doesn't exist.`
          });
          this.loading.report = false;
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
    this.processesToLoad.forEach(p => {
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
      default: this.reportForm.value.defaultPeriod ? true : false,
      from: this.period && this.period[0] ? this.period[0].toISOString().split('T')[0] : null,
      to:
        this.period && this.period[1]
          ? new Date(new Date(this.period[1]).setDate(this.period[1].getDate() + 1)).toISOString().split('T')[0]
          : null
    };

    if (this.isParent) {
      this.analyticsSvc.getParentProcessSubmissions(this.id, timePeriod).subscribe((value: any[]) => {
        if (value.length !== 0) {
          this.dataToLoad = value.filter(v => {
            return v.duration > 0;
          });
          this.prepareHistoricalData();
        } else {
          this.msgSvc.add({
            severity: 'info',
            summary: 'Try a different time period!',
            detail: `We couldn't find any submissions.`
          });
          this.loading.report = false;
        }
      });
    } else {
      this.analyticsSvc.getChildProcessSubmissions(this.id, timePeriod).subscribe((value: any[]) => {
        if (value.length !== 0) {
          this.dataToLoad = value.filter(v => {
            return v.duration > 0;
          });
          this.prepareHistoricalData();
        } else {
          this.msgSvc.add({
            severity: 'info',
            summary: 'Try a different time period!',
            detail: `We couldn't find any submissions.`
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
    const timePeriod = {
      default: false,
      from: this.dayZero.toISOString().split('T')[0],
      to: new Date(new Date(this.today).setDate(this.today.getDate() + 1)).toISOString().split('T')[0]
    };

    let totalTime = 0;

    if (this.isParent) {
      this.analyticsSvc.getParentProcessSubmissions(this.id, timePeriod).subscribe((value: any[]) => {
        let count = 0;
        value.forEach(v => {
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
        value.forEach(v => {
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

    this.dataToLoad.forEach(v => {
      if (v.duration > 0) {
        totalTime += v.duration;
      } else {
        this.info.stats.empty.push(v);
      }
    });

    this.dataToLoad = this.dataToLoad.filter(v => {
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
        detail: `We only found empty submissions.`
      });
    } else {
      this.info.labels.avgPeriod = DateCommon.dateDifference(null, null, true, this.stats.avg.period);

      this.loading.chart = true;

      this.loading.complete = true;
      this.reportForm.controls['level'].disable();
      this.reportForm.controls['process'].enable();
      this.reportForm.controls['period'].disable();
      this.reportForm.controls['defaultPeriod'].disable();
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
      this.reportForm.controls['defaultPeriod'].setValue(true);
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
      export: false // triggers export component
    };

    // processes available in form (based on selected level)
    this.processesToLoad = [];

    // raw submissions retrieved
    this.dataToLoad = [];

    // used for deviationMap of each status sub-component
    this.info = {
      process: {
        info: null
      },
      stats: {
        count: 0,
        median: null,
        avg: {
          hist: null,
          period: null,
          success: null
        },
        // captures faulty submissions (often from testing)
        // e.g. no/negative duration, incomplete (no endTime or 'in progress' status)
        empty: []
      },
      labels: {
        rangeLength: '',
        avgPeriod: '',
        avgHist: ''
      },
      report: {
        rangeStart: null,
        rangeEnd: null,
        rangeLength: null,
        dateGenerated: null
      }
    };
  }
}
