import { Component, OnInit, Input, ViewChild } from '@angular/core';
import { ScheduleSubmissionsService } from 'src/app/modules/admin/schedule/schedule-submissions.service';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';
import { GENotes } from 'src/app/shared/Components/GENotes';
import { MenuItem, MessageService } from 'primeng/api';
import { SessionService } from 'src/app/core/session/session.service';
import { Menu } from 'primeng/menu';
import { GeUpdateScheduledSubmissionComponent } from 'src/app/shared/Components/ge-update-scheduled-submission/ge-update-scheduled-submission.component';
import * as moment from 'moment-timezone';
import { Colors } from 'src/app/shared/Constants/Colors';
@Component({
	selector: 'app-submissions-calendar',
	templateUrl: './submissions-calendar.component.html',
	styleUrls: ['./submissions-calendar.component.scss']
})
export class SubmissionsCalendarComponent implements OnInit {
	options: any;
	@ViewChild('menu', { static: true })
	menu: Menu;
	@ViewChild('updateScheduledSubmission', { static: true })
	updateScheduledSubmissionComponent: GeUpdateScheduledSubmissionComponent;
	@Input() submissions: any[];
	@Input() submissionCalender: boolean;
	@Input() calendarViews = 'dayGridMonth,timeGridWeek,timeGridDay';
	expectedSubmissions = [];
	displayAcknowledgmentDialog = false;
	acknowledgementData: GENotes;
	displayDisabledDialog = false;
	disableNoteData: GENotes;
	editScheduleId = 0;
	items: MenuItem[];
	editNoteData: GENotes;
	selectedItem;
	loading = false;
	eventXPosition = 0;
	eventYPosition = 0;
	title;

	/**
   * Combine both dataset to return current and future submissions.
   */
	get calendarSubmission() {
		if (this.submissionCalender) {
			return {
				events: this.expectedSubmissions
			};
		} else {
			return {
				events: this.submissions.concat(this.expectedSubmissions)
			};
		}
	}

	constructor(
		private scheduleSubmissionsSvc: ScheduleSubmissionsService,
		private sessionSvc: SessionService,
		private msgSvc: MessageService
	) {}

	ngOnInit() {
		this.getExpectedSubmissions();
		this.setCalendarOptions();
		this.menuItems(event);
	}

	getExpectedSubmissions() {
		const date1 = new Date(new Date().setDate(new Date().getDate())).toISOString().split('T')[0];
		const date2 = new Date(new Date().setDate(new Date().getDate() + 90)).toISOString().split('T')[0];
		this.scheduleSubmissionsSvc.getExpectedSubmissions(date1, date2).subscribe((value: any[]) => {
			this.expectedSubmissions = value;
			this.expectedSubmissions.map(s => {
				this.scheduleSubmissionMapping(s);
			});
		});
	}

	menuItems(event) {
		if (this.sessionSvc.role !== 'user') {
			if (event !== undefined) {
				this.selectedItem = this.expectedSubmissions.find(x => x.id.toString() === event.id);
				moment.tz.setDefault('America/New_York');
				let startMoment = moment(this.selectedItem.start);
				let endMoment = moment(this.selectedItem.end);

				const start = startMoment.tz('America/New_York').format('MM/DD/YY h:m a');
				const end = endMoment.tz('America/New_York').format('MM/DD/YY h:m a');

				this.title = this.selectedItem.title + ' ( ' + start + ' - ' + end + ' ) ';
			}
			this.items = [
				{
					label: this.title,
					items: [
						{
							label: 'Acknowledge Delay',
							command: onclick => {
								this.onClickMenuOption({ item: 'Acknowledge Delay', value: this.selectedItem });
							}
						},
						{
							label: 'Disable submission',
							command: onclick => {
								this.onClickMenuOption({ item: 'Disable submission', value: this.selectedItem });
							}
						},
						{
							label: 'Edit submission',
							command: onclick => {
								this.onClickMenuOption({ item: 'Edit submission', value: this.selectedItem });
							}
						}
					]
				}
			];
		} else {
			return null;
		}
	}

	setCalendarOptions() {
		if (this.submissionCalender) {
			this.options = {
				plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
				defaultDate: new Date(),
				defaultView: 'timeGridDay',
				contentHeight: 'auto',
				header: {
					left: 'prev,next',
					center: 'title',
					right: this.calendarViews
				},
				editable: true,
				eventMouseEnter: e => {
					this.eventXPosition = e.jsEvent.x;
					this.eventYPosition = e.jsEvent.y;
					this.menu.show(e.jsEvent);
					this.menuItems(e.event);
					//this.selectedItem = this.expectedSubmissions.find(x => x.id.toString() === e.event.id);
				}
			};
		} else {
			this.options = {
				plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
				defaultDate: new Date(),
				contentHeight: 'auto',
				header: {
					left: 'prev,next',
					center: 'title',
					right: 'dayGridMonth,timeGridWeek,timeGridDay'
				},
				editable: false
			};
		}
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
			this.editScheduleId = row.id;
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

	onSubmitDisabledNote(noteValue) {
		this.scheduleSubmissionsSvc.setDisable(this.disableNoteData.id, noteValue).subscribe(
			value => {
				const index = this.expectedSubmissions.findIndex(x => {
					return x.id === this.disableNoteData.id;
				});
				this.expectedSubmissions[index] = value;
				this.scheduleSubmissionMapping(this.expectedSubmissions[index]);
				this.disableNoteData = null;
				this.displayDisabledDialog = false;
				this.msgSvc.add({
					severity: 'success',
					summary: 'Disabled flag set!',
					detail: `You make the world a better place. Good job!`
				});
			},
			err => {
				this.displayDisabledDialog = false;
			}
		);
	}

	scheduleSubmissionMapping(s) {
		s.title = s.procesName;
		moment.tz.setDefault('America/New_York');
		let startMoment = moment(s.startTime);
		let endMoment = moment(s.endTime);

		s.startTime = startMoment.tz('America/New_York').format('MM/DD/YY h:m a');
		s.endTime = endMoment.tz('America/New_York').format('MM/DD/YY h:m a');

		s.start = new Date(s.startTime);
		s.end = new Date(s.endTime);
		if (this.submissionCalender) {
			s.color = 'lightsteelblue';
		} else {
			s.color = Colors.lightgray;
		}
	}

	setAcknowledgementFlag(noteValue) {
		this.scheduleSubmissionsSvc
			.setAcknowledgementFlag(this.acknowledgementData.id, noteValue, this.acknowledgementData.name)
			.subscribe(value => {
				// Update submission
				const index = this.expectedSubmissions.findIndex(x => {
					return x.id === this.acknowledgementData.id;
				});
				this.expectedSubmissions[index] = value;
				this.scheduleSubmissionMapping(this.expectedSubmissions[index]);
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
		const index = this.expectedSubmissions.findIndex(x => {
			return x.id === value.id;
		});
		this.expectedSubmissions[index] = value;
		this.scheduleSubmissionMapping(this.expectedSubmissions[index]);
		this.updateScheduledSubmissionComponent.toggle();
		this.expectedSubmissions = [...this.expectedSubmissions];
		this.msgSvc.add({
			severity: 'success',
			summary: 'Single ocurrence updated!',
			detail: `All is set. Good luck!`
		});
	}
}
