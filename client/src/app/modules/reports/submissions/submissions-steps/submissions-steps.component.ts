import { Component, OnInit, Input, Output, OnDestroy, ViewEncapsulation, EventEmitter } from '@angular/core';
import { SubmissionsService } from '../submissions.service';
import { DateCommon } from 'src/app/shared/DateCommon';
import { SubmissionCommon } from 'src/app/shared/SubmissionCommon';
import { GoogleCharts } from 'google-charts';
@Component({
  selector: 'app-submissions-steps',
  templateUrl: './submissions-steps.component.html',
  styleUrls: ['./submissions-steps.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class SubmissionsStepsComponent implements OnInit, OnDestroy {
  @Input() isLightVersion;
  @Input() autoRefreshOn;
  @Output() stepsRefresh = new EventEmitter();

  selectedStep;
  steps = [];
  activeIndex = 0;
  autorefreshHandler;
  lastUpdatedTime;
  data;
  displaySubmissionDialog = false;
  submissionNotes;

  displayNotesDialog = false;
  notesData: any;

  @Input() processId: 0;

  constructor(private submissionsSvc: SubmissionsService) {}

  ngOnInit() {
    this.getSubmissionSteps(true);

    this.autorefreshHandler = setInterval(() => {
      if (this.autoRefreshOn) {
        this.getSubmissionSteps(false);
      }
    }, 1000 * 30);
  }

  ngOnDestroy() {
    clearInterval(this.autorefreshHandler);
  }

  getSubmissionSteps(updateStep) {
    this.steps = [];
    this.submissionsSvc.getSubmissionSteps(this.processId).subscribe(values => {
      const allDates = [];
      values.map(ps => {
        this.steps.push({
          label: `${ps.processStep.name}`,
          status: ps.status.name,
          notes: ps.notes,
          styleClass: ps.status.name.replace(/ /g, ''),
          startTime: ps.startTime,
          endTime: ps.endTime,
          elapsedTime: DateCommon.dateDifference(ps.startTime, ps.endTime ? ps.endTime : new Date(), false)
        });
        allDates.push(new Date(ps.startTime).getTime());
        allDates.push(new Date(ps.endTime).getTime());
      });
      this.stepsRefresh.emit({ id: values[0].submissionId.toString() });
      if (updateStep) {
        this.activeIndex = values.length - 1;
        this.selectedStep = this.steps[values.length - 1];
      }
      // Get latest step start/end date.
      this.lastUpdatedTime = new Date(Math.max(...allDates));
      GoogleCharts.load(
        _ => {
          this.drawChart();
        },
        { packages: ['timeline'] }
      );
    });
  }

  onSelectStep(event) {
    this.selectedStep = this.steps[event];
  }

  openNotesDialog(step) {
    if (!this.isLightVersion) {
      this.notesData = step.notes;
      this.displayNotesDialog = true;
    }
  }
  minutesToMilliseconds(minutes) {
    return minutes * 60 * 1000;
  }

  drawChart() {
    var container = document.getElementById('gant_chart');
    var chart = new GoogleCharts.api.visualization.Timeline(container);
    var dataTable = new GoogleCharts.api.visualization.DataTable();
    var chartHeight = 350;
    let barColors = [];

    dataTable.addColumn({ type: 'string', id: 'Step' });
    dataTable.addColumn({ type: 'string', id: 'dummy bar label' });
    //    dataTable.addColumn({ type: 'string',  });
    dataTable.addColumn({ type: 'string', id: 'style', role: 'style' });
    dataTable.addColumn({ type: 'date', id: 'Start' });
    dataTable.addColumn({ type: 'date', id: 'End' });

    for (let i = 0; i < this.steps.length; i++) {
      //How to properly iterate here!!
      var date1 = new Date(this.steps[i].startTime);
      var iconNotes = '';
      var iconColor = '';

      if (this.steps[i].notes != null) {
        iconColor = '; stroke-color: #085402';
        iconNotes = ' *';
      }

      if (this.steps[i].endTime != null) {
        var date2 = new Date(this.steps[i].endTime);
      } else {
        var date2 = new Date();
      }

      var diff = date2.getTime() - date1.getTime();

      if (diff == 0) {
        date2.setTime(date2.getTime() + 1000);
      }

      dataTable.addRows([
        [
          this.steps[i].label,
          this.steps[i].status + iconNotes,
          'color: ' + SubmissionCommon.submissionStatusColor(this.steps[i].status) + iconColor,
          date1,
          date2
        ]
      ]);
    }

    if (this.steps.length == 1) {
      chartHeight = this.steps.length * 100;
    } else if (this.steps.length <= 5) {
      chartHeight = this.steps.length * 65;
    }

    var options = {
      height: chartHeight
    };

    GoogleCharts.api.visualization.events.addListener(chart, 'select', _ => {
      this.changeMousePointerOut();
      this.showDialog(chart.getSelection()[0].row);
    });

    GoogleCharts.api.visualization.events.addListener(chart, 'onmouseover', _ => {
      this.changeMousePointerOn();
    });

    GoogleCharts.api.visualization.events.addListener(chart, 'onmouseout', _ => {
      this.changeMousePointerOut();
    });

    chart.draw(dataTable, options);
  }

  showDialog(row) {
    if (!this.isLightVersion && this.steps[row].notes != null) {
      this.notesData = this.steps[row].notes;
      this.displayNotesDialog = true;
    }
  }

  changeMousePointerOn() {
    document.body.style.cursor = 'pointer';
  }

  changeMousePointerOut() {
    document.body.style.cursor = 'initial';
  }
}
