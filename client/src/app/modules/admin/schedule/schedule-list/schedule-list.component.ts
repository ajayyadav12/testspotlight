import { SidebarService } from './../../../../core/sidebar/sidebar.service';
import { Component, OnInit } from '@angular/core';
import { ScheduleService } from '../schedule.service';
import { ProcessService } from '../../process/process.service';
import { MessageService } from 'primeng/api';
import { AuditLogService } from 'src/app/core/services/audit-log.service';

const TODAY = new Date();

@Component({
  selector: 'app-schedule-list',
  templateUrl: './schedule-list.component.html',
  styleUrls: ['./schedule-list.component.scss'],
  providers: [ProcessService, AuditLogService]
})
export class ScheduleListComponent implements OnInit {
  get hasProcesses(): boolean {
    return this.processes.length > 0;
  }
  schedules: any[];
  processId;
  processes = [];
  columns = [
    { field: 'id', header: 'ID' },
    { field: 'scheduleStartDate', header: 'Start Date' },
    { field: 'scheduleEndDate', header: 'End Date' },
    { field: 'startTime', header: 'Start Time' },
    { field: 'endTime', header: 'End Time' },
    { field: 'tolerance', header: 'Tolerance' },
    { field: 'recurrencePatternName', header: 'Recurrence Pattern' }
  ];
  loading = false;
  constructor(
    private scheduleSvc: ScheduleService,
    private processSvc: ProcessService,
    private msgSvc: MessageService,
    private sidebarSvc: SidebarService,
    private auditLogSvc: AuditLogService
  ) { this.auditLogSvc.newAuditLog('Scheduled Submissions List').subscribe(value => { }); }

  ngOnInit() {
    this.setupProcessDropdown();
  }

  getRecurrencePattern(rp) {
    let result = '';
    switch (rp) {
      case 'D':
        result = 'Daily';
        break;
      case 'W':
        result = 'Weekly';
        break;
      case 'M':
        result = 'Monthly';
        break;
      case 'Y':
        result = 'Yearly';
        break;
      case 'C':
        result = 'Custom';
        break;
    }
    return result;
  }

  getSchedules(id) {
    this.loading = true;
    this.scheduleSvc.getSchedules(id).subscribe(value => {
      this.schedules = value;
      this.sidebarSvc.title = 'Schedule: ' + this.processes.find(x => x.value === id).label;
      this.schedules.map(s => {
        s.recurrencePatternName = this.getRecurrencePattern(s.recurrencePattern);
        s.scheduleStartDate = new Date(s.scheduleStartDate).toDateString();
        s.scheduleEndDate = new Date(s.scheduleEndDate).toDateString();

        if (s.recurrencePattern !== 'C') {
          const startTime = new Date(s.startTime);
          const endTime = new Date(s.endTime);

          s.startTime = new Date(
            TODAY.getFullYear(),
            TODAY.getMonth(),
            TODAY.getDate(),
            startTime.getHours(),
            startTime.getMinutes()
          ).toTimeString();

          s.endTime = new Date(
            TODAY.getFullYear(),
            TODAY.getMonth(),
            TODAY.getDate(),
            endTime.getHours(),
            endTime.getMinutes()
          ).toTimeString();
        } else {
          s.startTime = 'NA';
          s.endTime = 'NA';
        }
      });
      this.loading = false;
    });
  }

  setupProcessDropdown() {
    this.processSvc.getProcessList().subscribe((value: any[]) => {
      value.map(p => {
        if (!p.isParent) {
          this.processes.push({ label: p.name, value: p.id });
        }
      });
      if (this.processes.length) {
        const localProcessId = Number.parseInt(localStorage.getItem('scheduleProcessId'));
        this.processId = localProcessId ? localProcessId : this.processes[0].value;
        this.getSchedules(this.processId);
      }
    });
  }

  onChangeProcess(event) {
    this.getSchedules(event.value);
    localStorage.setItem('scheduleProcessId', event.value);
  }

  onDeleteRecord(id) {
    this.scheduleSvc.deleteSchedule(this.processId, id).subscribe(value => {
      this.schedules = this.schedules.filter(p => {
        return p.id !== id;
      });
      this.msgSvc.add({
        severity: 'error',
        summary: `It's not me, it's you!`,
        detail: `Schedule was deleted`
      });
    });
  }
}
