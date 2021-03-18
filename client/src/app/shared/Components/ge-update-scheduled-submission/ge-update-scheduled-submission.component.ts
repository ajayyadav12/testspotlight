import { Component, OnInit, Output, EventEmitter, Input } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { DateCommon } from '../../DateCommon';
import { ScheduleSubmissionsService } from 'src/app/modules/admin/schedule/schedule-submissions.service';
import * as moment from 'moment';

@Component({
	selector: 'app-ge-update-scheduled-submission',
	templateUrl: './ge-update-scheduled-submission.component.html',
	styleUrls: [ './ge-update-scheduled-submission.component.scss' ]
})
export class GeUpdateScheduledSubmissionComponent implements OnInit {
	_scheduledSubmissionForm: FormGroup;
	displayEditDialog = false;
	public schedSubmissionID;
	get scheduledSubmissionFormValue(): any {
		return this._scheduledSubmissionForm.value;
	}

	@Output() onUpdateSubmission = new EventEmitter();

	constructor(private fb: FormBuilder, private scheduleSubmissionsSvc: ScheduleSubmissionsService) {
		this._scheduledSubmissionForm = this.fb.group({
			startTime: [ null, Validators.required ],
			endTime: [ null, Validators.required ],
			editNotes: [ '', Validators.required ]
		});
	}

	ngOnInit() {}

	updateUpcomingSubmission() {
		const startTime: Date = this._scheduledSubmissionForm.value.startTime;
		const endTime: Date = this._scheduledSubmissionForm.value.endTime;
		const editNotes = this._scheduledSubmissionForm.value.editNotes;

		this.scheduleSubmissionsSvc
			.updateUpcomingSubmission(
				this.schedSubmissionID,
				moment(startTime).format(),
				moment(endTime).format(),
				editNotes
			)
			.subscribe((value) => {
				this.onUpdateSubmission.emit(value);
			});
	}

	public setValue(value: { startTime: any; endTime: any; editNotes: string }) {
		this._scheduledSubmissionForm.setValue(value);
	}

	/**
   * openDialog
   */
	public toggle() {
		this.displayEditDialog = !this.displayEditDialog;
	}
}
