import { UserService } from './../../../user/user.service';
import { ActivatedRoute, Router } from '@angular/router';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Component, OnInit, Input } from '@angular/core';
import { Subject } from 'rxjs';
import { ProcessService } from '../../process.service';
import { MessageService } from 'primeng/api';
import { SidebarService } from 'src/app/core/sidebar/sidebar.service';
import { SessionService } from 'src/app/core/session/session.service';
import { SystemService } from '../../../system/system.service';
import { Process } from '../../Process';

@Component({
	selector: 'app-process-dtl-summary',
	templateUrl: './process-dtl-summary.component.html',
	styleUrls: ['./process-dtl-summary.component.scss'],
})
export class ProcessDtlSummaryComponent implements OnInit {
	@Input() isVisible: boolean = true;
	processForm: FormGroup;
	process: Process;
	display = false;

	// Dropdowns
	systems = [];
	users = [];
	processTypes = [];
	feedTypes = [];

	processToken = '';
	id: number;

	combosObservable = new Subject<boolean>();
	combosCount = 0;

	get isAdmin() {
		return this.sessionSvc.role === 'admin';
	}

	get isApplication() {
		return this.sessionSvc.role === 'application';
	}

	get isUser() {
		return this.sessionSvc.role === 'user';
	}

	get canEdit() {
		return this.id === 0 || this.sessionSvc.isUserOfProcess(this.id);
	}

	get submitButtonLabel() {
		if (this.id === 0) {
			return this.isAdmin ? 'Save' : 'Submit for Approval';
		} else {
			return 'Save';
		}
	}

	constructor(
		private processSvc: ProcessService,
		private systemSvc: SystemService,
		private fb: FormBuilder,
		private msgSvc: MessageService,
		private route: ActivatedRoute,
		private sidebarSvc: SidebarService,
		private sessionSvc: SessionService,
		private userSvc: UserService,
		private router: Router
	) {
		this.route.parent.params.subscribe((params) => {
			this.id = parseInt(params['id']) || 0;
			if (this.id != 0) {
				this.getProcess(this.id);
			} else {
				this.getAppOwner();
				this.getAppOwner();
				this.getSenderReceiver();
				this.getFeedType();
				this.getFeedType();
			}
		});
		this.setupForm();
	}

	ngOnInit() {}

	ngOnDestroy() {
		this.combosObservable.unsubscribe();
	}

	setupForm() {
		this.processForm = this.fb.group({
			approved: '0',
			appOwner: null,
			critical: false,
			feedType: [null, Validators.required],
			functionalOwner: null,
			id: [{ value: null, disabled: true }],
			isParent: false,
			longRunningSubAlrt: null,
			longRunningStepAlrt: null,
			name: ['', Validators.required],
			processType: [null, Validators.required],
			processParent: [null],
			processLevel: null,
			receiver: [null, Validators.required],
			sender: [null, Validators.required],
			submissionEscalationAlrt: null,
			supportTeamEmail: ['', [Validators.email, Validators.required]],
			technicalOwner: null,
			maxRunTimeHours: [12, Validators.min(0)],
			maxRunTimeMinutes: [0, Validators.min(0)],
			submissionDelayedEscalationAlrt: null,
			requiredStepAlrt: null,
			ignoreChildSequence: null,
		});
		if (!this.isAdmin && !this.canEdit) {
			this.processForm.disable();
		}
	}

	copyToken() {
		if (this.processForm.disabled) {
			return;
		}
		const selBox = document.createElement('textarea');
		selBox.style.position = 'fixed';
		selBox.style.left = '0';
		selBox.style.top = '0';
		selBox.style.opacity = '0';
		selBox.value = this.processToken;
		document.body.appendChild(selBox);
		selBox.focus();
		selBox.select();
		document.execCommand('copy');
		document.body.removeChild(selBox);
		this.msgSvc.add({
			severity: 'success',
			summary: 'Now you are ready!',
			detail: `Token has been copied`,
		});
	}

	getFeedType() {
		this.processSvc.getFeedTypes().subscribe((value: any[]) => {
			this.feedTypes = value;
		});
	}

	getSenderReceiver() {
		this.systemSvc.getAllSystems().subscribe((value) => {
			this.systems = value;
		});
	}

	getAppOwner() {
		this.userSvc.getUsers().subscribe((value) => {
			this.users = value;
		});
	}

	getProcessTypes() {
		this.processSvc.getAllProcessTypes().subscribe((value: any[]) => {
			this.processTypes = value;
		});
	}

	getProcess(id: number) {
		this.processSvc.getProcess(id).subscribe((process: any) => {
			this.process = process;
			this.sidebarSvc.title = 'Process: ' + process.name;
			this.sessionSvc.globalProcessName = process.name;
			this.processApprovalStatusAction(process.approved);
			this.populateDropDowns(process);
			this.processForm.setValue(process);
		});
	}

	populateDropDowns(process) {
		this.systems = [process.sender, process.receiver];
		this.feedTypes = [process.feedType];
		this.processTypes = [process.processType];
		this.users = [process.appOwner];
	}

	processApprovalStatusAction(approved) {
		if (approved === '0') {
			this.processForm.disable();
			this.msgSvc.add({
				severity: 'warn',
				summary: 'Patience is a virtue...',
				detail: 'Spotlight Admin Team is reviewing this process',
				key: 'persist',
			});
		} else if (approved === 'N') {
			this.processForm.disable();
			this.msgSvc.add({
				severity: 'error',
				summary: 'Sorry not sorry',
				detail: 'Spotlight Admin Team rejected this process',
				key: 'persist',
			});
		}
	}

	onCopy(): void {
		this.display = true;
	}

	save() {
		if (this.id !== 0) {
			this.processSvc.updateProcess(this.id, this.processForm.getRawValue()).subscribe((value) => {
				this.msgSvc.add({
					severity: 'success',
					summary: 'All set!',
					detail: `Process '${value.name}' was updated`,
				});
				location.assign(`process/${value.id}`);
			});
		} else {
			this.processSvc.newProcess(this.processForm.getRawValue()).subscribe((value) => {
				this.msgSvc.add({
					severity: 'success',
					summary: 'Congrats! A new process was added!',
					detail: `Process '${value.name}' created`,
				});
				location.assign(`process/${value.id}`);
			});
		}
	}

	getProcessToken() {
		if (this.processForm.disabled) {
			return;
		}
		this.processSvc.getToken(this.processForm.getRawValue().id).subscribe((value) => {
			this.processToken = value.token;
		});
	}
}
