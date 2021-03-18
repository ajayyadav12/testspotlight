import { Component, OnInit, OnDestroy, Input, ViewChild } from '@angular/core';
import { SelectItem, LazyLoadEvent, MessageService } from 'primeng/api';
import { SubmissionsService } from './submissions.service';
import { ActivatedRoute, Router } from '@angular/router';
import { SidebarService } from 'src/app/core/sidebar/sidebar.service';
import { Subject } from 'rxjs';
import { DateCommon } from 'src/app/shared/DateCommon';
import { Table } from 'primeng/table';
import { GENotes } from 'src/app/shared/Components/GENotes';
import { SubmissionCommon } from 'src/app/shared/SubmissionCommon';
import { AuditLogService } from 'src/app/core/services/audit-log.service';
import { GEChipFilter } from '../../dashboard/dashboard-filters/GEChipFilter';
import { GEFiltersComponent } from 'src/app/shared/Components/ge-filters/ge-filters.component';
import * as moment from 'moment-timezone';
import { SessionService } from 'src/app/core/session/session.service';
import { Colors } from 'src/app/shared/Constants/Colors';

enum SubmissionViews {
	Cards = 0,
	Table,
	Calendar,
}
@Component({
	selector: 'app-submissions',
	templateUrl: './submissions.component.html',
	styleUrls: ['./submissions.component.scss'],
	providers: [AuditLogService],
})
export class SubmissionsComponent implements OnInit, OnDestroy {
	timing: number = localStorage.getItem('autorefresh-timing')
		? Number.parseInt(localStorage.getItem('autorefresh-timing'))
		: 60;
	get areFiltersActive(): boolean {
		const temp = this.activatedRoute.snapshot.queryParams;
		if (
			temp['searchBy'] ||
			temp['periodFrom'] ||
			temp['periodTo'] ||
			temp['from'] ||
			temp['to'] ||
			temp['childId'] ||
			temp['parentId'] ||
			temp['sender'] ||
			temp['receiver'] ||
			temp['status'] ||
			temp['bu'] ||
			temp['altId'] ||
			temp['id'] ||
			temp['duration'] ||
			temp['durationTime'] ||
			temp['notes'] ||
			temp['adHoc']
		) {
			return true;
		}
	}

	@ViewChild('filter', { static: true })
	filter: GEFiltersComponent;
	displayType = SubmissionViews.Table;
	displayPopup = { value: false, submission: null };
	views = [
		{ label: 'Parents', value: SubmissionViews.Cards },
		{ label: 'Table', value: SubmissionViews.Table },
		{ label: 'Calendar', value: SubmissionViews.Calendar },
	];
	autoRefreshOn = true;
	autoRefreshHandler;
	clickRefresh = new Subject<any>();
	filterSelection = ['dateRange', 'process', 'parent', 'status'];
	showIsolation = false;
	isolatedSubmission;
	records;
	processSteps = [];
	queryParams;
	lastUpdatedTime;
	displayUserDialog = false;
	displayAckUserDialog = false;
	submissions = [];
	// Table
	@ViewChild('dt', { static: true })
	table: Table;
	totalRecords;
	size = 25;
	sizeOptions = [
		{ label: '10', value: 10 },
		{ label: '25', value: 25 },
		{ label: '50', value: 50 },
	];
	sortField;
	sortOrder;
	loading = false;
	hide = false;
	submissionFieldOptions: SelectItem[] = [
		{ label: 'ID', value: 'id' },
		{ label: 'Process', value: 'process' },
		{ label: 'Status', value: 'stat' },
		{ label: 'Start Time', value: 'startTime' },
		{ label: 'End Time', value: 'endTime' },
		{ label: 'Total Time', value: 'totalTime' },
		{ label: 'Records', value: 'records' },
		{ label: 'Warning', value: 'warnings' },
		{ label: 'Error', value: 'errors' },
		{ label: 'Period', value: 'period' },
		{ label: 'Adhoc', value: 'adHoc' },
		{ label: 'Parent', value: 'parent' },
		{ label: 'Action', value: 'action' },
	];
	columnFilters = [
		'id',
		'process',
		'stat',
		'startTime',
		'endTime',
		'totalTime',
		'records',
		'warnings',
		'errors',
		'period',
		'parent',
		'action',
	];

	displayAcknowledgmentDialog = false;
	displayManualSubmissionClosing = false;
	acknowledgementData: GENotes;

	displayNotesDialog = false;
	notesData: any;
	statusValue;
	displayDataFileDialog: boolean = false;

	selectedSubmission: { submissionId: number; processId: number };

	tParams;

	get curPage() {
		return this.table ? this.table.first / this.size : 0;
	}

