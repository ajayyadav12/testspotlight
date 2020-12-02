import { Component, OnInit, Input, AfterViewInit } from '@angular/core';
import { GoogleCharts } from 'google-charts';
import { SubmissionsService } from '../../reports/submissions/submissions.service';
import { ActivatedRoute } from '@angular/router';
const CHART_ID = 'line_chart';
@Component({
  selector: 'app-dashboard-trending-chart',
  templateUrl: './dashboard-trending-chart.component.html',
  styleUrls: ['./dashboard-trending-chart.component.scss']
})
export class DashboardTrendingChartComponent implements OnInit, AfterViewInit {
  @Input() uniqueId = '';
  isShowingLineChart = true;
  dataFound = true;
  submissionStats = [];
  days = 7;
  params;

  constructor(private submissionSvc: SubmissionsService, private activatedRoute: ActivatedRoute) {}

  ngOnInit() {}

  ngAfterViewInit(): void {
    this.activatedRoute.queryParams.subscribe(params => {
      this.days = params.days ? params.days : this.days;
      this.getSubmissionCount(params);
    });
  }

  getSubmissionCount(params: any = { days: this.days }) {
    this.params = this.getUpdatedParams(params);
    this.submissionSvc.getSubmissionCount(this.params).subscribe((value: any[]) => {
      this.dataFound = value.length > 0;
      if (!this.dataFound) return;
      this.submissionStats = value;
      GoogleCharts.load(
        _ => {
          this.drawChart();
        },
        { packages: ['line', 'bar'] }
      );
    });
  }

  getUpdatedParams(_params) {
    let params: any = {};
    Object.assign(params, _params);
    switch (params.level) {
      case 'PR':
        params.parentId = null;
        params.receiver = null;
        params.sender = null;
        break;
      case 'PA':
        params.childId = null;
        params.receiver = null;
        params.sender = null;
        break;
      case 'SR':
        params.childId = null;
        params.parentId = null;
        break;
    }

    return {
      days: params.days ? params.days : this.days,
      childId: params.childId ? params.childId : '-1,',
      parentId: params.parentId ? params.parentId : '-1,',
      receiver: params.receiver ? params.receiver : '-1,',
      sender: params.sender ? params.sender : '-1,',
      adHoc: params.adHoc ? params.adHoc : '-1',
      bu: params.bu ? params.bu : '-1'
    };
  }

  drawChart() {
    this.isShowingLineChart = true;
    let container = document.getElementById(CHART_ID + this.uniqueId);
    let chart = new GoogleCharts.api.charts.Line(container);
    let dataTable = new GoogleCharts.api.visualization.DataTable();

    dataTable.addColumn('date', 'Timeline');
    dataTable.addColumn('number', 'Failed');
    dataTable.addColumn('number', 'Warning');
    dataTable.addColumn('number', 'Long Running');
    dataTable.addColumn('number', 'Delayed');

    this.submissionStats.forEach(ss => {
      const failed = ss[1];
      const warning = ss[2];
      const long = ss[3];
      const delayed = ss[4];
      dataTable.addRows([[new Date(ss[0]), failed, warning, long, delayed]]);
    });

    let options = {
      chart: {
        subtitle: `Last ${this.days} days`
      },
      hAxis: {
        title: 'Date'
      },
      vAxis: {
        title: 'Count'
      },
      chartArea: { left: 50, top: 30, width: '80%' },
      height: '380',
      colors: ['#f44336', '#ffc107', '#2196f3', 'gray']
    };

    GoogleCharts.api.visualization.events.addListener(chart, 'select', _ => {
      let status;
      let selection = chart.getSelection()[0];
      if (!selection) return;
      switch (selection.column) {
        case 1:
          status = 'failed';
          break;
        case 2:
          status = 'warning';
          break;
        case 3:
          status = 'long';
          break;
        case 4:
          status = 'delayed';
          break;
      }

      this.submissionSvc.getSubmissionCountByProcess(this.params, status).subscribe((value: any[]) => {
        this.dataFound = value.length > 0;
        if (!this.dataFound) {
          return;
        }
        this.drawProcessBarChart(value);
      });
    });

    chart.draw(dataTable, options);
  }

  drawProcessBarChart(value: any[]) {
    this.isShowingLineChart = false;
    let container = document.getElementById(CHART_ID + this.uniqueId);
    let chart = new GoogleCharts.api.charts.Bar(container);
    let dataTable = new GoogleCharts.api.visualization.DataTable();

    dataTable.addColumn('date', 'Date');

    // Get processes for dynamic columns
    let processes = [];
    value.forEach(v => {
      if (!processes.includes(v[1])) processes.push(v[1]);
    });

    // Row Default values
    let initialize = [];
    processes.forEach(p => {
      initialize.push(0);
      dataTable.addColumn('number', p);
    });

    // Actual values mapped in Chart format
    const rows = [];
    value.forEach(v => {
      let result = [new Date(v[0])];
      result.push(...initialize);
      // Update
      let index = processes.indexOf(v[1]) + 1;
      result[index] = v[2];
      rows.push(result);
    });

    // Values grouped by date
    const reduced = rows.reduce((m, d) => {
      if (!m[d[0]]) {
        m[d[0]] = [...d];
        return m;
      }
      m[d[0]][0] = new Date(d[0]);
      for (let index = 1; index <= processes.length; index++) {
        m[d[0]][index] += d[index];
      }
      return m;
    }, {});

    Object.keys(reduced).forEach(key => {
      const temp = reduced[key];
      dataTable.addRows([temp]);
    });

    let options = {
      height: '340',
      isStacked: true
    };

    chart.draw(dataTable, GoogleCharts.api.charts.Bar.convertOptions(options));
  }
}
