import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { ProcessService } from '../../process.service';
import { MessageService } from 'primeng/api';
import { NotificationService } from '../../../notification/notification.service';
import { ActivatedRoute } from '@angular/router';
import { UserService } from '../../../user/user.service';
import { NotificationRequest } from './NotificationRequest';
import { SessionService } from 'src/app/core/session/session.service';

@Component({
	selector: 'app-process-dtl-notifications',
	templateUrl: './process-dtl-notifications.component.html',
	styleUrls: ['./process-dtl-notifications.component.scss'],
	providers: [NotificationService, UserService],
})
export class ProcessDtlNotificationsComponent implements OnInit {
	notificationId = -1;
	processId = 0;
	steps = [];
	dls = [];
	templates = [];
	status = [
		{ name: 'In progress', id: 1 },
		{ name: 'Success', id: 2 },
		{ name: 'Warning', id: 3 },
		{ name: 'Failed', id: 4 },
	];
	escalationTypes = [
		{ label: 'Delayed Escalation', value: 'Delayed Escalation' },
		{ label: 'Failed Escalation', value: 'Failed Escalation' },
	];
	submissionTypes = [
		{ label: 'Delayed Submission', value: 'Delayed Submission' },
		{ label: 'Disabled Submission', value: 'Disabled Submission' },
		{ label: 'Edited Submission', value: 'Edited Submission' },
		{ label: 'Long Running Submission', value: 'Long Running Submission' },
		{ label: 'Long Running Steps', value: 'Long Running Steps' },
		{ label: 'Required Steps', value: 'Required Steps' },
		{ label: 'Submission Step Incomplete', value: 'Submission Step Incomplete' },
	];
	processNotifications = [];
	users = [];
	processNotificationForm: FormGroup;
	columns = [
		{ field: 'id', header: 'ID' },
		{ field: 'processStepName', header: 'Step' },
		{ field: 'statusName', header: 'Status' },
		{ field: 'escalationLevel', header: 'Escalation' },
		{ field: 'additionalEmails', header: 'Additional Emails' },
		{ field: 'usersPhoneNumbers', header: 'Phone Number' },
		{ field: 'submissionLevel', header: 'Submission Type' },
	];
	level = 'noChoice';

	get input() {
		return this.processNotificationForm ? this.processNotificationForm.value : null;
	}

	constructor(
		private fb: FormBuilder,
		private processSvc: ProcessService,
		private msgSvc: MessageService,
		private route: ActivatedRoute,
		private userSvc: UserService
	) {
		this.setupForm();
	}

	setupForm() {
		this.processNotificationForm = this.fb.group({
			level: [{ value: 'noChoice' }],
			processSteps: [{ value: null, disabled: true }],
			status: [{ value: null, disabled: true }],
			escalationType: [{ value: null, disabled: true }],
			submissionType: [{ value: null, disabled: true }],
			enableTextMessaging: false,
		});
		this.onChangeLevel(this.level);

		this.processNotificationForm.get('submissionType').valueChanges.subscribe((value) => {
			this.onChangeType();
		});
	}

	ngOnInit() {
		this.route.parent.params.subscribe((params) => {
			this.processId = params['id'] || 0;
			if (this.processId != 0) {
				this.getProcessNotifications();
			}
		});
	}

	getNotificationPayload(form, processStepIndex) {
		return {
			processStep: form.processSteps && form.processSteps.length > 0 ? form.processSteps[processStepIndex] : null,
			status: form.status,
			escalationType: !form.escalationType ? '' : form.escalationType.value,
			submissionType: !form.submissionType ? '' : form.submissionType.value,
			enableTextMessaging: form.enableTextMessaging,
		};
	}

	cancel() {
		this.notificationId = -1;
	}

	onSubmit() {
		if (!this.isFormValid()) {
			alert('Please select a notification criteria');
			return;
		}
		const length = this.processNotificationForm.value.processSteps
			? this.processNotificationForm.value.processSteps.length
			: 1;

		for (let index = 0; index < length; index++) {
			if (this.notificationId === 0) {
				const notification = this.getNotificationPayload(this.processNotificationForm.value, index);
				const myNotification: NotificationRequest = {
					processStepId: notification.processStep ? notification.processStep.id : null,
					statusId: notification.status ? notification.status.id : null,
					enableTextMessaging: notification.enableTextMessaging,
					escalationType: notification.escalationType,
					submissionType: notification.submissionType,
				};
				this.processSvc.newProcessMyNotification(this.processId, myNotification).subscribe(() => {
					this.getProcessNotifications();
					this.setupForm();
					this.notificationId = -1;
				});
			} else {
				const notification = this.getNotificationPayload(this.processNotificationForm.value, index);
				const myNotification: NotificationRequest = {
					processStepId: notification.processStep ? notification.processStep.id : null,
					statusId: notification.status ? notification.status.id : null,
					enableTextMessaging: notification.enableTextMessaging,
					escalationType: notification.escalationType,
					submissionType: notification.submissionType,
				};
				this.processSvc
					.updateProcessMyNotification(this.processId, this.notificationId, myNotification)
					.subscribe(() => {
						this.getProcessNotifications();
						this.setupForm();
						this.notificationId = -1;
					});
			}
		}

		setTimeout((_) => {
			this.msgSvc.add({
				severity: 'success',
				summary: 'New notification setting!',
				detail: `Notification was added`,
			});
		}, 500);
	}

