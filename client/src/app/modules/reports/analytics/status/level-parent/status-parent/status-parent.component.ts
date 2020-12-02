import { Component, OnInit, Input } from '@angular/core';
import { SubmissionData } from './SubmissionData';
import { DatePipe } from '@angular/common';
import * as c3 from 'c3';
import { AnalyticsCommon } from 'src/app/shared/AnalyticsCommon';

@Component({
  selector: 'app-status-parent',
  templateUrl: './status-parent.component.html',
  styleUrls: ['./status-parent.component.scss']
})
export class StatusParentComponent implements OnInit {
  @Input() dataToLoad;
  @Input() reportInfo;

  private deviationMap;
  chart;

  columns;
  submissions: SubmissionData[] = [];

  get ranges() {
    return [this.deviationMap.success, this.deviationMap.warning, this.deviationMap.failed];
  }

  constructor(private datePipe: DatePipe) {}

  ngOnInit() {
    this.prepareDeviationMap();
    this.mapDeviationData();
    this.generateChart();
  }

  /**
   * Prepare storage of data for chart, incoming submissions and mapped calculations
   */
  prepareDeviationMap() {
    this.deviationMap = {
      info: this.reportInfo,
      success: {
        labels: {
          name: 'Success'
        },
        data: []
      },
      warning: {
        labels: {
          name: 'Warning'
        },
        data: []
      },
      failed: {
        labels: {
          name: 'Failed'
        },
        data: []
      }
    };
  }

  /**
   * Map each submission in dataToLoad[] to
   * corresponding deviation range in deviationMap[]
   */
  mapDeviationData() {
    this.dataToLoad.forEach(v => {
      if (v.submission.status.name === 'success') {
        this.deviationMap.success.data.push(v);
      } else if (v.submission.status.name === 'warning') {
        this.deviationMap.warning.data.push(v);
      } else if (v.submission.status.name === 'failed') {
        this.deviationMap.warning.data.push(v);
      }
    });
  }

  /**
   * Generate c3js chart using data in deviationMap[]
   */
  generateChart() {
    const ranges = this.ranges;

    this.chart = c3.generate({
      bindto: '#chartStatus5',
      size: {
        height: 350
      },
      color: {
        pattern: ['#3daf66', '#e8a325', '#db4c41']
      },
      data: {
        columns: [
          [ranges[0].labels.name, ranges[0].data.length],
          [ranges[1].labels.name, ranges[1].data.length],
          [ranges[2].labels.name, ranges[2].data.length]
        ],
        type: 'donut'
      },
      tooltip: {
        format: {
          value: function(value, ratio, id, index) {
            return value;
          }
        }
      },
      legend: {
        item: { onclick: function() {} }
      }
    });
  }

  /**
   * Call functions to preapre and download CSV
   */
  exportReport() {
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

      'Total Submissions',
      'Empty Submissions',

      'Sender',
      'Receiver',
      'Process Type',
      'Critical',

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

          total: this.deviationMap.info.stats.count,
          empty: this.deviationMap.info.stats.empty.length,

          sender: sub.process.sender.name ? sub.process.sender.name : 'N/A',
          receiver: sub.process.receiver.name ? sub.process.receiver.name : 'N/A',
          processType: sub.process.processType ? sub.process.processType.name : 'N/A',
          critical: sub.process.critical ? sub.process.critical : false,

          reportRangeStart: this.datePipe.transform(this.deviationMap.info.report.rangeStart, 'MM/dd/yyyy HH:mm z'),
          reportRangeEnd: this.datePipe.transform(this.deviationMap.info.report.rangeEnd, 'MM/dd/yyyy HH:mm z'),
          reportDate: this.datePipe.transform(this.deviationMap.info.report.dateGenerated, 'MM/dd/yyyy HH:mm z'),

          fileName:
            sub.process.name.replace(/ /g, '') +
            '_' +
            this.datePipe.transform(this.deviationMap.info.report.dateGenerated, 'yyyy-MM-dd') +
            '_STATUS_P1_Spotlight'
        };

        this.submissions.push(data);
      });
    });
  }
}
