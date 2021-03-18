import { Component, OnInit, Input } from '@angular/core';
import { SubmissionData } from './SubmissionData';
import { DatePipe } from '@angular/common';
import * as c3 from 'c3';
import { AnalyticsCommon } from 'src/app/shared/AnalyticsCommon';

@Component({
  selector: 'app-status-steps',
  templateUrl: './status-steps.component.html',
  styleUrls: ['./status-steps.component.scss']
})
export class StatusStepsComponent implements OnInit {
  @Input() dataToLoad;
  @Input() reportInfo;

  private deviationMap;

  get stepMap() {
    return this.deviationMap.stepMap;
  }

  get newStep() {
    return {
      info: {
        step: {
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

  constructor(private datePipe: DatePipe) {}

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
      stepMap: [] // for chart
    };
  }

  /**
   * Map each submission in dataToLoad[] to
   * corresponding deviation range in deviationMap[]
   */
  mapDeviationData(): any {
    this.dataToLoad.forEach(v => {
      const steps = [];

      v.submission.steps.forEach(step => {
        if (step.processStep.name !== 'start' && step.processStep.name !== 'end') {
          let found = false;
          let index = 0;

          // if step already created, find proper step index in step map
          this.deviationMap.stepMap.forEach(map => {
            if (map.info.step.id === step.processStep.id) {
              found = true;

              // add step to step map using found index
              this.mapStepByStatus(index, step);
              return;
            }
            index++;
          });

          // if existing step not found, create new step in step map
          if (!found) {
            this.deviationMap.stepMap.push(this.newStep);
            index = this.deviationMap.stepMap.length - 1;
            this.deviationMap.stepMap[index].info.step.id = step.processStep.id;
            this.deviationMap.stepMap[index].info.labels.name = step.processStep.name;

            // add step to step map using new index
            this.mapStepByStatus(index, step);
          }

          // populate step[] in submission (for csv export)
          steps.push(step);
        }
      });

      // sort steps[] by step submission id (for csv export)
      steps.sort((a, b) => {
        return a.id - b.id;
      });

      v.submission.steps = steps;
    });
  }

  prepareChartData() {
    // sort stepMap by processStep.id (for chart)
    this.deviationMap.stepMap.sort((a, b) => {
      return a.info.step.id - b.info.step.id;
    });

    this.calculatePercentages();
    this.generateChart();
  }

  /**
   * Place step into proper step map by status group
   */
  mapStepByStatus(index: number, step: any) {
    if (step.status.name === 'success') {
      this.deviationMap.stepMap[index].success.data.push(step);
    } else if (step.status.name === 'warning') {
      this.deviationMap.stepMap[index].warning.data.push(step);
    } else if (step.status.name === 'failed') {
      this.deviationMap.stepMap[index].failed.data.push(step);
    } else {
      this.deviationMap.stepMap[index].info.stats.empty.push(step);
      return;
    }
    this.deviationMap.stepMap[index].info.stats.count++;
  }

  /**
   * Calculate percentage values for each stepMap status range
   */
  calculatePercentages() {
    this.deviationMap.stepMap.map(map => {
      map.success.percentage = map.success.data.length / map.info.stats.count;
      map.warning.percentage = map.warning.data.length / map.info.stats.count;
      map.failed.percentage = map.failed.data.length / map.info.stats.count;
    });
  }

  /**
   *  Map stepMap by status group for chart
   */
  setRows() {
    const rows: any[] = [['Success', 'Warning', 'Failed'], []];
    const names = [];

    this.deviationMap.stepMap.forEach(map => {
      rows.push([map.success.percentage * 100, map.warning.percentage * 100, map.failed.percentage * 100]);
      names.push(map.info.labels.name);
    });

    return [rows, names];
  }

  /**
   * Generate c3js chart using data in deviationMap[]
   */
  generateChart() {
    const stepMap = this.stepMap;
    const data: any = this.setRows();
    const rows = data[0];
    const names = data[1];

    this.chart = c3.generate({
      bindto: '#chartStatus3',
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
            return 'Step ' + x + ': ' + names[x - 1];
          },
          value: function(value, ratio, id, index) {
            return (
              Math.round(value * 10) / 10 + '% (' + Math.round((value / 100) * stepMap[index].info.stats.count) + ')'
            );
          }
        }
      },
      axis: {
        rotated: true,
        x: {
          label: {
            text: 'Step Order (#)',
            position: 'outer-right'
          },
          min: 1,
          max: stepMap.length
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

      'Step Submission ID',
      'Step Name',
      'Step ID',
      'Step Start Time',
      'Step End Time',
      'Step Duration (m)',
      'Step Ending Status',

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
    this.dataToLoad.forEach(v => {
      const sub = v.submission;

      if (sub.steps.length > 0) {
        sub.steps.forEach(step => {
          const stepStart: any = new Date(step.startTime);
          const stepEnd: any = new Date(step.endTime);

          const data: SubmissionData = {
            id: sub.id,
            process: sub.process.name,
            adHoc: sub.adHoc ? sub.adHoc : false,
            startTime: this.datePipe.transform(new Date(sub.startTime), 'MM/dd/yyyy HH:mm z'),
            endTime: this.datePipe.transform(new Date(sub.endTime), 'MM/dd/yyyy HH:mm z'),
            duration: Math.round((v.duration / 60) * 10000) / 10000,
            status: sub.status ? sub.status.name : 'N/A',

            stepSubmissionId: step.id,
            stepName: step.processStep.name,
            stepId: step.processStep.id,
            stepStartTime: this.datePipe.transform(stepStart, 'MM/dd/yyyy HH:mm z'),
            stepEndTime: this.datePipe.transform(stepEnd, 'MM/dd/yyyy HH:mm z'),
            stepDuration: Math.round(((stepEnd - stepStart) / 60000) * 100) / 100,
            stepStatus: step.status ? step.status.name : 'N/A',

            total: this.deviationMap.info.stats.count,
            empty: this.deviationMap.info.stats.empty.length,

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
              '_STATUS_C2_Spotlight'
          };

          this.submissions.push(data);
        });
      } else {
        const data: SubmissionData = {
          id: sub.id,
          process: sub.process.name,
          adHoc: sub.adHoc ? sub.adHoc : false,
          startTime: this.datePipe.transform(new Date(sub.startTime), 'MM/dd/yyyy HH:mm z'),
          endTime: this.datePipe.transform(new Date(sub.endTime), 'MM/dd/yyyy HH:mm z'),
          duration: Math.round((v.duration / 60) * 10000) / 10000,
          status: sub.status ? sub.status.name : 'N/A',

          stepSubmissionId: 0,
          stepName: 'N/A',
          stepId: 0,
          stepStartTime: 'N/A',
          stepEndTime: 'N/A',
          stepDuration: 0,
          stepStatus: 'N/A',

          total: this.deviationMap.info.stats.count,
          empty: this.deviationMap.info.stats.empty.length,

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
            '_STATUS_C2_Spotlight'
        };

        this.submissions.push(data);
      }
    });
  }
}
