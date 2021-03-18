import { Component, OnInit } from '@angular/core';
import { MessageService } from 'primeng/api';
import { AuditLogService } from 'src/app/core/services/audit-log.service';
import { ProcessService } from '../../process/process.service';
import { ScheduleService } from '../schedule.service';
import { SidebarService } from './../../../../core/sidebar/sidebar.service';
import * as moment from 'moment-timezone';

const TODAY = new Date();

@Component({
	selector: 'app-schedule-list',
	templateUrl: './schedule-list.component.html',
	styleUrls: ['./schedule-list.component.scss'],
	providers: [ProcessService, AuditLogService]
})
export class ScheduleListComponent implements OnInit {
	newScheduleDialog: boolean;
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
		{ field: 'startTime', header: 'Start Time(EST)' },
		{ field: 'endTime', header: 'End Time(EST)' },
		{ field: 'tolerance', header: 'Tolerance' },
		{ field: 'recurrencePatternName', header: 'Recurrence Pattern' },
		{ field: 'active', header: 'Active' }
	];
	loading = false;
	constructor(
		private scheduleSvc: ScheduleService,
		private processSvc: ProcessService,
		private msgSvc: MessageService,
		private sidebarSvc: SidebarService,
		private auditLogSvc: AuditLogService
	) {
		this.auditLogSvc.newAuditLog('Scheduled Submissions List').subscribe((value) => { });
	}

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
			case 'E':
				result = 'Predecessor';
				break;
		}
		return result;
	}

	getSchedules(id) {
		this.loading = true;
		moment.tz.setDefault('America/New_York');
		this.scheduleSvc.getSchedules(id).subscribe((value) => {
			this.schedules = value;
			this.sidebarSvc.title = 'Schedule: ' + this.processes.find((x) => x.value === id).label;
			this.schedules.map((s) => {
				s.recurrencePatternName = this.getRecurrencePattern(s.recurrencePattern);
				s.scheduleStartDate = new Date(s.scheduleStartDate).toDateString();
				s.scheduleEndDate = new Date(s.scheduleEndDate).toDateString();

				if (s.recurrencePattern !== 'C') {
					let startTime = moment(new Date(s.startTime));
					let endTime = moment(new Date(s.endTime));

					s.startTime = startTime.tz('America/New_York').format('MM/DD/YY hh:mm a');
					s.endTime = endTime.tz('America/New_York').format('MM/DD/YY hh:mm a');

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
			this.processes = value.filter((x) => !x.isParent).map((p) => {
				return { label: p.name, value: p.id };
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
		this.scheduleSvc.deleteSchedule(this.processId, id).subscribe((value) => {
			this.schedules = this.schedules.filter((p) => {
				return p.id !== id;
			});
			this.msgSvc.add({
				severity: 'error',
				summary: `It's not me, it's you!`,
				detail: `Schedule was deleted`
			});
		});
	}

	openNewScheduleDialog() {
		this.newScheduleDialog = true;
	}
}
