import { Component, OnInit } from '@angular/core';
import { ScheduleService } from '../../../../../../schedule/schedule.service';
import { ActivatedRoute } from '@angular/router';
import { MessageService } from 'primeng/api';
import { SessionService } from 'src/app/core/session/session.service';

const TODAY = new Date();

@Component({
  selector: 'app-process-dtl-critical-calendar-list',
  templateUrl: './process-dtl-critical-cal-list.component.html',
  styleUrls: ['./process-dtl-critical-cal-list.component.scss'],
  providers: [ScheduleService]
})
export class ProcessDtlCriticalCalendarListComponent implements OnInit {
  
  get hasProcesses(): boolean {
    return this.processes.length > 0;
  }
  
  get isAdmin(): boolean {
    return this.sessionService.role === 'admin';
  }

  get canEdit(): boolean {
    return this.sessionService.isUserOfProcess(this.processId);
  }
  
  schedules: any[];
  processId;
  scheduleId;
  processes = [];
  columns = [
    { field: 'id', header: 'ID' },
    { field: 'scheduleStartDate', header: 'Start Date' },
    { field: 'scheduleEndDate', header: 'End Date' },
    { field: 'settings', header: 'Recurrence' },
    { field: 'recurrencePatternName', header: 'Frecuency' },
    { field: 'active', header: 'Active' }
  ];
  loading = false;
  constructor(private scheduleSvc: ScheduleService,
    private route: ActivatedRoute,
    private msgSvc: MessageService,
    private sessionService: SessionService) { }

  ngOnInit() {
    this.route.parent.params.subscribe(params => {
      this.processId = params['id'] || 0;
      if (this.processId != 0) {
        this.getSchedules(this.processId);
      }
    });
  }

  getRecurrencePattern(rp) {
    let result = '';
    switch (rp) {
      case 'W':
        result = 'Weekly';
        break;
      case 'M':
        result = 'Monthly';
        break;
      case 'C':
        result = 'Custom';
        break;
    }
    return result;
  }

  getSchedules(id) {
    let lsettings;
    let daysOfWeek = [];
    this.loading = true;
    this.scheduleSvc.getCriticalSchedules(id).subscribe(value => {
      this.schedules = value;
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

        if (s.recurrencePattern == 'M') {
          lsettings = JSON.parse(s.settings);

          if (lsettings.option == 'day') {
            s.settings = lsettings.daysNumber + 'th of every ' + lsettings.monthsNumber + ' month(s)';
          } else if (lsettings.option == 'lcd') {
            s.settings = 'LCD' + lsettings.fromLCD + ' to ' + 'LCD' + lsettings.toLCD + ' of every ' + lsettings.monthsLCD + ' months';
          } else if (lsettings.option == 'the') {
            if (lsettings.ocurrence == 1) {
              s.settings = 'First ' + lsettings.day + ' of every ' + lsettings.every + ' month(s)';
            } else if (lsettings.ocurrence == 2) {
              s.settings = 'Second ' + lsettings.day + ' of every ' + lsettings.every + ' month(s)';
            } else if (lsettings.ocurrence == 3) {
              s.settings = 'Third ' + lsettings.day + ' of every ' + lsettings.every + ' month(s)';
            } else if (lsettings.ocurrence == 4) {
              s.settings = 'Forth ' + lsettings.day + ' of every ' + lsettings.every + ' month(s)';
            } else if (lsettings.ocurrence == 5) {
              s.settings = 'Fifth ' + lsettings.day + ' of every ' + lsettings.every + ' month(s)';
            }

          }

        } else if (s.recurrencePattern == 'W') {
          lsettings = JSON.parse(s.settings);
          daysOfWeek = lsettings.days;

          s.settings = '';

          if (daysOfWeek[0]) {
            s.settings = 'Mon/';
          }
          if (daysOfWeek[1]) {
            s.settings = s.settings + 'Tue/';
          }
          if (daysOfWeek[2]) {
            s.settings = s.settings + 'Wed/';
          }
          if (daysOfWeek[3]) {
            s.settings = s.settings + 'Thur/';
          }
          if (daysOfWeek[4]) {
            s.settings = s.settings + 'Fri/';
          }
          if (daysOfWeek[5]) {
            s.settings = s.settings + 'Sat/';
          }
          if (daysOfWeek[6]) {
            s.settings = s.settings + 'Sun';
          }

          if (lsettings.recurEvery == null) { lsettings.recurEvery = 0; }

          s.settings = s.settings + ' every ' + lsettings.recurEvery + ' week(s)';
        }
      });
      this.loading = false;
    });
  }

  onDeleteRecord(id) {
    this.scheduleSvc.deleteCriticalSchedule(this.processId, id).subscribe(value => {
      this.schedules = this.schedules.filter(p => {
        return p.id !== id;
      });
      this.msgSvc.add({
        severity: 'error',
        summary: `It's not me, it's you!`,
        detail: `Critical Period was deleted`
      });
    });
  }

  onEditRecord(id) {
    this.scheduleId = Number.parseInt(id);
    const schedule = this.schedules.find(x => x.id === this.scheduleId);
  }
}
