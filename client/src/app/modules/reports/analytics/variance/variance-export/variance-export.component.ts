import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { SubmissionData } from './SubmissionData';
import { DatePipe } from '@angular/common';
import * as jspdf from 'jspdf';
import html2canvas from 'html2canvas';
import { AnalyticsCommon } from 'src/app/shared/AnalyticsCommon';

@Component({
  selector: 'app-variance-export',
  templateUrl: './variance-export.component.html',
  styleUrls: ['./variance-export.component.scss']
})
export class VarianceExportComponent implements OnInit {
  @Input() deviationMap;
  @Output() loading = new EventEmitter();

  get ranges() {
    return [
      this.deviationMap.minus,
      this.deviationMap.minus2,
      this.deviationMap.minus1,
      this.deviationMap.plus1,
      this.deviationMap.plus2,
      this.deviationMap.plus
    ];
  }

  get process() {
    return this.deviationMap.info.process;
  }

  get stats() {
    return this.deviationMap.info.stats;
  }

  get schedules() {
    return this.deviationMap.info.stats.schedule.data;
  }

  get report() {
    return this.deviationMap.info.report;
  }

  columns;
  submissions: SubmissionData[] = [];

  constructor(private datePipe: DatePipe) {}

  ngOnInit() {
    this.setColumns();
    this.prepareCSVData();
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
      'Deviation Range Value',
      'Deviation Range Count',

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
    this.ranges.forEach(range => {
      range.data.map(s => {
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
          range: this.stats.count > 1 ? range.value : 'N/A',
          rangeCount: this.stats.count > 1 ? range.data.length : 'N/A',

          warnings: sub.warnings ? sub.warnings : 0,
          records: sub.records ? sub.records : 0,
          errors: sub.errors ? sub.errors : 0,

          sender: sub.process.sender.name ? sub.process.sender.name : 'N/A',
          receiver: sub.process.receiver.name ? sub.process.receiver.name : 'N/A',
          processType: sub.process.processType ? sub.process.processType.name : 'N/A',
          critical: sub.process.critical ? sub.process.critical : false,
          parent: sub.process.processParent ? sub.process.processParent.name : 'N/A',

          reportRangeStart: this.datePipe.transform(this.report.rangeStart, 'MM/dd/yyyy HH:mm z'),
          reportRangeEnd: this.datePipe.transform(this.report.rangeEnd, 'MM/dd/yyyy HH:mm z'),
          reportDate: this.datePipe.transform(this.report.dateGenerated, 'MM/dd/yyyy HH:mm z'),

          fileName:
            sub.process.name.replace(/ /g, '') +
            '_' +
            this.datePipe.transform(this.report.dateGenerated, 'yyyy-MM-dd') +
            '_VAR_Spotlight'
        };

        this.submissions.push(data);
      });
    });
  }

  /**
   * Generate PDF file, using HTML element #page
   * Scale down to fit letter format
   */
  downloadPDF() {
    const data = document.getElementById('page');

    html2canvas(data).then(canvas => {
      // parameters: 'p' portarait orientation, 'in' inches unit, 'letter' document format
      const pdf = new jspdf('p', 'in', 'letter');

      const imgWidth = 8.5;
      const imgHeight = (canvas.height * imgWidth) / canvas.width;
      const positionX = 0;
      const positionY = 0;

      const contentDataURL = canvas.toDataURL('image/png');

      pdf.addImage(contentDataURL, 'PNG', positionX, positionY, imgWidth, imgHeight, null, 'FAST');

      pdf.save(this.submissions[0].fileName + '.pdf');
    });
  }

  /**
   * Use common download method for CSV
   */
  downloadCSV() {
    AnalyticsCommon.downloadCSVFile(this.submissions, this.columns);
  }

  close() {
    this.loading.emit(false);
  }
}
