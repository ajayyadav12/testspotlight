import { Component, OnInit, Input } from '@angular/core';
import { SubmissionData } from './SubmissionData';
import { DatePipe } from '@angular/common';
import * as c3 from 'c3';
import { ProcessService } from 'src/app/modules/admin/process/process.service';
import { AnalyticsCommon } from 'src/app/shared/AnalyticsCommon';

@Component({
  selector: 'app-status-children',
  templateUrl: './status-children.component.html',
  styleUrls: ['./status-children.component.scss']
})
export class StatusChildrenComponent implements OnInit {
  @Input() dataToLoad;
  @Input() reportInfo;

  private deviationMap;

  get childMap() {
    return this.deviationMap.childMap;
  }

  get newChild() {
    return {
      info: {
        child: {
          id: null
        },
        stats: {
          count: 0,
          empty: []
        },
        labels: {
          name: null
        }
      },
      success: {
        percentage: 0,
        data: [],
        labels: {
          name: 'Success'
        }
      },
      warning: {
        percentage: 0,
        data: [],
        labels: {
          name: 'Warning'
        }
      },
      failed: {
        percentage: 0,
        data: [],
        labels: {
          name: 'Failed'
        }
      }
    };
  }

  chart;

  columns;
  submissions: SubmissionData[] = [];

  constructor(private datePipe: DatePipe, private processSvc: ProcessService) {}

  ngOnInit() {
    this.prepareDeviationMap();
    this.mapDeviationData();
    this.prepareChartData();
  }

  /**
   * Prepare storage of data for chart, incoming submissions and mapped calculations
   */
  prepareDeviationMap() {
    this.deviationMap = {
      info: this.reportInfo,
      submissions: [], // for csv export
      childMap: [] // for chart
    };
  }

  /**
   * Map each submission in dataToLoad[] to
   * corresponding deviation range in deviationMap[]
   */
  mapDeviationData(): any {
    this.dataToLoad.forEach(v => {
      const children = [];

      v.submission.children.forEach(child => {
        let found = false;
        let index = 0;

        // if child already created, find proper child index in child map
        this.deviationMap.childMap.forEach(map => {
          if (map.info.child.id === child.process.id) {
            found = true;

            // add child to child map using found index
            this.mapChildByStatus(index, child);
            return;
          }
          index++;
        });

        // if existing child not found, create new child in child map
        if (!found) {
          this.deviationMap.childMap.push(this.newChild);
          index = this.deviationMap.childMap.length - 1;
          this.deviationMap.childMap[index].info.child.id = child.process.id;
          this.deviationMap.childMap[index].info.labels.name = child.process.name;

          // add child to child map using new index
          this.mapChildByStatus(index, child);
        }

        // populate child[] in submission (for csv export)
        children.push(child);
      });

      // sort childs[] by child submission id (for csv export)
      children.sort((a, b) => {
        return a.id - b.id;
      });

      v.submission.children = children;
    });
  }

  /**
   * Place child into proper child map by status group
   */
  mapChildByStatus(index: number, child: any) {
    if (child.status.name === 'success') {
      this.deviationMap.childMap[index].success.data.push(child);
    } else if (child.status.name === 'warning') {
      this.deviationMap.childMap[index].warning.data.push(child);
    } else if (child.status.name === 'failed') {
      this.deviationMap.childMap[index].failed.data.push(child);
    } else {
      this.deviationMap.childMap[index].info.stats.empty.push(child);
      return;
    }
    this.deviationMap.childMap[index].info.stats.count++;
  }

  prepareChartData() {
    // sort child by process.id (for chart)
    this.deviationMap.childMap.sort((a, b) => {
      return a.info.child.id - b.info.child.id;
    });

    this.calculatePercentages();
    this.generateChart();
  }

  /**
   * Calculate percentage values for each childMap status range
   */
  calculatePercentages() {
    this.deviationMap.childMap.map(map => {
      map.success.percentage = map.success.data.length / map.info.stats.count;
      map.warning.percentage = map.warning.data.length / map.info.stats.count;
      map.failed.percentage = map.failed.data.length / map.info.stats.count;
    });
  }

