import { Component, OnInit, Input } from '@angular/core';
import * as c3 from 'c3';

@Component({
  selector: 'app-data-distribution',
  templateUrl: './data-distribution.component.html',
  styleUrls: ['./data-distribution.component.scss']
})
export class DataDistributionComponent implements OnInit {
  @Input() dataToLoad;
  @Input() reportInfo;

  deviationMap;
  chart;

  get ranges() {
    return [this.deviationMap.successful, this.deviationMap.warnings, this.deviationMap.errors];
  }

  constructor() {}

  ngOnInit() {
    this.prepareDeviationMap();
    this.mapDeviationData();
    this.generateChart();
  }

  /**
   * Prepare storage of data for chart, incoming submisisons and mapped calculations
   */
  prepareDeviationMap() {
    this.deviationMap = {
      info: this.reportInfo,
      totalSum: 0,
      successful: {
        percentage: 0,
        sum: 0,
        labels: {
          name: 'Successful'
        }
      },
      warnings: {
        percentage: 0,
        sum: 0,
        labels: {
          name: 'Warnings'
        }
      },
      errors: {
        percentage: 0,
        sum: 0,
        labels: {
          name: 'Errors'
        }
      }
    };
  }

  /**
   * Map each submission in dataToLoad[] to
   * corresponding deviation range in deviationMap[]
   */
  mapDeviationData() {
    this.dataToLoad.forEach(v => {
      this.deviationMap.totalSum += v.submission.records;

      this.deviationMap.successful.sum += v.submission.records - v.submission.warnings - v.submission.errors;
      this.deviationMap.warnings.sum += v.submission.warnings;
      this.deviationMap.errors.sum += v.submission.errors;
    });

    this.deviationMap.successful.percentage = this.deviationMap.successful.sum / this.deviationMap.totalSum;
    this.deviationMap.warnings.percentage = this.deviationMap.warnings.sum / this.deviationMap.totalSum;
    this.deviationMap.errors.percentage = this.deviationMap.errors.sum / this.deviationMap.totalSum;
  }

  /**
   * Generate c3js chart using data in deviationMap[]
   */
  generateChart() {
    const ranges = this.ranges;
    const totalRecords = this.deviationMap.totalSum;

    this.chart = c3.generate({
      bindto: '#chartStatus2',
      size: {
        height: 350
      },
      color: {
        pattern: ['#3c97e0', '#f2c752', '#7c7c7c']
      },
      data: {
        columns: [
          [ranges[0].labels.name, ranges[0].percentage * 100],
          [ranges[1].labels.name, ranges[1].percentage * 100],
          [ranges[2].labels.name, ranges[2].percentage * 100]
        ],
        type: 'bar',
        groups: [[ranges[0].labels.name, ranges[1].labels.name, ranges[2].labels.name]],
        labels: false
      },
      tooltip: {
        format: {
          title: function(x) {
            return 'Record Distribution';
          },
          value: function(value, ratio, id, index) {
            return Math.round(value * 10) / 10 + '% (' + Math.round((value / 100) * totalRecords) + ')';
          }
        }
      },
      axis: {
        x: {
          show: false
        },
        y: {
          label: 'Percentage (%)',
          max: 100,
          min: 10
        }
      }
    });
  }
}
