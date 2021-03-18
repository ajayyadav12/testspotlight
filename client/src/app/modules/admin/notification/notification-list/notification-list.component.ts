import { Component, OnInit } from '@angular/core';
import { NotificationService } from '../notification.service';
import { MessageService } from 'primeng/api';

@Component({
	selector: 'app-notification-list',
	template: `
		<div class="list-banner">
			<button
				pButton
				type="button"
				icon="pi pi-refresh"
				class="p-button-raised p-button-text p-button-plain"
				(click)="getNotifications()"
			></button>
			<p-button label="New Template" (onClick)="openNewTemplateDialog()"></p-button>
		</div>
		<div>
			<div class="mx-auto">
				<p-dropdown
					[(ngModel)]="selectedTemplate"
					[options]="templates"
					optionLabel="name"
					inputId="template"
					dataKey="id"
					filter="true"
					placeholder="Select a template"
					(onChange)="onChangeTemplate($event)"
					[showClear]="true"
				>
				</p-dropdown>

				<button
					pButton
					type="button"
					label="Clear"
					(click)="resetFilters()"
					class="p-button-text p-button-plain"
				></button>
			</div>
			<br />
			<p-card>
				<app-ge-table
					[value]="templates"
					[columns]="columns"
					routerLink="/notification/"
					(deleteRecord)="onDeleteRecord($event)"
					[loading]="loading"
				></app-ge-table>
			</p-card>

			<p-dialog
				header="New Template"
				[(visible)]="newTemplateDialog"
				[style]="{ 'min-width': '600px', 'max-width': '1000px' }"
			>
				<app-notification-dtl *ngIf="newTemplateDialog"></app-notification-dtl>
			</p-dialog>
		</div>
	`,
})
export class NotificationListComponent implements OnInit {
	templates: any[];
	selectedTemplate;
	columns = [
		{ field: 'id', header: 'ID' },
		{ field: 'name', header: 'Name' },
		{ field: 'subject', header: 'Subject' },
	];
	loading = false;
	newTemplateDialog: boolean;
	constructor(private notificatioSvc: NotificationService, private msgSvc: MessageService) {}

	ngOnInit() {
		this.loading = true;
		this.getNotifications();
	}

	getNotifications() {
		this.notificatioSvc.getNotificationTemplates().subscribe((value) => {
			this.templates = value;
			this.loading = false;
		});
	}

	onDeleteRecord(id) {
		this.notificatioSvc.deleteNotificationtemplate(id).subscribe((value) => {
			this.templates = this.templates.filter((p) => {
				return p.id !== id;
			});
			this.msgSvc.add({
				severity: 'error',
				summary: `It's not me, it's you!`,
				detail: `Template was deleted`,
			});
		});
	}

	openNewTemplateDialog() {
		this.newTemplateDialog = true;
	}

	onChangeTemplate(event) {
		this.templates = this.templates.filter((s) => {
			return s.id === event.value.id;
		});
	}

	resetFilters() {
		this.selectedTemplate = 0;
		this.loading = true;
		this.getNotifications();
	}
}
