import { Component, OnInit, Input } from '@angular/core';
import { SubmissionData } from './SubmissionData';
import { DatePipe } from '@angular/common';
import { AnalyticsCommon } from 'src/app/shared/AnalyticsCommon';

@Component({
  selector: 'app-trend-export',
  template: ''
})
export class TrendExportComponent implements OnInit {
  @Input() dataToLoad;
  @Input() deviationMap;

  get stats() {
    return this.deviationMap.info.stats;
  }

  columns;
  submissions: SubmissionData[] = [];

  constructor(private datePipe: DatePipe) {}

  ngOnInit() {
    this.setColumns();
    this.prepareCSVData();
    AnalyticsCommon.downloadCSVFile(this.submissions, this.columns);
  }

  /**
   * Create Column names for CSV file
   */
  setColumns() {
    this.columns = [
      'Submission ID',
      'Process',
      'Adhoc',
      'Start Time',
      'End Time',
      'Duration (m)',
      'Ending Status',

      'Scheduled Duration (m)',
      'Tolerance (m)',
      'Average Period (m)',
      'Average Historical (m)',
      'Historical Comparison (%)',
      'Total Submissions',
      'Empty Submissions',

      'Warnings',
      'Records',
      'Errors',

      'Sender',
      'Receiver',
      'Process Type',
      'Critical',
      'Parent',

      'Report Start Date',
      'Report End Date',
      'Date Report Generated'
    ];
  }
  /**
   * Map submissions data for CSV file
   */
  prepareCSVData() {
    this.dataToLoad.forEach(s => {
      const sub = s.submission;

      const data: SubmissionData = {
        id: sub.id,
        process: sub.process.name,
        adHoc: sub.adHoc ? sub.adHoc : false,
        startTime: this.datePipe.transform(new Date(sub.startTime), 'MM/dd/yyyy HH:mm z'),
        endTime: this.datePipe.transform(new Date(sub.endTime), 'MM/dd/yyyy HH:mm z'),
        duration: Math.round((s.duration / 60) * 10000) / 10000,
        status: sub.status ? sub.status.name : 'N/A',

        schedule: Math.round((this.stats.schedule.value / 60) * 10000) / 10000,
        tolerance: Math.round((this.stats.schedule.tolerance / 60) * 10000) / 10000,
        avgPeriod: Math.round((this.stats.avg.period / 60) * 10000) / 10000,
        avgHist: Math.round((this.stats.avg.hist / 60) * 10000) / 10000,
        comparison: Math.round(((s.duration - this.stats.avg.hist) / this.stats.avg.hist) * 10000) / 10000,
        total: this.stats.count,
        empty: this.stats.empty.length,

        warnings: sub.warnings ? sub.warnings : 0,
        records: sub.records ? sub.records : 0,
        errors: sub.errors ? sub.errors : 0,

        sender: sub.process.sender.name ? sub.process.sender.name : 'N/A',
        receiver: sub.process.receiver.name ? sub.process.receiver.name : 'N/A',
        processType: sub.process.processType ? sub.process.processType.name : 'N/A',
        critical: sub.process.critical ? sub.process.critical : false,
        parent: sub.process.processParent ? sub.process.processParent.name : 'N/A',

        reportRangeStart: this.datePipe.transform(this.deviationMap.info.report.rangeStart, 'MM/dd/yyyy HH:mm z'),
        reportRangeEnd: this.datePipe.transform(this.deviationMap.info.report.rangeEnd, 'MM/dd/yyyy HH:mm z'),
        reportDate: this.datePipe.transform(this.deviationMap.info.report.dateGenerated, 'MM/dd/yyyy HH:mm z'),

        fileName:
          sub.process.name.replace(/ /g, '') +
          '_' +
          this.datePipe.transform(this.deviationMap.info.report.dateGenerated, 'yyyy-MM-dd') +
          '_TREND_Spotlight'
      };

      this.submissions.push(data);
    });
  }
}
