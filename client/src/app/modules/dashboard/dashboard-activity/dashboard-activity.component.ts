import { Component, OnInit, Input, ViewChild } from '@angular/core';
import { ScheduleSubmissionsService } from '../../admin/schedule/schedule-submissions.service';
import { GENotes } from 'src/app/shared/Components/GENotes';
import { SessionService } from 'src/app/core/session/session.service';
import { MessageService, SelectItem } from 'primeng/api';
import { ActivatedRoute } from '@angular/router';
import { GeUpdateScheduledSubmissionComponent } from 'src/app/shared/Components/ge-update-scheduled-submission/ge-update-scheduled-submission.component';
import * as moment from 'moment-timezone';
import * as Chart from 'chart.js';
import { DatePipe } from '@angular/common';

@Component({
	selector: 'app-dashboard-activity',
	templateUrl: './dashboard-activity.component.html',
	styleUrls: [ './dashboard-activity.component.scss' ]
})
export class DashboardActivityComponent implements OnInit {
	@Input() isLightVersion: boolean;
	@Input() editNotes: string;
	@Input() uniqueId = '';
	@ViewChild('updateScheduledSubmission', { static: true })
	updateScheduledSubmissionComponent: GeUpdateScheduledSubmissionComponent;

	scheduledSubmissions;
	dataFound = true;
	displayAcknowledgmentDialog = false;
	acknowledgementData: GENotes;
	displayDisabledDialog = false;
	disableNoteData: GENotes;
	editNoteData: GENotes;
	loading = false;
	displayType = 2;
	display = false;
	displayTableDialog = false;
	submissionList = true;
	params;
	columns = [
		{ header: 'ID', field: 'id', width: '4%' },
		{ header: 'Process', field: 'processName', width: '12%' },
		{ header: 'Planned Start', field: 'start', width: '9%' },
		{ header: 'Planned End', field: 'end', width: '9%' }
	];
	types: SelectItem[] = [
		{ label: 'Table', value: 0, icon: 'pi pi-table' },
		{ label: 'Calendar', value: 1, icon: 'pi pi-calendar' },
		{ label: 'Chart', value: 2 }
	];

	get menuItems() {
		if (this.sessionSvc.role !== 'user') {
			return [ { label: 'Acknowledge Delay' }, { label: 'Disable submission' }, { label: 'Edit submission' } ];
		} else {
			return null;
		}
	}
	constructor(
		private msgSvc: MessageService,
		private sessionSvc: SessionService,
		private scheduleSubmissionsSvc: ScheduleSubmissionsService,
		private activatedRoute: ActivatedRoute,
		private datePipe: DatePipe
	) {}

	ngAfterViewInit(): void {
		this.activatedRoute.queryParams.subscribe((params) => {
			if (!(params.childId || params.parentId || params.receiver || params.sender)) {
				this.getScheduledSubmissions();
			} else {
				this.getFilteredScheduleSubmission(params);
			}
		});
		const tab = localStorage.getItem('dashboard-submission-tab');
		if (tab) {
			this.updateTab({ value: tab });
		}
	}

	drawScatterChart() {
		const chart: any = document.getElementById('activity_scatter_chart' + this.uniqueId);
		const dayLabels = [];
		for (let i = 0; i <= 14; i++) {
			let day = new Date();
			day.setDate(day.getDate() + i);

			dayLabels.push(this.datePipe.transform(day, 'MMMM d'));
		}

		const amData = [];
		const pmData = [];
		this.scheduledSubmissions.forEach((sched) => {
			const date = this.datePipe.transform(sched.startTime, 'MMMM d');
			const hours = new Date(sched.startTime).getHours();
			if (hours < 12) {
				amData.push({ x: date, y: hours });
			} else {
				pmData.push({ x: date, y: hours });
			}
		});

		new Chart(chart.getContext('2d'), {
			type: 'scatter',
			data: {
				labels: dayLabels,
				datasets: [
					{
						label: 'AM',
						backgroundColor: '#9fcc9f',
						data: amData
					},
					{
						label: 'PM',
						data: pmData
					}
				]
			},
			options: {
				responsive: true,
				maintainAspectRatio: false,
				title: {
					display: false,
					text: 'Last'
				},
				tooltips: {
					callbacks: {
						label: (node) => {
							const sched = this.scheduledSubmissions.find(
								(x) => new Date(x.startTime).getHours().toString() === node.value
							);
							return `Start: ${this.datePipe.transform(
								sched.startTime,
								'short'
							)}, End: ${this.datePipe.transform(sched.endTime, 'short')}`;
						},
						title: (node) => {
							const sched = this.scheduledSubmissions.find(
								(x) => new Date(x.startTime).getHours().toString() === node[0].value
							);
							return sched.processName;
						}
					}
				},
				scales: {
					gridLines: {
						display: false
					},
					xAxes: [
						{
							type: 'category',
							position: 'bottom'
						}
					],
					yAxes: [
						{
							ticks: {
								stepSize: 2,
								min: 0,
								max: 24,
								callback: (v) => {
									return v + ':00';
								}
							}
						}
					]
				}
			}
		});
	}

	getFilteredScheduleSubmission(params: any = { scheduledSubmissions: this.scheduledSubmissions }) {
		this.params = {
			childId: params.childId ? params.childId : '-1,',
			parentId: params.parentId ? params.parentId : '-1,',
			receiver: params.receiver ? params.receiver : '-1,',
			sender: params.sender ? params.sender : '-1,'
		};
		this.loading = true;
		this.scheduleSubmissionsSvc.getScheduleSubmissions(this.params).subscribe((value: any[]) => {
			this.dataFound = value.length > 0;
			if (!this.dataFound) {
				this.submissionList = false;
				return;
			}
			this.scheduledSubmissions = value;
			this.scheduledSubmissions.map((s) => {
				this.scheduleSubmissionMapping(s);
			});
			this.drawScatterChart();
			this.loading = false;
		});
	}