  /**
   *  Map childMap by status group for chart
   */
  setRows() {
    const rows: any[] = [['Success', 'Warning', 'Failed'], []];
    const names = [];

    this.deviationMap.childMap.forEach(map => {
      rows.push([map.success.percentage * 100, map.warning.percentage * 100, map.failed.percentage * 100]);
      names.push(map.info.labels.name);
    });

    return [rows, names];
  }
  /**
   * Generate c3js chart using data in deviationMap[]
   */
  generateChart() {
    const childMap = this.childMap;
    const data: any = this.setRows();
    const rows = data[0];
    const names = data[1];

    this.chart = c3.generate({
      bindto: '#chartStatus4',
      size: {
        height: 350
      },
      color: {
        pattern: ['#3daf66', '#e8a325', '#db4c41']
      },
      data: {
        rows: rows,
        type: 'bar',
        groups: [rows[0]],
        labels: false
      },
      tooltip: {
        format: {
          title: function(x) {
            return 'Child ' + x + ': ' + names[x - 1];
          },
          value: function(value, ratio, id, index) {
            return (
              Math.round(value * 10) / 10 + '% (' + Math.round((value / 100) * childMap[index].info.stats.count) + ')'
            );
          }
        }
      },
      axis: {
        rotated: true,
        x: {
          label: {
            text: 'Child Order (#)',
            position: 'outer-right'
          },
          min: 1,
          max: childMap.length
        },
        y: {
          label: {
            text: 'Percentage (%)',
            position: 'outer-top'
          },
          max: 100,
          min: 10
        }
      }
    });
  }

  /**
   * Call functions to preapre and download CSV
   */
  exportReport() {
    this.processSvc.getChildren(this.reportInfo.process.info.id).subscribe(children => {
      this.setColumns();
      this.prepareCSVData(children.length);
      AnalyticsCommon.downloadCSVFile(this.submissions, this.columns);
    });
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

      'Expected Children',
      'Incoming Children',

      'Child Submission ID',
      'Child Name',
      'Child Feed Type',
      'Child Process ID',
      'Child Start Time',
      'Child End Time',
      'Child Duration (m)',
      'Child Ending Status',

      'Warnings',
      'Records',
      'Errors',

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
  prepareCSVData(expChildCount) {
    this.dataToLoad.forEach(v => {
      const sub = v.submission;

      sub.children.forEach(child => {
        const childStart: any = new Date(child.startTime);
        const childEnd: any = new Date(child.endTime);

        const data: SubmissionData = {
          id: sub.id,
          process: sub.process.name,
          adHoc: sub.adHoc ? sub.adHoc : false,
          startTime: this.datePipe.transform(new Date(sub.startTime), 'MM/dd/yyyy HH:mm z'),
          endTime: this.datePipe.transform(new Date(sub.endTime), 'MM/dd/yyyy HH:mm z'),
          duration: Math.round((v.duration / 60) * 10000) / 10000,
          status: sub.status ? sub.status.name : 'N/A',

          expectedChildren: expChildCount,
          incomingChildren: sub.children.length,

          childSubmissionId: child.id,
          childName: child.process.name,
          childFeedType: child.process.feedType ? child.process.feedType.name : 'N/A',
          childId: child.process.id,
          childStartTime: this.datePipe.transform(childStart, 'MM/dd/yyyy HH:mm z'),
          childEndTime: this.datePipe.transform(childEnd, 'MM/dd/yyyy HH:mm z'),
          childDuration: Math.round(((childEnd - childStart) / 60000) * 100) / 100,
          childStatus: child.status ? child.status.name : 'N/A',

          warnings: child.warnings ? child.warnings : 0,
          records: child.records ? child.records : 0,
          errors: child.errors ? child.errors : 0,

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
            '_STATUS_P2_Spotlight'
        };

        this.submissions.push(data);
      });
    });
  }
}
