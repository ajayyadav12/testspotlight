import { Component, OnInit, OnDestroy, Input, ViewChild, EventEmitter } from '@angular/core';
import { SelectItem, MessageService } from 'primeng/api';
import { SubmissionsService } from '../../../../reports/submissions/submissions.service';
import { ActivatedRoute } from '@angular/router';
import { Subject } from 'rxjs';
import { DateCommon } from 'src/app/shared/DateCommon';
import { Table } from 'primeng/table';
import { GENotes } from 'src/app/shared/Components/GENotes';
import { SubmissionCommon } from 'src/app/shared/SubmissionCommon';
import { AuditLogService } from 'src/app/core/services/audit-log.service';
import { SessionService } from 'src/app/core/session/session.service';
import { Colors } from 'src/app/shared/Constants/Colors';

@Component({
	selector: 'app-dashboard-submission-drill-down',
	templateUrl: './dashboard-submission-drill-down.component.html',
	styleUrls: ['./dashboard-submission-drill-down.component.scss'],
	providers: [AuditLogService]
})
export class DashboardSubmissionDrillDownComponent implements OnInit, OnDestroy {
	submissions = [];
	params;

	timing: number = localStorage.getItem('autorefresh-timing')
		? Number.parseInt(localStorage.getItem('autorefresh-timing'))
		: 60;

	get areFiltersActive(): boolean {
		const temp = this.route.snapshot.queryParams;
		if (
			temp['from'] ||
			temp['to'] ||
			temp['childId'] ||
			temp['parentId'] ||
			temp['sender'] ||
			temp['receiver'] ||
			temp['status'] ||
			temp['bu'] ||
			temp['altId'] ||
			temp['adHoc']
		) {
			return true;
		}
	}

	@Input() isLightVersion: boolean;
	@Input() displayType: number;
	types: SelectItem[] = [
		{ label: 'Cards', value: 0, icon: 'pi pi-th-large' },
		{ label: 'Table', value: 1, icon: 'pi pi-table' },
		{ label: 'Calendar', value: 2, icon: 'pi pi-calendar' }
	];

	displayPopup = { value: false, submission: null };
	autoRefreshOn = true;
	autoRefreshHandler;
	clickRefresh = new Subject<any>();

	showIsolation = false;
	isolatedSubmission;

	processSteps = [];
	queryParams;
	lastUpdatedTime;
	// submissions = [];
	days = 7;

	// Table
	@ViewChild('dt', { static: true })
	table: Table;
	totalRecords;
	size = 10;
	sortField;
	sortOrder;
	loading = false;

	displayDdisplayDrillDownDialog = true;
	displayAcknowledgmentDialog = false;
	displayManualSubmissionClosing = false;
	acknowledgementData: GENotes;
	displayUserDialog = false;
	displayAckUserDialog = false;
	drillDownData: true;

	displayNotesDialog = false;
	notesData: any;

	selectedSubmission: { submissionId: number; processId: number };

	tParams;

	get curPage() {
		return this.table ? this.table.first / this.size : 0;
	}

	constructor(
		private submissionsSvc: SubmissionsService,
		private route: ActivatedRoute,
		private msgSvc: MessageService,
		private auditLogSvc: AuditLogService,
		private sessionSvc: SessionService
	) {
		this.auditLogSvc.newAuditLog('Drill Down').subscribe(value => {});
	}

	ngOnInit() {
		this.params = this.route.snapshot.queryParams;

		switch (this.displayType) {
			case 1:
				this.getSubmissionsFailed(this.params);
				break;
			case 2:
				this.getSubmissionsInProgress(this.params);
				break;
			case 3:
				this.getSubmissionsDelayed(this.params);
				break;
			case 4:
				this.getSubmissionsWarning(this.params);
				break;
			case 5:
				this.getSubmissionsSuccess(this.params);
				break;
		}
	}

	ngOnDestroy() {
		this.showIsolation = false;
		clearInterval(this.autoRefreshHandler);
	}

	getSingleSubmission(index) {
		this.submissionsSvc.getSubmissions({ id: this.submissions[index].id }).subscribe((value: any) => {
			value.content.map(s => {
				this.submissions[index].color = Colors.lightgray;
				if (s.status) {
					this.submissions[index].status = s.status.name;
					this.submissions[index].color = this.submissionStatusColor(s.status);
				}

				this.submissions[index].end = new Date(s.endTime);
				this.submissions[index].elapsedTime = DateCommon.dateDifference(
					s.startTime,
					s.endTime ? s.endTime : new Date(),
					true
				);
				return;
			});
		});
	}

	getLatestStepName(steps: any[]): string {
		if (steps.length > 0) {
			return steps.sort((x, y) => {
				return y.id - x.id;
			})[0].processStep.name;
		} else {
			return '';
		}
	}

