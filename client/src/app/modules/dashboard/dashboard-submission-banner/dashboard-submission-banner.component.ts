import { Component, OnInit, Input, Output, EventEmitter, AfterViewInit } from '@angular/core';
import { GoogleCharts } from 'google-charts';
import { SubmissionsService } from '../../reports/submissions/submissions.service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-dashboard-submission-banner',
  templateUrl: './dashboard-submission-banner.component.html',
  styleUrls: ['./dashboard-submission-banner.component.scss']
})
export class DashboardSubmissionBannerComponent implements OnInit, AfterViewInit {
  @Input() uniqueId = '';
  @Output() displayDrillDownDialog = new EventEmitter<boolean>();

  constructor(private submissionSvc: SubmissionsService, private activatedRoute: ActivatedRoute) { }
  days = 7;
  params;
  isShowingLineChart = true;
  dataFound = true;

  ngOnInit() { }

  ngAfterViewInit(): void {
    this.activatedRoute.queryParams.subscribe(params => {
      this.days = params.days ? params.days : this.days;
      this.getSubmissionCount(params);
    });
  }

  getSubmissionCount(params: any = { days: this.days }) {
    this.params = this.getUpdatedParams(params);

    this.submissionSvc.getSubmissionCount(this.params).subscribe(value => {
      GoogleCharts.load(
        _ => {
          this.drawChartFailed(value);
          this.drawChartInProgress(value);
          this.drawChartInDelayed(value);
          this.drawChartInWarning(value);
          this.drawChartInSuccess(value);
        },
        { packages: ['corechart'] }
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

  drawChartFailed(submissionStats: any[]) {
    let chart = new GoogleCharts.api.visualization.PieChart(
      document.getElementById('bonus_chart_failed' + this.uniqueId)
    );
    let dataTable = new GoogleCharts.api.visualization.DataTable();
    var failed = 0;
    var unacknowledged = 0;
    var textSize = 15;
    var textTitle = 'Failed vs Unaknowledged';

    submissionStats.forEach(ss => {
      failed = failed + ss[1];
      unacknowledged = unacknowledged + ss[6];
    });

    dataTable = GoogleCharts.api.visualization.arrayToDataTable([
      ['Task', 'Status'],
      ['Failed', failed - unacknowledged],
      ['Unacknowledged', unacknowledged]
    ]);

    if (unacknowledged == 0) {
      textSize = 50;
      textTitle = 'Failed';
    }

    var options = {
      title: textTitle,
      pieSliceText: 'value',
      pieSliceTextStyle: { fontSize: textSize },
      titleTextStyle: { fontSize: 15 },
      slices: {
        0: { color: '#f44336' },
        1: { color: '#E78C3F' }
      },
      is3D: true,
      legend: 'none',
      width: 180,
      height: 200
    };

    chart.draw(dataTable, options);

    GoogleCharts.api.visualization.events.addListener(chart, 'select', _ => {
      this.changeMousePointerOut();
      this.showDialog(1);
    });

    GoogleCharts.api.visualization.events.addListener(chart, 'onmouseover', _ => {
      this.changeMousePointerOn();
    });

    GoogleCharts.api.visualization.events.addListener(chart, 'onmouseout', _ => {
      this.changeMousePointerOut();
    });
  }

  drawChartInProgress(submissionStats: any[]) {
    let chart = new GoogleCharts.api.visualization.PieChart(
      document.getElementById('bonus_chart_inprogress' + this.uniqueId)
    );
    let dataTable = new GoogleCharts.api.visualization.DataTable();
    var inprogress = 0;
    var longrunning = 0;
    var textSize = 15;
    var textTitle = 'In Progress vs Long Running';

    submissionStats.forEach(ss => {
      inprogress = inprogress + ss[7];
      longrunning = longrunning + ss[3];
    });

    dataTable = GoogleCharts.api.visualization.arrayToDataTable([
      ['Task', 'Status'],
      ['In Progress', inprogress - longrunning],
      ['Long Running', longrunning]
    ]);

    if (longrunning == 0) {
      textSize = 50;
      textTitle = 'In Progress';
    }

    var options = {
      title: textTitle,
      pieSliceText: 'value',
      pieSliceTextStyle: { fontSize: textSize },
      titleTextStyle: { fontSize: 15 },
      slices: {
        0: { color: '#027ad9' },
        1: { color: 'gray' }
      },
      is3D: true,
      legend: 'none',
      width: 180,
      height: 200
    };

    chart.draw(dataTable, options);

    GoogleCharts.api.visualization.events.addListener(chart, 'select', _ => {
      this.changeMousePointerOut();
      this.showDialog(2);
    });
    GoogleCharts.api.visualization.events.addListener(chart, 'onmouseover', _ => {
      this.changeMousePointerOn();
    });

    GoogleCharts.api.visualization.events.addListener(chart, 'onmouseout', _ => {
      this.changeMousePointerOut();
    });
  }
  drawChartInDelayed(submissionStats: any[]) {
    let chart = new GoogleCharts.api.visualization.PieChart(
      document.getElementById('bonus_chart_delayed' + this.uniqueId)
    );
    let dataTable = new GoogleCharts.api.visualization.DataTable();
    var delayed = 0;
    var unacknowledged = 0;
    var textSize = 15;
    var textTitle = 'Delayed vs Unacknowledged';

    submissionStats.forEach(ss => {
      delayed = delayed + ss[4];
      unacknowledged = unacknowledged + ss[8];
    });

    dataTable = GoogleCharts.api.visualization.arrayToDataTable([
      ['Task', 'Status'],
      ['Delayed', delayed],
      ['Unacknowledged', unacknowledged]
    ]);

    if (unacknowledged == 0 || delayed == 0) {
      textSize = 50;
      textTitle = 'Delayed';
    }

    var options = {
      title: textTitle,
      pieSliceText: 'value',
      pieSliceTextStyle: { fontSize: textSize },
      titleTextStyle: { fontSize: 15 },
      slices: {
        0: { color: 'gray' },
        1: { color: '#F17D1B' }
      },
      is3D: true,
      legend: 'none',
      width: 180,
      height: 200
    };

    chart.draw(dataTable, options);

    GoogleCharts.api.visualization.events.addListener(chart, 'select', _ => {
      this.changeMousePointerOut();
      this.showDialog(3);
    });

    GoogleCharts.api.visualization.events.addListener(chart, 'onmouseover', _ => {
      this.changeMousePointerOn();
    });

    GoogleCharts.api.visualization.events.addListener(chart, 'onmouseout', _ => {
      this.changeMousePointerOut();
    });
  }

  drawChartInWarning(submissionStats: any[]) {
    let chart = new GoogleCharts.api.visualization.PieChart(
      document.getElementById('bonus_chart_warning' + this.uniqueId)
    );
    let dataTable = new GoogleCharts.api.visualization.DataTable();
    var warning = 0;

    submissionStats.forEach(ss => {
      warning = warning + ss[2];
    });

    dataTable = GoogleCharts.api.visualization.arrayToDataTable([['Task', 'Status'], ['warning', warning]]);

    var options = {
      title: 'Warning',
      pieSliceText: 'value',
      pieSliceTextStyle: { fontSize: 50 },
      titleTextStyle: { fontSize: 15 },
      slices: { 0: { color: '#ffa600' } },
      is3D: true,
      legend: 'none',
      width: 180,
      height: 200
    };

    chart.draw(dataTable, options);

    GoogleCharts.api.visualization.events.addListener(chart, 'select', _ => {
      this.changeMousePointerOut();
      this.showDialog(4);
    });

    GoogleCharts.api.visualization.events.addListener(chart, 'onmouseover', _ => {
      this.changeMousePointerOn();
    });

    GoogleCharts.api.visualization.events.addListener(chart, 'onmouseout', _ => {
      this.changeMousePointerOut();
    });
  }

  drawChartInSuccess(submissionStats: any[]) {
    let chart = new GoogleCharts.api.visualization.PieChart(
      document.getElementById('bonus_chart_success' + this.uniqueId)
    );
    let dataTable = new GoogleCharts.api.visualization.DataTable();
    var success = 0;

    submissionStats.forEach(ss => {
      success = success + ss[5];
    });

    dataTable = GoogleCharts.api.visualization.arrayToDataTable([['Task', 'Status'], ['Success', success]]);

    var options = {
      title: 'Success',
      pieSliceText: 'value',
      pieSliceTextStyle: { fontSize: 50 },
      titleTextStyle: { fontSize: 15 },
      slices: { 0: { color: '#00bf6f' } },
      is3D: true,
      legend: 'none',
      width: 180,
      height: 200
    };


    chart.draw(dataTable, options);

    GoogleCharts.api.visualization.events.addListener(chart, 'select', _ => {
      this.changeMousePointerOut();
      this.showDialog(5);
    });

    GoogleCharts.api.visualization.events.addListener(chart, 'onmouseover', _ => {
      this.changeMousePointerOn();
    });

    GoogleCharts.api.visualization.events.addListener(chart, 'onmouseout', _ => {
      this.changeMousePointerOut();
    });
  }

  showDialog(value) {
    this.displayDrillDownDialog.emit(value);
  }

  changeMousePointerOn() {
    document.body.style.cursor = 'pointer';
  }

  changeMousePointerOut() {
    document.body.style.cursor = 'initial';
  }
}
