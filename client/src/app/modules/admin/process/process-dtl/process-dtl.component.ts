import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AuditLogService } from 'src/app/core/services/audit-log.service';
import { ProcessService } from '../process.service';
import { SessionService } from 'src/app/core/session/session.service';
import { browser } from 'protractor';

@Component({
	selector: 'app-process-dtl',
	templateUrl: './process-dtl.component.html',
	styleUrls: ['./process-dtl.component.scss'],
	providers: [AuditLogService],
})
export class ProcessDtlComponent implements OnInit {
	activeItem;

	PROCESS_MENU_USER_PARENT = [
		{ label: 'Notifications', routerLink: 'notifications' },
		{ label: 'Parent Setup', routerLink: 'parent-setup' },
		{ label: 'Critical Calendar', routerLink: 'critical' },
	];

	PROCESS_MENU_USER = [
		{ label: 'Steps', routerLink: 'steps' },
		{ label: 'Notifications', routerLink: 'notifications' },
		{ label: 'Parent Setup', routerLink: 'parent-setup' },
		{ label: 'Critical Calendar', routerLink: 'critical' },
	];

	PROCESS_MENU_PARENT = [
		{ label: 'Notifications', routerLink: 'notifications' },
		{ label: 'Users', routerLink: 'users' },
		{ label: 'Parent Setup', routerLink: 'parent-setup' },
		{ label: 'Critical Calendar', routerLink: 'critical' },
		{ label: 'Export', routerLink: 'export' },
	];

	PROCESS_MENU = [
		{ label: 'Steps', routerLink: 'steps' },
		{ label: 'Notifications', routerLink: 'notifications' },
		{ label: 'Users', routerLink: 'users' },
		{ label: 'Parent Setup', routerLink: 'parent-setup' },
		{ label: 'Critical Calendar', routerLink: 'critical' },
		{ label: 'Export', routerLink: 'export' },
	];

	isParent = false;

	processMenu = [{ label: 'Summary', routerLink: 'summary' }];

	constructor(
		private route: ActivatedRoute,
		private auditLogSvc: AuditLogService,
		private processSvc: ProcessService,
		private sessionService: SessionService
	) {
		this.auditLogSvc.newAuditLog('New-Modify Process').subscribe((value) => {});
	}

	ngOnInit() {
		const processId = Number.parseInt(this.route.snapshot.paramMap.get('id'), 10);
		if (processId !== 0) {
			this.processSvc.getProcess(processId).subscribe((process: any) => {
				this.isParent = process.isParent;
				if (this.isParent) {
					if (this.sessionService.role == 'admin' || this.sessionService.isUserOfProcess(process.id)) {
						this.processMenu = this.processMenu.concat(this.PROCESS_MENU_PARENT);
					} else {
						this.processMenu = this.processMenu.concat(this.PROCESS_MENU_USER_PARENT);
					}
				} else {
					if (this.sessionService.role == 'admin' || this.sessionService.isUserOfProcess(process.id)) {
						this.processMenu = this.processMenu.concat(this.PROCESS_MENU);
					} else {
						this.processMenu = this.processMenu.concat(this.PROCESS_MENU_USER);
					}
				}
				this.updateLocation();
			});
		}
	}

	updateLocation() {
		const cLocation = window.location.pathname.split('/').pop();
		switch (cLocation) {
			case 'summary':
				this.activeItem = this.processMenu[0];
				break;
			case 'notifications':
				this.activeItem = this.processMenu[1];
				break;
			case 'users':
				this.activeItem = this.processMenu[2];
				break;
			case 'parent-setup':
				this.activeItem = this.processMenu[3];
				break;
			case 'critical':
				this.activeItem = this.processMenu[4];
				break;
			case 'export':
				this.activeItem = this.processMenu[5];
				break;
			default:
				break;
		}
	}
}