	getSubmissionsInProgress(tparams: any = { days: this.days }) {
		this.params = {
			days: tparams.days ? tparams.days : this.days,
			childId: tparams.childId ? tparams.childId : '-1,',
			parentId: tparams.parentId ? tparams.parentId : '-1,',
			receiver: tparams.receiver ? tparams.receiver : '-1,',
			sender: tparams.sender ? tparams.sender : '-1,',
			adHoc: tparams.adHoc ? tparams.adHoc : '-1',
			bu: tparams.bu ? tparams.bu : '-1'
		};

		this.submissions = [];
		this.loading = true;
		let process = [];
		this.submissionsSvc.getSubmissionInProgress(this.params).subscribe(
			(value: any) => {
				this.submissions = [];
				this.loading = false;

				value.forEach(s => {
					const startTime = new Date(s[6]);
					const stepStart = new Date(s[3]);
					const stepEnd = new Date(s[4]);
					const today = new Date(s[10]);
					const duration = (today.getTime() - startTime.getTime()) / 1000;
					const stepDuration = (stepEnd.getTime() - stepStart.getTime()) / 1000;
					const schEnd = s[8] ? new Date(s[8]) : '';
					const schEnd2 = s[8] ? new Date(s[8]) : null;
					const schStart = s[7] ? new Date(s[7]) : null;
					const schDuration = schEnd && schStart ? (schEnd2.getTime() - schStart.getTime()) / 1000 : null;
					const tolerance = s[9] ? s[9] * 60 : 0;
					const endTime2 = null;
					process = [];

					process.push({
						id: s[11]
					});

					this.submissions.push({
						id: s[0],
						processname: s[1],
						currentstep: s[2],
						avgstepduration: s[5],
						start: startTime.toLocaleString(),
						schend: schEnd.toLocaleString(),
						schdurationtolerance: schDuration ? this.seconds_to_string(schDuration + tolerance) : '',
						stepelapsetime: this.seconds_to_string(stepDuration),
						elapsetime: this.seconds_to_string(duration),
						elapsedTime: DateCommon.dateDifference(
							s[6],
							today.getTime() ? today.getTime() : new Date(),
							true
						),
						process: process
					});
				});
			},
			err => {
				this.lastUpdatedTime = new Date();
				this.loading = false;
			}
		);
	}

	getSubmissionsFailed(params: any = { days: this.days }) {
		this.params = {
			days: params.days ? params.days : this.days,
			childId: params.childId ? params.childId : '-1,',
			parentId: params.parentId ? params.parentId : '-1,',
			receiver: params.receiver ? params.receiver : '-1,',
			sender: params.sender ? params.sender : '-1,',
			adHoc: params.adHoc ? params.adHoc : '-1',
			bu: params.bu ? params.bu : '-1'
		};

		this.submissions = [];
		this.loading = true;
		this.submissionsSvc.getSubmissionFailed(this.params).subscribe(
			(value: any) => {
				this.submissions = [];
				this.loading = false;

				value.forEach(s => {
					const startTime = new Date(s[9]);
					const endTime = new Date(s[13]);
					const elapsedTime = (endTime.getTime() - startTime.getTime()) / 1000;
					const timeFailed = new Date(s[3]);
					const schEnd = s[4] ? new Date(s[4]) : '';

					this.submissions.push({
						id: s[0],
						processname: s[1],
						firststep: s[2],
						timefailed: timeFailed.toLocaleString(),
						schend: schEnd.toLocaleString(),
						acknowledged: s[5],
						escalatedto: s[11],
						status: 'failed',
						elapsedTime: DateCommon.dateDifference(s[8], s[12] ? s[12] : new Date(), true),
						acknowledgementNote: s[13],
						acknowledgementFlag: s[15] === 1 ? true : false,
						acknowledgementDate: s[14],
						count: ''
					});
				});
			},
			err => {
				this.lastUpdatedTime = new Date();
				this.loading = false;
			}
		);
	}

	getSubmissionsDelayed(params: any = { days: this.days }) {
		this.params = {
			days: params.days ? params.days : this.days,
			childId: params.childId ? params.childId : '-1,',
			parentId: params.parentId ? params.parentId : '-1,',
			receiver: params.receiver ? params.receiver : '-1,',
			sender: params.sender ? params.sender : '-1,',
			adHoc: params.adHoc ? params.adHoc : '-1',
			bu: params.bu ? params.bu : '-1'
		};

		this.submissions = [];
		this.loading = true;
		this.submissionsSvc.getSubmissionDelayed(this.params).subscribe(
			(value: any) => {
				this.submissions = [];
				this.loading = false;

				value.forEach(s => {
					const schStart = new Date(s[2]);

					this.submissions.push({
						id: s[0],
						processname: s[1],
						schstart: schStart.toLocaleString(),
						acknowledged: s[3],
						escalatedto: s[4],
						count: ''
					});
				});
			},
			err => {
				this.lastUpdatedTime = new Date();
				this.loading = false;
			}
		);
	}

