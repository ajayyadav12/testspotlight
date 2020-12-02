import { Component, Input, OnInit } from '@angular/core';
import { Validators, FormGroup, FormBuilder } from '@angular/forms';
import { ProcessService } from '../../../admin/process/process.service';
import { ScheduleService } from '../../../admin/schedule/schedule.service';
import { AnalyticsService } from '../../analytics/analytics.service';
import { DateCommon } from '../../../../shared/DateCommon';
import { MessageService } from 'primeng/api';
import { AuditLogService } from 'src/app/core/services/audit-log.service';

@Component({
  selector: 'app-trend',
  templateUrl: './trend.component.html',
  styleUrls: ['./trend.component.scss'],
  providers: [AuditLogService]
})
export class TrendComponent implements OnInit {
  reportForm: FormGroup;
  processes = [];
  dayZero = new Date(2019, 1, 7);
  today = new Date();

  get id() {
    return this.reportForm.value.process.id;
  }
  get period() {
    return this.reportForm.value.period;
  }

  loading;
  private dataToLoad = [];
  private deviationMap;

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
    private auditLogSvc: AuditLogService
  ) {
    this.auditLogSvc.newAuditLog('Trending Report').subscribe(value => { });
    this.reportForm = this.fb.group({
      process: [null, Validators.required],
      period: [{ value: null, disabled: true }],
      defaultPeriod: [{ value: true }]
    });
  }

  ngOnInit() {
    this.resetData();
    this.processSvc.getAllProcesses(true).subscribe(value => {
      value.forEach(p => {
        if (!p.isParent) {
          // data (p) is used later to assign process info when selected
          this.processes.push({ label: p.name, value: { id: p.id }, data: p });
        }
      });
    });
  }

  /**
   * Disable and re-enable reportForm field for
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
   * Handle all compilation needed to retrieve/map data, calculate statistics, and generate chart
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

      this.deviationMap.info.report.dateGenerated = this.today;

      this.mapProcess();
      this.getSchedules();
      this.getData();
    }
  }

  /**
   * Map selected process data from form
   * To be used in export
   */
  mapProcess() {
    this.processes.forEach(p => {
      if (p.value.id === this.reportForm.value.process.id) {
        this.deviationMap.info.process.info = p.data;
        return;
      }
    });
  }

  /**
   * Retrieve all schedules of process.id
   * Calculate average duration and tolerance (since schedules may differ)
   */
  getSchedules() {
    this.scheduleSvc.getSchedules(this.id, true).subscribe(value => {
      if (value.length > 0) {
        let expectedTotal = 0;
        let expectedTolerance = 0;
        value.map(s => {
          s.startTime = new Date(s.startTime);
          s.endTime = new Date(s.endTime);
          expectedTotal += (s.endTime.getTime() - s.startTime.getTime()) / 1000;
          expectedTolerance += s.tolerance * 60;
          this.deviationMap.info.stats.schedule.data.push(s);
        });

        this.deviationMap.info.stats.schedule.value = expectedTotal / value.length;
        this.deviationMap.info.labels.schedule = DateCommon.dateDifference(null, null, true, this.stats.schedule.value);

        this.deviationMap.info.stats.schedule.tolerance = expectedTolerance / value.length;
        this.deviationMap.info.labels.tolerance = DateCommon.dateDifference(
          null,
          null,
          true,
          this.stats.schedule.tolerance
        );
      } else {
        this.deviationMap.info.labels.schedule = 'N/A';
        this.deviationMap.info.labels.tolerance = 'N/A';
      }
    });
  }

  /**
   * Retrieve submissions from database using process.id and timePeriod (default or range)
   * Call preareHistorical()
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

    this.analyticsSvc.getChildProcessSubmissions(this.id, timePeriod).subscribe((value: any[]) => {
      if (value.length !== 0) {
        this.dataToLoad = value;
        this.prepareHistoricalData();
      } else {
        this.deviationMap.info.labels.avgPeriod = 'N/A';
        this.deviationMap.info.stats.avg.period = 0;
        this.deviationMap.info.stats.count = 0;

        this.msgSvc.add({
          severity: 'info',
          summary: 'Try a different time period!',
          detail: `We couldn't find any submissions.`
        });
        this.loading.report = false;
      }
    });
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
    this.analyticsSvc.getChildProcessSubmissions(this.id, timePeriod).subscribe((value: any[]) => {
      let count = 0;
      value.forEach(v => {
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
   * Include count, duration average
   * Store "empty" submissions separately
   * Also creates formatted time labels using DateCommon to display duration statistics
   * Call prepareChart()
   */
  preparePeriodData() {
    let totalTime = 0;
    this.dataToLoad.map(v => {
      v.elapsedTime = DateCommon.dateDifference(null, null, true, v.duration);
      if (v.duration > 0) {
        totalTime += v.duration;
      } else {
        this.stats.empty.push(v);
      }
    });

    this.dataToLoad = this.dataToLoad.filter(v => {
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
          (this.dataToLoad[this.stats.count / 2].duration + this.dataToLoad[this.stats.count / 2 - 1].duration) / 2;
      }
    }
    this.deviationMap.info.labels.median = DateCommon.dateDifference(null, null, true, this.stats.median);

    this.deviationMap.info.stats.avg.period = Math.round(this.stats.avg.period * 100) / 100;

    this.prepareChart();
    this.loading.report = false;
  }

  /**
   * Generate chart component
   * Handle Error: only empty submissions
   */
  prepareChart() {
    if (this.stats.avg.period === 0) {
      this.deviationMap.info.labels.avgPeriod = 'N/A';
      this.msgSvc.add({
        severity: 'info',
        summary: 'Whoops! Testing?',
        detail: `We only found empty submissions.`
      });
    } else {
      this.info.labels.avgPeriod = DateCommon.dateDifference(null, null, true, this.stats.avg.period);

      if (this.stats.count > 1) {
        this.loading.chart = true;
      }
      this.loading.complete = true;
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
      this.resetData();
      this.reportForm.reset();
      this.reportForm.controls['defaultPeriod'].setValue(true);
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
      export: false // triggers export component
    };

    // raw submissions retrieved
    this.dataToLoad = [];

    // information + stats related to data
    this.deviationMap = {
      info: {
        process: {
          info: null
        },
        stats: {
          schedule: {
            value: null,
            tolerance: null,
            data: []
          },
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
          schedule: '',
          tolerance: '',
          avgPeriod: '',
          avgHist: '',
          median: '',
          rangeLength: ''
        },
        report: {
          rangeStart: null,
          rangeEnd: null,
          rangeLength: null,
          dateGenerated: null
        }
      }
    };
  }
}
