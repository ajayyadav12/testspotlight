import { Component, OnInit, Input } from '@angular/core';
import { SubmissionCommon } from 'src/app/shared/SubmissionCommon';
import { SubmissionsService } from '../submissions.service';
import { DateCommon } from 'src/app/shared/DateCommon';
import { MessageService } from 'primeng/api';
import { DatePipe } from '@angular/common';
import * as moment from 'moment-timezone';
import { ScheduleSubmissionsService } from 'src/app/modules/admin/schedule/schedule-submissions.service';

@Component({
	selector: 'app-submissions-isolation',
	templateUrl: './submissions-isolation.component.html',
	styleUrls: ['./submissions-isolation.component.scss']
})
export class SubmissionsIsolationComponent implements OnInit {
	@Input() incomingSubmission;
	@Input() previousUpdatedTime;

	submission;

	@Input() autoRefreshOn = true;
	autorefreshHandler;
	lastUpdatedTime;
	scheduleSubmission: any;

	constructor(
		private submissionsSvc: SubmissionsService,
		private ScheduleSubmissionSvc: ScheduleSubmissionsService,
		private msgSvc: MessageService,
		private datePipe: DatePipe
	) { }

	ngOnInit() {
		this.prepareIncomingSubmission(this.incomingSubmission);

		this.autorefreshHandler = setInterval(() => {
			if (this.autoRefreshOn) {
				this.getSingleSubmission();
			}
		}, 1000 * 20);
	}

	ngOnDestroy() {
		clearInterval(this.autorefreshHandler);
	}

	prepareIncomingSubmission(incomingSubmission) {
		this.submission = JSON.parse(JSON.stringify(incomingSubmission)); // deep clone

		this.submission.elapsedTime = DateCommon.dateDifference(
			this.submission.startTime,
			this.submission.endTime ? this.submission.endTime : new Date(),
			false
		);

		this.ScheduleSubmissionSvc.getScheduleSubmission(incomingSubmission.id).subscribe(value => {

			this.scheduleSubmission = value;
			moment.tz.setDefault('America/New_York');
			let startMoment = moment(this.scheduleSubmission.startTime);
			let endMoment = moment(this.scheduleSubmission.endTime);
			this.scheduleSubmission.startTime = new Date(this.scheduleSubmission.startTime);
			this.scheduleSubmission.endTime = new Date(this.scheduleSubmission.endTime);
			/* this.scheduleSubmission.startTime = startMoment.tz('America/New_York').format('MM/DD/YY h:m a');
			if (this.scheduleSubmission.endTime) {
				this.scheduleSubmission.endTime = endMoment.tz('America/New_York').format('MM/DD/YY h:m a');
			} */
		});

		this.submission.notes = this.submission.notes
			? this.submission.notes.split(' \n ').filter((n) => {
				return n !== 'null';
			})
			: [];

		if (!this.submission.steps) {
			this.getSubmissionSteps();
		} else {
			this.stepsMapping();
		}
		this.lastUpdatedTime = this.previousUpdatedTime;
	}

	getSingleSubmission() {
		this.submissionsSvc.getSubmissions({ id: this.submission.id }).subscribe((value: any) => {
			this.submission = value.content[0];
			this.submissionMapping();
			this.getSubmissionSteps();
			this.lastUpdatedTime = new Date();
		});
	}

	getSubmissionSteps() {
		this.submissionsSvc.getSubmissionSteps(this.submission.id).subscribe((steps) => {
			this.submission.steps = steps;
			this.stepsMapping();
		});
	}

	submissionMapping() {
		this.submission['process.name'] = this.submission.process.name;
		this.submission.status = this.submission.status.name;
		this.submission.notes = this.submission.notes
			? this.submission.notes.split(' \n ').filter((n) => {
				return n !== 'null';
			})
			: [];
		if (this.submission.status === 'failed' && !this.submission.acknowledgementFlag) {
			this.msgSvc.add({
				severity: 'error',
				summary: 'Submission Failed!',
				detail: `Exit this view to acknowledge.`
			});
		}
		this.submission.color = this.submissionStatusColor(this.submission.status);

		this.submission.elapsedTime = DateCommon.dateDifference(
			this.submission.startTime,
			this.submission.endTime ? this.submission.endTime : new Date(),
			false
		);
	}

	stepsMapping() {
		this.submission.steps.sort((a, b) => {
			return a.id - b.id;
		});

		let order = 1; // not consistent on refresh, check step comp. ordering

		this.submission.steps.map((ps) => {
			ps.notes = ps.notes
				? ps.notes.split(' \n ').filter((n) => {
					return n !== 'null';
				})
				: [];
			ps.displayTime =
				ps.processStep.name === 'start'
					? this.datePipe.transform(new Date(ps.startTime), 'mediumTime')
					: ps.processStep.name === 'end'
						? this.datePipe.transform(new Date(ps.endTime), 'mediumTime')
						: DateCommon.dateDifference(ps.startTime, ps.endTime ? ps.endTime : new Date(), false);
			ps.styleClass =
				'' +
				(ps.processStep.name === 'start' || ps.processStep.name === 'end' ? 'bumper ' : '') +
				ps.status.name.replace(/ /g, '');
			ps.order = order;
			order++;
		});

		this.submission.steps = this.submission.steps.reverse();
	}

	submissionStatusColor(status) {
		return SubmissionCommon.submissionStatusColor(status);
	}
}