	getSubmissionsWarning(tparams: any = { days: this.days }) {
		this.params = {
			days: tparams.days ? tparams.days : this.days,
			childId: tparams.childId ? tparams.childId : '-1,',
			parentId: tparams.parentId ? tparams.parentId : '-1,',
			receiver: tparams.receiver ? tparams.receiver : '-1,',
			sender: tparams.sender ? tparams.sender : '-1,',
			adHoc: tparams.adHoc ? tparams.adHoc : '-1',
			bu: tparams.bu ? tparams.bu : '-1'
		};

		this.submissions = [];
		this.loading = true;
		this.submissionsSvc.getSubmissionWarning(this.params).subscribe(
			(value: any) => {
				this.submissions = [];
				this.loading = false;

				value.forEach(s => {
					const startTime = new Date(s[7]);
					const endTime = new Date(s[8]);
					const duration = (endTime.getTime() - startTime.getTime()) / 1000;

					this.submissions.push({
						id: s[0],
						processname: s[1],
						altId: s[3],
						step: s[2],
						status: 'warning',
						statusStep: s[6],
						start: startTime.toLocaleString(),
						end: endTime.toLocaleString(),
						duration: this.seconds_to_string(duration),
						adhoc: s[10]
					});
				});
			},
			err => {
				this.lastUpdatedTime = new Date();
				this.loading = false;
			}
		);
	}

	getSubmissionsSuccess(tparams: any = { days: this.days }) {
		this.params = {
			days: tparams.days ? tparams.days : this.days,
			childId: tparams.childId ? tparams.childId : '-1,',
			parentId: tparams.parentId ? tparams.parentId : '-1,',
			receiver: tparams.receiver ? tparams.receiver : '-1,',
			sender: tparams.sender ? tparams.sender : '-1,',
			adHoc: tparams.adHoc ? tparams.adHoc : '-1',
			bu: tparams.bu ? tparams.bu : '-1'
		};

		this.submissions = [];
		this.loading = true;
		this.submissionsSvc.getSubmissionSuccess(this.params).subscribe(
			(value: any) => {
				this.submissions = [];
				this.loading = false;

				value.forEach(s => {
					const startTime = new Date(s[7]);
					const endTime = new Date(s[8]);
					const duration = (endTime.getTime() - startTime.getTime()) / 1000;

					this.submissions.push({
						id: s[0],
						processname: s[1],
						altId: s[3],
						step: s[2],
						status: s[6],
						start: startTime.toLocaleString(),
						end: endTime.toLocaleString(),
						duration: this.seconds_to_string(duration),
						adhoc: s[10]
					});
				});
			},
			err => {
				this.lastUpdatedTime = new Date();
				this.loading = false;
			}
		);
	}

	seconds_to_string(seconds) {
		// day, h, m and s
		const days = Math.floor(seconds / (24 * 60 * 60));
		seconds -= days * (24 * 60 * 60);
		const hours = Math.floor(seconds / (60 * 60));
		seconds -= hours * (60 * 60);
		const minutes = Math.floor(seconds / 60);
		seconds -= minutes * 60;
		return (0 < days ? days + ' day, ' : '') + hours + 'h, ' + minutes + 'm and ' + Math.trunc(seconds) + 's';
	}

	getSubmissionCount() {
		this.submissionsSvc.getSubmissionInProgress(this.days).subscribe((value: any[]) => {
			this.submissions = value;
		});
	}

	/**
     * Add table data (size, page and sorting) to submission request query params
     */
	getUpdatedSubmissionRequestParams(): any {
		const tParams: any = {};
		Object.assign(tParams, this.queryParams);
		tParams.size = this.size;
		tParams.page = this.curPage;
		tParams.sortField = this.queryParams.sortField
			? this.queryParams.sortField
			: this.sortField ? this.sortField : 'id';
		tParams.sortOrder = this.queryParams.sortOrder
			? this.queryParams.sortOrder
			: this.sortOrder ? this.sortOrder : -1;
		return tParams;
	}

	onSubmissionClose(submission) {
		const index = this.submissions.findIndex(x => {
			return x.id === submission.id;
		});
		this.submissionsMapping(submission);
		this.submissions[index] = submission;
		this.msgSvc.add({
			severity: 'success',
			summary: 'Submission closed!',
			detail: `Good job! Continue keeping things clear`
		});
		this.displayManualSubmissionClosing = false;
	}

