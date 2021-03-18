import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ProcessService } from '../../process.service';
import { ProcessExportRequest } from './ProcessExportRequest';
import { Process } from '../../Process';

@Component({
	selector: 'app-process-dtl-export',
	templateUrl: './process-dtl-export.component.html',
	styleUrls: [ './process-dtl-export.component.scss' ]
})
export class ProcessDtlExportComponent implements OnInit {
	isAdmin = false;
	processId = 0;
	requests: Array<ProcessExportRequest> = [];
	columns = [
		{ field: 'id', header: 'ID' },
		{ field: 'settings', header: 'Settings' },
		{ field: 'state', header: 'State' }
	];

	summary = true;
	steps = false;
	notifications = false;
	users = false;
	schedules = false;

	display = false;
	exportId = 0;
	notes = '';
	action = '';
	styleClass = '';
	correctFormat = false;
	importFile;

	constructor(private activatedRoute: ActivatedRoute, private processService: ProcessService) {
		this.activatedRoute.parent.params.subscribe(params => {
			this.processId = params['id'] || 0;
		});
	}

	private loadRequests() {
		this.processService.getExportRequests(this.processId).subscribe((requests) => (this.requests = requests));
	}

	ngOnInit(): void {
		const session = JSON.parse(localStorage.getItem('session'));
		if (session && session.user) {
			const role = session.user.role.description;
			if (role === 'admin') {
				this.isAdmin = true;
			}
		}
		this.loadRequests();
	}

	onExport(): void {
		const settings = [];
		if (this.summary) {
			settings.push('summary');
		}
		if (this.steps) {
			settings.push('steps');
		}
		if (this.notifications) {
			settings.push('notifications');
		}
		if (this.users) {
			settings.push('users');
		}
		if (this.schedules) {
			settings.push('schedules');
		}
		this.processService.createExportRequest(this.processId, settings).subscribe(() => {
			this.summary = true;
			this.steps = false;
			this.notifications = false;
			this.users = false;
			this.schedules = false;
			this.loadRequests();
		});
	}

	onImport(): void {
		this.processService.uploadImport(this.processId, this.importFile).subscribe((process: Process) => {
			location.assign(`process/${process.id}`);
		});
	}

	onFileSelected(event): void {
		const files = event.target.files;
		if (files && files.length > 0) {
			const reader = new FileReader();
			reader.onload = (evt: any) => {
				let content = evt.target.result as string;
				this.correctFormat = content.startsWith('data:application/json;base64,');
				if (this.correctFormat) {
					content = content.replace('data:application/json;base64,', '');
					this.importFile = JSON.parse(atob(content));
				}
			};
			reader.readAsDataURL(files[0]);
		}
	}

	approve(id): void {
		this.display = true;
		this.exportId = id;
		this.action = 'Accept';
		this.styleClass = 'ui-button-success';
	}

	decline(id): void {
		this.display = true;
		this.exportId = id;
		this.action = 'Decline';
		this.styleClass = 'ui-button-danger';
	}

	onSubmit() {
		this.display = false;
		if (this.action === 'Accept') {
			this.processService.approveExportRequest(this.processId, this.exportId, this.notes).subscribe(() => {
				this.loadRequests();
			});
		} else {
			this.processService.declineExportRequest(this.processId, this.exportId, this.notes).subscribe(() => {
				this.loadRequests();
			});
		}
	}

	export(id): void {
		this.processService.downloadExport(this.processId, id).subscribe((data) => {
			const blob = new Blob([ JSON.stringify(data) ], { type: 'application/octet-stream' });
			const url = window.URL.createObjectURL(blob);
			const a = document.createElement('a');
			document.body.appendChild(a);
			a.style.display = 'none';
			a.href = url;
			a.download = 'process.json';
			a.click();
			window.URL.revokeObjectURL(url);
		});
	}
}