	constructor(
		private submissionsSvc: SubmissionsService,
		private activatedRoute: ActivatedRoute,
		private sidebarSvc: SidebarService,
		private msgSvc: MessageService,
		private auditLogSvc: AuditLogService,
		private router: Router,
		private sessionSvc: SessionService
	) {
		this.sidebarSvc.title = 'All Submissions';

		this.activatedRoute.queryParams.subscribe((params) => {
			if (
				location.pathname !== '/dashboard' &&
				params === {} &&
				localStorage.getItem('submission-view') !== null
			) {
				return;
			}
			this.queryParams = params;
			if (this.table) {
				this.table.reset();
			}
			this.getSubmissions(false);
		});
		this.auditLogSvc.newAuditLog('Submission').subscribe((value) => {});
	}

	ngOnInit() {
		const tab = localStorage.getItem('submission-tab');
		if (tab) {
			this.updateTitle(tab);
		}
		this.onchangeTiming({});
	}

	autoRefreshSetup() {
		this.autoRefreshHandler = setInterval(() => {
			if (this.curPage === 0) {
				this.getSubmissions(true);
			}
		}, 1000 * this.timing);
	}

	showDataFileDialog(index, flag) {
		this.loading = true;
		this.submissionsSvc.getSubmissionReports(index, flag).subscribe((value: any) => {
			this.records = value;
			if (this.records !== null && this.records.length > 0) {
				this.displayDataFileDialog = true;
				this.loading = false;
				return;
			} else {
				this.submissionsSvc.getSubmissions({ id: index }).subscribe((value: any) => {
					value.content.map((s) => {
						var url = flag === 'R' ? s.reportFile : s.dataFile;
						window.open(url, '_blank');
						this.loading = false;
						return;
					});
				});
			}
		});
		this.displayDataFileDialog = false;
	}

	onchangeTiming(timing) {
		clearInterval(this.autoRefreshHandler);
		localStorage.setItem('autorefresh-timing', this.timing.toString());
		if (this.timing > 0) {
			this.autoRefreshOn = true;
			this.autoRefreshSetup();
		} else {
			this.autoRefreshOn = false;
		}
	}

	ngOnDestroy() {
		this.showIsolation = false;
		clearInterval(this.autoRefreshHandler);
	}

	updateTitle(index) {
		this.displayType = Number.parseFloat(index);
		localStorage.setItem('submission-tab', index.toString());
		this.sidebarSvc.title = this.displayType !== SubmissionViews.Cards ? 'All Submissions' : 'Parent Submissions';
	}

