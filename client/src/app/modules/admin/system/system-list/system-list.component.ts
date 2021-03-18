import { Component, OnInit, ViewChild } from '@angular/core';
import { System } from '../System';
import { SystemService } from '../system.service';
import { MessageService } from 'primeng/api';
import { MultiSelectModule } from 'primeng/multiselect';
import { SelectItem } from 'primeng/api';

@Component({
	selector: 'app-system-list',
	template: `
		<div class="list-banner">
			<div style="text-align: right">
				<button
					pButton
					type="button"
					icon="pi pi-refresh"
					class="p-button-raised p-button-text p-button-plain"
					(click)="getSystems()"
				></button>
				<p-button label="New System" (onClick)="openNewSystemDialog()"></p-button>
			</div>
		</div>
		<div>
			<div class="mx-auto">
				<p-dropdown
					[(ngModel)]="selectedSystem"
					[options]="systems"
					placeholder="Select System"
					optionLabel="name"
					dataKey="id"
					filter="true"
					(onChange)="onChangeSystem($event)"
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
		</div>
		<br />
		<p-card>
			<app-ge-table
				[value]="systems"
				[columns]="columns"
				routerLink="/system/"
				(deleteRecord)="onDeleteRecord($event)"
				[loading]="loading"
			></app-ge-table>
		</p-card>
		<p-dialog
			header="New System"
			[(visible)]="newSystemDialog"
			[style]="{ 'min-width': '500px', 'max-width': '800px' }"
		>
			<app-system-dtl *ngIf="newSystemDialog"></app-system-dtl>
		</p-dialog>
	`,
})
export class SystemListComponent implements OnInit {
	systems: System[];
	selectedSystem;
	columns = [
		{ field: 'name', header: 'Name' },
		{ field: 'closePhaseName', header: 'Close Phase' },
		{ field: 'appOwnerName', header: 'App Owner' },
	];
	loading = false;
	newSystemDialog: boolean;
	constructor(private systemSvc: SystemService, private msgSvc: MessageService) {}

	ngOnInit() {
		this.loading = true;
		this.getSystems();
	}

	getSystems() {
		this.systemSvc.getAllSystems().subscribe((value) => {
			this.loading = false;
			this.systems = value;
			this.systems.map((s) => {
				s.appOwnerName = s.appOwner ? s.appOwner.name : '';
				s.closePhaseName = s.closePhase ? s.closePhase.name : '';
			});
		});
	}

	onDeleteRecord(id) {
		this.systemSvc.deleteSystem(id).subscribe((value) => {
			this.systems = this.systems.filter((s) => {
				return s.id !== value.id;
			});
			this.msgSvc.add({
				severity: 'error',
				summary: `It's not me, it's you!`,
				detail: `System was deleted`,
			});
		});
	}

	openNewSystemDialog() {
		this.newSystemDialog = true;
	}

	onChangeSystem(event) {
		this.systems = this.systems.filter((s) => {
			return s.id === event.value.id;
		});
	}

	resetFilters() {
		this.selectedSystem = 0;
		this.loading = true;
		this.getSystems();
	}
}