	ngOnInit() {}

	showDialog(event) {
		this.display = false;
	}

	updateTab(event) {
		this.displayType = Number.parseFloat(event.value);
		localStorage.setItem('dashboard-submission-tab', event.value);
	}

	onClickMenuOption(event) {
		const session = JSON.parse(localStorage.getItem('session'));
		const row = event.value;
		if (event.item === 'Disable submission') {
			this.disableNoteData = {
				id: row.id,
				note: row.disabledNote,
				flag: row.disabled,
				date: null,
				name: null
			};
			this.displayDisabledDialog = true;
		} else if (event.item === 'Acknowledge Delay') {
			this.displayAcknowledgmentDialog = true;
			this.acknowledgementData = {
				id: row.id,
				note: row.acknowledgementNote,
				flag: row.acknowledgementFlag,
				date: row.acknowledgementDate,
				name: row.acknowledgedBy !== null ? row.acknowledgedBy : session.user.name
			};
		} else if (event.item === 'Edit submission') {
			const startTime = new Date(row.startTime);
			const endTime = new Date(row.endTime);
			this.editNoteData = {
				id: row.id,
				note: row.editNotes,
				flag: null,
				date: null,
				name: null
			};
			this.updateScheduledSubmissionComponent.setValue({
				startTime: startTime,
				endTime: endTime,
				editNotes: this.editNoteData.note
			});
			this.updateScheduledSubmissionComponent.toggle();
			this.updateScheduledSubmissionComponent.schedSubmissionID = row.id;
		}
	}

	getScheduledSubmissions() {
		const date1 = new Date(new Date().setDate(new Date().getDate())).toISOString().split('T')[0];
		const date2 = new Date(new Date().setDate(new Date().getDate() + 90)).toISOString().split('T')[0];
		this.loading = true;
		this.scheduleSubmissionsSvc.getExpectedSubmissions(date1, date2).subscribe((value: any[]) => {
			this.scheduledSubmissions = value;
			this.scheduledSubmissions.map((s) => {
				this.scheduleSubmissionMapping(s);
			});
			this.submissionList = true;
			this.drawScatterChart();
			this.loading = false;
		});
	}

	scheduleSubmissionMapping(s) {
		moment.tz.setDefault('America/New_York');
		let startMoment = moment(s.startTime);
		let endMoment = moment(s.endTime);

		s.startTime = startMoment.tz('America/New_York').format('MM/DD/YY h:m a');
		s.endTime = endMoment.tz('America/New_York').format('MM/DD/YY h:m a');

		const now = new Date();
		s.processName = s.process === null ? s.procesName : s.process.name;
		s.start = new Date(s.startTime).toLocaleString();
		const delayedTime = new Date(s.startTime);
		delayedTime.setMinutes(delayedTime.getMinutes() + 15 + s.tolerance);
		s.end = new Date(s.endTime).toLocaleString();
		s.cursor = 'pointer';
		if (!s.disabled) {
			const isDelayed = !s.acknowledgementFlag && now.getTime() > delayedTime.getTime();
			s.backgroundColor = isDelayed ? '#f080805c' : null;
			s.tooltipValue = isDelayed ? 'Delayed' : null;
		}
		s.id = s.acknowledgementFlag ? s.id : s.id;
		s.tooltipValue = s.disabled ? 'Disabled' : s.tooltipValue;
		s.color = s.disabled ? 'lightsteelblue' : null;
	}

	setAcknowledgementFlag(noteValue) {
		this.scheduleSubmissionsSvc
			.setAcknowledgementFlag(this.acknowledgementData.id, noteValue, this.acknowledgementData.name)
			.subscribe((value) => {
				// Update submission
				const index = this.scheduledSubmissions.findIndex((x) => {
					return x.id === this.acknowledgementData.id;
				});
				this.scheduledSubmissions[index] = value;
				this.scheduleSubmissionMapping(this.scheduledSubmissions[index]);
				this.displayAcknowledgmentDialog = false;
				this.acknowledgementData = null;
				this.msgSvc.add({
					severity: 'success',
					summary: 'Acknowledge flag set!',
					detail: `Now everybody knows you took care of the issue. Good job!`
				});
			});
	}

	onUpdateSubmission(value) {
		const index = this.scheduledSubmissions.findIndex((x) => {
			return x.id === value.id;
		});
		this.scheduleSubmissionMapping(value);
		this.scheduledSubmissions[index] = value;
		this.updateScheduledSubmissionComponent.toggle();
		this.msgSvc.add({
			severity: 'success',
			summary: 'Single ocurrence updated!',
			detail: `All is set. Good luck!`
		});
	}

	onSubmitDisabledNote(noteValue) {
		this.scheduleSubmissionsSvc.setDisable(this.disableNoteData.id, noteValue).subscribe(
			(value) => {
				const index = this.scheduledSubmissions.findIndex((x) => {
					return x.id === this.disableNoteData.id;
				});
				this.scheduledSubmissions[index] = value;
				this.scheduleSubmissionMapping(this.scheduledSubmissions[index]);
				this.disableNoteData = null;
				this.displayDisabledDialog = false;
				this.msgSvc.add({
					severity: 'success',
					summary: 'Disabled flag set!',
					detail: `You make the world a better place. Good job!`
				});
			},
			(err) => {
				this.displayDisabledDialog = false;
			}
		);
	}

	openTableDialog(scheduledSubmissions) {
		this.displayTableDialog = true;
	}
}
