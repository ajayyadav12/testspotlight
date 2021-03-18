import { Component, OnInit } from '@angular/core';
import { User } from '../User';
import { UserService } from '../user.service';
import { MessageService } from 'primeng/api';

@Component({
	selector: 'app-user-list',
	template: `
		<div class="list-banner">
			<button
				pButton
				type="button"
				icon="pi pi-refresh"
				class="p-button-raised p-button-text p-button-plain"
				(click)="getUsers()"
			></button>
			<p-button label="New User" (onClick)="openNewUserDialog()"></p-button>
		</div>

		<div class="mx-auto">
			<p-dropdown
				[(ngModel)]="selectedUser"
				[options]="users"
				optionLabel="name"
				inputId="user"
				dataKey="id"
				filter="true"
				placeholder="Select an user"
				(onChange)="onChangeUser($event)"
				[showClear]="true"
			>
			</p-dropdown>
			<button
				pButton
				class="p-button-text p-button-plain"
				type="button"
				label="Clear"
				(click)="resetFilters()"
			></button>
		</div>
		<br />
		<p-card>
			<app-ge-table
				[value]="users"
				[columns]="columns"
				[loading]="loading"
				routerLink="/user/"
				(deleteRecord)="onDeleteRecord($event)"
			></app-ge-table>
		</p-card>
		<p-dialog
			header="New User"
			[(visible)]="newUserDialog"
			[style]="{ 'min-width': '500px', 'max-width': '800px' }"
		>
			<app-user-dtl *ngIf="newUserDialog"></app-user-dtl>
		</p-dialog>
	`,
})
export class UserListComponent implements OnInit {
	loading = false;
	users: User[];
	columns = [
		{ field: 'name', header: 'Name' },
		{ field: 'sso', header: 'SSO' },
		{ field: 'roleName', header: 'Role' },
	];
	newUserDialog: boolean;
	selectedUser;
	constructor(private userSvc: UserService, private msgSvc: MessageService) {}

	ngOnInit() {
		this.loading = true;
		this.getUsers();
	}

	getUsers() {
		this.userSvc.getUsers().subscribe((value) => {
			this.users = value;
			this.users.map((u) => {
				u.roleName = u.role.description;
			});
			this.loading = false;
		});
	}

	onDeleteRecord(id) {
		this.userSvc.deleteUser(id).subscribe((value) => {
			this.users = this.users.filter((p) => {
				return p.id !== id;
			});
			this.msgSvc.add({
				severity: 'error',
				summary: `It's not me, it's you!`,
				detail: `User was deleted`,
			});
		});
	}

	openNewUserDialog() {
		this.newUserDialog = true;
	}

	onChangeUser(event) {
		this.users = this.users.filter((s) => {
			return s.id === event.value.id;
		});
	}

	resetFilters() {
		this.selectedUser = 0;
		this.loading = true;
		this.getUsers();
	}
}