	isFormValid(): boolean {
		return (
			this.level &&
			(this.processNotificationForm.value.processSteps ||
				this.processNotificationForm.value.status ||
				this.processNotificationForm.value.escalationType ||
				this.processNotificationForm.value.submissionType)
		);
	}

	getProcessNotifications() {
		this.processSvc.getProcessNotifications(this.processId).subscribe((value) => {
			this.processNotifications = value;
			this.processNotifications.forEach((pn) => {
				this.notificationMapping(pn);
			});
		});
	}

	notificationMapping(pn) {
		pn.processStepName = pn.processStep ? pn.processStep.name : '';
		if (pn.escalationType || pn.submissionType != 'Delayed Submission') {
			pn.statusName = pn.status ? pn.status.name : '';
		} else {
			pn.statusName = pn.status ? pn.status.name : 'Delayed';
		}
		pn.escalationLevel = pn.escalationType ? pn.escalationType : '';
		pn.submissionLevel = pn.submissionType ? pn.submissionType : '';
		pn.usersPhoneNumbers = '';
		pn.userMobiles.forEach((userMobile) => {
			pn.usersPhoneNumbers += `${userMobile.user.phoneNumber} - ${userMobile.user.name} \n`;
		});
	}

	onChangeLevel(value) {
		this.processNotificationForm.controls['processSteps'].setValue(null);

		switch (value) {
			case 'noChoice':
				this.processNotificationForm.controls['submissionType'].enable();
				this.processNotificationForm.controls['status'].disable();
				this.processNotificationForm.controls['processSteps'].disable();
				this.processNotificationForm.controls['escalationType'].disable();
				break;
			case 'statusStep':
				this.processNotificationForm.controls['submissionType'].setValue(null);
				this.processNotificationForm.controls['submissionType'].disable();
				this.processNotificationForm.controls['processSteps'].enable();
				this.processNotificationForm.controls['status'].enable();
				this.processNotificationForm.controls['escalationType'].disable();
				break;
			case 'statusProcess':
				this.processNotificationForm.controls['submissionType'].setValue(null);
				this.processNotificationForm.controls['submissionType'].disable();
				this.processNotificationForm.controls['status'].enable();
				this.processNotificationForm.controls['processSteps'].disable();
				this.processNotificationForm.controls['escalationType'].disable();
				break;
			case 'escalation':
				this.processNotificationForm.controls['submissionType'].setValue(null);
				this.processNotificationForm.controls['submissionType'].disable();
				this.processNotificationForm.controls['status'].disable();
				this.processNotificationForm.controls['processSteps'].disable();
				this.processNotificationForm.controls['escalationType'].enable();
				break;
			default:
				break;
		}
	}

	onChangeType() {
		switch (this.level) {
			case 'Delayed Submission':
				this.processNotificationForm.controls['status'].setValue(null);

				break;
		}
	}

	onFocusProcessStep(event) {
		this.processSvc.getAllProcessSteps(this.processId).subscribe((value: any[]) => {
			this.steps = value;
		});
	}

	onShowUserMobile() {
		this.userSvc.getUsers(true).subscribe((value) => {
			this.users = value;
		});
	}

	onDeleteRecord(id) {
		this.processSvc.deleteProcessMyNotification(this.processId, id).subscribe(() => {
			this.processNotifications = this.processNotifications.filter((p) => {
				return p.id !== id;
			});
			this.msgSvc.add({
				severity: 'error',
				summary: `It's not me, it's you!`,
				detail: `Notification was deleted`,
			});
		});
	}

	newNotification() {
		this.processNotificationForm.reset();
		this.notificationId = 0;
	}

	onEditRecord(id) {
		this.notificationId = Number.parseInt(id);
		const notification = this.processNotifications.find((x) => x.id === this.notificationId);

		let level = '';
		if (notification.escalationType != '') {
			level = 'escalation';
		} else if (notification.processStep != null) {
			level = 'statusStep';
			this.steps.push(notification.processStep);
		} else if (notification.submissionType != '') {
			level = 'noChoice';
		} else {
			level = 'statusProcess';
		}

		this.processNotificationForm.patchValue({
			level: level,
			processSteps: notification.processStep ? [notification.processStep] : null,
			status: notification.status,
			escalationType: { value: notification.escalationType },
			submissionType: { value: notification.submissionType },
			enableTextMessaging: notification.enableTextMessaging,
		});
	}
}