	openSubmissionDialog(submission) {
		const session = JSON.parse(localStorage.getItem('session'));
		if (this.isLightVersion) {
			return;
		}
		if (submission.status === 'failed') {
			if (this.sessionSvc.role !== 'user') {
				this.acknowledgementData = {
					id: submission.id,
					note: submission.acknowledgementNote,
					flag: submission.acknowledgementFlag,
					date: submission.acknowledgementDate,
					name: submission.acknowledgedBy !== null ? submission.acknowledgedBy : session.user.name
				};

				this.displayAcknowledgmentDialog = true;
			} else {
				this.displayAcknowledgmentDialog = true;
				this.displayAckUserDialog = true;
			}
		} else if (!submission.endTime) {
			if (this.sessionSvc.role !== 'user') {
				this.displayManualSubmissionClosing = true;
				this.displayUserDialog = false;
				this.selectedSubmission = { submissionId: submission.id, processId: submission.process.id };
			} else {
				this.displayManualSubmissionClosing = true;
				this.displayUserDialog = true;
				this.selectedSubmission = { submissionId: submission.id, processId: submission.process.id };
			}
		}
	}

	setAcknowledgementFlag(noteValue) {
		this.loading = true;
		this.submissionsSvc
			.setAcknowledgementFlag(this.acknowledgementData.id, noteValue, this.acknowledgementData.name)
			.subscribe(value => {
				// Update submission
				const index = this.submissions.findIndex(x => {
					return x.id === value.id;
				});
				this.submissions[index].acknowledgementFlag = value.acknowledgementFlag;
				this.submissions[index].acknowledgementNote = value.acknowledgementNote;
				this.displayAcknowledgmentDialog = false;
				this.acknowledgementData = null;
				this.msgSvc.add({
					severity: 'success',
					summary: 'Acknowledge flag set!',
					detail: `Now everybody knows you took care of the issue. Good job!`
				});
				this.loading = false;
				this.displayAcknowledgmentDialog = false;
			});
	}

	openNotesDialog(submission) {
		this.notesData = submission.notes;
		this.displayNotesDialog = true;
	}

	openIsolationMode(submission) {
		this.isolatedSubmission = submission;
		this.showIsolation = true;
	}

	submissionStatusColor(status) {
		return SubmissionCommon.submissionStatusColor(status);
	}

	/**
     * submissionsMapping() maps retreived data for All Submissions table,
     * Calendar, and parent component dialog box
     */
	submissionsMapping(s) {
		s['process.name'] = s.process.name;
		s.title = s.process.name;
		s.color = Colors.lightgray;
		if (s.status) {
			s.status = s.status.name;
			s.color = this.submissionStatusColor(s.status);
		}
		s.start = new Date(s.startTime);
		s.end = new Date(s.endTime);
		s.elapsedTime = DateCommon.dateDifference(s.startTime, s.endTime ? s.endTime : new Date(), true);
	}

	/**
     * changeView is called to manage the dialog boxes between parent/child views
     * uses submission/value to manage which component to view/hide
     * in Card View: changeView opens dialog box to show step component of child submission
     * in Table View: changeView opens dialog box to show parent component of child submission
     **/
	changeView(submission?) {
		if (!submission) {
			this.displayPopup.value = false;
			this.displayPopup.submission = null;
		} else if (this.displayType === 0) {
			this.displayPopup.value = true;
			this.displayPopup.submission = submission.element;
		} else {
			this.submissionsSvc.getSubmissionParent(submission.parentId).subscribe(parent => {
				this.mapParentSubmission(parent);
				this.displayPopup.value = true;
				this.displayPopup.submission = parent;
			});
		}
	}

	mapParentSubmission(value) {
		value.title = value.process.name;
		value.status = value.status.name;
		value.start = new Date(value.startTime);
		value.end = new Date(value.endTime);
		value.elapsedTime = DateCommon.dateDifference(
			value.startTime,
			value.endTime ? value.endTime : new Date(),
			true
		);
		value.children.map(c => {
			c.title = c.process.name;
			c.status = c.status.name;
			c.color = this.submissionStatusColor(c.status);
			c.parentId = value.id;
			c.start = new Date(c.startTime);
			c.end = new Date(c.endTime);
			c.elapsedTime = DateCommon.dateDifference(c.startTime, c.endTime ? c.endTime : new Date(), true);
		});
		// future: sort by predecessor/successor
		if (value.children.length > 1) {
			value.children.sort((a, b) => {
				return a.id - b.id;
			});
		}
	}

	/**
     * refreshStatus() is called when a submission dropdown is opened
     * retrieves updated information for that submissions so that data matches
     **/
	refreshStatus(submission) {
		const updateIndex = this.submissions.findIndex(s => s.id.toString() === submission.id);
		this.getSingleSubmission(updateIndex);
	}
}