	getSingleSubmission(index) {
		//alert(index);
		this.submissionsSvc.getSubmissions({ id: this.submissions[index].id }).subscribe((value: any) => {
			value.content.map((s) => {
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

	getSubmissions(fromAutoRefresh) {
		this.tParams = this.getUpdatedSubmissionRequestParams();

		this.loading = true;
		this.submissionsSvc.getSubmissions(this.tParams).subscribe(
			(value: any) => {
				if (fromAutoRefresh && this.totalRecords === value.totalElements) {
					this.lastUpdatedTime = new Date();
					this.loading = false;
					return;
				}
				if (this.tParams.durationTime && this.tParams.duration) {
					this.submissions = [];
					value.content.forEach((s) => {
						const totalTimeinMS =
							(new Date(s.endTime) ? new Date(s.endTime).getTime() : new Date().getTime()) -
							new Date(s.startTime).getTime();
						const durationTime = (this.tParams.durationTime ? this.tParams.durationTime : 0) * 60000;
						if (this.tParams.duration === 'G') {
							if (durationTime < totalTimeinMS) {
								this.submissions.push(s);
							}
						} else if (this.tParams.duration === 'L') {
							if (durationTime >= totalTimeinMS) {
								this.submissions.push(s);
							}
						}
					});
					//this.totalRecords = this.submissions.length;
				} else {
					this.submissions = [];
					this.submissions = value.content;
					this.totalRecords = value.totalElements;
				}
				this.submissions.map((s) => {
					this.submissionsMapping(s);
				});
				this.displayPopup.value = false;
				this.displayPopup.submission = null;
				this.lastUpdatedTime = new Date();
				this.loading = false;

				if (!this.totalRecords) {
					this.msgSvc.add({
						severity: 'info',
						summary: 'No submissions found!',
						detail: 'Try a different filter or view.',
					});
				}
				this.filter.updateFilters();
			},
			(err) => {
				this.lastUpdatedTime = new Date();
				this.loading = false;
			}
		);
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
			: this.sortField
			? this.sortField
			: 'id';
		tParams.sortOrder = this.queryParams.sortOrder
			? this.queryParams.sortOrder
			: this.sortOrder
			? this.sortOrder
			: -1;
		return tParams;
	}

	onSubmissionClose(submission) {
		const index = this.submissions.findIndex((x) => {
			return x.id === submission.id;
		});
		this.submissionsMapping(submission);
		this.submissions[index] = submission;
		this.msgSvc.add({
			severity: 'success',
			summary: 'Submission closed!',
			detail: `Good job! Continue keeping things clear`,
		});
		this.displayManualSubmissionClosing = false;
	}

	openSubmissionDialog(submission) {
		const session = JSON.parse(localStorage.getItem('session'));
		if (submission.status === 'failed') {
			if (this.sessionSvc.role !== 'user') {
				this.acknowledgementData = {
					id: submission.id,
					note: submission.acknowledgementNote,
					flag: submission.acknowledgementFlag,
					date: submission.acknowledgementDate,
					name: submission.acknowledgedBy !== null ? submission.acknowledgedBy : session.user.name,
				};
				this.displayAcknowledgmentDialog = true;
				this.displayAckUserDialog = false;
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
			.subscribe((value) => {
				// Update submission
				const index = this.submissions.findIndex((x) => {
					return x.id === value.id;
				});
				this.submissions[index].acknowledgementFlag = value.acknowledgementFlag;
				this.submissions[index].acknowledgementNote = value.acknowledgementNote;
				this.displayAcknowledgmentDialog = false;
				this.acknowledgementData = null;
				this.msgSvc.add({
					severity: 'success',
					summary: 'Acknowledge flag set!',
					detail: `Now everybody knows you took care of the issue. Good job!`,
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

	submissionStatusColorCode(status) {
		return SubmissionCommon.submissionStatusColorCode(status);
	}

	showFilter(filterName: string): boolean {
		return this.columnFilters.includes(filterName);
	}

	/**
	 * submissionsMapping() maps retreived data for All Submissions table,
	 * Calendar, and parent component dialog box
	 */
	submissionsMapping(s) {
		moment.tz.setDefault('America/New_York');
		let startMoment = moment(s.startTime);
		let endMoment = moment(s.endTime);

		s['process.name'] = s.process.name;
		s.title = s.process.name;
		s.color = Colors.lightgray;
		if (s.status) {
			s.status = s.status.name;
			s.color = this.submissionStatusColor(s.status);
			switch (s.status) {
				case 'success':
					s.statusValue = 'Success';
					break;
				case 'in progress':
					s.statusValue = 'In Progress';
					break;
				case 'warning':
					s.statusValue = 'Warning';
					break;
				case 'failed':
					s.statusValue = 'Failed';
					break;
				case 'long running':
					s.statusValue = 'Long Running';
					break;
				default:
					break;
			}
		}
		s.start = new Date(s.startTime);
		s.end = new Date(s.endTime);

		s.startTime = startMoment.tz('America/New_York').format('MM/DD/YY h:m a');
		if (s.endTime) {
			s.endTime = endMoment.tz('America/New_York').format('MM/DD/YY h:m a');
		}

		s.dataFileUrl = s.dataFile !== null ? s.dataFile : false;
		s.reportFileUrl = s.reportFile !== null ? s.reportFile : false;

		if (s.startTime !== null && s.endTime !== null) {
			s.totalTime = DateCommon.dateDifference(s.startTime, s.endTime ? s.endTime : new Date(), false);
			//remove comma
			s.totalTime = s.totalTime.replace(/,\s*$/, '');
		}

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
		} else if (this.displayType === SubmissionViews.Cards) {
			this.displayPopup.value = true;
			this.displayPopup.submission = submission.element;
		} else {
			this.submissionsSvc.getSubmissionParent(submission.parentId).subscribe((parent) => {
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
		value.children.map((c) => {
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
	 * LoadSubmissions is called when the table starts,
	 * because we are doing the same in constructor we avoid it when sortField is undefined
	 **/
	loadSubmissions(event: LazyLoadEvent) {
		if (this.sortField) {
			this.sortField = event.sortField ? event.sortField : 'id';
			this.sortOrder = event.sortField ? event.sortOrder : -1;
			this.getSubmissions(false);
		} else {
			this.sortField = 'id';
		}
	}

	onClickRefresh() {
		this.getSubmissions(false);
		this.clickRefresh.next(this.queryParams);
	}

	/**
	 * refreshStatus() is called when a submission dropdown is opened
	 * retrieves updated information for that submissions so that data matches
	 **/
	refreshStatus(submission) {
		const updateIndex = this.submissions.findIndex((s) => s.id.toString() === submission.id);
		this.getSingleSubmission(updateIndex);
	}

	updateColumnsSelection(events) {
		this.columnFilters = events;
	}

	fullscreen(divid) {
		const elem: any = document.getElementById(divid);

		if (elem.requestFullscreen) {
			elem.requestFullscreen();
		} else if (elem.mozRequestFullScreen) {
			elem.mozRequestFullScreen();
		} else if (elem.webkitRequestFullscreen) {
			elem.webkitRequestFullscreen();
		} else if (elem.msRequestFullscreen) {
			elem.msRequestFullscreen();
		}
	}

	onChangeView(event) {
		let view = 'table';
		switch (event.value) {
			case 0:
				view = 'parents';
				break;
			case 1:
				view = 'table';
				break;
			case 2:
				view = 'calendar';
				break;
		}
		this.router.navigate(['submissions', view]);
	}
}
