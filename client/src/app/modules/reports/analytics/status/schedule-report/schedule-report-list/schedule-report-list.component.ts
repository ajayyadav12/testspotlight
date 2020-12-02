import { Component, OnInit } from '@angular/core';
import { MessageService } from 'primeng/api';
import { AnalyticsService } from '../../../analytics.service';
import { ProcessService } from 'src/app/modules/admin/process/process.service';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-schedule-report-list',
  templateUrl: './schedule-report-list.component.html',
  styleUrls: ['./schedule-report-list.component.scss']
})
export class ScheduleReportListComponent implements OnInit {
  loading = true;
  schedules;

  processes = [];

  columns = [
    { field: 'id', header: 'ID' },
    { field: 'processName', header: 'Process' },
    { field: 'parent', header: 'Parent' },
    { field: 'range', header: 'Data Range' },
    { field: 'scheduleRange', header: 'Schedule Range' },
    { field: 'recurrence', header: 'Recurrence' },
    { field: 'additionalEmails', header: 'Emails' },
    { field: '', header: '' }
  ];

  constructor(
    private analyticsSvc: AnalyticsService,
    private processSvc: ProcessService,
    private msgSvc: MessageService,
    private datePipe: DatePipe
  ) {
    this.analyticsSvc.getAllScheduledReports().subscribe((schedules: any[]) => {
      this.schedules = schedules;
      this.getMatchOptions();
    });
  }

  ngOnInit() {}

  getMatchOptions() {
    this.processSvc.getAllProcesses(true).subscribe(processes => {
      this.processes = processes;
      this.matchSchedules();
      this.loading = false;
    });
  }

  matchSchedules() {
    this.schedules.map(sch => {
      const process = this.matchProcess(sch.processId);

      sch.processName = process.name;
      sch.parent = process.isParent ? 'Yes' : '';
      sch.range = sch.rangeLength + ' days';
      sch.scheduleRange =
        new Date(sch.endDate).getFullYear() === 2099
          ? 'Starting ' + this.datePipe.transform(new Date(sch.startDate), 'M/dd/yy')
          : this.datePipe.transform(new Date(sch.startDate), 'M/dd/yy') +
            ' to ' +
            this.datePipe.transform(new Date(sch.endDate), 'M/dd/yy');
      sch.recurrence = sch.recurrencePattern === 'M' ? 'Monthly' : sch.recurrencePattern;
    });
  }

  matchProcess(id): any {
    let process = null;

    this.processes.forEach(p => {
      process = p.id === id ? p : process;
    });

    return process;
  }

  deleteSchedule(report) {
    this.loading = true;
    this.analyticsSvc.deleteScheduledReport(report.id, report.processId).subscribe(value => {
      this.schedules = this.schedules.filter(s => {
        return s.id !== report.id;
      });
      this.loading = false;
      this.msgSvc.add({
        severity: 'success',
        summary: 'Welp!',
        detail: `Your report schedule has been deleted.`
      });
    });
  }
}
